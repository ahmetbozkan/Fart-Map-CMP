import { deleteApp, initializeApp, type App } from "firebase-admin/app";
import { getFirestore, Timestamp, type Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from "vitest";
import { projectPublicLocation } from "../../src/lib/location";
import { handleFartCreated } from "../../src/triggers/fartCreated";

// Admin SDK against the Firestore emulator (FIRESTORE_EMULATOR_HOST is set by `firebase emulators:exec`).
let app: App;
let db: Firestore;

const PRECISE = { lat: 41.008234, lng: 28.978359 };
const OLD = Timestamp.fromMillis(1_700_000_000_000);
const COORD_KEYS = ["lat", "lng", "public_lat", "public_lng", "latitude", "longitude"];

beforeAll(() => {
  process.env.FIRESTORE_EMULATOR_HOST ??= "127.0.0.1:8080";
  app = initializeApp({ projectId: "demo-fartmap" }, "fart-created-test");
  db = getFirestore(app);
});
afterAll(async () => {
  await deleteApp(app);
});
beforeEach(async () => {
  for (const collection of ["function_runs", "farts"]) {
    await db.recursiveDelete(db.collection(collection));
  }
  vi.restoreAllMocks();
});

async function seedFart(fartId: string, publicFields: Record<string, unknown>, location: Record<string, unknown> | null) {
  await db.doc(`farts/${fartId}`).set({ fart_id: fartId, author_user_id: "alice", updated_at: OLD, ...publicFields });
  if (location) await db.doc(`farts/${fartId}/private/location`).set({ ...location, updated_at: OLD });
}

/** Every logger call's structured payload, so tests can assert that no coordinate value was logged. */
function spyLogs() {
  const calls: unknown[][] = [];
  for (const level of ["info", "warn", "error"] as const) {
    vi.spyOn(logger, level).mockImplementation((...args: unknown[]) => {
      calls.push([level, ...args]);
    });
  }
  return calls;
}

function expectNoCoordinates(calls: unknown[][]) {
  for (const call of calls) {
    for (const arg of call) {
      if (arg && typeof arg === "object") {
        for (const key of COORD_KEYS) {
          const value = (arg as Record<string, unknown>)[key];
          expect(value === undefined || value === "[redacted]", `log carried ${key}`).toBe(true);
        }
      }
    }
    expect(JSON.stringify(call)).not.toContain("41.008");
  }
}

describe("onFartCreated: public projection re-validation (ADR-0019 §4)", () => {
  it("overwrites public fields that don't match the private location", async () => {
    const logs = spyLogs();
    await seedFart("f1", { public_lat: 41.008234, public_lng: 28.978359, coarse_location_key: "sxk9wz" }, PRECISE);

    expect(await handleFartCreated(db, "event-1", "f1")).toBe("ran");

    const expected = projectPublicLocation("f1", PRECISE.lat, PRECISE.lng);
    const fart = (await db.doc("farts/f1").get()).data()!;
    expect(fart.public_lat).toBeCloseTo(expected.public_lat, 12);
    expect(fart.public_lng).toBeCloseTo(expected.public_lng, 12);
    expect(fart.coarse_location_key).toBe(expected.coarse_location_key);
    expect((fart.updated_at as Timestamp).toMillis()).not.toBe(OLD.toMillis());
    expect(logs.some((c) => c[1] === "fart_projection_checked" && (c[2] as { corrected: boolean }).corrected)).toBe(true);
    expectNoCoordinates(logs);
  });

  it("leaves a fart whose public fields already match untouched", async () => {
    const logs = spyLogs();
    const expected = projectPublicLocation("f2", PRECISE.lat, PRECISE.lng);
    await seedFart("f2", { ...expected }, PRECISE);

    expect(await handleFartCreated(db, "event-2", "f2")).toBe("ran");

    const fart = (await db.doc("farts/f2").get()).data()!;
    expect((fart.updated_at as Timestamp).toMillis()).toBe(OLD.toMillis());
    expect(fart.public_lat).toBe(expected.public_lat);
    expectNoCoordinates(logs);
  });

  it("runs once per event id", async () => {
    await seedFart("f3", { public_lat: 0, public_lng: 0, coarse_location_key: "s00000" }, PRECISE);

    expect(await handleFartCreated(db, "event-3", "f3")).toBe("ran");
    expect(await handleFartCreated(db, "event-3", "f3")).toBe("skipped");
    expect(await handleFartCreated(db, "event-4", "f3")).toBe("ran");
  });

  it("logs an error and writes nothing when the private location is missing or invalid", async () => {
    const logs = spyLogs();
    const stale = { public_lat: 1, public_lng: 2, coarse_location_key: "s00000" };
    await seedFart("f4", stale, null);
    await seedFart("f5", stale, { lat: "x", lng: 200 });

    expect(await handleFartCreated(db, "event-5", "f4")).toBe("ran");
    expect(await handleFartCreated(db, "event-6", "f5")).toBe("ran");

    for (const id of ["f4", "f5"]) {
      const fart = (await db.doc(`farts/${id}`).get()).data()!;
      expect(fart.public_lat).toBe(1);
      expect((fart.updated_at as Timestamp).toMillis()).toBe(OLD.toMillis());
    }
    const skips = logs.filter((c) => c[0] === "error" && c[1] === "fart_projection_skipped").map((c) => c[2]);
    expect(skips).toEqual([
      { fartId: "f4", reason: "location_missing" },
      { fartId: "f5", reason: "location_invalid" },
    ]);
    expectNoCoordinates(logs);
  });

  it("logs an error and doesn't throw when the fart doc is gone", async () => {
    const logs = spyLogs();

    expect(await handleFartCreated(db, "event-7", "gone")).toBe("ran");

    expect((await db.doc("farts/gone").get()).exists).toBe(false);
    expect(logs.some((c) => c[0] === "error" && (c[2] as { reason?: string }).reason === "fart_missing")).toBe(true);
  });
});

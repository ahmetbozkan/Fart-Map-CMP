import { deleteApp, initializeApp, type App } from "firebase-admin/app";
import { getFirestore, Timestamp, type Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from "vitest";
import { handleFartCreated } from "../../src/triggers/fartCreated";
import { handleFartRemoved } from "../../src/triggers/fartRemoved";

// Admin SDK against the Firestore emulator (FIRESTORE_EMULATOR_HOST is set by `firebase emulators:exec`).
let app: App;
let db: Firestore;

const PRECISE = { lat: 41.008234, lng: 28.978359 };
const OLD = Timestamp.fromMillis(1_700_000_000_000);
const COORD_KEYS = ["lat", "lng", "public_lat", "public_lng", "latitude", "longitude"];

beforeAll(() => {
  process.env.FIRESTORE_EMULATOR_HOST ??= "127.0.0.1:8080";
  app = initializeApp({ projectId: "demo-fartmap" }, "fart-removed-test");
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

async function seedFart(fartId: string, isRemoved: boolean, withLocation = true) {
  await db.doc(`farts/${fartId}`).set({
    fart_id: fartId,
    author_user_id: "alice",
    public_lat: 41.0,
    public_lng: 29.0,
    coarse_location_key: "sxk9wz",
    is_removed: isRemoved,
    updated_at: OLD,
  });
  if (withLocation) await db.doc(`farts/${fartId}/private/location`).set({ ...PRECISE, updated_at: OLD });
}

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

const exists = async (path: string) => (await db.doc(path).get()).exists;

describe("onFartRemoved: purge the precise location when a fart is removed (ADR-0019)", () => {
  it("deletes private/location on the flip to removed and leaves the public doc", async () => {
    const logs = spyLogs();
    await seedFart("f1", true);

    expect(await handleFartRemoved(db, "event-1", "f1", { is_removed: false }, { is_removed: true })).toBe("ran");

    expect(await exists("farts/f1/private/location")).toBe(false);
    const fart = (await db.doc("farts/f1").get()).data()!;
    expect(fart.is_removed).toBe(true);
    expect(fart.public_lat).toBe(41.0);
    expect(logs.some((c) => c[1] === "fart_location_purged")).toBe(true);
    expectNoCoordinates(logs);
  });

  it("deletes nothing when is_removed doesn't flip", async () => {
    await seedFart("f2", false);
    await seedFart("f3", true);

    await handleFartRemoved(db, "event-2", "f2", { is_removed: false }, { is_removed: false });
    await handleFartRemoved(db, "event-3", "f3", { is_removed: true }, { is_removed: true });

    expect(await exists("farts/f2/private/location")).toBe(true);
    expect(await exists("farts/f3/private/location")).toBe(true);
  });

  it("runs once per event id, and a second removal event on a purged fart is harmless", async () => {
    await seedFart("f4", true);

    expect(await handleFartRemoved(db, "event-4", "f4", { is_removed: false }, { is_removed: true })).toBe("ran");
    expect(await handleFartRemoved(db, "event-4", "f4", { is_removed: false }, { is_removed: true })).toBe("skipped");
    expect(await handleFartRemoved(db, "event-5", "f4", { is_removed: false }, { is_removed: true })).toBe("ran");
    expect(await exists("farts/f4/private/location")).toBe(false);
  });

  it("is fine with a missing location doc", async () => {
    await seedFart("f5", true, false);

    await expect(
      handleFartRemoved(db, "event-6", "f5", { is_removed: false }, { is_removed: true }),
    ).resolves.toBe("ran");
  });

  it("rethrows a delete failure so the trigger retries, and releases the claim", async () => {
    await seedFart("f6", true);
    const failing = {
      doc: () => ({ delete: () => Promise.reject(new Error("boom")) }),
    } as unknown as Firestore;
    // The claim lives in the real db; only the purge goes through the failing double.
    const mixed = new Proxy(db, {
      get: (target, prop, receiver) =>
        prop === "doc"
          ? (path: string) => (path.endsWith("/private/location") ? failing.doc(path) : target.doc(path))
          : Reflect.get(target, prop, receiver),
    });

    await expect(
      handleFartRemoved(mixed, "event-7", "f6", { is_removed: false }, { is_removed: true }),
    ).rejects.toThrow("boom");

    expect(await exists("farts/f6/private/location")).toBe(true);
    expect(await handleFartRemoved(db, "event-7", "f6", { is_removed: false }, { is_removed: true })).toBe("ran");
    expect(await exists("farts/f6/private/location")).toBe(false);
  });
});

describe("onFartCreated on a removed fart (log then delete while offline)", () => {
  it("returns quietly without an error log when the location was already purged", async () => {
    const logs = spyLogs();
    await seedFart("r1", true, false);

    expect(await handleFartCreated(db, "event-c1", "r1")).toBe("ran");

    expect(logs.filter((c) => c[0] === "error")).toEqual([]);
    expect(logs.some((c) => c[1] === "fart_projection_removed")).toBe(true);
    expect(((await db.doc("farts/r1").get()).data()!.updated_at as Timestamp).toMillis()).toBe(OLD.toMillis());
  });

  it("doesn't rewrite a removed fart that still has its location", async () => {
    await db.doc("farts/r2").set({
      fart_id: "r2",
      author_user_id: "alice",
      public_lat: 1,
      public_lng: 2,
      coarse_location_key: "s00000",
      is_removed: true,
      updated_at: OLD,
    });
    await db.doc("farts/r2/private/location").set({ ...PRECISE, updated_at: OLD });

    expect(await handleFartCreated(db, "event-c2", "r2")).toBe("ran");

    const fart = (await db.doc("farts/r2").get()).data()!;
    expect(fart.public_lat).toBe(1);
    expect((fart.updated_at as Timestamp).toMillis()).toBe(OLD.toMillis());
  });
});

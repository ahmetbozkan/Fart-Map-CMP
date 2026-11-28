import { deleteApp, initializeApp, type App } from "firebase-admin/app";
import { getFirestore, type Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from "vitest";
import { handleReactionWritten } from "../../src/triggers/reactionWritten";

// Admin SDK against the Firestore emulator (FIRESTORE_EMULATOR_HOST is set by `firebase emulators:exec`).
let app: App;
let db: Firestore;

beforeAll(() => {
  process.env.FIRESTORE_EMULATOR_HOST ??= "127.0.0.1:8080";
  app = initializeApp({ projectId: "demo-fartmap" }, "reaction-written-test");
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

async function seedFart(fartId: string, count = 0, breakdown: Record<string, number> = {}) {
  await db.doc(`farts/${fartId}`).set({
    author_user_id: "alice",
    reaction_count: count,
    reaction_breakdown: breakdown,
  });
}

const fart = async (fartId: string) => (await db.doc(`farts/${fartId}`).get()).data();

describe("onReactionWritten: parent counters follow the reaction docs (SOCIAL-01)", () => {
  it("counts a new reaction and uncounts a removed one", async () => {
    await seedFart("f1");
    await handleReactionWritten(db, "evt-create", "f1", undefined, { type: "DEAD" });
    expect(await fart("f1")).toMatchObject({ reaction_count: 1, reaction_breakdown: { DEAD: 1 } });

    await handleReactionWritten(db, "evt-delete", "f1", { type: "DEAD" }, undefined);
    expect(await fart("f1")).toMatchObject({ reaction_count: 0, reaction_breakdown: { DEAD: 0 } });
  });

  it("moves one unit on a replace and leaves the total alone", async () => {
    await seedFart("f2", 2, { RIP: 1, LEGEND: 1 });
    await handleReactionWritten(db, "evt-replace", "f2", { type: "RIP" }, { type: "LEGEND" });
    expect(await fart("f2")).toMatchObject({ reaction_count: 2, reaction_breakdown: { RIP: 0, LEGEND: 2 } });
  });

  it("runs once per event: a redelivery never applies the delta twice", async () => {
    await seedFart("f3");
    expect(await handleReactionWritten(db, "evt-dup", "f3", undefined, { type: "TOXIC" })).toBe("ran");
    expect(await handleReactionWritten(db, "evt-dup", "f3", undefined, { type: "TOXIC" })).toBe("skipped");
    expect(await fart("f3")).toMatchObject({ reaction_count: 1, reaction_breakdown: { TOXIC: 1 } });
  });

  it("skips a missing parent without inventing one", async () => {
    await handleReactionWritten(db, "evt-orphan", "gone", undefined, { type: "DEAD" });
    expect((await db.doc("farts/gone").get()).exists).toBe(false);
  });

  it("logs the fart id and outcome, never coordinates", async () => {
    await seedFart("f4");
    const calls: unknown[][] = [];
    for (const level of ["info", "warn", "error"] as const) {
      vi.spyOn(logger, level).mockImplementation((...args: unknown[]) => {
        calls.push([level, ...args]);
      });
    }
    await handleReactionWritten(db, "evt-log", "f4", undefined, { type: "DEAD" });
    expect(JSON.stringify(calls)).not.toMatch(/lat|lng|41\.0|28\.9/);
  });
});

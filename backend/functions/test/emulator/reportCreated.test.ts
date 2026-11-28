import { deleteApp, initializeApp, type App } from "firebase-admin/app";
import { getFirestore, type Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from "vitest";
import { handleReportCreated } from "../../src/triggers/reportCreated";

// Admin SDK against the Firestore emulator (FIRESTORE_EMULATOR_HOST is set by `firebase emulators:exec`).
let app: App;
let db: Firestore;

beforeAll(() => {
  process.env.FIRESTORE_EMULATOR_HOST ??= "127.0.0.1:8080";
  app = initializeApp({ projectId: "demo-fartmap" }, "report-created-test");
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

async function seedFart(fartId: string, overrides: Record<string, unknown> = {}) {
  await db.doc(`farts/${fartId}`).set({
    author_user_id: "alice",
    visibility_mode: "PUBLIC",
    report_count: 0,
    is_hidden: false,
    is_removed: false,
    ...overrides,
  });
}

const fart = async (fartId: string) => (await db.doc(`farts/${fartId}`).get()).data();
const report = (reporter: string, fartId: string) => ({
  target_type: "FART", target_id: fartId, reporter_user_id: reporter,
});

/** The public map query shape the rules allow (DataContracts §5.2). */
async function publicIds(): Promise<string[]> {
  const snap = await db.collection("farts")
    .where("visibility_mode", "==", "PUBLIC")
    .where("is_hidden", "==", false)
    .where("is_removed", "==", false)
    .get();
  return snap.docs.map((doc) => doc.id);
}

describe("onReportCreated: count reports, hide at three distinct reporters (MOD-02)", () => {
  it("counts the first two reports and hides the fart on the third", async () => {
    await seedFart("f1");
    expect(await handleReportCreated(db, "evt-1", report("bob", "f1"))).toBe("counted");
    expect(await handleReportCreated(db, "evt-2", report("carol", "f1"))).toBe("counted");
    expect(await fart("f1")).toMatchObject({ report_count: 2, is_hidden: false });
    expect(await publicIds()).toContain("f1");

    expect(await handleReportCreated(db, "evt-3", report("dave", "f1"))).toBe("hidden");
    expect(await fart("f1")).toMatchObject({ report_count: 3, is_hidden: true });
    expect(await publicIds()).not.toContain("f1");
  });

  it("runs once per event: a redelivery never counts twice", async () => {
    await seedFart("f2");
    expect(await handleReportCreated(db, "evt-dup", report("bob", "f2"))).toBe("counted");
    expect(await handleReportCreated(db, "evt-dup", report("bob", "f2"))).toBe("skipped_duplicate");
    expect(await fart("f2")).toMatchObject({ report_count: 1 });
  });

  it("skips the author's own report, a removed, private or missing fart", async () => {
    await seedFart("f3");
    expect(await handleReportCreated(db, "evt-self", report("alice", "f3"))).toBe("skipped_self");
    expect(await fart("f3")).toMatchObject({ report_count: 0, is_hidden: false });

    await seedFart("f4", { is_removed: true });
    expect(await handleReportCreated(db, "evt-removed", report("bob", "f4"))).toBe("skipped_removed");
    expect(await fart("f4")).toMatchObject({ report_count: 0 });

    await seedFart("f7", { visibility_mode: "PRIVATE" });
    expect(await handleReportCreated(db, "evt-private", report("bob", "f7"))).toBe("skipped_private");
    expect(await fart("f7")).toMatchObject({ report_count: 0, is_hidden: false });

    expect(await handleReportCreated(db, "evt-missing", report("bob", "gone"))).toBe("skipped_missing");
    expect((await db.doc("farts/gone").get()).exists).toBe(false);
  });

  it("keeps a hidden fart hidden and keeps counting", async () => {
    await seedFart("f5", { report_count: 3, is_hidden: true });
    expect(await handleReportCreated(db, "evt-more", report("erin", "f5"))).toBe("counted");
    expect(await fart("f5")).toMatchObject({ report_count: 4, is_hidden: true });
  });

  it("logs the fart id and outcome, never the reporter", async () => {
    await seedFart("f6");
    const calls: unknown[][] = [];
    for (const level of ["info", "warn", "error"] as const) {
      vi.spyOn(logger, level).mockImplementation((...args: unknown[]) => {
        calls.push([level, ...args]);
      });
    }
    await handleReportCreated(db, "evt-log", report("reporter-secret-uid", "f6"));
    const logged = JSON.stringify(calls);
    expect(logged).toContain("f6");
    expect(logged).not.toContain("reporter-secret-uid");
  });
});

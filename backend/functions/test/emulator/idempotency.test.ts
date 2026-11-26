import { deleteApp, initializeApp, type App } from "firebase-admin/app";
import { getFirestore, Timestamp, type Firestore } from "firebase-admin/firestore";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { runOnce } from "../../src/lib/idempotency";

// Admin SDK against the Firestore emulator (FIRESTORE_EMULATOR_HOST is set by `firebase emulators:exec`).
let app: App;
let db: Firestore;

beforeAll(() => {
  process.env.FIRESTORE_EMULATOR_HOST ??= "127.0.0.1:8080";
  app = initializeApp({ projectId: "demo-fartmap" }, "idempotency-test");
  db = getFirestore(app);
});
afterAll(async () => {
  await deleteApp(app);
});
beforeEach(async () => {
  const runs = await db.collection("function_runs").get();
  await Promise.all(runs.docs.map((d) => d.ref.delete()));
});

describe("runOnce", () => {
  it("runs once and skips duplicate deliveries", async () => {
    let calls = 0;
    const work = async () => {
      calls++;
    };
    expect(await runOnce(db, "k1", work)).toBe("ran");
    expect(await runOnce(db, "k1", work)).toBe("skipped");
    expect(calls).toBe(1);
    expect((await db.doc("function_runs/k1").get()).data()?.status).toBe("done");
  });

  it("releases the claim on failure so a redelivery retries", async () => {
    await expect(runOnce(db, "k2", async () => {
      throw new Error("boom");
    })).rejects.toThrow("boom");
    expect((await db.doc("function_runs/k2").get()).exists).toBe(false);

    let retried = false;
    expect(await runOnce(db, "k2", async () => {
      retried = true;
    })).toBe("ran");
    expect(retried).toBe(true);
  });

  it("skips while another delivery holds a live lease, re-claims after it expires", async () => {
    let clock = 1_000_000;
    const now = () => clock;
    await db.doc("function_runs/k3").set({ status: "running", lease_until: Timestamp.fromMillis(clock + 60_000) });

    let calls = 0;
    const work = async () => {
      calls++;
    };
    expect(await runOnce(db, "k3", work, { now })).toBe("skipped");
    clock += 61_000; // the instance that held the lease died
    expect(await runOnce(db, "k3", work, { now })).toBe("ran");
    expect(calls).toBe(1);
  });

  it("a failing delivery never releases a claim that another delivery re-took after lease expiry", async () => {
    let clock = 2_000_000;
    const now = () => clock;
    const failing = runOnce(db, "k5", async () => {
      clock += 120_000; // lease (60s) expires while this delivery is still working
      expect(await runOnce(db, "k5", async () => undefined, { now, leaseMs: 60_000 })).toBe("ran"); // redelivery re-claims and finishes
      throw new Error("first delivery fails late");
    }, { now, leaseMs: 60_000 });
    await expect(failing).rejects.toThrow("first delivery fails late");
    expect((await db.doc("function_runs/k5").get()).data()?.status).toBe("done");
  });

  it("every record carries expire_at for the TTL policy", async () => {
    const clock = 5_000_000;
    await runOnce(db, "k4", async () => undefined, { now: () => clock, ttlMs: 1000 });
    const expireAt = (await db.doc("function_runs/k4").get()).data()?.expire_at as Timestamp;
    expect(expireAt.toMillis()).toBe(clock + 1000);
  });
});

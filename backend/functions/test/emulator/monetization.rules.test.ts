import { assertFails, assertSucceeds, type RulesTestEnvironment } from "@firebase/rules-unit-testing";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import { ALICE, BOB, FREE_AVATAR, catalog, createEnv, seed, signedOut, user } from "./helpers";

let env: RulesTestEnvironment;

beforeAll(async () => {
  env = await createEnv();
});
afterAll(async () => {
  await env.cleanup();
});
beforeEach(async () => {
  await env.clearFirestore();
  await seed(env, {
    ...catalog,
    [`user_entitlements/${ALICE}`]: { user_id: ALICE, items: {}, premium_active: false },
    [`rewarded_unlocks/${ALICE}/tokens/t1`]: { token_id: "t1", item_id: "avatar-paid" },
  });
});

describe("catalog (DataContracts §5.7)", () => {
  it("is readable when signed in, not signed out, never client-written", async () => {
    await assertSucceeds(user(env, BOB).doc(`cosmetic_items/${FREE_AVATAR}`).get());
    await assertSucceeds(user(env, BOB).collection("cosmetic_items").get());
    await assertFails(signedOut(env).doc(`cosmetic_items/${FREE_AVATAR}`).get());
    await assertFails(user(env, BOB).doc("cosmetic_items/new").set({ is_free: true, is_active: true }));
    await assertFails(user(env, BOB).doc(`cosmetic_items/${FREE_AVATAR}`).update({ is_free: false }));
  });
});

describe("entitlements and unlock tokens (ADR-0016, ADR-0017)", () => {
  it("the owner reads their own", async () => {
    await assertSucceeds(user(env, ALICE).doc(`user_entitlements/${ALICE}`).get());
    await assertSucceeds(user(env, ALICE).doc(`rewarded_unlocks/${ALICE}/tokens/t1`).get());
  });

  it("other users cannot read them", async () => {
    await assertFails(user(env, BOB).doc(`user_entitlements/${ALICE}`).get());
    await assertFails(user(env, BOB).doc(`rewarded_unlocks/${ALICE}/tokens/t1`).get());
  });

  it("nobody writes them from the client, not even the owner", async () => {
    const db = user(env, ALICE);
    await assertFails(db.doc(`user_entitlements/${ALICE}`).set({ user_id: ALICE, items: { "avatar-paid": {} } }));
    await assertFails(db.doc(`rewarded_unlocks/${ALICE}/tokens/t2`).set({ token_id: "t2", item_id: "avatar-paid" }));
    await assertFails(db.doc(`rewarded_unlocks/${ALICE}/tokens/t1`).delete());
  });
});

describe("default deny", () => {
  it("closed collections stay closed", async () => {
    const db = user(env, ALICE);
    await assertFails(db.doc("anything/x").get());
    await assertFails(db.doc("function_runs/x").set({ status: "done" }));
    await assertFails(db.doc("farts/f1/comments/c1").set({ text: "hi" }));
    await assertFails(db.doc(`follow_relationships/${ALICE}_${BOB}`).set({ status: "APPROVED" }));
    await assertFails(db.doc(`user_progress/${ALICE}`).set({ xp: 1_000_000 }));
    await assertFails(db.doc("leaderboard_entries/x").set({ rank: 1 }));
  });
});

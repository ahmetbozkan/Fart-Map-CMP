import { assertFails, assertSucceeds, type RulesTestEnvironment } from "@firebase/rules-unit-testing";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import { ALICE, BOB, anonymous, catalog, createEnv, seed, serverNow, signedOut, storedFart, user } from "./helpers";

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
    "farts/pub": storedFart("pub", ALICE),
    "farts/priv": storedFart("priv", ALICE, { visibility_mode: "PRIVATE" }),
  });
});

const reaction = (fartId: string, uid: string, type = "LEGEND") => ({
  fart_id: fartId, user_id: uid, type, created_at: serverNow(),
});

describe("reactions", () => {
  it("allows reacting as yourself to a readable fart, including anonymously", async () => {
    await assertSucceeds(user(env, BOB).doc(`farts/pub/reactions/${BOB}`).set(reaction("pub", BOB)));
    await assertSucceeds(anonymous(env, "anon-1").doc("farts/pub/reactions/anon-1").set(reaction("pub", "anon-1")));
  });

  it("allows changing and removing your own reaction", async () => {
    const db = user(env, BOB);
    await seed(env, { [`farts/pub/reactions/${BOB}`]: reaction("pub", BOB) });
    await assertSucceeds(db.doc(`farts/pub/reactions/${BOB}`).set(reaction("pub", BOB, "RIP")));
    await assertSucceeds(db.doc(`farts/pub/reactions/${BOB}`).delete());
  });

  it("denies reacting as someone else, invalid types, mismatched ids, or signed out", async () => {
    await assertFails(user(env, BOB).doc(`farts/pub/reactions/${ALICE}`).set(reaction("pub", ALICE)));
    await assertFails(user(env, BOB).doc(`farts/pub/reactions/${BOB}`).set(reaction("pub", BOB, "LOL")));
    await assertFails(user(env, BOB).doc(`farts/pub/reactions/${BOB}`).set(reaction("other", BOB)));
    await assertFails(signedOut(env).doc(`farts/pub/reactions/${BOB}`).set(reaction("pub", BOB)));
  });

  it("denies reacting to a fart you cannot read", async () => {
    await assertFails(user(env, BOB).doc(`farts/priv/reactions/${BOB}`).set(reaction("priv", BOB)));
  });

  it("denies deleting someone else's reaction", async () => {
    await seed(env, { [`farts/pub/reactions/${ALICE}`]: reaction("pub", ALICE) });
    await assertFails(user(env, BOB).doc(`farts/pub/reactions/${ALICE}`).delete());
  });

  it("reactions are readable exactly when the fart is", async () => {
    await assertSucceeds(user(env, BOB).collection("farts/pub/reactions").get());
    await assertFails(user(env, BOB).collection("farts/priv/reactions").get());
    await assertSucceeds(user(env, ALICE).collection("farts/priv/reactions").get());
  });
});

describe("blocks", () => {
  const block = (blocker: string, blocked: string) => ({ blocker_id: blocker, blocked_id: blocked, created_at: serverNow() });

  it("allows the blocker to create, read and delete", async () => {
    const db = user(env, ALICE);
    await assertSucceeds(db.doc(`blocks/${ALICE}_${BOB}`).set(block(ALICE, BOB)));
    await assertSucceeds(db.doc(`blocks/${ALICE}_${BOB}`).get());
    await assertSucceeds(db.collection("blocks").where("blocker_id", "==", ALICE).get());
    await assertSucceeds(db.doc(`blocks/${ALICE}_${BOB}`).delete());
  });

  it("denies blocking for someone else, mismatched ids, self-blocks and updates", async () => {
    await assertFails(user(env, BOB).doc(`blocks/${ALICE}_${BOB}`).set(block(ALICE, BOB)));
    await assertFails(user(env, ALICE).doc("blocks/whatever").set(block(ALICE, BOB)));
    await assertFails(user(env, ALICE).doc(`blocks/${ALICE}_${ALICE}`).set(block(ALICE, ALICE)));
    await seed(env, { [`blocks/${ALICE}_${BOB}`]: block(ALICE, BOB) });
    await assertFails(user(env, ALICE).doc(`blocks/${ALICE}_${BOB}`).update({ created_at: serverNow() }));
  });

  it("the blocked user cannot see or remove the block", async () => {
    await seed(env, { [`blocks/${ALICE}_${BOB}`]: block(ALICE, BOB) });
    await assertFails(user(env, BOB).doc(`blocks/${ALICE}_${BOB}`).get());
    await assertFails(user(env, BOB).doc(`blocks/${ALICE}_${BOB}`).delete());
  });
});

describe("moderation reports (DataContracts §5.6)", () => {
  const report = (id: string, reporter: string, overrides: Record<string, unknown> = {}) => ({
    report_id: id, reporter_user_id: reporter, target_type: "FART", target_id: "pub", reason_code: "SPAM",
    status: "OPEN", created_at: serverNow(), updated_at: serverNow(), ...overrides,
  });

  it("allows signed-in users (including anonymous) to report", async () => {
    await assertSucceeds(user(env, BOB).doc("moderation_reports/r1").set(report("r1", BOB)));
    await assertSucceeds(anonymous(env, "anon-1").doc("moderation_reports/r2").set(report("r2", "anon-1")));
  });

  it("denies spoofed reporters, non-OPEN status, unknown targets and signed-out users", async () => {
    await assertFails(user(env, BOB).doc("moderation_reports/r1").set(report("r1", ALICE)));
    await assertFails(user(env, BOB).doc("moderation_reports/r1").set(report("r1", BOB, { status: "ACTIONED" })));
    await assertFails(user(env, BOB).doc("moderation_reports/r1").set(report("r1", BOB, { target_type: "PLANET" })));
    await assertFails(signedOut(env).doc("moderation_reports/r1").set(report("r1", BOB)));
  });

  it("nobody reads or edits reports from the client, not even the reporter", async () => {
    await seed(env, { "moderation_reports/r1": report("r1", BOB) });
    await assertFails(user(env, BOB).doc("moderation_reports/r1").get());
    await assertFails(user(env, BOB).doc("moderation_reports/r1").update({ status: "DISMISSED" }));
    await assertFails(user(env, ALICE).collection("moderation_reports").get());
  });
});

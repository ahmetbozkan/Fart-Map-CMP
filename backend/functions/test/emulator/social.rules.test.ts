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

  it("denies reacting across a block in either direction", async () => {
    await seed(env, { [`blocks/${ALICE}_${BOB}`]: { blocker_id: ALICE, blocked_id: BOB } });
    await assertFails(user(env, BOB).doc(`farts/pub/reactions/${BOB}`).set(reaction("pub", BOB)));
    await env.clearFirestore();
    await seed(env, { ...catalog, "farts/pub": storedFart("pub", ALICE), [`blocks/${BOB}_${ALICE}`]: { blocker_id: BOB, blocked_id: ALICE } });
    await assertFails(user(env, BOB).doc(`farts/pub/reactions/${BOB}`).set(reaction("pub", BOB)));
  });

  it("denies reacting to a fart you cannot read", async () => {
    await assertFails(user(env, BOB).doc(`farts/priv/reactions/${BOB}`).set(reaction("priv", BOB)));
  });

  it("lets a user read their own reaction doc, and no one else's", async () => {
    await seed(env, { [`farts/pub/reactions/${BOB}`]: reaction("pub", BOB) });
    await assertSucceeds(user(env, BOB).doc(`farts/pub/reactions/${BOB}`).get());
    await assertFails(user(env, ALICE).doc(`farts/pub/reactions/${BOB}`).get());
    await assertFails(signedOut(env).doc(`farts/pub/reactions/${BOB}`).get());
  });

  it("denies deleting someone else's reaction", async () => {
    await seed(env, { [`farts/pub/reactions/${ALICE}`]: reaction("pub", ALICE) });
    await assertFails(user(env, BOB).doc(`farts/pub/reactions/${ALICE}`).delete());
  });

  it("reactor lists are never readable, so reactor ids stay hidden (own doc only, see below)", async () => {
    await seed(env, { [`farts/pub/reactions/${BOB}`]: reaction("pub", BOB) });
    await assertFails(user(env, BOB).collection("farts/pub/reactions").get());
    await assertFails(user(env, ALICE).collection("farts/pub/reactions").get());
    await assertFails(user(env, ALICE).collection("farts/priv/reactions").get());
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
  const report = (reporter: string, overrides: Record<string, unknown> = {}) => {
    const fields = { target_type: "FART", target_id: "pub", ...overrides };
    const id = `${reporter}_${fields.target_type}_${fields.target_id}`;
    return {
      id,
      data: {
        report_id: id, reporter_user_id: reporter, reason_code: "SPAM", status: "OPEN",
        created_at: serverNow(), updated_at: serverNow(), ...fields,
      },
    };
  };

  it("allows signed-in users (including anonymous) to report, one report per target", async () => {
    const bob = report(BOB);
    await assertSucceeds(user(env, BOB).doc(`moderation_reports/${bob.id}`).set(bob.data));
    const anon = report("anon-1");
    await assertSucceeds(anonymous(env, "anon-1").doc(`moderation_reports/${anon.id}`).set(anon.data));
  });

  it("denies a second report of the same target by the same reporter", async () => {
    const bob = report(BOB);
    await assertSucceeds(user(env, BOB).doc(`moderation_reports/${bob.id}`).set(bob.data));
    await assertFails(user(env, BOB).doc(`moderation_reports/${bob.id}`).set(bob.data));
  });

  it("allows every ReportReason code", async () => {
    for (const code of ["HARASSMENT", "PRIVATE_INFO", "MINOR_SAFETY", "SEXUAL", "ILLEGAL", "SPAM", "OTHER"]) {
      const r = report(BOB, { reason_code: code, target_id: `pub-${code}` });
      await assertSucceeds(user(env, BOB).doc(`moderation_reports/${r.id}`).set(r.data));
    }
  });

  it("denies free-form ids, spoofed reporters, non-OPEN status, unknown targets, free-text reasons and signed-out users", async () => {
    const bob = report(BOB);
    await assertFails(user(env, BOB).doc("moderation_reports/r1").set({ ...bob.data, report_id: "r1" }));
    const spoof = report(ALICE);
    await assertFails(user(env, BOB).doc(`moderation_reports/${spoof.id}`).set(spoof.data));
    const actioned = report(BOB, { status: "ACTIONED" });
    await assertFails(user(env, BOB).doc(`moderation_reports/${actioned.id}`).set(actioned.data));
    const planet = report(BOB, { target_type: "PLANET" });
    await assertFails(user(env, BOB).doc(`moderation_reports/${planet.id}`).set(planet.data));
    const freeText = report(BOB, { reason_code: "he smells, ban him" });
    await assertFails(user(env, BOB).doc(`moderation_reports/${freeText.id}`).set(freeText.data));
    await assertFails(signedOut(env).doc(`moderation_reports/${bob.id}`).set(bob.data));
  });

  it("nobody reads or edits reports from the client, not even the reporter", async () => {
    const bob = report(BOB);
    await seed(env, { [`moderation_reports/${bob.id}`]: bob.data });
    await assertFails(user(env, BOB).doc(`moderation_reports/${bob.id}`).get());
    await assertFails(user(env, BOB).doc(`moderation_reports/${bob.id}`).update({ status: "DISMISSED" }));
    await assertFails(user(env, ALICE).collection("moderation_reports").get());
  });
});

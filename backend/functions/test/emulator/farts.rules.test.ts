import { assertFails, assertSucceeds, type RulesTestEnvironment } from "@firebase/rules-unit-testing";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import {
  ALICE, BOB, CAROL, FREE_SOUND, PAID_AVATAR, PAID_SOUND, anonymous, catalog, createEnv, newFart, seed, serverNow,
  signedOut, storedFart, user,
} from "./helpers";

let env: RulesTestEnvironment;

beforeAll(async () => {
  env = await createEnv();
});
afterAll(async () => {
  await env.cleanup();
});
beforeEach(async () => {
  await env.clearFirestore();
  await seed(env, catalog);
});

describe("farts: create", () => {
  it("allows the author, including an anonymous session", async () => {
    await assertSucceeds(user(env, ALICE).doc("farts/f1").set(newFart("f1", ALICE)));
    await assertSucceeds(anonymous(env, "anon-1").doc("farts/f2").set(newFart("f2", "anon-1")));
  });

  it("allows optional description and a free sound", async () => {
    const fart = newFart("f1", ALICE, { description_text: "Silent but deadly", sound_id: FREE_SOUND });
    await assertSucceeds(user(env, ALICE).doc("farts/f1").set(fart));
  });

  it("denies signed-out users", async () => {
    await assertFails(signedOut(env).doc("farts/f1").set(newFart("f1", ALICE)));
  });

  it("denies writing as someone else or with a mismatched id", async () => {
    await assertFails(user(env, BOB).doc("farts/f1").set(newFart("f1", ALICE)));
    await assertFails(user(env, ALICE).doc("farts/f1").set(newFart("other", ALICE)));
  });

  it("denies precise coordinates or unknown keys on the public doc (ADR-0019)", async () => {
    const db = user(env, ALICE);
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { lat: 41.008234, lng: 28.978359 })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { voice_url: "gs://x" })));
  });

  it("denies invalid enums and out-of-range values", async () => {
    const db = user(env, ALICE);
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { visibility_mode: "EVERYONE" })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { flavor_level: 6 })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { flavor_level: 0 })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { intensity_value: 1.5 })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { public_lat: 91 })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { description_text: "x".repeat(501) })));
  });

  it("denies a coarse location key that is not a geohash-6 cell", async () => {
    const db = user(env, ALICE);
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { coarse_location_key: "sxk9" })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { coarse_location_key: "41.008234,28.978359" })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { coarse_location_key: "sxk9wa" }))); // 'a' is not geohash
  });

  it("denies a cosmetic used as the wrong type", async () => {
    await assertFails(user(env, ALICE).doc("farts/f1").set(newFart("f1", ALICE, { avatar_id: FREE_SOUND })));
  });

  it("denies client-set counters, moderation flags or timestamps", async () => {
    const db = user(env, ALICE);
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { reaction_count: 1 })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { reaction_breakdown: { DEAD: 3 } })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { report_count: 2 })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { is_hidden: true })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { created_at: new Date() })));
  });

  it("denies paid cosmetics without an entitlement and allows them with one (ADR-0016)", async () => {
    const db = user(env, ALICE);
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { avatar_id: PAID_AVATAR })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { sound_id: PAID_SOUND })));
    await assertFails(db.doc("farts/f1").set(newFart("f1", ALICE, { avatar_id: "not-in-catalog" })));

    await seed(env, { [`user_entitlements/${ALICE}`]: { user_id: ALICE, items: { [PAID_AVATAR]: { source: "PURCHASE" } } } });
    await assertSucceeds(db.doc("farts/f1").set(newFart("f1", ALICE, { avatar_id: PAID_AVATAR })));
  });
});

describe("farts: private location (ADR-0019)", () => {
  const location = () => ({ lat: 41.008234, lng: 28.978359, updated_at: serverNow() });

  it("allows the author to write it in the same batch as the fart", async () => {
    const db = user(env, ALICE);
    const batch = db.batch();
    batch.set(db.doc("farts/f1"), newFart("f1", ALICE));
    batch.set(db.doc("farts/f1/private/location"), location());
    await assertSucceeds(batch.commit());
  });

  it("denies other users writing it, extra keys, or other doc ids", async () => {
    await seed(env, { "farts/f1": storedFart("f1", ALICE) });
    await assertFails(user(env, BOB).doc("farts/f1/private/location").set(location()));
    await assertFails(user(env, ALICE).doc("farts/f1/private/location").set({ ...location(), note: "x" }));
    await assertFails(user(env, ALICE).doc("farts/f1/private/other").set(location()));
  });

  it("is frozen once the fart is soft-deleted", async () => {
    await seed(env, { "farts/f1": storedFart("f1", ALICE, { is_removed: true, removed_reason: "USER_DELETED" }) });
    await assertFails(user(env, ALICE).doc("farts/f1/private/location").set(location()));
  });

  it("is readable by the author only", async () => {
    await seed(env, { "farts/f1": storedFart("f1", ALICE), "farts/f1/private/location": { lat: 1, lng: 2 } });
    await assertSucceeds(user(env, ALICE).doc("farts/f1/private/location").get());
    await assertFails(user(env, BOB).doc("farts/f1/private/location").get());
    await assertFails(anonymous(env).doc("farts/f1/private/location").get());
    await assertFails(signedOut(env).doc("farts/f1/private/location").get());
  });
});

describe("farts: single reads (DataContracts §5.2)", () => {
  it("PUBLIC is readable by any signed-in user, not signed-out", async () => {
    await seed(env, { "farts/f1": storedFart("f1", ALICE) });
    await assertSucceeds(user(env, BOB).doc("farts/f1").get());
    await assertSucceeds(anonymous(env).doc("farts/f1").get());
    await assertFails(signedOut(env).doc("farts/f1").get());
  });

  it("PRIVATE is readable by the author only", async () => {
    await seed(env, { "farts/f1": storedFart("f1", ALICE, { visibility_mode: "PRIVATE" }) });
    await assertSucceeds(user(env, ALICE).doc("farts/f1").get());
    await assertFails(user(env, BOB).doc("farts/f1").get());
  });

  it("FOLLOWERS needs an approved follow and no block either way", async () => {
    await seed(env, {
      "farts/f1": storedFart("f1", ALICE, { visibility_mode: "FOLLOWERS" }),
      [`follow_relationships/${CAROL}_${ALICE}`]: { from_user_id: CAROL, to_user_id: ALICE, status: "PENDING" },
    });
    await assertSucceeds(user(env, ALICE).doc("farts/f1").get());
    await assertFails(user(env, BOB).doc("farts/f1").get()); // no relation
    await assertFails(user(env, CAROL).doc("farts/f1").get()); // pending

    await seed(env, { [`follow_relationships/${BOB}_${ALICE}`]: { from_user_id: BOB, to_user_id: ALICE, status: "APPROVED" } });
    await assertSucceeds(user(env, BOB).doc("farts/f1").get());

    await seed(env, { [`blocks/${ALICE}_${BOB}`]: { blocker_id: ALICE, blocked_id: BOB } });
    await assertFails(user(env, BOB).doc("farts/f1").get());
  });

  it("FOLLOWERS is denied when the follower blocked the author", async () => {
    await seed(env, {
      "farts/f1": storedFart("f1", ALICE, { visibility_mode: "FOLLOWERS" }),
      [`follow_relationships/${BOB}_${ALICE}`]: { from_user_id: BOB, to_user_id: ALICE, status: "APPROVED" },
      [`blocks/${BOB}_${ALICE}`]: { blocker_id: BOB, blocked_id: ALICE },
    });
    await assertFails(user(env, BOB).doc("farts/f1").get());
  });

  it("hidden or removed farts are visible to the author only", async () => {
    await seed(env, {
      "farts/h": storedFart("h", ALICE, { is_hidden: true }),
      "farts/r": storedFart("r", ALICE, { is_removed: true, removed_reason: "USER_DELETED" }),
    });
    await assertFails(user(env, BOB).doc("farts/h").get());
    await assertFails(user(env, BOB).doc("farts/r").get());
    await assertSucceeds(user(env, ALICE).doc("farts/h").get());
  });
});

describe("farts: queries", () => {
  beforeEach(async () => {
    await seed(env, {
      "farts/p": storedFart("p", ALICE),
      "farts/x": storedFart("x", ALICE, { visibility_mode: "PRIVATE" }),
    });
  });

  it("allows the map query shape: public, not hidden, not removed", async () => {
    const q = user(env, BOB).collection("farts")
      .where("visibility_mode", "==", "PUBLIC")
      .where("is_hidden", "==", false)
      .where("is_removed", "==", false);
    await assertSucceeds(q.get());
  });

  it("allows querying your own farts", async () => {
    await assertSucceeds(user(env, ALICE).collection("farts").where("author_user_id", "==", ALICE).get());
  });

  it("denies unconstrained or under-constrained queries", async () => {
    const db = user(env, BOB);
    await assertFails(db.collection("farts").get());
    await assertFails(db.collection("farts").where("visibility_mode", "==", "PUBLIC").get());
    await assertFails(db.collection("farts").where("author_user_id", "==", ALICE).get());
    await assertFails(signedOut(env).collection("farts").where("visibility_mode", "==", "PUBLIC")
      .where("is_hidden", "==", false).where("is_removed", "==", false).get());
  });
});

describe("farts: update and delete", () => {
  beforeEach(async () => {
    await seed(env, { "farts/f1": storedFart("f1", ALICE) });
  });

  it("allows the author to edit description and visibility", async () => {
    await assertSucceeds(user(env, ALICE).doc("farts/f1").update({
      description_text: "Edited", visibility_mode: "FOLLOWERS", updated_at: serverNow(),
    }));
  });

  it("denies other users and server-owned fields", async () => {
    await assertFails(user(env, BOB).doc("farts/f1").update({ description_text: "x", updated_at: serverNow() }));
    await assertFails(user(env, ALICE).doc("farts/f1").update({ reaction_count: 99, updated_at: serverNow() }));
    await assertFails(user(env, ALICE).doc("farts/f1").update({ is_hidden: true, updated_at: serverNow() }));
    await assertFails(user(env, ALICE).doc("farts/f1").update({ report_count: 0, is_removed: false, updated_at: serverNow(), author_user_id: BOB }));
    await assertFails(user(env, ALICE).doc("farts/f1").update({ public_lat: 10, updated_at: serverNow() }));
    await assertFails(user(env, ALICE).doc("farts/f1").update({ description_text: "no timestamp" }));
  });

  it("allows a soft delete with the user reason only, then freezes the fart", async () => {
    const db = user(env, ALICE);
    await assertFails(db.doc("farts/f1").update({ is_removed: true, removed_reason: "SPAM", updated_at: serverNow() }));
    await assertSucceeds(db.doc("farts/f1").update({ is_removed: true, removed_reason: "USER_DELETED", updated_at: serverNow() }));
    await assertFails(db.doc("farts/f1").update({ description_text: "back", updated_at: serverNow() }));
  });

  it("denies hard deletes", async () => {
    await assertFails(user(env, ALICE).doc("farts/f1").delete());
  });
});

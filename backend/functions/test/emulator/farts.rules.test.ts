import { assertFails, assertSucceeds, type RulesTestEnvironment } from "@firebase/rules-unit-testing";
import type firebase from "firebase/compat/app";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import {
  ALICE, BOB, CAROL, FREE_SOUND, PAID_AVATAR, PAID_SOUND, PRECISE, anonymous, catalog, createEnv, createFart, newFart,
  seed, serverNow, signedOut, storedFart, user, type Db,
} from "./helpers";

type Query = firebase.firestore.Query;

let env: RulesTestEnvironment;

beforeAll(async () => {
  env = await createEnv();
});
afterAll(async () => {
  await env.cleanup();
});
const account = (ageBand: string) => ({ age_band: ageBand, linked_providers: [], is_anonymous: false, is_suspended: false });

beforeEach(async () => {
  await env.clearFirestore();
  await seed(env, {
    ...catalog,
    [`users/${ALICE}/private/account`]: account("AGE_18_PLUS"),
    "users/anon-1/private/account": account("AGE_18_PLUS"),
    "users/teen-1/private/account": account("AGE_13_17"),
  });
});

describe("farts: PUBLIC is for adults only (ADR-0023 amendment)", () => {
  it("an adult may post PUBLIC", async () => {
    await assertSucceeds(createFart(user(env, ALICE), "f1", newFart("f1", ALICE)));
  });

  it("a 13–17 user may not post PUBLIC, but may post FOLLOWERS or PRIVATE", async () => {
    const db = user(env, "teen-1");
    await assertFails(createFart(db, "f1", newFart("f1", "teen-1")));
    await assertSucceeds(createFart(db, "f2", newFart("f2", "teen-1", { visibility_mode: "FOLLOWERS" })));
    await assertSucceeds(createFart(db, "f3", newFart("f3", "teen-1", { visibility_mode: "PRIVATE" })));
  });

  it("without an account doc there is no PUBLIC (fail closed)", async () => {
    const db = user(env, "no-account");
    await assertFails(createFart(db, "f1", newFart("f1", "no-account")));
    await assertSucceeds(createFart(db, "f2", newFart("f2", "no-account", { visibility_mode: "FOLLOWERS" })));
  });

  it("a 13–17 user may not switch an existing fart to PUBLIC; an adult may", async () => {
    await seed(env, {
      "farts/t": storedFart("t", "teen-1", { visibility_mode: "FOLLOWERS" }),
      "farts/a": storedFart("a", ALICE, { visibility_mode: "FOLLOWERS" }),
    });
    await assertFails(user(env, "teen-1").doc("farts/t").update({ visibility_mode: "PUBLIC", updated_at: serverNow() }));
    await assertSucceeds(user(env, "teen-1").doc("farts/t").update({ visibility_mode: "PRIVATE", updated_at: serverNow() }));
    await assertSucceeds(user(env, ALICE).doc("farts/a").update({ visibility_mode: "PUBLIC", updated_at: serverNow() }));
  });
});

describe("farts: create", () => {
  it("allows the author, including an anonymous session", async () => {
    await assertSucceeds(createFart(user(env, ALICE), "f1", newFart("f1", ALICE)));
    await assertSucceeds(createFart(anonymous(env, "anon-1"), "f2", newFart("f2", "anon-1")));
  });

  it("allows optional description and a free sound, also sent as null", async () => {
    const fart = newFart("f1", ALICE, { description_text: "Silent but deadly", sound_id: FREE_SOUND });
    await assertSucceeds(createFart(user(env, ALICE), "f1", fart));
    // FirestoreFartRepository encodes absent optionals as null.
    const nulls = newFart("f2", ALICE, { description_text: null, sound_id: null });
    await assertSucceeds(createFart(user(env, ALICE), "f2", nulls));
  });

  // The rules' string.size() does not count code points (500 emoji were denied in CI, PR #28). This pins the unit as
  // UTF-16 code units, which is what validateNewFart counts (Kotlin String.length): an emoji counts 2.
  it("counts the description limit in UTF-16 units: 250 emoji allowed, 251 denied", async () => {
    const db = user(env, ALICE);
    await assertSucceeds(createFart(db, "f1", newFart("f1", ALICE, { description_text: "💨".repeat(250) })));
    await assertFails(createFart(db, "f2", newFart("f2", ALICE, { description_text: "💨".repeat(251) })));
  });

  it("denies signed-out users", async () => {
    await assertFails(createFart(signedOut(env), "f1", newFart("f1", ALICE)));
  });

  it("denies writing as someone else or with a mismatched id", async () => {
    await assertFails(createFart(user(env, BOB), "f1", newFart("f1", ALICE)));
    await assertFails(createFart(user(env, ALICE), "f1", newFart("other", ALICE)));
  });

  it("denies precise coordinates or unknown keys on the public doc (ADR-0019)", async () => {
    const db = user(env, ALICE);
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { lat: 41.008234, lng: 28.978359 })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { voice_url: "gs://x" })));
  });

  it("denies invalid enums and out-of-range values", async () => {
    const db = user(env, ALICE);
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { visibility_mode: "EVERYONE" })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { flavor_level: 6 })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { flavor_level: 0 })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { intensity_value: 1.5 })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { public_lat: 91 })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { description_text: "x".repeat(501) })));
  });

  it("denies a coarse location key that is not a geohash-6 cell", async () => {
    const db = user(env, ALICE);
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { coarse_location_key: "sxk9" })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { coarse_location_key: "41.008234,28.978359" })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { coarse_location_key: "sxk9wa" }))); // 'a' is not geohash
  });

  it("denies a cosmetic used as the wrong type", async () => {
    await assertFails(createFart(user(env, ALICE), "f1", newFart("f1", ALICE, { avatar_id: FREE_SOUND })));
  });

  it("denies client-set counters, moderation flags or timestamps", async () => {
    const db = user(env, ALICE);
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { reaction_count: 1 })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { reaction_breakdown: { DEAD: 3 } })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { report_count: 2 })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { is_hidden: true })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { created_at: new Date() })));
  });

  it("denies paid cosmetics without an entitlement and allows them with one (ADR-0016)", async () => {
    const db = user(env, ALICE);
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { avatar_id: PAID_AVATAR })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { sound_id: PAID_SOUND })));
    await assertFails(createFart(db, "f1", newFart("f1", ALICE, { avatar_id: "not-in-catalog" })));

    await seed(env, { [`user_entitlements/${ALICE}`]: { user_id: ALICE, items: { [PAID_AVATAR]: { source: "PURCHASE" } } } });
    await assertSucceeds(createFart(db, "f1", newFart("f1", ALICE, { avatar_id: PAID_AVATAR })));
  });
});

describe("farts: private location (ADR-0019)", () => {
  const location = () => ({ ...PRECISE, updated_at: serverNow() });

  it("allows the author to write it in the same batch as the fart", async () => {
    await assertSucceeds(createFart(user(env, ALICE), "f1", newFart("f1", ALICE)));
  });

  it("denies a fart created without its private location in the same batch", async () => {
    await assertFails(user(env, ALICE).doc("farts/f1").set(newFart("f1", ALICE)));
  });

  it("denies creating it later for an existing fart, even by the author", async () => {
    await seed(env, { "farts/f1": storedFart("f1", ALICE) });
    await assertFails(user(env, ALICE).doc("farts/f1/private/location").set(location()));
  });

  it("denies moving it afterwards (update), even by the author", async () => {
    await seed(env, { "farts/f1": storedFart("f1", ALICE), "farts/f1/private/location": { ...PRECISE } });
    await assertFails(user(env, ALICE).doc("farts/f1/private/location").set(location()));
    await assertFails(user(env, ALICE).doc("farts/f1/private/location").update({ lat: 40, updated_at: serverNow() }));
  });

  it("denies a creating batch with extra keys or another private doc id", async () => {
    const db = user(env, ALICE);
    await assertFails(createFart(db, "f1", newFart("f1", ALICE), { ...PRECISE, note: "x" }));
    // A valid location is in the batch, so the extra private/other doc is the only reason for the denial.
    const batch = db.batch();
    batch.set(db.doc("farts/f2"), newFart("f2", ALICE));
    batch.set(db.doc("farts/f2/private/location"), location());
    batch.set(db.doc("farts/f2/private/other"), location());
    await assertFails(batch.commit());
  });

  it("denies other users writing it, extra keys, or other doc ids", async () => {
    await seed(env, { "farts/f1": storedFart("f1", ALICE) });
    await assertFails(user(env, BOB).doc("farts/f1/private/location").set(location()));
    await assertFails(user(env, ALICE).doc("farts/f1/private/location").set({ ...location(), note: "x" }));
    await assertFails(user(env, ALICE).doc("farts/f1/private/other").set(location()));
  });

  // Both cases above and below are now also covered by "the fart must not exist yet" (create-only); they stay as
  // regression guards for the author and removal checks.
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

// The exact shapes FirestoreMapFartRepository sends (STORY-01-03-03): a geohash prefix range per cell, or one world
// query, each with a limit. Composite indexes for these live in firestore.indexes.json (test/indexes.test.ts).
describe("farts: map queries (STORY-01-03-03)", () => {
  const PREFIX = "sxk9";

  beforeEach(async () => {
    await seed(env, {
      "farts/p": storedFart("p", ALICE, { coarse_location_key: "sxk97b" }),
      "farts/h": storedFart("h", ALICE, { coarse_location_key: "sxk97b", is_hidden: true }),
      "farts/x": storedFart("x", ALICE, { coarse_location_key: "sxk97b", visibility_mode: "PRIVATE" }),
    });
  });

  const publicBase = (db: Db): Query => db.collection("farts")
    .where("visibility_mode", "==", "PUBLIC")
    .where("is_hidden", "==", false)
    .where("is_removed", "==", false);

  const inCell = (q: Query): Query => q
    .where("coarse_location_key", ">=", PREFIX)
    .where("coarse_location_key", "<", PREFIX + "~")
    .orderBy("coarse_location_key")
    .limit(50);

  it("allows the public cell query and the public world query", async () => {
    await assertSucceeds(inCell(publicBase(user(env, BOB))).get());
    await assertSucceeds(publicBase(user(env, BOB)).limit(200).get());
    await assertSucceeds(inCell(publicBase(anonymous(env, "anon-1"))).get());
  });

  it("allows the owner's Mine cell query and Mine world query", async () => {
    const mine = user(env, ALICE).collection("farts")
      .where("author_user_id", "==", ALICE)
      .where("is_removed", "==", false);
    await assertSucceeds(inCell(mine).get());
    await assertSucceeds(mine.limit(200).get());
  });

  it("denies cell queries missing a public-safety constraint, FOLLOWERS, others' Mine, and signed-out", async () => {
    const db = user(env, BOB);
    await assertFails(inCell(db.collection("farts")
      .where("visibility_mode", "==", "PUBLIC").where("is_hidden", "==", false)).get());
    await assertFails(inCell(db.collection("farts")
      .where("visibility_mode", "==", "PUBLIC").where("is_removed", "==", false)).get());
    await assertFails(inCell(db.collection("farts")
      .where("visibility_mode", "==", "FOLLOWERS").where("is_hidden", "==", false)
      .where("is_removed", "==", false)).get());
    await assertFails(inCell(db.collection("farts")
      .where("author_user_id", "==", ALICE).where("is_removed", "==", false)).get());
    await assertFails(inCell(publicBase(signedOut(env))).get());
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

  const removal = () => ({ is_removed: true, removed_reason: "USER_DELETED", updated_at: serverNow() });

  it("lets the author soft-delete a fart of any visibility, anonymous included (STORY-01-04-05)", async () => {
    await seed(env, {
      "farts/fol": storedFart("fol", ALICE, { visibility_mode: "FOLLOWERS" }),
      "farts/pri": storedFart("pri", ALICE, { visibility_mode: "PRIVATE" }),
      "farts/anon": storedFart("anon", "anon-1"),
    });
    await assertSucceeds(user(env, ALICE).doc("farts/f1").update(removal()));
    await assertSucceeds(user(env, ALICE).doc("farts/fol").update(removal()));
    await assertSucceeds(user(env, ALICE).doc("farts/pri").update(removal()));
    await assertSucceeds(anonymous(env, "anon-1").doc("farts/anon").update(removal()));
  });

  it("denies deleting someone else's fart, signed out, or removal plus a server-owned field", async () => {
    await assertFails(user(env, BOB).doc("farts/f1").update(removal()));
    await assertFails(signedOut(env).doc("farts/f1").update(removal()));
    await assertFails(user(env, ALICE).doc("farts/f1").update({ ...removal(), reaction_count: 5 }));
    await assertFails(user(env, ALICE).doc("farts/f1").update({ ...removal(), public_lat: 10 }));
    await assertFails(user(env, ALICE).doc("farts/f1").update({ ...removal(), is_hidden: true }));
  });

  it("denies un-removing a deleted fart", async () => {
    await seed(env, { "farts/gone": storedFart("gone", ALICE, { is_removed: true, removed_reason: "USER_DELETED" }) });
    await assertFails(user(env, ALICE).doc("farts/gone").update({ is_removed: false, removed_reason: null, updated_at: serverNow() }));
  });

  it("denies hard deletes", async () => {
    await assertFails(user(env, ALICE).doc("farts/f1").delete());
  });
});

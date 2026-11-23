import { assertFails, assertSucceeds, type RulesTestEnvironment } from "@firebase/rules-unit-testing";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import {
  ALICE, BOB, FREE_AVATAR, PAID_AVATAR, anonymous, catalog, createEnv, seed, serverNow, signedOut, user,
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

const profile = (uid: string, overrides: Record<string, unknown> = {}) => ({
  user_id: uid, display_name: "Gas Master", avatar_id: FREE_AVATAR, follower_count: 0, following_count: 0,
  is_following_list_public: false, created_at: serverNow(), updated_at: serverNow(), ...overrides,
});

const account = (overrides: Record<string, unknown> = {}) => ({
  age_band: "AGE_18_PLUS", linked_providers: [], is_anonymous: false, is_suspended: false,
  created_at: serverNow(), updated_at: serverNow(), ...overrides,
});

describe("users: public profile (ADR-0025)", () => {
  it("allows creating your own profile with a free avatar", async () => {
    await assertSucceeds(user(env, ALICE).doc(`users/${ALICE}`).set(profile(ALICE, { bio: "Professional." })));
  });

  it("denies other users, private fields, counters, paid avatars and signed-out users", async () => {
    await assertFails(user(env, BOB).doc(`users/${ALICE}`).set(profile(ALICE)));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}`).set(profile(ALICE, { age_band: "AGE_18_PLUS" })));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}`).set(profile(ALICE, { follower_count: 100 })));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}`).set(profile(ALICE, { avatar_id: PAID_AVATAR })));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}`).set(profile(ALICE, { display_name: "" })));
    await assertFails(signedOut(env).doc(`users/${ALICE}`).set(profile(ALICE)));
  });

  it("is readable by any signed-in user, not signed-out", async () => {
    await seed(env, { [`users/${ALICE}`]: profile(ALICE) });
    await assertSucceeds(user(env, BOB).doc(`users/${ALICE}`).get());
    await assertSucceeds(anonymous(env).doc(`users/${ALICE}`).get());
    await assertFails(signedOut(env).doc(`users/${ALICE}`).get());
  });

  it("the owner edits profile fields only", async () => {
    await seed(env, { [`users/${ALICE}`]: profile(ALICE) });
    const db = user(env, ALICE);
    await assertSucceeds(db.doc(`users/${ALICE}`).update({ display_name: "Captain Gas", updated_at: serverNow() }));
    await assertFails(db.doc(`users/${ALICE}`).update({ follower_count: 9, updated_at: serverNow() }));
    await assertFails(db.doc(`users/${ALICE}`).update({ is_suspended: false, updated_at: serverNow() }));
    await assertFails(user(env, BOB).doc(`users/${ALICE}`).update({ display_name: "Hacked", updated_at: serverNow() }));
    await assertFails(db.doc(`users/${ALICE}`).delete());
  });

  it("a later-revoked avatar does not block editing other profile fields", async () => {
    await seed(env, {
      [`users/${ALICE}`]: profile(ALICE, { avatar_id: PAID_AVATAR }), // granted earlier, entitlement since revoked
    });
    const db = user(env, ALICE);
    await assertSucceeds(db.doc(`users/${ALICE}`).update({ bio: "Still here.", updated_at: serverNow() }));
    await assertFails(db.doc(`users/${ALICE}`).update({ avatar_id: "avatar-unknown", updated_at: serverNow() }));
  });
});

describe("users: private account (ADR-0025)", () => {
  it("allows the owner to create it once, with anonymity matching the session", async () => {
    await assertSucceeds(user(env, ALICE).doc(`users/${ALICE}/private/account`).set(account()));
    await assertSucceeds(anonymous(env, "anon-1").doc("users/anon-1/private/account")
      .set(account({ is_anonymous: true, age_band: "AGE_13_17" })));
  });

  it("denies spoofed anonymity, providers, suspension, bad age bands and other users", async () => {
    await assertFails(anonymous(env, "anon-1").doc("users/anon-1/private/account").set(account({ is_anonymous: false })));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}/private/account`).set(account({ is_anonymous: true })));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}/private/account`).set(account({ linked_providers: ["google"] })));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}/private/account`).set(account({ is_suspended: true })));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}/private/account`).set(account({ age_band: "AGE_UNDER_13" })));
    await assertFails(user(env, BOB).doc(`users/${ALICE}/private/account`).set(account()));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}/private/other`).set(account()));
  });

  it("is readable by the owner only, never by other users (minor status stays private)", async () => {
    await seed(env, { [`users/${ALICE}/private/account`]: account({ age_band: "AGE_13_17" }) });
    await assertSucceeds(user(env, ALICE).doc(`users/${ALICE}/private/account`).get());
    await assertFails(user(env, BOB).doc(`users/${ALICE}/private/account`).get());
    await assertFails(signedOut(env).doc(`users/${ALICE}/private/account`).get());
  });

  it("is immutable from the client (age band set once; the rest is server-managed)", async () => {
    await seed(env, { [`users/${ALICE}/private/account`]: account() });
    const db = user(env, ALICE);
    await assertFails(db.doc(`users/${ALICE}/private/account`).update({ age_band: "AGE_13_17" }));
    await assertFails(db.doc(`users/${ALICE}/private/account`).delete());
  });
});

describe("users: devices", () => {
  const device = (platform = "ANDROID") => ({ fcm_token: "token-abc", platform, updated_at: serverNow() });

  it("the owner reads, writes and deletes their devices", async () => {
    const db = user(env, ALICE);
    await assertSucceeds(db.doc(`users/${ALICE}/devices/d1`).set(device()));
    await assertSucceeds(db.doc(`users/${ALICE}/devices/d1`).get());
    await assertSucceeds(db.doc(`users/${ALICE}/devices/d1`).delete());
  });

  it("denies other users and invalid platforms", async () => {
    await assertFails(user(env, BOB).doc(`users/${ALICE}/devices/d1`).set(device()));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}/devices/d1`).set(device("WINDOWS_PHONE")));
    await seed(env, { [`users/${ALICE}/devices/d1`]: { fcm_token: "t", platform: "IOS" } });
    await assertFails(user(env, BOB).doc(`users/${ALICE}/devices/d1`).get());
  });
});

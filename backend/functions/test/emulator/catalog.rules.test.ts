import { assertFails, assertSucceeds, type RulesTestEnvironment } from "@firebase/rules-unit-testing";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import { catalogDocs, loadStarterCatalog } from "../../src/admin/starterCatalog";
import { ALICE, anonymous, createEnv, seed, serverNow, user } from "./helpers";

// The real starter catalog (STORY-01-02-02) against the users rules: what the app's ProfileBootstrap writes must pass.
// Other-user, signed-out, paid-avatar and extra-field denials are covered in users.rules.test.ts.

let env: RulesTestEnvironment;
const items = loadStarterCatalog();
const avatars = items.filter((item) => item.type === "AVATAR").map((item) => item.item_id);
const sounds = items.filter((item) => item.type === "SOUND").map((item) => item.item_id);

beforeAll(async () => {
  env = await createEnv();
});
afterAll(async () => {
  await env.cleanup();
});
beforeEach(async () => {
  await env.clearFirestore();
  await seed(env, catalogDocs(items, new Date()));
});

const profile = (uid: string, avatarId: string) => ({
  user_id: uid, display_name: "Sneaky Bean 42", avatar_id: avatarId, follower_count: 0, following_count: 0,
  is_following_list_public: false, created_at: serverNow(), updated_at: serverNow(),
});

describe("starter catalog: profile creation", () => {
  it("accepts every starter avatar for a signed-in and an anonymous user", async () => {
    for (const avatar of avatars) {
      await env.clearFirestore();
      await seed(env, catalogDocs(items, new Date()));
      await assertSucceeds(user(env, ALICE).doc(`users/${ALICE}`).set(profile(ALICE, avatar)));
      await assertSucceeds(anonymous(env, "anon-1").doc("users/anon-1").set(profile("anon-1", avatar)));
    }
  });

  it("rejects a starter sound as an avatar", async () => {
    await assertFails(user(env, ALICE).doc(`users/${ALICE}`).set(profile(ALICE, sounds[0])));
  });

  it("rejects a starter avatar once it is deactivated", async () => {
    const [first] = items.filter((item) => item.type === "AVATAR");
    await seed(env, catalogDocs([{ ...first, is_active: false }], new Date()));
    await assertFails(user(env, ALICE).doc(`users/${ALICE}`).set(profile(ALICE, first.item_id)));
  });

  it("still denies client writes to the catalog", async () => {
    await assertFails(user(env, ALICE).doc(`cosmetic_items/${avatars[0]}`).update({ is_free: false }));
    await assertFails(anonymous(env).doc("cosmetic_items/avatar_new").set({ ...items[0], item_id: "avatar_new" }));
  });
});

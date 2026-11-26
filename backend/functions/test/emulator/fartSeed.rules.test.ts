import { assertFails, assertSucceeds, type RulesTestEnvironment } from "@firebase/rules-unit-testing";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { DEFAULT_SEED_CENTER, fartSeedDocs, SEED_AUTHOR_ID } from "../../src/admin/fartSeed";
import { ALICE, BOB, FREE_AVATAR, FREE_SOUND, catalog, createEnv, createFart, seed, serverNow, signedOut, user } from "./helpers";

let env: RulesTestEnvironment;

beforeAll(async () => {
  env = await createEnv();
});
afterAll(async () => {
  await env.cleanup();
});

const COUNT = 6;
const seedDocs = () =>
  fartSeedDocs(
    { count: COUNT, center: DEFAULT_SEED_CENTER, nowMs: 1_764_000_000_000, avatars: [FREE_AVATAR], sounds: [FREE_SOUND] },
    serverNow(),
  );

beforeEach(async () => {
  await env.clearFirestore();
  await seed(env, {
    ...catalog,
    [`users/${ALICE}/private/account`]: { age_band: "AGE_18_PLUS", linked_providers: [], is_anonymous: false, is_suspended: false },
  });
});

describe("dev fart seed (STORY-01-03-01)", () => {
  it("a seed doc's shape passes the real create rule for an 18+ author; an extra field does not", async () => {
    // seed_fart_001 carries a sound_id (every third fart), so the optional field is covered too.
    const shape = { ...seedDocs()["farts/seed_fart_001"], fart_id: "f_shape", author_user_id: ALICE };
    expect(shape).toHaveProperty("sound_id");
    await assertSucceeds(createFart(user(env, ALICE), "f_shape", shape));
    await assertFails(createFart(user(env, ALICE), "f_extra", { ...shape, fart_id: "f_extra", lat: 41.0 }));
  });

  it("the map's PUBLIC query lists every seeded fart for a signed-in user, but not when signed out", async () => {
    await seed(env, seedDocs());
    const query = (db: ReturnType<typeof user>) =>
      db.collection("farts")
        .where("visibility_mode", "==", "PUBLIC")
        .where("is_hidden", "==", false)
        .where("is_removed", "==", false)
        .get();
    const snapshot = await assertSucceeds(query(user(env, BOB)));
    expect(snapshot.size).toBe(COUNT);
    await assertFails(query(signedOut(env)));
  });

  it("the precise point stays author-only", async () => {
    await seed(env, seedDocs());
    await assertFails(user(env, BOB).doc("farts/seed_fart_001/private/location").get());
    await assertFails(signedOut(env).doc("farts/seed_fart_001/private/location").get());
    await assertSucceeds(user(env, SEED_AUTHOR_ID).doc("farts/seed_fart_001/private/location").get());
  });
});

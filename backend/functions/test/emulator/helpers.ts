import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { initializeTestEnvironment, type RulesTestEnvironment } from "@firebase/rules-unit-testing";
import firebase from "firebase/compat/app";
import "firebase/compat/firestore";

export type Db = firebase.firestore.Firestore;
export type Data = Record<string, unknown>;

export const ALICE = "alice";
export const BOB = "bob";
export const CAROL = "carol";

/** One environment per test file; files run sequentially (see the test:emulator script). */
export function createEnv(): Promise<RulesTestEnvironment> {
  return initializeTestEnvironment({
    projectId: "demo-fartmap",
    firestore: {
      rules: readFileSync(resolve(__dirname, "../../../firestore.rules"), "utf8"),
      host: "127.0.0.1",
      port: 8080,
    },
  });
}

export const serverNow = (): firebase.firestore.FieldValue => firebase.firestore.FieldValue.serverTimestamp();

/** A signed-in user with a non-anonymous provider (Google/Apple). */
export function user(env: RulesTestEnvironment, uid: string): Db {
  return env.authenticatedContext(uid, { firebase: { sign_in_provider: "google.com" } }).firestore();
}

/** An anonymous Firebase session (ADR-0018): counts as signed in. */
export function anonymous(env: RulesTestEnvironment, uid = "anon-1"): Db {
  return env.authenticatedContext(uid, { firebase: { sign_in_provider: "anonymous" } }).firestore();
}

export function signedOut(env: RulesTestEnvironment): Db {
  return env.unauthenticatedContext().firestore();
}

/** Writes fixtures bypassing the rules. */
export async function seed(env: RulesTestEnvironment, docs: Record<string, Data>): Promise<void> {
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    for (const [path, data] of Object.entries(docs)) await db.doc(path).set(data);
  });
}

export const FREE_AVATAR = "avatar-free";
export const PAID_AVATAR = "avatar-paid";
export const FREE_SOUND = "sound-free";
export const PAID_SOUND = "sound-paid";

export const catalog: Record<string, Data> = {
  [`cosmetic_items/${FREE_AVATAR}`]: { item_id: FREE_AVATAR, type: "AVATAR", is_free: true, is_active: true },
  [`cosmetic_items/${PAID_AVATAR}`]: { item_id: PAID_AVATAR, type: "AVATAR", is_free: false, is_active: true },
  [`cosmetic_items/${FREE_SOUND}`]: { item_id: FREE_SOUND, type: "SOUND", is_free: true, is_active: true },
  [`cosmetic_items/${PAID_SOUND}`]: { item_id: PAID_SOUND, type: "SOUND", is_free: false, is_active: true },
};

/** A valid new public fart doc as the client writes it (DataContracts.md §3.2). */
export function newFart(fartId: string, author: string, overrides: Data = {}): Data {
  return {
    fart_id: fartId,
    author_user_id: author,
    public_lat: 41.01,
    public_lng: 28.97,
    coarse_location_key: "sxk9w2",
    timestamp_epoch_ms: 1_700_000_000_000,
    avatar_id: FREE_AVATAR,
    flavor_level: 3,
    intensity_value: 0.5,
    visibility_mode: "PUBLIC",
    reaction_count: 0,
    reaction_breakdown: {},
    comment_count: 0,
    report_count: 0,
    is_hidden: false,
    is_removed: false,
    created_at: serverNow(),
    updated_at: serverNow(),
    ...overrides,
  };
}

/** A stored fart for read tests (seeded with rules disabled). */
export function storedFart(fartId: string, author: string, overrides: Data = {}): Data {
  return newFart(fartId, author, overrides);
}

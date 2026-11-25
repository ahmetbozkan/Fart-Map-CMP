import { FieldValue, type Firestore } from "firebase-admin/firestore";
import { HttpsError } from "firebase-functions/v2/https";
import { logError, logInfo } from "../lib/log";

/**
 * `syncAccountProviders` callable (STORY-01-02-04). After a guest links Google, Firebase Auth holds the link but the
 * owner-only account doc still says `is_anonymous: true`. Both `is_anonymous` and `linked_providers` are
 * server-managed (ADR-0025, DataContracts.md §3.1.1), so the client asks the server to re-derive them from the Admin
 * Auth record of the caller. The callable takes no input and is idempotent.
 */

export type SyncStatus = "updated" | "missing";

export interface AccountProviderFields {
  is_anonymous: boolean;
  linked_providers: string[];
}

// Firebase provider ids → the wire names in DataContracts.md (`google`, `apple`). Anything else is not a sign-in
// provider Fart Map offers (ADR-0018) and is ignored.
const PROVIDER_NAMES: Record<string, string> = {
  "google.com": "google",
  "apple.com": "apple",
};

/** Derives the server-managed account fields from the Auth record's provider ids. */
export function accountProvidersFrom(providerIds: readonly string[]): AccountProviderFields {
  const names = new Set<string>();
  for (const id of providerIds) {
    const name = PROVIDER_NAMES[id];
    if (name !== undefined) names.add(name);
  }
  const linked = [...names].sort();
  return { is_anonymous: linked.length === 0, linked_providers: linked };
}

/** Writes the derived fields to an existing account doc. Never creates it: the age gate does that (ADR-0025). */
export async function syncAccountProviders(
  db: Firestore,
  uid: string,
  providerIds: readonly string[],
): Promise<SyncStatus> {
  const ref = db.doc(`users/${uid}/private/account`);
  return db.runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    if (!snap.exists) return "missing";
    tx.update(ref, { ...accountProvidersFrom(providerIds), updated_at: FieldValue.serverTimestamp() });
    return "updated";
  });
}

export interface SyncDeps {
  /** Provider ids of the Auth user (`providerData[].providerId`). */
  lookupProviders(uid: string): Promise<string[]>;
  writeAccount(uid: string, providerIds: readonly string[]): Promise<SyncStatus>;
}

/**
 * The callable body. Only `auth.uid` is used; the caller's payload is never read, so a client can't name another
 * user. Logs carry the outcome only (no uid, email or provider data).
 */
export async function handleSyncAccountProviders(
  deps: SyncDeps,
  auth: { uid: string } | undefined,
): Promise<{ status: SyncStatus }> {
  if (!auth?.uid) throw new HttpsError("unauthenticated", "Sign in first.");
  let providerIds: string[];
  try {
    providerIds = await deps.lookupProviders(auth.uid);
  } catch (error) {
    logError("sync_account_providers_failed", { stage: "lookup", code: errorCode(error) });
    throw new HttpsError("internal", "Could not read the account.");
  }
  let status: SyncStatus;
  try {
    status = await deps.writeAccount(auth.uid, providerIds);
  } catch (error) {
    logError("sync_account_providers_failed", { stage: "write", code: errorCode(error) });
    throw new HttpsError("internal", "Could not update the account.");
  }
  logInfo("sync_account_providers_done", { status });
  return { status };
}

// A stable code only (e.g. `auth/user-not-found`); messages can contain identifiers.
function errorCode(error: unknown): string {
  if (typeof error === "object" && error !== null && "code" in error) {
    const code = (error as { code: unknown }).code;
    if (typeof code === "string" || typeof code === "number") return String(code);
  }
  return "unknown";
}

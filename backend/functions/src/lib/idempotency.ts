import { randomUUID } from "node:crypto";
import { FieldValue, Firestore, Timestamp } from "firebase-admin/firestore";
import { logError } from "./log";

export interface RunOnceOptions {
  /** How long a claim blocks duplicates while the work runs; after it, a redelivery may re-claim (crash recovery). */
  leaseMs?: number;
  /** How long a record is kept; Firestore TTL deletes it after `expire_at` (firestore.indexes.json field override). */
  ttlMs?: number;
  now?: () => number;
}

const DEFAULT_LEASE_MS = 9 * 60 * 1000; // above the default v2 event function timeout
const DEFAULT_TTL_MS = 30 * 24 * 60 * 60 * 1000; // Eventarc redelivers for far less than this

/**
 * Runs `work` once per `key` for redelivered trigger events (planning/architecture/Backend.md §6a rule 1).
 *
 * - The first delivery claims `function_runs/{key}` with status `running` and a lease, runs the work, then marks it
 *   `done`. Later deliveries of a done key are skipped.
 * - If the work throws, the claim is released (only if it is still this delivery's claim) and the error rethrown,
 *   so a redelivery (retry) runs it again.
 * - A `running` claim whose lease has expired (the instance died mid-work) may be re-claimed.
 * - Every record carries `expire_at`, so `function_runs` doesn't grow forever.
 */
export async function runOnce(
  db: Firestore,
  key: string,
  work: () => Promise<void>,
  options: RunOnceOptions = {},
): Promise<"ran" | "skipped"> {
  const now = options.now ?? Date.now;
  const leaseMs = options.leaseMs ?? DEFAULT_LEASE_MS;
  const expireAt = Timestamp.fromMillis(now() + (options.ttlMs ?? DEFAULT_TTL_MS));
  const ref = db.collection("function_runs").doc(key);
  const claimId = randomUUID();

  const claimed = await db.runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    const existing = snap.data();
    if (existing?.status === "done") return false;
    const leaseUntil = existing?.lease_until as Timestamp | undefined;
    if (existing?.status === "running" && leaseUntil && leaseUntil.toMillis() > now()) return false;
    tx.set(ref, {
      status: "running",
      claim_id: claimId,
      lease_until: Timestamp.fromMillis(now() + leaseMs),
      claimed_at: FieldValue.serverTimestamp(),
      expire_at: expireAt,
    });
    return true;
  });
  if (!claimed) return "skipped";

  try {
    await work();
  } catch (error) {
    await releaseClaim(db, key, claimId).catch((releaseError: unknown) => {
      // The lease still expires, so a later redelivery can re-claim; log so the stuck window is visible.
      logError("run_once_release_failed", { key, error: String(releaseError) });
    });
    throw error;
  }
  await ref.set({ status: "done", done_at: FieldValue.serverTimestamp(), expire_at: expireAt }, { merge: true });
  return "ran";
}

/** Deletes the run record only if it still holds this delivery's claim (a re-claim after lease expiry wins). */
async function releaseClaim(db: Firestore, key: string, claimId: string): Promise<void> {
  const ref = db.collection("function_runs").doc(key);
  await db.runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    if (snap.data()?.claim_id === claimId) tx.delete(ref);
  });
}

/** Deterministic key for a Firestore trigger delivery. */
export function triggerKey(name: string, eventId: string): string {
  return `${name}:${eventId}`;
}

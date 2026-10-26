import { Firestore, FieldValue } from "firebase-admin/firestore";

/**
 * Runs `work` at most once per `key`, recording the run in `function_runs/{key}`.
 * Triggers can be delivered more than once; feature functions wrap their side effects with this helper
 * (planning/architecture/FirebaseStrategy.md §6a rule 1).
 */
export async function runOnce(db: Firestore, key: string, work: () => Promise<void>): Promise<"ran" | "skipped"> {
  const ref = db.collection("function_runs").doc(key);
  const claimed = await db.runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    if (snap.exists) return false;
    tx.set(ref, { claimed_at: FieldValue.serverTimestamp() });
    return true;
  });
  if (!claimed) return "skipped";
  await work();
  return "ran";
}

/** Deterministic key for a Firestore trigger delivery. */
export function triggerKey(name: string, eventId: string): string {
  return `${name}:${eventId}`;
}

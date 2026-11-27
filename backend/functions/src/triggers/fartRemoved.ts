import type { Firestore } from "firebase-admin/firestore";
import { runOnce, triggerKey } from "../lib/idempotency";
import { logInfo } from "../lib/log";

/** The fields of a fart doc this handler looks at. */
export type FartRemovalFields = { is_removed?: unknown } | undefined;

/**
 * Handler behind `onFartRemoved`: when a fart flips to `is_removed = true` (the author's soft delete, `USER_DELETED`),
 * delete its owner-only `private/location` so nothing precise outlives the fart (ADR-0019; clients can't delete it,
 * the rules deny that). The public doc stays as the tombstone.
 *
 * - Only the first flip does work: an update that doesn't change `is_removed` (or a fart already removed) is ignored.
 * - Deleting a missing document is a no-op, so a redelivery or a second removal path is harmless; `runOnce` also
 *   guards redeliveries of the same event.
 * - A delete failure propagates: `runOnce` releases its claim and the trigger (`retry: true`) retries.
 * - Logs carry the fart id and outcome only, never coordinates.
 */
export async function handleFartRemoved(
  db: Firestore,
  eventId: string,
  fartId: string,
  before: FartRemovalFields,
  after: FartRemovalFields,
): Promise<"ran" | "skipped"> {
  const result = await runOnce(db, triggerKey("onFartRemoved", eventId), async () => {
    const flipped = after?.is_removed === true && before?.is_removed !== true;
    if (!flipped) {
      logInfo("fart_removed_ignored", { fartId });
      return;
    }
    await db.doc(`farts/${fartId}/private/location`).delete();
    logInfo("fart_location_purged", { fartId });
  });
  logInfo("onFartRemoved_done", { fartId, result });
  return result;
}

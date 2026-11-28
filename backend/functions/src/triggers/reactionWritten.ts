import { FieldValue, type Firestore } from "firebase-admin/firestore";
import { runOnce, triggerKey } from "../lib/idempotency";
import { logInfo } from "../lib/log";

/** The reaction types of SOCIAL-01 (DataContracts §3.3). Anything else is ignored, never counted. */
export const REACTION_TYPES = ["DEAD", "TOXIC", "RIP", "SMELLED", "LEGEND"] as const;
export type ReactionType = (typeof REACTION_TYPES)[number];

/** The fields of a reaction doc this handler looks at. */
export type ReactionFields = { type?: unknown } | undefined;

function knownType(value: unknown): ReactionType | null {
  return (REACTION_TYPES as readonly unknown[]).includes(value) ? (value as ReactionType) : null;
}

/**
 * The parent-counter change for one reaction write (DataContracts §3.2: `reaction_count`, `reaction_breakdown`).
 * - create: count +1, breakdown[type] +1;
 * - delete: count −1, breakdown[type] −1;
 * - type change: count unchanged, breakdown moves one unit from the old type to the new one;
 * - an update that keeps the type (or an unknown type on both sides) changes nothing.
 */
export function counterDelta(
  before: ReactionFields,
  after: ReactionFields,
): { count: number; breakdown: Partial<Record<ReactionType, number>> } {
  const oldType = knownType(before?.type);
  const newType = knownType(after?.type);
  if (oldType === newType) return { count: 0, breakdown: {} };
  if (oldType === null) return { count: 1, breakdown: { [newType as ReactionType]: 1 } };
  if (newType === null) return { count: -1, breakdown: { [oldType]: -1 } };
  // Both sides set and different: a replace, the total stays the same.
  return { count: 0, breakdown: { [oldType]: -1, [newType]: 1 } };
}

/**
 * Handler behind `onReactionWritten` (SOCIAL-01, ADR-0015): keeps the parent fart's `reaction_count` and
 * `reaction_breakdown` equal to its reaction docs. Clients can't write those counters (rules), so this is the only
 * writer.
 * - `runOnce` keyed on the event: a redelivery doesn't apply the delta twice (deltas are not idempotent on their own).
 * - The parent is read and updated in one transaction, so a concurrent reaction can't lose an increment.
 * - A missing parent (the fart was purged) is skipped and logged; no counter is invented.
 * - Logs carry the fart id and outcome only.
 */
export async function handleReactionWritten(
  db: Firestore,
  eventId: string,
  fartId: string,
  before: ReactionFields,
  after: ReactionFields,
): Promise<"ran" | "skipped"> {
  const result = await runOnce(db, triggerKey("onReactionWritten", eventId), async () => {
    const delta = counterDelta(before, after);
    if (delta.count === 0 && Object.keys(delta.breakdown).length === 0) {
      logInfo("reaction_counter_unchanged", { fartId });
      return;
    }
    const fartRef = db.doc(`farts/${fartId}`);
    await db.runTransaction(async (tx) => {
      const snap = await tx.get(fartRef);
      if (!snap.exists) {
        logInfo("reaction_parent_missing", { fartId });
        return;
      }
      const update: Record<string, unknown> = {};
      if (delta.count !== 0) update.reaction_count = FieldValue.increment(delta.count);
      for (const [type, change] of Object.entries(delta.breakdown)) {
        update[`reaction_breakdown.${type}`] = FieldValue.increment(change ?? 0);
      }
      tx.update(fartRef, update);
    });
    logInfo("reaction_counter_applied", { fartId });
  });
  logInfo("onReactionWritten_done", { fartId, result });
  return result;
}

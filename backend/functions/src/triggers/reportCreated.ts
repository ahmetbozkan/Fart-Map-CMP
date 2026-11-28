import type { Firestore } from "firebase-admin/firestore";
import { runOnce, triggerKey } from "../lib/idempotency";
import { logInfo } from "../lib/log";

/** Distinct reporters that hide a fart pending review (DataContracts §3.2 `is_hidden`, ADR-0015). */
export const AUTO_HIDE_THRESHOLD = 3;

/** The fields of a `moderation_reports` doc this handler looks at. */
export type ReportFields = { target_type?: unknown; target_id?: unknown; reporter_user_id?: unknown } | undefined;

/**
 * The fart's moderation fields after one more distinct report: the new `report_count`, and whether the fart must be
 * hidden now. A fart already hidden stays hidden (only moderation un-hides); nothing here ever un-hides.
 */
export function moderationUpdate(currentCount: number, isHidden: boolean): { reportCount: number; hide: boolean } {
  const reportCount = Math.max(0, currentCount) + 1;
  return { reportCount, hide: !isHidden && reportCount >= AUTO_HIDE_THRESHOLD };
}

export type ReportOutcome =
  | "counted"
  | "hidden"
  | "skipped_target"
  | "skipped_missing"
  | "skipped_removed"
  | "skipped_self"
  | "skipped_private"
  | "skipped_duplicate";

/**
 * Handler behind `onReportCreated` (MOD-02 baseline, ADR-0015): counts a new fart report on the fart and hides the
 * fart from public reads once it has [AUTO_HIDE_THRESHOLD] distinct reporters.
 * - Distinctness comes from the report id (`{reporter}_{type}_{target}`, create-only in the rules), so each created
 *   report is one distinct reporter; `runOnce` keyed on the event keeps a redelivery from counting twice.
 * - The fart is read and updated in one transaction, so concurrent reports can't lose a count.
 * - Skipped (logged): a non-FART target (comments and users have no counter yet), a malformed report, a missing or
 *   removed fart, the author reporting their own fart, and a PRIVATE fart (no one else can read it).
 * - Logs carry the fart id and outcome only: never the reporter (Security.md §7) or coordinates.
 */
export async function handleReportCreated(
  db: Firestore,
  eventId: string,
  report: ReportFields,
): Promise<ReportOutcome> {
  if (report?.target_type !== "FART" || typeof report.target_id !== "string" || report.target_id === "" ||
    typeof report.reporter_user_id !== "string") {
    logInfo("report_skipped_target", {});
    return "skipped_target";
  }
  const fartId = report.target_id;
  const reporter = report.reporter_user_id;
  let outcome: ReportOutcome = "skipped_duplicate";
  await runOnce(db, triggerKey("onReportCreated", eventId), async () => {
    const fartRef = db.doc(`farts/${fartId}`);
    outcome = await db.runTransaction(async (tx): Promise<ReportOutcome> => {
      const snap = await tx.get(fartRef);
      if (!snap.exists) return "skipped_missing";
      const fart = snap.data() ?? {};
      if (fart.is_removed === true) return "skipped_removed";
      if (fart.author_user_id === reporter) return "skipped_self";
      // Only the author can read a PRIVATE fart, so anyone else reporting it got the id some other way: not counted.
      if (fart.visibility_mode === "PRIVATE") return "skipped_private";
      const count = typeof fart.report_count === "number" ? fart.report_count : 0;
      const update = moderationUpdate(count, fart.is_hidden === true);
      tx.update(fartRef, update.hide
        ? { report_count: update.reportCount, is_hidden: true }
        : { report_count: update.reportCount });
      return update.hide ? "hidden" : "counted";
    });
  });
  logInfo("onReportCreated_done", { fartId, outcome });
  return outcome;
}

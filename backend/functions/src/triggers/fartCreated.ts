import { Firestore } from "firebase-admin/firestore";
import { runOnce, triggerKey } from "../lib/idempotency";
import { logInfo } from "../lib/log";

/**
 * Handler behind `onFartCreated` (skeleton). Replaced by the location-projection work in EPIC-01-04; it keeps the
 * trigger, idempotency and logging paths exercised. Deployed to dev only (prod allow-list in deploy-backend.yml).
 */
export async function handleFartCreated(db: Firestore, eventId: string, fartId: string): Promise<"ran" | "skipped"> {
  const result = await runOnce(db, triggerKey("onFartCreated", eventId), async () => {
    logInfo("fart_created", { fartId });
  });
  logInfo("onFartCreated_done", { fartId, result });
  return result;
}

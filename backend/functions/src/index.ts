import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { onRequest } from "firebase-functions/v2/https";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { REGION } from "./region";
import { logInfo } from "./lib/log";
import { runOnce, triggerKey } from "./lib/idempotency";

initializeApp();

/** Smoke function: proves deploy and routing. */
export const health = onRequest({ region: REGION }, (_req, res) => {
  res.json({ status: "ok", service: "fartmap-functions" });
});

/**
 * Trigger skeleton. Replaced by the reaction-counter and location-projection functions in the feature epics;
 * it exists so the deploy path, the idempotency helper and the redacted logger are exercised from day one.
 */
export const onFartCreated = onDocumentCreated({ document: "farts/{fartId}", region: REGION }, async (event) => {
  const result = await runOnce(getFirestore(), triggerKey("onFartCreated", event.id), async () => {
    logInfo("fart_created", { fartId: event.params.fartId });
  });
  logInfo("onFartCreated_done", { fartId: event.params.fartId, result });
});

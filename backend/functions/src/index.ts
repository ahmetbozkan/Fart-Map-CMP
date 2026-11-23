import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { onRequest } from "firebase-functions/v2/https";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { REGION } from "./region";
import { handleFartCreated } from "./triggers/fartCreated";

initializeApp();

/** Smoke function: proves deploy and routing. */
export const health = onRequest({ region: REGION }, (_req, res) => {
  res.json({ status: "ok", service: "fartmap-functions" });
});

/** Trigger skeleton (dev only, see triggers/fartCreated.ts). */
export const onFartCreated = onDocumentCreated({ document: "farts/{fartId}", region: REGION }, async (event) => {
  await handleFartCreated(getFirestore(), event.id, event.params.fartId);
});

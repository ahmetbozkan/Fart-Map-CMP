import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";
import { onCall, onRequest } from "firebase-functions/v2/https";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { handleSyncAccountProviders, syncAccountProviders as writeAccountProviders } from "./callables/syncAccountProviders";
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

/** Re-derives the caller's server-managed account fields after account linking (callables/syncAccountProviders.ts). */
// maxInstances caps spend until App Check enforcement lands (STORY-01-02-05, Security.md §6).
export const syncAccountProviders = onCall({ region: REGION, maxInstances: 10 }, (request) =>
  handleSyncAccountProviders(
    {
      lookupProviders: async (uid) => (await getAuth().getUser(uid)).providerData.map((p) => p.providerId),
      writeAccount: (uid, providerIds) => writeAccountProviders(getFirestore(), uid, providerIds),
    },
    request.auth,
  ),
);

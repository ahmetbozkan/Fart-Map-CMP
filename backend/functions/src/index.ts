import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";
import { onCall, onRequest } from "firebase-functions/v2/https";
import { onDocumentCreated, onDocumentUpdated, onDocumentWritten } from "firebase-functions/v2/firestore";
import { handleSyncAccountProviders, syncAccountProviders as writeAccountProviders } from "./callables/syncAccountProviders";
import { REGION } from "./region";
import { handleFartCreated } from "./triggers/fartCreated";
import { handleFartRemoved } from "./triggers/fartRemoved";
import { handleReactionWritten } from "./triggers/reactionWritten";

initializeApp();

/** Smoke function: proves deploy and routing. */
export const health = onRequest({ region: REGION }, (_req, res) => {
  res.json({ status: "ok", service: "fartmap-functions" });
});

/**
 * Re-validates a new fart's public projection against its private location (ADR-0019 §4, triggers/fartCreated.ts).
 * `retry: true` so a transient failure is retried; `runOnce` keeps redeliveries idempotent.
 */
export const onFartCreated = onDocumentCreated(
  { document: "farts/{fartId}", region: REGION, retry: true },
  async (event) => {
    await handleFartCreated(getFirestore(), event.id, event.params.fartId);
  },
);

/**
 * Deletes a removed fart's owner-only precise location (ADR-0019, triggers/fartRemoved.ts). `retry: true` so a
 * transient failure is retried; the handler ignores updates that don't flip `is_removed`.
 */
export const onFartRemoved = onDocumentUpdated(
  { document: "farts/{fartId}", region: REGION, retry: true },
  async (event) => {
    await handleFartRemoved(
      getFirestore(),
      event.id,
      event.params.fartId,
      event.data?.before.data(),
      event.data?.after.data(),
    );
  },
);

/**
 * Keeps a fart's `reaction_count` / `reaction_breakdown` equal to its reaction docs (SOCIAL-01, triggers/reactionWritten.ts).
 * `retry: true` with `runOnce` keyed on the event, so a redelivery never applies a delta twice.
 */
export const onReactionWritten = onDocumentWritten(
  { document: "farts/{fartId}/reactions/{userId}", region: REGION, retry: true },
  async (event) => {
    await handleReactionWritten(
      getFirestore(),
      event.id,
      event.params.fartId,
      event.data?.before.data(),
      event.data?.after.data(),
    );
  },
);

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

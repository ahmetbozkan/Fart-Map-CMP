---
name: cloud-function
description: Add or change a Firebase Cloud Function (v2, TypeScript) - triggers, callables, webhooks (RevenueCat), AdMob SSV callback, push fan-out - with idempotency, redacted logging and emulator tests. Use for any server-side compute in backend/functions.
---

# Cloud Function

1. **Is it really server-side?** Functions own counters, reaction breakdowns, entitlements mirroring, unlock-token issue and consumption, ad caps, push payloads, moderation auto-hide and account-deletion cleanup (ADR-0015). Clients never compute these.
2. **Write it** in `backend/functions/src/<area>/<name>.ts` and export it from `src/index.ts`:
   - v2 API (`firebase-functions/v2/...`) with `region` from `src/region.ts`;
   - triggers wrapped in `runOnce(eventId, …)` from `src/lib/idempotency.ts`. Known bug: it claims before doing the work, so failures are never retried; fix it in STORY-01-01-04 before relying on it for money paths;
   - log with `src/lib/log.ts`, which redacts coordinates, tokens and emails. No raw payload logging.
3. **Untrusted input:**
   - callables use `request.auth.uid`, never a client-sent uid, and validate the payload shape;
   - HTTP webhooks verify the signature or auth header (RevenueCat: shared secret header; AdMob server-side verification: verify the signature against Google's published keys);
   - reject on failure.
4. **Secrets** via `defineSecret` / Secret Manager. Never in code or `.env` files in git.
5. **Tests:** vitest unit tests for pure logic, plus emulator tests (`firebase emulators:exec --only firestore,functions`) for triggers. Cover the duplicate delivery (idempotency) and authorization-failure paths.
6. **Run:** `cd backend/functions && npm run build && npm run lint && npm test`.
7. **Deploy:** CI deploys `dev` on push; `prod` needs a release plus reviewer approval. Never deploy from a laptop.

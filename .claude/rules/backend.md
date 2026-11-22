---
paths:
  - "backend/**"
---

# Backend rules (Firebase rules + Cloud Functions)

Sources: `planning/architecture/Backend.md`, `planning/architecture/DataContracts.md` (schema §3, rules matrix §5/§9), ADR-0015/0016/0017/0019.

- **Default deny.** Every rules change comes with emulator tests in `backend/functions/test/rules.test.ts` (or a sibling file) covering **each allowed and each denied case** of the collection: owner, other user, anonymous, signed-out.
- **Location split (ADR-0019).** Public `farts/{id}` docs hold fuzzed coordinates only. `farts/{id}/private/location` is readable and writable by the owner only.
- **Visibility is enforced in rules.** Client checks are UX only. Precise location never appears in any public read model.
- **Server-only fields** (counters, `reaction_breakdown`, XP, entitlements, unlock tokens, moderation flags) are written by functions or the Admin SDK only. Rules reject client writes to them.
- **Monetization (ADR-0016/0017).**
  - `user_entitlements/{uid}` is written only by the RevenueCat webhook function.
  - `rewarded_unlocks/{uid}/tokens/{id}` is written only by the AdMob server-side-verification function and consumed server-side when a fart uses the item.
  - The 3/day cap is enforced in the function.
  - Content using a non-free item without an entitlement or token is rejected.
- **Functions:**
  - v2 API;
  - region from `src/region.ts`;
  - every trigger is idempotent via `src/lib/idempotency.ts` (`runOnce`; see the known bug in `planning/ROADMAP.md` STORY-01-01-04);
  - log through `src/lib/log.ts`, which redacts coordinates, tokens and emails;
  - a test for every function.
- **Validate inputs** in callable and HTTP functions. Verify the server-side-verification signature and the webhook auth. Never trust client-sent uids; use `request.auth`.
- **Commands:** `cd backend/functions && npm ci && npm run build && npm run lint && npm test` (Java 21 on PATH for the emulators).
- **Deploys** run only from CI (`.github/workflows/deploy-backend.yml`): `dev` on push, `prod` on release with reviewer approval. Never deploy prod from a local machine.

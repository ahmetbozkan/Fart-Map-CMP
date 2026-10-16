# ADR-0015: Cloud Functions as Backend Compute and Firebase Product Scope

- Status: Accepted
- Date: 2026-10-14
- Source: owner decision (handover closeout, questions H1–H4)

## Context
`DataContracts.md`, `SecurityAndSecrets.md` and `ProgressionSystems.md` require server-authoritative behavior (denormalized counters, moderation isolation, audio limits, XP and leaderboard integrity, entitlement checks, push fan-out), but `TechStack.md` and `FirebaseStrategy.md` listed only Auth, Firestore, Storage and Analytics and named no compute layer. The PoC repository also carries an empty Firebase Functions template pointed at a foreign project.

## Decision
1. **Cloud Functions for Firebase (2nd generation, TypeScript on the current Node LTS)** is the backend compute layer from Phase 1.
2. The Firebase product scope is: Auth, Firestore, Storage, Analytics, **Cloud Functions, Cloud Messaging (FCM with APNs), Crashlytics, App Check**. App Check is enabled from Phase 1 (Play Integrity on Android, App Attest on iOS, debug providers in dev).
3. Backend code lives in the new repository under `backend/` (`firestore.rules`, `firestore.indexes.json`, `storage.rules`, `functions/`), deployed with the Firebase CLI from CI. Two Firebase projects: `dev` and `prod`. The PoC's `functions/` and `.firebaserc` are left untouched and are not reused.
4. Phase 1 function responsibilities: reaction counters on `farts`, "reaction received" push fan-out, report auto-hide at three distinct reports, entitlement mirroring from the purchases provider (ADR-0016), rewarded-ad unlock token issuance and consumption (ADR-0017). Later phases add XP, streaks, leaderboards and moderation tooling.

## Rationale
- Firestore security rules cannot express counters, fan-out, caps or receipt verification; functions are the smallest compute layer that keeps the client untrusted.
- One toolchain (Firebase CLI, emulator suite) keeps a solo developer's backend operable.

## Consequences
- `TechStack.md`, `FirebaseStrategy.md`, `Architecture.md` §3 and `DataContracts.md` are updated to this scope.
- Functions must be idempotent (retry-safe triggers), must never embed secrets, and run against the emulator suite in tests.
- Any further Firebase product needs an architecture note; a non-Firebase backend remains a deferred decision (`Architecture.md` §12).

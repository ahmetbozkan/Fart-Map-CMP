# Backend (Firebase Strategy): Fart Map

> Formerly FirebaseStrategy.md. Client-side Firebase code lives only in the `firebaseData` module (ADR-0021); `shared` holds the contracts. Backend code lives in `backend/`.

> **Planning spine position:** authoritative Firebase usage and boundary contract.
> Upstream: `architecture/Architecture.md`, `architecture/Security.md`, `architecture/DataContracts.md`, ADR-0002.
> Downstream: Firebase initialization code, repository remote implementations, security rule deployment workflow.
> This document defines how Firebase is used. It does not redefine product scope.

---

## 1) Purpose

This document standardizes Firebase integration so all remote behavior is consistent, secure, and auditable.

It prevents:

- mixed Firebase access patterns,
- client-side trust mistakes,
- secret/config handling regressions.

---

## 2) Core Firebase Decision

Use GitLive Firebase as the single cross-platform Firebase SDK integration layer (ADR-0002).

In scope Firebase products:

- Auth,
- Firestore,
- Storage,
- Analytics,
- Cloud Functions (2nd gen, TypeScript) — backend compute (ADR-0015),
- Cloud Messaging (FCM, APNs on iOS) — push notifications,
- Crashlytics — crash reporting,
- App Check — app attestation, enforced on Firestore, Storage and Functions.

External services integrated alongside Firebase: RevenueCat for purchases and entitlements (ADR-0016) and AdMob for rewarded ads (ADR-0017). Both are accessed only through platform adapters; their server-side callbacks terminate in Cloud Functions.

Any additional Firebase product adoption requires architecture review and, if it changes patterns, a new ADR.

---

## 3) Firebase Access Boundaries

1. Firebase APIs are called only from data/infrastructure layers.
2. Presentation and domain layers never import Firebase types.
3. Repository implementations encapsulate Firestore/Storage operations.
4. Auth/session orchestration exposes app-domain models, not raw SDK models.

---

## 4) Firestore Usage Rules

1. Collection/document contracts must follow `DataContracts.md`.
2. Security rules are authoritative for visibility/access control.
3. Client-side filtering is UX optimization only, not security.
4. Public projections must avoid precise coordinates unless owner-authorized path.
5. Write operations must be idempotent under retries where possible.
6. Server timestamps are the canonical ordering source for conflict resolution.

---

## 5) Auth Strategy Rules

1. Auth state is session source for start-destination and capability gating (ADR-0006).
2. Providers are Google, Apple and Anonymous only. An anonymous session is a full session (ADR-0018); linking Google or Apple upgrades the same uid and must use Firebase account linking, never a new sign-in that creates a second uid. The age gate (`ContentSafety.md` §5) runs before the first session becomes Active.
3. User identity used in writes must come from auth context, never trusted raw client parameter.
4. Sign-out/account-switch must trigger the cache/session safety behavior in `LocalData.md` §7 (`terminate()` + `clearPersistence()`).

---

## 6) Storage Strategy Rules

1. Media uploads (for example voice clips) are ownership-scoped paths.
2. Upload validation must enforce file constraints (ADR-0014 audio duration/security constraints).
3. Storage URLs stored in app models must be sanitized and never logged with secret query parameters.
4. No public raw-file sharing defaults for voice recording in MVP.

---

## 6a) Cloud Functions Rules

1. Functions live in `backend/functions/` (TypeScript); each trigger is idempotent and safe to retry.
2. Functions never embed secrets; configuration comes from the Firebase secret manager / environment per project.
3. Phase 1 responsibilities: reaction counters, reaction push fan-out, report auto-hide, entitlement mirroring (RevenueCat webhook), rewarded-ad token issuance and consumption, location projection re-validation (ADR-0019).
4. Every function has emulator-suite tests; rules tests run against the emulator in CI.

## 6a2) Remote Config

Firebase Remote Config holds the ADR-0024 levers (offering selection, rewarded-ad cap, offer timing, copy variants). Every key has a safe client default. Server-enforced values (the ad cap) are read by the enforcing function from the server-side template, never from client input.

## 6b) Messaging Rules

1. Device tokens are registered under `users/{userId}/devices/{deviceId}` by the platform layer and removed on sign-out.
2. Notification payloads are built server-side and contain no precise location, no private text and no secrets; they carry a deep link (`fartmap://fart/{fartId}`).
3. Notification permission is requested in context (once, after the first successful log, ADR-0023), never at launch.

## 6c) App Check and Crashlytics

1. App Check is enforced from Phase 1 with Play Integrity (Android) and App Attest (iOS); debug providers are used in dev builds only.
2. Crashlytics receives redacted crash reports; custom keys never include user content, coordinates or tokens (`Observability.md` §5).

## 7) Security Rule Governance

1. Firestore rules follow default-deny posture.
2. Rule behavior must match `DataContracts.md` visibility/follow/block constraints.
3. Rule changes require test updates for impacted access paths.
4. Rule deployment is environment-scoped: two projects, `dev` (`fart-map-dev`) and `prod` (`fart-map-1a558`); no stage project.

---

## 8) Environment and Config Rules

1. Two Firebase projects: `dev` and `prod` (owner-created); every config is environment-specific.
2. Sensitive credentials/service accounts are never committed.
3. `google-services.json` and `GoogleService-Info.plist` are never committed; templates are, and local setup / CI inject the real files (`Security.md` §4).
4. CI injects required credentials via secret store; logs must never print them.

---

## 9) Offline Coupling Rules

1. Firebase availability must not block local writes: writes are launched in app scope and queued by the Firestore SDK (ADR-0022, `LocalData.md` §3).
2. Snapshot listeners serve cache first, then server deltas; there is no separate local store to backfill.
3. Rules rejections are reported by `PendingWriteMonitor` and never retried; transient failures are retried by the SDK.

---

## 10) Observability Rules for Firebase Operations

Log/metric events for:

- auth transitions,
- Firestore read/write failures by category,
- Storage upload success/failure,
- security-rule-denied outcomes.

Never log secret tokens, private payload bodies, or precise coordinates.

---

## 11) Testing Contract

Minimum Firebase-related tests:

1. Firestore rule authorization matrix for Public/Followers/Private.
2. Follow approval and block precedence behavior.
3. Auth state to session-state mapping.
4. Storage ownership and constraint enforcement paths.
5. Remote mapper round-trip for required fields.
6. Functions emulator tests for counters, fan-out, auto-hide, entitlement mirror, unlock token lifecycle and location projection re-validation.
7. Account-linking test: anonymous uid is preserved after linking Google or Apple.

---

## 12) Explicit Non-Goals

This document does not define:

- local cache and write-pattern details (`LocalData.md`),
- UI state machine contracts (`Architecture.md` + feature specs).

---

*This is the Firebase integration contract. Any direct or inconsistent Firebase usage outside these boundaries is out of spec.*

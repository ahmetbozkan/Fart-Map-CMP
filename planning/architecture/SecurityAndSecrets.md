# Security and Secrets: Fart Map

> **Planning spine position:** security, privacy, and secrets architecture contract.
> Upstream: `product/ContentSafety.md`, `product/ProductGovernance.md`, `architecture/ArchitecturePrinciples.md`, `architecture/Architecture.md`, ADR-0001/0002/0007/0008/0009/0010/0012/0014.
> Downstream: `architecture/DataModel.md`, `architecture/DataContracts.md`, `architecture/SyncStrategy.md`, `architecture/FirebaseStrategy.md`, `architecture/Observability.md`, implementation code.
> This document is architecture guidance and guardrails. It is not a legal/privacy policy.

---

## Contents

1. [Purpose and Scope](#1-purpose-and-scope)
2. [Security Principles for This Project](#2-security-principles-for-this-project)
3. [Data Sensitivity Classification](#3-data-sensitivity-classification)
4. [Secrets Management Rules](#4-secrets-management-rules)
5. [Client vs Backend Trust Boundaries](#5-client-vs-backend-trust-boundaries)
6. [Firebase and Backend Security Considerations](#6-firebase-and-backend-security-considerations)
7. [Local Storage, Offline, and Cache Safety](#7-local-storage-offline-and-cache-safety)
8. [UGC, Audio, and Moderation Security Considerations](#8-ugc-audio-and-moderation-security-considerations)
9. [Logging, Diagnostics, and Observability Safety](#9-logging-diagnostics-and-observability-safety)
10. [Implementation Guardrails Checklist](#10-implementation-guardrails-checklist)

---

## 1) Purpose and Scope

### What this document governs

This document defines security and privacy guardrails for the Fart Map architecture, specifically:

- secrets handling across development, CI, and production,
- trust boundaries between mobile clients and backend enforcement,
- data sensitivity classification and storage boundaries,
- safe offline/cache behavior for sensitive data,
- UGC and moderation security posture,
- observability/logging constraints.

### What this document does not govern

- legal policy wording (privacy policy, terms of service),
- exact Firestore schema and rule syntax (owned by `DataContracts.md`),
- exact sync algorithm/retry implementation (owned by `SyncStrategy.md`),
- exact analytics event taxonomy (owned by `Observability.md`).

### Why this is mandatory

PoC evidence showed critical security failures (committed API key and committed Firebase plist), plus correctness/safety failures that could lead to privacy issues. This document exists to prevent recurrence by defining non-negotiable boundaries before implementation starts.

---

## 2) Security Principles for This Project

These principles apply to all features and all phases.

1. **No secrets in version control. Ever.**
   - No API keys, service credentials, private config files, signing files, or tokens in tracked files.

2. **Privacy-safe by default.**
   - Restrictive default choices must be encoded in data contracts and UI defaults.
   - Visibility defaults to `FOLLOWERS` for new users.
   - Following list visibility defaults to private (ADR-0012).
   - Anonymous sessions are full sessions (ADR-0018); every privacy default applies to them unchanged.

3. **Server-side authority for access control.**
   - Client checks are UX only.
   - Backend rules enforce who can read/write what.

4. **Least privilege for both users and systems.**
   - Users only get access to allowed records.
   - Services only get permissions required for their role.

5. **Defense in depth.**
   - Input validation in client + repository + backend rules.
   - Moderation checks and abuse controls layered, not single-point.

6. **Fail closed on security uncertainty.**
   - On auth failure, permission ambiguity, or rule evaluation failure, deny access.

7. **Security applies to offline paths too.**
   - Local caches and pending operation queues must obey visibility/privacy constraints.

8. **Auditability without leakage.**
   - Security-relevant actions are observable; sensitive payloads are not logged.

---

## 3) Data Sensitivity Classification

Sensitivity levels used in this repo:

- **Critical**: exposure creates immediate account/security risk.
- **High**: exposure creates privacy/safety risk.
- **Medium**: exposure creates user trust or abuse risk.
- **Low**: exposure is acceptable with minimal constraints.

| Data class | Sensitivity | Storage boundary / owner | Special handling rules |
|---|---|---|---|
| Account identity data (uid, display name, provider id) | High | Firebase Auth + user profile docs | Never trust client-supplied `uid`; always derive from auth context. |
| Auth/session tokens | Critical | Platform-secure storage + auth SDK internals | Never persisted in plaintext DB or logs; never exposed to app-level analytics. |
| Follower graph / follow requests / block state | High | Backend canonical + local cache | Must enforce ADR-0009 and ADR-0012 in backend rules; block status overrides follow status. |
| Fart posts metadata (flavor, intensity, description, timestamp) | Medium | Backend canonical + local cache | Visibility-gated reads; content moderation path required. |
| Visibility state (Public/Followers/Private) | High | Backend canonical + local cache | Must round-trip exactly per ADR-0010; no default fallback to permissive tier. |
| Precise location coordinates | Critical | Local: own farts only; backend: owner-only `farts/{id}/private/location` (ADR-0019) | Never exposed in public payloads, share cards, or analytics; public documents carry only fuzzed coordinates and a coarse key. |
| Coarse location / map tile-level location | Medium | Backend/public-facing projections + local cache | May be displayed publicly when visibility rules permit. |
| Location history / frequent places | Critical | Private user-scoped data only | Private by default; never queryable by other users. |
| Comments / reactions | Medium | Backend canonical + local cache | Must support report/block/mute safety controls. |
| Avatar/cosmetic/profile presentation data | Low to Medium | Backend canonical + local cache | Cosmetic-only monetization; no security elevation based on cosmetic ownership. |
| Audio recording URL and metadata | High | Firebase Storage + backend references | ADR-0014 constraints: max 10s, async moderation, no raw-file share defaults. |
| Moderation reports / safety events | Critical | Restricted backend collections | Reporter and accused visibility tightly scoped; never exposed in standard user APIs. |
| Analytics / diagnostics events | Medium | Analytics pipeline | Never include secrets, raw precise coordinates, raw sensitive text, or auth tokens. |
| Push notification tokens | High | Backend user-scoped token store | Token treated as sensitive identifier; not exposed to other users. |
| Premium entitlement / purchases | High | RevenueCat (source of truth) mirrored into `user_entitlements` by a Cloud Function (ADR-0016) | Client may cache for UX; rules and functions check the mirror before accepting content that uses non-free items. |
| Rewarded-ad unlock tokens | High | Backend-issued `rewarded_unlocks` (ADR-0017) | Issued only by the server-side verification callback; single use; consumed server-side; daily cap enforced server-side. |

---

## 4) Secrets Management Rules

### What counts as a secret in this project

- API keys (maps, backend, ad providers),
- Firebase private credentials/service accounts,
- signing credentials/certificates,
- auth tokens and refresh tokens,
- CI secrets and deployment tokens,
- any value that can grant unauthorized access or impersonation.

### Environment model

Maintain strict environment separation:

- **Local development** (developer machines)
- **CI / automation**
- **Production**

No secret may be shared across environments unless explicitly required and documented.

### Local development rules

- Secrets live in untracked local files or secure OS keychain mechanisms.
- Provide example template files with placeholder values only.
- Add and enforce ignore rules from first commit of a new repo.
- If a secret appears in git history, treat as incident: revoke/rotate immediately.

### CI / automation rules

- Inject secrets via CI secret store; never hardcode in workflow files.
- Use short-lived credentials where possible.
- Restrict secret scope per workflow/job (least privilege).
- Never print secrets in CI logs.

### Production rules

- Production keys are never present in developer local config by default.
- Access to production secret stores is role-restricted and audited.
- Key rotation policy must exist for maps, backend service credentials, and push credentials.

### Firebase/service credentials rules

- Client config files (`google-services.json`, `GoogleService-Info.plist`) are **never committed** (owner decision 2026-10-14; `CLAUDE.md` §4). Templates with placeholder values are committed; local setup and CI inject the real files.
- Admin/service-account credentials are always secret and never shipped in mobile binaries.
- Firestore/Storage access must rely on authenticated user context and security rules, not client obscurity.

---

## 5) Client vs Backend Trust Boundaries

The mobile client is user-controlled and cannot be trusted for security decisions.

### Allowed optimistic client behavior

- Immediate local write for UX (offline-first),
- local state transitions (draft -> uploading -> success/offline),
- client-side validation for user feedback,
- temporary cached entitlement/visibility hints for UI rendering.

### Required backend enforcement (non-negotiable)

| Concern | Client may do | Backend must enforce |
|---|---|---|
| Visibility enforcement | Hide/show content optimistically based on cached graph | Authoritative read rules for Public/Followers/Private (ADR-0010 + ADR-0009) |
| Social graph changes | Optimistically mark follow request as pending | Validate actor identity, block states, allowed transitions for follow lifecycle |
| Moderation/report flows | Submit report with local confirmation | Store reports in restricted collection, prevent user access to other reports |
| Progression/ranking integrity | Show provisional score updates | Server-side computed scoring inputs, anti-abuse checks, cap enforcement |
| Premium entitlement checks | Show cached entitlements for UX | Verify against the RevenueCat-mirrored `user_entitlements` before accepting content that uses a non-free cosmetic (ADR-0016) |
| Rewarded ad unlocks | Show the ad and a pending state | Issue unlock tokens only from the AdMob server-side verification callback; consume on write; enforce the daily cap (ADR-0017) |
| Location-sensitive behavior | Show local location preview | Store precise coordinates only in the owner-only private subdocument; public reads see fuzzed projections; a function re-validates the projection (ADR-0019) |
| Media/audio upload constraints | Show client-side timer/limit warnings | Enforce upload constraints (e.g., 10s audio max, allowed formats), ownership checks, moderation queue hooks |

### Rule of authority

If client and backend disagree, backend state wins for canonical data and access control.

---

## 6) Firebase and Backend Security Considerations

### Access control posture

- Every collection/document type must have explicit read/write conditions.
- Default-deny posture for unknown/unmatched paths.
- User-scoped documents use authenticated uid ownership checks.

### Visibility isolation

- Public content: readable by authenticated users per policy.
- Followers content: readable only where approved follow relationship exists.
- Private content: readable only by owner.
- Following-list visibility obeys ADR-0012 (private by default; opt-in for exposure).

### User-owned vs shared data

- User-owned docs (profile settings, private history, draft/pending metadata) are writeable only by owner.
- Shared docs (public markers, global catalogs) are write-restricted to controlled paths/services.

### Moderation and report data

- Reports and moderation actions stored in restricted backend scopes.
- Reporter identity and moderation notes are never publicly readable.

### Analytics separation

- Analytics storage and reporting separated from transactional app data.
- Analytics events cannot be used as authoritative source for permissions or monetization state.

### Private location protection

- Precise coordinates live only in `farts/{fartId}/private/location`, readable by the author (ADR-0019).
- Public documents and query paths expose `public_lat`/`public_lng` (fuzzed) and `coarse_location_key` only.
- No backend endpoint/query returns precise location for content not owned by the requesting user; the rule is enforceable because precise data is physically separated.

---

## 7) Local Storage, Offline, and Cache Safety

Offline-first is required, but local persistence must remain privacy-safe.

### Safe-to-cache categories

- Public and follower-permitted content needed for active UI views,
- user-owned drafts and pending operations,
- non-sensitive UI preferences,
- cosmetic catalog metadata.

### Extra-caution categories

| Category | Local handling requirement |
|---|---|
| Follower-only content | Cache only with access context; invalidate/prune on permission change (unfollow/block/revoke). |
| Private posts | Owner-only cache; never exposed to shared/guest surfaces. |
| Location history | Store minimally; avoid long retention in plaintext where not needed. |
| Pending uploads | Keep until sync complete; include visibility/security metadata in queue item. |
| Moderation-sensitive content | Avoid broad local persistence if content under review/removal. |
| Auth/session state | Use secure storage boundaries; clear on sign-out/account switch. |

### Cache invalidation security triggers

The system must invalidate or restrict local cached data when:

- user signs out,
- account changes,
- follow approval revoked,
- user blocked or blocks another,
- content visibility changes,
- content removed by moderation.

---

## 8) UGC, Audio, and Moderation Security Considerations

### UGC abuse surfaces

- description text,
- comments,
- reactions used for harassment patterns,
- profile bios/usernames,
- audio clips,
- external shares.

### Required protections

1. **Reporting path on all UGC surfaces**
   - Farts, comments, profiles, users must be reportable.

2. **Blocking/muting controls**
   - Blocking must suppress further interaction and visibility as defined by social safety rules.

3. **Rate limiting / abuse dampening**
   - Protect against spam posting, comment spam, reaction flooding, and ping-bomb behavior.

4. **Moderation pipeline**
   - Async moderation for text/audio allowed in MVP, but moderation queue and removal workflow must exist.

5. **Retention implications**
   - Reported/removed content lifecycle must be defined (active, quarantined, removed, appeal) in downstream contracts.

### Audio-specific guardrails (ADR-0014)

- Hard max 10 seconds enforced server-side.
- Audio attached to fart; no raw-audio sharing default.
- Upload path must validate ownership and content type.
- Audio moderation events must be auditable without leaking raw payload in logs.

---

## 9) Logging, Diagnostics, and Observability Safety

### Never log these items

- auth/session tokens,
- API keys or credential values,
- precise coordinates,
- raw private descriptions/comments where unnecessary,
- raw audio URLs containing sensitive query params,
- moderation reporter identity in general logs,
- payment receipt payloads.

### Logging rules

- Use structured logging with redaction at source.
- Log identifiers should prefer stable internal IDs over personal data.
- Error logs should include category/context, not sensitive payloads.
- Client crash logs and analytics must pass a privacy filter before emission.

### Diagnostics data minimization

- For location debugging, use coarse bins or hashed region IDs.
- For user identity debugging, use internal uid hash/short id in logs.
- For UGC debugging, log content length/type and moderation status, not full content text by default.

### Observability separation

- Security events, product analytics events, and debug logs must remain separable channels.
- Security-sensitive events should have tighter retention/access controls than product metrics.

---

## 10) Implementation Guardrails Checklist

Use this checklist before approving any implementation touching security/privacy/secrets.

- [ ] No secrets are added to tracked files, sample values only.
- [ ] Environment separation (dev/CI/prod) is preserved.
- [ ] Feature access control is enforced server-side, not client-only.
- [ ] Visibility rules match ADR-0009/0010 and round-trip correctly.
- [ ] Following-list privacy defaults match ADR-0012.
- [ ] Location handling keeps precise coordinates private and public data coarse.
- [ ] Offline cache behavior includes invalidation for auth/visibility/block changes.
- [ ] Pending operation queue preserves visibility and ownership context.
- [ ] UGC/report/block/mute controls exist on applicable surfaces.
- [ ] Audio constraints (10s max, async moderation, no raw-file share) are enforced.
- [ ] Logs are redacted and do not include forbidden sensitive data.
- [ ] Premium entitlement checks rely on authoritative backend/store-verified state.
- [ ] Progression/ranking integrity not delegated to client-only calculations.
- [ ] Security-relevant changes are reflected in `DataContracts.md`, `SyncStrategy.md`, and `Observability.md` as needed.

---

*Security and privacy are architecture constraints, not optional polish. Any implementation that violates this document is incomplete regardless of feature correctness.*

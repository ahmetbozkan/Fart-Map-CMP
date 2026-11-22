# Decisions (ADRs)

**Accepted** ADRs are settled. Don't relitigate them. If new information changes a trade-off, write a new ADR with status **Proposed** (use the `adr` skill) and flag it in `planning/STATE.md`. A Proposed ADR is not authority until the owner accepts it.

**Format:** `ADR-NNNN-kebab-title.md` containing Status, Date, Context, Decision, Alternatives, Consequences. Keep it under about 40 lines. A superseded ADR keeps its file and gets `Status: Superseded by ADR-XXXX`.

| ADR | Decision | Status |
|---|---|---|
| [0001](ADR-0001-room-over-sqldelight.md) | Room for local persistence | Superseded by 0022 |
| [0002](ADR-0002-gitlive-firebase-single-sdk.md) | GitLive Firebase as the single backend SDK | Accepted |
| [0003](ADR-0003-koin-with-expect-actual-platform-modules.md) | Koin DI with shared + platform modules | Accepted |
| [0004](ADR-0004-custom-stateviewmodel-pattern.md) | Custom StateViewModel (State/Action/Event) is the only presentation pattern | Accepted |
| [0005](ADR-0005-type-safe-compose-navigation.md) | Type-safe Compose Navigation with serializable routes | Accepted |
| [0006](ADR-0006-session-driven-navigation.md) | Session-driven start destination | Accepted |
| [0007](ADR-0007-local-first-read-with-backfill.md) | Local-first reads with remote backfill | Accepted; mechanism amended by 0022 |
| [0008](ADR-0008-expect-actual-platform-boundary-pattern.md) | Platform code behind shared contracts (expect/actual) | Accepted |
| [0009](ADR-0009-follow-only-with-approval.md) | Follow-only social graph with approval | Accepted |
| [0010](ADR-0010-visibility-tiers-public-followers-private.md) | Visibility tiers Public / Followers / Private | Accepted; default tier set by 0023 |
| [0011](ADR-0011-territory-out-of-mvp.md) | Territory out of MVP | Accepted |
| [0012](ADR-0012-following-list-private-by-default.md) | Following list private by default | Accepted |
| [0013](ADR-0013-signature-single-style-selection.md) | Signature = single style selection | Accepted |
| [0014](ADR-0014-audio-recording-10s-mvp-simple.md) | Audio recording 10 s, record → attach → play | Accepted |
| [0015](ADR-0015-cloud-functions-and-firebase-product-scope.md) | Cloud Functions backend; Messaging, Crashlytics, App Check in scope | Accepted |
| [0016](ADR-0016-purchases-and-entitlements-via-revenuecat.md) | RevenueCat purchases; entitlements mirrored server-side | Accepted; amended by 0024 |
| [0017](ADR-0017-rewarded-ads-admob-with-server-side-verification.md) | AdMob rewarded ads with server-side verification; single-use tokens | Accepted; cap tunable per 0024 |
| [0018](ADR-0018-anonymous-accounts-can-create-content.md) | Anonymous sessions are full sessions; Google / Apple / Anonymous only | Accepted |
| [0019](ADR-0019-location-storage-split-and-public-fuzzing.md) | Precise location owner-only; public projection fuzzed (geohash-6) | Accepted |
| [0020](ADR-0020-phase-1-scope-additions-2026-10-14.md) | Phase 1 adds purchases, rewarded ads, reaction push, delete, all filters, four tabs | Accepted; amended by 0023/0024 |
| [0021](ADR-0021-shared-module-layout-for-firebase-isolation.md) | Modules `shared` / `firebaseData` / `umbrella` for Firebase isolation | Accepted |
| [0022](ADR-0022-firestore-offline-persistence.md) | Firestore offline persistence replaces Room + SyncEngine (Phases 1–2) | Accepted 2026-11-22 |
| [0023](ADR-0023-phase-1-growth-kernel.md) | Phase 1 growth kernel: share card + deep links, cold start, PUBLIC default in Phase 1 (18+) | Accepted 2026-11-22 (option A) |
| [0024](ADR-0024-monetization-v2.md) | Monetization v2: shop, catalog, starter pack, Remote Config, subscription in Phase 2 | Accepted 2026-11-22 (single-use ads, no trims) |

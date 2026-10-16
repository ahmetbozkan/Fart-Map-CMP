# HANDOFF-UXF-01-04-MVP: Phase 1 map-and-create core

> Updated 2026-10-14 for ADR-0018, ADR-0019 and ADR-0020. Covers every Phase 1 UI surface.

## Traceability
- feature: `MAP-01`, `MAP-02`, `MAP-03`, `CREATE-01`–`CREATE-06`, `CREATE-09` (delete), `SOCIAL-01`, `PROFILE-01`, `PROFILE-02` (light), `PRIV-01`, `PRIV-02`, `PRIV-03` (baseline), `MOD-01`, `MON-01`, `MON-03`, `NOTIF-01` (minimal)
- flow: `UXF-01`, `UXF-02`, `UXF-03`, `UXF-04`, `UXF-08`, `UXF-11`, `UXF-12`, `UXF-13`
- loop: `L1`, `L2`, `L3`, `L5` (seed), `L8` (seed)
- journey stage: `2 -> 3 -> 4 -> 5 -> 7`
- architecture links: `planning/architecture/NavigationStrategy.md`, `planning/architecture/DataContracts.md`, `planning/architecture/SyncStrategy.md`, `planning/architecture/SecurityAndSecrets.md`, ADR-0015–ADR-0020

## Included Design Artifacts
- design spec sections: `planning/design/DesignSpecification.md` 9.4 (navigation), 10, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.9, 11.10
- screen specs:
  - `planning/design/screens/AuthLoginScreen.md`
  - `planning/design/screens/MapScreen.md`
  - `planning/design/screens/LogFartScreen.md`
  - `planning/design/screens/FartDetailSheet.md`
  - `planning/design/screens/CosmeticUnlockSheet.md`
  - `planning/design/screens/SocialTabScreen.md`
  - `planning/design/screens/RankTabScreen.md`
- stitch artifacts (placeholders until produced screen by screen): `TBD-UXF-01-AUTH`, `TBD-UXF-04-MAP`, `TBD-UXF-03-LOG`, `TBD-UXF-04-DETAIL`, `TBD-UXF-11-UNLOCK`, `TBD-UXF-04-SOCIAL`, `TBD-UXF-09-RANK-P1`

## Interaction Contract
- primary actions: continue (Google / Apple / without account), browse map and Social list, open marker details, create and submit a fart, react, delete own fart, unlock a cosmetic (buy / rewarded ad), link an account
- state transitions: auth idle->loading->success/error; session SignedOut->Active(anonymous|linked); map loading->empty/success/error/offline; create `DRAFT`->`UPLOADING`->`SUCCESS`/`FAILED`/`OFFLINE`; unlock `LoadingOffer`->`Ready`->`Purchasing`/`WatchingAd`->`Unlocked`/`Failed`/`Capped`
- error/offline behavior: errors are user-visible and recoverable; offline create, react and delete are accepted with deferred sync; purchases and ads require a connection and say so

## Safety/Privacy UX Contract
- visibility behavior: creation defaults to Followers (last-used thereafter) and exposes tier selection; Followers filter shows an empty state until Phase 2
- location display restrictions: precise coordinates are never shown for others' content; coarse area only; the owner sees their own exact pin (ADR-0019)
- moderation/reporting affordances: Report and Block on every other user's detail sheet; own-fart Delete with confirmation; hidden content never opens for non-authors
- age gate: 13+ self-declared birth year before the first session becomes Active; under-13 blocked
- anonymous sessions: full capability; link nudge after the first log; sign-out warning

## Platform Notes
- Android-specific UX: native auth launchers, Play Billing sheet, Google Mobile Ads rewarded flow, UMP consent, Material sheets, map and back conventions
- iOS-specific UX: native auth launchers, StoreKit sheet, Google Mobile Ads rewarded flow, UMP consent and ATT in context, sheet and gesture conventions

## Implementation Acceptance Criteria
1. Given a first-time user, when they tap "Continue without account", then an anonymous session starts and the Map tab opens with four tabs Map / Social / Rank / Profile.
2. Given a first-time user without session, when Google or Apple auth succeeds, then the map opens as start destination and the auth graph is removed from the back stack.
3. Given map markers are available, when the user taps a marker, then the detail sheet opens with the reaction bar, Report and Block, and no precise coordinate is displayed.
4. Given the user opens Log Fart, when they submit with defaults, then visibility is Followers, the marker drops on the map, and the stored document carries fuzzed public coordinates plus an owner-only private location.
5. Given submission fails or the device is offline, when the user retries or continues, then draft data is preserved and a recoverable status is shown; the operation syncs later through the single queue.
6. Given a locked avatar, when the user taps it, then the unlock sheet offers Buy and Watch Ad to Try Once only; the item becomes selectable only after the server-mirrored entitlement or token exists.
7. Given the user's fart receives a reaction and notification permission was granted in context, when the push is opened, then the Map tab shows the detail sheet for that fart.
8. Given an anonymous user links Google or Apple, when linking completes, then the uid, content and entitlements are unchanged and the session stays Active.
9. Given the user deletes their own fart, when confirmed, then the marker disappears immediately and the deletion syncs later if offline.

## Validation Plan
- tests: navigation start-route and anonymous/linked transition tests, create-flow state tests, visibility default assertion, location split round-trip (private vs public fields), unlock state model tests (no local unlock path), rules tests for entitlements/tokens/private location, deep-link routing test
- manual checks: auth-to-map path on both platforms, anonymous start, marker-detail open/close, create success/error/offline paths, buy and rewarded-ad flows with test products and test ad units, push open path, report and block entry, delete path
- accessibility checks: button labels, screen-reader state announcements, focus order for auth, create and unlock sheet

# Rank Tab (Phase 1 variant)

## Ownership
- Status: active
- Owner concern: design/screen behavior
- Upstream dependencies: `planning/design/DesignSpecification.md` (11.10), `planning/product/ReleaseStrategy.md` §3 (leaderboards are Phase 3), `planning/product/Monetization.md` §6 (no purchase prompt), ADR-0018, ADR-0020
- Downstream consumers: UI stories/tasks for the Rank tab; Phase 3 leaderboard stories replace the teaser

## Traceability
- Feature link(s): `PROFILE-02` (light stats); Phase 3: `PROG-03`, `PROFILE-04`
- Loop link(s): `L5` (seed), `L6` (seed)
- Journey stage(s): `5 -> 7`
- Persona focus: Rival, Identity Builder
- Architecture link(s): `planning/architecture/DataContracts.md` §3.1, `planning/architecture/Observability.md` §11

## Screen Goal
- Primary user outcome: see personal activity numbers and know that competition is coming, without being misled.
- Success signal: the tab is visited without confusion; no purchase or ranking is implied.

## Entry/Exit
- Entry routes: Rank tab.
- Exit routes: account-link card → linking flow (anonymous sessions); none otherwise.
- Navigation/back behavior: tab root.

## Layout Structure
- Top-level regions: top app bar "Rank"; stat cards (farts logged, reactions received, days active) using JetBrains Mono numerals; teaser card ("Leaderboards are brewing. Keep logging."); account-link card for anonymous sessions.
- Primary CTA: none (link card CTA only for anonymous sessions).
- Secondary actions: none.

## UI States
- Loading: skeleton stat cards.
- Empty: zero-state copy ("No stats yet. Science needs data.").
- Success: stats rendered from the user document.
- Error: "Could not load your stats" with retry.
- Offline: cached values shown.

## Interactions
- User actions: tap link-account card (anonymous only).
- Feedback/animation notes: none beyond global rules.
- Validation behavior: stats are the server-maintained counters; no client computation.

## Safety and Privacy UX
- Visibility/privacy defaults shown: N/A.
- Reporting/blocking affordance: N/A.
- Sensitive data redaction/display rules: no other users shown; no location.

## Platform Differences
- Android behavior: none specific.
- iOS behavior: none specific.
- Shared behavior constraints: identical copy and numbers.

## Stitch Mapping (if used)
- stitch_artifact_id: `TBD-UXF-09-RANK-P1`
- stitch_version: `TBD`
- mapped_frames: rank-default, rank-zero, rank-anonymous
- unresolved deltas: teaser illustration

## Implementation Handoff
- Required components: top app bar, stat card, teaser card, link-account card.
- State model expectations: `Loading`, `Content(stats, isAnonymous)`, `Error`.
- Test/validation notes: no purchase prompt rendered; numbers come from `users/{uid}` counters; anonymous variant shows the link card.

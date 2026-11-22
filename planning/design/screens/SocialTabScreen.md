# Social Tab (Phase 1 variant)

## Ownership
- Status: active
- Owner concern: design/screen behavior
- Upstream dependencies: `planning/design/DesignSpecification.md` (11.9), `planning/product/features/` (`MAP-02`, `SOCIAL-01`), `planning/product/Product.md` (map-first social rule), ADR-0020
- Downstream consumers: UI stories/tasks for the Social tab; Phase 2 feed stories extend this screen

## Traceability
- Feature link(s): `MAP-02`, `SOCIAL-01`, `PRIV-02`; Phase 2: `SOCIAL-04`
- Loop link(s): `L2`, `L3`
- Journey stage(s): `3 -> 4`
- Persona focus: Social Reactor, Explorer
- Architecture link(s): `planning/architecture/DataContracts.md` §3.2/§6, `planning/architecture/DataModel.md` §8 (map feed by coarse key), `planning/architecture/Architecture.md`

## Screen Goal
- Primary user outcome: see what is happening nearby as a list when scanning the map is slower.
- Success signal: card tap opens the detail sheet; "View on Map" recenters the map on the marker.

## Entry/Exit
- Entry routes: Social tab.
- Exit routes: card → Fart Detail Sheet; "View on Map" → Map tab centered on the fart; Log CTA from the empty state → Log Fart.
- Navigation/back behavior: back from the detail sheet returns to the list position.

## Layout Structure
- Top-level regions: top app bar "Social"; list of fart cards (avatar, username, relative time, flavor + intensity, description, reaction counts, coarse area, "View on Map").
- Primary CTA: "View on Map" on every card.
- Secondary actions: card tap (detail), pull to refresh.

## UI States
- Loading: skeleton cards.
- Empty: "Nobody has let one rip nearby yet. Be the first." with Log CTA.
- Success: 20 cards per page, infinite scroll.
- Error: "Could not load nearby activity" with retry.
- Offline: cached cards with a sync-later note.

## Interactions
- User actions: scroll, pull to refresh, tap card, tap "View on Map".
- Feedback/animation notes: card press state per global rules.
- Validation behavior: only PUBLIC, non-hidden farts from the user's coarse cell and its neighbours; the user's own farts are included.

## Safety and Privacy UX
- Visibility/privacy defaults shown: N/A (read surface).
- Reporting/blocking affordance: through the detail sheet; blocked users' content never appears.
- Sensitive data redaction/display rules: coarse area names only (ADR-0019).

## Platform Differences
- Android behavior: standard list and pull-to-refresh.
- iOS behavior: standard list and pull-to-refresh.
- Shared behavior constraints: identical eligibility rules and card content.

## Stitch Mapping (if used)
- stitch_artifact_id: `TBD-UXF-04-SOCIAL`
- stitch_version: `TBD`
- mapped_frames: social-default, social-empty, social-loading, social-error
- unresolved deltas: Phase 2 tab row (Following / Nearby) placement

## Implementation Handoff
- Required components: top app bar, fart card (organism), skeleton, empty state, snackbar.
- State model expectations: `Loading`, `Content(items, hasMore)`, `Empty`, `Error`.
- Test/validation notes: eligibility (public, non-hidden, not blocked) enforced by the query, not by client filtering; "View on Map" present on every card.

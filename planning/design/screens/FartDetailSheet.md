# Fart Detail Sheet (Phase 1 variant)

## Ownership
- Status: active
- Owner concern: design/screen behavior
- Upstream dependencies: `planning/design/DesignSpecification.md` (11.4), `planning/product/features/` (`MAP-02`, `SOCIAL-01`, `CREATE-09`, `MOD-01`), `planning/product/SocialSystems.md` §2, `planning/product/ContentSafety.md`, ADR-0019, ADR-0020
- Downstream consumers: UI stories/tasks for marker detail, reactions, delete and safety actions

## Traceability
- Feature link(s): `MAP-02`, `SOCIAL-01`, `CREATE-09` (delete), `MOD-01`, `PRIV-02`
- Loop link(s): `L2`, `L3`
- Journey stage(s): `4`
- Persona focus: Social Reactor, Explorer, Instant Joker
- Architecture link(s): `planning/architecture/DataContracts.md` §3.2/§3.3/§5, `planning/architecture/SyncStrategy.md`, `planning/architecture/Architecture.md` (deep link)

## Screen Goal
- Primary user outcome: understand a moment and react to it in one tap.
- Success signal: reaction given or removed with optimistic feedback; author can delete own content; report/block reachable.

## Entry/Exit
- Entry routes: marker tap on Map; card tap on Social tab; reaction push deep link `fartmap://fart/{fartId}`.
- Exit routes: drag down / back returns to the underlying tab; delete success returns to Map with the marker removed.
- Navigation/back behavior: sheet dismiss never leaves the current tab.

## Layout Structure
- Top-level regions: drag handle; user row (avatar, username, relative time at hour granularity); flavor + intensity row; sound play control (if a sound is attached); description; coarse area line ("Approximate area"); reaction bar (5 types with counts); action row.
- Primary CTA: reaction bar.
- Secondary actions: Share (system share of the deep link), Report, Block; Delete for own farts (confirmation dialog).

## UI States
- Loading: content fades in; counts skeleton.
- Empty: N/A.
- Success: all regions rendered; own-fart variant shows Delete instead of Report/Block.
- Error: "Could not load details" snackbar with retry; reaction failure rolls back silently (`SocialSystems.md` §2).
- Offline: cached content shown; reaction queued with optimistic state; delete queued with the marker hidden immediately.

## Interactions
- User actions: tap reaction (toggle; selecting another replaces), tap sound play, tap Share, tap Report (reason sheet), tap Block (confirmation), tap Delete (confirmation), tap username (Phase 1: no profile navigation for other users; Phase 2 adds it).
- Feedback/animation notes: reaction scale 1.2x → 1.0x over 150 ms; counts update optimistically.
- Validation behavior: reactions on PRIVATE farts are impossible by construction (only the author can open them).

## Safety and Privacy UX
- Visibility/privacy defaults shown: none editable here; visibility badge shown on own farts.
- Reporting/blocking affordance: Report and Block always present on others' farts; hidden content (`is_hidden`) never opens for non-authors.
- Sensitive data redaction/display rules: coarse area only, never coordinates (ADR-0019); no precise timestamp below hour granularity for others' farts.

## Platform Differences
- Android behavior: Material bottom sheet with back dismiss; system share sheet.
- iOS behavior: sheet with grabber and swipe dismiss; UIActivityViewController share.
- Shared behavior constraints: identical reaction semantics, delete confirmation and safety actions.

## Stitch Mapping (if used)
- stitch_artifact_id: `TBD-UXF-04-DETAIL`
- stitch_version: `TBD`
- mapped_frames: detail-default, detail-own, detail-loading, detail-error
- unresolved deltas: sound play control placement; report reason list copy

## Implementation Handoff
- Required components: bottom sheet, avatar, reaction chips, icon buttons, confirmation dialogs, snackbar.
- State model expectations: `Loading`, `Content(fart, isOwn, myReaction)`, `Error`; events `Dismissed`, `Deleted`, `Shared`.
- Test/validation notes: reaction toggle and rollback; delete queues offline and hides the marker; deep link opens the sheet over the Map tab; no coordinate appears in any rendered string.

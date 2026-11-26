# Map Screen

## Ownership
- Status: active
- Owner concern: design/screen behavior
- Upstream dependencies: `planning/design/DesignSpecification.md` (11.2), `planning/product/features/` (`MAP-01`, `MAP-02`, `CREATE-01`, `SOCIAL-01`), `planning/product/ContentSafety.md`
- Downstream consumers: UI stories/tasks for map exploration and marker interaction

## Traceability
- Feature link(s): `MAP-01`, `MAP-02`, `CREATE-01`, `SOCIAL-01`
- Loop link(s): `L1`, `L2`
- Journey stage(s): `3 -> 4`
- Persona focus: primary persona set (`planning/product/Product.md`)
- Architecture link(s): `planning/architecture/Architecture.md`, `planning/architecture/DataContracts.md`

## Screen Goal
- Primary user outcome: quickly understand nearby activity and enter create/detail flows from the map.
- Success signal: user can discover markers and open detail/create in one interaction from map.

## Entry/Exit
- Entry routes: session-driven start destination and tab navigation.
- Exit routes: modal log flow, marker detail sheet, other tabs via nav.
- Navigation/back behavior: map remains hub context; modal/sheet dismiss returns to map.

## Layout Structure
- Top-level regions: full-screen map canvas, overlay top app bar, filter row (Mine / Followers / Everyone), marker layer, "center on me" button, FAB, bottom navigation (Map / Social / Rank / Profile, ADR-0020).
- Primary CTA: log action FAB.
- Secondary actions: marker tap, filter controls, "center on me", navigation tabs.

## UI States
- Loading: base map visible while marker layer hydrates.
- Empty: no markers state with clear create nudge.
- Success: markers visible and interactive.
- Error: recoverable data-load failure with retry action.
- Offline: local/cached markers shown with sync-later indication where applicable.
- Location notices ("center on me", STORY-01-03-04): locating (spinner on the button), denied, blocked (with "Open settings"), location services off, no fix. Each is a dismissible card; the map stays fully usable. No permission prompt at launch.

## Interactions
- User actions: pan/zoom map, tap marker, tap FAB, apply filters, switch tab.
- Feedback/animation notes: marker appearance and selection states follow global motion rules.
- Validation behavior: filter changes and marker selections must be visibly reflected.

## Safety and Privacy UX
- Visibility/privacy defaults shown: marker visibility follows server-enforced tiers; UI must not imply unsupported visibility.
- Reporting/blocking affordance: detail surface entry must expose report/block pathway.
- Sensitive data redaction/display rules: markers for others' farts are placed at the fuzzed public coordinates (ADR-0019); the owner's own markers may use the precise pin; no coordinate text is ever displayed.

## Platform Differences
- Android behavior: platform-native map gestures and back handling conventions.
- iOS behavior: platform-native map gestures and sheet interaction conventions.
- Shared behavior constraints: same marker semantics, same privacy visibility behavior, same action outcomes.

## Stitch Mapping (if used)
- stitch_artifact_id: `TBD-UXF-04-MAP`
- stitch_version: `TBD`
- mapped_frames: map-default, map-loading, map-empty, map-error, marker-selected
- unresolved deltas: filter-surface placement and notification entry affordance

## Implementation Handoff
- Required components: map host, marker renderer, overlay app bar, FAB, nav bar, detail-sheet entry.
- State model expectations: loading/empty/success/error/offline map states.
- Test/validation notes: verify marker interaction, privacy redaction, and map-hub back behavior.

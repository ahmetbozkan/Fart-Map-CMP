# Auth/Login Screen

## Ownership
- Status: active
- Owner concern: design/screen behavior
- Upstream dependencies: `planning/design/DesignSpecification.md` (11.1), `planning/product/features/` (`MAP-01`, `CREATE-01`), `planning/product/ContentSafety.md`
- Downstream consumers: UI stories/tasks for first-open and authentication flow

## Traceability
- Feature link(s): `MAP-01`, `CREATE-01`
- Loop link(s): `L1`
- Journey stage(s): `2 -> 3`
- Persona focus: primary persona set (`planning/product/Product.md`)
- Architecture link(s): `planning/architecture/Architecture.md`, `planning/architecture/Backend.md`, `planning/architecture/Security.md`

## Screen Goal
- Primary user outcome: authenticate quickly and reach map context with minimal friction.
- Success signal: user reaches Map screen in first session without onboarding dead-end.

## Entry/Exit
- Entry routes: app cold start when no active session.
- Exit routes: authenticated session routes to Map as start destination.
- Navigation/back behavior: no return to login after successful auth session creation.

## Layout Structure
- Top-level regions: hero illustration, title/tagline, sign-in actions, guest action.
- Primary CTA: provider sign-in action.
- Secondary actions: "Continue without account" (anonymous session with full capability, ADR-0018). Phase 1 has Google only. The alternate provider (Apple) arrives in Phase 2 (owner, 2026-11-25).

## UI States
- Loading: sign-in action shows in-progress state while auth request executes.
- Empty: N/A.
- Success: transition to map-start route.
- Error: inline recoverable auth error with retry path.
- Offline: explicit offline auth guidance; "Continue without account" still works if an anonymous session already exists on the device.

## Interactions
- User actions: provider sign-in, continue without account, retry after failure; age gate (birth year) precedes the first Active session (`ContentSafety.md` §5).
- Feedback/animation notes: subtle action-state transitions only; avoid blocking visuals.
- Validation behavior: provider-specific auth failures mapped to user-readable errors.

## Safety and Privacy UX
- Visibility/privacy defaults shown: not set here; no override of user defaults.
- Reporting/blocking affordance: N/A.
- Sensitive data redaction/display rules: no credential/token display, no sensitive debug details in UI errors.

## Platform Differences
- Android behavior: native provider auth launcher integration.
- iOS behavior: native provider auth launcher integration.
- Shared behavior constraints: identical success/failure outcomes and routing semantics.

## Stitch Mapping (if used)
- stitch_artifact_id: `TBD-UXF-01-AUTH`
- stitch_version: `TBD`
- mapped_frames: auth-entry, auth-loading, auth-error
- unresolved deltas: provider button styling parity and guest pathway framing

## Implementation Handoff
- Required components: provider auth buttons, inline error row, loading-capable CTA states.
- State model expectations: idle -> loading -> success/error.
- Test/validation notes: verify navigation back-stack reset, provider failure handling, and accessibility labels.

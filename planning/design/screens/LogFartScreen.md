# Log Fart Screen

## Ownership
- Status: active
- Owner concern: design/screen behavior
- Upstream dependencies: `planning/design/DesignSpecification.md` (11.3), `planning/product/features/` (`CREATE-01` to `CREATE-08`), `planning/product/Monetization.md`, `planning/product/ContentSafety.md`
- Downstream consumers: UI stories/tasks for creation flow, draft handling, and posting states

## Traceability
- Feature link(s): `CREATE-01`, `CREATE-02`, `CREATE-03`, `CREATE-04`, `CREATE-05`, `CREATE-06`, `CREATE-07`, `CREATE-08`
- Loop link(s): `L1`, `L8`
- Journey stage(s): `3 -> 4 -> 8`
- Persona focus: primary persona set (`planning/product/Product.md`)
- Architecture link(s): `planning/architecture/LocalData.md`, `planning/architecture/DataContracts.md`, `planning/architecture/Security.md`

## Screen Goal
- Primary user outcome: complete a fart log quickly while supporting optional expressive depth.
- Success signal: user submits successfully and sees marker reflected on map without data loss.

## Entry/Exit
- Entry routes: map FAB/action, contextual create affordances.
- Exit routes: successful submission returns to map; cancel/back supports draft-safe behavior.
- Navigation/back behavior: back from draft prompts safe discard or preserves draft.

## Layout Structure
- Top-level regions: map preview, expression controls, optional text/audio inputs, visibility selector, submit CTA.
- Primary CTA: submit (`Let It Rip`).
- Secondary actions: location adjust, flavor/intensity/sound configuration, cancel; locked avatars and sounds open the Cosmetic Unlock Sheet (`screens/CosmeticUnlockSheet.md`).

## UI States
- Loading: submission progress with clear in-flight feedback.
- Empty: default prefilled state with safe defaults.
- Success: transition to map with success feedback and marker confirmation.
- Error: recoverable error with retry and draft preservation.
- Offline: offline post acceptance with deferred sync messaging.

## Location step (quick log, STORY-01-04-02)
- Granted: a fresh fix at open (12 s bound), the cached fix as a fallback, else "Location unavailable".
- Not asked yet: "Location unavailable" with "Use my location" (asks on tap) and "Adjust location"; blocked adds "Open settings". Opening the screen never prompts.
- A resolved point shows as a non-interactive map thumbnail at neighbourhood zoom with a centre pin, never coordinate text.
- An approximate fix (Android coarse, iOS reduced accuracy, or worse than 200 m) shows "Roughly here. Move the pin if this isn't right." and never blocks submit (ADR-0026, Proposed: the hint ships either way).
- "Adjust location" (also while locating) opens a full-screen centre-pin picker: the user moves the map under a fixed pin, then "Use this spot" or "Cancel". It opens at the resolved point, else at the map's camera.
- Back: closes the picker if open; does nothing while uploading; otherwise leaves (keep-or-discard comes with drafts, STORY-01-04-04).

## Interactions
- User actions: set metadata, optional audio, set visibility, submit/cancel.
- Feedback/animation notes: submit and success feedback follow global motion constraints.
- Validation behavior: required fields and limits enforced inline; no silent submit failures.

## Safety and Privacy UX
- Visibility/privacy defaults shown: default visibility is Public for 18+ and Followers for 13–17 in Phase 1 (ADR-0023), last-used thereafter; always shown explicitly with "your exact spot stays private".
- Reporting/blocking affordance: N/A on creation form.
- Sensitive data redaction/display rules: only coarse location confirmation visuals, no precise coordinate exposure.

## Platform Differences
- Android behavior: platform-native permission prompts and recording affordances.
- iOS behavior: platform-native permission prompts and recording affordances.
- Shared behavior constraints: identical validation rules, limits, and submission outcomes.

## Stitch Mapping (if used)
- stitch_artifact_id: `TBD-UXF-03-LOG`
- stitch_version: `TBD`
- mapped_frames: create-default, create-validation-error, create-uploading, create-success, create-offline
- unresolved deltas: premium cosmetic lock treatment and audio control layout variants

## Implementation Handoff
- Required components: preview map, selectors/chips/slider, locked-item treatment, voice row shown locked ("Premium — coming later"), text field (280 soft limit), visibility selector, submit CTA.
- State model expectations: `DRAFT`, `UPLOADING`, `SUCCESS`, `FAILED`, `OFFLINE`.
- Test/validation notes: verify draft durability, offline acceptance, visibility default, and recoverable error behavior.

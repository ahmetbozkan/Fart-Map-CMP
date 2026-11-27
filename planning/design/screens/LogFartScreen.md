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
- Back: closes the picker if open; does nothing while uploading; on a form the user changed it asks keep or discard (Drafts below); otherwise it leaves.

## Detailed fields (STORY-01-04-03)
- Order: where, when, avatar, flavor, intensity, story, sound, voice, visibility; "Let It Rip" stays pinned at the bottom, so the quick log is still one tap.
- Defaults: the screen-open time, Bean Bandit, Stinky (3 of 5), middle intensity, no caption, no sound.
- When: shows the local date and time; "Change" opens the date picker then the time picker. No max date.
- Avatar and sound: chips of the free and owned items (sound adds "No sound"). Locked items show a lock and can't be picked yet; the tap opens the Cosmetic Unlock Sheet once EPIC-01-06 ships it. Without a reachable catalog the starter items are offered.
- Flavor: five labelled steps (Fresh, Hmm, Stinky, Toxic, Lethal). Intensity: a slider from Whisper to Thunderclap.
- Story: one rotating placeholder per open, a counter, stops at 280 characters (UTF-16 units, an emoji counts 2).
- Voice: always shown locked ("Premium — coming later"); a tap opens an informational sheet with "Got it" only.
- Sound preview and the "Play sounds on tap" toggle come with marker sound playback (EPIC-01-05).

## Drafts (STORY-01-04-04)
- Once the user changes anything (a field, the visibility, a placed pin), the form saves itself half a second after the last change, while editing or after a failed submit. A failed submit saves at once and keeps every field.
- The draft never holds a location (neither the fix nor a pin) and keeps the time only if the user picked it. On the next open the location step runs as usual.
- Next open (same account): the fields come back with a note at the top, "Picked up where you left off." The note has no dismiss; back → Discard starts over. A locked or not-yet-owned item is not selected until it's usable. A visibility not allowed for the age band falls back to the default.
- Back on a changed form: a dialog with "Keep draft" and "Discard". Tapping outside or back stays on the form. An untouched form closes without asking.
- A successful or queued (offline) log clears the draft. Sign-out wipes it.

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
- Required components: preview map, date/time pickers, avatar and sound chips, flavor stepper, intensity slider, locked-item treatment (lock on the chip; unlock sheet in EPIC-01-06), voice row shown locked ("Premium — coming later") with its info sheet, text field (stops at 280, counter), visibility selector, submit CTA, restored-draft note, keep-or-discard dialog.
- State model expectations: `DRAFT`, `UPLOADING`, `SUCCESS`, `FAILED`, `OFFLINE`.
- Test/validation notes: verify draft durability, offline acceptance, visibility default, and recoverable error behavior.

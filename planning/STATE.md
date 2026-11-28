# STATE

> Live operating state. Keep it under about 40 lines; history belongs in git and PRs, decisions in `planning/decisions/`. The SessionStart hook prints the "Now" block. Update it with every story status change.
> Last updated: 2026-11-28.

## Now
- Phase 1, EPIC-01-01. STORY-01-01-01 Done. Planning consolidation, `.claude/` setup, static analysis and ADR-0022/0023/0024 acceptance merged (PRs #2–#5).
- Backend deploys to `fart-map-dev` from CI on every merge to `main` (first green run 2026-11-22: rules, indexes, `health`, `onFartCreated`, `europe-west1`).
- STORY-01-01-02/-03/-04 Done (PRs #6–#8): Koin, navigation shell, theme, observability, `StateViewModel`; Firestore cache, write path, preferences, Remote Config contract; rules skeleton + CI dev deploy. **EPIC-01-01 complete.**
- EPIC-01-02: STORY-01-02-01 Done (PR #9 merged as `96ac220`): anonymous session + 13+ age gate.
- Autopilot workflow merged (PR #10), with the hook-checked squash merge (PR #11, `4ddec11`).
- STORY-01-02-02 Done (PR #13): public profile `users/{uid}` (generated name + free starter avatar, created on Active), starter catalog in `backend/catalog/starter-catalog.json` + `npm run seed:catalog`.
- STORY-01-02-03 Done (PR #15): Google sign-in on Android (Credential Manager) and iOS (GoogleSignIn SDK). Apple moved to Phase 2, and -05 App Check is deferred (owner, issue #14).
- STORY-01-02-04 Done (PR #16): a guest links Google from the Profile tab; the `syncAccountProviders` callable re-derives `is_anonymous` / `linked_providers`. **EPIC-01-02 complete** except the deferred -05 (App Check). The repo is public now, so CI minutes are free (owner, issue #17).
- EPIC-01-03 (map) started and split into -01..-04 (ROADMAP). STORY-01-03-01 Done (PR #22): geohash-6 fuzzed projection in Kotlin (`core/domain/location`) and TS (`backend/functions/src/lib/location.ts`), proven equal by shared vectors; dev fart seed `npm run seed:farts`.
- STORY-01-03-02 Done (PR #23): the Map tab is a native map (Google Maps Compose / MapKit in Kotlin) behind `core/platform/MapHost`; default camera Istanbul, no permission asked; the camera survives tab switches.
- STORY-01-03-03 Done (PR #24): live markers from geohash-range listeners (`GeohashCover`, composite indexes), Mine / Followers (humorous card) / Everyone persisted in `map_filter_last_selected`, loading / empty / error+retry / offline states, `map_viewed` and `marker_opened`.
- STORY-01-03-04 Done: `core/platform/LocationProvider` (Play Services Location, coarse only / Core Location, in Kotlin), "center on me" asks on the first tap, denied / blocked / location-off notices, later starts open on the user, `location_permission_result`. **EPIC-01-03 complete.**
- EPIC-01-04 (Log Fart) started and split into -01..-06 (ROADMAP). STORY-01-04-01 Done: `FartRepository.createFart` (one never-awaited batch: public doc with the fuzzed point + owner-only `private/location`). Rules now require the private location in the creating batch and make it create-only. `onFartCreated` re-validates the public projection and is now in `PROD_FUNCTIONS`.
- STORY-01-04-02 Done (PR #29): quick log from the map FAB: location step (fix / "Use my location" / centre-pin picker, approximate hint), ADR-0023 visibility default, "Let It Rip" with success / offline / failed, pending markers at half opacity, `NetworkStatus` capability, fart rejections in a shell snackbar, `log_started` / `log_submitted`.
- STORY-01-04-03 Done: detailed fields on Log Fart (date/time pickers, avatar and sound chips from `cosmetic_items` + `user_entitlements` with a bundled starter fallback, flavor stepper, intensity slider, caption with a 280 stop, locked voice row + info sheet). Locked items are drawn but inert until EPIC-01-06; sound playback, preview and the sound toggle moved to EPIC-01-05.
- STORY-01-04-04 Done: Log Fart drafts. A changed form autosaves to `log_draft` (user-scoped; never coordinates; the time only if picked), is restored on the next open with a note, is cleared on hand-off and saved at once on failure. Back on a changed form asks Keep / Discard. `log_abandoned` with `step_reached`.
- STORY-01-04-05 Done: delete own fart. Tapping an own marker asks "Take this one back?"; Delete soft-deletes (`USER_DELETED`, never awaited, so the marker leaves at once, offline too). New `onFartRemoved` function purges `private/location` (in `PROD_FUNCTIONS`), and `onFartCreated` now skips a removed fart quietly. `fart_deleted` with `age_bucket`.

- STORY-01-04-06 Done: one-time account-link nudge. After a guest's first online log the shell shows "Keep this one forever?" (Link Google / Not now), once per user (`account_link_nudge_shown_at`, user-scoped); an offline log defers it. `account_link_prompted` / `account_linked`. **EPIC-01-04 complete**; test plan `planning/testing/EPIC-01-04.md`.

## Next
1. Owner: run the EPIC-01-04 test plan (`planning/testing/EPIC-01-04.md`, Android + iOS) and file failures as `bug` issues.
2. Owner: run the EPIC-01-03 test plan (`planning/testing/EPIC-01-03.md`, Android + iOS) and file failures as `bug` issues. Seed the dev catalog and dev test farts first (`OwnerTodo.md` A, D).
3. Owner: run the EPIC-01-02 test plan (`planning/testing/EPIC-01-02.md`) if not done yet.
4. EPIC-01-05 (reactions, detail sheet, safety) is next. Owner: decide ADR-0026 (location precision; -02 shipped with today's permissions and the hint). Owner: `.claude/rules/ios-shell.md` wording update suggested in `stories/STORY-01-03-02.md` Evidence (autopilot can't edit `.claude/`).
5. On the Mac: commit the `Package.resolved` Xcode updates (GoogleSignIn + FirebaseFunctions pins).

## Pending owner decisions
- ADR-0026 (Proposed): Log Fart location precision. Recommended: Android stays coarse, and an approximate fix shows a "move the pin" hint. The alternative is a fine-location ask on Log Fart. STORY-01-04-02 shipped with today's permissions and the hint.
- Further minor-safe defaults for the 13–17 band (PUBLIC is already blocked for them) and the other Phase 1 launch items in `planning/product/Scope.md` §7a.
- Launch catalog prices (RevenueCat Offerings) and the owned domain for App Links / Universal Links (ADR-0023/0024).
- Minimum iOS version (the template uses 18.2).
- Open items in `planning/product/Scope.md` §7a marked "Phase 1 launch": push preferences, sensitive zones.

## Owner actions (external consoles)
The full checklist, with instructions, is `planning/OwnerTodo.md`: A now (CI minutes, dev catalog seed), B the Mac/iOS manual check, C prod parity, D the Maps key, E the paid Apple account (Apple sign-in, App Check), F monetization and launch consoles.

## Decided (owner, 2026-11-22)
- Crash and analytics collection stays on in `prod` until the consent screen (EPIC-01-06) exists; then collection moves behind consent.
- PUBLIC is adults-only, enforced in rules (2026-11-23, ADR-0023 amendment): 13–17 users post FOLLOWERS or PRIVATE only.
- Autopilot may merge its own PRs after review and green CI, checked by the guard hook (2026-11-23; GitHub Free, no auto-merge). Defaults: stop at each epic boundary, merge everything that passes (flags `--cross-epics`, `--hold-sensitive`).
- Location permission (2026-11-26, issue #25): asked on the first "center on me" tap, never at launch; once granted, later starts open on the user.
- Phase 1 implementation prioritizes functionality; visual design (contrast, `surfaceContainer*` roles, fonts, icons) is revisited after implementation.

## Provisional
- Rate limits and cooldowns in `SocialSystems.md` §4/§9 (Phase 2).

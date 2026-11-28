# Roadmap

> Phases → epics → stories. Replaces the phase/epic/sprint files and templates (archived in `planning/archive/roadmap/`).
> - Phase content and gates: `planning/product/Scope.md`.
> - Story format and Definition of Done: `planning/WorkingAgreement.md`.
> - Author a story file only when its epic is next; don't generate a backlog beyond that.

## PHASE-01: Core Loop (Active)

**Goal:** install → first marker in under 60 s, fun and safe, on Android and iOS from one codebase.

**Exit:** Gate 1 in `Scope.md` §6 measurably passing, plus:
- rules and functions suites green;
- no entitlement bypass;
- no precise location in public read models;
- no secrets.

**Kernel features:**
- Map: MAP-01, MAP-02, MAP-03 (all filters).
- Create: CREATE-01–06, CREATE-07 (free + purchasable set), CREATE-09 (delete).
- Social and profile: SOCIAL-01, PROFILE-01, PROFILE-02 (light).
- Privacy and moderation: PRIV-01–03 (baseline), MOD-01, MOD-02 (baseline), MOD-03 (basic).
- Monetization and push: MON-01 (a la carte + Wardrobe shop + launch catalog + starter pack, ADR-0024), MON-03 (single-use), NOTIF-01 (reaction push; permission after first log, ADR-0023).
- Growth: SHARE-01-lite, SHARE-02 deep link + landing page, cold-start seeding (ADR-0023).

**Accepted 2026-11-22:** ADR-0022 (Firestore offline persistence), ADR-0023 (growth kernel, PUBLIC default for 18+ in Phase 1), ADR-0024 (monetization v2, single-use ads, no scope trims).

### EPIC-01-01: Bootstrap and walking skeleton (Done)

**Done when:**
- both apps launch to the four-tab shell (Map / Social / Rank / Profile) with placeholders;
- the auth entry shows Google / Continue without account (stubs allowed; Apple moved to Phase 2, owner 2026-11-25);
- the theme comes from `DesignSpecification.md` tokens;
- the observability base exists;
- `firebase deploy --only firestore:rules,functions` works against `dev` from CI;
- no tracked secrets.

**Stories:**
- [x] STORY-01-01-01: Project bootstrap and build pipeline (`stories/STORY-01-01-01.md`)
- [x] STORY-01-01-02 (`stories/STORY-01-01-02.md`): Core packages: Koin modules, typed navigation shell (four tabs + auth entry), theme tokens → `core/design`, observability base (`AppError`, `AppResult`, logger, Crashlytics/Analytics init, event contract), `StateViewModel` base class (makes the `new-feature` skill templates compile)
- [x] STORY-01-01-03 (`stories/STORY-01-01-03.md`): Local data layer (ADR-0022, `architecture/LocalData.md`): Firestore persistence settings at startup, `AppWriteScope` + `PendingWriteMonitor` (contracts in `shared`, implementation in `firebaseData`), sign-out `clearPersistence()`, DataStore keys from `LocalData.md` §5 including the age-band-aware visibility default, tests from §9. Also adds the `RemoteConfig` contract + GitLive config adapter with safe defaults (ADR-0024), and the `is_first_open` parameter of `app_open` (needs DataStore; deferred from STORY-01-01-02)
- [x] STORY-01-01-04 (`stories/STORY-01-01-04.md`): Backend: real rules skeleton per `DataContracts.md` §5 with a rules test per case, CI deploy to `dev`. Also fix `backend/functions/src/lib/idempotency.ts`, which claims the key before running the work (a failure is never retried) and has no TTL on `function_runs`; add tests for `runOnce` and `onFartCreated`. Keep `onFartCreated` out of prod until it does real work

### Planned epics (instantiate one at a time)

- **EPIC-01-02:** Session and identity: anonymous, Google, linking, 13+ age band.
  - **Owner, 2026-11-25 (issue #14):**
    - Sign in with Apple moves to Phase 2, because there is no paid Apple Developer account yet. App Store guideline 4.8 must be met before the iOS store release (`product/Growth.md` §2).
    - After -04, EPIC-01-03 starts.
  - [x] STORY-01-02-01: Anonymous session + 13+ age gate + private account doc (`stories/STORY-01-02-01.md`)
  - [x] STORY-01-02-02: Public profile (generated display name, free starter avatar) + dev seed of free starter catalog items (`stories/STORY-01-02-02.md`)
  - [x] STORY-01-02-03: Google sign-in on Android and iOS (`stories/STORY-01-02-03.md`)
  - [x] STORY-01-02-04: Link anonymous → Google (same uid, ADR-0018; Google only) from the Profile tab, plus the `syncAccountProviders` callable (`stories/STORY-01-02-04.md`)
  - [ ] STORY-01-02-05: App Check (Play Integrity / App Attest) + enforcement. **Deferred** until the paid Apple account exists (App Attest). Stays Phase 1 and release-blocking (ADR-0015); not needed to start EPIC-01-03
- **EPIC-01-03 (Done):** Map and markers on both platforms: filters, fuzzed projection, marker parity. Split in `stories/STORY-01-03-01.md` §Decisions. Manual test plan: `testing/EPIC-01-03.md`.
  - [x] STORY-01-03-01: Fuzzed location projection (Kotlin + TS twin, shared vectors) + dev fart seed (`stories/STORY-01-03-01.md`)
  - [x] STORY-01-03-02 (`stories/STORY-01-03-02.md`): Map host platform capability: Google Maps Compose (Android) and MapKit through `UIKitView` (iOS) behind one shared contract; renders shared marker models, reports marker taps and viewport changes; replaces the Map tab placeholder (default camera)
  - [x] STORY-01-03-03 (`stories/STORY-01-03-03.md`): Map data and filters: `observeMapFarts(viewport, filter)` over geohash range queries + composite indexes; Mine / Followers (humorous empty state, ADR-0020) / Everyone persisted in `map_filter_last_selected`; loading / empty / success / error / offline states; `map_viewed` and `marker_opened` events (the detail sheet is EPIC-01-05). Its acceptance also proves on device what -02 only compiles: markers render and are removed, a tap reports the id (twice on the same marker too), and the TalkBack/VoiceOver marker label
  - [x] STORY-01-03-04 (`stories/STORY-01-03-04.md`): Device location: `LocationProvider` contract (Play Services Location / Core Location), foreground permission asked on the first "center on me" tap (owner, issue #25), denied / blocked / location-off notices, later starts open on the user
- **EPIC-01-04 (Done):** Log Fart: quick log + detailed log, drafts, offline (Firestore queue), delete, visibility default per ADR-0023. Also the one-time account-link nudge after the first successful log (ADR-0018 §3; reuses `SessionRepository.linkGoogle`, throttle key `account_link_nudge_shown_at`). Split in `stories/STORY-01-04-01.md` §Decisions.
  - [x] STORY-01-04-01: Fart create path: `FartRepository.createFart` (one batch, public + private location, never awaited), rules require the private location in the creating batch, `onFartCreated` re-validates the public projection (`stories/STORY-01-04-01.md`)
  - [x] STORY-01-04-02: Quick log: map action → Log Fart screen with the location step (device fix, "Adjust location" pin, the approximate-fix hint per ADR-0026), visibility selector with the ADR-0023 default, defaults for the other fields, "Let It Rip", uploading / success / failed / offline states, the new marker with the pending style, `last_visibility_mode`, `log_started` / `log_submitted` (`stories/STORY-01-04-02.md`)
  - [x] STORY-01-04-03: Detailed fields: date/time, avatar and sound pickers (free + owned; locked items wait for EPIC-01-06's unlock sheet), flavor stepper, intensity slider, description (rotating placeholders, 280 counter), the locked voice row (`stories/STORY-01-04-03.md`)
  - [x] STORY-01-04-04: Drafts: `log_draft` survives process death, restored on reopen, cleared on hand-off, kept on failure; keep-or-discard on back; `log_abandoned` (`stories/STORY-01-04-04.md`)
  - [x] STORY-01-04-05: Delete own fart (CREATE-09): owner action on the user's own marker, soft delete `USER_DELETED`, server purge of `private/location`
  - [x] STORY-01-04-06: One-time account-link nudge after a guest's first successful log (ADR-0018 §3). Manual test plan: `testing/EPIC-01-04.md`.
- **EPIC-01-05:** Reactions, detail sheet and safety: report, block, auto-hide at 3. Also the marker sound on tap, the sound preview in the Log Fart picker and the "Play sounds on tap" toggle (CREATE-03.1 step 7; moved from STORY-01-04-03, which ships the picker without playback).
- **EPIC-01-06:** Cosmetics monetization: launch catalog, Wardrobe shop, starter pack offer, RevenueCat purchases + Offerings, AdMob rewarded (single-use) with server-side verification and Remote Config cap, entitlements mirror (ADR-0016/0017/0024).
- **EPIC-01-07:** Social tab, Rank tab, Profile and Settings (Phase 1 variants; all four tabs stay, ADR-0024 accepted with no trims). Keep the guest link card on Profile (STORY-01-02-04) and add it to Rank (`RankTabScreen.md`). Sign-out with the anonymous-loss warning (ADR-0018 §4) is also the way out for a guest whose Google account already has its own Fart Map account (link conflict).
- **EPIC-01-08:** Reaction push notification and deep link; permission prompt after the first successful log (ADR-0023).
- **EPIC-01-09:** Backend rules, functions, emulator tests and CI deploy (cross-cutting; grows with each epic).
- **EPIC-01-10:** Growth kernel (ADR-0023): share card rendering on both platforms, App Links / Universal Links on an owned domain, web landing page, Play Install Referrer, official landmark markers (admin script), aggregated regional counts.
- **EPIC-01-11:** Launch readiness (`planning/product/Growth.md` §8): store listings, privacy labels / Data Safety, review notes, analytics funnels incl. revenue metrics, closed test.

## PHASE-02 … PHASE-05

Not instantiated. Scope is in `Scope.md` §3–§5. Start only after Gate 1 passes, unless the owner accepts a proposal that moves something earlier.

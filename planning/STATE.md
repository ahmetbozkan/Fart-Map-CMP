# STATE

> Live operating state. Keep it under about 40 lines; history belongs in git and PRs, decisions in `planning/decisions/`. The SessionStart hook prints the "Now" block. Update it with every story status change.
> Last updated: 2026-11-25.

## Now
- Phase 1, EPIC-01-01. STORY-01-01-01 Done. Planning consolidation, `.claude/` setup, static analysis and ADR-0022/0023/0024 acceptance merged (PRs #2–#5).
- Backend deploys to `fart-map-dev` from CI on every merge to `main` (first green run 2026-11-22: rules, indexes, `health`, `onFartCreated`, `europe-west1`).
- STORY-01-01-02/-03/-04 Done (PRs #6–#8): Koin, navigation shell, theme, observability, `StateViewModel`; Firestore cache, write path, preferences, Remote Config contract; rules skeleton + CI dev deploy. **EPIC-01-01 complete.**
- EPIC-01-02: STORY-01-02-01 Done (PR #9 merged as `96ac220`): anonymous session + 13+ age gate.
- Autopilot workflow merged (PR #10), with the hook-checked squash merge (PR #11, `4ddec11`).
- STORY-01-02-02 Done (PR #13): public profile `users/{uid}` (generated name + free starter avatar, created on Active), starter catalog in `backend/catalog/starter-catalog.json` + `npm run seed:catalog`.
- STORY-01-02-03 Done (PR #15): Google sign-in on Android (Credential Manager) and iOS (GoogleSignIn SDK). Apple moved to Phase 2, and -05 App Check is deferred (owner, issue #14).
- STORY-01-02-04 **Blocked** (PR #16, reviewed, verify green locally): a guest links Google from the Profile tab; the `syncAccountProviders` callable re-derives `is_anonymous` / `linked_providers`. CI jobs refuse to start: GitHub Actions billing (owner, see the `needs-owner` issue).

## Next
1. **Owner: fix GitHub billing / the Actions spending limit**, re-run CI on PR #16 (or re-run the autopilot driver, which resumes it), then the merge finishes EPIC-01-02 (except the deferred -05).
2. Owner: seed the dev catalog once (Owner actions), then check `users/{uid}` on a fresh dev install.
3. On the Mac: run `FartMap Dev` once (shell, Google sign-in, Profile → Link Google), and commit the `Package.resolved` it updates (GoogleSignIn + FirebaseFunctions pins).
4. EPIC-01-03 (map; the Maps key isn't in place yet).

## Pending owner decisions
- Further minor-safe defaults for the 13–17 band (PUBLIC is already blocked for them) and the other Phase 1 launch items in `planning/product/Scope.md` §7a.
- Launch catalog prices (RevenueCat Offerings) and the owned domain for App Links / Universal Links (ADR-0023/0024).
- Minimum iOS version (the template uses 18.2).
- Open items in `planning/product/Scope.md` §7a marked "Phase 1 launch": push preferences, sensitive zones.

## Owner actions (external consoles)
- **Dev catalog seed (unblocks profile creation on dev):**
  1. `gcloud auth application-default login`;
  2. `cd backend/functions && npm run seed:catalog -- --project fart-map-dev`.

  Until then, profile creates on dev are logged as `write_rejected`.
- **Release-blocking:**
  - seed the prod catalog before any prod release (EPIC-01-06; the seed script only seeds `fart-map-dev` and emulators);
  - set up Authentication with Anonymous + Google on `fart-map-1a558`: release and Play app-signing SHA-1/SHA-256, then re-download the prod configs;
  - iOS: meet App Store guideline 4.8 (Apple sign-in is Phase 2) and App Check (STORY-01-02-05) once the paid Apple Developer account exists.
- Prod (`fart-map-1a558`) before the first release deploy: the same setup as dev (deploy roles incl. Service Account User, APIs incl. Cloud Run / Eventarc / Cloud Billing, the three service-agent roles, Blaze); expect one retry for Eventarc propagation.
- Add the iOS configs on the Mac (`~/.fartmap/` + `scripts/inject-config.sh`) and `androidApp/secrets.properties` once the map lands.
- Inter and JetBrains Mono font files (OFL) for `core/design`, or approve the download.
- Later epics: App Check registration (STORY-01-02-05, deferred); RevenueCat project + Offerings, AdMob app + test units, Remote Config template, App Store Connect / Play Console products (EPIC-01-06); a domain for links + landing page (EPIC-01-10).

## Decided (owner, 2026-11-22)
- Crash and analytics collection stays on in `prod` until the consent screen (EPIC-01-06) exists; then collection moves behind consent.
- PUBLIC is adults-only, enforced in rules (2026-11-23, ADR-0023 amendment): 13–17 users post FOLLOWERS or PRIVATE only.
- Autopilot may merge its own PRs after review and green CI, checked by the guard hook (2026-11-23; GitHub Free, no auto-merge). Defaults: stop at each epic boundary, merge everything that passes (flags `--cross-epics`, `--hold-sensitive`).
- Phase 1 implementation prioritizes functionality; visual design (contrast, `surfaceContainer*` roles, fonts, icons) is revisited after implementation.

## Provisional
- Rate limits and cooldowns in `SocialSystems.md` §4/§9 (Phase 2).

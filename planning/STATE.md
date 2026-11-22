# STATE

> Live operating state. Keep it under about 40 lines; history belongs in git and PRs, decisions in `planning/decisions/`. The SessionStart hook prints the "Now" block. Update it with every story status change.
> Last updated: 2026-11-22.

## Now
- Phase 1, EPIC-01-01. STORY-01-01-01 Done. Planning consolidation, `.claude/` setup, static analysis and ADR-0022/0023/0024 acceptance merged (PRs #2–#5).
- Backend deploys to `fart-map-dev` from CI on every merge to `main` (first green run 2026-11-22: rules, indexes, `health`, `onFartCreated`, `europe-west1`).
- STORY-01-01-02 Done (PR #6 merged as `ceb2c71`): Koin, typed navigation shell, theme tokens, observability base, `StateViewModel`.
- STORY-01-01-03 (local data layer: Firestore cache, write path, preferences, sign-out wipe, Remote Config contract) in Review: draft PR from `07-local-data`. Android verified locally; iOS by the CI `ios` job.

## Next
1. Owner reviews and merges the STORY-01-01-03 PR. On the Mac, run `FartMap Dev` once to confirm the STORY-01-01-02 shell on iOS.
2. STORY-01-01-04 (backend rules skeleton, `runOnce` fix).
3. EPIC-01-02 (session and identity) replaces the in-memory session stub.

## Pending owner decisions
- Minor-safe defaults for the 13–17 band (currently: never PUBLIC by default, FOLLOWERS) and the other Phase 1 launch items in `planning/product/Scope.md` §7a.
- Launch catalog prices (RevenueCat Offerings) and the owned domain for App Links / Universal Links (ADR-0023/0024).
- Minimum iOS version (the template uses 18.2).
- Open items in `planning/product/Scope.md` §7a marked "Phase 1 launch": push preferences, sensitive zones.

## Owner actions (external consoles)
- Prod (`fart-map-1a558`) before the first release deploy: the same setup as dev (deploy roles incl. Service Account User, APIs incl. Cloud Run / Eventarc / Cloud Billing, the three service-agent roles, Blaze); expect one retry for Eventarc propagation.
- Add the iOS configs on the Mac (`~/.fartmap/` + `scripts/inject-config.sh`) and `androidApp/secrets.properties` once the map lands.
- Inter and JetBrains Mono font files (OFL) for `core/design`, or approve the download.
- Later epics: Auth providers + App Check (EPIC-01-02); RevenueCat project + Offerings, AdMob app + test units, Remote Config template, App Store Connect / Play Console products (EPIC-01-06); a domain for links + landing page (EPIC-01-10).
- Install `gh` (GitHub CLI) on this Windows machine so agents can open draft PRs.

## Decided (owner, 2026-11-22)
- Crash and analytics collection stays on in `prod` until the consent screen (EPIC-01-06) exists; then collection moves behind consent.
- Phase 1 implementation prioritizes functionality; visual design (contrast, `surfaceContainer*` roles, fonts, icons) is revisited after implementation.

## Provisional
- Rate limits and cooldowns in `SocialSystems.md` §4/§9 (Phase 2).

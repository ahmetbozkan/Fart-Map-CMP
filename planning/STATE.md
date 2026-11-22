# STATE

> Live operating state. Keep it under about 40 lines; history belongs in git and PRs, decisions in `planning/decisions/`. The SessionStart hook prints the "Now" block. Update it with every story status change.
> Last updated: 2026-11-22.

## Now
- Phase 1, EPIC-01-01. STORY-01-01-01 is Done (PR #1 merged as `66f9d8b`).
- Draft PRs in review (stacked): #2 `02-agentic-setup` (planning consolidation, `.claude/` setup) → #3 `03-static-analysis` (detekt, Konsist, `scripts/verify.sh`) → `04-accept-adr-0022-0024`.
- ADR-0022/0023/0024 **accepted 2026-11-22** and applied to the docs: Firestore offline persistence (`architecture/LocalData.md`), Phase 1 growth kernel with a PUBLIC default for 18+, monetization v2 (shop, catalog, starter pack, Remote Config, subscription in Phase 2; single-use ads; no scope trims).

## Next
1. Owner reviews and merges the stacked PRs #2 → #3 → the ADR-acceptance PR.
2. STORY-01-01-02: core packages (DI, navigation shell, theme, observability, `StateViewModel` base). Author `planning/stories/STORY-01-01-02.md` from `planning/ROADMAP.md`.
3. STORY-01-01-03 (Firestore persistence, write pattern, Remote Config contract) and STORY-01-01-04 (backend rules skeleton, `runOnce` fix).

## Pending owner decisions
- Minor-safe defaults for the 13–17 band (currently: never PUBLIC by default, FOLLOWERS) and the other Phase 1 launch items in `planning/product/Scope.md` §7a.
- Launch catalog prices (RevenueCat Offerings) and the owned domain for App Links / Universal Links (ADR-0023/0024).
- Minimum iOS version (the template uses 18.2).
- Open items in `planning/product/Scope.md` §7a marked "Phase 1 launch": push preferences, sensitive zones.

## Owner actions (external consoles)
- Confirm the GitHub secrets exist (`GOOGLE_SERVICES_JSON_DEV/PROD`, `GOOGLE_SERVICE_INFO_PLIST_DEV/PROD`; `FIREBASE_SERVICE_ACCOUNT` in environments `dev` and `prod`, with a required reviewer on prod).
- Blaze plan + Firestore (native, `europe-west1`) + Storage on `fart-map-dev` and `fart-map-1a558`; repository stays private.
- Later epics: Auth providers + App Check (EPIC-01-02); RevenueCat project + Offerings, AdMob app + test units, Remote Config template, App Store Connect / Play Console products (EPIC-01-06); a domain for links + landing page (EPIC-01-10).
- Install `gh` (GitHub CLI) on this Windows machine so agents can open draft PRs.

## Provisional
- Rate limits and cooldowns in `SocialSystems.md` §4/§9 (Phase 2).

# STATE

> Live operating state. Keep it under about 40 lines; history belongs in git and PRs, decisions in `planning/decisions/`. The SessionStart hook prints the "Now" block. Update it with every story status change.
> Last updated: 2026-11-21.

## Now
- Phase 1, EPIC-01-01. STORY-01-01-01 is Done (PR #1 merged as `66f9d8b`).
- Branch `02-agentic-setup` is in review:
  - planning consolidated;
  - `.claude/` set up (rules, skills, agents, hooks);
  - growth/monetization and offline-data proposals (ADR-0022/23/24, Proposed).
- Branch `03-static-analysis` adds detekt, Konsist, `scripts/verify.sh` and doc-link checks.

## Next
1. Owner reviews and merges the two PRs above, then decides ADR-0022 / 0023 / 0024.
2. STORY-01-01-02: core packages (DI, navigation shell, theme, observability, `StateViewModel` base). Author `planning/stories/STORY-01-01-02.md` from `planning/ROADMAP.md`.
3. STORY-01-01-04: backend rules skeleton and the `runOnce` fix. STORY-01-01-03 waits for ADR-0022.

## Pending owner decisions
- **ADR-0022** (Proposed): Firestore offline persistence instead of Room + SyncEngine. Blocks STORY-01-01-03.
- **ADR-0023** (Proposed): Phase 1 growth kernel. Includes a privacy-default choice (Public vs Followers default in Phase 1).
- **ADR-0024** (Proposed): monetization v2 (shop, catalog, starter pack, Remote Config, earlier subscription, rewarded-ad value).
- Minimum iOS version (the template uses 18.2).
- Open items in `planning/product/Scope.md` §7a marked "Phase 1 launch": push preferences, minor-safe defaults, sensitive zones.

## Owner actions (external consoles)
- Confirm the GitHub secrets exist (`GOOGLE_SERVICES_JSON_DEV/PROD`, `GOOGLE_SERVICE_INFO_PLIST_DEV/PROD`; `FIREBASE_SERVICE_ACCOUNT` in environments `dev` and `prod`, with a required reviewer on prod).
- Blaze plan + Firestore (native, `europe-west1`) + Storage on `fart-map-dev` and `fart-map-1a558`; repository stays private.
- Later epics: Auth providers + App Check (EPIC-01-02); RevenueCat project, AdMob app + test units, App Store Connect / Play Console products (EPIC-01-06).
- Install `gh` (GitHub CLI) on this Windows machine so agents can open draft PRs.

## Provisional
- Rate limits and cooldowns in `SocialSystems.md` §4/§9 (Phase 2).

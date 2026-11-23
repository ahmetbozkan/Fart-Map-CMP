# STATE

> Live operating state. Keep it under about 40 lines; history belongs in git and PRs, decisions in `planning/decisions/`. The SessionStart hook prints the "Now" block. Update it with every story status change.
> Last updated: 2026-11-23.

## Now
- Phase 1, EPIC-01-01. STORY-01-01-01 Done. Planning consolidation, `.claude/` setup, static analysis and ADR-0022/0023/0024 acceptance merged (PRs #2–#5).
- Backend deploys to `fart-map-dev` from CI on every merge to `main` (first green run 2026-11-22: rules, indexes, `health`, `onFartCreated`, `europe-west1`).
- STORY-01-01-02 Done (PR #6 merged as `ceb2c71`): Koin, typed navigation shell, theme tokens, observability base, `StateViewModel`.
- STORY-01-01-03 Done (PR #7 merged as `30aa4fa`): Firestore cache, write path, preferences, sign-out wipe, Remote Config contract.
- STORY-01-01-04 Done (PR #8 merged as `fb275fd`; dev deploy green: rules, indexes + `function_runs` TTL, functions). **EPIC-01-01 complete.**
- EPIC-01-02: STORY-01-02-01 Done (PR #9 merged as `96ac220`): anonymous session + 13+ age gate.
- Autopilot workflow (`scripts/autopilot.sh` + `/autopilot-story`) merged (PR #10). GitHub Free has no auto-merge or branch protection on private repos, so merges move to a hook-checked squash merge: in Review on `11-hook-checked-merge`.

## Next
1. Owner merges the hook-checked merge PR, then runs `bash scripts/autopilot.sh --max 1` once and watches it. Before release: set up Authentication + Anonymous on `fart-map-1a558` too.
2. On the Mac, run `FartMap Dev` once to confirm the STORY-01-01-02/03 shell on iOS.
3. STORY-01-02-02 to -05 (profile + starter catalog seed, Google/Apple, linking, App Check), then EPIC-01-03 (map). -03 and -05 need owner console setup first.

## Pending owner decisions
- Further minor-safe defaults for the 13–17 band (PUBLIC is already blocked for them) and the other Phase 1 launch items in `planning/product/Scope.md` §7a.
- Launch catalog prices (RevenueCat Offerings) and the owned domain for App Links / Universal Links (ADR-0023/0024).
- Minimum iOS version (the template uses 18.2).
- Open items in `planning/product/Scope.md` §7a marked "Phase 1 launch": push preferences, sensitive zones.

## Owner actions (external consoles)
- Prod (`fart-map-1a558`) before the first release deploy: the same setup as dev (deploy roles incl. Service Account User, APIs incl. Cloud Run / Eventarc / Cloud Billing, the three service-agent roles, Blaze); expect one retry for Eventarc propagation.
- Add the iOS configs on the Mac (`~/.fartmap/` + `scripts/inject-config.sh`) and `androidApp/secrets.properties` once the map lands.
- Inter and JetBrains Mono font files (OFL) for `core/design`, or approve the download.
- Later epics: Auth providers + App Check (EPIC-01-02); RevenueCat project + Offerings, AdMob app + test units, Remote Config template, App Store Connect / Play Console products (EPIC-01-06); a domain for links + landing page (EPIC-01-10).

## Decided (owner, 2026-11-22)
- Crash and analytics collection stays on in `prod` until the consent screen (EPIC-01-06) exists; then collection moves behind consent.
- PUBLIC is adults-only, enforced in rules (2026-11-23, ADR-0023 amendment): 13–17 users post FOLLOWERS or PRIVATE only.
- Autopilot may merge its own PRs after review and green CI, checked by the guard hook (2026-11-23; GitHub Free, no auto-merge). Defaults: stop at each epic boundary, merge everything that passes (flags `--cross-epics`, `--hold-sensitive`).
- Phase 1 implementation prioritizes functionality; visual design (contrast, `surfaceContainer*` roles, fonts, icons) is revisited after implementation.

## Provisional
- Rate limits and cooldowns in `SocialSystems.md` §4/§9 (Phase 2).

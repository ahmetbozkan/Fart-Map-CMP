# STATE

> Live operating state. Keep it under about 40 lines; history belongs in git and PRs, decisions in `planning/decisions/`. The SessionStart hook prints the "Now" block. Update it with every story status change.
> Last updated: 2026-11-25.

## Now
- Phase 1, EPIC-01-01. STORY-01-01-01 Done. Planning consolidation, `.claude/` setup, static analysis and ADR-0022/0023/0024 acceptance merged (PRs #2–#5).
- Backend deploys to `fart-map-dev` from CI on every merge to `main` (first green run 2026-11-22: rules, indexes, `health`, `onFartCreated`, `europe-west1`).
- STORY-01-01-02 Done (PR #6 merged as `ceb2c71`): Koin, typed navigation shell, theme tokens, observability base, `StateViewModel`.
- STORY-01-01-03 Done (PR #7 merged as `30aa4fa`): Firestore cache, write path, preferences, sign-out wipe, Remote Config contract.
- STORY-01-01-04 Done (PR #8 merged as `fb275fd`; dev deploy green: rules, indexes + `function_runs` TTL, functions). **EPIC-01-01 complete.**
- EPIC-01-02: STORY-01-02-01 Done (PR #9 merged as `96ac220`): anonymous session + 13+ age gate.
- Autopilot workflow merged (PR #10), with the hook-checked squash merge (PR #11, `4ddec11`).
- STORY-01-02-02 Done (PR #13): public profile `users/{uid}` (generated name + free starter avatar, created on Active), starter catalog in `backend/catalog/starter-catalog.json` + `npm run seed:catalog`.
- STORY-01-02-03 Done (PR #15): Google sign-in on Android (Credential Manager) and iOS (GoogleSignIn SDK). Apple moved to Phase 2, and -05 App Check is deferred (owner, issue #14).

## Next
1. Owner: seed the dev catalog once (Owner actions), then check `users/{uid}` on a fresh dev install.
2. On the Mac: run `FartMap Dev` once (shell + Google sign-in), and commit the `Package.resolved` it updates.
3. STORY-01-02-04 (link anonymous → Google), then EPIC-01-03 (map; the Maps key isn't in place yet).

## Pending owner decisions
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
- Phase 1 implementation prioritizes functionality; visual design (contrast, `surfaceContainer*` roles, fonts, icons) is revisited after implementation.

## Provisional
- Rate limits and cooldowns in `SocialSystems.md` §4/§9 (Phase 2).

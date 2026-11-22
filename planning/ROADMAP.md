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
- Monetization and push: MON-01 (a la carte), MON-03, NOTIF-01 (reaction push).

**Pending proposals that would change Phase 1:**
- ADR-0022: offline data layer.
- ADR-0023: growth kernel (share + deep link, cold start).
- ADR-0024: monetization v2.

### EPIC-01-01: Bootstrap and walking skeleton (Active)

**Done when:**
- both apps launch to the four-tab shell (Map / Social / Rank / Profile) with placeholders;
- the auth entry shows Google / Apple / Continue without account (stubs allowed);
- the theme comes from `DesignSpecification.md` tokens;
- the observability base exists;
- `firebase deploy --only firestore:rules,functions` works against `dev` from CI;
- no tracked secrets.

**Stories:**
- [x] STORY-01-01-01: Project bootstrap and build pipeline (`stories/STORY-01-01-01.md`)
- [ ] STORY-01-01-02: Core packages: Koin modules, typed navigation shell (four tabs + auth entry), theme tokens → `core/design`, observability base (`AppError`, `Result`, logger, Crashlytics/Analytics init, event contract), `StateViewModel` base class (makes the `new-feature` skill templates compile)
- [ ] STORY-01-01-03: Local data layer: **blocked on the ADR-0022 decision.** Room + PendingOperation + SyncEngine if rejected; Firestore persistence config + write pattern + `PendingWriteMonitor` if accepted
- [ ] STORY-01-01-04: Backend: real rules skeleton per `DataContracts.md` §5 with a rules test per case, CI deploy to `dev`. Also fix `backend/functions/src/lib/idempotency.ts`, which claims the key before running the work (a failure is never retried) and has no TTL on `function_runs`; add tests for `runOnce` and `onFartCreated`. Keep `onFartCreated` out of prod until it does real work

### Planned epics (instantiate one at a time)

- **EPIC-01-02:** Session and identity: anonymous, Google, Apple, linking, 13+ age band.
- **EPIC-01-03:** Map and markers on both platforms: filters, fuzzed projection, marker parity.
- **EPIC-01-04:** Log Fart: quick log + detailed log, drafts, offline, delete.
- **EPIC-01-05:** Reactions, detail sheet and safety: report, block, auto-hide at 3.
- **EPIC-01-06:** Cosmetics monetization: catalog, RevenueCat purchases, AdMob rewarded with server-side verification, entitlements mirror.
- **EPIC-01-07:** Social tab, Rank tab, Profile and Settings (Phase 1 variants; see the trim candidates in ADR-0024).
- **EPIC-01-08:** Reaction push notification and deep link.
- **EPIC-01-09:** Backend rules, functions, emulator tests and CI deploy (cross-cutting; grows with each epic).
- **Launch readiness** (proposed in `planning/product/Growth.md`): store listings, privacy labels / Data Safety, review notes, share card, analytics funnel.

## PHASE-02 … PHASE-05

Not instantiated. Scope is in `Scope.md` §3–§5. Start only after Gate 1 passes, unless the owner accepts a proposal that moves something earlier.

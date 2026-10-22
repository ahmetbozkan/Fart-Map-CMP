# PHASE-01: Core Loop

## Status
- Workflow state: Active
- Owner: product owner (decisions) + lead implementation agent (delivery)
- Horizon: from bootstrap of the new repository until Gate 1 (`planning/product/ReleaseStrategy.md` §6) passes

## Phase Intent
- Objective: prove that opening the app, seeing a living map, logging a fart and seeing it appear is fun, fast and safe — on Android and iOS from one codebase.
- Why now: everything later (social retention, progression, community) depends on this loop being healthy (`ReleaseStrategy.md` §2 Principle 1).
- User-value outcome: install → first marker on the map in under 60 seconds; first reaction received or given within 24 hours.

## Scope Boundaries
- Included feature systems (Phase 1 kernel, `Features.md` §18 + ADR-0020): `MAP-01`, `MAP-02`, `MAP-03` (all filters), `CREATE-01`–`CREATE-06`, `CREATE-09` (delete), `SOCIAL-01`, `PROFILE-01`, `PROFILE-02` (light), `PRIV-01`, `PRIV-02`, `PRIV-03` (baseline), `MOD-01` (report + block), `MOD-02` (baseline), `MOD-03` (basic), `MON-01` (a la carte avatars/sounds), `MON-03` (rewarded ads), `NOTIF-01` (reaction push only).
- Explicit exclusions: comments, follow graph, feed, edit fart, streaks, XP, levels, leaderboards, achievements, signature catalog, subscription, voice recording, share cards, groups, challenges, territory, direct messaging.
- Dependency notes: backend compute (ADR-0015), purchases (ADR-0016) and ads (ADR-0017) are Phase 1 infrastructure; anonymous sessions (ADR-0018) and the location split (ADR-0019) shape every data contract.

## Traceability
- ReleaseStrategy links: §3 Phase 1, §4 MVP definition, §5 matrix (Phase 1 rows), §6 Gate 1
- Product pillar links: Location 35 / Humor 30 / Social 20 / Identity 10 / Progress 5 (`ReleaseStrategy.md` §3)
- Loop links: L1 (full), L2 (seed), L3 (seed), L5 (seed via cosmetics identity), L8 (seed via system share)
- Success metrics: first-session loop completion ≥ 60%; median time-to-first-log < 60 s; first social touch ≥ 40%; log abandonment < 30%; safety baseline met; monetization integrity (`ReleaseStrategy.md` §6 Gate 1)

## Entry Gates
- Gate 1 (met for bootstrap on 2026-10-17): new repository `github.com/ahmetbozkan/Fart-Map-CMP`, Firebase projects `fart-map-dev` and `fart-map-1a558`, app ids `com.ahmetbozkan.fartmap` / `.dev`, `planning/` copied. Still owed before the named epics: Blaze plan + Firestore/Storage enabled (EPIC-01-01 TASK-06), Auth providers and App Check registration (EPIC-01-02), RevenueCat project and AdMob app with test units (EPIC-01-06), App Store Connect and Play Console apps and store products (EPIC-01-06).
- Gate 2: `TechStack.md` §3.1 baseline filled by the bootstrap story; validation commands documented in the repository README.

## Exit Gates
- Exit criterion 1: all Gate 1 criteria in `ReleaseStrategy.md` §6 measurably passing on production analytics.
- Exit criterion 2: rules and functions test suites green; no entitlement bypass; no precise location in any public read model; no secrets in the repository.

## Risk Register
- Product/scope risk: Followers default with no follow graph keeps the public map thin; mitigated by the Social tab nearby list and explicit visibility choice (owner accepted).
- Technical risk: iOS Firebase linking and shared-test isolation (PoC failure); mitigated by `TechStack.md` §6.2 and the bootstrap story's link check.
- Safety/privacy risk: anonymous sessions can create content; mitigated by App Check, report/block, auto-hide and the age gate (ADR-0018).

## Planned Epics
- EPIC-01-01: Bootstrap and walking skeleton (this document's first instance, authored)
- EPIC-01-02: Session and identity (anonymous, Google, Apple, linking, age gate)
- EPIC-01-03: Map and markers on both platforms (filters, fuzzed projection, marker parity)
- EPIC-01-04: Log Fart (10 steps, drafts, offline queue, delete)
- EPIC-01-05: Reactions, detail sheet and safety (report, block, auto-hide)
- EPIC-01-06: Cosmetics monetization (catalog, purchases, rewarded ads, entitlements)
- EPIC-01-07: Social tab, Rank tab, Profile and Settings (Phase 1 variants)
- EPIC-01-08: Reaction push notification and deep link
- EPIC-01-09: Backend rules, functions, emulator tests and CI deploy (cross-cutting)

## Evidence and Notes
- Gate verification evidence: none yet (no implementation).
- Open issues/blockers: owner deliverables (Entry Gate 1); provisional parameters in ADR-0019 to confirm before EPIC-01-03 enters Ready.

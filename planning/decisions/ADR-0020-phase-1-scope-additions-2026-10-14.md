# ADR-0020: Phase 1 Scope Additions (Owner Decision, 2026-10-14)

- Status: Accepted; amended by ADR-0023 and ADR-0024 (2026-11-22): the Phase 1 visibility default, the notification-permission timing and the subscription phase below are changed there; everything else stands.
- Date: 2026-10-14
- Source: owner answers to the closeout questions C1–C4, K2 (recorded per `Product.md` "Phase reassignment")

## Context
The MVP in `Scope.md` §4 kept monetization and push notifications out of Phase 1 and did not define the root navigation. The owner wants cosmetics monetized from launch and the four-tab navigation of the PoC retained.

## Decision
Phase 1 (MVP) additionally includes:
1. **MON-01 a la carte purchases** of avatars and sounds, permanent ownership (ADR-0016). Confirmed 2026-10-15: the monetized items are the locked cosmetics; the five MVP reactions stay free (`Monetization.md` §2 rule 5).
2. **MON-03 rewarded ads** for avatars and sounds, single-use one action (ADR-0017).
3. **NOTIF-01 minimal:** a push notification when a user's fart receives a reaction, with notification permission requested in context (after the first reaction; changed by ADR-0023 to after the first successful log), and a deep link to the marker.
4. **CREATE-09 delete** own fart. Edit stays Phase 2.
5. **MAP-03 all three filters** (Mine / Followers / Everyone). The Followers filter shows an empty state with humor copy until SOCIAL-03 ships in Phase 2.
6. **Four-tab root navigation: Map / Social / Rank / Profile**, with the Log action as the map FAB. Phase 1 content (confirmed by the owner on 2026-10-15): Social tab = map-anchored list of nearby public farts with "View on Map"; Rank tab = the user's own stats (farts logged, reactions received) plus a leaderboard teaser. Leaderboards remain Phase 3.

Unchanged at the time (both later amended by ADR-0023/0024): visibility default Followers (ADR-0010); follower count hidden until Phase 2; subscription (MON-02) Phase 4; comments, follow graph, feed Phase 2; streaks, XP, achievements Phase 3.

## Rationale
- Owner's monetization strategy requires purchase and ad paths to be real at launch; "safety ships with the phase" is honored by App Check, server-side entitlement checks, report/block and the age gate.
- Retaining the PoC tab structure gives Phase 2/3 features a stable home and avoids a navigation redesign later.

## Consequences
- `Scope.md` §4/§5, `features/` (CREATE-03.1/03.2, MAP-03, CREATE-09, NOTIF-01, onboarding), `Monetization.md`, `DesignSpecification.md` §9.4/§10/§11, `Architecture.md` and `planning/ROADMAP.md` are updated.
- Gate 1 (`Scope.md` §6) gains a monetization-integrity criterion: no entitlement bypass in rules tests.

# ADR-0023: Phase 1 Growth Kernel (Share Card, Deep Links, Cold-Start Fixes)

- Status: Proposed
- Date: 2026-11-21
- Affects (if accepted): `Scope.md` §4/§5/§8 rule 8, `features/share.md` SHARE-01/02, ADR-0010 default (Phase 1 only, option A), ADR-0020

## Context
Phase 1 has no growth mechanism:
- share cards are Phase 2;
- the only deep link is push → marker;
- `Scope.md` §8.8 forbids shipping Phase 2 features to fix Phase 1 metrics.

Gate 1 needs map density (first social touch ≥ 40% within 24 h), and density needs installs. That is a deadlock. Meanwhile the Followers default with no follow graph hides most content from everyone. The full plan is in `planning/product/Growth.md` §4–§5.

## Decision
1. **SHARE-01-lite moves into Phase 1:**
   - a rendered share card (coarse area only, never precise coordinates) through the OS share sheet;
   - App Links / Universal Links on an owned domain;
   - a web landing page with store badges;
   - Play Install Referrer attribution.
   - The full SHARE-01/02 remains Phase 2.
2. **Cold start:**
   - "Fart Map Official" landmark markers, clearly labeled, created by the owner through an admin script;
   - aggregated regional counts at low zoom;
   - a launch city or campus first.
3. **Notification permission** is asked after the first log instead of after the first reaction.
4. **Phase 1 default visibility (owner choice):**
   - **A (recommended):** default Public in Phase 1. The public projection is already fuzzed (ADR-0019) and precise location stays owner-only. Revert to Followers as the default when SOCIAL-03 ships.
   - **B:** keep Followers, plus a one-time choice card on the first log.

## Alternatives
- Keep the sequencing and rely on paid user acquisition to reach density: costly, and wasted without sharing.
- Fake activity (bots): rejected, it breaks trust and Product principles.

## Consequences
- **Cost:** about one epic of work (card rendering per platform, link handling, landing page).
- Option A is a privacy-default change. The owner must accept it explicitly, and the Privacy disclosures and onboarding copy must say "your exact spot stays private".
- New analytics: share created, link opened, install attributed.

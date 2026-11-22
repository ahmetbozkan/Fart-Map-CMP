# ADR-0023: Phase 1 Growth Kernel (Share Card, Deep Links, Cold-Start Fixes, Public Default)

- Status: Accepted (2026-11-22, owner), with **option A** for the default visibility
- Date: 2026-11-21
- Amends: ADR-0010 (default tier), ADR-0020 (notification-permission timing); `Scope.md` §4/§5/§8 rule 8; `features/share.md` SHARE-01/02

## Context
Phase 1 had no growth mechanism:
- share cards were Phase 2;
- the only deep link was push → marker;
- `Scope.md` §8.8 forbade shipping Phase 2 features to fix Phase 1 metrics.

Gate 1 needs map density (first social touch ≥ 40% within 24 h), and density needs installs. That is a deadlock. Meanwhile the Followers default with no follow graph hid most content from everyone. The full plan is in `planning/product/Growth.md` §4–§5.

## Decision (accepted)
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
3. **Notification permission** is asked once, after the first successful log, instead of after the first reaction.
4. **Phase 1 default visibility: option A.**
   - New farts default to **PUBLIC** in Phase 1. The public projection is already fuzzed (ADR-0019) and the precise location stays owner-only. After the first log, the user's last-used choice wins.
   - **Minors:** the 13–17 age band never defaults to PUBLIC. It defaults to FOLLOWERS until the owner defines minor-safe defaults (`Scope.md` §7a), because `ContentSafety.md` requires the most restrictive defaults for minors.
   - When SOCIAL-03 ships (Phase 2), the default for users without a last-used choice reverts to **FOLLOWERS**.
   - The Log Fart visibility selector always shows the current choice explicitly. Onboarding and the selector copy say "your exact spot stays private".
   - Rejected alternative: B (keep Followers, plus a one-time choice card on the first log).

## Alternatives
- Keep the sequencing and rely on paid user acquisition to reach density: costly, and wasted without sharing.
- Fake activity (bots): rejected, it breaks trust and the product principles.

## Consequences
- **Cost:** about one epic of work (card rendering per platform, link handling, landing page). It is added to `ROADMAP.md` as EPIC-01-10.
- **Privacy:** option A is a privacy-default change, explicitly accepted by the owner. Privacy disclosures and onboarding copy must state that the exact spot stays private.
- **New analytics:** share created, link opened, install attributed.
- **Applied (2026-11-22):**
  - product docs: `Scope.md`, `Growth.md`, `features/share.md`, `features/create.md`, `features/notifications.md`, `features/privacy.md`;
  - design: `HANDOFF-UXF-01-04-MVP.md`, `UXFlowCatalog.md`, `screens/LogFartScreen.md`;
  - architecture: `Backend.md` §6b, `Security.md`, `LocalData.md` §5;
  - `CLAUDE.md`, `.claude/rules/kotlin-shared.md`;
  - ADR-0010/0020 notes; `ROADMAP.md`.

# ADR-0024: Monetization v2 (Shop, Launch Catalog, Offers, Remote Config, Earlier Subscription)

- Status: Accepted (2026-11-22, owner), with rewarded-ad value **(a) single-use** and **no** Phase 1 scope trims
- Date: 2026-11-21
- Amends: ADR-0016 (subscription phase, offerings), ADR-0017 (cap tunable), ADR-0020 (subscription phase); `Monetization.md` §3–§7, `Scope.md` §4–§6, `features/monetization.md`, `Metrics.md`

## Context
The app's purpose is revenue, but the plan's monetization was weak at launch:
- purchases were reachable only from locked-item sheets;
- the catalog size and prices were undefined;
- there was no recurring revenue until Phase 4, which sat behind Gates 1–3;
- there were no pricing or experiment levers;
- Gate 1 checked monetization *integrity* but not *revenue*.

All decisions below stay inside the hard rules (`Monetization.md` §2): cosmetics only, no pay-to-win, no passive ads, no dark patterns.

## Decision (accepted)
1. **Shop surface in Phase 1:** a "Wardrobe" entry in Profile; browse and buy avatars and sounds. Never on the map or reaction surfaces.
2. **Launch catalog:**
   - at least 2 free + 8 paid avatars and 3 free + 9 paid sounds, plus 2 themed packs;
   - price tiers: single item low tier, pack mid tier;
   - final prices are set by the owner in RevenueCat Offerings, never hardcoded.
3. **Starter pack offer** shown once after the first successful log (never during onboarding), dismissible, no countdown.
4. **Remote Config** controls price-offering selection, the rewarded-ad cap (default 3), offer timing and copy variants. Every key has a safe in-app default. Values the server enforces (the ad cap) are read by the enforcing Cloud Function; the client copy is display-only.
5. **RevenueCat Offerings + Experiments** for price and paywall tests (`growth-experiment` skill).
6. **Subscription "Fart Map+"** moves from Phase 4 to **Phase 2**, gated on a catalog of 20 or more items instead of Gate 3. The content and lapse rules from `Monetization.md` §4 are unchanged.
7. **Rewarded-ad value: (a) single-use.** One ad unlocks one avatar or sound for one fart, then it re-locks. `Monetization.md` rule 6 is unchanged. Rejected: (b) 24-hour try, (c) 3 ads = own a common item.
8. **Gate 1 adds tracked revenue metrics** (payer conversion, ARPDAU) alongside integrity. They are observed, not pass/fail, in Phase 1.
9. **Phase 1 scope trims: none.** The four tabs, the locked voice UI and the full detailed log stay in Phase 1 (ADR-0020). Rejected: hiding Social/Rank, dropping the locked voice UI, quick-log-first.

## Alternatives
- Keep the previous plan: lowest risk to principles, weakest revenue and no learning loop.
- Interstitial or banner ads: rejected by the hard rules.

## Consequences
- **More Phase 1 work:** shop UI, Remote Config adapter, offer logic. Added to EPIC-01-06 in `ROADMAP.md`.
- **Phase 2:** the subscription work moves here.
- **Pricing** becomes an ongoing owner task through dashboards, not code.
- **Applied (2026-11-22):**
  - product docs: `Monetization.md`, `Scope.md`, `features/monetization.md`, `features/profile.md`, `Metrics.md`, `Growth.md`;
  - architecture: `Architecture.md` §5 (Remote Config), `LocalData.md` §5;
  - design: `screens/CosmeticUnlockSheet.md`;
  - `.claude/skills/monetization` and `growth-experiment`;
  - ADR-0016/0017/0020 notes; `ROADMAP.md`.

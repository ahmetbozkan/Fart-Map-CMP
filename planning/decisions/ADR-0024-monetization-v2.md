# ADR-0024: Monetization v2 (Shop, Launch Catalog, Offers, Remote Config, Earlier Subscription)

- Status: Proposed
- Date: 2026-11-21
- Affects (if accepted): `Monetization.md` §3–§7, `Scope.md` §4–§6, `features/monetization.md`, ADR-0016/0017/0020 consequences, `Metrics.md`

## Context
The app's purpose is revenue, but the plan's monetization is weak at launch:
- purchases are reachable only from locked-item sheets;
- the catalog size and prices are undefined;
- one rewarded ad buys one single-use cosmetic (low perceived value, low ad revenue per user);
- there is no recurring revenue until Phase 4, which sits behind Gates 1–3 and may never arrive;
- there are no pricing or experiment levers;
- Gate 1 checks monetization *integrity* but not *revenue*.

All proposals below stay inside the hard rules (`Monetization.md` §2): cosmetics only, no pay-to-win, no passive ads, no dark patterns.

## Decision (proposed)
1. **Shop surface in Phase 1:** a "Wardrobe" entry in Profile; browse and buy avatars and sounds. Never on the map or reaction surfaces.
2. **Launch catalog:**
   - at least 2 free + 8 paid avatars and 3 free + 9 paid sounds, plus 2 themed packs;
   - price tiers: single item low tier, pack mid tier;
   - final prices are set by the owner in RevenueCat Offerings, never hardcoded.
3. **Starter pack offer** shown once after the first successful log (never during onboarding), dismissible, no countdown.
4. **Remote Config** controls price-offering selection, the rewarded-ad cap (default 3), offer timing and copy variants. Every key has a safe in-app default.
5. **RevenueCat Offerings + Experiments** for price and paywall tests (`growth-experiment` skill).
6. **Subscription "Fart Map+"** moves from Phase 4 to **Phase 2**, gated on a catalog of 20 or more items instead of Gate 3. The content and lapse rules from `Monetization.md` §4 are unchanged.
7. **Rewarded-ad value (owner picks one):**
   - (a) keep single-use;
   - (b) a 24-hour try of the item;
   - (c) watch 3 ads to own one common-tier item permanently.
   - (b) and (c) change Monetization rule 6, so the owner must choose explicitly.
8. **Gate 1 adds tracked revenue metrics** (payer conversion, ARPDAU) alongside integrity. They are observed, not pass/fail, in Phase 1.
9. **Phase 1 scope trims** to fund the above (owner picks any):
   - hide the thin Social and Rank tabs until Phase 2;
   - drop the locked voice-recording UI until it is sellable;
   - ship the quick log first and the detailed log steps incrementally.

## Alternatives
- Keep the current plan: lowest risk to principles, weakest revenue and no learning loop.
- Interstitial or banner ads: rejected by the hard rules.

## Consequences
- More Phase 1 work: shop UI, Remote Config adapter, offer logic.
- Earlier subscription work in Phase 2.
- Pricing becomes an ongoing owner task through dashboards, not code.

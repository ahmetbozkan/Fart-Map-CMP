---
name: monetization
description: Implement cosmetic purchases (RevenueCat), rewarded-ad unlocks (AdMob with server-side verification), entitlement checks, the unlock sheet and paywall surfaces without ever trusting the client. Use for anything touching purchases, prices, ads, entitlements, locked items, or monetization copy.
---

# Monetization and entitlements

Rules: `planning/product/Monetization.md` (hard rules §2, surface map §6), `planning/product/features/create.md` CREATE-03.2, ADR-0016/0017. Monetization v2: ADR-0024 (accepted 2026-11-22).

## Hard rules (stop and ask if a change would break one)
- Cosmetics only. Logging, the map, reacting and safety tools are always free. No XP, rank or achievements for sale.
- Rewarded ads:
  - opt-in;
  - one ad = one single-use unlock of one avatar or sound;
  - at most 3 per day, enforced server-side;
  - never voice recording or marker themes.
- No banners or interstitials, no dark patterns, no purchase prompt during onboarding, the map, reactions, comments or privacy screens.
- Locked items stay visible. A tap opens the unlock sheet; it never auto-opens a paywall. Phase 1 sheet: **Buy** + **Watch ad to try once**; after the cap, Buy only. "Go Premium" appears once MON-02 ships (Phase 2, ADR-0024).
- Copy is humorous and on-voice ("The Thunderclap Collection", never "Upgrade for a better experience").

## Phase 1 surfaces (ADR-0024)
- **Wardrobe** shop entry in Profile (browse + buy avatars, sounds, packs). Never on the map or reaction surfaces.
- **Starter pack** offer once after the first successful log (`starter_pack_offer_shown` in DataStore); dismissible, no countdown, never in onboarding.
- **Remote Config** keys with safe defaults: offering id, `rewarded_daily_cap` (default 3; the enforcing function reads the server-side value), offer timing, copy variant.

## Purchase flow
1. Catalog from `cosmetic_items` (Firestore); prices from the RevenueCat offering via `PurchasesClient`. Never hardcode prices.
2. `PurchasesClient.purchase(productId)` → on success show "Unlocking…" and **wait for the mirrored `user_entitlements/{uid}` doc** before showing the item as owned.
3. Restore purchases is always reachable (Profile/Settings). RevenueCat app user id = Firebase uid, anonymous included.
4. Store success but no mirror within the timeout: show a "restoring" state, trigger restore, never unlock locally.

## Rewarded ad flow
1. The UMP consent flow runs before the first request (ATT on iOS only in that context). Request with location disabled; use test ad units in `dev`.
2. `RewardedAdProvider.show(itemId)` → the AdMob server-side-verification callback hits a function, which issues a token in `rewarded_unlocks/{uid}/tokens/{id}` → the client observes the token → the item is selectable for this fart.
3. The token is consumed server-side when the fart using the item is written. A failed or dismissed ad grants nothing, with no error beyond the sheet closing.

## Telemetry
Emit the events in `planning/architecture/Observability.md` §7/§11 (offering shown, purchase started/succeeded/failed/cancelled, ad requested/completed, token issued/consumed, cap reached). No receipts or PII.

## Validation
- [ ] Rules tests: a non-free item is rejected without an entitlement or token; a token works once; the cap is enforced.
- [ ] The client never writes `user_entitlements` or tokens.
- [ ] No location in ad requests; UMP runs first.
- [ ] Copy reviewed for voice; no forbidden surfaces.

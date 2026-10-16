# Skill: Monetization and Entitlements

## Purpose
Implement cosmetic purchases, rewarded-ad unlocks and entitlement checks so that no unlock can be granted by the client.

## Sources
- `planning/product/Monetization.md` (hard rules, locked-item UX, caps)
- `planning/product/Features.md` CREATE-03.2
- ADR-0016 (RevenueCat), ADR-0017 (AdMob + server-side verification), ADR-0015 (Cloud Functions)
- `planning/architecture/DataContracts.md` §3.13–3.15, §5.7

## Procedure
1. Confirm the item is cosmetic and which paths apply (free / purchase / rewarded ad / premium from Phase 4).
2. Catalog comes from `cosmetic_items`; prices come from the store offering via the purchases adapter, never hardcoded.
3. Purchase: call the `PurchasesClient` contract; on success wait for the mirrored `user_entitlements` document before showing the item as owned.
4. Rewarded ad: `RewardedAdProvider` shows the ad; the unlock token appears in `rewarded_unlocks` only after the SSV callback function writes it; consume by submitting the fart with the item.
5. Render locked/unlocked state from the entitlement and token caches; treat them as read-only mirrors.
6. Emit the monetization telemetry events (`Observability.md` §11).

## Failure recovery
- Purchase succeeded in the store but no entitlement mirrored: show "restoring" state, trigger restore purchases, never unlock locally.
- Ad completed but no token: show the sheet again with a retry; never grant locally.

## Escalation triggers
- Any request to grant, extend or cache an unlock client-side.
- Any new monetization surface, price change or ad format not in `Monetization.md` §6.

## Validation
- [ ] Rules tests: non-free item rejected without entitlement or token; token single use
- [ ] Daily rewarded cap enforced server-side
- [ ] No location passed to ad requests; UMP consent ran first
- [ ] Copy is humor-consistent (no "Upgrade for a better experience")

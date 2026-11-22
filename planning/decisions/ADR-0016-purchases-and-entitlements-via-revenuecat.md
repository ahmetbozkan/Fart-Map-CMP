# ADR-0016: Purchases and Entitlements via RevenueCat (Kotlin Multiplatform SDK)

- Status: Accepted; amended by ADR-0024 (2026-11-22): subscription moves to Phase 2, prices are selected through RevenueCat Offerings/Experiments, and a shop surface plus starter pack ship in Phase 1.
- Date: 2026-10-14
- Source: owner decision (closeout question C3: a la carte purchases from Phase 1) + architecture recommendation

## Context
The owner moved permanent a la carte cosmetic purchases (avatars, sounds) into Phase 1. `Monetization.md` §4 requires Apple In-App Purchase on iOS and Google Play Billing on Android, and `Security.md` §5 requires entitlements to be verified against a store-backed source of truth, never trusted from the client. Implementing StoreKit 2 and Play Billing separately plus receipt validation functions is a large surface for one developer.

## Decision
- Use the **RevenueCat Kotlin Multiplatform SDK (`purchases-kmp`)** as the single purchases layer over StoreKit 2 and Google Play Billing.
- RevenueCat is the **entitlement source of truth**. A Cloud Function receives RevenueCat webhooks and mirrors entitlements into `user_entitlements/{userId}` (backend-written only). The client caches entitlements for UX; Firestore rules and functions enforce ownership on content writes.
- RevenueCat app user id = Firebase uid, including anonymous uids (ADR-0018). Purchases on a lost anonymous uid are recovered through the store's "restore purchases".
- Product catalog: cosmetic items live in Firestore (`cosmetic_items`), each carrying its store product ids; prices are displayed from the store offering, never hardcoded.
- The subscription (MON-02) stays Phase 4 and will use the same SDK. (Moved to Phase 2 by ADR-0024.)

## Alternatives considered
- Native billing via `expect`/`actual` plus own receipt validation functions: full control, roughly three times the work and the riskiest code in the app for a solo developer.

## Consequences
- `Monetization.md`, `features/` CREATE-03.2, `DataContracts.md`, `DataModel.md`, `TechStack.md`, `Architecture.md` updated.
- The RevenueCat dashboard, App Store Connect and Play Console product setup are owner-operated external consoles.
- Purchases are online-only operations; they are never queued offline (ADR-0022).

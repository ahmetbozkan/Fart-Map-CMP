# Monetization — Fart Map

> **Authority level:** This document expands MON-01, MON-02, and MON-03 from `features/` with authoritative monetization rules, product boundaries, SKU definitions, pricing philosophy, and implementation constraints. When this document and `features/` conflict, this document wins for monetization-specific behavior. Always read `features/` first for pillar scores and phase assignments.

**Related documents:**
- `features/` Monetization Systems (MON-01–03)
- `CREATE-03.2` in `features/` — Log Fart monetization binding rules
- `SocialSystems.md` — no direct monetization, but reaction packs are future MON-01
- `ProgressionSystems.md` — cosmetic unlock tracks tied to progression
- `ContentSafety.md` — monetization must not weaken safety controls
- `Product.md` — humor-first, no pay-to-win, fairness as trust

---

## Contents

1. [Monetization Philosophy](#1-monetization-philosophy)
2. [Hard Rules — What Monetization Can Never Do](#2-hard-rules--what-monetization-can-never-do)
3. [MON-01 Cosmetic Economy](#3-mon-01-cosmetic-economy)
4. [MON-02 Premium Subscription](#4-mon-02-premium-subscription)
5. [MON-03 Rewarded Ads](#5-mon-03-rewarded-ads)
6. [Monetization Surface Map](#6-monetization-surface-map)
7. [Pricing Philosophy](#7-pricing-philosophy)
8. [Anti-Patterns to Avoid](#8-anti-patterns-to-avoid)
9. [Implementation Validation Checklist](#9-implementation-validation-checklist)

---

> **Phase assignment update (2026-10-14, ADR-0020):** MON-01 a la carte purchases of avatars and sounds and MON-03 rewarded ads ship in Phase 1; MON-02 subscription remains Phase 4. **Updated 2026-11-22 (ADR-0024):** subscription moves to Phase 2 (catalog ≥ 20 items); Phase 1 adds the Wardrobe shop, a defined launch catalog, a starter pack after the first log, Remote Config levers and RevenueCat Offerings/Experiments. Rewarded ads stay single-use. Purchases run through RevenueCat (ADR-0016); rewarded ads through AdMob with server-side verification (ADR-0017).

## 1) Monetization Philosophy

Fart Map monetizes **self-expression and identity depth**, not access to the core product. Every person who downloads the app must be able to:

- Log a fart (full functionality)
- See the map (full functionality)
- React to and engage with public content (full functionality)
- Build a social graph (full functionality)
- Earn achievements and XP (full functionality)

What premium users get is a **richer identity palette** — more avatars, more sounds, voice recording, more profile customization options. They do not get a better product experience in any functional sense. They get a more expressive one.

**The test:** If removing a feature from the free tier makes the core loop worse for the free user (not just less expressive), that feature must not be gated. If removing it only makes the free user's identity feel less customized, gating is acceptable.

---

## 2) Hard Rules — What Monetization Can Never Do

These are product-level constraints. No business case overrides them. If any of these are violated, it is a product integrity failure that must be rolled back.

| # | Rule | Rationale |
|---|---|---|
| 1 | Never gate content creation | Anyone at any tier can log a fart. Flavor, Intensity, Description, Location, Visibility — all free. |
| 2 | Never sell XP, levels, or rank | Progression reflects activity, not wallet size. XP boosts, purchased levels, or bought rank are permanently off the table. |
| 3 | Never sell achievements or badges | All achievements are earned through behavior. Premium may offer display frames, never the achievement itself. |
| 4 | Never gate map viewing | The map is the product. Free users see the full map with full resolution and all public markers. |
| 5 | Never gate social reactions | Free users can react to public content. Reaction packs (cosmetic variety) may be premium; the ability to react is always free. |
| 6 | Rewarded ads must not permanently equal premium | After a single-use ad unlock is consumed, the item must re-lock. Ads are a taste mechanism, not a free premium path. |
| 7 | No dark patterns | No countdown timers on unrelated purchase prompts, no fake scarcity, no shaming copy for free users, no deceptive UI that tricks users into purchasing. |
| 8 | No ads without explicit user action | The only acceptable ad format is opt-in rewarded ads. Banner ads, interstitial ads, and passive ad placements are not acceptable in MVP. |
| 9 | Safety controls are never paywalled | No feature that protects user safety (block, report, visibility controls, content moderation) can be gated behind any payment tier. |
| 10 | Humor is never flattened for monetization | Monetization copy must match the product's tone. "Your Stinky Starter Pack" is acceptable. "Upgrade to Premium for a Better Experience" is not. |

---

## 3) MON-01 Cosmetic Economy

### What it is

A catalog of purchasable cosmetic items that expand the user's identity and humor expression palette. Items are sold individually (a la carte) or as themed packs.

### Item categories

| Category | Description | Surfaces |
|---|---|---|
| **Avatars** | Character skins for the user's fart identity | Log Fart screen, map markers, feed, profile |
| **Sounds** | Fart sound clips played on marker tap | Log Fart screen, map playback |
| **Marker themes** | Visual style variants for the map marker (shape, animation, glow) | Map markers only |
| **Profile frames** | Border/background treatment on the user's profile photo | Profile (PROFILE-02) |
| **Signature packs** | Icon/color/effect combinations for signature (PROFILE-03) | Profile, map markers |
| **Achievement display frames** | Visual frame applied to badges in the showcase (ACH-02) | Profile showcase |
| **Reaction packs** (Phase 2+) | Animated or seasonal variants of the standard reaction set | Feed, fart detail view |

### Free tier guarantees

The following must always be free and must never be moved to a paid tier once established:

| Item | Minimum free allotment |
|---|---|
| Avatars | 2 permanently free |
| Sounds | ≥3 permanently free |
| Marker theme | 1 default always free |
| Reactions | All 5 MVP reactions always free |
| Profile frame | 1 default always free |

### Item acquisition paths

| Path | Available for |
|---|---|
| Direct purchase (a la carte, from Phase 1 for avatars and sounds; ADR-0016) | Any cosmetic item |
| Pack purchase (themed bundle) | Group of related items at a discount |
| Premium subscription unlock | Full library access while subscription is active |
| Rewarded ad (single-use one action only) | Avatars, sounds (not voice recording, not marker themes) |
| Earned via achievement or challenge | Special limited items — cosmetic only, never resellable |

Items earned via achievement or challenge must be permanently owned by the user. They cannot expire.

### Entitlement source of truth

RevenueCat holds purchase and entitlement state (ADR-0016). A Cloud Function mirrors it into `user_entitlements/{userId}`; Firestore rules and functions check that mirror before accepting content that uses a non-free item. The client caches entitlements for UX only.

### Cosmetic item lifecycle

- Once a user purchases an item (a la carte or pack), they own it permanently.
- Items unlocked via rewarded ads are single-use and re-lock after one action.
- Items unlocked via premium subscription are accessible while the subscription is active. If the subscription lapses, items re-lock (purchased items remain owned; subscription-exclusive items re-lock).
- Items earned via achievements are permanently owned and survive subscription lapse.

### Locked item UX (universal rules)

These rules apply to every locked cosmetic item across every surface. They are not surface-specific.

1. Locked items are always visible in the selector/catalog. They are never hidden.
2. Tapping a locked item shows a bottom sheet with the appropriate unlock options (see CREATE-03.2 for Log Fart-specific rules).
3. The bottom sheet must clearly label the item name, a short humorous description, and the price/unlock method.
4. No locked item triggers an automatic paywall. The paywall is always user-initiated.
5. (From MON-02.) If the user has an active premium subscription, all items accessible via subscription show no locked state. No "Go Premium" CTA is shown for items the user already has access to.

---

## 4) MON-02 Premium Subscription

### Phase assignment

Phase 2 (ADR-0024), once the catalog has 20 or more items. **Do not ship this before the cosmetic economy (MON-01) is established and meaningful.** Voice recording (CREATE-08) joins the subscription when it ships (Phase 4). The subscription has nothing to sell if there is no catalog depth.

### What premium includes

| Benefit | Notes |
|---|---|
| Full avatar library access | All current and future avatars while subscribed |
| Full sound library access | All current and future sounds while subscribed |
| Voice recording (CREATE-08) | Record and attach voice clips to farts |
| Full marker theme library | All marker styles |
| Full signature library (PROFILE-03) | All signature styles |
| Profile frame library | All profile frame options |
| Premium profile badge | Visual indicator on profile that user is premium — must be tasteful, not aggressive |
| Achievement showcase extra slots (TBD) | Possible Phase 4+ benefit |
| Early access to seasonal drops | Cosmetic items available to subscribers before general sale |

### What premium never includes

- XP boost
- Rank advantage
- Leaderboard bonus
- Faster challenge completion
- Additional fart logs per day (the daily soft cap applies equally to all users)
- Any feature that affects the core loop or social visibility

### Subscription model

| Option | Cadence | Notes |
|---|---|---|
| Monthly | Recurring monthly | Cancel anytime |
| Annual | Recurring annually | Discount vs monthly; exact discount TBD |

Price points are not defined in this planning document — they must be set by product/business after market research and competitive analysis.

### Subscription lapse behavior

- User is notified 3 days before renewal failure (grace period communication).
- On lapse: subscription-only items immediately re-lock. Purchased items (a la carte) remain owned.
- Profile premium badge is removed.
- Voice recording ability is removed. Existing voice clips attached to past farts remain audible — they are not deleted.
- The user's account, all content, all social connections, all achievements, and all XP are entirely unaffected by subscription lapse.

### Platform payments

- iOS: Apple In-App Purchase only, via RevenueCat (ADR-0016). No side-loading payment. This is an Apple App Store requirement.
- Android: Google Play Billing only, via RevenueCat (ADR-0016).
- No web payment surface in MVP. Web monetization is Phase 4+.

---

## 5) MON-03 Rewarded Ads

### Phase assignment

Phase 1 for avatars and sounds (ADR-0020). **The catalog must be meaningful enough that the "taste" metaphor works:** the Phase 1 catalog ships with purchasable items beyond the free set. Rewarding an ad to try an avatar nobody cares about creates no value and erodes trust.

### Format constraint

**Only opt-in rewarded ads.** The user always initiates the ad by choosing to watch it in exchange for a specific unlock. No passive ad placement anywhere in the product.

Acceptable trigger flow:
```
User taps locked item → Bottom sheet → "Watch Ad to Try Once" → Ad plays → Item unlocked for one action
```

Unacceptable:
- Banner ad visible on the map
- Interstitial ad between sessions or actions
- Auto-playing ad after a fart is logged
- Any ad shown without explicit user-initiated opt-in

### What can be unlocked via rewarded ad

| Item type | Rewarded ad unlock? | Duration |
|---|---|---|
| Avatars | Yes | One action |
| Sounds | Yes | One action |
| Voice recording | **No** | Premium only — never ad-unlockable |
| Marker themes | No — too close to functional | Premium or purchase only |
| Profile frames | TBD | Evaluate in Phase 4 |
| Achievement frames | No | Purchase only |

### Rewarded ad frequency limits

To prevent rewarded ads from becoming a de facto free premium path:
- Maximum 3 rewarded ad views per user per day (across all item types). The value is a Remote Config key (ADR-0024) read by the enforcing Cloud Function; the client value is display-only.
- After the daily limit is reached, the "Watch Ad" option is hidden. The "Buy" option remains (Phase 1); "Go Premium" is added once MON-02 ships (Phase 2, ADR-0024).
- Exact limit may be adjusted later based on ad revenue data, but must never be set high enough that the ad path meaningfully competes with premium conversion.

### Ad SDK and provider

**Decided: Google AdMob rewarded format with server-side verification (ADR-0017).** Requirements the integration must keep satisfying:
- Must support rewarded ad format with completion callback.
- Must not inject passive ad inventory automatically.
- Must comply with COPPA, GDPR, and App Store / Play Store ad policies.
- Must not share user location data with the ad network without explicit consent (PRIV-01, PRIV-02 compliance).

---

## 6) Monetization Surface Map

This table maps every in-app surface where a monetization interaction can occur. Surfaces not listed here must not present any purchase prompt.

| Surface | Monetization interaction | Type |
|---|---|---|
| Log Fart — Avatar selector | Locked avatar → bottom sheet | MON-01 / MON-03 |
| Log Fart — Sound picker | Locked sound → bottom sheet | MON-01 / MON-03 |
| Log Fart — Voice recording | Locked recording → bottom sheet | MON-02 |
| Map — Marker tap | No purchase prompt. Sound plays if selected. | None |
| Feed | No purchase prompt. | None |
| Profile — Avatar section | Locked avatar → bottom sheet | MON-01 / MON-03 |
| Profile — Signature section (PROFILE-03) | Locked signature → bottom sheet | MON-01 |
| Profile — Achievement showcase (ACH-02) | Locked frame → bottom sheet | MON-01 |
| Settings — Premium (from MON-02 phase) | Full subscription paywall | MON-02 |
| Cosmetic catalog / shop (Profile → Wardrobe, Phase 1) | Browse and purchase | MON-01 / MON-02 |
| After first successful log (once) | Starter pack offer; dismissible, no countdown, never during onboarding (ADR-0024) | MON-01 |
| Achievement unlock celebration | "Add to Showcase" only; no purchase | None |
| Level-up celebration | No purchase prompt | None |
| Leaderboard | No purchase prompt | None |

**Non-negotiable absent surfaces:**
- Map view: zero purchase prompts
- Reactions: zero purchase prompts (reaction packs may exist in catalog, but never prompted inline)
- Comment thread: zero purchase prompts
- Moderation / privacy settings: zero purchase prompts

---

## 7) Pricing Philosophy

Exact prices are not defined here — they must be set by product/business. The following principles govern pricing decisions:

1. **Humor-aligned pricing labels.** A sound pack should be called "The Thunderclap Collection", not "Sound Pack — 4 items". Naming is part of the product.
2. **Free users feel respected, not punished.** The free tier must feel complete, not crippled. Free users are potential future payers; shaming them or making the product feel broken without payment destroys conversion.
3. **Annual subscription must be clearly worth it.** If the annual saving vs monthly is trivial, users won't upgrade. A minimum 20% saving is a reasonable expectation.
4. **a la carte must not undercut subscription.** If individual item purchases sum to a lower cost than subscription, the subscription value proposition collapses. Price individual items accordingly.
5. **Price parity between iOS and Android where legally required.** Some markets require this. Confirm legal requirements before setting price points.
6. **No dark pricing.** No price anchoring with fake crossed-out prices. No countdown timers on discounts that reset. No "limited time" offers that are always available.

---

## 8) Anti-Patterns to Avoid

These are known monetization anti-patterns that are explicitly prohibited in Fart Map. If any of these are identified in a design review, they must be rejected.

| Anti-pattern | Why it's prohibited |
|---|---|
| Energy system / wait timers | The map is always available. No cool-downs on logging. |
| Gacha / loot boxes | Randomized purchases are gambling-adjacent and build distrust. |
| Pay-to-skip social grinding | Social graph and progression are earned. No purchased follower count, no purchased head start. |
| Mandatory onboarding upsell | The first purchase prompt a user sees must not be during onboarding. Users must experience the product before being asked to pay. |
| Loss aversion manipulation | Streak freezes sold with "You're about to lose your 30-day streak!" framing are manipulative. If streak freeze is ever introduced, it must be earned or universally available. |
| Subscription that paywalls existing functionality | If a feature is free today, it cannot be retroactively moved behind a subscription without an explicit user communication and opt-out path. |
| Whale optimization at expense of free users | Designing content and features exclusively for the highest-paying users while degrading the free experience is not acceptable. |
| Ad interstitials disguised as transitions | Any loading screen, level-up animation, or screen transition that plays an ad is classified as an interstitial and is prohibited. |

---

## 9) Implementation Validation Checklist

**MON-01 Cosmetic Economy:**
- [ ] All locked items are visible in selectors/catalog — none are hidden
- [ ] Tapping any locked item shows a bottom sheet — never an automatic redirect to paywall
- [ ] Free tier minimums enforced: 2 free avatars, ≥3 free sounds, 5 free reactions, 1 free profile frame
- [ ] Purchased items (a la carte) persist permanently regardless of subscription status
- [ ] Achievement-earned items persist permanently and never expire
- [ ] No purchase prompt on map view, feed, comment thread, or moderation/privacy surfaces

**MON-02 Premium Subscription:**
- [ ] Voice recording is premium-only; no rewarded ad unlock path exists at the architecture level
- [ ] Premium badge on profile is visual only; confers no functional advantage
- [ ] Subscription lapse re-locks subscription-only items but does not delete any content or social connections
- [ ] Existing voice clips on past farts remain audible after subscription lapse
- [ ] iOS uses Apple IAP only; Android uses Google Play Billing only

**MON-03 Rewarded Ads:**
- [ ] Rewarded ads are opt-in only; no passive ad placement exists anywhere in the product
- [ ] Rewarded ad unlock re-locks immediately after one action is consumed
- [ ] Daily rewarded ad cap enforced server-side
- [ ] After daily cap: "Watch Ad" option is hidden; "Buy" remains ("Go Premium" only once MON-02 exists)
- [ ] Ad SDK does not inject passive inventory; reviewed before integration

**Entitlements and infrastructure (ADR-0016, ADR-0017):**
- [ ] Entitlement checks for non-free cosmetics are enforced server-side (rules + functions) against the mirrored RevenueCat state
- [ ] Rewarded unlock tokens are issued only by the server-side verification callback and consumed server-side
- [ ] Daily rewarded-ad cap (3) enforced server-side
- [ ] No ad request includes user location; UMP consent runs before the first ad

**Hard rules:**
- [ ] No XP boost purchasable at any price point — architecture must make this impossible
- [ ] No rank or leaderboard advantage purchasable — architecture must make this impossible
- [ ] No achievement purchasable — no SKU exists for badge unlock
- [ ] No paywall on map viewing, logging, reacting, or social graph features
- [ ] No paywall on any safety, privacy, or moderation feature
- [ ] Monetization copy is humorous and product-voice-consistent — reviewed before shipping

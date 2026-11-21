# Growth, Store and Launch: Fart Map

> **Status: Proposed (2026-11-21).** Nothing here changes scope, defaults or monetization until the owner accepts the matching ADR:
> - ADR-0023: growth kernel;
> - ADR-0024: monetization v2.
>
> Store-policy items (§2) are compliance requirements, not proposals; the `release` skill enforces them. The figures below are planning assumptions to validate, not market data. Use the `growth-analyst` agent to research them with sources.

## 1) Why this document exists

The product docs optimize retention and fairness well but never set a revenue goal. Phase 1 had no growth feature:
- share cards were Phase 2;
- there was no deep link beyond push;
- there was no ASO or store plan;
- the cold-start problem was named many times but never solved.

A novelty app lives or dies on its first weeks of organic installs. This document covers that gap.

## 2) Store risk and compliance (must-do before launch)

| Risk | Detail | Mitigation |
|---|---|---|
| **App Store 4.3 (spam / saturated categories)** | Apple's guideline names fart apps as a saturated category it rejects unless the app is a unique, high-quality experience | Position as a *social map game*. The listing, screenshots and review notes lead with the map, reactions, profiles and moderation; sounds are secondary. Ship polished: no placeholder UI or empty map at review time (§4). Explain the anonymous start in the review notes |
| **App Store 1.2 (UGC)** | UGC apps must filter objectionable content, offer report and block, act on reports, and publish contact info | Phase 1 already has report/block and auto-hide at 3. Add terms-of-use acceptance in onboarding, a published support contact, and a banned-term filter on descriptions |
| **Account deletion** | Apple 5.1.1(v): in-app. Google Play: in-app plus a web URL | PRIV-03 baseline plus a simple web form or page on the app's domain |
| **Sign in with Apple** | Required when Google sign-in is offered | Already planned (ADR-0018) |
| **Age rating** | Crude humor plus UGC | 12+ (Apple) / Teen (Google); 13+ age band enforced |
| **Privacy disclosures** | App Privacy labels, Play Data Safety, ATT prompt, UMP consent | Generated from the actual data map (`DataContracts.md`) at release time |
| **Location permission** | Foreground only; purpose string must explain the map | Ask at first map use, never at launch |
| **Name / brand** | "Fart" in the name can trip filters in some markets and ad networks | Keep it (it's the hook), but test the AdMob and RevenueCat app review early with test builds |

## 3) ASO (store discoverability)

- **Positioning line:** "The world's funniest social map: drop your mark, see what's happening around you, and react with friends."
- **Keyword themes** (validate volume with research):
  - head terms: fart map, fart, prank app, funny app;
  - feature terms: map with friends, funny sounds, social map;
  - long tail: localized equivalents.
- Search demand for "fart sounds" style apps is high, which makes it a traffic magnet. Use sounds as a secondary keyword, never as the headline (4.3).
- **Screenshot storyboard:**
  1. living map;
  2. one-tap log;
  3. friends reacting;
  4. avatars and sounds;
  5. "your exact spot stays private";
  6. rank teaser.
- **Preview video:** 15–20 s of the log → marker drop → reaction loop.
- **Ratings:** in-app review prompt (StoreKit / Play In-App Review) after a positive moment (the first reaction received, or the third log), never after an error, at most once per 90 days.
- **Localization order:** EN → ES → PT-BR → DE → FR → TR. The listing comes first; UI strings follow once the copy stabilizes.

## 4) Cold start: the empty-map problem

**The problem:** a map with no markers kills the L1/L3 loops. In Phase 1, the Followers default plus no follow graph means most farts are invisible to everyone else, so the Everyone filter and the Social tab stay empty and Gate 1's "first social touch ≥ 40% in 24 h" cannot be met.

**Proposed fixes (ADR-0023):**
1. **Default visibility for Phase 1.** Choose one (owner):
   - (a) default **Public** in Phase 1, since the public projection is already fuzzed to geohash-6 (ADR-0019) and precise location stays owner-only;
   - (b) keep **Followers**, but on the first log show a one-time choice card ("Let the world smell it?" Public / Followers / Private).
2. **Honest seeding.** "Fart Map Official" landmark markers (famous places, campuses, stadiums), clearly labeled as official, with jokes, created by the owner through an admin script. Never fake users.
3. **Aggregated activity.** At low zoom, show counts per region ("312 farts in Berlin this week") from public farts. Cheap, honest, and the map always looks alive.
4. **Launch geography.** Seed and market in one city or campus first to reach visible density, then expand.
5. **Push timing.** Ask for notification permission after the user's **first log** ("Want to know when someone reacts?"), not after the first reaction. Without this, most users who never receive a reaction never get a push channel.

## 5) Viral loop (SHARE-01-lite in Phase 1, ADR-0023)

- **Share card:** a rendered image of the user's marker on a stylized map snippet, coarse area name ("near Kreuzberg"), avatar, flavor and a funny caption, plus a QR code or short link. Never precise coordinates.
- **Delivery:** the OS share sheet (Instagram Stories, TikTok, WhatsApp, iMessage). Entry points: the success state after logging and the detail sheet of your own fart.
- **Links:**
  - App Links (Android) and Universal Links (iOS) on an owned domain;
  - a lightweight web landing page shows the card and store badges;
  - Firebase Dynamic Links is shut down, so don't use it;
  - install attribution through Play Install Referrer; on iOS rely on the landing page plus store campaign links.
- **Content angle:** short-form video of the log → marker → reaction loop and seeded official markers at landmarks. The owner can run a lightweight TikTok/Reels account from launch week.

## 6) Monetization levers (ADR-0024)

See ADR-0024 for the decision options. In summary:
- a shop surface;
- a defined launch catalog with price tiers;
- a starter pack after the first log (never in onboarding);
- Remote Config for prices, caps and offers;
- RevenueCat Offerings and Experiments;
- a subscription ("Fart Map+") in Phase 2 once the catalog has 20 or more items;
- a better rewarded-ad value (owner picks);
- revenue as a tracked Gate 1 metric.

All of it stays inside the hard rules in `Monetization.md` §2.

## 7) Measurement

- Funnels, defined as events via the `analytics-event` skill:
  - install → first map view → first log → first share → first reaction received;
  - unlock sheet shown → buy / ad started → unlocked;
  - share created → link opened → install (where attributable).
- **Weekly owner dashboard:** installs by source, D1/D7 retention, first-session loop completion, share rate per logger, ARPDAU (IAP + ads), payer conversion, crash-free users.

## 8) Launch checklist (proposed epic "Launch readiness")

- [ ] Store listings + screenshots + preview video (`store-listing` skill)
- [ ] Compliance items in §2 (`release` skill)
- [ ] Share card + links + landing page (if ADR-0023 is accepted)
- [ ] Official seed markers in the launch city or campus
- [ ] Analytics funnels verified in DebugView
- [ ] Closed test with 20 or more testers for 14 days (Google Play requires this for new personal developer accounts; check the current rule)
- [ ] Support contact, terms, privacy policy and deletion page on the domain

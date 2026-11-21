# ADR-0017: Rewarded Ads via Google AdMob with Server-Side Verification

- Status: Accepted
- Date: 2026-10-14
- Source: owner decision (closeout question C3: single-use rewarded-ad unlocks from Phase 1) + architecture recommendation

## Context
`Monetization.md` §5 deferred the ad SDK choice to a Phase 4 ADR. The owner moved rewarded-ad unlocks (single use, one action) for avatars and sounds into Phase 1. Hard rules: opt-in rewarded format only, no passive placements, unlock re-locks after one action, maximum three rewarded views per user per day enforced server-side, no user location shared with the ad network.

## Decision
- Use the **Google Mobile Ads SDK (AdMob) rewarded format** on both platforms behind a shared `RewardedAdProvider` `expect`/`actual` contract (ADR-0008). No other ad format is integrated.
- **Server-side verification (SSV):** AdMob's SSV callback hits a Cloud Function that issues a single-use unlock token in `rewarded_unlocks/{userId}/tokens/{tokenId}` naming the item. The client never grants itself an unlock.
- The token is **consumed server-side** when the fart that uses the item is written (function trigger validates and deletes the token); the daily cap of three views is enforced in the same backend path.
- Consent: the Google User Messaging Platform (UMP) flow runs before the first ad request; iOS App Tracking Transparency is requested only in that context. Ad requests are made with location disabled.
- Dev builds use AdMob test ad units; `app-ads.txt` is published with the app's web presence.

## Consequences
- `Monetization.md` §5, `features/` CREATE-03.2, `DataContracts.md`, `TechStack.md`, `Architecture.md` updated.
- AdMob account, app ids and ad units are owner-operated external consoles.
- Ads are online-only; a failed or dismissed ad grants nothing and shows no error beyond the sheet closing.

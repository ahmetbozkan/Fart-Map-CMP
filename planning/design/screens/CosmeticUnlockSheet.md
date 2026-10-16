# Cosmetic Unlock Sheet (Phase 1)

## Ownership
- Status: active
- Owner concern: design/screen behavior
- Upstream dependencies: `planning/product/Features.md` CREATE-03.2, `planning/product/Monetization.md` §3/§5/§6, ADR-0016, ADR-0017, ADR-0020, `planning/design/DesignSpecification.md` (9.5 bottom sheet)
- Downstream consumers: UI stories/tasks for purchases and rewarded unlocks on Log Fart and Profile

## Traceability
- Feature link(s): `MON-01`, `MON-03`, `CREATE-03` (Steps 3 and 7), `PROFILE-01`
- Loop link(s): `L1`, `L5`
- Journey stage(s): `3 -> 7`
- Persona focus: Identity Builder, Instant Joker
- Architecture link(s): `planning/architecture/DataContracts.md` §3.13–3.15, §5.7; `planning/architecture/MultiplatformStrategy.md` §8; `planning/skills/monetization-and-entitlements.md`

## Screen Goal
- Primary user outcome: unlock a locked avatar or sound by buying it (permanent) or watching a rewarded ad (one use) without leaving the current flow.
- Success signal: the item becomes selectable only after the server-mirrored entitlement or unlock token exists.

## Entry/Exit
- Entry routes: tapping a locked item in the Log Fart avatar row or sound picker, or in Profile → Edit avatar.
- Exit routes: success returns to the selector with the item selected; cancel/dismiss returns unchanged.
- Navigation/back behavior: back dismisses the sheet; an in-flight purchase or ad cannot be interrupted by the app.

## Layout Structure
- Top-level regions: item preview (asset, humorous name and one-line description), price line from the store offering, two buttons.
- Primary CTA: **Buy** (label carries the localized price).
- Secondary actions: **Watch Ad to Try Once**; close. No third option until MON-02 ships (Phase 4).

## UI States
- Loading: offering/price loading skeleton; buttons disabled.
- Empty: N/A.
- Success: "It's yours." (purchase) or "Unlocked for this one." (ad) then auto-return.
- Error: purchase failed/cancelled → sheet stays with a humor-consistent note; ad not available/dismissed early → "No ad right now. Try again or buy it."; daily ad cap reached → ad button hidden, Buy remains.
- Offline: both buttons disabled with "Needs a connection" note; the free items remain usable.

## Interactions
- User actions: Buy (store flow via RevenueCat), Watch Ad (UMP consent on first use, then the rewarded ad), close.
- Feedback/animation notes: unlock state appears only when the entitlement/token cache updates; never before.
- Validation behavior: the ad button is shown only for items with `rewarded_unlockable == true`; never for voice recording.

## Safety and Privacy UX
- Visibility/privacy defaults shown: N/A.
- Reporting/blocking affordance: N/A.
- Sensitive data redaction/display rules: no receipt or token values displayed; ad requests carry no location.

## Platform Differences
- Android behavior: Play Billing sheet; Google Mobile Ads rewarded flow; UMP consent form.
- iOS behavior: StoreKit sheet; Google Mobile Ads rewarded flow; UMP consent and ATT prompt in this context only.
- Shared behavior constraints: identical options, copy, caps and outcomes.

## Stitch Mapping (if used)
- stitch_artifact_id: `TBD-UXF-11-UNLOCK`
- stitch_version: `TBD`
- mapped_frames: unlock-default, unlock-loading, unlock-success, unlock-error, unlock-capped
- unresolved deltas: preview treatment for sounds (waveform vs icon)

## Implementation Handoff
- Required components: bottom sheet, item preview, primary and secondary buttons with loading state, inline note.
- State model expectations: `LoadingOffer`, `Ready(offer, adAvailable)`, `Purchasing`, `WatchingAd`, `Unlocked(source)`, `Failed(category)`, `Capped`.
- Test/validation notes: no local unlock path exists; cap hides the ad button; purchase without mirrored entitlement shows restoring state; copy review against `Monetization.md` §2 rule 10.

---
name: release
description: Prepare an app store release (Google Play and App Store) - versioning, signing, release builds, Crashlytics mappings, privacy labels and Data Safety form, review notes for App Store guidelines 4.3 and 1.2, staged rollout, backend prod deploy. Use when preparing a beta, a launch, or any store submission.
disable-model-invocation: true
---

# Release checklist

Agents prepare; the **owner** performs every console action, signing step and submission.

## 1. Build
- [ ] Bump `versionCode`/`versionName` (Android) and `CFBundleShortVersionString`/`CFBundleVersion` (iOS) together; tag `vX.Y.Z` after merge.
- [ ] `prod` release builds: Android App Bundle (R8 on; Crashlytics mapping upload), iOS Archive with `Release-prod`. Signing material lives only in the owner's keychain or CI secrets.
- [ ] Backend: rules and functions deployed to `prod` through the release workflow (reviewer approval) **before** the app goes live.
- [ ] Remote Config defaults are in place (if ADR-0024 is accepted).

## 2. Store compliance (fart apps get extra scrutiny)
- [ ] **App Store 4.3 (spam / saturated categories; Apple names fart apps).** The listing, screenshots and review notes lead with the social map and UGC community, not sound effects. The review notes explain the map, reactions, moderation and the anonymous start. Provide a demo account only if login is required (it isn't: anonymous start).
- [ ] **App Store 1.2 (UGC).** Report and block on all content, auto-hide at 3 reports, a published contact, terms of use accepted in-app, and a way to filter objectionable content.
- [ ] **Account deletion** in-app (5.1.1(v)), plus a web deletion URL for Google Play.
- [ ] **Sign in with Apple** is offered, since Google sign-in exists.
- [ ] **Age rating:** crude humor and UGC → 12+ / Teen; minimum age 13 enforced by the age band.
- [ ] **Privacy:** App Privacy labels and Play Data Safety match the data actually collected (location coarse public / precise private, identifiers, purchases, ads with consent). ATT prompt text; UMP consent configured in AdMob.
- [ ] **Location permission:** foreground only, with purpose strings that explain the map.
- [ ] `app-ads.txt` published on the app's domain (AdMob).

## 3. Listing
- [ ] Use the `store-listing` skill output: title, subtitle/short description, keywords, screenshots, preview video, localized copy.

## 4. Rollout
- [ ] Android: internal → closed testing → staged production (10% → 50% → 100%), watching Crashlytics crash-free users ≥ 99.5% and ANRs.
- [ ] iOS: TestFlight → phased release.
- [ ] Post-launch: Gate 1 dashboard (`planning/product/Scope.md` §6) and revenue events are flowing.

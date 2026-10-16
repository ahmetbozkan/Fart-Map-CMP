# Tech Stack: Fart Map

> **Planning spine position:** authoritative technology and version contract.
> Upstream: `architecture/ArchitecturePrinciples.md`, `architecture/Architecture.md`, ADR-0001/0002/0003/0004/0005/0008/0015/0016/0017.
> Downstream: Gradle configuration, dependency declarations, CI build definitions, backend toolchain, implementation modules.
> This document defines approved technologies and version governance. It does not define feature behavior.

---

## 1) Purpose

This document locks the baseline stack so all contributors use one consistent toolchain.

It exists to prevent:

- drift in framework choices,
- mixed dependency patterns across features,
- unreviewed version upgrades that break multiplatform parity,
- a new project starting from the PoC's year-old dependency lines by accident.

---

## 2) Core Stack Decisions

Settled stack decisions:

- Kotlin Multiplatform + Compose Multiplatform for shared client code.
- Room for local persistence (ADR-0001).
- GitLive Firebase SDK for Firebase access from shared code (ADR-0002).
- Koin for DI with shared + platform modules (ADR-0003).
- Custom StateViewModel pattern for presentation (ADR-0004).
- Type-safe Compose Navigation (ADR-0005).
- `expect`/`actual` seam for platform APIs (ADR-0008).
- Cloud Functions for Firebase (2nd gen, TypeScript) as backend compute; Firebase scope = Auth, Firestore, Storage, Analytics, Cloud Functions, Cloud Messaging, Crashlytics, App Check (ADR-0015).
- RevenueCat Kotlin Multiplatform SDK for purchases and entitlements (ADR-0016).
- Google Mobile Ads (AdMob) rewarded format with server-side verification (ADR-0017).
- Maps: Google Maps on Android, MapKit on iOS, behind one shared map contract (owner decision 2026-10-14).

No alternative technology may be introduced for any settled concern without a new ADR.

---

## 3) Approved Technology Catalog

The new project does **not** inherit the PoC's version lines. It is bootstrapped on the **latest stable, mutually compatible set** of the libraries below (§4.1) and the exact versions are then recorded in §3.1 as the baseline.

### Language and build

- Kotlin (K2) with the Compose compiler plugin.
- Gradle with the Android Gradle Plugin; version catalog (`gradle/libs.versions.toml`) as the single version source.
- KSP for annotation processing (Room).
- JDK: 21 LTS if the AGP/Gradle pair at bootstrap supports it, otherwise 17 LTS. The JDK is pinned in the repository README and CI.

### UI and app framework

- Compose Multiplatform with Material 3.
- AndroidX Navigation Compose for Compose Multiplatform (type-safe routes).
- AndroidX Lifecycle (ViewModel, runtime compose) for Compose Multiplatform.

### Dependency injection

- Koin (core, compose, compose-viewmodel; android artifacts on Android only).

### Persistence

- Room for Kotlin Multiplatform with the bundled SQLite driver (Android) and the native driver (iOS).
- DataStore Preferences for lightweight settings.

### Backend and network

- GitLive Firebase: auth, firestore, storage, analytics, crashlytics, messaging modules as needed by the phase.
- Firebase iOS SDK via Swift Package Manager in the iOS app target; the Kotlin framework must link against the same Firebase products (see §6.2).
- Ktor client for non-Firebase HTTP use cases only (none in Phase 1).

### Backend toolchain

- Node.js current LTS, TypeScript, `firebase-tools` CLI, Firebase Emulator Suite.
- Repository layout: `backend/firestore.rules`, `backend/firestore.indexes.json`, `backend/storage.rules`, `backend/functions/`.

### Monetization and growth

- RevenueCat `purchases-kmp` (ADR-0016).
- Google Mobile Ads SDK (Android) and Google Mobile Ads SDK for iOS via SPM, plus the User Messaging Platform SDK for consent (ADR-0017), behind `expect`/`actual` adapters.

### Serialization and time

- `kotlinx-serialization`.
- `kotlinx-datetime` (note: `Instant`/`Clock` moved to `kotlin.time`; pick the Compose Material3 version compiled against the same line).

### Media and images

- Coil 3 for image loading.
- Audio playback and recording through platform APIs behind `expect`/`actual` (Phase 1 playback only).

### Location and maps

- Google Maps Compose (Android) and MapKit (iOS) via platform layer only.
- Location: Play Services Location (Android), Core Location (iOS) behind the shared `LocationProvider` contract.
- Any direct map or location SDK usage in shared domain/data code is forbidden.

### 3.1 Baseline at bootstrap (to be filled on day one of the new repository)

| Component | Version | Verified on |
|---|---|---|
| Kotlin | _record_ | _date_ |
| Compose Multiplatform | _record_ | |
| AGP / Gradle | _record_ | |
| Room / KSP | _record_ | |
| Koin | _record_ | |
| GitLive Firebase / Firebase BOM / Firebase iOS SDK | _record_ | |
| Navigation / Lifecycle (CMP) | _record_ | |
| RevenueCat purchases-kmp | _record_ | |
| Google Mobile Ads (Android / iOS) / UMP | _record_ | |
| kotlinx-serialization / kotlinx-datetime / Coil | _record_ | |
| Node.js / firebase-tools | _record_ | |

The PoC lines (Kotlin 2.2.20, CMP 1.9.0, AGP 8.11.2, Room 2.8.2, Koin 4.1.1, GitLive 2.4.0, Firebase BOM 34.4.0, Firebase iOS 12.6.0) are reference only; they are known to build on Android and to link the iOS framework, and known to fail the iOS app link when Firebase products are not linked explicitly (§6.2).

---

## 4) Version Governance Rules

### 4.1 Bootstrap policy (new project)

1. Choose the latest stable release of each catalog entry whose compatibility is documented by its maintainer for the chosen Kotlin and Compose Multiplatform versions.
2. Prove the set on day one: Android debug build, shared unit tests, iOS framework link, iOS simulator app build, backend emulator tests.
3. Record the proven versions in §3.1 with the date. That table is the baseline; the version catalog in the repository must match it.
4. Do not mix a dependency from the PoC catalog into the new catalog without checking it against step 1.

### 4.2 Upgrade policy

1. Centralize versions in one version catalog (`libs.versions.toml`).
2. Keep Android/iOS shared dependency lines aligned where multiplatform artifacts are used.
3. Prefer patch/minor upgrades; major upgrades require a compatibility review note in the PR.
4. Any upgrade touching Compose/Kotlin/Firebase/Room/Koin/RevenueCat/Ads requires a regression pass for Android build, iOS build, serialization compatibility, navigation/state integration, purchase and ad smoke flows.
5. Do not mix competing libraries for the same concern (two DI frameworks, two local DB stacks, two purchase SDKs).

---

## 5) Prohibited Additions

Without ADR approval, do not add:

- SQLDelight (conflicts with ADR-0001),
- Hilt/Dagger or other DI frameworks,
- alternate Firebase wrappers in parallel with GitLive,
- a second navigation framework,
- a second state-management framework,
- a second purchases or ads SDK,
- any non-rewarded ad format,
- platform SDK references directly in shared business logic.

---

## 6) Build, CI and Runtime Environment Baseline

### 6.1 Targets

- Android: min SDK 24; target/compile SDK tracked in the version catalog.
- iOS: deployment target matching the Compose Multiplatform baseline; `iosArm64` and `iosSimulatorArm64`.

### 6.2 iOS linking rule

The Kotlin framework produced from shared code auto-links the Firebase products it uses. The iOS app target must link every Firebase product the shared code references (Core, Auth, Firestore, Storage, Analytics, Crashlytics, Messaging) through SPM, and the Kotlin test binary must either link the same frameworks or exclude Firebase-backed code behind interfaces (owner decision L3: keep Firebase behind interfaces so shared tests do not need it). The PoC failed both links with `framework 'FirebaseCore' not found`; this rule prevents that.

### 6.3 CI (GitHub Actions, owner decision L2)

Every pull request runs:

- shared unit tests (`:composeApp:testDebugUnitTest` and the iOS simulator test task once §6.2 is satisfied),
- Android debug build,
- iOS framework link and an `xcodebuild` simulator build on a macOS runner,
- backend: functions unit tests and Firestore rules tests on the emulator suite,
- static checks configured in the repository (Android lint; detekt if adopted).

Secrets (Firebase config files, signing, API keys) are injected from the CI secret store (`SecurityAndSecrets.md` §4) and never printed.

### 6.4 Local validation commands

The new repository's entrypoint README lists the exact commands and the JDK; they are the commands agents run after each meaningful change (Definition of Done).

---

## 7) Verification Checklist

- [ ] Every dependency is from the approved catalog or has ADR approval.
- [ ] §3.1 baseline table filled with proven versions and date.
- [ ] No duplicate framework for a settled concern.
- [ ] Versions are catalog-driven (no hardcoded per-module versions).
- [ ] Shared/business code remains platform-agnostic.
- [ ] iOS app and test binaries link all referenced Firebase products (§6.2).
- [ ] Upgrade PRs include compatibility notes and test evidence.

---

*This is the stack contract. If implementation uses tools outside this contract without ADR approval, it is out of spec.*

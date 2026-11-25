# Tech Stack: Fart Map

> **Planning spine position:** authoritative technology and version contract.
> Upstream: `architecture/Architecture.md`, ADR-0001/0002/0003/0004/0005/0008/0015/0016/0017.
> Downstream: Gradle configuration, dependency declarations, CI build definitions, backend toolchain, implementation modules.
> This document defines approved technologies and version governance. It does not define feature behavior.

---

## 1) Purpose

This document locks the baseline stack so all contributors use one consistent toolchain.

It exists to prevent:

- drift in framework choices,
- mixed dependency patterns across features,
- unreviewed version upgrades that break multiplatform parity,
- this project drifting back to the PoC's old dependency lines by accident.

---

## 2) Core Stack Decisions

Settled stack decisions:

- Kotlin Multiplatform + Compose Multiplatform for shared client code.
- Firestore offline persistence (via GitLive) as the local data layer; DataStore for preferences and drafts (ADR-0022, superseding ADR-0001 Room).
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

This project does **not** inherit the PoC's version lines. It is bootstrapped on the **latest stable, mutually compatible set** of the libraries below (§4.1) and the exact versions are then recorded in §3.1 as the baseline.

### Language and build

- Kotlin (K2) with the Compose compiler plugin.
- Gradle with the Android Gradle Plugin; version catalog (`gradle/libs.versions.toml`) as the single version source.
- JDK: 21 LTS if the AGP/Gradle pair at bootstrap supports it, otherwise 17 LTS. The JDK is pinned in the repository README and CI.

### UI and app framework

- Compose Multiplatform with Material 3.
- AndroidX Navigation Compose for Compose Multiplatform (type-safe routes).
- AndroidX Lifecycle (ViewModel, runtime compose) for Compose Multiplatform.

### Dependency injection

- Koin (core, compose, compose-viewmodel; android artifacts on Android only).

### Persistence

- Firestore persistent cache through GitLive (ADR-0022, `LocalData.md`). Room is not used; reintroducing it needs a new ADR (`LocalData.md` §8).
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

### Authentication

- Firebase Auth through GitLive: Anonymous and Google in Phase 1. Apple moves to Phase 2 (owner, 2026-11-25).
- Google on Android: Credential Manager (`androidx.credentials` + `credentials-play-services-auth`) with `googleid`'s `GetSignInWithGoogleOption`, in `shared/src/androidMain`.
- Google on iOS: the GoogleSignIn SDK via SPM (`GoogleSignIn-iOS`, product `GoogleSignIn`), in Swift behind the `umbrella` callback bridge.
- Both return Google tokens to the shared `GoogleIdTokenProvider`. The Firebase credential sign-in happens in `firebaseData`.

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

### 3.1 Baseline at bootstrap (filled 2026-10-21 by TASK-01-01-01-02; proven by the command set in the repository `CLAUDE.md`)

| Component | Version | Verified on |
|---|---|---|
| Kotlin | 2.4.21 (K2; Compose compiler plugin and serialization plugin on the same version) | 2026-10-21 |
| Compose Multiplatform | 1.12.2; Material3 `1.12.0-alpha03` (no stable Material3 line exists after 1.9.0; this is the line the JetBrains wizard pairs with CMP 1.12.x; replace by the first stable 1.12.x) | 2026-10-21 |
| AGP / Gradle | 9.4.1 / 9.8.2 (template shipped 9.1.1 / 9.5.1; raised on day one, both builds green); daemon JVM Azul Zulu 21 from `gradle/gradle-daemon-jvm.properties`, launcher JDK 17+; compileSdk 37, targetSdk 37, minSdk 24; JVM target 17 | 2026-10-21 |
| Room / KSP | Removed 2026-11-22 (ADR-0022); never resolved | — |
| Firebase Remote Config | Added with EPIC-01-06 (ADR-0024): GitLive `firebase-config` on the same GitLive line + `Firebase/RemoteConfig` SPM product linked in `iosApp` | not yet resolved |
| Koin | 4.2.2 (core, compose, compose-viewmodel) | 2026-10-21 |
| GitLive Firebase / Firebase BOM / Firebase iOS SDK | 2.7.0 (auth, firestore, storage, analytics, crashlytics, messaging) / 35.0.2 / 13.0.1 via SPM `upToNextMajor` (products Analytics, Auth, Firestore, Storage, Crashlytics, Messaging, AppCheck linked in `iosApp`; `-ObjC`); `Package.resolved` tracked | 2026-10-21 |
| Navigation / Lifecycle (CMP) | 2.9.2 / 2.11.0 | 2026-10-21 |
| kotlinx-coroutines / DataStore Preferences | 1.11.0 / 1.3.0 | 2026-10-21 |
| RevenueCat purchases-kmp | 3.14.0 — catalog entry only; first resolved by EPIC-01-06 | not yet resolved |
| Credential Manager / googleid / GoogleSignIn-iOS | 1.6.0 / 1.2.1 / SPM `upToNextMajor` from 10.0.0, resolved 10.1.0 with AppAuth 3.0.0, GTMAppAuth 6.0.0 and gtm-session-fetcher 5.3.1 (STORY-01-02-03). GoogleSignIn 9.x can't sit next to Firebase 13: it needs gtm-session-fetcher <4 and app-check 11. The pins are written to `Package.resolved` on the next Mac build; the CI `ios` job resolves the package until then | 2026-11-25 (Android build; iOS via CI) |
| Google Mobile Ads (Android / iOS) / UMP | 25.5.0 / iOS SPM package not yet added / 4.0.0 — catalog entries only; EPIC-01-06 | not yet resolved |
| kotlinx-serialization / kotlinx-datetime / Coil | 1.12.0 / 0.8.0 / 3.6.3 (Coil catalog entry only) | 2026-10-21 (Coil not yet resolved) |
| Node.js / firebase-tools | 22 (`backend/functions` engine; CI) / 15.25.1; firebase-functions 6.6.0, firebase-admin 13.10.0, TypeScript 5.9.3, vitest 3.2.7, `@firebase/rules-unit-testing` 4.0.1 | 2026-10-21 (local run on Node 20.11.0; the emulator needs Java 21) |
| detekt / detekt-formatting / compose-rules / Konsist | 1.23.8 (its analyzer is pinned to Kotlin 2.0.21 in the root build, the documented workaround for detekt 1.x on newer Kotlin) / 1.23.8 / 0.4.28 (the line built for detekt 1.23) / 0.18.1. detekt 2.0 is still alpha (2.0.0-alpha.6); move to it and compose-rules 0.6+ once it is stable | 2026-11-21 (Windows 11, JBR 21) |

Verification environment: macOS 26 (Darwin 25.6), Xcode 26.4.1, iOS simulator "iPhone 17", Android Studio 2026.2 lint. Notes recorded with the baseline:

- Module layout is ADR-0021: `shared` (Firebase-free), `firebaseData` (GitLive), `umbrella` (iOS framework `Shared`, static). Because the framework is static, `:umbrella:embedAndSignAppleFrameworkForXcode` reports SKIPPED for the embed step by design; linking happens through the framework search path.
- The Xcode configurations are `Debug-dev`, `Debug-prod`, `Release-dev`, `Release-prod`; `KOTLIN_FRAMEWORK_BUILD_TYPE` is set per configuration in `iosApp/Configuration/Config.xcconfig` because Kotlin cannot infer it from custom names.
- iOS deployment target is the template's 18.2. The linker warns that the Kotlin/Native `libicu` data object was built for simulator 18.5; harmless, revisit when the minimum iOS version is decided.
- Android needs the Crashlytics Gradle plugin (`com.google.firebase.crashlytics` 3.0.8, applied in `androidApp`) as soon as the Crashlytics SDK is on the classpath; without it the app aborts at start with "The Crashlytics build ID is missing".
- GitLive 2.7.0: `Firebase.app.options` throws `NullPointerException` on iOS for projects without Realtime Database (its Apple implementation force-unwraps `databaseURL`). Project identity is read through platform actuals in `firebaseData` (`FIRApp.defaultApp()?.options?.projectID` via the FirebaseCore cinterop that GitLive ships; GitLive on Android). Re-check when GitLive is upgraded.
- CI: the two iOS jobs (shared tests; app build) run on GitHub's `macos-26` image with Xcode 26.4.1 selected (`macos-15` defaults to Xcode 16.4, whose Swift tools 6.1 cannot resolve `firebase-ios-sdk` 13, which declares 6.2.1); Android and backend jobs run on `ubuntu-latest` with Zulu 21.
- Lint on the baseline: 0 errors, 3 warnings (template launcher icons and `drawable-v24`), to be cleared when STORY-01-01-02 replaces the icons.
- Gradle reports "Deprecated Gradle features were used … incompatible with Gradle 10" from third-party plugins; no action until Gradle 10.

The PoC lines (Kotlin 2.2.20, CMP 1.9.0, AGP 8.11.2, Room 2.8.2, Koin 4.1.1, GitLive 2.4.0, Firebase BOM 34.4.0, Firebase iOS 12.6.0) are reference only; they are known to build on Android and to link the iOS framework, and known to fail the iOS app link when Firebase products are not linked explicitly (§6.2).

---

## 4) Version Governance Rules

### 4.1 Bootstrap policy (applied 2026-10-21)

1. Choose the latest stable release of each catalog entry whose compatibility is documented by its maintainer for the chosen Kotlin and Compose Multiplatform versions.
2. Prove the set on day one: Android debug build, shared unit tests, iOS framework link, iOS simulator app build, backend emulator tests.
3. Record the proven versions in §3.1 with the date. That table is the baseline; the version catalog in the repository must match it.
4. Do not mix a dependency from the PoC catalog into the new catalog without checking it against step 1.

### 4.2 Upgrade policy

1. Centralize versions in one version catalog (`libs.versions.toml`).
2. Keep Android/iOS shared dependency lines aligned where multiplatform artifacts are used.
3. Prefer patch/minor upgrades; major upgrades require a compatibility review note in the PR.
4. Any upgrade touching Compose/Kotlin/Firebase/Koin/RevenueCat/Ads requires a regression pass for Android build, iOS build, serialization compatibility, navigation/state integration, purchase and ad smoke flows.
5. Do not mix competing libraries for the same concern (two DI frameworks, two local DB stacks, two purchase SDKs).

---

## 5) Prohibited Additions

Without ADR approval, do not add:

- SQLDelight or Room (local data is Firestore persistence per ADR-0022; a dataset that needs a local DB requires a new ADR),
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

The Kotlin framework produced from shared code auto-links the Firebase products it uses. The iOS app target must link every Firebase product the shared code references (Core, Auth, Firestore, Storage, Analytics, Crashlytics, Messaging) through SPM, and the Kotlin test binary must either link the same frameworks or exclude Firebase-backed code behind interfaces (owner decision L3, implemented by the module layout of ADR-0021: `shared` has no Firebase dependency, `firebaseData` holds the GitLive adapters, `umbrella` builds the iOS framework). The PoC failed both links with `framework 'FirebaseCore' not found`; this rule prevents that.

### 6.3 CI (GitHub Actions, owner decision L2)

Every pull request runs:

- shared unit tests (`:shared:testAndroidHostTest` on Linux, `:shared:iosSimulatorArm64Test` on macOS),
- Android debug build,
- iOS framework link and an `xcodebuild` simulator build on a macOS runner,
- backend: functions unit tests and Firestore rules tests on the emulator suite,
- static checks: Android lint, `detekt` (all Kotlin modules, `config/detekt/detekt.yml`) and the Konsist architecture tests in `:shared:testAndroidHostTest`; `scripts/check-doc-links.sh` in the secret-scan job.

Secrets (Firebase config files, signing, API keys) are injected from the CI secret store (`Security.md` §4) and never printed.

### 6.4 Local validation commands

The root `CLAUDE.md` and `README.md` list the exact commands; `scripts/verify.sh` runs the right subset for a change (macOS also builds iOS; on Windows/Linux iOS is verified by CI).

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

---
name: platform-capability
description: Add a native platform capability (maps, location, auth providers, audio, push token, ads, purchases, App Check, secure storage, share sheet) behind a shared contract with Android and iOS implementations. Use when shared code needs something only a platform SDK can do.
---

# Platform capability

1. **Contract** in `shared/src/commonMain/.../core/platform/<Name>.kt`. Expose domain-safe types only (no SDK types). Keep it minimal: suspend functions or `Flow`s, a typed `AppResult` on failure.
2. **Choose the seam (ADR-0008):**
   - **`expect`/`actual`:** a small pure-Kotlin seam (platform name, clock, simple file path). Actuals go in `shared/src/androidMain` and `shared/src/iosMain`.
   - **Interface + implementation:** anything SDK-backed. Android implementation in `shared/src/androidMain` (or `firebaseData/src/androidMain` if it's Firebase). iOS implementation either in Kotlin `iosMain` via cinterop-available frameworks, or **in Swift** (MapKit, AuthenticationServices, Google Mobile Ads, UMP/ATT, RevenueCat UI) as a class conforming to the Kotlin interface, handed to Koin through a `umbrella` entry point (e.g. `MainViewController(platformBindings = …)`).
3. **Bind** it in the platform Koin module: Android at app start, iOS in the `umbrella` composition.
4. **Permissions:** request in context at first use. Map denied or restricted to a typed state, never a crash.
5. **Parity:** implement both platforms in the same PR. If one platform is impossible right now, stop and report it; don't ship a one-sided feature.
6. **Test:** a fake implementation in `commonTest` for ViewModel tests; contract behavior only, not SDK behavior.
7. **Verify:** `/verify`. On Windows the iOS implementation is verified by the CI `ios` job; call that out in the PR.

## Known platform specifics
- **Maps:** Google Maps Compose on Android, MapKit on iOS, behind one `MapView` contract. The Maps key comes from untracked `secrets.properties`.
- **Ads:** request ads with location disabled, after UMP consent; ATT only in that context (ADR-0017).
- **Purchases:** RevenueCat `purchases-kmp`, app user id = Firebase uid (ADR-0016).
- **GitLive on iOS:** `Firebase.app.options` crashes without a Realtime Database URL. Read the project id via `FIRApp` (see `firebaseData/src/iosMain`).

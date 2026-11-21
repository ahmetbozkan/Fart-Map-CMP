---
paths:
  - "iosApp/**"
  - "umbrella/**"
---

# iOS shell rules

- The SwiftUI shell stays thin: `iOSApp.swift` / `ContentView.swift` host `MainViewControllerKt.MainViewController(...)`. Product logic belongs in `shared`.
- Swift code is allowed only for platform capabilities that need native SDKs (MapKit, AuthenticationServices, StoreKit via RevenueCat, Google Mobile Ads, UMP/ATT). It implements a `shared` interface and is injected through Koin from `umbrella`.
- Configurations: `Debug-dev` / `Release-dev` / `Debug-prod` / `Release-prod`.
  - `Configuration/{Dev,Prod}.xcconfig` set the bundle id, display name and `APP_ENV`.
  - `Config.xcconfig` sets `KOTLIN_FRAMEWORK_BUILD_TYPE` per configuration (Kotlin can't infer it from custom names).
- The "Copy Firebase Config" phase copies `GoogleService-Info-$APP_ENV.plist`. Those files are injected, untracked, and never read or printed.
- The Firebase iOS SDK comes through SPM (`Package.resolved` is tracked). Link every Firebase product the Kotlin side references, or the app link fails (TechStack §6.2).
- The framework is static, so the embed step reporting SKIPPED is expected.
- Building iOS needs macOS (`xcodebuild … -scheme "FartMap Dev"`). On Windows, rely on the CI `ios` job and say so in the PR.

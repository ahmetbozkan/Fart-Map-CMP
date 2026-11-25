# Fart Map

Kotlin Multiplatform + Compose Multiplatform app for Android and iOS. The specification lives in [`planning/`](planning/README.md); agents and humans start with the root `CLAUDE.md` and `planning/STATE.md`.

## Modules

- `shared` — Compose Multiplatform UI, domain, data contracts and sync. No Firebase SDK types; shared tests run without Firebase (ADR-0021, `planning/architecture/TechStack.md` §6.2).
- `firebaseData` — GitLive Firebase adapters implementing the `shared` contracts; the only Kotlin module that depends on Firebase.
- `umbrella` — iOS-only module that links `shared` + `firebaseData` into the static framework `Shared` consumed by Xcode.
- `androidApp` — Android shell (flavors `dev` / `prod`, build types `debug` / `release`).
- `iosApp` — Xcode project and SwiftUI shell (configurations `Debug-dev`, `Release-dev`, `Debug-prod`, `Release-prod`; schemes `FartMap Dev`, `FartMap Prod`).
- `backend` — Firebase rules, indexes and Cloud Functions (aliases `dev`, `prod`).

## Toolchain

- JDK 21 for the Gradle daemon (`gradle/gradle-daemon-jvm.properties` provisions it; installing it avoids the download: macOS `brew install openjdk@21`, Windows `winget install Azul.Zulu.21.JDK`). The launcher needs JDK 17 or newer.
- Windows: run the bash scripts in Git Bash (`bash scripts/verify.sh`); in PowerShell use `gradlew.bat`. iOS cannot be built on Windows; the macOS CI jobs verify it on every PR that touches iOS-relevant code (`scripts/ci-ios-affected.sh`).
- Gradle wrapper, Kotlin, Compose Multiplatform and AGP versions: `gradle/libs.versions.toml`; the proven baseline is recorded in `planning/architecture/TechStack.md` §3.1.
- Xcode 26 with the iOS simulator runtime. Firebase iOS SDK comes through Swift Package Manager (`Package.resolved` is tracked).
- Node.js 22 and `firebase-tools` 15 for the backend; the Firestore emulator needs Java 21 on `PATH` (`backend/README.md`).

## Config injection (never commit real files)

Firebase client configs and secrets are not tracked. Templates are committed next to each real file:

| Real file (untracked) | Template |
|---|---|
| `androidApp/src/dev/google-services.json` | `…/google-services.json.template` |
| `androidApp/src/prod/google-services.json` | `…/google-services.json.template` |
| `iosApp/Configuration/GoogleService-Info-dev.plist` | `…/GoogleService-Info-dev.plist.template` |
| `iosApp/Configuration/GoogleService-Info-prod.plist` | `…/GoogleService-Info-prod.plist.template` |
| `androidApp/secrets.properties` | `androidApp/secrets.properties.template` |

Run `scripts/inject-config.sh` to write them. It reads, per file, a base64 environment variable (`GOOGLE_SERVICES_JSON_DEV`, `GOOGLE_SERVICES_JSON_PROD`, `GOOGLE_SERVICE_INFO_PLIST_DEV`, `GOOGLE_SERVICE_INFO_PLIST_PROD`, plus optional `MAPS_API_KEY_DEV` / `MAPS_API_KEY_PROD`) or a copy in `~/.fartmap/` (`google-services-dev.json`, `google-services-prod.json`, `GoogleService-Info-dev.plist`, `GoogleService-Info-prod.plist`). CI uses the environment variables from the repository secret store. `scripts/check-no-secrets.sh` fails if a real file is ever tracked; CI also runs gitleaks with `.gitleaks.toml`.

If a real config is ever committed: remove it from the index, rotate the affected keys in the Firebase console, and record the incident in the PR description and `planning/STATE.md`.

## Commands

```bash
# Android (both flavors) and lint
./gradlew :androidApp:assembleDevDebug :androidApp:assembleProdDebug
./gradlew :androidApp:lintDevDebug

# Shared tests (Android host + iOS simulator)
./gradlew :shared:allTests

# iOS framework and app (xcodebuild runs the Gradle embed task itself)
./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64
xcodebuild -project iosApp/iosApp.xcodeproj -scheme "FartMap Dev" -destination 'platform=iOS Simulator,name=iPhone 17' build
xcodebuild -project iosApp/iosApp.xcodeproj -scheme "FartMap Prod" -destination 'platform=iOS Simulator,name=iPhone 17' build
# The Kotlin framework is static: the embed step reports SKIPPED by design; the custom configuration
# names need KOTLIN_FRAMEWORK_BUILD_TYPE, which Configuration/Config.xcconfig sets per configuration.

# Secrets guard
scripts/check-no-secrets.sh
```

Backend commands are listed in `backend/README.md`.

## Rules that never bend

- No secrets or Firebase config files in git.
- No error swallowing; no dependency-direction violations (`CLAUDE.md`, `planning/architecture/Architecture.md`).
- The story file and `planning/STATE.md` are updated with every meaningful change (`planning/WorkingAgreement.md`).

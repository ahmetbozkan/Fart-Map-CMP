# Fart Map — repository entrypoint

Read `planning/CLAUDE.md` first; it is the agent front door and names the mandatory reading order. Then `planning/roadmap/STATE.md` for what is authorized now, and `planning/meta/AgentGuardrails.md` for hard stops.

This repository is the product. The PoC (`FartMap-CMP`) is frozen reference material only.

## Toolchain (verified 2026-10-21; baseline table in `planning/architecture/TechStack.md` §3.1)

- Gradle wrapper 9.8.2 with a Java 21 daemon JVM (`gradle/gradle-daemon-jvm.properties`); launcher JDK 17 or newer. Install JDK 21: `brew install openjdk@21`.
- Kotlin 2.4.21, Compose Multiplatform 1.12.2, AGP 9.4.1, compileSdk 37, minSdk 24, JVM target 17.
- Backend: Node.js 22, `firebase-tools` 15, Java 21 on `PATH` for the emulators.
- Modules (ADR-0021): `shared` (Compose Multiplatform UI, domain, data contracts, sync; no Firebase SDK types), `firebaseData` (GitLive Firebase adapters), `umbrella` (iOS-only; builds the `Shared` framework), `androidApp` (flavors `dev`/`prod`), `iosApp` (Xcode project; configurations `Debug-dev`, `Release-dev`, `Debug-prod`, `Release-prod`; schemes `FartMap Dev`, `FartMap Prod`), `backend/` (Firebase rules and Cloud Functions).

## Commands (run after every meaningful change; the launcher JDK must be 17+, the daemon provisions 21)

```bash
./gradlew :androidApp:assembleDevDebug :androidApp:assembleProdDebug
./gradlew :shared:allTests
./gradlew :androidApp:lintDevDebug
./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64
xcodebuild -project iosApp/iosApp.xcodeproj -scheme "FartMap Dev" -destination 'platform=iOS Simulator,name=iPhone 17' build
cd backend/functions && npm ci && npm run build && npm run lint && npm test   # Java 21 on PATH for the emulator
scripts/check-no-secrets.sh
```

Firebase configs are injected, never committed: `scripts/inject-config.sh` (see README, Config injection).

## Non-negotiables

- No secrets or Firebase config files in git (`google-services.json`, `GoogleService-Info*.plist`, `secrets.properties`): templates only; real files are injected (`planning/architecture/SecurityAndSecrets.md` §4).
- No error swallowing; no dependency-direction violations (`planning/CLAUDE.md` §4).
- Update `planning/roadmap/STATE.md`, `planning/meta/ChangeLog.md` and the work item under `planning/roadmap/` after meaningful changes.

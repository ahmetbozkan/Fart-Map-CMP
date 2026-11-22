# ADR-0021: Shared Module Layout — `shared`, `firebaseData`, `umbrella`

- Status: Accepted
- Date: 2026-10-21
- Source: owner decision L3 (shared tests must run without Firebase), PoC failure `framework 'FirebaseCore' not found`, `TechStack.md` §6.2

## Context
`Architecture.md` fixed a single shared module for Phases 1–2. A Kotlin/Native test binary links every dependency of its module; if the GitLive Firebase klibs are dependencies of `shared`, the iOS test executable must link the Firebase frameworks, which Swift Package Manager builds only inside the Xcode app build. The PoC's shared tests and app build both failed on exactly this. The owner decided that Firebase stays behind interfaces so shared tests do not need it.

## Decision
Three Gradle modules form the shared code, still one codebase with the feature-first package layout of `Architecture.md`:

1. `shared` — everything except Firebase SDK types: features, `core/*`, repository interfaces, local-data contracts (`AppWriteScope`, `PendingWriteMonitor`; ADR-0022 replaced the planned sync engine), Compose UI. No GitLive dependency. Its `commonTest`/`iosTest` run without Firebase.
2. `firebaseData` — the GitLive-based implementations of the repository and platform contracts declared in `shared` (`core/data` adapters, auth provider, Firestore/Storage gateways, messaging token registration). Depends on `shared`.
3. `umbrella` — iOS-only KMP module that depends on `shared` and `firebaseData`, builds the static framework `Shared` consumed by `iosApp`, and hosts `MainViewController` plus the iOS Koin composition. `androidApp` depends on `shared` and `firebaseData` directly.

Dependency direction stays Presentation → Domain ← Data: `firebaseData` is the data layer's Firebase corner, nothing in `shared` imports it.

## Rationale
- Isolation by module is the only way Kotlin/Native keeps Firebase out of the test link; it also enforces the "Firebase only behind repository interfaces" rule at compile time.
- The umbrella module is the standard Kotlin Multiplatform pattern for composing several modules into one iOS framework; Swift keeps `import Shared`.
- Cost: two small Gradle modules; no feature code moves across them later because the split follows an existing boundary.

## Consequences
- `Architecture.md` are amended to this layout; `TechStack.md` §6.2 references this ADR.
- CI links the framework from `:umbrella` and runs shared tests from `:shared`.
- Fakes for the Firebase-backed interfaces live in `shared`'s test source sets.

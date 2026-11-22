---
paths:
  - "shared/**"
  - "firebaseData/**"
  - "umbrella/**"
---

# Kotlin shared code rules

Source: `planning/architecture/Architecture.md`. These are enforced by Konsist and detekt where possible.

## Where code goes
- `feature/<name>/presentation`: `[X]Screen.kt`, `[X]ViewModel.kt`, `[X]State.kt` (State + Action + Event together while small).
- `feature/<name>/domain`: entities, `[X]Repository` interfaces, use cases **only if they hold logic** (no pass-through use cases).
- `feature/<name>/data`: mappers and data logic without Firebase types.
- `core/domain`: types two or more features need. `core/presentation`: `StateViewModel` base. `core/navigation`: routes and host. `core/design`: theme/components. `core/di`: Koin. `core/platform`: native capability contracts. `core/observability`: `AppError`, `Result` helpers, logger, analytics events.
- `firebaseData/.../firebase/<mirror of the package>`: `[X]RepositoryImpl`, DTOs, remote mappers. GitLive stays here (keep it an `implementation` dependency).
- `umbrella`: `MainViewController` and the iOS Koin composition only.

## Imports
- Domain: Kotlin stdlib, coroutines, `kotlinx-datetime`, `kotlinx-serialization` annotations only. Never `data`, `presentation`, `androidx.room`, `dev.gitlive`, or Android/iOS SDKs.
- Presentation never imports `data` packages, DTOs, Room entities or Firebase.
- `feature.a` never imports `feature.b`. Shared things go to `core/domain`, and navigation goes through `core/navigation` routes.

## State and UI logic (ADR-0004)
- Every screen ViewModel extends `StateViewModel<State, Action, Event>`. State is one immutable `data class` with defaults; Action and Event are `sealed interface`s.
- Events are one-time effects (navigate, toast, open sheet). Never put them in State.
- The UI renders State and dispatches Actions. No business logic in composables. ViewModels never touch Firebase or DAOs.

## Errors
- Repository writes return `AppResult<T>` with a typed `AppError`. Reads expose `Flow`s of domain types.
- A `catch` must map to State/Event or log a structured event through the logger. Never `printStackTrace`, an empty catch, or catch-and-ignore. Catch `CancellationException` separately and rethrow it.
- Fail closed on auth, permissions and entitlement uncertainty.

## Data
- The domain ↔ DTO mapping round-trips **every** field. Add a mapper test for each new field; the `visibility` regression test is mandatory.
- Visibility default (ADR-0023): PUBLIC in Phase 1 for the 18+ age band, FOLLOWERS for 13–17 and for everyone once SOCIAL-03 ships; last-used (DataStore) wins after the first log if still allowed. PUBLIC is adults-only (`allowedVisibilities`; rules enforce it): never offer it to 13–17 or an unknown age band. An unknown enum value decodes to PRIVATE.
- A fart write = public doc with fuzzed geohash-6 coordinates + `farts/{id}/private/location` with the precise coordinates, in one batch. Precise coordinates never enter presentation types for content the user doesn't own.
- Never compute counters, ranks, entitlements or unlocks on the client.
- Offline (ADR-0022, `planning/architecture/LocalData.md`): Firestore's persistent cache is the local layer; no Room, no sync engine. Client-generated id, one batch per user action launched in `AppWriteScope` (**never `await` GitLive `set()`/`commit()` on the UI path**, because it suspends until the server acknowledges), rejections reported through `PendingWriteMonitor`. Pending state comes from `hasPendingWrites`.
- Runtime levers (prices, ad cap, offer timing) come from the `RemoteConfig` contract with safe defaults (ADR-0024); never trust a client-side value for anything the server enforces.
- Purchases, rewarded ads and account linking are online-only; never queue them.

## Platform code (ADR-0008)
- A small pure seam uses `expect`/`actual`. An SDK-backed capability uses an interface in `core/platform`, implemented in `androidMain`/`iosMain` or in Swift and injected through Koin from `umbrella`. Contracts expose domain types only. Both platforms ship together.

## DI and navigation
- Koin modules per feature in `core/di` style; platform bindings come from platform modules. No global singletons outside Koin.
- Routes are `@Serializable` types. The start destination comes from session state. The ViewModel emits navigation as an Event and the UI navigates.

## Tests (`commonTest`, fast host run: `./gradlew :shared:testAndroidHostTest`)
- ViewModel: Actions in → State/Event out, using fakes for repositories (fakes live in test source sets).
- Use `kotlinx-coroutines-test`, never real dispatchers or sleeps. No mocking of the class under test.

## Complexity
- Don't add an abstraction with one implementation (a test fake counts as a second).
- Don't build anything that belongs to a later phase. Check `planning/product/Scope.md`.

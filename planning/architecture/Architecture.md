# Architecture: Fart Map

> Merged on 2026-11-21 from ArchitecturePrinciples, Architecture, RepositoryRules, MultiplatformStrategy and NavigationStrategy (originals and their rationale and PoC case studies are in `planning/archive/architecture/`). No decision changed. Day-to-day rules are also distilled into `.claude/rules/kotlin-shared.md` and enforced where possible by detekt and Konsist. This file is the reference behind them.
> Authority: below `planning/product/Product.md` and the ADRs, above every other architecture doc.

## 1) Goals (priority order)

1. **Correctness.** Every field round-trips through every layer. The PoC dropped `visibility` silently and every fart became PRIVATE.
2. **Maintainability.**
3. **Safety and security.**
4. **Testability.** Every layer can be tested without the full stack.
5. **Offline resilience.** User actions never fail because of the network.
6. **AI readability.** One pattern per concern, no magic.
7. **Simplicity.**
8. **Scalability.** Build for this phase and the next, not later.

When two goals conflict, the higher one wins. "Might be useful later" never justifies complexity. Phase-appropriate complexity is a rule: nothing that is first needed in Phase N+2 is built in Phase N.

## 2) Principles

1. **Product first.** Every structural choice traces to a product requirement.
2. **Simplicity before cleverness.** Boring, explicit code. An abstraction must earn its place: an interface exists when there are two real implementations (the real one plus a test fake counts). A use case exists only when it holds domain logic. No empty delegation.
3. **Offline-tolerant by default.** See §8.
4. **Single source of truth.** Each piece of state has one owner. The UI observes; it never keeps its own copy of persisted data.
5. **Predictable state flow.** data source → repository → ViewModel → UI. Errors are typed values.
6. **Consistency over local optimization.** A second pattern for any concern needs an ADR first.

## 3) Layers and dependency direction

```
Presentation ──► Domain ◄── Data ──► Platform contracts
                    ▲
Infrastructure (DI, SDK init, app shells) wires everything
```

| Layer | Owns | Must not contain |
|---|---|---|
| Presentation | Screens, ViewModels, State/Action/Event, navigation effects, permission requests | Business rules, data access, Room/DTO/Firebase types, another feature's ViewModel |
| Domain | Entities, business rules, validation, repository interfaces, use cases with logic | Any import from data, presentation, platform SDKs, Room, Firebase |
| Data | Repository implementations, mapping (domain ↔ local ↔ remote), local persistence, remote access | Presentation or navigation types, business rules |
| Platform | Adapters for native capabilities behind shared contracts | Business decisions (visibility, monetization, safety) |
| Infrastructure | Koin modules, SDK initialization, secrets loading, app entry points | Product logic |

**Non-negotiable:**
- Domain never imports Data.
- Presentation never imports Data.
- Features never import each other: shared types live in `core/domain`, and cross-feature navigation uses `core/navigation` routes.
- Mapping happens in the data layer, never in domain models.

## 4) Modules and packages (ADR-0021)

| Module | Contents |
|---|---|
| `shared` | All features, `core/*`, repository interfaces, Compose UI, ViewModels. **No Firebase SDK types**, so `commonTest` and `iosTest` run without Firebase |
| `firebaseData` | GitLive implementations of the `shared` contracts: repositories, auth provider, Firestore/Storage gateways, messaging token. Depends on `shared` |
| `umbrella` | iOS only. Depends on `shared` + `firebaseData`, builds the static framework `Shared`, hosts `MainViewController` and the iOS Koin composition |
| `androidApp` | Android shell; flavors `dev` / `prod` |
| `iosApp` | SwiftUI shell; configurations `Debug-dev` / `Release-dev` / `Debug-prod` / `Release-prod` |
| `backend/` | Firestore/Storage rules, indexes, Cloud Functions (TypeScript, v2) |

Package layout inside `shared/src/commonMain/kotlin/com/ahmetbozkan/fartmap/`:

```
feature/<name>/presentation   Screen, ViewModel, State/Action/Event
feature/<name>/domain         entities, repository interfaces, use cases with logic
feature/<name>/data           repository impls + mappers that need no Firebase types
core/domain                   shared types (User, GeoPoint, FartVisibility, AppError…)
core/data                     shared data infrastructure
core/presentation             StateViewModel base, shared UI-state helpers
core/navigation               routes, nav host, session-driven start
core/design                   theme tokens, components
core/di                       Koin modules
core/platform                 contracts for native capabilities (actuals in androidMain/iosMain or Swift)
core/observability            Result/AppError, logger, analytics event contracts
```

- **Features:** `map`, `log`, `auth`, `profile`, `social`, `progression`, `monetization`, `notifications`.
- **Ownership:** a fart is a `feature/log` entity even when the map shows it.
- **Firebase:** any code that touches Firebase lives in `firebaseData/src/*/kotlin/com/ahmetbozkan/fartmap/firebase/`, mirroring the feature or `core/data` package it implements.
- **Local data** (ADR-0022): Firestore's persistent cache through `firebaseData`; no Room database. Writes go through the app-scoped `AppWriteScope` and `PendingWriteMonitor` in `core/data` (contracts) / `firebaseData` (implementation). Details: `LocalData.md`.

## 5) Patterns: one answer per concern

| Concern | Pattern |
|---|---|
| Screen state | `StateViewModel<State, Action, Event>` (ADR-0004): one immutable `data class` State, a sealed `Action` interface, a sealed `Event` interface for one-time effects (navigation, toasts). Errors are fields in State or Events |
| Navigation | Type-safe Compose Navigation with `@Serializable` routes (ADR-0005); session-driven start (ADR-0006, §7) |
| DI | Koin: `sharedModule` plus platform modules composed at startup (ADR-0003). No singleton shortcuts |
| Platform capability | A contract in `core/platform`. Use `expect`/`actual` for small pure-Kotlin seams; use an interface implemented in `androidMain`/`iosMain`, or in Swift and injected through Koin from `umbrella`, when an SDK is involved (ADR-0008) |
| Local persistence | Firestore offline persistence behind repository interfaces (ADR-0022, `LocalData.md`); preferences and drafts in DataStore. Room only for a dataset that hits a trigger in `LocalData.md` §8, via a new ADR |
| Runtime config | Firebase Remote Config behind a `RemoteConfig` contract in `core/platform`, every key with a safe in-app default (ADR-0024); values the server enforces (e.g. the rewarded-ad cap) are read by functions from the same source, never trusted from the client |
| Remote | GitLive Firebase, called only from `firebaseData` (ADR-0002, ADR-0021) |
| Errors | Repositories return `Result<T>` with a typed `AppError`; nothing is swallowed (§6) |
| Backend compute | Cloud Functions (ADR-0015). Clients never compute counters, ranks, entitlements or unlocks |
| Purchases | RevenueCat behind `PurchasesClient`. Entitlements are mirrored server-side to `user_entitlements/{uid}` (ADR-0016) |
| Rewarded ads | AdMob behind `RewardedAdProvider`; the server-side-verification callback issues a single-use token (ADR-0017) |
| Push | Token in `users/{uid}/devices`; payloads are built by functions (ADR-0015) |

**Naming:**
- `[Feature]Screen`, `[Feature]ViewModel`, `[Feature]State` (State/Action/Event live together when small).
- `[Name]Repository` (domain interface) and `[Name]RepositoryImpl` (data / firebaseData).
- Mappers: `[Name]LocalMapper` / `[Name]RemoteMapper`.

## 6) Error handling

- Every failure path is a typed outcome (`Result`, sealed errors).
- A `catch` either produces a State or Event the UI can render, or emits a structured observability event (`Observability.md`).
- Never use `printStackTrace()`, an empty catch, or a log-and-continue that changes no state.
- Any unexpected error has a visible answer to "what does the user see?"
- Security checks fail closed.

## 7) Navigation

- **Session → start destination:**
  - `Loading` → splash gate;
  - `SignedOut` → auth (Google / Apple / "Continue without account");
  - `Active(anonymous | linked)` → map-first root (ADR-0018).
- **Root tabs:** Map (default) / Social / Rank / Profile (ADR-0020). Log Fart is a modal launched from the map FAB. Account linking happens inside the Active session and leaves the session, uid and back stack unchanged.
- **Navigation calls:** ViewModels emit navigation Events and the UI calls the navigator. Never navigate from repositories or domain code.
- **Back stack:** auth completion clears the auth graph; sign-out clears protected graphs.
- **Deep links:** they map to typed routes and check eligibility. Phase 1: `fartmap://fart/{fartId}` opens the Map tab with the marker sheet.
- **Bad input:** invalid route arguments lead to a safe fallback, never a crash.
- **Required tests:** session → start mapping (including anonymous); anonymous → linked keeps the back stack; auth and sign-out back-stack clearing; route serialization.

## 8) Offline-first and data flow

**Invariants:**
- Reads render local state immediately; remote updates arrive in the background.
- User writes succeed locally and are never lost to connectivity.
- There is exactly **one** remote write path.
- Online-only operations (purchases, ad rewards, account linking, callable functions) are never queued; they fail fast with a typed "needs connection" error.
- The user never sees "no connectivity" as a failure. Only terminal failures that need user action surface as typed UI state.

**Conflicts:**
- Server timestamps order writes.
- The user's own content is last-write-wins.
- Remote wins for others' content.
- On visibility or safety ambiguity, the restrictive state wins.
- Soft removals propagate and are hidden from read models.

**Idempotency:** deterministic document ids. Reactions, follows and blocks use composite keys.

**Mechanism (ADR-0022):** Firestore's persistent cache and its durable write queue. Client-generated ids, one batch per user action, the commit launched in app scope and never awaited on the UI path (GitLive `set()` suspends until the server acknowledges), rejections reported by `PendingWriteMonitor`. Full contract: `LocalData.md`.

**Canonical flows:**
1. **Log a fart.** Screen Action → ViewModel validates → repository `addFart(fart)`. One batch (public doc with fuzzed coordinates + `farts/{id}/private/location` with the precise ones, ADR-0019) is launched in app scope; the cache shows the marker at once (`hasPendingWrites`) and Firestore delivers it when online.
2. **Map.** ViewModel observes `observeMapFarts(viewport, filter)`: a snapshot listener on geohash ranges that emits from cache first, then server deltas (including removals).
3. **Profile.** Cached profile renders first and remote refreshes it. Show an error only when there is no cache.
4. **Error.** `Result.Failure(AppError)` → State error plus an observability event.

## 9) Multiplatform rules

- Shared by default; platform code only where a platform API forces it.
- Contracts expose domain-safe types, never SDK types. No platform branching inside shared business code.
- **Platform capabilities from the start:** map rendering (Google Maps on Android, MapKit on iOS, one contract), location, native auth providers, audio, push token, secure storage, rewarded ads + UMP, purchases, App Check, Crashlytics initialization.
- **Parity:** a feature is not done until it works on Android and iOS. Known gaps are tracked defects. UX may follow platform conventions while behavior stays identical.

## 10) Testing

| Layer | What to test |
|---|---|
| Domain | Every rule and validation (fastest, highest coverage) |
| Data | Repository contracts with fakes; field round-trips, with the `visibility` regression test from day one |
| Presentation | ViewModel state machines: Actions in, State/Event out. No UI rendering |
| Backend | Rules tests for every allowed and denied case (emulator); function tests |
| Integration | Phase 1 critical path: map → log → persist → marker |
| Platform | Contract behavior only, not SDK behavior |

**Never:**
- test third-party SDKs;
- mock the class under test;
- write order-dependent tests;
- chase coverage percentages in place of behavior coverage.

## 11) Security architecture (summary; details in `Security.md`)

- No secrets in git; configs are injected (`scripts/inject-config.sh`).
- Privacy by default:
  - visibility default: PUBLIC in Phase 1 for the 18+ age band, FOLLOWERS for 13–17 and for everyone once SOCIAL-03 ships (ADR-0023); last-used thereafter;
  - unknown values map to PRIVATE;
  - precise coordinates never reach presentation types for content the user does not own.
- Least privilege for permissions and rules.
- The server is authoritative: every security-relevant rule has rules/functions enforcement, and client checks are UX only.

## 12) Deferred decisions and their triggers

| Decision | Trigger |
|---|---|
| Multi-module feature extraction | Clean build over 3 min, or more than 3 agents working concurrently on separate features. Extraction must be mechanical because features never import each other |
| Non-Firebase backend / API layer | A Firebase limitation blocks a requirement, or cost at Phase 4 scale |
| CDN / media pipeline | Media storage cost becomes material |
| Event sourcing, real-time collaboration, web client, custom notification backend, data warehouse | An explicit product requirement in a later phase |

## 13) Review checklist (automated where marked)

- [ ] Code is in the right feature or `core` package; no feature → feature imports *(Konsist)*
- [ ] Domain imports nothing from data/presentation/platform/Room/Firebase *(Konsist)*
- [ ] Firebase is only in `firebaseData` *(Gradle: `shared` has no GitLive dependency)*
- [ ] No swallowed errors *(detekt)*; every error case has a State or Event
- [ ] All screens use StateViewModel; one-time effects are Events
- [ ] Visibility default follows ADR-0023 (Phase 1: PUBLIC for 18+, FOLLOWERS for 13–17; last-used thereafter); no precise coordinates in public docs or presentation types for others' content
- [ ] Non-free cosmetics need a server-verified entitlement or token; the client never grants unlocks
- [ ] Any new pattern has an ADR

# Architecture: Fart Map

> **Planning spine position:** master architectural blueprint; the highest-authority document in the engineering design hierarchy below `ArchitecturePrinciples.md`.
> Upstream: `ArchitecturePrinciples.md`, all product layer documents, all 14 ADRs, PoC audit findings.
> Downstream: every document in `planning/architecture/`, every module in every implementation codebase.
> **When any downstream architecture document and this document conflict, this document wins.**
> This document defines system shape, architectural boundaries, module ownership, dependency direction, and data flow. It does not define implementation details, database schemas, Firebase collection structures, or sync algorithms. Those live in downstream documents.

---

## Contents

1. [Purpose](#1-purpose)
2. [Architectural Goals](#2-architectural-goals)
3. [System Overview](#3-system-overview)
4. [Architectural Style](#4-architectural-style)
5. [Layer Model](#5-layer-model)
6. [Module Strategy](#6-module-strategy)
7. [Dependency Rules](#7-dependency-rules)
8. [Canonical Data Flow](#8-canonical-data-flow)
9. [Offline-First Architecture](#9-offline-first-architecture)
10. [AI-Agent Optimization](#10-ai-agent-optimization)
11. [Fixed Decisions](#11-fixed-decisions)
12. [Deferred Decisions](#12-deferred-decisions)
13. [Architecture Decision Guidance](#13-architecture-decision-guidance)
14. [Validation Checklist](#14-validation-checklist)

---

## 1) Purpose

### Why this architecture exists

Software architecture is the set of decisions that are hard to change later. Every other decision in the system can be revised — a bug can be fixed, a UI can be redesigned, a feature can be cut. But architectural decisions compound: each new feature is built on top of the architectural foundation, and a broken foundation makes every feature more expensive to build, test, and maintain.

Fart Map has two properties that make its architectural foundation unusually important:

**It will be built primarily by AI agents.** An AI agent has no implicit context. It reads the code that exists, infers the patterns, and contributes accordingly. A codebase with inconsistent patterns or unclear boundaries produces inconsistent contributions — not because agents are careless, but because ambiguity is resolved by inference, and inference at scale produces drift. This architecture is designed to make the correct pattern for any concern unambiguous before an agent writes a single line.

**It already has a PoC with known failures.** The proof-of-concept demonstrated what happens when architecture is assumed rather than specified: a domain model that imported a data entity, a visibility field that was silently dropped in persistence, two sync paths with no reconciliation policy, a committed API key, errors swallowed without observable state. None of these failures were caused by careless engineering — they were caused by the absence of a written system. This document is that system.

### What business problems it solves

The architecture must deliver a product that:

- Lets any user log a fart and see it on the map within seconds, with or without network connectivity
- Shows a living map populated by the community's content, filtered by social graph
- Supports a social graph, reaction/comment system, and notification layer
- Tracks user progression, streaks, and competitive standing
- Enforces privacy-by-default, with user opt-in to exposure
- Scales from Phase 1 (single core loop) to Phase 4 (community systems) without a rewrite
- Monetizes through cosmetics only, with all core capabilities permanently free

### What engineering problems it solves

- **Predictability:** every engineer (human or AI) knows where code belongs before they write it
- **Testability:** every layer can be tested in isolation without standing up the full stack
- **Offline resilience:** user actions succeed regardless of network state, with no data loss
- **Security by structure:** secrets management and privacy defaults are architectural, not policy
- **Phase-appropriate complexity:** the architecture starts simple and has defined evolution triggers

---

## 2) Architectural Goals

These goals govern all structural decisions in the system. They are in priority order. When goals conflict, the higher-priority goal wins unless a documented exception exists.

### Goal 1 — Correctness of behavior

The system must do exactly what the product specification says it does. State round-trips faithfully through all layers. A field written to persistence is read back unchanged. A user action produces the documented outcome every time, not most of the time.

**Architectural consequence:** All layer boundaries are explicit and tested. Mapping between representations is owned by a single layer. No field silently drops at a boundary. The PoC visibility failure — a field present in the domain model, missing from the persistence mapping — is the canonical wrong answer. The architecture prevents this class of failure through explicit mapping ownership and mandatory round-trip testing.

---

### Goal 2 — Predictable structure for AI agents

The architecture must be structured so that a future AI agent with no prior context, after reading the relevant planning documents, knows exactly where to place new code, what pattern to follow, and what contracts to honor. No inference. No guessing. One correct answer per concern.

**Architectural consequence:** One pattern per architectural concern. Feature boundaries are explicit. Every structural rule is written down, not assumed. The package structure mirrors the conceptual architecture — an agent reading the directory layout should understand the system before reading any code.

---

### Goal 3 — Offline resilience by default

Every user-initiated action that produces a content change must be possible without a live network connection. The application queues operations and synchronizes when connectivity returns. Offline is the default mode; online is an optimization.

**Architectural consequence:** The local data store is the single source of truth for all reads. Writes go to the local store first, always. Remote synchronization is a background concern, not a prerequisite for user action. The sync system is its own architectural layer with a documented contract.

---

### Goal 4 — Testability at every layer

Each layer must be independently testable. Domain logic is testable with no platform or persistence dependencies. Repository behavior is verifiable without a running backend. ViewModel state transitions are verifiable without a running UI.

**Architectural consequence:** Hard layer separation is not optional — it is what makes layer-level testing possible. Domain interfaces live in the domain layer; data implementations implement them. No layer holds its dependencies directly; dependencies are injected. A layer that cannot be tested in isolation is a layer whose boundaries are not clean.

---

### Goal 5 — Feature isolation

Features are self-contained. Adding a new feature does not require modifying the core infrastructure of an existing feature. Removing a feature does not leave orphaned dependencies. Features communicate through well-defined, documented contracts, not through direct imports of each other's internals.

**Architectural consequence:** The package structure groups code by feature, not by layer type. Each feature owns its own presentation, domain, and data concerns. Shared infrastructure lives in explicitly owned core packages. Cross-feature dependencies are forbidden except through the core packages.

---

### Goal 6 — Privacy and security by structure

Privacy defaults and security controls must be enforced architecturally — through data model defaults, server-side rules, and layer boundaries — not through developer policy that can be forgotten or bypassed.

**Architectural consequence:** Default field values in entity definitions reflect the restrictive default (e.g., visibility defaults to `FOLLOWERS`, not `PUBLIC`). Precise location coordinates are never passed to the presentation layer — coarse coordinates are a data layer responsibility. Firebase security rules are the authoritative access control enforcement point, not client-side checks. Secrets have no valid path into version control — by structure, not by convention.

---

### Goal 7 — Phase-appropriate complexity

The architecture for Phase 1 must be correct and maintainable for Phase 1. It must be extensible to Phase 2 without a rewrite. It must not be designed for Phase 4 scale at Phase 1 launch.

**Architectural consequence:** The starting structure is a single shared module with explicit feature packages. Multi-module extraction is a defined future evolution, triggered by measurable build time or team-scale thresholds, not by speculation. The package boundaries are drawn so that extraction is possible when the trigger arrives — not pre-extracted before the trigger exists.

---

### Goal 8 — Consistent state management

User-visible state must flow in one direction. Every state transition must be observable and typed. Errors are first-class state values, not exceptions that disappear into a log.

**Architectural consequence:** A single state management pattern is used for all screens (ADR-0004 StateViewModel). The pattern is not varied per screen. Errors produce typed error states, not console output. The ViewModel is the single source of truth for UI state.

---

## 3) System Overview

### The three system domains

Fart Map divides into three domains that must be understood separately before they are connected:

```
┌─────────────────────────────────────────────────────────────────┐
│                     CLIENT APPLICATIONS                          │
│                                                                  │
│    ┌──────────────────────┐   ┌──────────────────────────┐      │
│    │     Android App       │   │         iOS App           │      │
│    │  (platform-specific   │   │  (platform-specific       │      │
│    │   shell + bindings)   │   │   shell + bindings)       │      │
│    └──────────┬───────────┘   └───────────┬──────────────┘      │
│               │                           │                      │
│               └────────────┬──────────────┘                      │
│                            ▼                                     │
│           ┌────────────────────────────────┐                     │
│           │       SHARED KMP MODULE        │                     │
│           │                                │                     │
│           │  ┌────────────────────────┐    │                     │
│           │  │   Feature Packages     │    │                     │
│           │  │  (map, log, profile,   │    │                     │
│           │  │   auth, social, ...)   │    │                     │
│           │  └────────────────────────┘    │                     │
│           │  ┌────────────────────────┐    │                     │
│           │  │    Core Packages       │    │                     │
│           │  │  (navigation, design,  │    │                     │
│           │  │   di, observability,   │    │                     │
│           │  │   sync engine, ...)    │    │                     │
│           │  └────────────────────────┘    │                     │
│           └────────────────────────────────┘                     │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴──────────────┐
              ▼                              ▼
┌─────────────────────────┐   ┌─────────────────────────────────┐
│   LOCAL PERSISTENCE      │   │        CLOUD SERVICES            │
│                          │   │                                  │
│  Room Database           │   │  Firestore (document store)      │
│  DataStore (preferences) │   │  Firebase Auth (identity)        │
│  Pending Operations      │   │  Firebase Storage (media)        │
│  Queue (offline writes)  │   │  Firebase Analytics              │
│                          │   │  Remote Notifications (FCM/APNs) │
└─────────────────────────┘   └─────────────────────────────────┘
```

### Client applications

Two platform-specific application shells exist — one for Android, one for iOS. Each shell is minimal: it initializes the application, sets up the dependency injection container, binds platform-specific implementations to shared interfaces, and hosts the shared UI composition. No product logic lives in a platform shell. No routing decisions are made in a platform shell. If a behavior is not platform-forced, it belongs in the shared module.

### Shared KMP module

All product logic lives here. This is the architecture's center of gravity. It contains:

- **Feature packages:** all code specific to a product feature (presentation, domain, data)
- **Core packages:** all code shared across features (navigation, design system, DI wiring, sync engine, observability, shared domain types)

The shared module is the single place where product decisions are implemented. Platform differences are expressed through `expect`/`actual` declarations — the shared module declares the interface; the platform shells provide the implementation.

### Local persistence

The device-local data store is the authoritative source of truth for all reads. It contains the current state of all entities the user has interacted with, plus a queue of pending write operations that have not yet been synchronized to the remote. All reads go to local storage first. The remote is never consulted for a read that local storage can satisfy.

### Cloud services

Firebase provides identity, document storage, media storage, analytics, push notifications, crash reporting, app attestation and server-side compute (Cloud Functions, ADR-0015). Two external services are integrated through the platform and data layers only: RevenueCat for purchases and entitlements (ADR-0016) and AdMob for rewarded ads (ADR-0017); their server-side callbacks land in Cloud Functions. Firebase is the authoritative source for canonical state — local storage is a cache of what Firestore contains, plus pending operations not yet sent. The sync engine is responsible for reconciling the two.

---

## 4) Architectural Style

Fart Map uses four complementary architectural patterns. Each is chosen to solve a specific problem. They are not alternatives to each other — they operate at different levels of abstraction and together define the complete architectural style.

### Pattern 1 — Feature-First Package Organization

**What it is:** Code is organized by product feature, not by architectural layer type. A feature package contains all code that belongs to that feature: its screens, its ViewModels, its domain entities, its use cases, its repository interfaces, its repository implementations, its local entities, and its remote data types.

**Why it was chosen:** Layer-first organization (a flat `domain/`, `data/`, `ui/` at the top level) works for small applications but degrades as the feature count grows. Finding all code related to the "follow a user" feature requires navigating across three or more top-level packages. Feature-first organization makes features self-contained: an agent implementing a feature reads one package, not three scattered packages.

The PoC used layer-first organization. It worked for five screens. At thirty screens (the full feature set), it would require significant cross-navigation to understand any single feature.

**What was rejected:** Pure layer-first organization (flat `data/`, `domain/`, `ui/` at root). Rejected because it scales poorly and forces agents to work across multiple directories to understand a single feature.

---

### Pattern 2 — Clean Architecture (Adapted)

**What it is:** The system is organized into discrete layers with a strict dependency direction. The domain layer defines what the system does (business entities, business rules, repository contracts). The data layer implements how the system persists and synchronizes (Room entities, Firestore documents, repository implementations). The presentation layer renders what the user sees and dispatches what the user does. No layer depends on the layer above it. The domain layer depends on nothing inside the project.

**Why it was chosen:** Clean Architecture creates independently testable layers. The domain layer can be tested with no database, no Firebase connection, and no platform SDK. Repository contracts can be tested with in-memory fakes instead of a real database. ViewModel logic can be tested without a running UI. This is the architectural foundation that makes the testability goal achievable.

The PoC violated clean architecture by having the domain model (`Fart.kt`) import a data layer entity (`FartEntity`) to perform mapping. This single violation made the domain layer untestable in isolation and created a dependency direction reversal. Clean Architecture prevents this class of error by making the rule explicit: the domain layer imports nothing from the data layer.

**Adaptation from classical Clean Architecture:** Use cases are not mandatory wrappers for every repository call. A use case is created when it contains domain logic that would otherwise be inappropriate in a ViewModel or repository. Empty delegation use cases are prohibited (PoC Anti-Pattern 8: Premature Abstraction). Repository interfaces are defined in the domain layer and implemented in the data layer; the domain layer does not know about the implementation.

**What was rejected:** Transaction Script (business logic scattered across ViewModels and repositories), anemic domain model (entities with no behavior), active record pattern (entities that know how to persist themselves).

---

### Pattern 3 — Repository Pattern

**What it is:** All access to data — local or remote — is mediated through a repository. A repository is the single gateway between the domain layer and any persistence or network concern. It exposes domain entities and reactive streams; it consumes local storage and remote APIs. No code outside the data layer reads from Room directly. No code outside the data layer calls Firebase directly.

**Why it was chosen:** The repository pattern is the boundary that enforces the Clean Architecture rule in practice. By requiring all data access to go through a repository, the pattern ensures that:
- The domain layer remains free of persistence details
- The presentation layer never imports a database entity or a Firestore document
- The sync strategy is an internal concern of the repository, not visible to its consumers
- Swapping or augmenting a data source (e.g., adding an in-memory cache, changing a Firestore query) does not require changes outside the data layer

**What was rejected:** Direct DAO access from ViewModels, direct Firestore calls from screens, active record pattern.

---

### Pattern 4 — Unidirectional Data Flow with Typed State

**What it is:** Every screen in the application is driven by a ViewModel that exposes a single immutable `State` object, accepts typed `Action` values, and emits typed `Event` values for one-time effects. State flows down from the ViewModel to the UI. Actions flow up from the UI to the ViewModel. Events flow from the ViewModel to the UI for navigation and ephemeral effects. State never flows sideways between ViewModels.

**Why it was chosen:** Unidirectional data flow makes state transitions deterministic and auditable. Given a starting `State` and a sequence of `Action` values, the resulting `State` is reproducible. This makes ViewModel behavior testable without a running UI and makes bugs reproducible without a full session replay.

The PoC already used this pattern (`EventStateViewModel` base class with `State`/`Action`/`Event` types) and it worked well. The pattern is preserved and formalized. The PoC failure was not in the pattern itself — it was in error handling within the pattern (`printStackTrace()` instead of typed error states). The new system uses the same pattern with the error handling fixed.

**What was rejected:** Two-way data binding, shared mutable state between screens, direct LiveData + imperative UI updates, state held in the UI layer.

---

## 5) Layer Model

The system is organized into five layers. Each layer has a defined responsibility, an explicit set of allowed and forbidden dependencies, and a clear ownership boundary. Code that belongs in one layer must not appear in another.

```
┌─────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                     │
│   Screens · ViewModels · UI State · Navigation Actions   │
└──────────────────────┬──────────────────────────────────┘
                       │ observes / dispatches
                       ▼
┌─────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                         │
│  Business Entities · Use Cases · Repository Interfaces   │
│                 (imports nothing inside)                 │
└───────────┬─────────────────────────────────────────────┘
            │ implements                    ▲ uses interfaces
            ▼                              │
┌───────────────────────────────────────────────────────────┐
│                      DATA LAYER                            │
│  Repository Implementations · Local Entities · Sync Engine│
│      DTOs · Mapping · Pending Operations Queue             │
└─────────────────────────────┬─────────────────────────────┘
                              │ uses platform contracts
                              ▼
┌─────────────────────────────────────────────────────────┐
│                   PLATFORM LAYER                         │
│  expect/actual implementations: maps, location, auth,    │
│       notifications, camera, file system, logging        │
└──────────────────────────┬──────────────────────────────┘
                           │ initializes
                           ▼
┌─────────────────────────────────────────────────────────┐
│                INFRASTRUCTURE LAYER                      │
│  DI initialization · SDK configuration · Secrets loading │
│        Analytics init · Application lifecycle            │
└─────────────────────────────────────────────────────────┘
```

Dependencies point downward. No arrow may be reversed. No layer imports from a layer above it.

---

### Presentation Layer

**Responsibility:** Render UI state. Dispatch user actions. Observe ViewModel state. Trigger navigation. Produce one-time UI effects (navigate, show a toast, dismiss a dialog, play a sound). Request platform permissions.

**Allowed dependencies:**
- Domain layer entities and domain layer types (read-only, for displaying state)
- ViewModel (through dependency injection, for state observation and action dispatch)
- Navigation destinations (route types only, not navigation implementation internals)
- Design system components

**Forbidden dependencies:**
- Data layer entities (Room entities, Firestore DTOs, pending operation models)
- Repository implementations (or their interfaces — repositories are accessed only through use cases or ViewModels)
- Platform-specific SDKs beyond what is required for rendering (no direct Firebase calls, no direct Room access)
- Other features' ViewModels (state is not shared between features through direct ViewModel references)

**Ownership:** Every screen in a feature package owns its own composable file, ViewModel, State class, Action sealed interface, and Event sealed class. The naming convention is `[Feature]Screen.kt`, `[Feature]ViewModel.kt`, `[Feature]State.kt`. The State, Action, and Event types are co-located in one file when they are small, in a dedicated `[Feature]State.kt` file when they are substantive.

**Examples:** `MapScreen`, `LogFartScreen`, `ProfileScreen`, `LoginScreen`, `FeedScreen`

---

### Domain Layer

**Responsibility:** Define business entities. Define business rules and validation logic. Define repository contracts (interfaces) that the data layer must implement. Express what the system does, with no concern for how it persists or transmits data.

**Allowed dependencies:**
- Kotlin standard library
- `kotlinx-datetime` for temporal types
- `kotlinx-serialization` annotations on entities that need cross-layer transport
- Other domain entities within the same feature (not across feature boundaries except through shared core domain types)

**Forbidden dependencies:**
- Data layer entities or implementations (the PoC violation: `Fart.kt` importing `FartEntity`)
- Room annotations or imports
- Firebase imports or SDK types
- Platform-specific types (Android SDK, iOS UIKit)
- Presentation layer types
- Any other project layer

**Ownership:** Repository interfaces are defined here, not in the data layer. Mapping between domain entities and data entities is the data layer's responsibility, implemented at the data layer boundary — never in domain classes. Use cases exist only when they contain domain logic; empty delegation use cases are not created.

**Critical rule:** The domain layer is the center of the architecture. Both the presentation layer and the data layer depend on it. It depends on neither. Any import from within the project that originates in the domain layer is a violation.

**Examples:** `Fart` entity, `FartRepository` interface, `User` entity, `FollowRelationship` entity, `AddFartUseCase`, `ResolveFeedUseCase`, `ValidateFartSubmissionUseCase`

---

### Data Layer

**Responsibility:** Implement repository interfaces defined by the domain layer. Own all persistence logic (local database entities, DAOs, queries). Own all remote communication (Firestore document reading and writing, Firebase Storage operations). Own the sync engine (pending operation queue, retry logic, conflict resolution). Own all mapping between domain entities and persistence/transport representations.

**Allowed dependencies:**
- Domain layer interfaces and entities (to implement contracts and produce/consume domain types)
- Local persistence SDK (Room) — internal to the data layer; domain layer never sees Room types
- Remote SDK (Firebase via GitLive) — internal to the data layer; domain layer never sees Firebase types
- Platform layer interfaces (for network access, file system operations)
- `kotlinx.coroutines` for reactive streams

**Forbidden dependencies:**
- Presentation layer
- ViewModel types
- Navigation types
- Design system components
- Other features' data layer internals (cross-feature data access must go through repository interfaces, not direct DAO or Firestore collection access)

**Ownership:** Each feature's data layer owns its local entities, DAOs, Firestore collection naming, repository implementation, and all mapping functions. Mapping from domain entity to local entity and from local entity to domain entity lives here — never in the domain entity itself. The sync engine is a shared data layer concern (in `core/data/`) that all feature repositories use through a common interface.

**Examples:** `FartRepositoryImpl`, `FartEntity` (Room entity), `FartDto` (Firestore document model), `FartDao`, `PendingOperationQueue`, `SyncEngine`

---

### Platform Layer

**Responsibility:** Provide platform-specific implementations of shared contracts. Abstract platform capabilities — native map rendering, location services, platform authentication flows, camera access, microphone recording, native file access, platform logging — behind shared interfaces defined in the shared module. The shared module sees only the interface; the platform module provides the implementation.

**Allowed dependencies:**
- Shared module `expect` declarations (fulfilling the `actual` contract)
- Platform-specific SDKs (Android: Google Maps Compose, Google Play Services Location, Credential Manager; iOS: MapKit/UIKit map, Core Location, ASAuthorizationController)
- Domain layer types (to produce domain-typed results from platform operations)

**Forbidden dependencies:**
- Data layer implementations
- Presentation layer composables
- ViewModel types
- Business logic (a platform implementation is an adapter, not a decision-maker)

**Ownership:** Every `expect` declaration has exactly one `actual` per supported platform. Platform-specific code is the only code that may import platform SDK types. Any shared module code that needs platform behavior declares an `expect` function or interface; it never conditionally imports platform types.

**Examples:** `PlatformLocationProvider` (expect/actual), `PlatformMapView` (expect/actual), `GoogleAuthProvider` (Android actual), `AppleAuthProvider` (iOS actual), `PlatformLogger` (expect/actual), `AudioRecorder` (expect/actual), `RewardedAdProvider` (expect/actual, ADR-0017), `PurchasesClient` (expect/actual over RevenueCat, ADR-0016), `PushTokenProvider` (expect/actual), `AppCheckProvider` (expect/actual)

---

### Infrastructure Layer

**Responsibility:** Initialize and configure SDKs. Load secrets from environment-provided sources. Configure crash reporting and analytics. Initialize the dependency injection container. Manage application-level lifecycle concerns (app startup, background execution limits, connectivity monitoring).

**Allowed dependencies:**
- All other layers (for wiring)
- Platform SDKs for initialization
- Secrets management mechanism (environment variables, BuildConfig, platform keychain)

**Forbidden dependencies:**
- Product logic (no business rules live here)
- UI state or navigation
- Domain entities

**Ownership:** DI module definitions live in the infrastructure layer (organized in `core/di/` in the shared module, with platform-specific additions in platform modules). SDK configuration (Firebase options, analytics configuration) is infrastructure. Application entry points (`Application` subclass on Android, `@main` on iOS) are infrastructure shells.

**Examples:** `startKoin(...)`, `platformModule`, `sharedModule`, `AppLogger`, `FirebaseInitializer`

---

## 6) Module Strategy

### Starting structure: one shared codebase in three Gradle modules

The new project starts as one shared Kotlin/Compose Multiplatform codebase plus two minimal platform shells. The shared code is split into three Gradle modules for one reason only, Firebase isolation (ADR-0021): `shared` (features, `core/*`, repository interfaces, sync engine, Compose UI; no Firebase SDK types), `firebaseData` (GitLive-based implementations of the `shared` contracts) and `umbrella` (iOS-only; depends on both and produces the `Shared` framework consumed by `iosApp`). `androidApp` depends on `shared` and `firebaseData`. Everything below describes the package layout inside `shared`; `firebaseData` mirrors the `core/data` and feature `data` packages it implements.

The justification for single-module start:
- AI agents work more efficiently in a coherent codebase without multi-module navigation overhead
- Phase 1–2 has a manageable feature count (map, log, auth, profile, basic social)
- The build performance benefit of multi-module (parallel compilation) does not materialize until the module count and code volume are significantly larger
- Package boundaries can be drawn to enable future extraction without the cost of pre-extraction

The multi-module evolution trigger is defined in §12 (Deferred Decisions).

### Package structure

Within the single shared module, code is organized into two top-level categories: **feature packages** and **core packages**. The division is: if code is specific to one product feature, it belongs in that feature's package. If code is shared across two or more features, it belongs in a core package.

```
shared/src/commonMain/kotlin/com/ahmetbozkan/fartmap/
│
├── feature/                        ← feature-first grouping
│   ├── map/                        ← MAP-01 through MAP-05 (Features.md)
│   │   ├── presentation/           ← screens, viewmodels, state/action/event
│   │   ├── domain/                 ← map-specific entities, use cases, repository interfaces
│   │   └── data/                   ← map-specific entities, DAOs, repository implementations
│   │
│   ├── log/                        ← CREATE-01 through CREATE-08
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   │
│   ├── auth/                       ← AUTH-01 (login, session)
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   │
│   ├── profile/                    ← PROFILE-01 through PROFILE-05
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   │
│   ├── social/                     ← SOCIAL-01 through SOCIAL-05 (Phase 2)
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   │
│   ├── progression/                ← PROG-01 through PROG-05, ACH-01/02 (Phase 2–3)
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   │
│   ├── monetization/              ← MON-01 a la carte + MON-03 rewarded ads (Phase 1), MON-02 (Phase 4)
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   │
│   └── notifications/             ← NOTIF-01 through NOTIF-03 (Phase 1: reaction push + deep link)
│       ├── presentation/
│       ├── domain/
│       └── data/
│
└── core/                           ← shared infrastructure
    ├── domain/                     ← shared domain types (User, Location, FartVisibility, etc.)
    ├── data/                       ← shared data infrastructure (database, sync engine, pending ops)
    ├── navigation/                 ← nav graphs, destinations, nav host
    ├── design/                     ← design system components, theme, typography
    ├── di/                         ← Koin module definitions
    ├── platform/                   ← expect declarations: map, location, auth providers, audio, push token, ads, purchases, app check (actuals in platform modules)
    └── observability/              ← Result type, error types, logging, analytics event contracts
```

Platform-specific modules:

```
shared/src/androidMain/kotlin/com/ahmetbozkan/fartmap/
└── platform/                       ← Android actual implementations

shared/src/iosMain/kotlin/com/ahmetbozkan/fartmap/
└── platform/                       ← iOS actual implementations

firebaseData/src/{commonMain,androidMain,iosMain}/kotlin/com/ahmetbozkan/fartmap/firebase/   ← GitLive adapters (ADR-0021)
umbrella/src/iosMain/kotlin/com/ahmetbozkan/fartmap/                                    ← MainViewController, iOS Koin composition
```

### Cross-feature dependency rule

Feature packages must not import from other feature packages. Features communicate through core packages only. If Feature A needs a type defined by Feature B, that type belongs in `core/domain/`, not in `feature/b/domain/`.

This rule enables future module extraction: a feature package with no cross-feature imports can be extracted to its own module without untangling dependencies.

### Evolution path

When the multi-module trigger fires (see §12), the extraction order follows Phase alignment:

1. `core/*` packages → shared library modules
2. `feature/map` and `feature/log` → first feature modules (most stable, Phase 1)
3. `feature/social` → social module (Phase 2, higher churn during development)
4. `feature/progression` and `feature/monetization` → later extraction

---

## 7) Dependency Rules

These rules are architectural law. Violations are not style issues or debt — they are defects that degrade every downstream contribution to the codebase.

### Allowed dependencies

| From | May import | Rationale |
|---|---|---|
| `feature/*/presentation` | `feature/*/domain` entities and interfaces | Domain types are the presentation layer's data vocabulary |
| `feature/*/presentation` | `core/domain` shared types | User, Location, and other shared domain types |
| `feature/*/presentation` | `core/navigation` destinations | For type-safe navigation actions |
| `feature/*/presentation` | `core/design` | UI components |
| `feature/*/presentation` | `core/observability` error types | For typed error state rendering |
| `feature/*/domain` | `core/domain` | Shared entities referenced by feature domain types |
| `feature/*/data` | `feature/*/domain` | Implementing repository interfaces, producing domain entities |
| `feature/*/data` | `core/data` | Using sync engine, database infrastructure |
| `core/data` | `core/domain` | Data infrastructure uses shared domain types |
| `core/platform` (expect) | `core/domain` | Platform contracts use domain types |
| Platform `actual` implementations | Platform-specific SDKs | Platform layer is the only place SDK types appear |
| `core/di` | All layers | DI module wires the entire graph |
| Infrastructure (app shell) | `core/di` | Entry point starts the DI container |

### Forbidden dependencies (hard violations)

| Forbidden pattern | Reason | PoC example |
|---|---|---|
| `feature/*/domain` imports `feature/*/data` | Domain must not know about persistence implementations | `Fart.kt` imported `FartEntity` |
| `feature/*/presentation` imports `feature/*/data` | Screens must not see database entities or DTOs | Would couple UI to persistence technology |
| Any feature package imports another feature package | Feature isolation — cross-feature means shared domain type | N/A in PoC but a common drift pattern |
| `feature/*/domain` imports platform SDK types | Domain must be platform-agnostic | N/A in PoC but structurally at risk |
| Any layer calls Firebase or Room directly outside `feature/*/data` or `core/data` | Repository is the only data gateway | PoC: Firebase called directly from `FartRepositoryImpl` in `data/remote/` — tolerable location, but pattern must be centralized |
| `core/platform` (expect declarations) contains business logic | Platform layer is an adapter, not a decision-maker | N/A but a common temptation |
| Infrastructure layer contains product logic | Infrastructure initializes; it does not decide | N/A in PoC |

### The three non-negotiable dependency rules

These three rules are restated here because they cover the most common failure modes:

1. **Domain never imports Data.** Repository interfaces live in domain. Repository implementations live in data. Mapping between domain entities and persistence entities is the data layer's job, performed at the data layer boundary.

2. **Presentation never imports Data.** The only persistence-related type the presentation layer ever sees is a domain entity. It never sees a Room entity, a Firestore document model, or a DTO.

3. **Features never import each other.** Cross-feature data sharing is done through core domain types. Cross-feature navigation is done through core navigation destinations. Direct feature-to-feature imports are always wrong.

---

## 8) Canonical Data Flow

The following examples define the authoritative data flow for the most important system operations. Downstream architecture documents that describe specific implementations must be consistent with these flows.

### Flow 1 — Logging a fart (primary use case)

```
USER taps "Log Fart" button
  │
  ▼
LogFartScreen dispatches OnFartClick action
  │
  ▼
LogFartViewModel.handleAction(OnFartClick)
  │  validates state; constructs Fart domain entity
  ▼
AddFartUseCase.invoke(fart: Fart)
  │  contains submission business logic (validation, draft management)
  ▼
FartRepository.addFart(fart: Fart): Result<FartId>
  │
  ├──► [ALWAYS] Write to local Room database (FartLocalEntity, sync_state = PENDING) and enqueue a PendingOperation
  │      │
  │      ▼
  │    Local DB acknowledges write
  │      │
  │      ▼
  │    FartDao emits updated Flow → ViewModel observes → State updates
  │      │
  │      ▼
  │    Marker appears on map (immediate, before sync)
  │
  └──► [ALWAYS] Request an immediate SyncEngine tick — the single remote write path (SyncStrategy.md §3)
         │
         ├── ONLINE and SUCCESS: SyncEngine writes Firestore; local sync_state = SYNCED
         │
         └── OFFLINE or FAILURE: operation stays queued; SyncEngine retries with backoff
                       (no error shown to user for background sync)

User sees: OFFLINE state toast if no connectivity
           SUCCESS state (marker drop) as soon as local write completes
           No distinction visible to user between local-only and synced states
```

---

### Flow 2 — Loading the map (read path)

```
MapScreen composed / resumed
  │
  ▼
MapViewModel init / resume
  │
  ▼
GetFartsForMapUseCase.observe(): Flow<List<MapMarker>>
  │
  ▼
FartRepository.observeMapFarts(filter: MapFilter): Flow<List<Fart>>
  │
  ├──► Room database query (reactive Flow — UI is always local-first)
  │      │
  │      ▼
  │    Fart entities → domain Fart objects → MapMarker UI models
  │      │
  │      ▼
  │    MapViewModel State.markers updated
  │      │
  │      ▼
  │    MapScreen renders markers
  │
  └──► [BACKGROUND] SyncEngine checks Firestore for new/updated farts
         │
         ▼
       Writes any new remote entities to local Room
         │
         ▼
       Room Flow emits → ViewModel observes → State updates → UI re-renders
       (user sees markers appear as sync completes)
```

---

### Flow 3 — Loading a user profile

```
ProfileScreen receives UserId (via navigation argument)
  │
  ▼
ProfileViewModel.init(userId: UserId)
  │
  ▼
GetProfileUseCase.observe(userId): Flow<UserProfile>
  │
  ▼
UserRepository.observeProfile(userId): Flow<User>
  │
  ├──► Local Room query for cached user
  │      │
  │      ▼
  │    Return cached profile immediately (may be stale)
  │      │
  │      ▼
  │    ViewModel State.profile = cached
  │      │
  │      ▼
  │    Screen renders immediately with cached data
  │
  └──► [BACKGROUND] Fetch from Firestore
         │
         ├── Response: write to local Room; Flow emits; State refreshes
         │
         └── Error: if cache exists, no error shown; if no cache, show error state
```

---

### Flow 4 — Following a user

```
ProfileScreen dispatches FollowUser(targetUserId) action
  │
  ▼
ProfileViewModel.handleAction(FollowUser)
  │
  ▼
RequestFollowUseCase.invoke(targetUserId: UserId): Result<Unit>
  │  contains: blocked user check, existing request check, PENDING state logic
  ▼
FollowRepository.requestFollow(targetUserId): Result<Unit>
  │
  ├──► Write PENDING FollowRelationship to local Room
  │      │
  │      ▼
  │    FollowRelationshipDao emits → ViewModel observes → Profile state shows "Pending"
  │
  ├──► [ALWAYS] Enqueue PendingOperation; SyncEngine creates the Firestore document when online
  │      │
  │      ├── SUCCESS: local sync_state = SYNCED
  │      └── FAILURE: stays queued; SyncEngine retries
  │
  └──► [TARGET USER] receives notification (via server-side trigger)
```

---

### Flow 5 — Error handling (any operation)

```
Any ViewModel action that can fail
  │
  ▼
Use case invocation
  │
  ▼
Repository call → returns Result<T>
  │
  ├── Result.Success → ViewModel processes; State updates to success state
  │
  └── Result.Failure(error: AppError) → ViewModel processes; State updates to error state
        │
        ▼
      Screen observes error state → renders error UI (retry CTA, error message)
        │
        ▼
      Observability layer logs structured event (error category, feature, context)
        │
        NO EXCEPTION IS SWALLOWED. NOTHING IS PRINTED AND DISCARDED.
```

---

## 9) Offline-First Architecture

This section defines the principles governing offline behavior. Implementation details (pending operation schema, retry policy, conflict resolution rules) are defined in `architecture/SyncStrategy.md`.

### Principle 1 — Local store is the source of truth for reads

All reads go to the local database. The UI never waits for a network response to render. If local state exists, it is shown immediately. Remote data enriches and updates local state in the background.

### Principle 2 — Writes always succeed locally

A user action that modifies content (logging a fart, sending a reaction, following a user) writes to the local database first, always. The write to the local database is what the user experiences as success. The remote write is a background operation.

### Principle 3 — Pending operations are the offline contract

When a write cannot be delivered to the remote (no connectivity, transient error, app killed before sync), the operation is stored in the pending operations queue. The queue is durable — it survives app restarts and process death. The sync engine processes the queue when connectivity is available.

### Principle 4 — Eventual consistency, not real-time consistency

The system does not guarantee that local state and remote state are identical at any given moment. It guarantees that they will converge. A user's fart is correct on their device immediately. It becomes visible to others after sync completes.

### Principle 5 — Conflict ownership

When a conflict is detected between a pending local write and a newer remote state, the remote wins for content the user does not own (other users' farts, follow states changed by the other party). The user's own content follows last-write-wins within a session. The conflict policy is fully specified in `architecture/SyncStrategy.md`.

### Principle 6 — The user never sees "no connectivity" as a failure

A submission that queues offline shows a non-blocking acknowledgment, not an error. The queued state is surfaced gently if the user returns to the same content before sync completes. Errors shown to the user are for operations that have exhausted retry attempts with a recoverable user action, not for transient network conditions.

---

## 10) AI-Agent Optimization

This section explains how the architecture is designed to make AI-assisted development reliable. It is not advisory — these properties are requirements that every structural decision must preserve.

### Discoverability: where does this code belong?

The package structure maps directly to the product feature taxonomy in `Features.md`. An agent implementing `CREATE-03 Log Fart` knows immediately that all code belongs in `feature/log/`. An agent implementing `SOCIAL-03 Follow System` knows all code belongs in `feature/social/`. An agent building a shared authentication primitive knows it belongs in `feature/auth/`. An agent adding a cross-feature domain type knows it belongs in `core/domain/`.

No inference is required. The directory structure is a physical expression of the product feature map.

### Predictability: how is this done?

For any architectural concern, there is exactly one answer:

| Concern | Answer |
|---|---|
| Screen state management | `EventStateViewModel` (ADR-0004) |
| Navigation | Type-safe Compose Navigation with serializable routes (ADR-0005) |
| Dependency injection | Koin with `sharedModule` + platform `expect`/`actual` module (ADR-0003) |
| Local persistence | Room with a single shared database per feature (ADR-0001) |
| Remote communication | GitLive Firebase via repository only (ADR-0002) |
| Error handling | `Result<T>` return type; typed error states in ViewModel |
| Cross-platform behavior | `expect`/`actual` declarations in `core/platform/` (ADR-0008) |
| Offline writes | Pending operations queue via SyncEngine |
| Backend compute | Cloud Functions (ADR-0015); clients never compute counters, ranks or entitlements |
| Purchases / entitlements | RevenueCat KMP SDK behind a domain contract; server-mirrored entitlements (ADR-0016) |
| Rewarded ads | AdMob behind `RewardedAdProvider` expect/actual; server-issued single-use tokens (ADR-0017) |
| Push notifications | Platform token registration under `users/{uid}/devices`; payloads built by Cloud Functions (ADR-0015) |

No option paralysis. No "which approach should I use?" A new agent reads these rules once and follows them.

### Ownership boundaries: who owns what?

Every concept has a single owner:

| Concept | Owner |
|---|---|
| What a Fart is (entity) | `feature/log/domain/` |
| How a Fart is stored locally | `feature/log/data/` |
| How a Fart is stored remotely | `feature/log/data/` |
| How a Fart is displayed on the map | `feature/map/presentation/` |
| What the User entity is | `core/domain/` |
| How navigation works | `core/navigation/` |
| How platform location is accessed | `core/platform/` (expect) + platform `actual` |
| How analytics events are fired | `core/observability/` |
| How errors are typed | `core/observability/` |

When two owners seem to have a claim, the rule is: the owning layer is the one closest to where the concern originates. A Fart that appears on the map is still a `feature/log` domain entity — the map feature uses it but does not own it.

### Low coupling: one reason to change

Each feature package has one reason to change: its product feature changes. `feature/log/` changes when Log Fart behavior changes. `feature/map/` changes when map display behavior changes. Core packages change when infrastructure changes. Changes in one feature do not propagate to other features.

### Documentation hierarchy: the chain of authority

Every agent knows where to look for any answer:

```
CLAUDE.md (front door)
  → STATE.md (what to work on now)
  → AgentGuardrails.md (what not to do)
  → ArchitecturePrinciples.md (engineering philosophy)
  → Architecture.md (this document — system shape)
  → [specific architecture doc] (implementation contract for the specific concern)
  → [product doc] (feature specification)
  → [ADR] (settled decision)
```

No document in this hierarchy is optional. An agent that skips a level invents answers that may contradict the level it skipped.

---

## 11) Fixed Decisions

These decisions are settled. They are not re-evaluated without a new ADR presenting new information that materially changes the underlying tradeoffs. Downstream documents implement these decisions; they do not re-litigate them.

| Decision | Rationale | ADR |
|---|---|---|
| Kotlin Multiplatform | Shared business logic across Android and iOS without code duplication | PoC audit — working; justified by platform targets |
| Compose Multiplatform | Shared UI across Android and iOS in a single language | PoC audit — working; enables single design system |
| Feature-first package organization | Scales with feature count; maps to product taxonomy; enables future module extraction | This document (§6) |
| Clean Architecture layer model | Independently testable layers; enforces dependency direction | This document (§5); ArchitecturePrinciples.md |
| Repository pattern | Single data gateway; domain isolation; sync strategy encapsulation | ADR-0001, ADR-0002, ADR-0007 |
| StateViewModel (UDF) pattern | Deterministic state; testable transitions; typed error states | ADR-0004 |
| Type-safe Compose Navigation | Compile-time route safety; no string-based navigation | ADR-0005 |
| Session-driven start destination | Auth state determines entry point without explicit routing logic | ADR-0006 |
| Room for local persistence | KMP support; strong typing; reactive queries | ADR-0001 |
| GitLive Firebase as remote backend | KMP-native Firebase wrapper; Firestore + Auth + Storage in one dependency | ADR-0002 |
| Koin with expect/actual platform modules | KMP-compatible; simple configuration; platform module separation | ADR-0003 |
| expect/actual for all platform-specific code | Explicit boundary; shared module never imports platform SDK types | ADR-0008 |
| Local-first reads with remote backfill | Offline resilience; perceived performance; user trust | ADR-0007 |
| Offline write queueing | User action must never fail because of network state | ADR-0007; this document §9 |
| One shared codebase in three Gradle modules (`shared`, `firebaseData`, `umbrella`) for Phase 1–2 | Agent coherence plus Firebase isolation so shared tests never link Firebase | This document (§6); ADR-0021 |
| Privacy by default | Architectural enforcement of product principle | ArchitecturePrinciples.md §11; ContentSafety.md |
| No secrets in version control | PoC failure; non-negotiable security | ArchitecturePrinciples.md §11; SecurityAndSecrets.md |
| Cloud Functions as backend compute; FCM, Crashlytics, App Check in scope | Server authority for counters, fan-out, entitlements, caps | ADR-0015 |
| RevenueCat for purchases and entitlements | One cross-platform purchases layer with a server-side source of truth | ADR-0016 |
| AdMob rewarded ads with server-side verification | Single-use unlocks cannot be forged by the client | ADR-0017 |
| Anonymous sessions are full sessions | Owner decision; friction-free start with linking | ADR-0018 |
| Precise location owner-only; public projection fuzzed | Firestore has no field-level read rules | ADR-0019 |

---

## 12) Deferred Decisions

These decisions are intentionally not made in this document. They are deferred because the information required to make them well does not yet exist, or because the cost of pre-building does not justify the benefit at current scale.

Each deferred decision has a defined trigger — the condition that makes the decision necessary. When the trigger fires, the decision is made and documented in an ADR before implementation.

| Decision | Why deferred | Trigger |
|---|---|---|
| **Multi-module extraction** | No build performance benefit at Phase 1–2 scale; adds navigation complexity before justified | Build time exceeds 3 minutes clean; or team size exceeds 3 concurrent agents working on separate features |
| **Backend replacement (non-Firebase)** | Firebase satisfies Phase 1–4 requirements; custom backend is significant infrastructure investment | Specific Firebase limitation that blocks a product requirement; or cost at Phase 4 scale makes Firebase impractical |
| **GraphQL / API layer** | Firebase's real-time listeners are a direct value proposition; adding an API layer reduces that value | Requirement for a public API, third-party integrations, or backend operations that Firestore rules cannot express |
| **Event sourcing / CQRS** | Command-query separation adds complexity not justified at Phase 1–4 volume | Audit log requirements; or read/write load patterns that justify separate read models |
| **Real-time collaborative features** | No product requirement at Phase 1–3 | Phase 4+ community features (group challenges, live events) requiring sub-second multi-user state |
| **CDN and media pipeline** | Phase 1 media volume is low; Firebase Storage is sufficient | Average media storage per user exceeds threshold where Firebase Storage cost becomes material |
| **Server-side rendering / web client** | Mobile-only product; no web requirement in Phase 1–4 | Explicit Phase 5 web requirement |
| **Custom notification backend** | FCM and APNs via Firebase handle Phase 1–3 notification requirements | Phase 4 notification fan-out at graph scale that exceeds Firebase Functions limits |
| **Analytics data warehouse** | Firebase Analytics satisfies Phase 1–3 product analytics needs | Phase 3+ success metric reporting requiring cross-session cohort analysis beyond Firebase's capabilities |

---

## 13) Architecture Decision Guidance

Future agents making architectural decisions must apply these rules before proceeding. These rules supplement the decision framework in `ArchitecturePrinciples.md` §4 with architecture-specific guidance.

**Rule 1 — Trace every structural decision to a product requirement.**
Before adding a new package, a new layer boundary, a new abstraction, or a new pattern, identify the product requirement that makes it necessary. Architecture serves product. An architectural choice with no product traceability is premature abstraction.

**Rule 2 — The feature package is the first question.**
When implementing any code, the first question is: "which feature does this belong to?" If it belongs to exactly one feature, it goes in that feature's package. If it is shared by two or more features, it goes in `core/`. If the answer is unclear, the uncertainty signals either a product scope question (what feature is this?) or a domain modeling question (should this be a shared type?). Resolve the question before writing the code.

**Rule 3 — When two structural choices are available, prefer the one that preserves feature isolation.**
Feature isolation is the property that enables future module extraction, limits blast radius of changes, and makes features independently understandable. When in doubt, choose the option that reduces cross-feature dependencies.

**Rule 4 — The domain layer is the decision point for entity ownership.**
If two features both need a concept (e.g., both `map` and `social` need to know about a `User`), the concept belongs in `core/domain/`, not in either feature. Feature domain types are feature-specific; shared concepts are explicitly shared.

**Rule 5 — New patterns require documentation before implementation.**
If an implementation requires a structural approach not described in this document or the downstream architecture documents, the approach must be documented in an ADR before any code is written. An undocumented pattern encountered by a future agent will be either copied (producing inconsistency) or ignored (producing fragmentation). Documentation is the control mechanism.

**Rule 6 — Phase-appropriate scope.**
Before adding architectural complexity, check the current phase in `roadmap/STATE.md` and verify against `product/ReleaseStrategy.md`. Infrastructure that will not be used until Phase 3 must not be introduced in Phase 1. The cost of carrying unused complexity is paid in every agent session.

**Rule 7 — The banned list is not optional.**
The forbidden dependency list in §7 is not advisory. A dependency that is on the forbidden list is a violation regardless of how locally reasonable it seems. If a forbidden dependency appears to be the only solution to a real problem, stop and write an ADR — do not silently introduce the violation.

**Rule 8 — Errors travel upward as typed values, not exceptions.**
Every component that calls a component that can fail receives a typed failure representation. `Result<T>`, sealed error hierarchies, or equivalent typed outcomes are the contract. An exception that propagates to a ViewModel without becoming a typed state is an error in the data flow design, not just a style issue.

**Rule 9 — Single module → multi-module is an extraction, not a redesign.**
When the multi-module trigger fires, the extraction must be mechanical, not architectural. If feature packages are clean (no cross-feature imports), extraction is moving directories and updating build configuration. If extraction requires architectural changes, the architecture has already drifted and must be corrected first.

**Rule 10 — Respect the plan-then-code order.**
An agent that encounters ambiguity about where code belongs reads the relevant planning documents before writing anything. The planning documents contain the answers to most structural questions. Reading time is cheaper than drift.

---

## 14) Validation Checklist

Every downstream architecture document, every implementation plan, and every completed feature must be reviewed against this checklist before it is considered correct. This checklist is the operationalization of the architectural goals.

### Structure

- [ ] All code for this feature lives in the correct feature package (`feature/[name]/`)
- [ ] No cross-feature imports exist (cross-feature types are in `core/domain/`)
- [ ] Shared infrastructure additions are in the correct `core/` sub-package
- [ ] The package structure is consistent with the layout defined in §6

### Layer separation

- [ ] Domain layer imports nothing from data, presentation, or platform layers
- [ ] Presentation layer imports nothing from data layer (no Room entities, no DTOs in UI state)
- [ ] Data layer mapping (domain ↔ entity) lives entirely in the data layer
- [ ] Platform-specific code is behind an `expect`/`actual` boundary
- [ ] Repository interfaces are defined in the domain layer
- [ ] Repository implementations are in the data layer

### Data flow

- [ ] All reads go through the repository; no direct DAO or Firestore access from above the data layer
- [ ] All writes go to local storage first; remote write is async
- [ ] Failed remote writes are queued, not discarded
- [ ] State flows in one direction: data source → repository → domain → ViewModel → UI

### State management

- [ ] All screens use `EventStateViewModel` (ADR-0004); no alternative pattern introduced
- [ ] Every possible error case produces a typed error state visible in the ViewModel's `State`
- [ ] No caught exception terminates in `printStackTrace()` or equivalent swallowing
- [ ] One-time effects (navigation, toasts) travel as `Event` types, not as `State` fields

### Privacy and security

- [ ] Visibility field defaults to `FOLLOWERS` in entity definition (not PUBLIC, not PRIVATE)
- [ ] Precise lat/lng coordinates do not appear in any presentation layer type for content the user does not own
- [ ] Precise coordinates are written only to the owner-only private subdocument; public documents carry fuzzed coordinates (ADR-0019)
- [ ] No API key, credential, or configuration secret exists in any tracked file
- [ ] Firebase security rules are the authoritative enforcement point; client-side checks are UX only

### Product fidelity

- [ ] Every field in CREATE-03.3 (`Features.md`) round-trips without loss through all layers
- [ ] Rewarded ad unlocks are single-use; no `unlock_expiry` field in any entity
- [ ] Free users can log a fart, view the map, and react — no capability gate exists in any layer
- [ ] Intensity field is stored in every Fart entity with value but contributes 0% to ranking in Phase 1–3
- [ ] Non-free cosmetics are usable only with a server-verified entitlement or a server-issued unlock token (ADR-0016, ADR-0017)

### AI readability

- [ ] Any agent reading the package structure can understand which feature owns each area without reading code
- [ ] Any new pattern introduced has a corresponding ADR before implementation
- [ ] Naming follows the `[Feature][Type].kt` convention consistently
- [ ] The document describing this concern is updated before the code is reviewed as complete

---

*This document is the master architectural blueprint for Fart Map. It describes system shape, not implementation detail. All downstream architecture documents derive from it. When any downstream document conflicts with this one, this document wins.*

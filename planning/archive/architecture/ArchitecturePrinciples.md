# Architecture Principles: Fart Map

> **Planning spine position:** engineering constitution; the highest-authority document in the architecture layer.
> Upstream: `product/ProductGovernance.md`, `product/Vision.md`, `meta/archive/PlanningAuditReport.md`, all ADRs.
> Downstream: every document in `planning/architecture/`, `planning/execution/`, `planning/skills/`, and all implementation code.
> **When this document and any other architecture document conflict, this document wins.**
> This document defines engineering philosophy, guardrails, and decision frameworks. It does not specify technologies, frameworks, or implementation details. Those live in downstream architecture documents.

---

## Contents

1. [Purpose](#1-purpose)
2. [Engineering Goals](#2-engineering-goals)
3. [Core Architectural Principles](#3-core-architectural-principles)
4. [Architectural Decision Framework](#4-architectural-decision-framework)
5. [Engineering Anti-Patterns](#5-engineering-anti-patterns)
6. [AI Development Guardrails](#6-ai-development-guardrails)
7. [Layering Philosophy](#7-layering-philosophy)
8. [Cross-Platform Philosophy](#8-cross-platform-philosophy)
9. [Scalability Philosophy](#9-scalability-philosophy)
10. [Testing Philosophy](#10-testing-philosophy)
11. [Security Philosophy](#11-security-philosophy)
12. [AI Decision Rules for Architecture](#12-ai-decision-rules-for-architecture)
13. [Architecture Constitution](#13-architecture-constitution)

---

## 1) Purpose

### Why architecture principles exist

Architecture principles exist to make a codebase predictable. A predictable codebase is one where:

- The location of any piece of logic can be inferred from the category of that logic.
- Every new feature is implemented the same way as every existing feature.
- A developer encountering a new part of the codebase does not need to ask "which pattern are we using here?"
- A bug in one layer cannot be caused by logic that belongs in a different layer.
- Changes in one system do not silently corrupt the state of another.

Without written principles, consistency depends entirely on shared memory and shared judgment. In a small, stable team with no turnover, that is survivable. In any other context, it is not.

### Why principles are especially critical in AI-assisted development

AI agents have no implicit context. An agent writing a new feature reads the surrounding code and infers the pattern from what it sees. If the codebase contains three different approaches to the same concern — because each was added by a different agent in a different session, each making a locally reasonable decision — the next agent will pick one of the three at random, or invent a fourth.

This is not a hypothetical. The PoC codebase, built without written architecture governance, already demonstrates the cost:

- The domain layer imported the data layer (`domain/Fart.kt` imported `FartEntity`) — a fundamental dependency direction violation that would have compounded with every feature added on top of it.
- Two independent sync paths existed for the same entity — one writing inline, one re-pushing from a queue — with no documented reconciliation policy.
- Errors were swallowed in the state management layer (`printStackTrace()`) — making every bug in the application invisible in production.
- A secrets file was committed to version control — a security failure that is trivially preventable with a written policy and effectively unrecoverable once the key is exposed.
- A domain field (`visibility`) was present in the model but silently dropped during persistence — producing a product behavior (every fart becoming PRIVATE) that contradicted the product spec.

Each of these failures has the same root cause: the correct behavior was not written down anywhere. Every agent (human or AI) working on the codebase had to invent the answer, and some answers were wrong.

This document ensures the answers are written down before anyone writes code.

### The relationship between this document and the product layer

Architecture serves product. This principle is stated in `ProductGovernance.md` §1 and repeated here because it is the single most important constraint on architectural decision-making.

When an architectural choice is in tension with a product requirement, the architectural choice is reconsidered first. Architecture is a means; the product is the end. An architecture that is technically elegant but cannot deliver the product is not a good architecture for this project — regardless of what it achieves in isolation.

The converse is also enforced: architecture must not make product decisions. When an architectural constraint forces a product tradeoff, that tradeoff must be surfaced explicitly and decided by the product layer, not silently absorbed by the architecture.

---

## 2) Engineering Goals

The following goals govern all architectural decisions. They are listed in priority order. When two goals are in tension, the higher-priority goal wins unless there is an overwhelming, documented argument for an exception.

### Goal 1 — Correctness

Features must do what the product specification says they do. State must round-trip faithfully. A field written to persistence must be read back unchanged. A user action must produce the documented outcome every time, not most of the time.

**Tradeoff:** Correctness is never traded for speed of delivery. A feature that ships fast but behaves incorrectly is not a shipped feature — it is a bug with a release date. The PoC visibility field failure is the canonical example: the field was wired into the UI, the entity, and the repository, but the mapping dropped it silently. The feature appeared complete and was entirely broken.

### Goal 2 — Maintainability

The codebase must remain navigable and modifiable as it grows. Any developer or agent encountering any part of the codebase six months after it was written must be able to understand what it does, why it is structured the way it is, and where to make a change.

**Tradeoff:** Maintainability sometimes requires more code than a clever solution. A slightly verbose, obvious implementation is preferred over a concise, subtle one. The additional lines of code are documentation. The saved lines are a maintenance tax paid later, with interest.

### Goal 3 — Safety and Security

Secrets must never enter version control. User data must be protected by default. Security controls must live in the server layer and not be bypassable by client-side manipulation. Privacy-safe defaults must be enforced architecturally, not just by policy.

**Tradeoff:** Security sometimes adds friction to development workflows (secrets injection, rule validation, permission requests). This friction is not optional. The PoC committed a Google Maps API key and `GoogleService-Info.plist` to version control. The new project must treat secret management as a first-class architectural concern from the first commit.

### Goal 4 — Testability

Each layer must be independently testable without requiring the full application stack. Domain logic must be testable with no platform dependencies. Repository behavior must be verifiable without a running server. ViewModel state transitions must be checkable without a running UI.

**Tradeoff:** Testability requires deliberate layer separation that may feel like overhead when a feature is small. This overhead pays compound interest: a codebase where every layer is independently testable can be changed confidently; one where layers are coupled can only be changed carefully.

### Goal 5 — Offline Resilience

User-initiated actions must succeed regardless of network state. The application must queue operations when offline and synchronize when connectivity returns. A user who logs a fart in an area with no signal must see their marker appear on their map immediately, and must see it persist after they return to connectivity. See ADR-0007 (local-first reads with remote backfill).

**Tradeoff:** Offline resilience adds complexity to the data and sync layers. This complexity is mandatory, not optional — it is required by the product specification (`Features.md` CREATE-03.3 OFFLINE state) and by the user trust model. A product that loses user content when the network drops will not retain users.

### Goal 6 — AI Readability

The codebase must be structured so that a future AI agent with no prior context can read a relevant set of files and make a correct, consistent contribution. This means: one pattern per concern, exhaustive naming conventions, explicit rather than implicit behavior, and no magic.

**Tradeoff:** AI readability and local optimization are frequently in tension. An agent might identify a clever optimization that is correct and performant but introduces a pattern that other agents will misread and misapply. In this project, AI readability takes precedence over local optimization except when the performance difference is product-visible (i.e., the user can feel it).

### Goal 7 — Simplicity

The simplest solution that correctly implements the requirement is preferred. Complexity must be justified by a real constraint that simpler solutions cannot satisfy. "This might be useful later" is not a sufficient justification. "Phase 3 data volume will exceed the capacity of this approach" is.

**Tradeoff:** Simplicity is the last goal before scalability, not the first. This ordering is deliberate. A simple solution that is incorrect, insecure, or untestable is not simple — it is incomplete. Simplicity is applied after the higher-priority goals are satisfied.

### Goal 8 — Scalability

The architecture must accommodate the product's growth from Phase 1 to Phase 4 without requiring a fundamental rewrite. It must not be designed to handle Phase 5 scale before Phase 1 has launched. Scalability decisions are made when triggered by real growth, not in anticipation of hypothetical scale.

**Tradeoff:** Scalability and simplicity are in tension. The resolution is time-gated: build the simplest architecture that supports the current phase and the next phase. Pre-building architecture for two phases ahead is over-engineering. Ignoring the next phase entirely is under-engineering.

---

## 3) Core Architectural Principles

### Principle 1 — Product First

Architecture is a delivery mechanism for the product. Every architectural decision must be traceable to a product requirement. An architectural approach that is technically superior but makes the product harder to build, slower to iterate, or less correct is not a good architectural choice for this project.

**In practice:** When an architectural constraint creates a product tradeoff — for example, when an offline-first design makes a certain real-time feature harder — that tradeoff must be surfaced and decided at the product level. The architecture layer does not resolve product tradeoffs silently.

**Governance test:** Can this architectural decision be traced back to a product requirement? If not, it needs justification before it is accepted.

---

### Principle 2 — Simplicity Before Cleverness

When two solutions both satisfy the requirements, choose the simpler one. Boring, explicit, slightly verbose code is preferred over elegant, implicit, concise code in a project where AI agents are primary contributors.

An agent reading 40 lines of straightforward code understands it immediately. An agent reading 10 lines of clever code may understand it, misunderstand it, or partially understand it — and the ambiguity produces drift.

**In practice:** Factory patterns, generics, extension functions, and abstractions are tools — not defaults. Each one must earn its presence by solving a real problem that a simpler approach cannot. Abstract when there are multiple real implementations. Not in anticipation of possible future implementations.

**Governance test:** Could a new agent understand this in 30 seconds? If not, is the complexity justified by a concrete requirement?

---

### Principle 3 — Offline-Tolerant by Default

Every user action that produces a content change must be possible without a live network connection. The application must never inform the user that an action failed solely because the network is unavailable. Actions queue locally and synchronize when connectivity returns.

This is not a graceful degradation mode. It is the default operating mode. The online-connected experience is a performance optimization over the offline-first baseline.

**In practice:** See ADR-0007. The sync strategy (pending operations, retry logic, conflict policy) is defined in `architecture/SyncStrategy.md`. This principle establishes the requirement; that document establishes the implementation contract.

**Governance test:** If the network drops at any point during this user flow, what happens? If the answer is "the user sees an error and their action is lost," the design violates this principle.

---

### Principle 4 — Single Source of Truth

Every piece of state has exactly one authoritative location. Two systems cannot both claim to be the source of truth for the same value. When state is derived from another value, the derivation is explicit and one-directional.

**In practice:**
- The local database (Room) is the source of truth for all persisted application state. The UI observes the local database; it does not maintain its own copy of persisted data.
- The remote database (Firestore) is the authoritative source for canonical state. The local database is a cache. Sync resolves discrepancies in favor of the remote.
- The ViewModel is the source of truth for UI state. The UI observes the ViewModel; it does not hold state independently.

The PoC visibility field failure is the canonical violation: the `FartVisibility` value existed in the UI model and in the domain model, but the mapping from domain to entity silently dropped it, creating two conflicting sources of truth (the in-memory domain model said "PUBLIC"; the persisted entity said "PRIVATE"). The local database must be the definitive state, and every mapping must be verified to round-trip all fields.

**Governance test:** For any given piece of state, can you identify its single authoritative location? If two places claim authority, which one wins, and is that documented?

---

### Principle 5 — Predictable State Flow

State flows in one direction: data source → repository → domain → ViewModel → UI. No layer reaches up or sideways. Side effects are explicit. Every state transition produces an observable, typed state change. Errors are first-class values — they are modeled as explicit states in the ViewModel, not swallowed as exceptions.

**In practice:**
- The `StateViewModel` pattern (ADR-0004) defines the canonical state contract: a sealed `State` hierarchy, a sealed `Action` type, and an explicit `Event` type for one-time UI effects. No deviation from this pattern without a new ADR.
- `try/catch` blocks that terminate in `e.printStackTrace()` or similar swallowing are prohibited. Every caught exception must produce either a typed error state that the UI can observe and display, or a logged event that surfaces to observability infrastructure. Nothing is silently discarded.
- A state change that a user triggered must be auditable: given a sequence of Actions, the resulting State must be deterministically derivable.

**Governance test:** Given an unexpected error in this code path, what does the user see? If the answer is "nothing," the design violates this principle.

---

### Principle 6 — AI-Friendly Architecture

The codebase must be structured so that a future agent with no prior context can read it and contribute correctly. This means:

- **One pattern per concern.** Not "either approach works." One approach, applied consistently everywhere.
- **Explicit naming conventions.** Names encode responsibility. A file named `FartRepository.kt` contains the fart repository. A file named `FartRepositoryImpl.kt` contains its implementation. There is no ambiguity about what belongs where.
- **Documented boundaries.** Every layer boundary has a written contract in the architecture documents. Code that crosses a boundary without a documented justification is an error.
- **No magic.** No implicit behavior, no hidden state, no conventions that exist only in the minds of the original authors. Everything an agent needs to understand the codebase is either in the code itself or in a planning document that the agent is instructed to read.
- **Self-describing structure.** A new agent reading the package structure should be able to infer the architecture before reading a single line of code.

**Governance test:** Could an agent who has read the architecture documents and the relevant source files make a correct change to this area without asking any questions? If not, what is missing from the documentation or the structure?

---

### Principle 7 — Consistency Over Local Optimization

When an established pattern already exists for a concern, that pattern is used — even if a different approach would be locally superior for a specific case. The consistency tax is paid once; the inconsistency tax is paid on every future contribution to the codebase.

**In practice:** Introducing a second way to handle state management, a second navigation approach, or a second DI mechanism requires an ADR that explains why the existing pattern is insufficient for this specific case. The ADR is not a formality — it is the mechanism that prevents drift from accumulating silently.

The PoC demonstrated this with its two sync paths. Neither path was wrong in isolation. Together, they created a system where no agent (human or AI) could determine which path applied in a given situation — requiring either trial-and-error or a full codebase read to answer a simple question.

**Governance test:** Does a pattern already exist for this concern? If yes, why is this implementation different, and is that difference documented?

---

## 4) Architectural Decision Framework

When choosing between two or more approaches to an architectural problem, apply this framework in order. Stop at the first step that produces a clear answer.

### Step 1 — Consistency gate (run first)

Does an established pattern already solve this problem? If yes, use it. A new pattern is not introduced unless the existing one is demonstrably insufficient for this specific case.

A new pattern requires:
1. A written explanation of why the existing pattern fails for this case.
2. An ADR documenting the new pattern, its scope of application, and its consequences.
3. Human review if the new pattern affects a layer boundary or an existing ADR.

### Step 2 — Five engineering questions

Answer all five. An approach that cannot answer all five affirmatively requires redesign before proceeding.

1. **Does this serve the product?** Can this decision be traced to a product requirement in `Features.md`, `ReleaseStrategy.md`, or a system detail document?
2. **Is this simpler than the alternative?** Or, if it is more complex, is the added complexity justified by a specific, documented constraint?
3. **Does this preserve layer separation?** Does every dependency point in the correct direction (Presentation → Domain ← Data)?
4. **Can an agent who has read the architecture documents understand this in 30 seconds?**
5. **Is the observable behavior of this approach correct under all conditions,** including network loss, process death, and malformed input?

### Step 3 — Weighted scoring

For decisions that remain genuinely ambiguous after Steps 1 and 2:

| Criterion | Weight | Description |
|---|---|---|
| Product alignment | 30% | How directly does this serve a documented product requirement? |
| Simplicity and maintainability | 25% | How readable, modifiable, and boring is this approach? |
| Consistency with existing patterns | 20% | How closely does this match what already exists? |
| AI comprehensibility | 15% | How quickly can an agent understand this without prior context? |
| Performance and scalability fit | 10% | How appropriate is this for the current phase's scale requirements? |

Score each option 1–5 on each criterion. Multiply by weight. The higher score wins unless Step 1 or Step 2 produced a hard constraint.

### Step 4 — Record the decision

Every decision that affects a layer boundary, introduces a new dependency, changes an existing pattern, or touches a settled ADR must be recorded as a new ADR in `planning/meta/decisions/`. Record:

- The decision
- The alternatives considered
- The criteria that determined the outcome
- The consequences for downstream documents and code

Decisions not recorded are decisions that will be re-litigated by the next agent.

---

## 5) Engineering Anti-Patterns

These are specific behaviors that degrade the architecture of Fart Map. Each one has a concrete PoC or product precedent. Every agent must be able to recognize and reject these patterns without escalation.

### Anti-Pattern 1 — Dependency Direction Violation

**What it looks like:** A higher layer imports a type from a lower layer's implementation rather than its interface. Domain models import data entities. ViewModels import repository implementations. UI components import Room entities directly.

**Why it damages the codebase:** Layer separation is the primary mechanism that makes individual layers testable. A domain model that imports a data entity cannot be tested without a database. A ViewModel that imports a Room DAO is permanently coupled to a specific persistence technology. Every dependency direction violation reduces the number of things that can be changed independently.

**PoC example:** `domain/Fart.kt` imported `data.local.db.model.FartEntity` to perform the `toFartEntity()` mapping. This is a domain layer file depending on a data layer implementation detail. The mapping belongs in the data layer.

**Correct behavior:** Mapping between layers happens at the boundary of the lower layer, not in the higher layer's model. Domain models are defined in the domain layer. Entities are defined in the data layer. The data layer maps between them.

---

### Anti-Pattern 2 — Error Swallowing

**What it looks like:** `try/catch` blocks that catch exceptions and terminate in `e.printStackTrace()`, `e.message?.let { log(it) }` with no state update, or any pattern where a caught exception does not produce a user-visible state or an observable error event.

**Why it damages the codebase:** Swallowed errors make bugs invisible. A feature that silently fails looks identical to a feature that succeeds, until a user reports it. In a product with offline sync, analytics, and social interactions, silent failures compound: a fart that fails to sync looks posted to the user, produces no error log visible to the team, and silently becomes a data inconsistency.

**PoC example:** `StateViewModel.performAction` caught exceptions and called `printStackTrace()`. Every failed action in the application produced a console line and returned the UI to an undefined state.

**Correct behavior:** Every caught exception produces either a typed error state that the UI can observe and present to the user, or an explicit observability event (logged at the appropriate severity level with structured context). Nothing is silently discarded.

---

### Anti-Pattern 3 — Committed Secrets

**What it looks like:** API keys, authentication credentials, Firebase configuration files, certificates, tokens, or any other sensitive value present in any file that is tracked by version control.

**Why it damages the codebase:** A secret committed to version control is a secret that is permanently compromised, even after deletion, because version control history preserves it. Rotating the secret does not remove it from history. Public repositories make this an immediate breach; private repositories make it a breach waiting for a misconfiguration or access leak.

**PoC example:** The Google Maps API key was present in `secrets.properties` and `AndroidManifest.xml`. The `GoogleService-Info.plist` (containing Firebase project credentials) was committed to `iosApp/`. Both files were in the working tree despite a `.gitignore` entry — demonstrating that `.gitignore` hygiene alone is insufficient.

**Correct behavior:** Secrets live in untracked local files (with a `.gitignore` entry enforced from the first commit), CI/CD environment variables, or a secrets management service. The architecture documents governing secrets management are `architecture/SecurityAndSecrets.md`.

---

### Anti-Pattern 4 — Silent State Inconsistency

**What it looks like:** A field present in a domain model or UI model that is not persisted, not transmitted, or not round-tripped faithfully through all layers. The field exists in one layer but is silently dropped or defaulted at a layer boundary.

**Why it damages the codebase:** Silent state inconsistency produces product behavior that contradicts the specification without producing any error. The discrepancy between what the product says should happen and what actually happens can only be discovered through end-to-end testing or user reports.

**PoC example:** The `FartVisibility` field was modeled in `Fart.kt` and presented in the UI, but the `FartEntity` mapping did not include it. Every fart was persisted as PRIVATE regardless of the user's selection — a product behavior failure with no error trace.

**Correct behavior:** Every field in a domain model has a corresponding field in its persistence representation. The data layer mapping is tested to verify that all fields round-trip without loss or corruption. `CREATE-03.3` in `Features.md` is the authoritative field mapping for the Fart entity; any deviation from it is a bug.

---

### Anti-Pattern 5 — Dual Sync Paths

**What it looks like:** Two independent code paths both claiming to be responsible for the same synchronization concern. One path writes data to the remote on the action that creates it. A second path independently pushes unsynced data from a queue. Neither path coordinates with the other.

**Why it damages the codebase:** Dual sync paths create a question that cannot be answered by reading either path alone: "what actually happens if both fire simultaneously, or if neither fires?" Undefined coordination produces duplicated writes, missing writes, or conflicting state depending on timing.

**PoC example:** `addFart()` wrote to Firestore inline during the creation action. `syncFarts()` separately re-pushed records flagged as unsynced. Both paths operated on the same data without a documented reconciliation policy.

**Correct behavior:** One sync path, defined in `architecture/SyncStrategy.md`. Every write goes through the same path. The path handles both the immediate case (online) and the deferred case (offline queue) through the same mechanism.

---

### Anti-Pattern 6 — Multiple State Management Styles

**What it looks like:** Some screens use the documented StateViewModel pattern; others use a different approach (plain ViewModel + LiveData, direct Compose state, a third-party state library introduced for one feature). Multiple patterns coexist without documentation of which should be used where.

**Why it damages the codebase:** Every agent that writes a new screen must choose a pattern. If multiple patterns are present, the agent either guesses or copies the nearest example. Neither approach produces consistency. The result is a codebase where the architecture varies by screen rather than by layer.

**Correct behavior:** One state management pattern, documented in `architecture/Architecture.md`, applied to every screen in the application. ADR-0004 designates the StateViewModel pattern. Any new screen uses it. Any deviation requires a new ADR.

---

### Anti-Pattern 7 — Mixed Navigation Approaches

**What it looks like:** Some flows use the documented type-safe navigation approach; others use a different navigation mechanism (back stack manipulation, intent-based navigation, hardcoded routes, a second navigation library introduced for one flow).

**Why it damages the codebase:** Navigation is the connective tissue of the application. Inconsistent navigation produces untestable back stack behavior, untracked deep link handling, and session state that is observable from one approach but not the other.

**Correct behavior:** One navigation approach, documented in `architecture/NavigationStrategy.md`, applied to all flows. ADR-0005 designates type-safe navigation with serializable routes. ADR-0006 designates session-driven start destination. Any deviation requires a new ADR.

---

### Anti-Pattern 8 — Premature Abstraction

**What it looks like:** Interfaces with a single implementation and no documented reason for the interface to exist. Empty use case classes that delegate directly to a repository without adding any logic. Abstract base classes that enforce a contract satisfied by one concrete type.

**Why it damages the codebase:** Premature abstractions consume agent attention (an agent reading an interface must find its implementation to understand the behavior) while providing no actual benefit. They also establish a false pattern: future agents see the interface and add more interfaces by analogy, producing a codebase where every simple operation requires reading three files.

**PoC example:** `GetUserUseCase.kt` was an empty placeholder that contained no logic. It added a layer of indirection with no corresponding value.

**Correct behavior:** Interfaces exist when there are multiple real implementations (e.g., a repository interface with a real implementation and a test fake). Use cases exist when they contain domain logic that would otherwise live in a ViewModel or repository. Empty scaffolding is not committed.

---

### Anti-Pattern 9 — Premature Optimization

**What it looks like:** Performance optimizations (caching layers, parallel execution, query tuning, memory pooling) introduced before there is a measured performance problem. Complex reactive pipelines designed to handle scale that has not arrived.

**Why it damages the codebase:** Premature optimizations add complexity that increases maintenance cost and reduces AI readability. If the optimization addresses a problem that does not exist at the current phase's scale, its cost is paid in full while its benefit is zero.

**Correct behavior:** Performance optimization is triggered by measurement, not anticipation. The architecture must accommodate Phase 1–4 scale efficiently; it must not be designed for hypothetical Phase 5 scale before Phase 1 has launched.

---

### Anti-Pattern 10 — Feature-Specific Architecture

**What it looks like:** A new feature introduces a pattern that is not used anywhere else in the codebase and is not documented in the architecture. The pattern may be reasonable for the feature, but it creates a local divergence that future agents will encounter without context.

**Why it damages the codebase:** Feature-specific patterns are how architectural drift begins. A codebase starts consistent. One feature ships with a slightly different approach, documented only in a PR description. The next feature's agent reads the new feature as an example and propagates its pattern. Within ten features, the codebase has three approaches to the same concern, and none of them are documented.

**Correct behavior:** If a feature requires a genuinely new approach, that approach is documented in an ADR before it is implemented. The ADR defines the scope of the new pattern, the reason the existing pattern is insufficient, and the expectations for future features in the same category.

---

## 6) AI Development Guardrails

These rules govern how AI agents interact with the codebase. They are not suggestions. An agent that violates these rules is producing drift, and drift in an AI-assisted codebase compounds faster than in a human-only codebase.

### Rule 1 — Read architecture documents before writing code in a new area

Before implementing anything in a layer or system the agent has not previously worked in during the current session, read the relevant architecture document. The architecture documents are the contract that the code must satisfy. Code written without reading the contract may be functionally correct and architecturally wrong.

**Required reading before any implementation:**
- This document (`ArchitecturePrinciples.md`) — always
- `Architecture.md` — for any code in any layer
- The layer-specific document (`DataModel.md`, `SyncStrategy.md`, etc.) — for the specific layer being worked in
- The relevant product document (`Features.md`, system detail doc) — for the feature being implemented

### Rule 2 — Identify the existing pattern before implementing

Before writing a new implementation of any concern (state management, data persistence, navigation, dependency injection, error handling, sync), identify how the same concern is handled in the existing codebase. If a pattern exists, use it. Do not invent a new pattern because the existing one is unfamiliar.

### Rule 3 — Reuse before extending; extend before replacing

The preference order for adding capability is:
1. Use an existing component without modification.
2. Add a parameter or behavior to an existing component.
3. Create a new component that follows the existing pattern exactly.
4. Introduce a new pattern — only with an ADR and only when the existing pattern is genuinely insufficient.

### Rule 4 — Preserve dependency direction

Presentation → Domain ← Data ← Platform ← Infrastructure. No layer imports from a layer above it. No layer imports the implementation of a layer below it — only its interface. Any proposed dependency that runs against this direction requires an explicit architectural review before implementation.

### Rule 5 — Model errors explicitly; swallow nothing

Every code path that can fail must model its failure as a typed outcome. Use `Result`, sealed class hierarchies, or equivalent typed failure representations. No `try/catch` block may terminate without either producing an observable state change or emitting a structured observability event. `e.printStackTrace()` and silent swallowing are prohibited.

### Rule 6 — Platform-specific code belongs behind a shared interface

Any code that references a platform-specific API (Android SDK, iOS UIKit/SwiftUI interop, platform file system, etc.) must be placed behind a shared interface with a platform-specific implementation. See ADR-0008. Code that references a platform API directly in a shared module is a platform boundary violation.

### Rule 7 — Secrets never in code or tracked files

No API key, credential, token, configuration file with sensitive values, or certificate may be placed in any file that is or could be tracked by version control. If an implementation requires a secret, read `architecture/SecurityAndSecrets.md` before writing the code. If the document does not cover the case, create an ADR before proceeding.

### Rule 8 — Document new patterns before implementing them

If an implementation requires a pattern that does not exist in the codebase and is not documented in the architecture documents, the pattern must be documented first. Write the ADR or update the relevant architecture document, then implement. An undocumented pattern is a liability even if it is correct.

---

## 7) Layering Philosophy

The application is structured in five layers. Each layer has a defined set of responsibilities. Concerns that belong in one layer must not appear in another.

These are logical layers — not necessarily module boundaries, though module boundaries should reflect them. The exact modularization strategy is defined in `architecture/Architecture.md`.

### Layer 1 — Presentation

**Responsibility:** Render UI state. Dispatch user events. Observe ViewModel state. Trigger navigation. Produce one-time UI effects (show a toast, navigate, dismiss a dialog).

**Does NOT contain:** Business logic. Data fetching. Direct access to persistence or remote services. Platform-specific SDK calls beyond what is required for rendering. State held independently of the ViewModel.

**How it communicates:** Observes ViewModel state via reactive streams. Dispatches typed Actions to the ViewModel. Receives one-time Events from the ViewModel for effects.

### Layer 2 — Domain

**Responsibility:** Define business entities. Define business rules and validation logic. Define use case contracts (what the system must do, not how it does it). Remain entirely free of platform dependencies and persistence details.

**Does NOT contain:** UI state. Persistence annotations. SDK imports. Platform-specific types. References to any data layer implementation.

**How it communicates:** Defines pure Kotlin entities, interfaces, and logic. The data layer implements domain interfaces. The presentation layer observes domain state through the ViewModel.

**Critical rule:** The domain layer must not import any type from the data layer. Mapping between domain entities and data entities is the data layer's responsibility. See Anti-Pattern 1.

### Layer 3 — Data

**Responsibility:** Implement repository contracts defined by the domain layer. Manage local persistence. Manage remote synchronization. Map between domain entities and persistence/transport representations. Define the sync contract (pending operations, retry, conflict resolution).

**Does NOT contain:** Business rules. UI state. Presentation concerns. Platform-specific APIs beyond what is required for persistence and network access.

**How it communicates:** Implements domain interfaces. Exposes reactive streams (flows) that the domain and presentation layers can observe. Coordinates with the platform layer for SDK-level operations.

### Layer 4 — Platform

**Responsibility:** Provide platform-specific implementations of shared contracts. Abstract platform capabilities (maps rendering, location services, authentication providers, native file access, platform-specific logging) behind shared interfaces so that the domain and data layers remain platform-agnostic.

**Does NOT contain:** Business logic. UI composition. Data mapping beyond what is required to adapt a platform API to a shared contract.

**How it communicates:** Implements `expect` declarations from shared modules with platform-specific `actual` implementations. See ADR-0008.

### Layer 5 — Infrastructure

**Responsibility:** Initialize and configure SDKs. Load secrets. Configure analytics and crash reporting. Set up dependency injection. Manage application-level lifecycle concerns.

**Does NOT contain:** Any product logic. Any user-facing behavior. Business rules. UI state.

**How it communicates:** Provides configured instances to the dependency injection container. Integrates with observability tooling.

### Dependency direction (strictly enforced)

```
Presentation
    │ observes / dispatches
    ▼
  Domain  ◄──────────────── Data
    (interfaces)        (implementations)
                              │
                              ▼
                           Platform
                              │
                              ▼
                        Infrastructure
```

No arrow may be reversed. No layer may import from a layer above it in this diagram. The Domain layer is the center of the architecture — it is imported by both Presentation and Data, but imports neither.

---

## 8) Cross-Platform Philosophy

Fart Map targets multiple mobile platforms. The architecture must support this without duplicating business logic, while respecting the platform-appropriate conventions of each target.

### The default is shared

Business logic, domain entities, data layer contracts, repository implementations, sync logic, ViewModel state management, and navigation logic are shared by default. Platform-specific code is the exception, not the default.

Before placing any code in a platform-specific module, answer: *"Is there a technical reason this cannot be shared?"* If the answer is no, it belongs in the shared module.

### Platform-specific code is bounded and explicit

When platform-specific code is necessary — native UI components, platform authentication flows, map SDKs, platform file APIs — it lives behind a shared interface with a platform-specific implementation. The boundary is the `expect`/`actual` seam (ADR-0008).

The shared module declares the contract. The platform modules fulfill it. Business logic never sees a platform-specific type.

### Behavioral consistency; convention appropriateness

User-facing behavior must be consistent across platforms. A fart logged on Android must behave identically to a fart logged on iOS: same visibility rules, same sync behavior, same validation, same state machine.

Platform-appropriate UX conventions — navigation gesture styles, native controls, platform-specific dialogs, keyboard behavior — may differ between platforms where a shared implementation would feel wrong to platform users. Behavioral parity and visual parity are different requirements.

### Platform parity gaps are tracked, not accepted

When a feature works on one platform but not another, that gap is a tracked defect — not an accepted state. The PoC shipped with no markers rendering on iOS and no Apple Sign-In implementation. These were not "future features" — they were functional gaps that made the product incomplete on one platform.

Every feature that touches a platform boundary has a parity requirement: it must work on all supported platforms before it is considered complete.

---

## 9) Scalability Philosophy

### Build for today; design for tomorrow; do not pre-build next year

The architecture for Phase 1 must be correct and maintainable for Phase 1. It must be extensible to Phase 2 without a rewrite. It must not be designed to handle Phase 4 or Phase 5 at Phase 1 launch.

The phases defined in `ReleaseStrategy.md` §3 provide the scaling timeline. Architecture complexity that is not required until a later phase must not be introduced in an earlier phase. The cost of carrying unnecessary complexity through early phases is paid in every agent session that reads and works with that code.

### Scaling is triggered by measurement

The signal to introduce additional architectural complexity is a measured constraint, not an anticipated one. When a specific performance metric, data volume threshold, or team coordination problem demonstrates that the current architecture is insufficient, that is the time to introduce the next level of complexity — documented in an ADR.

Without measurement, "this might not scale" is not an architectural argument. It is speculation.

### Known scaling pressure points

The following areas are expected to require architectural evolution as the product grows. They are noted here so that the foundational architecture does not accidentally make the evolution harder. They are not pre-built.

| Area | Current phase | Expected evolution trigger |
|---|---|---|
| Leaderboard / ranking | Phase 3 | High read volume on aggregated scores → denormalized counters, pre-computed board |
| Notification fan-out | Phase 2 | Large follower graphs → async fan-out write pattern |
| Community / group content | Phase 4 | Group activity feeds → fan-out-on-write or fan-out-on-read strategy |
| Analytics event volume | Phase 2+ | High event throughput → batching, buffering, sampling |
| Image and audio storage | Phase 2+ | Media growth → CDN, compression policy |

None of these require pre-built complexity. Each requires that the foundational architecture not prevent the evolution when it is needed.

### Architecture evolves through ADRs, not drift

When a scaling event requires an architectural change, that change is documented in a new ADR before implementation. The ADR explains the trigger (the measured constraint), the new approach, and the consequences for existing code. Architecture that evolves through drift — undocumented changes that accumulate feature by feature — is architecture that eventually becomes incomprehensible.

---

## 10) Testing Philosophy

Testing is not a phase that happens after development. It is a layer of the architecture, built alongside the code it tests.

### Layer-by-layer testing intent

**Domain layer — highest priority and coverage**
The domain layer contains pure business logic with no platform dependencies. It can be tested anywhere — on a development machine, in CI, on any platform — with no additional setup. Domain layer tests are the fastest to write, the fastest to run, and the most stable. They are the first line of defense against regressions.

Every business rule, validation condition, and entity transformation in the domain layer has a corresponding test.

**Data layer — repository contracts and persistence round-trips**
Data layer tests verify that the repository implementations satisfy their contracts. Local persistence tests verify that every field in every entity round-trips without loss. The `CREATE-03.3` entity field mapping table in `Features.md` is the specification for the Fart entity's persistence tests.

Specifically: the visibility field failure from the PoC (`FartVisibility` silently persisted as PRIVATE) must have a regression test that verifies the field round-trips correctly. This test exists from the first day of data layer implementation.

**Presentation layer — state machine tests**
ViewModel state machine tests verify that given a specific sequence of Actions in a specific starting State, the ViewModel produces the expected resulting State. These tests do not render UI. They verify the state transition logic in isolation.

**Integration — critical path smoke tests**
Integration tests verify the critical paths identified in `SuccessMetrics.md` §7 and `ReleaseStrategy.md` §6: the complete map → log → persist → appear flow for Phase 1, and the core social and sync flows for Phase 2.

**Platform layer — contract coverage only**
Platform-specific implementations are thin adapters over platform APIs. Test the contract the platform layer implements — verify that calling the shared interface produces the expected outcome — not the platform SDK behavior itself.

### Testing anti-patterns

- Testing the behavior of third-party SDKs (Room, Firebase, Koin) — those are tested by their maintainers.
- Testing UI pixel-perfect rendering — that is a design review, not a unit test.
- Mocking the system under test — if a test requires mocking the class it is testing, the class has too many responsibilities.
- Tests that only pass when run in a specific order — test isolation is required; each test must set up and tear down its own state.

### Test coverage philosophy

Coverage percentage is not the target metric. Behavior coverage is. A 90% line-coverage suite that does not test the visibility field round-trip is less valuable than a 60% suite that tests every documented state transition and data contract.

---

## 11) Security Philosophy

Security is an architectural concern, not a feature. It must be designed into the system from the first commit. The PoC demonstrated the consequences of treating security as deferred: a committed API key and a committed Firebase configuration file represent permanent exposure regardless of when they are discovered and rotated.

### Principle 1 — Secrets never in version control

No API key, credential, token, certificate, configuration file containing sensitive values, or service account file may exist in any file that is tracked, trackable, or accidentally trackable by version control. This applies to all branches and all environments.

The mechanism for secrets management is defined in `architecture/SecurityAndSecrets.md`. This principle establishes the requirement; that document establishes the implementation contract.

### Principle 2 — Privacy by default

The architectural default is always the most restrictive privacy option. User data is private unless the user explicitly opts into sharing it. Location data is coarse unless the user explicitly enables precision. Content visibility defaults to Followers, not Public.

This mirrors `ProductGovernance.md` §6 (social guardrails) and `ContentSafety.md` §3 (location privacy). The architecture layer enforces these defaults at the data contract level — default values in entity definitions and Firestore document schemas must reflect the restrictive default, not the permissive one.

### Principle 3 — Least privilege

The application requests only the permissions required for the current user-initiated action. It does not pre-request permissions for features that are not yet active. It does not retain access to a resource (location, camera, microphone) after the action that required it is complete.

This applies to both device permissions (location, microphone) and Firestore security rules (a user's data is accessible to that user; other users' data is accessible according to the visibility model in ADR-0010).

### Principle 4 — Defense in depth; server is authoritative

Client-side validation is a UX layer, not a security layer. A user who bypasses the client-side visibility check must still be blocked by the server-side Firestore security rule. The server-side rule is the authoritative enforcement point.

No security decision may be expressed only in client code. Every security-relevant constraint (visibility, access control, rate limiting, content moderation triggers) must have a server-side enforcement mechanism that operates independently of the client.

### Principle 5 — Fail securely

When a security check fails — authentication expires, a permission is denied, a Firestore rule rejects a read — the application defaults to the restricted state. It never defaults to the permissive state on failure. A failed authentication check means the user sees the unauthenticated experience. A failed visibility check means the content is not shown. Errors in security enforcement are never resolved by granting access.

---

## 12) AI Decision Rules for Architecture

Future agents making or evaluating architectural decisions must apply these rules before proceeding. These rules are in addition to the general AI development guardrails in §6.

1. **Architecture serves product.** Before proposing any architectural decision, identify the product requirement it serves. An architectural decision with no product traceability is not yet a complete proposal.

2. **Check the ADR registry first.** Before making any decision that touches a layer boundary, a cross-platform seam, a state management approach, or a data persistence strategy, check `planning/meta/decisions/` for an existing ADR. If one exists, it is settled. Do not re-litigate it without new information that changes the underlying tradeoffs.

3. **One pattern per concern.** If a pattern exists for the concern being addressed, use it. Do not introduce an alternative pattern because it is more familiar, more elegant, or more performant without a documented reason the existing pattern fails for this case.

4. **Dependency direction is non-negotiable.** Any proposed dependency that runs against the direction defined in §7 is rejected before any other evaluation. Dependency direction violations are not tradeoffs — they are architectural defects.

5. **Errors are states, not exceptions.** Any proposed design that handles failure through exception propagation without typed error states is incomplete. Redesign the error model before evaluating the rest of the proposal.

6. **Secrets require a documented mechanism before code is written.** If a proposed implementation requires a secret, read `architecture/SecurityAndSecrets.md` first. If the document does not address the specific case, write an ADR before writing the code.

7. **Platform-specific code requires a shared interface.** Any proposed implementation that places platform-specific code in a shared module without a shared interface and expect/actual boundary violates ADR-0008 and is rejected.

8. **Complexity must be justified by phase.** If a proposed architectural feature is not required until Phase 3, it must not be introduced in Phase 1. Reference `ReleaseStrategy.md` §3 and the current phase gate status. Premature complexity is a liability, not an asset.

9. **New patterns require ADRs before implementation.** An agent that determines a new pattern is necessary must write the ADR, reference it in the relevant architecture document, and have it reviewed before implementing. Code that implements an undocumented pattern is a drift event.

10. **When in doubt, read before writing.** If a decision is ambiguous, the agent reads more context before implementing. Ambiguous decisions resolved by reading produce consistent codebases. Ambiguous decisions resolved by guessing produce drift.

---

## 13) Architecture Constitution

*This section is a standalone summary. A future AI agent who has read no other architecture document should be able to read only this section and understand the intended engineering direction with sufficient fidelity to make correct architectural decisions.*

---

**What this architecture serves:**
A location-based social entertainment mobile application built for two platforms (Android and iOS) from a shared codebase. The architecture must support offline-first content creation, real-time social interaction, progression systems, and community features across five strategic phases of product growth. Product requirements always take precedence over architectural preferences.

**The five-layer model:**

| Layer | Owns |
|---|---|
| Presentation | UI rendering, state observation, user event dispatch |
| Domain | Business entities, business rules, use case contracts |
| Data | Repository implementations, local persistence, remote sync |
| Platform | Platform-specific implementations of shared contracts |
| Infrastructure | SDK configuration, secrets loading, DI setup, analytics init |

Dependencies flow downward. No layer imports from a layer above it. The Domain layer imports from neither Presentation nor Data — it is the architecture's center of gravity.

**The seven principles (in priority order):**
1. Product First — architecture is a delivery mechanism, not an end in itself
2. Simplicity Before Cleverness — boring and consistent beats elegant and novel
3. Offline-Tolerant by Default — user actions never fail because of the network
4. Single Source of Truth — every piece of state has one authoritative owner
5. Predictable State Flow — state flows one direction; errors are typed states, not swallowed exceptions
6. AI-Friendly Architecture — one pattern per concern, documented exhaustively, no magic
7. Consistency Over Local Optimization — a second pattern for anything requires a documented justification

**The eight engineering goals (in priority order):**
Correctness → Maintainability → Safety → Testability → Offline Resilience → AI Readability → Simplicity → Scalability

**The three non-negotiable constraints:**
1. No secrets in version control. Ever. For any environment. No exceptions.
2. No error swallowing. Every caught exception produces a typed state or an observability event.
3. No dependency direction violations. Domain never imports Data. Presentation never imports Data directly. The diagram in §7 is enforced, not advisory.

**The settled decisions:**
The ADR registry (`planning/meta/decisions/`, ADR-0001 through ADR-0020) holds the settled decisions. The architecture-level ones are: Room for local persistence (ADR-0001), GitLive Firebase as the backend SDK (ADR-0002), Koin for dependency injection (ADR-0003), StateViewModel as the presentation pattern (ADR-0004), type-safe Compose Navigation (ADR-0005), session-driven start navigation (ADR-0006), local-first reads with remote backfill (ADR-0007), expect/actual for platform-specific implementations (ADR-0008), Cloud Functions as backend compute with Messaging, Crashlytics and App Check (ADR-0015), RevenueCat for purchases and entitlements (ADR-0016), AdMob rewarded ads with server-side verification (ADR-0017), and owner-only precise location with a fuzzed public projection (ADR-0019). Product-level decisions are ADR-0009 to ADR-0014, ADR-0018 and ADR-0020. These decisions are not re-evaluated without a new ADR presenting new information.

**The one architecture test:**
*"Could an agent who has read the architecture documents and the relevant source files make a correct, consistent contribution to this area without asking any questions?"*

If the answer is no, the architecture or the documentation is incomplete. Fix the documentation or the structure before the next contribution is made.

**The document that owns each architectural concern:**
- Engineering philosophy and guardrails → this document
- Technology stack and version decisions → `architecture/TechStack.md`
- Layered system design → `architecture/Architecture.md`
- Local data schema → `architecture/DataModel.md`
- Remote data schema and security rules → `architecture/DataContracts.md`
- Dependency and layer enforcement → `architecture/RepositoryRules.md`
- Offline sync contract → `architecture/SyncStrategy.md`
- Firebase SDK conventions → `architecture/FirebaseStrategy.md`
- Cross-platform boundaries → `architecture/MultiplatformStrategy.md`
- Navigation system → `architecture/NavigationStrategy.md`
- Error handling, logging, analytics → `architecture/Observability.md`
- Secrets management → `architecture/SecurityAndSecrets.md`
- Settled decisions → `planning/meta/decisions/ADR-*.md`

# Repository Rules: Fart Map

> **Planning spine position:** authoritative repository-boundary and layer-enforcement contract.
> Upstream: `architecture/ArchitecturePrinciples.md`, `architecture/Architecture.md`, ADR-0001/0002/0007.
> Downstream: feature repository interfaces/implementations, data mapping code, review checklists.
> This document defines how repositories are designed and what they are allowed to do.

---

## 1) Purpose

This document enforces one repository pattern across the codebase so data flow remains predictable, testable, and offline-safe.

It directly prevents PoC failure modes:

- domain importing data entities,
- dual sync paths,
- direct Firebase/Room usage outside repository boundaries.

---

## 2) Canonical Repository Pattern

For every feature with persisted or remote-backed state:

1. Domain defines repository interface (`feature/*/domain`).
2. Data layer implements it (`feature/*/data`).
3. Presentation consumes use cases/ViewModels, not repository implementations.
4. All local/remote mapping is owned by data layer.

One concern, one repository contract.

---

## 3) Ownership and Location Rules

### Repository interface ownership

- Repository interfaces belong to feature domain packages.
- Shared cross-feature repository contracts belong in `core/domain` only if truly cross-feature.

### Repository implementation ownership

- Implementations belong to feature data packages.
- Shared data infrastructure (queue engine, db builder, common mappers) belongs in `core/data`.

### Mapping ownership

- Domain -> local entity mapping: data layer.
- Local entity -> domain mapping: data layer.
- Domain -> remote DTO mapping: data layer.
- Remote DTO -> domain mapping: data layer.

Domain models must not import Room entities or DTO types.

---

## 4) Allowed and Forbidden Repository Dependencies

### Allowed in repository implementations

- Feature domain interfaces and domain models.
- Local persistence APIs (Room DAO/entity in data layer only).
- Remote backend SDK (GitLive Firebase via data layer only).
- Shared sync engine contracts (`core/data`).
- Coroutine/Flow primitives.

### Forbidden in repository implementations

- Presentation types (`State`, `Action`, `Event`, composables).
- Navigation route types.
- Platform UI APIs.
- Business logic that belongs in use cases/domain rules.

### Forbidden outside repositories

- Direct DAO calls from presentation/domain.
- Direct Firestore/Auth/Storage calls from presentation/domain.

---

## 5) Repository Method Contracts

1. Read APIs expose reactive streams (`Flow<DomainType>` or `Flow<List<DomainType>>`) where state can change.
2. Write APIs return typed outcomes (`Result<T>` or equivalent typed error contract).
3. Repository API surface uses domain models, not local or remote models.
4. Errors are classified and propagated; they are never swallowed.

---

## 6) Offline-First Rules Inside Repositories

For user-initiated writes:

1. Validate input and ownership context.
2. Persist local mutation first.
3. Enqueue or dispatch remote sync via the single sync engine path.
4. Reflect state from local source-of-truth.

No inline-only remote write path may bypass the queue contract. Immediate remote send is allowed only as part of the same single sync mechanism, never as a second independent path.

---

## 7) Cross-Feature Repository Access

1. Features do not call each other's DAOs or repository implementations.
2. If Feature A needs Feature B data, dependency is through:
   - a domain-level shared contract in `core/domain`, or
   - an explicit use case boundary.
3. Direct `feature/a/data -> feature/b/data` dependency is prohibited.

---

## 8) Naming and Structure Conventions

- Interface: `[Feature]Repository`.
- Implementation: `[Feature]RepositoryImpl`.
- Mapper files: `[Feature]LocalMapper`, `[Feature]RemoteMapper` (or equivalent explicit names).
- Repository file contains orchestration logic, not giant inline mapping blocks when those mappers are reused.

---

## 9) Testing Contract

Each repository must have tests for:

1. Local-first write success path.
2. Offline queue behavior when remote is unavailable.
3. Remote success reconciliation (`sync_state` transitions).
4. Error propagation as typed failures.
5. Field round-trip coverage for required entities (especially visibility fields).

---

## 10) Review Checklist

- [ ] Interface in domain, implementation in data.
- [ ] No domain/presentation import of data entities or DTOs.
- [ ] Local-first write rule is honored.
- [ ] Single sync path used; no duplicate push logic.
- [ ] Errors are typed and observable.
- [ ] Cross-feature boundaries are respected.

---

*This is the repository boundary contract. Any data access pattern outside it is an architectural violation.*

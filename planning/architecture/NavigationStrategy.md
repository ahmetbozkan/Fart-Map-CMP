# Navigation Strategy: Fart Map

> **Planning spine position:** authoritative navigation architecture contract.
> Upstream: `architecture/ArchitecturePrinciples.md`, `architecture/Architecture.md`, ADR-0005, ADR-0006.
> Downstream: nav graph code, destination models, start-destination/session wiring, screen event handling.
> This document defines navigation behavior and boundaries. It does not define feature copy or visual design.

---

## 1) Purpose

This document standardizes navigation so routes are type-safe, session-aware, and consistent across all features.

It prevents:

- string-route drift,
- inconsistent start destination logic,
- graph ownership ambiguity.

---

## 2) Settled Navigation Decisions

1. Type-safe Compose Navigation with serializable routes (ADR-0005).
2. Session-driven start destination (ADR-0006).
3. ViewModel emits navigation events; UI layer executes navigation effects.

No alternate navigation mechanism may be introduced without ADR.

---

## 3) Route and Graph Contracts

### Route model rules

- Destinations are modeled as typed route objects/classes.
- Route arguments must be serializable and explicit.
- No raw string literals for primary navigation paths.

### Graph ownership rules

- Core navigation package owns graph assembly and top-level host wiring.
- Feature packages own their destination contracts and screen registration helpers.
- Cross-feature navigation occurs through core route contracts, not direct feature implementation coupling.

---

## 4) Session-Driven Start Contract

Start destination is resolved from session state:

- `Loading` -> startup/splash gate,
- `SignedOut` -> auth entry graph (Google / Apple / "Continue without account"),
- `Active(anonymous)` and `Active(linked)` -> map-first root graph (ADR-0018).

The root graph has four tabs: **Map (default) / Social / Rank / Profile** (ADR-0020). Log Fart is a modal destination launched from the map FAB, never a tab. Account linking is a destination inside the Active session reachable from Profile; completing it does not change the session or the back stack.

Map-first default is non-negotiable per product governance.

Imperative start-route overrides that bypass session mapping are forbidden.

---

## 5) Navigation Event Flow

Canonical flow:

1. User action dispatched to ViewModel.
2. ViewModel updates state and/or emits one-time navigation event.
3. UI collects event and calls navigator.
4. Nav host resolves typed route.

Navigation side effects must not be triggered from repositories or domain logic.

---

## 6) Back Stack and Re-entry Rules

1. Auth completion clears auth graph from back stack when entering logged-in root.
2. Sign-out clears authenticated graphs and returns to auth entry.
3. Deep links map to typed destination contracts and pass eligibility checks before render. Phase 1 deep link: `fartmap://fart/{fartId}` from the reaction push opens the Map tab with the marker detail sheet.
4. Back stack behavior for each graph transition must be deterministic and testable.

---

## 7) Feature Navigation Boundaries

1. A feature may request navigation by emitting a typed route/event only.
2. A feature must not mutate another feature's internal nav graph state directly.
3. Navigation arguments use domain-safe types; avoid passing persistence-layer internals.

---

## 8) Error and Guard Handling

1. Invalid or missing route arguments must map to explicit error handling path (safe fallback screen or recoverable state), never crash-only behavior.
2. Auth-required destinations must enforce session checks before entry.
3. Restricted content routes must enforce eligibility at data/backend layer as final authority.

---

## 9) Testing Contract

Minimum navigation tests:

1. session state -> start destination mapping (including anonymous Active),
1b. anonymous -> linked transition keeps the session, the uid and the back stack,
2. auth success transition clears auth back stack,
3. sign-out transition clears protected graphs,
4. typed route serialization/deserialization for required arguments,
5. key feature flows (map -> log -> map, profile -> follow actions) navigate correctly.

---

## 10) Anti-Patterns (Forbidden)

- String-based ad-hoc route literals as primary pattern.
- Multiple navigation frameworks in same app.
- Repository/domain-driven navigation.
- Start destination decided by UI-local hacks rather than session contract.
- Incomplete graph registration for defined feature routes.

---

## 11) Review Checklist

- [ ] Typed route contracts are used for all feature paths.
- [ ] Start destination follows session-driven mapping.
- [ ] Navigation events originate from ViewModel one-time events.
- [ ] Back stack transitions are explicit and tested.
- [ ] No cross-feature internal graph coupling.

---

*This is the navigation contract. Any non-typed or non-session-driven navigation behavior is out of spec.*

# ADR-0004: Custom StateViewModel Pattern

- Status: Accepted (hardening requirement satisfied by `architecture/Observability.md` §4 and `architecture/Architecture.md` Flow 5)
- Date: 2026-06-20
- Source: `planning/archive/meta/archive/PlanningAuditReport.md` §8

## Context
PoC presentation layer uses custom `StateViewModel` / `EventStateViewModel` abstractions for MVVM state handling.

## Decision
Retain custom **StateViewModel pattern** as the canonical presentation state contract.

## Rationale
- Minimal, clear state/action/event model.
- Works well with Compose Multiplatform shared presentation logic.
- Avoids framework lock-in.

## Consequences
- Must be hardened with explicit error modeling (PoC swallowed errors).
- `architecture/Observability.md` + `architecture/Architecture.md` should define error state conventions.

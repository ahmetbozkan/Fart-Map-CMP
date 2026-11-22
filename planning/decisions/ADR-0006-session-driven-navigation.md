# ADR-0006: Session-Driven Start Navigation

- Status: Accepted
- Date: 2026-06-20
- Source: `planning/archive/meta/archive/PlanningAuditReport.md` §8

## Context
PoC derives navigation state from auth/session flow rather than imperative route pushes.

## Decision
Use **session-driven navigation**: auth state -> session state -> start destination.

## Rationale
- Deterministic app entry behavior.
- Clear separation of auth/session logic from UI routing.
- Easier to reason about lifecycle and re-entry cases.

## Consequences
- `architecture/Architecture.md` should encode session states and destination mapping.
- Anonymous/authenticated capability gates should be explicit in product/features docs.

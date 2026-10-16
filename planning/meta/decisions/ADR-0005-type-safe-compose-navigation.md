# ADR-0005: Type-Safe Compose Navigation

- Status: Accepted
- Date: 2026-06-20
- Source: `planning/meta/archive/PlanningAuditReport.md` §8

## Context
PoC navigation uses serializable, type-safe route definitions and nested graphs.

## Decision
Adopt **type-safe Compose Navigation** with serializable destinations/arguments.

## Rationale
- Refactor-safe route evolution.
- Better compile-time guarantees than string routes.
- Aligns with PoC's strongest navigation pattern.

## Consequences
- `architecture/NavigationStrategy.md` should define route contracts and graph ownership.
- Incomplete PoC graph wiring (Social/Rank host gap) should be treated as anti-pattern.

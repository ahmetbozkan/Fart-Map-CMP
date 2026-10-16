# ADR-0001: Room over SQLDelight

- Status: Accepted
- Date: 2026-06-20
- Source: `planning/meta/archive/PlanningAuditReport.md` §8

## Context
The PoC already integrates Room KMP successfully. The rebuild brief left local storage as "Room (or SQLDelight if future planning recommends it)".

## Decision
Use **Room** as the local persistence technology for the new Compose Multiplatform project.

## Rationale
- Proven in PoC with DAO/entity/converter patterns.
- Reduces decision churn and migration risk during early rebuild.
- Aligns with extracted architecture intent in audit.

## Consequences
- `architecture/DataModel.md` and related skills should standardize Room patterns.
- Reopening this decision requires a superseding ADR with explicit migration cost analysis.

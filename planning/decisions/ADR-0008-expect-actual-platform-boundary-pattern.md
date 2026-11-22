# ADR-0008: expect/actual Platform Boundary Pattern

- Status: Accepted
- Date: 2026-06-20
- Source: `planning/archive/meta/archive/PlanningAuditReport.md` §8

## Context
PoC uses expect/actual seams for platform-specific capabilities (maps, auth providers, datastore, logging, etc.).

## Decision
Adopt **expect/actual boundary pattern** for platform-specific APIs while keeping domain/data/business logic shared.

## Rationale
- Maintains multiplatform architecture hygiene.
- Avoids Android/iOS logic leakage into shared domain.
- Scales with additional platform-specific integrations.

## Consequences
- `architecture/Architecture.md` should define what belongs in shared vs platform modules.
- New capabilities should default to shared abstraction + platform actual implementations.

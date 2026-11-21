# ADR-0003: Koin DI with expect/actual Platform Modules

- Status: Accepted
- Date: 2026-06-20
- Source: `planning/archive/meta/archive/PlanningAuditReport.md` §8

## Context
PoC DI uses Koin with shared module composition and platform-specific expect/actual injection seams.

## Decision
Use **Koin** as DI framework with `sharedModule + expect/actual platformModule` pattern.

## Rationale
- Keeps shared business logic platform-agnostic.
- Cleanly isolates platform bindings (maps, auth providers, DB builders, etc.).
- Already proven workable in PoC.

## Consequences
- `architecture/Architecture.md` and `.claude/skills/platform-capability/SKILL.md` standardize this composition.
- New platform capabilities should enter via platform modules, not shared leakage.

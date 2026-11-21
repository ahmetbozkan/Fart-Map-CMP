# Design SYSTEM: Fart Map

> Authority: design-layer operating contract.
> Upstream: `planning/product/features/`, `planning/product/Product.md`, `planning/product/ContentSafety.md`, `planning/architecture/Architecture.md`, `planning/WorkingAgreement.md`.
> Downstream: `planning/design/DesignSpecification.md`, per-screen specs, Stitch artifacts, implementation stories/tasks.

---

## 1) Purpose

This document defines how design work is structured, versioned, and handed off to implementation agents.

It owns design process, artifact standards, and design-to-implementation traceability.
It does not own product scope (product docs), architecture rules (architecture docs), or sprint sequencing (execution/roadmap docs).

---

## 2) Ownership Boundaries

## Design owns

- Screen behavior and states (loading/empty/error/success)
- UX flows and interaction sequencing
- Component usage patterns and visual consistency
- Stitch artifact references and mapping to implementation
- Platform UX differences where conventions differ but behavior stays equivalent

## Design does not own

- Feature inclusion/exclusion or phase placement
- Data model fields or backend rule behavior
- Layer boundaries, DI, sync implementation, or repository contracts

---

## 3) Design Artifacts

The design layer consists of:

1. `DesignSpecification.md` (authoritative visual/interaction baseline)
2. `SYSTEM.md` (this operating contract)
3. `UXFlowCatalog.md` (feature/journey/flow mapping)
4. `ScreenSpecificationTemplate.md` (standard per-screen contract)
5. `ImplementationHandoff.md` (handoff protocol and evidence checklist)

Per-screen files are added incrementally using `ScreenSpecificationTemplate.md` and must not duplicate global token/component rules already in `DesignSpecification.md`.

---

## 4) Stitch Integration Contract

Fart Map uses Stitch as the visual-design generation source. Owner process (2026-10-14): screens are designed **one at a time** in Stitch, each fully defined before the next; once all screens exist they may be imported into Figma so agents can read them through the Figma MCP. The planning system must support this pipeline:

Product spec -> UX requirements -> Design specification -> Stitch artifact -> Handoff package -> Story/task -> Implementation -> Validation

### Stitch artifact requirements

Each Stitch output used by engineering must include:

- artifact id/version
- source prompt summary
- mapped screen id(s)
- supported states (loading/empty/error/success)
- platform notes (Android/iOS differences if any)
- unresolved design questions

No Stitch artifact is implementation-authoritative until linked from a screen spec and handoff checklist.

### Design-ready definition for the first slice

A screen is "design ready" when `DesignSpecification.md` tokens apply, its screen spec under `screens/` is complete and its handoff record exists. A Stitch (or Figma) export is attached when available and refines visuals; its absence does not block the story from `Ready`. Placeholder ids (`TBD-UXF-…`) are replaced screen by screen as the owner produces designs.

---

## 5) Traceability Rules

Every screen/flow spec must maintain the chain:

- `links.feature` -> from `planning/product/features/`
- `links.loop` -> from `planning/product/Product.md`
- `links.journey_stage` -> from `planning/product/Product.md`
- `links.architecture` -> relevant architecture contracts

If a design artifact cannot be traced to at least one feature and one loop, it is out of scope.

---

## 6) Platform Difference Rules (CMP)

1. Behavioral parity is required across Android and iOS.
2. UX conventions may differ where platform norms differ (sheet gestures, back affordance, permission dialog framing).
3. Any platform-specific UX difference must be documented in the relevant screen spec under "Platform differences".
4. Design cannot require platform behavior that violates `Architecture.md` or `Architecture.md`.

---

## 7) Design-to-Implementation Gate

A UI-impacting story can move to `Ready` only if:

1. Required screen/flow spec exists or explicitly references `DesignSpecification.md` sections.
2. States and interaction outcomes are specified.
3. Safety/privacy UX requirements are explicit (visibility defaults, coarse location display, reporting surfaces).
4. Stitch artifact linkage is recorded if Stitch output is used.
5. Handoff checklist in `ImplementationHandoff.md` passes.

---

## 8) Validation Checklist

- [ ] Ownership boundaries respected (no architecture/product scope drift)
- [ ] Feature-loop-journey traceability complete
- [ ] Platform difference notes documented when needed
- [ ] Safety/privacy UX controls present where required
- [ ] Stitch artifact references versioned and linked
- [ ] Handoff package complete before execution readiness

---

## 9) AI Usage Guidance

1. Reuse `DesignSpecification.md` for global rules; avoid restating tokens/components.
2. For new UI stories, create or update only the minimum per-screen/flow docs needed for unambiguous implementation.
3. If design and architecture conflict, escalate with exact conflicting clauses.
4. Do not generate final visual assets in planning docs; record structure and handoff requirements.

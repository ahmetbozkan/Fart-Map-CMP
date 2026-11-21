# Design Implementation Handoff: Fart Map

> Authority: design-to-engineering handoff protocol for autonomous execution.
> Upstream: `planning/design/SYSTEM.md`, `planning/design/UXFlowCatalog.md`, `planning/design/DesignSpecification.md`, `planning/WorkingAgreement.md`, `planning/product/features/`, `planning/architecture/Architecture.md`.
> Downstream: story/task creation and UI implementation execution.

---

## 1) Purpose

This document defines the minimum package of design information required before UI-impacting work can enter `Ready`.

It owns handoff completeness criteria and verification steps.
It does not own sprint sequencing or implementation-level architecture decisions.

---

## 2) Handoff Package Requirements

A UI story is handoff-ready only when all are present:

1. Feature and flow links (`links.feature`, flow id from `UXFlowCatalog.md`).
2. Screen spec(s) using `ScreenSpecificationTemplate.md` or explicit section references to `DesignSpecification.md`.
3. Interaction state definitions (loading/empty/error/success/offline where relevant).
4. Privacy/safety UX requirements (visibility, location precision, report/block controls where applicable).
5. Platform difference notes for Android/iOS if behavior differs.
6. Stitch artifact mapping metadata if Stitch output is used.
7. Implementation acceptance criteria with measurable outcomes.

---

## 3) Handoff Record Template

```md
# HANDOFF-<id>: <feature/flow>

## Traceability
- feature:
- flow:
- loop:
- journey stage:
- architecture links:

## Included Design Artifacts
- design spec sections:
- screen specs:
- stitch artifacts:

## Interaction Contract
- primary actions:
- state transitions:
- error/offline behavior:

## Safety/Privacy UX Contract
- visibility behavior:
- location display restrictions:
- moderation/reporting affordances:

## Platform Notes
- Android-specific UX:
- iOS-specific UX:

## Implementation Acceptance Criteria
1. Given ... When ... Then ...
2. Given ... When ... Then ...

## Validation Plan
- tests:
- manual checks:
- accessibility checks:
```

---

## 4) DoR Coupling

For UI-impacting items, DoR is incomplete unless handoff package is complete.

This is an extension of `DefinitionOfReady.md` Feature-item gates:

- UX/design reference exists
- acceptance criteria testable
- risks documented

If handoff is incomplete, the story stays in `Backlog`.

---

## 5) Validation Checklist

- [ ] Handoff record created and linked from story
- [ ] Flow id and feature id mapped
- [ ] Screen states defined clearly
- [ ] Safety/privacy UX controls explicit
- [ ] Platform differences captured
- [ ] Stitch references versioned (if used)
- [ ] Acceptance criteria measurable

---

## 6) AI Usage Guidance

1. Do not start UI implementation from only feature prose; require handoff package.
2. If design artifacts conflict, resolve in this order: Product -> Architecture -> Design SYSTEM -> DesignSpecification -> Screen spec -> Stitch artifact.
3. Keep handoff concise but testable; ambiguity at handoff becomes implementation churn.

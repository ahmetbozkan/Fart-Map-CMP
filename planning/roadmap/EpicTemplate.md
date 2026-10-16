# Epic Template: Fart Map

> Authority: roadmap epic-level delivery contract.
> Upstream: `planning/roadmap/SYSTEM.md`, `planning/product/Features.md`, `planning/product/Prioritization.md`, `planning/architecture/Architecture.md`.
> Downstream: stories/tasks.

---

## 1) Purpose

Defines the minimum structure for epics that group related user-value stories.

It owns epic objective, acceptance boundary, and traceability.
It does not replace story-level acceptance criteria.

---

## 2) Template

```md
# EPIC-<phase>-<nn>: <title>

## Status
- Workflow state: Draft | Active | Completed | Superseded
- Owner: <human-or-agent>
- Parent phase: PHASE-<nn>

## Epic Intent
- Objective:
- Target user outcome:
- Why this is grouped as one epic:

## Traceability
- links.phase:
- links.feature:
- links.loop:
- links.pillar:
- links.architecture:
- ADR links (if applicable):

## Scope
- In scope:
- Out of scope:

## Acceptance Boundary
- Epic-level acceptance condition 1:
- Epic-level acceptance condition 2:

## Dependencies
- blocked_by:
- enables:

## Risks
- Scope risk:
- Dependency risk:
- Safety/privacy risk:

## Planned Stories
- STORY-...:
- STORY-...:

## Evidence and Notes
- Prioritization evidence:
- Open questions:
```

---

## 3) Validation Checklist

- [ ] Parent phase exists and linked
- [ ] Feature/loop/pillar/architecture links complete
- [ ] Scope boundary explicit
- [ ] Planned stories listed or intentionally deferred
- [ ] Prioritization evidence captured

---

## 4) AI Usage Guidance

1. Keep epic boundaries around one coherent outcome, not mixed domains.
2. If links.architecture is unclear, block epic decomposition and resolve contract mapping first.
3. Do not create stories until epic acceptance boundary is testable.

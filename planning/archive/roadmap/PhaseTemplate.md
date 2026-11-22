# Phase Template: Fart Map

> Authority: roadmap phase-level delivery contract.
> Upstream: `planning/product/ReleaseStrategy.md`, `planning/product/Prioritization.md`, `planning/roadmap/SYSTEM.md`.
> Downstream: epic/story/task/sprint planning.

---

## 1) Purpose

Defines the minimum structure for a phase artifact.

It owns phase goals, constraints, and gate evidence.
It does not own feature details or task decomposition.

---

## 2) Template

```md
# PHASE-<nn>: <title>

## Status
- Workflow state: Draft | Active | Completed | Superseded
- Owner: <human-or-agent>
- Horizon: <date-range-or-release-window>

## Phase Intent
- Objective:
- Why now:
- User-value outcome:

## Scope Boundaries
- Included feature systems:
- Explicit exclusions:
- Dependency notes:

## Traceability
- ReleaseStrategy links:
- Product pillar links:
- Loop links:
- Success metrics:

## Entry Gates
- Gate 1:
- Gate 2:

## Exit Gates
- Exit criterion 1:
- Exit criterion 2:

## Risk Register
- Product/scope risk:
- Technical risk:
- Safety/privacy risk:

## Planned Epics
- EPIC-...:
- EPIC-...:

## Evidence and Notes
- Gate verification evidence:
- Open issues/blockers:
```

---

## 3) Validation Checklist

- [ ] Phase ID valid and unique
- [ ] Release strategy links present
- [ ] Scope and exclusions explicit
- [ ] Entry and exit gates measurable
- [ ] Planned epics enumerated or explicitly deferred

---

## 4) AI Usage Guidance

1. Keep phase scope aligned to release strategy; do not invent new phase goals.
2. Use explicit exclusions to prevent scope creep.
3. Treat unmet entry gates as blockers before creating phase epics.

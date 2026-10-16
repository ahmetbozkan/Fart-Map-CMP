# Sprint Template: Fart Map

> Authority: roadmap sprint-level planning contract.
> Upstream: `planning/roadmap/SYSTEM.md`, `planning/execution/SprintRules.md`, `planning/execution/Workflow.md`, `planning/execution/DefinitionOfReady.md`.
> Downstream: sprint execution and review evidence.

---

## 1) Purpose

Defines the minimum structure for sprint plans built from Ready stories/tasks.

It owns sprint goal, committed scope, and review evidence.
It does not override workflow transitions or DoD.

---

## 2) Template

```md
# SPRINT-<yyyy>-<nn>: <title>

## Status
- Workflow state: Planned | Active | Completed | Cancelled
- Owner: <human-or-agent>
- Timebox: <start-date> -> <end-date>

## Sprint Goal
- Primary outcome:
- Success measure:

## Capacity and Selection
- Capacity heuristic used:
- Selection rationale:
- Carry-over policy:

## Committed Scope
- Stories:
  - STORY-... (state: Ready)
  - STORY-... (state: Ready)
- Tasks:
  - TASK-... (state: Ready)
  - TASK-... (state: Ready)

## Dependency and Risk Check
- Blocking dependencies:
- Risk notes:
- Safety/privacy checks:

## Sprint Operating Rules
- WIP policy:
- Blocker escalation path:
- Scope-change policy:

## Review and Evidence
- Completed items:
- Deferred items:
- Validation evidence summary:
- DoD/Review checklist references:
```

---

## 3) Validation Checklist

- [ ] Sprint ID valid
- [ ] All committed items are in Ready state
- [ ] Capacity rationale documented
- [ ] Dependencies and risks reviewed
- [ ] Review evidence section prepared

---

## 4) AI Usage Guidance

1. Never commit Backlog items directly into a sprint.
2. Keep committed scope coherent with one sprint goal.
3. Use explicit deferrals instead of silent carry-over.

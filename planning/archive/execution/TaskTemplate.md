# Task Template: Fart Map

> Authority: execution work-item contract for tasks.
> Upstream: `planning/execution/SYSTEM.md`, `planning/execution/Workflow.md`, `planning/execution/DefinitionOfReady.md`, `planning/execution/DefinitionOfDone.md`, `planning/execution/StoryTemplate.md`.
> Downstream: `planning/roadmap/tasks/*`, implementation and validation execution.

---

## 1) Purpose

This template defines the minimum structure for implementation tasks derived from stories.

It owns task-level execution metadata, dependency edges, and evidence capture.
It does not own product-level prioritization (owned by `planning/product/Prioritization.md`) or story-level scope (owned by `StoryTemplate.md`).

---

## 2) Task Rules

1. Every task must have exactly one parent story.
2. Every task must keep the parent story traceability chain.
3. Tasks should be small, reviewable, and independently verifiable.
4. A task cannot move to `In Progress` unless dependencies are resolved and DoR passes.

---

## 3) Template

```md
# TASK-<id>: <title>

## Status
- Type: feature | bug | refactor | chore | docs
- Priority: P0 | P1 | P2 | P3
- Workflow state: Backlog | Ready | In Progress | Blocked | Review | Done | Verified | Cancelled
- Owner: <human-or-agent>
- Parent story: STORY-<id>

## Intent
- Objective:
- Expected outcome:

## Traceability
- Feature/system link:
- Loop link:
- Pillar link:
- Architecture contracts touched:
- ADR links (if applicable):

## Scope
- In scope:
- Out of scope:

## Implementation Plan
1.
2.
3.

## Dependencies
- blocked_by: <task/story IDs or none>

## Risks
- Security/privacy:
- Correctness/regression:
- Platform parity:

## Validation
- Tests to run:
- Build checks:
- Manual checks:

## Completion Evidence
- Changes made:
- Validation output summary:
- DoD gate result:
```

---

## 4) Validation Checklist

- [ ] Parent story exists and is linked
- [ ] Traceability fields are complete
- [ ] Dependencies explicit
- [ ] Validation steps are concrete
- [ ] Risks documented for touched surfaces

---

## 5) AI Usage Guidance

1. If a task spans unrelated concerns, split it.
2. Keep one task owner and one clear objective.
3. Promote blockers explicitly using `Blocked` state and blocker metadata.
4. Include enough evidence in the task to make review reproducible.

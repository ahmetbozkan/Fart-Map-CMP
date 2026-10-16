# Story Template: Fart Map

> Authority: execution work-item contract for stories.
> Upstream: `planning/execution/SYSTEM.md`, `planning/execution/Workflow.md`, `planning/execution/DefinitionOfReady.md`, `planning/execution/DefinitionOfDone.md`, `planning/product/Features.md`, `planning/architecture/Architecture.md`.
> Downstream: `planning/roadmap/stories/*`, sprint planning, implementation tasks.

---

## 1) Purpose

This template defines the minimum structure for a story that can be executed autonomously by an AI agent.

It owns story-level scope, acceptance criteria, traceability, and evidence references.
It does not own task decomposition internals (owned by `TaskTemplate.md`) or sprint selection (owned by `SprintRules.md`).

---

## 2) Story Rules

1. A story is a vertical, user-visible slice.
2. A story must be executable in one sprint.
3. A story must pass DoR before entering `Ready`.
4. A story must include upward traceability: Pillar -> Loop -> Feature.
5. A story must include architecture links for every touched contract.

---

## 3) Template

```md
# STORY-<id>: <title>

## Status
- Type: feature | bug | refactor | chore | docs
- Priority: P0 | P1 | P2 | P3
- Workflow state: Backlog | Ready | In Progress | Blocked | Review | Done | Verified | Cancelled
- Owner: <human-or-agent>
- Phase: Phase 1 | Phase 2 | Phase 3 | Phase 4 | Phase 5

## Why This Exists
- Problem:
- Expected user outcome:
- Primary metric target (`planning/product/SuccessMetrics.md`):
- Counter-metric guardrail:

## Traceability (Required)
- Pillar link(s):
- Loop link(s):
- Feature/system link(s):
- Journey stage(s):
- Persona(s):
- Architecture contract links:
- ADR links (if applicable):

## Scope
- In scope:
- Out of scope:

## Acceptance Criteria (Given/When/Then)
1. Given ... When ... Then ...
2. Given ... When ... Then ...

## Dependencies
- blocked_by: <story/task IDs or none>

## Risks
- Safety/privacy risks:
- Abuse/fairness risks:
- Technical risks:

## Design and UX Inputs
- Design source(s):
- Stitch artifact(s) (if available):

## Validation Plan
- Required tests:
- Manual checks:
- Security/rule checks:

## Task Breakdown
- TASK-...:
- TASK-...:

## Completion Evidence
- DoD checklist result:
- Test/build evidence:
- Contract conformance notes:
- STATE/ChangeLog updates (if planning docs changed):
```

---

## 4) Validation Checklist

- [ ] User-visible vertical slice (not a pure internal change unless story type is refactor/chore/docs)
- [ ] Traceability section complete
- [ ] Acceptance criteria measurable and testable
- [ ] Dependencies explicit
- [ ] Safety/privacy risk notes present when relevant
- [ ] Validation plan includes build/test checks

---

## 5) AI Usage Guidance

1. Reject any story with missing traceability fields.
2. If criteria are ambiguous, tighten criteria before implementation.
3. If architecture contracts are missing, move story to `Blocked` and create prerequisite work.
4. Do not start tasks until this story passes DoR.

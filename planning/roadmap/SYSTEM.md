# Roadmap SYSTEM: Fart Map

> Authority: delivery-sequencing contract for roadmap artifacts.
> Upstream: `planning/product/ReleaseStrategy.md`, `planning/product/Prioritization.md`, `planning/execution/SYSTEM.md`, `planning/execution/Workflow.md`, `planning/execution/StoryTemplate.md`, `planning/execution/TaskTemplate.md`.
> Downstream: `planning/roadmap/phases/*`, `planning/roadmap/epics/*`, `planning/roadmap/stories/*`, `planning/roadmap/tasks/*`, `planning/roadmap/sprints/*`.

---

## 1) Purpose

This document defines how roadmap artifacts are structured and linked.

It owns hierarchy, identifiers, and traceability requirements.
It does not own product scope, architecture rules, or execution state-machine behavior.

---

## 2) Hierarchy Ownership

Roadmap decomposition is strict:

`Phase -> Epic -> Story -> Task -> Sprint`

Ownership by level:

- `phases/`: phase goals and entry/exit gates aligned to `ReleaseStrategy.md`
- `epics/`: feature-system outcomes under a phase
- `stories/`: vertical user-value slices
- `tasks/`: implementation units under stories
- `sprints/`: time-boxed execution groupings from Ready stories/tasks

---

## 3) Traceability Contract

Every artifact below `phase` must include links upward and to architecture:

- `links.phase`
- `links.epic` (for stories/tasks)
- `links.story` (for tasks)
- `links.feature`
- `links.loop`
- `links.pillar`
- `links.architecture`

Artifacts with broken links are invalid.

---

## 4) Identifier Conventions

- Phase: `PHASE-01`, `PHASE-02`, ...
- Epic: `EPIC-<phase>-<nn>` (example `EPIC-01-01`)
- Story: `STORY-<epic>-<nn>` (example `STORY-01-01-01`)
- Task: `TASK-<story>-<nn>` (example `TASK-01-01-01-01`)
- Sprint: `SPRINT-<yyyy>-<nn>`

IDs are immutable once published.

---

## 5) Readiness Rules by Layer

1. Phases require release-gate definitions from `ReleaseStrategy.md`.
2. Epics require product prioritization evidence and phase fit.
3. Stories must use `execution/StoryTemplate.md` and pass DoR.
4. Tasks must use `execution/TaskTemplate.md` and pass DoR.
5. Sprint inclusion follows `execution/SprintRules.md`.

---

## 6) Validation Checklist

- [ ] Artifact located in correct hierarchy folder
- [ ] ID format valid and unique
- [ ] Required traceability links complete
- [ ] DoR/DoD references consistent with execution docs
- [ ] No scope introduced outside `planning/product/Features.md`

---

## 7) AI Usage Guidance

1. Do not create delivery content (epics/stories/tasks/sprints) until current `STATE.md` authorizes it.
2. Start with structure and templates, then instantiate incrementally.
3. Keep one source of truth per level; avoid copying details across layers.

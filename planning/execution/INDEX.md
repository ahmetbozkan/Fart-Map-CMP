# Execution INDEX: Fart Map

> Purpose: navigation map for the execution layer.
> Upstream: `planning/meta/INDEX.md`, `planning/execution/SYSTEM.md`.
> Downstream: roadmap work-item artifacts and sprint operations.

---

## 1) Document Ownership Map

| Document | Owns | Does not own |
|---|---|---|
| `SYSTEM.md` | Agent control loop, decision authority, execution kernel | Status transition details, story/task formatting internals |
| `Workflow.md` | State machine and valid transitions | Product priority rules, architecture constraints |
| `DefinitionOfReady.md` | Start gate criteria | Finish gate criteria |
| `DefinitionOfDone.md` | Completion gate criteria | Sprint capacity planning |
| `StoryTemplate.md` | Story contract and required traceability/evidence fields | Task implementation details |
| `TaskTemplate.md` | Task contract and execution evidence | Story-level scope and business outcome framing |
| `SprintRules.md` | Sprint entry criteria, capacity model, in-sprint discipline | Feature prioritization method |
| `ReviewChecklist.md` | Pre-Done review script and quality checks | DoR/DoD definitions themselves |

---

## 2) Work Item Lifecycle (Execution Layer View)

1. Draft candidate story/task from upstream product + architecture + design inputs.
2. Validate with `DefinitionOfReady.md`.
3. Move through workflow states defined in `Workflow.md`.
4. Review via `ReviewChecklist.md`.
5. Close via `DefinitionOfDone.md`.
6. Synchronize state in `planning/roadmap/STATE.md` when repository-level status changes.

---

## 3) Required Traceability Fields

All stories/tasks must include:

- `links.pillar`
- `links.loop`
- `links.feature`
- `links.architecture`

And include ADR references when a settled decision applies.

---

## 4) Validation Checklist

- [ ] All execution docs exist and have non-overlapping ownership
- [ ] Story and task templates align with SYSTEM schema fields
- [ ] Sprint rules rely on product prioritization rather than replacing it
- [ ] Review checklist enforces DoD evidence, not opinion-only review

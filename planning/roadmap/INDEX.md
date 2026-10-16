# Roadmap INDEX: Fart Map

> Purpose: navigation and authoring entrypoint for roadmap artifacts.
> Authority: `planning/roadmap/SYSTEM.md` and `planning/roadmap/STATE.md`.

---

## 1) Folder Map

- `planning/roadmap/SYSTEM.md` - hierarchy, ID conventions, and traceability contract
- `planning/roadmap/STATE.md` - current authorization state and next allowed work
- `planning/roadmap/INDEX.md` - this navigation map
- `planning/roadmap/PhaseTemplate.md` - canonical phase artifact template
- `planning/roadmap/EpicTemplate.md` - canonical epic artifact template
- `planning/roadmap/SprintTemplate.md` - canonical sprint artifact template
- `planning/roadmap/phases/` - concrete phase instances (when authorized)
- `planning/roadmap/epics/` - concrete epic instances (when authorized)
- `planning/roadmap/stories/` - concrete story instances (when authorized)
- `planning/roadmap/tasks/` - concrete task instances (when authorized)
- `planning/roadmap/sprints/` - concrete sprint instances (when authorized)

---

## 2) Template Ownership

- Phase artifacts must use `PhaseTemplate.md`.
- Epic artifacts must use `EpicTemplate.md`.
- Story artifacts must use `planning/execution/StoryTemplate.md`.
- Task artifacts must use `planning/execution/TaskTemplate.md`.
- Sprint artifacts must use `SprintTemplate.md` and `planning/execution/SprintRules.md`.

---

## 3) Authoring Sequence

1. Verify authorization in `planning/roadmap/STATE.md`.
2. Create phase instances.
3. Create epic instances under phases.
4. Create story/task instances only after phase and epic links exist.
5. Build sprint artifacts only from Ready stories/tasks.

---

## 4) Validation Checklist

- [ ] Artifact type uses correct template
- [ ] ID format follows `planning/roadmap/SYSTEM.md`
- [ ] Upward traceability links are complete
- [ ] Product scope references `planning/product/Features.md`
- [ ] DoR/DoD workflow links reference execution docs

---

## 5) AI Usage Guidance

1. Do not instantiate phase/epic/story/task/sprint artifacts unless `STATE.md` authorizes it.
2. Keep one file per artifact and avoid duplicate definitions across levels.
3. Treat broken links as a blocker; fix traceability before status transitions.

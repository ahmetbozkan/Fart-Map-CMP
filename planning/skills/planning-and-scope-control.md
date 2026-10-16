# Skill: Planning and Scope Control

## Purpose
Convert phase-aligned product scope into executable stories/tasks with traceability.

## Sources
- `planning/product/Prioritization.md`
- `planning/product/ReleaseStrategy.md`
- `planning/execution/StoryTemplate.md`
- `planning/execution/TaskTemplate.md`

## Procedure
1. Validate phase eligibility and hard gates.
2. Create story using `StoryTemplate.md`.
3. Decompose into tasks using `TaskTemplate.md`.
4. Preserve pillar/loop/feature/architecture links for each item.
5. Set dependencies and acceptance criteria before Ready transition.

## Failure recovery
- Missing upstream definition (feature/design/architecture): block and create prerequisite planning item.

## Validation
- [ ] Story/task fields complete
- [ ] Traceability chain intact
- [ ] Scope respects current phase and non-goals

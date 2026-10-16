# Review Checklist: Fart Map

> Authority: pre-completion quality gate for stories and tasks.
> Upstream: `planning/execution/DefinitionOfDone.md`, `planning/execution/Workflow.md`, `planning/meta/AgentGuardrails.md`, architecture and product contracts linked by the work item.
> Downstream: `Review -> Done` decisions across `planning/roadmap/stories/*` and `planning/roadmap/tasks/*`.

---

## 1) Purpose

This checklist is the final review pass before marking work `Done`.

It owns review discipline and evidence quality.
It does not replace DoD; it operationalizes DoD into a repeatable reviewer script for autonomous agents.

---

## 2) Required Review Inputs

Before review starts, reviewer reads:

1. Work item file (story/task)
2. Linked product sources (`links.feature`, loop, pillar)
3. Linked architecture sources (`links.architecture`)
4. Relevant ADRs
5. Validation output attached by implementer

If any required input is missing, return to `In Progress`.

---

## 3) Review Checks

## Scope and traceability

- [ ] Change matches story/task scope
- [ ] No hidden scope creep
- [ ] Upward links (pillar/loop/feature) are complete
- [ ] Architecture links are complete and relevant

## Contract conformance

- [ ] Product constraints preserved (map-first, humor-first, privacy defaults, no pay-to-win)
- [ ] Architecture boundaries preserved (no Presentation->Data or Domain->Data violations)
- [ ] Settled ADRs respected

## Safety and security

- [ ] No secrets introduced in tracked files
- [ ] Visibility/privacy behaviors preserved where touched
- [ ] Server-authoritative checks preserved for access control surfaces
- [ ] Logs and telemetry avoid sensitive payload leakage

## Correctness and quality

- [ ] Acceptance criteria pass with explicit evidence
- [ ] Required tests executed and results attached
- [ ] No swallowed error paths introduced
- [ ] Offline/sync behavior preserved where relevant

## State and governance sync

- [ ] Workflow status transitions valid
- [ ] Blockers recorded or cleared explicitly
- [ ] `planning/roadmap/STATE.md` updated if project-level state changed
- [ ] `planning/meta/ChangeLog.md` updated for meaningful planning/governance changes

---

## 4) Review Outcomes

Allowed outcomes only:

1. **Pass** -> `Review -> Done`
2. **Fail: Rework needed** -> `Review -> In Progress`
3. **Fail: Blocked** -> `Review -> Blocked` with blocker metadata

No partial pass.

---

## 5) AI Usage Guidance

1. Reject work without evidence even if code/docs look correct.
2. Prefer explicit contract references over reviewer assumptions.
3. If review reveals contradiction between authoritative docs, escalate immediately.
4. Record concise failure reasons so rework is deterministic.

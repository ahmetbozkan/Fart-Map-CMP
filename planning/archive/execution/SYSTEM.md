# SYSTEM: Fart Map Execution Kernel

> **Authority level:** execution operating kernel for autonomous and human-assisted delivery.
> Upstream: `planning/meta/AgentGuardrails.md`, `planning/roadmap/STATE.md`, `planning/product/ProductGovernance.md`, `planning/architecture/ArchitecturePrinciples.md`, ADRs.
> Downstream: `execution/Workflow.md`, `execution/DefinitionOfReady.md`, `execution/DefinitionOfDone.md`, all roadmap execution artifacts.

---

## 1) Purpose

This document defines the closed control loop an AI agent must run for every unit of work.

Without this kernel, the planning system is a document library. With this kernel, the planning system becomes an operable delivery system.

---

## 2) The Execution Control Loop

Every work cycle follows this exact sequence:

1. **Orient**
   - Read `planning/CLAUDE.md`.
   - Read `planning/roadmap/STATE.md`.
   - Read `planning/meta/AgentGuardrails.md`.
   - Confirm current authorized scope.

2. **Select Work**
   - Choose the top-priority item in `Ready` state from roadmap artifacts.
   - If no item is `Ready`, run readiness preparation (not implementation).

3. **Validate Readiness**
   - Run `execution/DefinitionOfReady.md` checklist.
   - If any gate fails, move item to `Blocked` with explicit blocker reason.

4. **Implement / Update**
   - Execute work within architecture and product constraints.
   - Follow single-pattern rules from architecture docs and ADRs.

5. **Verify Completion**
   - Run `execution/DefinitionOfDone.md` checklist.
   - If any DoD gate fails, return item to `In Progress`.

6. **Record and Sync State**
   - Update roadmap work item status.
   - Update `planning/roadmap/STATE.md` if project-level state changed.
   - Update `planning/meta/ChangeLog.md` for meaningful planning/system changes.
   - Add ADR entry if decision boundary required it.

7. **Publish Session Outcome**
   - Report completed work, blockers, and next authorized action.

The loop is complete only when Step 6 is finished.

---

## 3) Work Item Schema (Required)

Every story/task in execution artifacts must include these fields:

| Field | Required | Description |
|---|---|---|
| `id` | Yes | Stable identifier |
| `title` | Yes | Concise action-oriented title |
| `type` | Yes | `feature`, `bug`, `refactor`, `chore`, `docs` |
| `status` | Yes | Must be one of workflow states |
| `priority` | Yes | `P0` / `P1` / `P2` / `P3` |
| `owner` | Yes | Human/agent owner |
| `phase` | Yes | Product phase alignment |
| `links.pillar` | Yes | Pillar(s) from ProductPillars |
| `links.loop` | Yes | Loop(s) from CoreLoops |
| `links.feature` | Yes | Feature/system source in product docs |
| `links.architecture` | Yes | Relevant architecture contract docs |
| `links.adr` | Conditional | Required when tied to settled decisions |
| `acceptance_criteria` | Yes | Testable Given/When/Then criteria |
| `dependencies` | Yes | Blocking item IDs or `none` |
| `risk_notes` | Yes | Safety/privacy/security/abuse risks |

Work items missing required fields are invalid and cannot enter `Ready`.

---

## 4) Decision Authority Matrix

All decisions must fit one of three classes.

### 4.1 Decide (agent-autonomous)

Agent may decide without explicit user approval:

- naming and file organization inside established patterns,
- non-behavioral refactors that preserve contracts,
- internal implementation details already covered by architecture docs,
- copy/format improvements that do not change policy, scope, or rules.

### 4.2 Record (ADR required, no immediate user blocking unless risky)

Agent may proceed only after recording ADR when introducing:

- a new reusable pattern for an existing concern,
- a new dependency with architectural impact,
- a change in layer boundary behavior,
- an update to contract assumptions used by multiple features.

### 4.3 Escalate (user approval required before proceeding)

Agent must stop and escalate for:

- changes to non-negotiable product principles,
- privacy-default changes (visibility, location exposure, following-list exposure),
- monetization changes affecting fairness/core access,
- security posture changes (secrets handling, trust boundary relaxation),
- reversal/supersession of accepted ADRs,
- scope moves across release phases.

---

## 5) Escalation Triggers

Immediate escalation is required when any of these occur:

1. Two authoritative docs conflict with no safe deterministic resolution.
2. Required implementation contract doc is missing for current task.
3. A feature request violates ProductGovernance hard constraints.
4. A change would require storing/committing secrets in tracked files.
5. A proposed flow weakens privacy-by-default or moderation baseline.
6. A task depends on undefined backend enforcement behavior.

Escalation output must include: issue, affected docs, risk, recommended default.

---

## 6) Readiness and Completion Gates

- **Start gate:** item passes `DefinitionOfReady`.
- **Finish gate:** item passes `DefinitionOfDone`.
- **No bypass policy:** there is no "ship anyway" route for unmet gates.

For bugs and refactors:

- Bug fix requires reproducible failing case before fix.
- Refactor requires characterization protection before structural changes.

---

## 7) Dependency and Sequencing Rules

1. No item starts while declared blockers remain unresolved.
2. Dependencies are resolved in topological order.
3. `P0` safety/security/correctness blockers preempt all feature work.
4. A feature item cannot start until required architecture contracts exist.
5. Implementation cannot start if `STATE.md` says not authorized.

---

## 8) Verification Contract

Every completed item must have evidence appropriate to its type:

| Item type | Minimum evidence |
|---|---|
| Feature | Acceptance criteria pass + tests + contract alignment |
| Bug | Repro case fails before fix and passes after fix + regression test |
| Refactor | Characterization tests before/after + behavior unchanged |
| Security/privacy | Rule/path validation + negative access checks |
| Docs/governance | Cross-reference integrity + state/changelog sync |

Evidence is required in the execution artifact, not just terminal output.

---

## 9) Required State Synchronization

After meaningful progress, agent must update:

1. roadmap artifact status,
2. `planning/roadmap/STATE.md` when project state changes,
3. `planning/meta/ChangeLog.md` for planning/operating changes,
4. ADR registry when decisions are introduced/superseded.

Work is not considered complete until state synchronization is done.

---

## 10) Kernel Integrity Rules

The execution kernel is valid only if all are true:

- control loop followed,
- work item schema complete,
- decision authority respected,
- readiness/completion gates enforced,
- state synchronized.

Any exception must be explicitly documented and escalated.

---

*This file is the execution kernel. `Workflow.md` defines status transitions, while DoR/DoD define gate criteria.*

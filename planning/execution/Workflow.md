# Workflow: Fart Map Execution State Machine

> **Authority level:** canonical work-state model and transition protocol.
> Upstream: `execution/SYSTEM.md`, `meta/AgentGuardrails.md`, `roadmap/STATE.md`.
> Downstream: all roadmap execution artifacts (phases, epics, stories, tasks, sprint boards).

---

## 1) Purpose

This document defines how work items move from idea to verified completion.

It prevents ambiguous status, skipped gates, and hidden blockers by enforcing one state machine for all execution artifacts.

---

## 2) Canonical Work States

Use these states only:

| State | Meaning |
|---|---|
| `Backlog` | Not ready; parked candidate work |
| `Ready` | Fully specified and can start immediately |
| `In Progress` | Actively being implemented or authored |
| `Blocked` | Cannot proceed due to explicit blocker |
| `Review` | Work done; under validation against DoD |
| `Done` | Passed DoD and merged/accepted |
| `Verified` | Post-merge/acceptance validation confirmed in system context |
| `Cancelled` | Intentionally stopped; not to be resumed |

No custom statuses are allowed.

---

## 3) Transition Rules

Allowed transitions only:

| From | To | Condition |
|---|---|---|
| Backlog | Ready | Meets Definition of Ready |
| Backlog | Cancelled | Explicit de-scope decision |
| Ready | In Progress | Assigned and started |
| Ready | Backlog | Priority/scope changed before start |
| In Progress | Review | Implementation complete + evidence prepared |
| In Progress | Blocked | New blocker discovered |
| Blocked | In Progress | Blocker resolved |
| Blocked | Backlog | Blocker unresolved and item deferred |
| Review | Done | Passes Definition of Done |
| Review | In Progress | DoD failed; rework required |
| Done | Verified | Post-completion checks pass |
| Done | In Progress | Regression or missed requirement found |
| Any (except Done/Verified) | Cancelled | Explicit cancellation decision |

Any transition not listed is invalid.

---

## 4) Entry and Exit Criteria by State

### Backlog

- **Entry:** candidate work identified.
- **Exit:** DoR complete -> Ready, or explicit cancel.

### Ready

- **Entry:** all DoR gates pass.
- **Exit:** work starts -> In Progress.

### In Progress

- **Entry:** active ownership assigned and work started.
- **Exit:**
  - implementation complete with evidence -> Review,
  - blocker encountered -> Blocked.

### Blocked

- **Entry:** a specific blocker prevents progress.
- **Required fields:** blocker description, blocker owner, unblock condition, target date.
- **Exit:** blocker resolved -> In Progress, or deferred -> Backlog.

### Review

- **Entry:** implementation appears complete; DoD verification pending.
- **Exit:**
  - DoD pass -> Done,
  - DoD fail -> In Progress.

### Done

- **Entry:** all DoD gates passed and accepted.
- **Exit:** post-completion validation -> Verified.

### Verified

- **Entry:** downstream/system-level checks confirm no hidden regression.
- **Exit:** none (terminal success state).

### Cancelled

- **Entry:** explicit scope/prioritization decision.
- **Exit:** none (terminal state unless explicitly re-created as new item).

---

## 5) Work Item Priority and Queue Discipline

Priorities:

- `P0`: correctness/security/privacy blockers
- `P1`: execution-critical architecture/contracts
- `P2`: planned feature delivery
- `P3`: optimization/nice-to-have

Queue rules:

1. `P0` preempts all lower-priority active work.
2. Start highest-priority `Ready` item with resolved dependencies.
3. Do not start a second `In Progress` item for same owner unless explicitly parallelized by plan.

---

## 6) Dependency and Blocker Protocol

Each item must declare dependencies explicitly.

### Dependency states

- `none`
- `blocked_by: [item_ids]`

An item cannot move to `In Progress` while dependencies remain not `Done` (or `Verified` where required).

### Blocker severity

| Severity | Definition | Action |
|---|---|---|
| `Critical` | Stops multiple items / safety risk | Immediate escalation |
| `High` | Stops current item | Resolve before continuing |
| `Medium` | Partial progress possible | Continue unaffected sub-work |
| `Low` | Cosmetic/document clarity blocker | Track and resolve in normal flow |

---

## 7) Required Artifact Updates per Transition

When state changes, update execution artifacts immediately.

| Transition | Required updates |
|---|---|
| Backlog -> Ready | Fill DoR evidence fields |
| Ready -> In Progress | Set owner, start timestamp |
| In Progress -> Blocked | Add blocker metadata |
| In Progress -> Review | Attach completion evidence |
| Review -> Done | Record DoD pass summary |
| Done -> Verified | Record verification notes |
| Any -> Cancelled | Record cancellation reason |

If project-level status changes, also update `roadmap/STATE.md`.

---

## 8) Escalation Hooks in Workflow

Escalate immediately when:

- item blocked by doc conflict across authoritative sources,
- required architecture contract missing,
- security/privacy risk discovered,
- request violates ProductGovernance non-negotiables,
- ADR reversal appears necessary.

Escalation output must include: current state, blocker, risk, proposed default path.

---

## 9) Workflow Anti-Patterns (Disallowed)

1. Moving Backlog -> In Progress directly.
2. Marking Done without Review.
3. Marking Done without DoD evidence.
4. Keeping items indefinitely in In Progress without status notes.
5. Hiding blockers in comments instead of `Blocked` state.
6. Closing work without syncing `STATE.md` when repository-level status changed.

---

## 10) Minimal Execution Board Template

Use this structure in roadmap artifacts:

| id | title | type | priority | status | owner | deps | links.feature | links.architecture | notes |
|---|---|---|---|---|---|---|---|---|---|

Status values must be from §2 only.

---

*This workflow defines how work progresses. `SYSTEM.md` defines why and in what order the workflow is applied. DoR/DoD define gate criteria.*

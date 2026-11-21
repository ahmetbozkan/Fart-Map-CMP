# Definition of Done (DoD): Fart Map

> **Authority level:** completion gate for execution work items.
> Upstream: `execution/SYSTEM.md`, `execution/Workflow.md`, `execution/DefinitionOfReady.md`, product and architecture authoritative docs.
> Downstream: all `Review -> Done` decisions.

---

## 1) Purpose

This document defines when a work item can be marked `Done`.

If any mandatory DoD gate fails, the item must return to `In Progress`.

---

## 2) DoD Core Rule

A work item is `Done` only when:

1. required implementation/document change is complete,
2. acceptance criteria are satisfied,
3. quality and safety gates pass,
4. execution artifacts and repository state are synchronized.

---

## 3) Mandatory Gates (All Item Types)

| Gate | Requirement | Pass condition |
|---|---|---|
| Scope completion | Work objective fully addressed | No unresolved in-scope criteria |
| Acceptance criteria | All criteria validated | Each criterion marked pass with evidence |
| Contract alignment | Product + architecture rules preserved | No contradiction with ProductGovernance, ArchitecturePrinciples, Architecture, ADRs |
| Safety/security | No new privacy/security regressions | Security-sensitive checks pass for touched areas |
| Error behavior | Failures are observable and handled | No silent-failure paths introduced |
| State sync | Tracking artifacts updated | Workflow state, roadmap state, and relevant logs updated |

---

## 4) Evidence Requirements by Work Type

### Feature item

- [ ] Acceptance criteria pass evidence.
- [ ] Layer/contract conformance verified.
- [ ] Relevant tests added/updated and passing.
- [ ] No violation of locked monetization/privacy constraints.

### Bug item

- [ ] Repro failed before fix and passes after fix.
- [ ] Regression protection added (test/validation check).
- [ ] Root cause documented.

### Refactor item

- [ ] Characterization checks pass before and after.
- [ ] No behavior change unless explicitly planned.
- [ ] Architecture boundary quality improved or preserved.

### Security/privacy item

- [ ] Backend trust-boundary requirements preserved.
- [ ] Sensitive data handling validated.
- [ ] Logging redaction/safety validated.

### Docs/governance item

- [ ] Upstream/downstream references updated.
- [ ] `STATE.md` updated if project-level status changed.
- [ ] `ChangeLog.md` updated for meaningful planning/system updates.

---

## 5) Verification Checklist Before `Review -> Done`

- [ ] Work item schema remains complete and accurate.
- [ ] All dependencies remain satisfied.
- [ ] No new blockers were hidden or deferred silently.
- [ ] Required docs were updated where ownership changed.
- [ ] If a new decision was made, ADR created or decision explicitly escalated.

---

## 6) Post-Done Validation (`Done -> Verified`)

`Done` means completion gate passed.

`Verified` is an additional check for systemic confidence:

- [ ] downstream integration context is healthy,
- [ ] no immediate regressions observed,
- [ ] state ledger remains accurate.

High-risk items (security/privacy/core data flow) should not be considered fully closed until `Verified`.

---

## 7) State Synchronization Requirements

Before marking Done:

1. Update work item status and evidence.
2. Update `planning/roadmap/STATE.md` when repo-level readiness/state changes.
3. Update `planning/meta/ChangeLog.md` when planning or governance docs changed.
4. Update `planning/meta/INDEX.md` if tree/read-order/ownership changed.

If synchronization is missing, item is not Done.

---

## 8) DoD Failure Handling

If any gate fails during review:

1. Move item from `Review` back to `In Progress`.
2. Record failing gate(s) explicitly.
3. Add corrective sub-tasks if needed.
4. Re-run verification after fixes.

No partial "done enough" status is allowed.

---

## 9) DoD Anti-Patterns (Disallowed)

1. Marking Done without evidence.
2. Skipping safety/security checks for non-security-labeled items.
3. Treating green local behavior as sufficient without contract checks.
4. Closing item while `STATE.md` and changelog remain stale.
5. Declaring completion while known blocker remains unresolved.

---

*Definition of Done is the quality firewall. If DoD is weak, execution speed becomes defect speed.*

# Definition of Ready (DoR): Fart Map

> **Authority level:** start gate for execution work items.
> Upstream: `execution/SYSTEM.md`, `execution/Workflow.md`, `meta/AgentGuardrails.md`, product and architecture authoritative docs.
> Downstream: all story/task start decisions.

---

## 1) Purpose

This document defines when a work item is allowed to move from `Backlog` to `Ready` and then to `In Progress`.

If DoR is not satisfied, implementation must not begin.

---

## 2) DoR Core Rule

A work item is **Ready** only when all mandatory gates pass and evidence is recorded.

No exceptions for urgency.

---

## 3) Mandatory Gates (All Item Types)

| Gate | Requirement | Pass condition |
|---|---|---|
| Scope clarity | Clear problem and expected outcome | Item has concise, testable objective |
| Traceability | Product and architecture linkage exists | `links.pillar`, `links.loop`, `links.feature`, `links.architecture` filled |
| Authority alignment | No conflict with constitutions/ADRs | Checked against ProductGovernance, ArchitecturePrinciples, ADRs |
| Dependencies | Blocking dependencies resolved | `dependencies = none` or all blockers `Done`/`Verified` |
| Acceptance criteria | Testable criteria defined | Given/When/Then or equivalent measurable criteria present |
| Risk annotation | Security/privacy/safety impact known | Risk notes present; mitigation path stated |
| Ownership | Accountable owner assigned | Owner and priority assigned |

If any gate fails, item remains in `Backlog`.

---

## 4) Additional Gates by Work Type

### Feature item

- [ ] Phase alignment confirmed against `product/ReleaseStrategy.md`.
- [ ] Feature/system source section identified in product docs.
- [ ] Required architecture contracts exist for touched layers.
- [ ] UX/design reference exists (if UI-impacting).

### Bug item

- [ ] Repro steps documented (deterministic or bounded probabilistic).
- [ ] Expected vs actual behavior captured.
- [ ] Suspected scope/impact documented.
- [ ] Initial validation plan defined.

### Refactor item

- [ ] Refactor intent explicitly non-functional (or functional change called out).
- [ ] Characterization coverage plan exists.
- [ ] Invariants to preserve are listed.
- [ ] Rollback strategy noted if risk is high.

### Security/privacy item

- [ ] Mapped to `architecture/SecurityAndSecrets.md` section(s).
- [ ] Backend enforcement expectations defined (not client-only).
- [ ] Sensitive data handling path identified.

### Docs/governance item

- [ ] Owner doc and upstream/downstream references identified.
- [ ] State/changelog update impact identified.

---

## 5) Read-Before-Start Checklist

Before changing status to `In Progress`, assignee confirms:

- [ ] `planning/CLAUDE.md` read this session.
- [ ] Current `planning/roadmap/STATE.md` read.
- [ ] `planning/meta/AgentGuardrails.md` read.
- [ ] Relevant product and architecture contracts reviewed.
- [ ] Relevant ADRs reviewed.

---

## 6) Ready Evidence Template

Every `Ready` item must include:

```text
ReadyEvidence:
  problem:
  expected_outcome:
  links:
    pillar:
    loop:
    feature:
    architecture:
    adr:
  acceptance_criteria:
  dependencies:
  risks:
  owner:
  priority:
  phase:
```

Items lacking this block are not Ready.

---

## 7) DoR Failure Handling

When DoR fails:

1. Keep item in `Backlog`.
2. Record failed gates explicitly.
3. Create prerequisite sub-items if needed (e.g., missing architecture contract).
4. Re-evaluate after prerequisites complete.

Do not bypass by moving item directly to `In Progress`.

---

## 8) DoR Anti-Patterns (Disallowed)

1. Starting work with "we'll define details while coding".
2. Missing traceability links.
3. Implicit dependencies not listed in item metadata.
4. Acceptance criteria that are subjective/non-testable.
5. Security/privacy-sensitive work without explicit risk handling notes.

---

*Definition of Ready protects execution quality by preventing ambiguous starts. Definition of Done protects finish quality.*

# AI Operating System Review — Fart Map

> Phase 3 deliverable of `meta/archive/Planning-Rebuild.md`.
> Purpose: evaluate whether the planning system (as refined in Phase 2) is sufficient to let autonomous AI agents run the full software lifecycle for a brand-new Compose Multiplatform project with minimal human intervention.
> This is a capability/readiness review — not implementation. No code, stories, or tasks are produced.

---

## 1) Framing: What "Operating System" Means Here

An AI Operating System is the set of documents and protocols that let an agent answer four questions at any moment, without a human:

1. **Where am I?** (current state, what's next) → navigation
2. **What are the rules?** (binding constraints) → governance
3. **How do I do this task?** (recipes) → skills
4. **Am I allowed to do this, and is it done?** (guardrails + gates) → control

A planning system "passes" if an agent can complete a full loop — *pick work → understand it → build it → verify it → record it → advance state* — using only the documents, looping back to a human only at defined escalation points.

This review scores the system against the **six autonomy capabilities** the brief names, then identifies weak points and improvements.

---

## 2) Readiness Scorecard

Scoring against the **Phase-2 recommended structure** (i.e., assuming the P0/P1 additions are created). Levels: **Blocked** / **Partial** / **Ready**.

| Capability | Readiness | Primary enablers | Primary blocker if missing |
|---|---|---|---|
| Autonomous story creation | **Partial → Ready** | `StoryTemplate.md`, `DefinitionOfReady.md`, product spine, `Prioritization.md` | No traceability rule linking story → feature → pillar |
| Autonomous sprint planning | **Partial** | `SprintRules.md`, `roadmap/STATE.md`, `Prioritization.md` | No capacity/estimation model for an agent |
| Autonomous implementation | **Ready (conditional)** | architecture/* rules, skills/*, `DesignSystem.md`, `DataContracts.md` | Conditional on skills being concrete (examples, not prose) |
| Autonomous architecture decisions | **Partial** | `Architecture.md`, `meta/decisions/` (ADRs), guardrails | No decision-authority boundary (what an agent may decide alone) |
| Autonomous refactoring | **Partial** | `skills/refactoring.md`, `TestingStrategy.md`, `ReviewChecklist.md` | No regression-safety contract (tests-before-refactor gate) |
| Autonomous bug fixing | **Partial** | `skills/debugging.md`, `Observability.md` | No bug-intake/repro/verify protocol |

**Overall:** the refined structure is **architecturally sufficient** but **operationally under-specified**. The folders and documents exist; what's missing are the *protocols that connect them into a closed control loop*. Sections 3–8 detail each capability; §9 lists the cross-cutting weak points; §10 gives the prioritized fixes.

---

## 3) Autonomous Story Creation

**Goal:** an agent converts a feature into well-formed, ready-to-build stories without a human.

| Have | Gap |
|---|---|
| `StoryTemplate.md`, `TaskTemplate.md` (structure) | No **traceability rule**: every story must link upward (feature → core loop → pillar) and downward (tasks). Without it, scope drifts from product intent. |
| `DefinitionOfReady.md` (P1, gate) | No **slicing heuristic**: how to split a feature into vertically-shippable stories (the most common autonomous-failure mode is stories that are too big or non-shippable). |
| Product spine (Vision→…→Features) | No **acceptance-criteria standard**: criteria must be testable/observable so DoD can be checked autonomously. |

**Improvement (P1):** add a **Traceability Rule** to `execution/SYSTEM.md` (or `StoryTemplate.md`) — every work item carries `links: {pillar, loop, feature, parent}`. Add a **vertical-slice heuristic** + an **acceptance-criteria format** (Given/When/Then) so stories are machine-verifiable.

---

## 4) Autonomous Sprint Planning

**Goal:** an agent assembles the next sprint from ready stories.

| Have | Gap |
|---|---|
| `SprintRules.md`, `roadmap/STATE.md` (P0) | No **capacity model**. Human sprints use velocity; an agent needs an analog (e.g., complexity budget per sprint, or "N ready stories with all deps satisfied"). |
| `Prioritization.md` (P2) | No **dependency-resolution rule**: a story can't enter a sprint until its blocking stories/ADRs are done. Needs an explicit "ready = deps satisfied" check. |

**Improvement (P1):** define an **agent capacity heuristic** in `SprintRules.md` (complexity points or fixed-count budget) and a **dependency gate** ("a story is sprint-eligible only if all `links.parent`/blockers are Done"). Sprint planning becomes deterministic: *filter ready → topologically order by deps → fill to budget by priority.*

---

## 5) Autonomous Implementation

**Goal:** an agent builds a story to spec.

This is the **strongest** capability once the Phase-2 architecture docs exist, because the PoC already proves the patterns and the `architecture/` + `skills/` + `design/` triad covers the "how."

| Have | Gap |
|---|---|
| `architecture/*` binding rules; `skills/*` recipes; `design/*` per-screen; `DataContracts.md`; `DesignSystem.md` | **Skills risk being prose, not playbooks.** Autonomy depends on skills containing *concrete code-shaped examples* (a `StateViewModel` skeleton, a repository+DAO+mapper example, a sync pending-op example), not abstract description. |
| `RepositoryRules.md` (layer direction) | No **"golden path" walkthrough**: one end-to-end example (add a feature touching UI→VM→UseCase→Repo→Room+Firestore→sync) that an agent can pattern-match against. The PoC's `AddFartUseCase` flow is the natural template. |

**Improvement (P1):** mandate that each `skills/*` doc include a **canonical example** and an **anti-pattern** (the PoC's swallowed-errors / domain→data leak make perfect "don't do this" cases). Add one **golden-path reference** in `architecture/Architecture.md`.

---

## 6) Autonomous Architecture Decisions

**Goal:** an agent makes or escalates technical decisions correctly.

| Have | Gap |
|---|---|
| `Architecture.md`, `meta/decisions/` (ADRs), `AgentGuardrails.md` | No **decision-authority matrix**: which decisions an agent may make alone (e.g., naming, internal structure), which require an ADR (e.g., new dependency, schema change), which require a human (e.g., security model, monetization). |
| ADR folder seeded from Audit §8 | No **ADR trigger rule**: *when* must an agent open an ADR vs. just proceed. |

**Improvement (P0):** add a **Decision Authority Matrix** to `AgentGuardrails.md` with three tiers — *Decide* / *Record (ADR)* / *Escalate*. This is the single most important control for "minimal human intervention" because it prevents both over-asking (agent stalls) and over-reaching (agent makes irreversible/unsafe calls).

---

## 7) Autonomous Refactoring

**Goal:** an agent improves code without changing behavior or breaking layers.

| Have | Gap |
|---|---|
| `skills/refactoring.md`, `TestingStrategy.md`, `ReviewChecklist.md` | No **regression-safety contract**: refactoring is only safe with characterization tests first. The PoC has **near-zero tests** — an agent refactoring blind is dangerous. |
| `RepositoryRules.md` (target invariants) | No **invariant set** to refactor *toward* (e.g., "no domain→data imports", "all errors mapped to Result"). The Audit's inconsistency list is the natural seed. |

**Improvement (P1):** add a **refactor pre-gate** to `skills/refactoring.md`: "no refactor without characterization tests covering the touched behavior." Encode the Audit's findings (layer direction, error model, field round-tripping) as **refactor target invariants** an agent can drive toward and verify.

---

## 8) Autonomous Bug Fixing

**Goal:** an agent triages, reproduces, fixes, and verifies a defect.

| Have | Gap |
|---|---|
| `skills/debugging.md`, `Observability.md` (logs/crash/analytics) | No **bug protocol**: intake → reproduce → isolate → fix → regression-test → verify against acceptance. Without a repro+verify loop, fixes are unverifiable autonomously. |
| `Observability.md` (signals) | No **definition of "verified fixed"** tied to a reproduction case + a new test. |

**Improvement (P1):** add a **Bug Lifecycle protocol** (likely in `Workflow.md` or a `skills/debugging.md` section): every fix requires a failing repro first and a regression test that locks it, mirroring the refactor safety contract.

---

## 9) Cross-Cutting Weak Points (System-Level)

These affect *all six* capabilities and are the real determinants of autonomy.

| # | Weak point | Impact | Severity |
|---|---|---|---|
| 9.1 | **No closed control loop defined.** Docs exist as references but no single protocol says "this is the loop an agent runs every cycle." | Agent doesn't know the *sequence* of consult-INDEX → check-STATE → verify-DoR → build-by-skills → run-ReviewChecklist → record-ADR → update-STATE. | **High** |
| 9.2 | **No traceability backbone.** Nothing enforces pillar→feature→story→task links. | Scope drift; can't prove work serves product intent; can't prioritize. | **High** |
| 9.3 | **No decision-authority boundary.** | Agent either stalls (asks too much) or over-reaches (unsafe autonomy). | **High** |
| 9.4 | **Test-safety is assumed, not gated.** PoC has ~no tests; refactor/bugfix without a test gate is unsafe. | Silent regressions in an offline-first, sync-heavy app. | **High** |
| 9.5 | **Skills may be prose, not executable playbooks.** | Implementation autonomy degrades to guesswork. | **Medium** |
| 9.6 | **No estimation/capacity model.** | Sprint planning can't be deterministic. | **Medium** |
| 9.7 | **No state-machine for work items.** (Backlog→Ready→In-Progress→Review→Done→Verified) | Ambiguous status; STATE.md can't be authoritative. | **Medium** |

---

## 10) Prioritized Improvements

### P0 — Required for safe autonomy
1. **Define the Agent Control Loop** (in `execution/SYSTEM.md`): the explicit per-cycle sequence binding INDEX → STATE → DoR → skills → ReviewChecklist → ADR → STATE. *Fixes 9.1.*
2. **Decision Authority Matrix** (in `execution/AgentGuardrails.md`): Decide / Record / Escalate tiers + escalation triggers. *Fixes 9.3, §6.*
3. **Work-Item State Machine** (in `execution/Workflow.md`): canonical statuses + transition rules; `roadmap/STATE.md` reflects them. *Fixes 9.7.*

### P1 — Required before implementation phases
4. **Traceability Rule** (links on every work item: pillar/loop/feature/parent). *Fixes 9.2, §3, §4.*
5. **Test-Safety Gates** for refactor and bugfix (characterization/repro test required first). *Fixes 9.4, §7, §8.*
6. **Skill Playbook Standard** (every skill = rule-reference + canonical example + anti-pattern + checklist). *Fixes 9.5, §5.*
7. **Acceptance-Criteria Standard** (Given/When/Then, machine-verifiable) tied to DoD. *Fixes §3.*

### P2 — Required to scale
8. **Agent Capacity/Estimation heuristic** + **dependency gate** for sprint planning. *Fixes 9.6, §4.*
9. **Golden-Path reference** (one end-to-end feature walkthrough). *Strengthens §5.*
10. **Bug Lifecycle protocol** + **"verified fixed" definition**. *Strengthens §8.*

---

## 11) The Minimum Autonomy Kernel

If only a few documents could exist, autonomy would hinge on this kernel. An agent can run the full loop with these alone:

```
meta/INDEX.md            → where everything is
roadmap/STATE.md         → where we are / what's next
execution/SYSTEM.md      → the control loop (the kernel)
execution/AgentGuardrails.md → what I may/may not do + escalation + decision tiers
execution/DefinitionOfReady.md + DefinitionOfDone.md → start/finish gates
execution/ReviewChecklist.md → self-verification before "done"
architecture/* (rules)   → constraints I must obey
skills/* (playbooks)     → how I actually build
meta/decisions/          → settled choices I must not relitigate
```

Everything else (product spine, design, systems) feeds *what* to build; the kernel governs *how the agent operates*. **The kernel is currently the least-developed part of the system** — it is the focus of the P0 improvements.

---

## 12) Conclusion

The planning system, with Phase-2 additions, has the **right documents** but not yet the **right protocols**. It is a well-stocked library without a librarian's procedure. The decisive gap is the **control layer**: a defined agent loop, a decision-authority boundary, a work-item state machine, a traceability backbone, and test-safety gates. These are small in volume but high in leverage — they convert a pile of governance docs into a system an agent can *operate* safely with minimal human intervention.

**Recommendation:** before any roadmap/implementation work begins, author the three **P0** control documents (Control Loop, Decision Authority Matrix, Work-Item State Machine). They are the true "operating system kernel." Phase 4 (dependency map) and Phase 5 (product docs) can proceed in parallel, since they concern *what* to build rather than *how the agent operates*.

# Sprint Rules: Fart Map

> Authority: execution planning contract for sprint composition and flow control.
> Upstream: `planning/execution/SYSTEM.md`, `planning/execution/Workflow.md`, `planning/execution/DefinitionOfReady.md`, `planning/execution/DefinitionOfDone.md`, `planning/product/Prioritization.md`, `planning/product/ReleaseStrategy.md`.
> Downstream: `planning/roadmap/sprints/*`, sprint boards, story/task selection.

---

## 1) Purpose

This document defines how a sprint is assembled and operated so an AI agent can plan and execute sprint work deterministically.

It owns sprint eligibility, capacity heuristics, dependency ordering, and sprint-level done criteria.
It does not own feature priority logic (owned by `planning/product/Prioritization.md`) or item state transitions (owned by `Workflow.md`).

---

## 2) Sprint Entry Rules

Only items in `Ready` state may enter a sprint.

Each candidate item must satisfy:

1. DoR passed with recorded evidence.
2. Dependencies resolved (`none` or all blockers `Done/Verified`).
3. Traceability complete (pillar/loop/feature/architecture links).
4. Phase alignment with `planning/product/ReleaseStrategy.md`.
5. No unresolved escalation trigger.

---

## 3) Capacity Heuristic (Agent-Friendly)

Use complexity points per story:

- `S` (small) = 1
- `M` (medium) = 2
- `L` (large) = 3

Default sprint budget:

- Solo developer + autonomous agent budget: **6 points per sprint**.

Rules:

1. Do not exceed 6 points unless user explicitly approves an exception.
2. Include at least one buffer slot (>=1 point unallocated) for bug/regression work.
3. If a sprint contains a `P0` item, reserve at least 2 points for stabilization.

---

## 4) Selection Algorithm

1. Filter candidate backlog to `Ready` items.
2. Remove any item with unresolved dependencies.
3. Sort by priority (`P0` -> `P3`) then by product PriorityScore ranking.
4. Topologically order by dependency graph.
5. Add items until budget limit is reached.
6. Validate phase coherence (no premature phase work unless explicitly allowed).

---

## 5) In-Sprint Flow Rules

1. One `In Progress` story per owner unless explicit parallelization plan exists.
2. `P0` blockers preempt all lower-priority work.
3. New scope discovered mid-sprint goes to `Backlog` unless required to finish committed story.
4. Blocked items must carry unblock condition and owner.
5. No bypass of DoD for sprint closure.

---

## 6) Sprint Done Criteria

A sprint is complete when:

1. All committed items are `Done` or explicitly moved with reason.
2. No hidden blockers remain.
3. Evidence for each completed item is recorded.
4. `planning/roadmap/STATE.md` is updated if repo-level status changed.
5. `planning/meta/ChangeLog.md` is updated for meaningful planning changes.

---

## 7) Sprint Artifact Template

Use `planning/roadmap/SprintTemplate.md`; it is the single owner of the sprint schema. Record the capacity heuristic from §3 in its "Capacity and Selection" section.

---

## 8) AI Usage Guidance

1. Prefer finishing fewer high-quality stories over overcommitting breadth.
2. If uncertainty is high, downsize sprint rather than overpacking.
3. Keep phase discipline: do not pull later-phase stories to fill unused capacity.
4. Record why items were deferred; avoid silent carry-over.

# Working Agreement

> Replaces execution/SYSTEM, Workflow, DefinitionOfReady, DefinitionOfDone, ReviewChecklist, SprintRules, the story/task/epic/phase/sprint templates, and AgentGuardrails (originals in `planning/archive/`). Applies to humans and agents.

## 1) Authority (highest wins on conflict)

1. `planning/product/Product.md`: principles, non-goals, hard constraints.
2. Accepted ADRs in `planning/decisions/`. A **Proposed** ADR is not authority.
3. `planning/architecture/Architecture.md`.
4. Concern docs: product (`Scope.md`, `features/`, `Monetization.md`, `ContentSafety.md`, Systems docs), architecture (`DataContracts.md`, `Security.md`, …), design (`screens/*.md`).
5. `planning/STATE.md` and stories, for what to do now.

`planning/archive/` is never authority. The PoC repository is reference only. A lower source that contradicts a higher one is stale: fix the lower doc, never copy it into code.

## 2) Work loop (the `/implement-story` skill runs this)

1. Read `planning/STATE.md` (the SessionStart hook prints its "Now" block), then the story file.
2. Pull only the spec you need: the feature file under `planning/product/features/`, the relevant contract section, the screen spec. Use the `spec-scout` agent when the reading is broad.
3. Use plan mode for anything beyond a small change. Branch `NN-short-name` from `main`.
4. Implement within the rules (`.claude/rules/` loads them by path).
5. Run `scripts/verify.sh` (or `/verify`) until it is green.
6. Tick the story checklist, add evidence (commands run plus PR link), and update `STATE.md`.
7. Commit (message ends with the Co-Authored-By line), push the branch, and open a **draft** PR. Never push to `main`, never merge, never force-push.

## 3) States

`Todo` → `In progress` → `Review` (draft PR open, CI running) → `Done` (merged by the owner, CI green). `Blocked` carries a one-line reason and who unblocks it. No other states.

## 4) Story file (`planning/stories/STORY-PP-EE-SS.md`)

```
# STORY-PP-EE-SS: <title>
Status: Todo | In progress | Review | Done | Blocked (<reason>)   Epic: EPIC-PP-EE   Features: <ids>   ADRs: <ids>
## Goal          one or two sentences, user-visible outcome
## Scope         in / out
## Acceptance    numbered Given/When/Then, each testable
## Tasks         - [ ] checklist (no separate task files)
## Validation    commands + manual checks (both platforms)
## Evidence      PR link, notable output, deviations
```

**Ready when:**
- the acceptance criteria are testable;
- the referenced specs and contracts exist;
- blockers are named;
- privacy and monetization implications are stated if the story touches them.

Phase, pillar, loop and persona traceability is not repeated per story; it lives in `Scope.md` and the feature files.

## 5) Definition of Done

- [ ] Acceptance criteria met on Android **and** iOS (parity is part of done).
- [ ] `scripts/verify.sh --full` is green locally (on Windows, iOS is covered by CI).
- [ ] CI is green on the PR (secret scan, Android, iOS, backend).
- [ ] Tests:
  - ViewModel state transitions;
  - repository round-trip for new fields;
  - a rules test for every allowed and denied case of a changed collection;
  - a regression test for every bug fix.
- [ ] No secrets, no swallowed errors, no layering violations (detekt + Konsist enforce this).
- [ ] The story and `planning/STATE.md` are updated. A changed contract updates its owner doc in the same PR. A changed decision gets an ADR (Proposed until the owner accepts).

## 6) Decide, record, or escalate

| Class | Examples | Action |
|---|---|---|
| Decide alone | Naming and files inside established patterns, refactors that keep contracts, implementation details covered by docs, copy polish within the voice | Just do it |
| Record (ADR, Proposed) | A new reusable pattern, a new dependency with architectural impact, a layer-boundary change, a contract assumption used by several features | Write the ADR, keep working on what doesn't depend on it, flag it in STATE |
| Escalate (stop, ask the owner) | Product principles, privacy or safety defaults, monetization posture, security posture, reversing an accepted ADR, moving scope across phases, a feature not in `features/`, two authoritative docs in hard conflict, anything requiring a committed secret | Report the current state, the proposed change, the risk and a recommended default |

When docs leave room for interpretation, choose the option that is safer for privacy, consistent with the ADRs, simpler and phase-appropriate, and easiest for the next agent to understand.

## 7) Never

- Commit secrets or real Firebase configs; read or print them either (hooks block it).
- Plan or ship pay-to-win, paywalls on core actions, passive ads, or weaker privacy defaults.
- Turn the map-first product into a feed-first one.
- Delete passing tests or skip hooks.
- Treat PoC code as precedent. Classify each pattern you borrow as Preserve, Evolve or Reject.

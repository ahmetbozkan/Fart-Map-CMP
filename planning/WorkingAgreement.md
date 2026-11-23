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
7. Commit (message ends with the Co-Authored-By line), push the branch, and open a **draft** PR. Never push to `main`, never force-push. In this interactive loop the agent never merges; the owner does.

### Autopilot (the `/autopilot-story` skill, driven by `scripts/autopilot.sh`)

The same loop without the owner's approvals. One fresh headless session per story; each owner gate is replaced by a check:

| Gate | Interactive | Autopilot |
|---|---|---|
| Story authored | Owner confirms | Agent checks it against "Ready when" (§4) |
| Open questions | Owner answers | Agent decides from the docs (§1, §6 tie-break) and lists each answer under `## Decisions` in the story |
| Plan | Owner approves plan mode | `## Plan` in the story, reviewed by the `plan-reviewer` agent (up to 2 revisions) |
| PR review | Owner reads the PR | `code-reviewer` (+ `security-reviewer` when relevant) posts findings on the PR; the agent fixes them (up to 2 more rounds) |
| CI | Owner checks | Agent watches `gh pr checks`, fixes failures through the retry ladder (§6) |
| Merge | Owner merges | `gh pr ready`, then `gh pr merge <n> --auto --squash`; GitHub merges once the required checks pass |

The story is set to `Done` and STATE updated inside the PR, since it merges only on green CI. By default the driver stops at each epic boundary (`--cross-epics` lets the agent split and start the next epic) and auto-merges every PR that passes review and CI (`--hold-sensitive` holds PRs touching rules, functions, entitlements or location for the owner). Setup: `.claude/README.md` "Autopilot".

## 3) States

`Todo` → `In progress` → `Review` (draft PR open, CI running) → `Done` (merged, CI green: by the owner, or by GitHub auto-merge under autopilot). `Blocked` carries a one-line reason and who unblocks it. No other states.

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

**Retry ladder (technical problems).** A failing test, build or CI job, a reviewer finding, a plan blocker: the first fix plus **at most 2 retries, each with a different approach** (a new root-cause hypothesis or design, not the same fix again). Log each as `attempt N: <approach> → <result>` in the story's Evidence. After the third failed attempt, escalate. Weakening tests, adding suppressions or baselines, or widening rules to get green is never an attempt.

**Escalate rows skip the ladder.** Anything in the Escalate row above, a dependency on a Proposed ADR, or a step that needs an owner console action goes to the owner at once, without retries.

**How autopilot escalates:** push the work to the story branch, set the story to `Blocked (<reason>; owner)` and update STATE, then open a GitHub issue labelled `needs-owner` with what's blocked, every attempt, the options, a recommended default and the reply that unblocks it. The owner answers in the issue (agent comments start with `<!-- autopilot -->`, so any other comment is the owner's) and re-runs the driver; the next session reads the answer and continues.

## 7) Never

- Commit secrets or real Firebase configs; read or print them either (hooks block it).
- Plan or ship pay-to-win, paywalls on core actions, passive ads, or weaker privacy defaults.
- Turn the map-first product into a feed-first one.
- Delete passing tests or skip hooks.
- Merge any way other than GitHub auto-merge on green checks: no `--admin`, no API merges, no changes to branch protection or repository settings.
- Treat PoC code as precedent. Classify each pattern you borrow as Preserve, Evolve or Reject.

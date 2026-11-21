---
name: implement-story
description: Run the full work loop for one story: read the story and its spec, plan, branch, implement, verify, update the story and STATE, commit, push and open a draft PR. Use when the user says "implement STORY-…", "do the next story", or "/implement-story".
disable-model-invocation: true
argument-hint: "[STORY-PP-EE-SS | next]"
---

# Implement a story

Argument: a story id, or `next` (take the first unchecked story in the active epic of `planning/ROADMAP.md`).

1. **Orient (cheap).** Read `planning/STATE.md` and the story file `planning/stories/<id>.md`. If the story file doesn't exist yet, author it from the ROADMAP line using the template in `planning/WorkingAgreement.md` §4, show it to the owner, and stop until they confirm.
2. **Check readiness.**
   - The acceptance criteria are testable.
   - The referenced specs exist.
   - The story isn't blocked by a Proposed ADR (STATE "Pending owner decisions").
   - If anything fails, report it and stop.
3. **Collect only the needed spec.** For one feature, read its section in `planning/product/features/<system>.md`, the screen spec, and the relevant `DataContracts.md` section. If the reading spans more than about 3 docs, delegate to the `spec-scout` agent and work from its excerpt.
4. **Plan.** Enter plan mode for anything beyond a small change. The plan names the files, the tests, and both platforms.
5. **Branch.** `git switch -c NN-short-name` from an up-to-date `main`.
6. **Implement** using the matching skills (`new-feature`, `platform-capability`, `firestore-change`, `cloud-function`, `monetization`, `analytics-event`). Run independent backend work in parallel with the `backend-engineer` agent when it touches only `backend/`.
7. **Verify.** `/verify --full` until it's green. Then run the `code-reviewer` agent on the diff, plus `security-reviewer` if rules, auth, location, monetization or logging changed. Fix the findings that matter.
8. **Record.**
   - Tick the story tasks.
   - Fill **Evidence** (commands run, notable output, deviations).
   - Set the status to `Review`.
   - Update `planning/STATE.md` (Now / Next).
   - If a contract changed, update its owner doc in the same commit.
9. **Ship.**
   - `git add` the specific paths, never `-A` over untracked config.
   - Commit with a clear message ending in the Co-Authored-By line.
   - `git push -u origin <branch>`.
   - `gh pr create --draft --fill`, then edit the body: summary, story link, the DoD checklist from `.github/pull_request_template.md`, and on Windows "iOS verified by CI".
   - Never push to `main` or merge.
10. **Report.** Give the PR link, what's verified, what isn't, and any Proposed ADR or owner action.

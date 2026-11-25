---
name: autopilot-story
description: Unattended version of implement-story. Takes one story from planning to a merged PR with no owner approvals, using automatic checks in their place (plan-reviewer, code/security review on the PR, CI, a hook-checked squash merge). Escalates through a GitHub issue only when it must. Run by scripts/autopilot.sh in a headless session; use only when the owner types /autopilot-story.
disable-model-invocation: true
argument-hint: "[next | STORY-PP-EE-SS] [--cross-epics] [--hold-sensitive]"
---

# Autopilot: one story, start to merge

You run headless (`claude -p`), launched by `scripts/autopilot.sh`. **Nobody is watching and nobody can answer a prompt.** A tool call that isn't allowed is denied, not asked. Work until the story is merged or you must escalate, then write the status file (§9) and stop. One session = one story.

Arguments: `next` (default) or a story id. `--cross-epics`: you may open the next epic when the active one is finished (default: stop at the epic boundary). `--hold-sensitive`: don't merge a PR whose diff touches security-sensitive paths (§7).

Everything in `CLAUDE.md` and `planning/WorkingAgreement.md` still holds, except that the owner's approvals are replaced by the checks below. Hooks still block secrets, pushes to `main`, force-pushes and any merge other than the hook-checked form in §7.

**Commands that pass headless permissions.** Allowed commands come from `.claude/settings.json` plus `.claude/autopilot.settings.json`. A denied command fails without asking anyone and wastes turns, so:
- run every command from the repository root, which is already the working directory: no `cd <path> &&` prefixes, no `VAR=value cmd` prefixes (`scripts/verify.sh` finds Java itself);
- backend commands: `npm --prefix backend/functions run <script>`;
- keep each Bash call to one command where you can; pipes into `tail`, `grep` or `head` are fine;
- multi-line text (PR bodies, review comments, issues) goes in a file under `.autopilot/` (create it with Write), then `--body-file`; never inline it with `--body "..."`;
- push with `git push -u origin <branch>`;
- if a command you need is denied, don't keep trying variants: use an allowed equivalent, or count it as an attempt on the retry ladder (§8) and name the command in the escalation so the owner can allow it.

**Interruptions.** The driver may stop you at any time (usage limit, crash, the owner). It later resumes the same session, or starts a fresh one that resumes through §0. Commit and push after each meaningful step (a passing slice, a fix, the story update), so at most a few minutes of work is only on disk. When you are resumed, check `git status`, the branch and the story before doing anything else.

## 0. Resume before starting anything new

1. `git status`. If the tree is dirty from a crashed run, look at what's there: if it belongs to the story on the current branch, keep it and continue that story; never discard work you didn't inspect.
2. Read `planning/STATE.md` and `planning/ROADMAP.md`. Then `gh pr list --author @me --state open` and `gh issue list --label needs-owner --state open`.
3. Pick what to work on, in this order:
   - **A story in `Review` with an open PR** → switch to its branch and go to §6 (review) if the PR has no `<!-- autopilot-review -->` comment yet, otherwise §7 (CI).
   - **A story whose branch exists but has no PR** (any status, including `Done` set early by §5 before a crash) → switch to it, commit any leftover work from the interrupted run after checking it, and continue from where its tasks and Evidence stop; if the work is complete, go to §5 step 2.
   - **A story `In progress` whose branch exists** → switch to it and continue from where its tasks and Evidence stop.
   - **A `Blocked` story with a `needs-owner` issue** → read the issue with `gh issue view <n> --comments`. Every comment you write starts with `<!-- autopilot -->`; **any comment without that marker is the owner's answer** (the agent and the owner share one GitHub account, so the author can't tell you). If the owner answered, apply the answer, record it in the story's `## Decisions`, reply on the issue (marker first) and close it, set the story back to `In progress` and continue. If not, skip that story and take the next one that doesn't depend on it; if every remaining story in the epic depends on it, write status `escalated` (same issue) and stop.
   - **Otherwise** the argument decides: the given id, or for `next` the first unchecked story in the active epic of `ROADMAP.md`.
4. **Epic boundary.** If the active epic has no unchecked stories left: mark the epic done in `ROADMAP.md` (that edit rides along in the last story's PR, so it should already be done), and without `--cross-epics` write status `epic-boundary` and stop. With `--cross-epics`, split the next epic into stories (§1 rules apply to each, and the split is recorded in the first story's `## Decisions`), then continue with its first story. If no epics are left in the phase, write `roadmap-done`.
5. Make sure you start from fresh `main`: `git switch main && git pull --ff-only` before creating a branch.

## 1. Author or check the story

If `planning/stories/<id>.md` doesn't exist, write it from the ROADMAP line with the template in `WorkingAgreement.md` §4, pulling the spec through `spec-scout` when more than ~3 docs are involved. Then check it against "Ready when" yourself.

**Stop and escalate at once (§8, no retries) when:**
- the story needs something in the "Escalate" column of `WorkingAgreement.md` §6: a privacy or safety default, monetization posture, security posture, reversing an accepted ADR, moving scope across phases, a feature not in `planning/product/features/`, two authoritative docs in hard conflict, a committed secret;
- the story depends on a **Proposed** ADR (STATE "Pending owner decisions");
- the story needs an **owner action** in an external console (`planning/OwnerTodo.md`), for example Google/Apple sign-in setup or App Check registration, and it isn't ticked there. Spot this now, before writing code. A new owner action you discover goes into `OwnerTodo.md` (right section, why + steps) in the same PR or escalation; never tick an item yourself unless the owner says it's done. If only part of the story needs it, you may ship the part that doesn't, as long as the acceptance criteria still make sense; otherwise escalate.

## 2. Decide the open questions yourself

Where you would have asked the owner, answer from the docs in the authority order of `WorkingAgreement.md` §1, using the tie-break at the end of §6 (safer for privacy, consistent with ADRs, simpler, phase-appropriate). Record each answer in a `## Decisions` section of the story file, one line each: the question, the answer, the source. The owner reviews these in the PR after the fact, so make them easy to scan. A question only the owner can answer is an escalation, not a decision.

## 3. Plan, and have it reviewed

1. Write `## Plan` into the story file: files per module, tests, how each acceptance criterion is proven, Android and iOS parity, and how it is verified on Windows (iOS through CI).
2. Run the **`plan-reviewer`** agent on the story file. Fix every `blocker` and `major` finding and record what you changed. Re-run it at most twice (three reviews total). If a blocker is still open after that, it counts as a failed technical attempt chain: escalate (§8).

Do **not** use plan mode: nobody can approve it.

## 4. Branch, implement, verify

- `git switch -c NN-short-name` from fresh `main` (NN = the next PR number; check `gh pr list --state all --limit 1`). Set the story to `In progress`.
- Implement with the matching skills, exactly as `implement-story` steps 6–7 describe.
- `/verify --full` until green. Commit in logical steps as you go, so a crash loses little; push the branch early with `git push -u origin <branch>`.
- Every failure goes through the retry ladder (§8).

## 5. Record and open the PR

1. Tick the story tasks, fill `## Evidence`, set the story to `Done`, tick it in `ROADMAP.md`, and update `planning/STATE.md` (Now / Next) as if the PR were merged. The guard only lets the merge through on green CI, so "merged" and "Done" are the same event and no follow-up commit is needed. If the PR ends up not merging, you set these back before escalating.
2. Commit, push, then write the PR body to `.autopilot/pr-body.md` and run `gh pr create --draft --base main --head <branch> --title "<story id>: <title>" --body-file .autopilot/pr-body.md`. The body follows `.github/pull_request_template.md`: summary, story link, DoD checklist, "Decisions made by the agent" (copied from the story), "iOS verified by CI" on Windows, and "Not verified".
3. Add the PR link to the story's Evidence and push.

## 6. Second look on the PR

1. Run the **`code-reviewer`** agent on `origin/main...HEAD`. Also run **`security-reviewer`** if the diff touches rules, auth, location, monetization, logging, analytics or `backend/functions`.
2. Write the findings to `.autopilot/review-<pr>.md` (gitignored), starting with the line `<!-- autopilot-review -->`, and post it with `gh pr comment <pr> --body-file .autopilot/review-<pr>.md`.
3. Fix every finding that is a real bug, a rule violation, a parity gap or a missing test. For a finding you reject, say why in a reply comment (marker `<!-- autopilot -->`). Re-run `/verify`, commit, push.
4. Re-review only what changed, at most 2 more rounds. A finding still open after that goes through §8 as a technical problem.

## 7. CI, then merge

1. Wait for CI: `gh pr checks <pr> --watch --interval 60`, with the Bash timeout at its maximum (600000 ms); repeat the command until every check has finished. The iOS job can take 20+ minutes.
2. **A check failed** → `gh run view <run-id> --log-failed`, find the cause, fix it locally, verify, commit, push, wait again. Each fix is one attempt on the retry ladder (§8). On Windows the iOS job is your only iOS compiler: read its log carefully before changing Swift or `iosMain` code.
3. **`main` moved and the PR conflicts** → `git fetch origin && git merge origin/main`, resolve, verify, push (never rebase a pushed branch, never force-push).
4. **All checks green:**
   - With `--hold-sensitive`, if `gh pr diff <pr> --name-only` touches `backend/firestore.rules`, `backend/storage.rules`, `backend/functions/`, anything with `entitlement`, `unlock`, `purchase` or `location` in its path: run `gh pr ready <pr>`, then escalate with "ready for your review, not merged" (§8) instead of merging.
   - Otherwise: `gh pr ready <pr>`, then read the head with `gh pr view <pr> --json headRefOid --jq .headRefOid` and merge with `gh pr merge <pr> --squash --match-head-commit <sha> --delete-branch`. The guard hook asks GitHub itself and refuses the merge unless the PR is open, not a draft, mergeable, its head is that sha and all four required checks passed on it; gh refuses too if the head moved. Never pass `--admin` or `--auto`, never merge any other way.
5. If the guard refuses, its message says why. Checks still running or mergeability not computed yet: wait (`sleep 60`) and retry. Anything else (a red check, conflicts, a moved head): fix it through §8, wait for CI on the new head, and retry with the new sha. Confirm with `gh pr view <pr> --json state,mergeCommit` that the state is `MERGED`.
6. After the merge: `git switch main && git pull --ff-only`, then write status `merged` (§9).

## 8. Retry ladder and escalation

**Technical problems** (a failing test, build or CI job, a reviewer finding, a plan blocker, a merge that won't go through):
- attempt 1 is your first fix; then **at most 2 retries, and each retry must use a different approach** from every earlier attempt (a different root-cause hypothesis, a different design, a different API). Running the same fix again, or a cosmetic variant of it, doesn't count as a retry and isn't allowed;
- log every attempt in the story's `## Evidence` as `attempt N: <approach> → <result>`;
- when the third attempt fails, escalate.

**Owner decisions** (§1's list, plus anything you can't decide from the docs): escalate **immediately**, no retries. Don't try "a different way" around a privacy, safety or monetization question.

**Never** as a way out: weaken or delete a test, skip a check, add a suppression or baseline entry to make detekt/Konsist/lint pass, widen a security rule to make a test pass, or mark something verified that wasn't.

**How to escalate:**
1. Commit and push whatever work exists to the story branch (it may be red; that's fine on a branch).
2. Set the story to `Blocked (<one-line reason>; owner)` and update `STATE.md` (and undo `Done` / the ROADMAP tick if you set them). Commit and push.
3. Make sure the label exists: `gh label create needs-owner --color D93F0B --description "Autopilot is waiting for the owner" --force`. Open an issue with `gh issue create --label needs-owner --assignee @me --title "<story id>: <short question>" --body-file .autopilot/issue.md`. The body starts with `<!-- autopilot -->` and contains: the story and branch/PR, what's blocked, every attempt from the ladder, the options with their trade-offs, **your recommended default**, and exactly what reply unblocks it ("reply with A or B" is ideal).
4. Write status `escalated` with the issue number, and stop.

## 9. Status file (last step, always)

Write `.autopilot/status.json` (the folder is gitignored; create the file with the Write tool):

```json
{ "result": "merged", "story": "STORY-01-02-02", "pr": 12, "issue": null, "summary": "one line for the owner" }
```

`result` is one of: `merged`, `escalated`, `epic-boundary`, `roadmap-done`. The driver treats a missing file as `crashed`, and resumes through §0 on the next run, so it's safe to stop at any point as long as work is committed and pushed.

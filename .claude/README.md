# How the Claude Code setup works here

This folder is for the owner and future contributors. Claude Code does not load this README automatically.

## The layers: what loads when

| Layer | Where | When it enters context | Use it for |
|---|---|---|---|
| Project memory | `/CLAUDE.md` | Every session, in full | The few facts every task needs: what the app is, modules, commands, non-negotiables, where things live. Keep it short; every line costs tokens on every turn |
| Path rules | `.claude/rules/*.md` with `paths:` frontmatter | Only when Claude reads or edits a matching file | Coding rules for one area: Kotlin shared code, Compose UI, backend, shells, planning docs |
| Skills | `.claude/skills/<name>/SKILL.md` (+ templates) | Only the `description` line is always present; the body loads when the task matches or you type `/<name>` | Repeatable procedures: the story loop, feature scaffolding, Firestore changes, functions, monetization, release, ASO, experiments |
| Subagents | `.claude/agents/*.md` | Run in their **own** context window; only their final report comes back | Isolating heavy reading (`spec-scout`), fresh-eyes review (`plan-reviewer`, `code-reviewer`, `security-reviewer`), parallel work (`backend-engineer`), web research (`growth-analyst`) |
| Hooks | `.claude/hooks/*.mjs`, wired in `settings.json` | Run deterministically by the harness, not by the model | Rules that must never depend on memory: block reading or committing real configs, block pushes to main and force-pushes, allow a merge only in one form and only after checking with GitHub that all required checks are green (`merge-check.mjs`); print STATE at session start |
| Permissions | `.claude/settings.json` | Always | Pre-approve safe commands (gradle, git read/commit/branch push, draft PRs) and deny dangerous ones |
| Personal overrides | `.claude/settings.local.json`, `CLAUDE.local.md` (gitignored) | Always, for you only | Machine-specific tweaks; never commit them |

**Rule of thumb:**
- A **fact** every task needs goes in `CLAUDE.md`.
- A **rule** for one area goes in `rules/`.
- A **procedure** goes in a skill.
- **Work that would flood the main context or can run in parallel** goes to a subagent.
- **Something that must always happen** goes in a hook.

Subagents are not a "team of roles" handing work to each other. Each one starts cold, so use them where isolation or parallelism pays for that cost.

## Skills

| Skill | Trigger |
|---|---|
| `/implement-story [id\|next]` | Full loop: spec → plan → branch → code → verify → review agents → STATE → commit → draft PR |
| `/autopilot-story [next\|id]` | Unattended loop for one story, ending in a hook-checked squash merge or a `needs-owner` issue. Run it through `scripts/autopilot.sh` (see "Autopilot") |
| `/verify [--quick\|--full]` | Path- and OS-aware checks (`scripts/verify.sh`) |
| `new-feature` | Screen + ViewModel + repository + fake + firebaseData impl + DI + route + tests from `templates/` |
| `platform-capability` | Native capability behind a shared contract on both platforms |
| `firestore-change` | Schema or rules change with emulator tests |
| `cloud-function` | v2 function with idempotency, redaction, tests |
| `monetization` | RevenueCat / AdMob server-side verification / entitlements without client trust |
| `analytics-event` | Typed, PII-free analytics |
| `adr` | Proposed decision record + index + STATE |
| `upgrade-deps` | Safe dependency bumps |
| `/release` | Store submission checklist (4.3 / 1.2 / privacy / rollout) |
| `store-listing` | ASO copy and screenshot plan |
| `growth-experiment` | Remote Config / RevenueCat experiments |

## Everyday usage

- **Start a session:** the hook prints the branch and STATE. Say "do the next story" or `/implement-story next`.
- **Context hygiene:**
  - `/clear` between stories;
  - plan mode (Shift+Tab) for anything non-trivial;
  - `/compact` in long sessions;
  - ask for `spec-scout` instead of "read the planning docs".
- **Parallel work:** independent stories (e.g. a backend story and a UI story) can run in separate sessions or git worktrees, as long as they don't touch the same files.
- **Models:** the main session uses your default; `spec-scout` runs on Haiku (cheap reading); the reviewers and `backend-engineer` run on Sonnet.
- **iOS on Windows:** not buildable locally. The draft PR's macOS CI jobs (`iOS shared tests`, `iOS app build`) are the check, and the session hook reminds the agent of that.

## Autopilot

`scripts/autopilot.sh` runs stories back to back without you. Each story gets a headless session (`claude -p "/autopilot-story next" --permission-mode acceptEdits --settings .claude/autopilot.settings.json`), so nothing waits on a prompt: edits are allowed, Bash runs what `settings.json` plus `autopilot.settings.json` allow, and anything else is denied (the guard hook still applies). The rules are in `planning/WorkingAgreement.md` §2 (gates) and §6 (retry ladder, escalation); the procedure is `.claude/skills/autopilot-story/SKILL.md`.

**One-time setup:**
1. Install `gh` and run `gh auth login`.
2. Keep squash merging enabled (GitHub → Settings → General).
3. Phone notifications: copy `scripts/autopilot.local.env.example` to `scripts/autopilot.local.env` and set `AUTOPILOT_NTFY_TOPIC` (install the free ntfy app and subscribe to the same, hard-to-guess topic). GitHub won't notify you about issues the agent opens, because it opens them as you. The script also shows a desktop notification.

**Following a run.** The terminal shows one line per step: commands, file edits, subagents, short agent messages, errors and `DENIED` permissions, then a per-session summary listing every denial. The same lines go to `.autopilot/live.log`, so you can follow from another window (`Get-Content .autopilot\live.log -Wait` in PowerShell, `tail -f` in bash). The raw event stream is kept in `.autopilot/logs/`. To read the whole conversation later, `claude --resume <session id>` (printed at the start of each session) opens it interactively.

**Interruptions resume.** The session id is saved in `.autopilot/session.json` as soon as a session starts and removed only when the agent finishes properly. If a session stops for any reason (usage limit, Ctrl+C, crash, turn cap, a reboot), the next `scripts/autopilot.sh` continues **the same session** with `claude --resume`, with its full context. If that session can't be resumed, it starts a fresh one, which picks the story up from the branch, STATE and the open PR. Uncommitted work on a story branch is fine; on `main` the script refuses. On a usage limit the script sends a notification, waits for the reset (read from Claude's message, or retries every 30 min) and resumes on its own; `--no-wait` exits instead. Use `--fresh` to throw the saved session away.

**From your phone.** You get a push at each milestone: branch started, PR opened, merged, usage limit, and every escalation. When the agent needs you, it opens a `needs-owner` issue and the script **keeps waiting** (up to `--max-wait-hours`, default 8) while checking it every 3 minutes. Answer on the issue in the GitHub app; any comment without the `<!-- autopilot -->` marker counts as your answer, and closing the issue also counts. The script then starts the next session, which reads your answer and carries on. Headless sessions can't use Remote Control; to talk to the agent directly, run `claude --resume <session id>` on the PC.

**How merging is kept safe.** The repository is private on GitHub Free, which offers neither branch protection nor auto-merge, so the check runs in the guard hook instead. The agent may only run `gh pr merge <n> --squash --match-head-commit <sha>`. Before letting it through, `.claude/hooks/merge-check.mjs` asks GitHub for the PR and refuses unless it is open, not a draft, based on `main`, mergeable, its head is exactly `<sha>`, every required CI job (`REQUIRED_CHECKS` in `merge-check.mjs`) passed on that sha and no other check failed or is still running. `--match-head-commit` makes gh refuse the merge if anything was pushed after the check. The required job names live in `merge-check.mjs`: rename a job in `ci.yml` and you must update that list, or every merge is refused. This protects against agent mistakes, not against a manual merge from the GitHub UI. With GitHub Pro you could add real branch protection on top.

**Run** it, ideally in a dedicated clone so you can keep working elsewhere:

```bash
scripts/autopilot.sh                     # up to 5 stories, stop at the epic boundary
scripts/autopilot.sh --max 2 --hold-sensitive
scripts/autopilot.sh --cross-epics       # also split and start the next epic
scripts/autopilot.sh --model claude-opus-5-5   # a harder story (default claude-sonnet-5-5)
scripts/autopilot.sh --help              # all options
```

**Model.** The driver passes `--model` (default `claude-sonnet-5-5`), so the agent never inherits whatever you last chose with `/model` in an interactive session. Each session logs its model on the "model:" line and on the viewer's session line. The review agents keep their own model from their frontmatter (Sonnet; `spec-scout` uses Haiku).

It stops on `epic-boundary` (the notification points to the epic's manual test plan in `planning/testing/`), `roadmap-done`, the story cap, an escalation you didn't answer in time, a sign-in problem, or two failed sessions in a row. Everything it keeps is in `.autopilot/` (gitignored). Keep the PC awake and plugged in while it runs.

**Denied commands.** The session summary lists every permission denial. If the agent needed a safe command, add it to `.claude/autopilot.settings.json`, not to `settings.json`, so interactive sessions keep the stricter list. That file also denies deploys, `gcloud`, `curl`, `git reset --hard` and `git clean` for the agent. Merges to `main` deploy the backend to `fart-map-dev`; prod stays behind the `prod` environment approval.

## MCP servers (optional, per machine)

The Firebase MCP server lets Claude query projects, rules and emulators. It is configured per machine because `npx` needs a wrapper on native Windows:

- **macOS:** `claude mcp add firebase -s local -- npx -y firebase-tools@15 mcp --dir backend`
- **Windows:** `claude mcp add firebase -s local -- cmd /c npx -y firebase-tools@15 mcp --dir backend`

It uses your `firebase login`. Keep deploys in CI. The Figma MCP is already enabled as a plugin at user level.

## Maintaining this setup
- Keep `CLAUDE.md` under about 150 lines. If a rule applies to one area only, move it to `rules/`.
- When a pattern changes (for example the real `StateViewModel` lands), update the skill templates in the same PR.
- Test the hooks after editing: run `node .claude/hooks/guard.mjs` with a sample JSON on stdin (exit 2 means blocked).

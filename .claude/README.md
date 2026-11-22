# How the Claude Code setup works here

This folder is for the owner and future contributors. Claude Code does not load this README automatically.

## The layers: what loads when

| Layer | Where | When it enters context | Use it for |
|---|---|---|---|
| Project memory | `/CLAUDE.md` | Every session, in full | The few facts every task needs: what the app is, modules, commands, non-negotiables, where things live. Keep it short; every line costs tokens on every turn |
| Path rules | `.claude/rules/*.md` with `paths:` frontmatter | Only when Claude reads or edits a matching file | Coding rules for one area: Kotlin shared code, Compose UI, backend, shells, planning docs |
| Skills | `.claude/skills/<name>/SKILL.md` (+ templates) | Only the `description` line is always present; the body loads when the task matches or you type `/<name>` | Repeatable procedures: the story loop, feature scaffolding, Firestore changes, functions, monetization, release, ASO, experiments |
| Subagents | `.claude/agents/*.md` | Run in their **own** context window; only their final report comes back | Isolating heavy reading (`spec-scout`), fresh-eyes review (`code-reviewer`, `security-reviewer`), parallel work (`backend-engineer`), web research (`growth-analyst`) |
| Hooks | `.claude/hooks/*.mjs`, wired in `settings.json` | Run deterministically by the harness, not by the model | Rules that must never depend on memory: block reading or committing real configs, block pushes to main, merges and force-pushes; print STATE at session start |
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
- **iOS on Windows:** not buildable locally. The draft PR's macOS CI job is the check, and the session hook reminds the agent of that.

## MCP servers (optional, per machine)

The Firebase MCP server lets Claude query projects, rules and emulators. It is configured per machine because `npx` needs a wrapper on native Windows:

- **macOS:** `claude mcp add firebase -s local -- npx -y firebase-tools@15 mcp --dir backend`
- **Windows:** `claude mcp add firebase -s local -- cmd /c npx -y firebase-tools@15 mcp --dir backend`

It uses your `firebase login`. Keep deploys in CI. The Figma MCP is already enabled as a plugin at user level.

## Maintaining this setup
- Keep `CLAUDE.md` under about 150 lines. If a rule applies to one area only, move it to `rules/`.
- When a pattern changes (for example the real `StateViewModel` lands), update the skill templates in the same PR.
- Test the hooks after editing: run `node .claude/hooks/guard.mjs` with a sample JSON on stdin (exit 2 means blocked).

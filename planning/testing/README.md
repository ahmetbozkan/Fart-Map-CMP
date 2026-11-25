# Epic test plans

The owner tests by hand once per epic, not per story. When an epic's last (non-deferred) story is done, the agent that finishes it writes `planning/testing/EPIC-PP-EE.md` in that story's PR, alongside the ROADMAP epic tick. The owner runs it on Android and iOS, files a `bug` issue per failure, and the autopilot fixes those issues before the next story (`autopilot-story` §0).

Example: `EPIC-01-02.md`.

## Format

1. **Header:** which stories it covers, which are deferred, and a few lines on what the epic built and what is still a placeholder.
2. **Before you start:** a checklist.
   - Owner actions the cases need (link `OwnerTodo.md` items).
   - Configs; how to build and install each platform (exact commands, variant, scheme).
   - Accounts and devices.
   - What to have open: console paths, log filters.
   - How to reset to a fresh install and how to make test data "new" again.
3. **Cases:** numbered, each with `Android ☐ iOS ☐`, short steps, and an **Expected** result the owner can check: the exact UI text, the console doc and field values, the log lines. Cover:
   - every acceptance criterion with a user-visible or console-visible effect;
   - every story's `## Validation` manual check;
   - cancel, offline and error paths;
   - anything the story Evidence lists as "not verified" or "manual only", marked as such.
4. **Not in this epic:** behavior that is deliberately missing, so it isn't reported as a bug.
5. **Reporting a bug:** the issue title `EPIC-PP-EE case N: …`, label `bug`, and what to include. No emails, tokens or uids in the issue.

Write for someone who knows Android but not this codebase. Use the real strings from `composeResources` and the real field names from `DataContracts.md`. Never put a secret or a real config value in the plan.

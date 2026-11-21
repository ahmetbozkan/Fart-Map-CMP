---
name: verify
description: Run the project's verification for the current change (builds, shared tests, lint, detekt/Konsist, backend tests, secret check; iOS on macOS) and summarize failures with the fix. Use after any code change, before committing, or when asked to "verify", "run checks", or "/verify".
argument-hint: "[--quick | --full]"
allowed-tools: Bash(bash scripts/verify.sh*), Bash(scripts/verify.sh*), Bash(./gradlew *), Bash(scripts/check-no-secrets.sh), Read, Grep
---

# Verify

1. Run `bash scripts/verify.sh $ARGUMENTS` from the repository root (default `--quick`; use `--full` before a PR). If the script doesn't exist yet (the static-analysis PR is not merged), run the commands from the root `CLAUDE.md` that match the changed paths:
   - `shared/` or `firebaseData/` → `:shared:testAndroidHostTest` + both Android assembles;
   - `androidApp/` → assembles + `:androidApp:lintDevDebug`;
   - `backend/` → `npm run build && npm run lint && npm test` in `backend/functions`;
   - always `scripts/check-no-secrets.sh`.
2. On macOS the script also links the iOS framework and builds `FartMap Dev`. On Windows it prints that iOS is verified by CI; repeat that in the PR.
3. On failure, read only the relevant report (`*/build/reports/**`, the first compiler error, the failing test's assertion), fix the root cause, and rerun the failing step. Never suppress a detekt or Konsist rule to get green without saying why in the PR.
4. Report one line per step: pass, fail, or skipped with the reason.

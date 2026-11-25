// Merge gate used by guard.mjs. The repository is private on GitHub Free, which has no branch protection
// and no auto-merge, so this hook is what keeps a red PR out of `main` (see .claude/README.md "Autopilot").
//
// The one allowed merge command is:
//   gh pr merge <number> --squash --match-head-commit <40-char sha> [--delete-branch]
// It is allowed only if GitHub reports, for that PR: open, not a draft, based on main, mergeable, its head
// is exactly <sha> (so nothing pushed after the check can slip in; gh refuses the merge if the head moved),
// every required check passed on that head, and no other check failed or is still running.

// The CI job names in .github/workflows/ci.yml. Renaming a job there means updating this list, or every
// merge is blocked (fail closed). The two iOS jobs always report SUCCESS-or-FAILURE: when the path gate
// finds no iOS-relevant change they succeed without building. A skipped job (e.g. the gate job failed)
// is not SUCCESS, so it blocks.
export const REQUIRED_CHECKS = [
  'Secret scan',
  'Android build, shared tests, lint',
  'iOS shared tests',
  'iOS app build',
  'Backend build, lint, emulator tests',
];

export const MERGE_FORM = /^\s*gh\s+pr\s+merge\s+(\d+)\s+--squash\s+--match-head-commit\s+([0-9a-f]{40})(\s+--delete-branch)?\s*$/;
export const MERGE_USAGE = '`gh pr merge <number> --squash --match-head-commit <head sha>` (optionally `--delete-branch`), on its own';

const BAD = new Set(['FAILURE', 'CANCELLED', 'TIMED_OUT', 'ACTION_REQUIRED', 'STARTUP_FAILURE', 'STALE', 'ERROR']);
const OK = new Set(['SUCCESS', 'NEUTRAL', 'SKIPPED']);

// One entry of statusCheckRollup is either a CheckRun {name, status, conclusion} or a StatusContext {context, state}.
const normalize = (c) => c.__typename === 'StatusContext' || (c.context && !c.name)
  ? { name: c.context, done: c.state !== 'PENDING' && c.state !== 'EXPECTED', result: c.state }
  : { name: c.name, done: c.status === 'COMPLETED', result: c.conclusion || '' };

/** Returns null when the merge may proceed, otherwise the reason it may not. */
export function evaluateMerge(pr, sha) {
  if (!pr || typeof pr !== 'object') return 'could not read the PR from GitHub.';
  if (pr.state !== 'OPEN') return `the PR is ${pr.state}, not OPEN.`;
  if (pr.isDraft) return 'the PR is still a draft; run `gh pr ready <number>` first.';
  if (pr.baseRefName !== 'main') return `the PR targets ${pr.baseRefName}, not main.`;
  if (pr.mergeable !== 'MERGEABLE') {
    return pr.mergeable === 'UNKNOWN'
      ? 'GitHub has not computed mergeability yet; check again in a minute.'
      : `the PR is ${pr.mergeable} (conflicts?); merge origin/main into the branch and push.`;
  }
  if (pr.headRefOid !== sha) return `the PR head is ${pr.headRefOid}, not ${sha}; read the current head, wait for its checks, and use that sha.`;

  const checks = (pr.statusCheckRollup || []).map(normalize);
  const missing = [], notGreen = [];
  for (const name of REQUIRED_CHECKS) {
    const runs = checks.filter((c) => c.name === name);
    if (runs.length === 0) missing.push(name);
    else if (!runs.every((c) => c.done && c.result === 'SUCCESS')) notGreen.push(`${name} (${runs.map((c) => c.done ? c.result : 'running').join(', ')})`);
  }
  if (missing.length) return `required checks have not reported on ${sha.slice(0, 7)}: ${missing.join('; ')}.`;
  if (notGreen.length) return `required checks are not green: ${notGreen.join('; ')}.`;

  const others = checks.filter((c) => !REQUIRED_CHECKS.includes(c.name));
  const running = others.filter((c) => !c.done).map((c) => c.name);
  if (running.length) return `checks still running: ${running.join(', ')}.`;
  const failed = others.filter((c) => BAD.has(c.result) || !OK.has(c.result)).map((c) => `${c.name} (${c.result})`);
  if (failed.length) return `other checks did not pass: ${failed.join(', ')}.`;
  return null;
}

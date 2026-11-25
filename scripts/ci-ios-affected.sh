#!/usr/bin/env bash
# Decides whether a change can affect the iOS build, for the CI path gate (.github/workflows/ci.yml).
# Prints ios=true|false and, in GitHub Actions, writes it to $GITHUB_OUTPUT.
#
# Fail-safe: anything not on the skip list below, an unknown event, or a failing diff means ios=true.
# Only paths that iOS can never compile or read are skippable.
#
# Usage: scripts/ci-ios-affected.sh [<base-ref>]   (default base: $BASE_SHA, else origin/main)
set -uo pipefail

SKIPPABLE='^(backend/|planning/|\.claude/|androidApp/|config/detekt/|scripts/(autopilot|check-|verify|doc-links|ci-ios-affected)|\.github/workflows/deploy-backend\.yml$|\.gitignore$|\.gitleaks\.toml$|LICENSE)|\.md$'

emit() {
  echo "ios=$1"
  [[ -n "${GITHUB_OUTPUT:-}" ]] && echo "ios=$1" >> "$GITHUB_OUTPUT"
  exit 0
}

# Pushes to main always run the full iOS check: they also seed the caches pull requests read.
if [[ -n "${EVENT:-}" && "$EVENT" != "pull_request" ]]; then
  echo "event $EVENT: full run"
  emit true
fi

base="${1:-${BASE_SHA:-origin/main}}"
if ! files=$(git diff --name-only "$base" HEAD 2>/dev/null); then
  echo "diff against $base failed: full run"
  emit true
fi
if [[ -z "$files" ]]; then
  echo "no changed files found: full run"
  emit true
fi

affecting=$(grep -Ev "$SKIPPABLE" <<<"$files" || true)
if [[ -n "$affecting" ]]; then
  echo "iOS-affecting changes:"
  head -20 <<<"$affecting" | sed 's/^/  /'
  emit true
fi
echo "only iOS-irrelevant paths changed:"
head -20 <<<"$files" | sed 's/^/  /'
emit false

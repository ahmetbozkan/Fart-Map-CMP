#!/usr/bin/env bash
# Fails if any real config or secret file is tracked by git. Templates are allowed.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
bad="$(git ls-files | grep -E '(^|/)(google-services\.json|GoogleService-Info[^/]*\.plist|secrets\.properties|local\.properties)$' || true)"
bad+="$(git ls-files | grep -E '\.(keystore|jks|p8|p12|mobileprovision)$' || true)"
if [[ -n "$bad" ]]; then
  echo "Tracked secret or config files found:" >&2
  echo "$bad" >&2
  exit 1
fi
echo "check-no-secrets: ok"

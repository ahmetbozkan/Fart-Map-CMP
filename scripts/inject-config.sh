#!/usr/bin/env bash
# Writes the untracked Firebase client configs (and optional secrets) into place.
# Sources, in order of precedence per file:
#   1. environment variable holding the file's base64 content (CI secret store),
#   2. a copy in $FARTMAP_CONFIG_DIR (default ~/.fartmap) with the same file name,
#   3. an existing file at the target path (kept as is).
# Usage: scripts/inject-config.sh [--require-all]
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CONFIG_DIR="${FARTMAP_CONFIG_DIR:-$HOME/.fartmap}"
REQUIRE_ALL=0
[[ "${1:-}" == "--require-all" ]] && REQUIRE_ALL=1

# target path | env var name | file name in the config dir
FILES=(
  "androidApp/src/dev/google-services.json|GOOGLE_SERVICES_JSON_DEV|google-services-dev.json"
  "androidApp/src/prod/google-services.json|GOOGLE_SERVICES_JSON_PROD|google-services-prod.json"
  "iosApp/Configuration/GoogleService-Info-dev.plist|GOOGLE_SERVICE_INFO_PLIST_DEV|GoogleService-Info-dev.plist"
  "iosApp/Configuration/GoogleService-Info-prod.plist|GOOGLE_SERVICE_INFO_PLIST_PROD|GoogleService-Info-prod.plist"
)

missing=0
for entry in "${FILES[@]}"; do
  IFS='|' read -r target var name <<<"$entry"
  dest="$ROOT/$target"
  mkdir -p "$(dirname "$dest")"
  if [[ -n "${!var:-}" ]]; then
    printf '%s' "${!var}" | base64 --decode >"$dest"
    echo "inject-config: wrote $target from \$$var"
  elif [[ -f "$CONFIG_DIR/$name" ]]; then
    cp "$CONFIG_DIR/$name" "$dest"
    echo "inject-config: copied $target from $CONFIG_DIR/$name"
  elif [[ -f "$dest" ]]; then
    echo "inject-config: kept existing $target"
  else
    echo "inject-config: MISSING $target (set \$$var or place $name in $CONFIG_DIR)" >&2
    missing=1
  fi
done

# Optional secrets consumed by Gradle (never tracked). Empty values are acceptable until the feature needs them.
SECRETS="$ROOT/androidApp/secrets.properties"
if [[ -n "${MAPS_API_KEY_DEV:-}${MAPS_API_KEY_PROD:-}" ]]; then
  {
    echo "MAPS_API_KEY_DEV=${MAPS_API_KEY_DEV:-}"
    echo "MAPS_API_KEY_PROD=${MAPS_API_KEY_PROD:-}"
  } >"$SECRETS"
  echo "inject-config: wrote androidApp/secrets.properties"
fi

if [[ $missing -eq 1 && $REQUIRE_ALL -eq 1 ]]; then
  echo "inject-config: required files are missing" >&2
  exit 1
fi

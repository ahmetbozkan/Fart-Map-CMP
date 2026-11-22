#!/usr/bin/env bash
# Path- and OS-aware verification. Usage: scripts/verify.sh [--quick|--full]
#   --quick (default): checks for what changed vs origin/main (or main) plus uncommitted files.
#   --full: everything (what CI runs). iOS steps run only on macOS; elsewhere iOS is verified by CI.
# Works in Git Bash on Windows and on macOS/Linux. Exit code is non-zero if any step fails.
set -uo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

MODE="${1:---quick}"
OS="$(uname -s)"
GRADLEW="./gradlew"
results=()
failed=0

# A JDK 17+ launcher is needed; fall back to Android Studio's bundled JBR when JAVA_HOME is unset.
if [[ -z "${JAVA_HOME:-}" ]] && ! command -v java >/dev/null 2>&1; then
  for jbr in "/c/Program Files/Android/Android Studio/jbr" "/Applications/Android Studio.app/Contents/jbr/Contents/Home"; do
    [[ -d "$jbr" ]] && export JAVA_HOME="$jbr" && break
  done
fi
# The Firestore emulator looks up `java` on PATH.
[[ -n "${JAVA_HOME:-}" ]] && export PATH="$JAVA_HOME/bin:$PATH"

run() { # run <label> <command...>
  local label="$1"; shift
  echo "==> $label: $*"
  if "$@"; then results+=("PASS  $label"); else results+=("FAIL  $label"); failed=1; fi
}

base="origin/main"; git rev-parse -q --verify "$base" >/dev/null || base="main"
changed="$( { git diff --name-only "$base"...HEAD 2>/dev/null; git diff --name-only HEAD; git ls-files --others --exclude-standard; } | sort -u )"
has() { [[ "$MODE" == "--full" ]] || grep -qE "$1" <<<"$changed"; }

# The Android app build needs the injected Firebase configs (scripts/inject-config.sh); without them only
# static analysis and shared tests can run locally, and CI covers the rest.
configs_present=1
for env in dev prod; do [[ -f "androidApp/src/$env/google-services.json" ]] || configs_present=0; done

gradle_tasks=()
if has '^(shared|firebaseData|umbrella|androidApp)/|^gradle/|\.gradle\.kts$|^config/detekt/'; then
  gradle_tasks+=(detekt :shared:testAndroidHostTest)
  if (( configs_present )); then gradle_tasks+=(:androidApp:assembleDevDebug :androidApp:assembleProdDebug)
  else results+=("SKIP  android assemble/lint (Firebase configs not injected: run scripts/inject-config.sh; CI verifies)"); fi
fi
if (( configs_present )) && has '^androidApp/|^gradle/|\.gradle\.kts$'; then gradle_tasks+=(:androidApp:lintDevDebug); fi
if (( ${#gradle_tasks[@]} )); then run "gradle" "$GRADLEW" "${gradle_tasks[@]}" --console=plain; fi

if has '^(shared|firebaseData|umbrella|iosApp)/|^gradle/|\.gradle\.kts$'; then
  if [[ "$OS" == "Darwin" ]]; then
    run "ios-tests+framework" "$GRADLEW" :shared:iosSimulatorArm64Test :umbrella:linkDebugFrameworkIosSimulatorArm64 --console=plain
    run "ios-app" xcodebuild -project iosApp/iosApp.xcodeproj -scheme "FartMap Dev" -configuration Debug-dev \
      -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO -quiet build
  else
    results+=("SKIP  ios (not macOS: verified by the CI ios job on the PR)")
  fi
fi

if has '^backend/'; then
  if command -v firebase >/dev/null 2>&1; then
    run "backend" bash -c 'cd backend/functions && npm ci --no-audit --no-fund && npm run build && npm run lint && npm test'
  else
    run "backend (build, lint, unit)" bash -c 'cd backend/functions && npm ci --no-audit --no-fund && npm run build && npm run lint && npm run test:unit'
    results+=("SKIP  backend emulator tests (rules + idempotency; firebase CLI missing: npm install -g firebase-tools@15; CI runs them)")
  fi
fi

if has '\.md$|^\.claude/|^planning/'; then run "doc-links" bash scripts/check-doc-links.sh; fi
run "no-secrets" bash scripts/check-no-secrets.sh

echo; echo "verify ($MODE) summary:"; printf '  %s\n' "${results[@]}"
exit $failed

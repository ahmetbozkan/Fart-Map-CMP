#!/usr/bin/env bash
# Runs stories unattended, one fresh headless Claude Code session per story, until one needs the owner.
# Usage: scripts/autopilot.sh [--max N] [--max-turns N] [--cross-epics] [--hold-sensitive] [--story STORY-PP-EE-SS]
#   --max N           stop after N merged stories (default 5)
#   --max-turns N     turn cap per session (default 400); hitting it counts as a crash
#   --cross-epics     let the agent open the next epic (default: stop at each epic boundary)
#   --hold-sensitive  don't merge PRs touching rules, functions, entitlements or location; escalate instead
#   --story ID        start with this story instead of "next" (first iteration only)
# Each session runs the autopilot-story skill (.claude/skills/autopilot-story/SKILL.md) and writes
# .claude/loop/status.json: merged | escalated | epic-boundary | roadmap-done. A missing file = crashed.
# Optional: AUTOPILOT_NOTIFY_CMD='curl -d "$AUTOPILOT_MESSAGE" ntfy.sh/<your-topic>' to get a phone ping.
set -uo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

max=5; max_turns=400; flags=""; story="next"
while [[ $# -gt 0 ]]; do
  case "$1" in
    --max) max="$2"; shift 2 ;;
    --max-turns) max_turns="$2"; shift 2 ;;
    --cross-epics) flags="$flags --cross-epics"; shift ;;
    --hold-sensitive) flags="$flags --hold-sensitive"; shift ;;
    --story) story="$2"; shift 2 ;;
    -h|--help) sed -n '2,11p' "$0"; exit 0 ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done

loop_dir=".claude/loop"
status_file="$loop_dir/status.json"
mkdir -p "$loop_dir"

notify() { # notify <message>
  local msg="$1"
  echo "[autopilot] $msg"
  printf '\a'
  if [[ -n "${AUTOPILOT_NOTIFY_CMD:-}" ]]; then
    AUTOPILOT_MESSAGE="$msg" bash -c "$AUTOPILOT_NOTIFY_CMD" || echo "[autopilot] AUTOPILOT_NOTIFY_CMD failed" >&2
  fi
  case "$(uname -s)" in
    Darwin) osascript -e "display notification \"${msg//\"/\'}\" with title \"Fart Map autopilot\"" 2>/dev/null || true ;;
    MINGW*|MSYS*|CYGWIN*)
      AUTOPILOT_MESSAGE="$msg" powershell.exe -NoProfile -Command '
        Add-Type -AssemblyName System.Windows.Forms
        $n = New-Object System.Windows.Forms.NotifyIcon
        $n.Icon = [System.Drawing.SystemIcons]::Information
        $n.Visible = $true
        $n.ShowBalloonTip(10000, "Fart Map autopilot", $env:AUTOPILOT_MESSAGE, "Info")
        Start-Sleep -Seconds 10
        $n.Dispose()' >/dev/null 2>&1 || true ;;
  esac
}

field() { # field <name> -> value from status.json, empty if missing
  node -e 'try { const s = JSON.parse(require("fs").readFileSync(process.argv[1], "utf8")); const v = s[process.argv[2]]; process.stdout.write(v == null ? "" : String(v)); } catch { }' \
    "$status_file" "$1"
}

# Prerequisites: fail fast instead of discovering them an hour in.
command -v claude >/dev/null || { echo "claude CLI not on PATH" >&2; exit 1; }
command -v node >/dev/null   || { echo "node not on PATH" >&2; exit 1; }
command -v gh >/dev/null     || { echo "gh (GitHub CLI) not on PATH; install it and run 'gh auth login'" >&2; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "gh is not authenticated; run 'gh auth login'" >&2; exit 1; }
if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean. Commit or stash first (the agent resumes from branches, not from loose files)." >&2
  exit 1
fi

merged=0; crashes=0; i=0
while [[ $merged -lt $max ]]; do
  i=$((i + 1))
  rm -f "$status_file"
  stamp="$(date +%Y%m%d-%H%M%S)"
  log="$loop_dir/run-$stamp.json"
  echo "[autopilot] session $i: /autopilot-story $story$flags (log: $log)"
  claude -p "/autopilot-story $story$flags" \
    --permission-mode acceptEdits \
    --max-turns "$max_turns" \
    --output-format json > "$log" 2> "$loop_dir/run-$stamp.err"
  exit_code=$?
  story="next"

  result="$(field result)"; [[ -n "$result" ]] || result="crashed"
  sid="$(field story)"; pr="$(field pr)"; issue="$(field issue)"; summary="$(field summary)"
  echo "[autopilot] session $i ended: $result ${sid:+($sid)} ${pr:+PR #$pr} ${issue:+issue #$issue} (claude exit $exit_code)"
  [[ -n "$summary" ]] && echo "[autopilot]   $summary"

  case "$result" in
    merged)
      merged=$((merged + 1)); crashes=0 ;;
    crashed)
      crashes=$((crashes + 1))
      if [[ $crashes -ge 2 ]]; then
        notify "Stopped: two sessions in a row ended without a status file. See $log and $loop_dir/run-$stamp.err."
        exit 1
      fi
      echo "[autopilot] no status file (turn cap or error); one more session will resume the same story." ;;
    escalated)
      notify "Needs you: ${sid:-a story} is blocked${issue:+, see issue #$issue}. ${summary}"
      exit 0 ;;
    epic-boundary)
      notify "Epic finished after ${sid:-the last story}. Review it, then re-run (add --cross-epics to keep going)."
      exit 0 ;;
    roadmap-done)
      notify "No stories left in the roadmap phase. ${summary}"
      exit 0 ;;
    *)
      notify "Stopped: unknown status '$result'. See $log."
      exit 1 ;;
  esac
done
notify "Story cap reached: $merged stories merged this run."

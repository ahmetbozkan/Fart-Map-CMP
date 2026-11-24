#!/usr/bin/env bash
# Runs stories unattended in headless Claude Code sessions until one needs the owner.
# Usage: scripts/autopilot.sh [options]
#   --max N             stop after N merged stories (default 5)
#   --max-turns N       turn cap per session (default 400)
#   --cross-epics       let the agent open the next epic (default: stop at each epic boundary)
#   --hold-sensitive    don't merge PRs touching rules, functions, entitlements or location; escalate instead
#   --story ID          start with this story instead of "next"
#   --fresh             ignore a saved interrupted session and start a new one
#   --no-wait           exit instead of waiting on a usage limit or for your answer on a needs-owner issue
#   --max-wait-hours N  longest wait for a limit reset or your answer (default 8)
# Interrupted runs (usage limit, Ctrl+C, crash, PC restart) resume the same Claude session on the next start.
# Live view: this terminal, or `tail -f .autopilot/live.log`. Phone pings: set AUTOPILOT_NTFY_TOPIC in
# scripts/autopilot.local.env (gitignored). Details: .claude/README.md "Autopilot".
set -uo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

max=5; max_turns=400; flags=""; story="next"; fresh=0; wait=1; max_wait_hours=8
while [[ $# -gt 0 ]]; do
  case "$1" in
    --max) max="$2"; shift 2 ;;
    --max-turns) max_turns="$2"; shift 2 ;;
    --cross-epics) flags="$flags --cross-epics"; shift ;;
    --hold-sensitive) flags="$flags --hold-sensitive"; shift ;;
    --story) story="$2"; shift 2 ;;
    --fresh) fresh=1; shift ;;
    --no-wait) wait=0; shift ;;
    --max-wait-hours) max_wait_hours="$2"; shift 2 ;;
    -h|--help) sed -n '2,15p' "$0"; exit 0 ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done

# Local, gitignored settings (AUTOPILOT_NTFY_TOPIC, AUTOPILOT_NOTIFY_CMD, ...).
if [[ -f scripts/autopilot.local.env ]]; then set -a; . scripts/autopilot.local.env; set +a; fi
# gh may be missing from PATH right after installing it on Windows.
command -v gh >/dev/null || { [[ -x "/c/Program Files/GitHub CLI/gh.exe" ]] && export PATH="$PATH:/c/Program Files/GitHub CLI"; }

dir=".autopilot"
status_file="$dir/status.json"     # written by the agent as its last step
session_file="$dir/session.json"   # written by the viewer at session start; removed once a session finishes
result_file="$dir/last-result.json"
live_log="$dir/live.log"
mkdir -p "$dir/logs"

log() { echo "[autopilot $(date +%H:%M)] $*" | tee -a "$live_log"; }

notify() { # notify <message>: terminal, phone (ntfy / custom command), desktop
  local msg="$1"
  log "$msg"
  printf '\a'
  if [[ -n "${AUTOPILOT_NTFY_TOPIC:-}" ]]; then
    curl -s -o /dev/null -H "Title: Fart Map autopilot" -d "$msg" "${AUTOPILOT_NTFY_SERVER:-https://ntfy.sh}/$AUTOPILOT_NTFY_TOPIC" \
      || echo "[autopilot] ntfy failed" >&2
  fi
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

json() { # json <file> <field> -> value, empty if missing
  node -e 'try { const s = JSON.parse(require("fs").readFileSync(process.argv[1], "utf8")); const v = s[process.argv[2]]; process.stdout.write(v == null ? "" : String(v)); } catch { }' "$1" "$2"
}

sleep_until() { # sleep_until <seconds> <reason>; returns 1 if it would exceed --max-wait-hours
  local secs="$1" cap=$((max_wait_hours * 3600))
  if [[ $secs -gt $cap ]]; then return 1; fi
  log "waiting $((secs / 60)) min ($2), until about $(date -d "+$secs seconds" +%H:%M 2>/dev/null || echo later). Ctrl+C is safe; the next run resumes."
  sleep "$secs"
}

owner_replied() { # owner_replied <issue>: true if a comment without the autopilot marker exists, or the issue is closed
  gh issue view "$1" --json state,comments \
    --jq '(.state == "CLOSED") or ([.comments[] | select(.body | startswith("<!-- autopilot") | not)] | length > 0)' 2>/dev/null \
    | grep -q true
}

wait_for_owner() { # wait_for_owner <issue>: poll until the owner answers on the issue (phone-friendly)
  local issue="$1" deadline=$(( $(date +%s) + max_wait_hours * 3600 ))
  log "waiting for your answer on issue #$issue (checking every 3 min, up to ${max_wait_hours}h). Reply on GitHub, from your phone is fine."
  while [[ $(date +%s) -lt $deadline ]]; do
    if owner_replied "$issue"; then notify "Got your answer on issue #$issue, continuing."; return 0; fi
    sleep 180
  done
  return 1
}

# ---------- prerequisites: fail fast instead of an hour in ----------
command -v claude >/dev/null || { echo "claude CLI not on PATH" >&2; exit 1; }
command -v node >/dev/null   || { echo "node not on PATH" >&2; exit 1; }
command -v gh >/dev/null     || { echo "gh (GitHub CLI) not on PATH; install it and run 'gh auth login'" >&2; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "gh is not authenticated; run 'gh auth login'" >&2; exit 1; }
branch="$(git rev-parse --abbrev-ref HEAD)"
if [[ -n "$(git status --porcelain)" ]]; then
  if [[ "$branch" == "main" ]]; then
    echo "Uncommitted changes on main. Commit, stash or move them to a branch first." >&2; exit 1
  fi
  log "uncommitted changes on $branch: treating them as the interrupted story's work and resuming."
fi
[[ $fresh -eq 1 ]] && rm -f "$session_file"

trap 'echo; log "stopped by you. The session is saved; run the script again to resume where it stopped."; exit 130' INT

merged=0; crashes=0; i=0
while [[ $merged -lt $max ]]; do
  i=$((i + 1))
  rm -f "$status_file" "$result_file"
  stamp="$(date +%Y%m%d-%H%M%S)"
  raw="$dir/logs/run-$stamp.jsonl"
  sid="$(json "$session_file" id)"
  if [[ -n "$sid" ]]; then
    mode="resume"
    prompt="You were interrupted in the middle of an /autopilot-story run (args: next$flags): a usage limit, a crash or the owner stopped the process. Continue from where you stopped. First re-check git status, the current branch, open PRs and the story's Tasks and Evidence, because work may have been committed after your last message. Then finish the remaining steps of the skill and write .autopilot/status.json at the end."
    resume_args=(--resume "$sid")
    log "session $i: resuming interrupted session $sid"
  else
    mode="new"
    prompt="/autopilot-story $story$flags"
    resume_args=()
    log "session $i: $prompt"
  fi
  log "live log: $live_log   raw: $raw"

  claude -p "$prompt" "${resume_args[@]}" \
    --permission-mode acceptEdits \
    --settings .claude/autopilot.settings.json \
    --max-turns "$max_turns" \
    --output-format stream-json --verbose 2> "$dir/logs/run-$stamp.err" \
    | node scripts/autopilot-view.mjs --raw "$raw" --live "$live_log" --session "$session_file" --result "$result_file"
  exit_code=${PIPESTATUS[0]}
  story="next"

  result="$(json "$status_file" result)"
  if [[ -n "$result" ]]; then
    rm -f "$session_file"   # finished properly: the next run starts a fresh session
  else
    result="crashed"
  fi
  sid_story="$(json "$status_file" story)"; pr="$(json "$status_file" pr)"; issue="$(json "$status_file" issue)"; summary="$(json "$status_file" summary)"
  log "session $i ended: $result ${sid_story:+($sid_story)} ${pr:+PR #$pr} ${issue:+issue #$issue} (claude exit $exit_code)"
  [[ -n "$summary" ]] && log "  $summary"

  case "$result" in
    merged)
      merged=$((merged + 1)); crashes=0
      notify "Merged ${sid_story:-a story}${pr:+ (PR #$pr)}. $summary" ;;
    escalated)
      notify "Needs you: ${sid_story:-a story} is blocked${issue:+, issue #$issue}. $summary"
      if [[ $wait -eq 1 && -n "$issue" ]] && wait_for_owner "$issue"; then crashes=0; continue; fi
      exit 0 ;;
    epic-boundary)
      notify "Epic finished after ${sid_story:-the last story}. Review it, then re-run (add --cross-epics to keep going)."
      exit 0 ;;
    roadmap-done)
      notify "No stories left in the roadmap phase. $summary"
      exit 0 ;;
    crashed)
      reason="$(json "$result_file" result)"; [[ -n "$reason" ]] || reason="$(json "$result_file" terminal_reason)"
      turns="$(json "$result_file" num_turns)"
      if grep -qiE 'authenticat|oauth|log ?in|credit balance' <<<"$reason"; then
        notify "Stopped: Claude Code can't run ($reason). Run 'claude' once to sign in, then re-run; it resumes."
        exit 1
      fi
      limit_wait="$(node scripts/autopilot-view.mjs --limit-wait "$reason")"
      if [[ -n "$limit_wait" ]]; then
        notify "Usage limit hit ($reason). Work is saved; it resumes when the limit resets."
        if [[ $wait -eq 1 ]] && sleep_until "$limit_wait" "usage limit"; then continue; fi
        log "not waiting (--no-wait or longer than ${max_wait_hours}h). Run the script again later; it resumes."
        exit 0
      fi
      if [[ "$mode" == "resume" && "${turns:-0}" -le 1 ]]; then
        log "could not resume session $sid ($reason); starting a fresh session, which picks the story up from git and STATE."
        rm -f "$session_file"
      fi
      crashes=$((crashes + 1))
      if [[ $crashes -ge 2 ]]; then
        notify "Stopped: two sessions in a row ended without finishing ($reason). See $raw. Re-running resumes."
        exit 1
      fi
      log "session ended early (${reason:-no reason given}); trying once more." ;;
    *)
      notify "Stopped: unknown status '$result'. See $raw."
      exit 1 ;;
  esac
done
notify "Story cap reached: $merged stories merged this run."

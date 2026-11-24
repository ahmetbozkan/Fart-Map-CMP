#!/usr/bin/env node
// Live view for scripts/autopilot.sh. Reads `claude -p --output-format stream-json --verbose` on stdin and:
//   - prints one readable line per step (tool calls, short assistant text, errors, denials) to the terminal
//     and to --live (follow it from another window: `tail -f .autopilot/live.log`);
//   - keeps the raw stream in --raw;
//   - saves the session id to --session as soon as the session starts, so an interrupted run can be resumed
//     with `claude --resume <id>` even after Ctrl+C or a usage limit;
//   - writes the final `result` event to --result;
//   - sends phone notifications for milestones (branch, PR opened, merged) when AUTOPILOT_NTFY_TOPIC or
//     AUTOPILOT_NOTIFY_CMD is set.
// Also: `node scripts/autopilot-view.mjs --limit-wait "<message>"` prints the seconds until a usage limit
// resets (parsed from the message), or nothing if it isn't a usage-limit message.
import { appendFileSync, writeFileSync, readFileSync, existsSync } from 'node:fs';
import { spawn } from 'node:child_process';
import readline from 'node:readline';
import path from 'node:path';

const args = Object.fromEntries(
  process.argv.slice(2).reduce((acc, a, i, all) => (a.startsWith('--') ? [...acc, [a.slice(2), all[i + 1]]] : acc), []),
);

// ---------- usage-limit parsing (exported behaviour, used by autopilot.sh) ----------
export const LIMIT_RE = /usage limit|limit reached|hit your (usage )?limit|rate.?limit|out of (extra )?usage|limit will reset|resets? (at|in)\b/i;

export function limitWaitSeconds(message, now = new Date()) {
  const msg = String(message || '');
  if (!LIMIT_RE.test(msg)) return null;
  const epoch = msg.match(/\|(\d{10})\b/);
  if (epoch) return Math.max(60, Number(epoch[1]) - Math.floor(now.getTime() / 1000) + 120);
  const rel = msg.match(/in\s+(?:(\d+)\s*h(?:ours?)?)?\s*(?:(\d+)\s*m(?:in(?:utes?)?)?)?/i);
  if (rel && (rel[1] || rel[2])) return Math.max(60, (Number(rel[1] || 0) * 60 + Number(rel[2] || 0)) * 60 + 120);
  const clock = msg.match(/resets?\s+(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)?/i);
  if (clock) {
    let h = Number(clock[1]);
    const m = Number(clock[2] || 0);
    const ap = (clock[3] || '').toLowerCase();
    if (ap === 'pm' && h < 12) h += 12;
    if (ap === 'am' && h === 12) h = 0;
    const t = new Date(now);
    t.setHours(h, m, 0, 0);
    if (t <= now) t.setDate(t.getDate() + 1);
    return Math.round((t - now) / 1000) + 120;
  }
  return 30 * 60; // a limit, but no reset time we can read: try again in 30 minutes
}

if (args['limit-wait'] !== undefined) {
  const s = limitWaitSeconds(args['limit-wait']);
  if (s != null) process.stdout.write(String(s));
  process.exit(0);
}

// ---------- phone notifications ----------
export function notifyPhone(message) {
  const topic = process.env.AUTOPILOT_NTFY_TOPIC;
  if (topic && typeof fetch === 'function') {
    const server = process.env.AUTOPILOT_NTFY_SERVER || 'https://ntfy.sh';
    fetch(`${server}/${encodeURIComponent(topic)}`, { method: 'POST', body: message, headers: { Title: 'Fart Map autopilot' } })
      .catch((e) => process.stderr.write(`[autopilot] ntfy failed: ${e.message}\n`));
  }
  if (process.env.AUTOPILOT_NOTIFY_CMD) {
    const p = spawn('bash', ['-c', process.env.AUTOPILOT_NOTIFY_CMD], { env: { ...process.env, AUTOPILOT_MESSAGE: message }, stdio: 'ignore' });
    p.on('error', (e) => process.stderr.write(`[autopilot] AUTOPILOT_NOTIFY_CMD failed: ${e.message}\n`));
  }
}

// ---------- stream rendering ----------
const repo = process.cwd();
const rel = (p) => { const s = String(p || ''); const r = path.relative(repo, s); return r && !r.startsWith('..') ? r.replace(/\\/g, '/') : s; };
const one = (s, n = 160) => String(s ?? '').replace(/\s+/g, ' ').trim().slice(0, n);
const time = () => new Date().toTimeString().slice(0, 8);

function out(line, indent = '') {
  const text = `${time()} ${indent}${line}`;
  process.stdout.write(text + '\n');
  if (args.live) { try { appendFileSync(args.live, text + '\n'); } catch { /* the terminal line is enough */ } }
}

function describeTool(name, input = {}) {
  switch (name) {
    case 'Bash': return `$ ${one(input.description ? `${input.description}  [${input.command}]` : input.command, 200)}`;
    case 'Read': return `read  ${rel(input.file_path)}`;
    case 'Edit': case 'MultiEdit': return `edit  ${rel(input.file_path)}`;
    case 'Write': return `write ${rel(input.file_path)}`;
    case 'Grep': return `grep  ${one(input.pattern, 80)}${input.path ? ' in ' + rel(input.path) : ''}`;
    case 'Glob': return `glob  ${one(input.pattern, 80)}`;
    case 'Agent': case 'Task': return `agent ${input.subagent_type || 'general'}: ${one(input.description || input.prompt, 100)}`;
    case 'Skill': return `skill ${input.skill || input.name || ''} ${one(input.args, 60)}`;
    case 'TodoWrite': return null;
    default: return `${name} ${one(JSON.stringify(input), 100)}`;
  }
}

const tools = new Map(); // tool_use_id -> { name, input }
const toolText = (c) => (Array.isArray(c) ? c.map((x) => x.text || '').join(' ') : String(c ?? ''));

function milestones(tool, resultText) {
  if (tool.name !== 'Bash') return;
  const cmd = String(tool.input.command || '');
  const branch = cmd.match(/git\s+(?:switch|checkout)\s+(?:-c|-b)\s+(\S+)/);
  if (branch) notifyPhone(`Started branch ${branch[1]}`);
  if (/\bgh\s+pr\s+create\b/.test(cmd)) {
    const url = resultText.match(/https:\/\/github\.com\/\S+\/pull\/\d+/);
    if (url) { out(`>>> PR opened: ${url[0]}`); notifyPhone(`PR opened: ${url[0]}`); }
  }
  const merged = cmd.match(/\bgh\s+pr\s+merge\s+(\d+)/);
  if (merged) { out(`>>> PR #${merged[1]} merge command succeeded`); notifyPhone(`PR #${merged[1]} merged`); }
}

function handle(ev) {
  const indent = ev.parent_tool_use_id ? '    | ' : '';
  if (ev.type === 'system' && ev.subtype === 'init') {
    if (args.session && ev.session_id) {
      let prev = {};
      try { if (existsSync(args.session)) prev = JSON.parse(readFileSync(args.session, 'utf8')); } catch { prev = {}; }
      writeFileSync(args.session, JSON.stringify({ ...prev, id: ev.session_id, updated: new Date().toISOString() }, null, 2));
    }
    out(`session ${ev.session_id} (model ${ev.model || '?'})`);
    return;
  }
  if (ev.type === 'assistant') {
    for (const c of ev.message?.content || []) {
      if (c.type === 'text' && c.text.trim()) out(`> ${one(c.text, 300)}`, indent);
      if (c.type === 'tool_use') {
        tools.set(c.id, { name: c.name, input: c.input || {} });
        const d = describeTool(c.name, c.input);
        if (d) out(d, indent);
      }
    }
    return;
  }
  if (ev.type === 'user') {
    for (const c of ev.message?.content || []) {
      if (c.type !== 'tool_result') continue;
      const tool = tools.get(c.tool_use_id) || { name: '?', input: {} };
      const text = toolText(c.content);
      if (c.is_error) {
        const denied = /permission|denied|not allowed|blocked by/i.test(text);
        out(`  ! ${denied ? 'DENIED' : 'error'}: ${one(text, 220)}`, indent);
      } else {
        milestones(tool, text);
      }
    }
    return;
  }
  if (ev.type === 'result') {
    if (args.result) writeFileSync(args.result, JSON.stringify(ev, null, 2));
    const mins = Math.round((ev.duration_ms || 0) / 60000);
    out(`session ended: ${ev.subtype}${ev.is_error ? ' (error)' : ''}, ${ev.num_turns ?? '?'} turns, ${mins} min${ev.total_cost_usd ? `, ~$${ev.total_cost_usd.toFixed(2)} API-equivalent` : ''}`);
    if (ev.is_error && ev.result) out(`  claude said: ${one(ev.result, 300)}`);
    const denials = ev.permission_denials || [];
    if (denials.length) {
      out(`  ${denials.length} permission denial(s) this session (add safe ones to .claude/autopilot.settings.json):`);
      for (const d of denials.slice(0, 20)) out(`    - ${d.tool_name}: ${one(d.tool_input?.command || d.tool_input?.file_path || JSON.stringify(d.tool_input), 140)}`);
    }
  }
}

const rl = readline.createInterface({ input: process.stdin, crlfDelay: Infinity });
rl.on('line', (line) => {
  if (args.raw) { try { appendFileSync(args.raw, line + '\n'); } catch { /* raw log is best effort */ } }
  if (!line.trim()) return;
  let ev;
  try { ev = JSON.parse(line); } catch { out(line); return; }
  try { handle(ev); } catch (e) { out(`[viewer] could not render an event: ${e.message}`); }
});

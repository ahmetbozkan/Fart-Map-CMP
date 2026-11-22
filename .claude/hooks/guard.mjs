#!/usr/bin/env node
// PreToolUse guard (Read | Edit | Write | MultiEdit | Bash). Exit 2 = block (stderr is shown to Claude).
// 1. Never read/edit/print real Firebase configs or secrets (templates are fine).
// 2. Never push to main/master or force-push; never merge PRs.
// 3. Before `git commit`, run scripts/check-no-secrets.sh on the index.
import { readFileSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import path from 'node:path';

let input;
try { input = JSON.parse(readFileSync(0, 'utf8')); } catch { process.exit(0); }
const tool = input.tool_name ?? '';
const ti = input.tool_input ?? {};
const projectDir = process.env.CLAUDE_PROJECT_DIR || input.cwd || process.cwd();

const SECRET = /(google-services\.json|GoogleService-Info[^\/\\\s"']*\.plist|secrets\.properties|[\/\\]\.fartmap[\/\\])(?!\.template)/i;
const isSecretPath = (p) => !!p && SECRET.test(p) && !/\.template$/i.test(p) && !/\.example$/i.test(p);
const block = (msg) => { process.stderr.write(`Blocked by .claude/hooks/guard.mjs: ${msg}\n`); process.exit(2); };

if (['Read', 'Edit', 'Write', 'MultiEdit', 'NotebookEdit'].includes(tool)) {
  const p = ti.file_path || ti.notebook_path || '';
  if (isSecretPath(p)) block(`${path.basename(p)} is a real config/secret file. Use the .template, or ask the owner (configs are injected by scripts/inject-config.sh).`);
  process.exit(0);
}

if (tool === 'Bash') {
  const cmd = String(ti.command || '');
  const allowedSecretMentions = /(scripts\/inject-config\.sh|scripts\/check-no-secrets\.sh|git (ls-files|check-ignore|status|rm --cached)|\.template)/;
  if (SECRET.test(cmd) && !allowedSecretMentions.test(cmd)) {
    block('the command touches a real config/secret file. Only scripts/inject-config.sh may write them; nothing may print them.');
  }
  if (/\bgit\s+push\b/.test(cmd)) {
    if (/(--force\b|\s-f\b|--force-with-lease)/.test(cmd)) block('force-push is not allowed.');
    if (/(\s|:)(main|master)(\s|$)/.test(cmd) || /HEAD:(refs\/heads\/)?(main|master)\b/.test(cmd)) block('pushing to main is not allowed; push a feature branch and open a draft PR.');
  }
  if (/\bgh\s+pr\s+merge\b/.test(cmd)) block('agents never merge PRs; the owner merges.');
  if (/\bgit\s+commit\b/.test(cmd)) {
    const r = spawnSync('bash', [path.join(projectDir, 'scripts', 'check-no-secrets.sh')], { cwd: projectDir, encoding: 'utf8' });
    if (r.status !== 0) block(`check-no-secrets failed:\n${r.stderr || r.stdout}`);
  }
}
process.exit(0);

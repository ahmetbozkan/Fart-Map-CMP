#!/usr/bin/env node
// SessionStart: print a compact orientation (branch, dirty files, STATE "Now" block) into context.
import { readFileSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import path from 'node:path';

const dir = process.env.CLAUDE_PROJECT_DIR || process.cwd();
const git = (...a) => (spawnSync('git', a, { cwd: dir, encoding: 'utf8' }).stdout || '').trim();

const branch = git('rev-parse', '--abbrev-ref', 'HEAD') || 'unknown';
const dirty = git('status', '--short').split('\n').filter(Boolean);
let now = '';
try {
  const state = readFileSync(path.join(dir, 'planning', 'STATE.md'), 'utf8').replace(/\r\n/g, '\n');
  const m = state.match(/## Now\n([\s\S]*?)(\n## |$)/);
  const n = state.match(/## Next\n([\s\S]*?)(\n## |$)/);
  now = [m && `Now:\n${m[1].trim()}`, n && `Next:\n${n[1].trim()}`].filter(Boolean).join('\n');
} catch { now = 'planning/STATE.md not found.'; }

const lines = [
  `[fart-map] branch: ${branch}${branch === 'main' ? ' (create a feature branch before changing code)' : ''}`,
  `[fart-map] uncommitted files: ${dirty.length}${dirty.length ? ' -> ' + dirty.slice(0, 8).join(', ') + (dirty.length > 8 ? ', ...' : '') : ''}`,
  `[fart-map] platform: ${process.platform === 'darwin' ? 'macOS (iOS builds available)' : process.platform + ' (iOS verified by CI only)'}`,
  now,
];
process.stdout.write(lines.join('\n') + '\n');

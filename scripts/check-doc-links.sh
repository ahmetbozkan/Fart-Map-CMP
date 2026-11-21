#!/usr/bin/env bash
# Fails if a markdown file references a repo path that does not exist.
# Scans root *.md, planning/ (except archive), .claude/, backend/, scripts/, .github/.
# Checks `backticked` paths and [links](paths). Skips gitignored paths (real configs), placeholders
# (NNNN, PP-EE) and entries listed in scripts/doc-links-allow.txt (planned files).
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
node - <<'EOF'
const fs = require('fs'), path = require('path'), { execSync } = require('child_process');
const NL = String.fromCharCode(10), CR = String.fromCharCode(13);
const roots = ['planning', '.claude', 'backend', 'scripts', '.github'];
const files = fs.readdirSync('.').filter(f => f.endsWith('.md'));
const walk = d => { if (!fs.existsSync(d)) return; for (const e of fs.readdirSync(d, { withFileTypes: true })) {
  const p = path.join(d, e.name);
  if (e.isDirectory()) { if (['archive', 'node_modules', 'build', 'lib'].includes(e.name)) continue; walk(p); }
  else if (p.endsWith('.md')) files.push(p); } };
roots.forEach(walk);
const allowFile = 'scripts/doc-links-allow.txt';
const allow = fs.existsSync(allowFile)
  ? fs.readFileSync(allowFile, 'utf8').split(NL).map(s => s.split(CR).join('').trim()).filter(s => s && !s.startsWith('#'))
  : [];
const ignored = p => { try { execSync('git check-ignore -q "' + p + '"', { stdio: 'ignore' }); return true; } catch { return false; } };
const prefixes = /^(planning|\.claude|scripts|backend|shared|firebaseData|umbrella|androidApp|iosApp|gradle|\.github)\//;
let bad = 0;
for (const f of files) {
  const text = fs.readFileSync(f, 'utf8');
  const refs = new Set();
  for (const m of text.matchAll(/`([^`\s]+)`/g)) refs.add(m[1]);
  for (const m of text.matchAll(/\]\(([^)\s#]+)(#[^)]*)?\)/g)) refs.add(m[1]);
  for (let r of refs) {
    if (/^https?:/.test(r) || /[*<>{}$|]/.test(r) || r.includes('…') || r.includes('...') || /NNNN|PP-EE|NNN/.test(r)) continue;
    r = r.replace(/[:,.;]+$/, '').replace(/:\d+$/, '');
    let target;
    if (prefixes.test(r)) target = r;
    else if (/^\.{1,2}\//.test(r) || (/\.md$/.test(r) && !r.includes(' '))) target = path.join(path.dirname(f), r);
    else continue;
    if (fs.existsSync(target)) continue;
    // bare doc names like `Scope.md`: fine if a live doc with that name exists
    if (/\.md$/.test(r) && !prefixes.test(r) && files.some(x => path.basename(x) === path.basename(r))) continue;
    if (allow.includes(r) || allow.includes(path.basename(r)) || ignored(target)) continue;
    console.log(f + ': missing ' + r); bad++;
  }
}
if (bad) { console.error('check-doc-links: ' + bad + ' broken reference(s)'); process.exit(1); }
console.log('check-doc-links: ok (' + files.length + ' files)');
EOF

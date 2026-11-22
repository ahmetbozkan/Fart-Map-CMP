## Summary
<!-- What changed and why, in 2–4 bullets. -->

Story: `planning/stories/STORY-…` (or "chore / planning")

## Definition of Done (`planning/WorkingAgreement.md` §5)
- [ ] Acceptance criteria met on Android **and** iOS
- [ ] `scripts/verify.sh --full` green locally (Windows: iOS verified by the CI `ios` job)
- [ ] CI green: secret scan, Android, iOS, backend
- [ ] Tests added: ViewModel transitions / mapper round-trips / rules allow+deny cases / regression test for bugs
- [ ] No secrets, no swallowed errors, no layering violations (detekt + Konsist)
- [ ] Story checklist + `planning/STATE.md` updated; a changed contract updates its owner doc
- [ ] Any decision change is a **Proposed** ADR in `planning/decisions/`

## Not verified / follow-ups
<!-- Be explicit about anything not run or not checked. -->

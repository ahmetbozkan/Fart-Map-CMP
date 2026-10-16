# Skill: Incident Debug Recovery

## Purpose
Run autonomous bug triage and recovery loops safely.

## Sources
- `planning/architecture/Observability.md`
- `planning/execution/SYSTEM.md`
- `planning/meta/AgentGuardrails.md`

## Procedure
1. Capture reproducible failure case.
2. Classify failure category (network/auth/validation/conflict/unknown).
3. Localize layer and contract mismatch.
4. Apply minimal corrective change preserving architecture boundaries.
5. Add regression test and re-validate acceptance criteria.

## Failure recovery
- If issue involves security/privacy ambiguity or doc contradiction, escalate with recommended default.

## Validation
- [ ] Repro fails before fix and passes after fix
- [ ] Regression protection added
- [ ] No new architecture or security violations introduced

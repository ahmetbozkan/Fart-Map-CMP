# Skill: Security and Privacy Guardrails

## Purpose
Apply secrets, privacy, and safety constraints during implementation and review.

## Sources
- `planning/architecture/SecurityAndSecrets.md`
- `planning/product/ContentSafety.md`
- `planning/architecture/Observability.md`

## Procedure
1. Classify touched data by sensitivity.
2. Verify privacy-safe defaults and visibility rules.
3. Confirm backend-authoritative enforcement for access control.
4. Check local cache invalidation behavior for auth/visibility changes.
5. Enforce redacted logging and no-secrets policy.

## Failure recovery
- Any potential secret exposure or precise-location leak -> stop and escalate.

## Validation
- [ ] No secrets in tracked files
- [ ] No precise-location leakage in public/log channels
- [ ] Safety controls included for affected feature class

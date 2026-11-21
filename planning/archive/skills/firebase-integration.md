# Skill: Firebase Integration

## Purpose
Implement Firebase-backed behavior through rule-safe repository boundaries.

## Sources
- `planning/architecture/FirebaseStrategy.md`
- `planning/architecture/DataContracts.md`
- `planning/architecture/SecurityAndSecrets.md`

## Procedure
1. Confirm collection/field contract in DataContracts.
2. Implement repository remote operations in data layer only.
3. Apply authenticated-user ownership checks.
4. Respect visibility/follow/block rule intent.
5. Ensure no secrets in code or tracked files.

## Failure recovery
- Permission-denied path: treat as backend-authoritative, map to typed error, do not bypass.

## Validation
- [ ] Client-only access control not relied on
- [ ] Rule-sensitive flows tested
- [ ] Sensitive payloads not logged

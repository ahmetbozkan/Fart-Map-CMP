# Skill: Multiplatform Boundaries

## Purpose
Implement shared/platform separation using expect/actual seams.

## Sources
- `planning/architecture/MultiplatformStrategy.md`
- ADR-0008

## Procedure
1. Identify platform-forced capability.
2. Define minimal shared contract in shared code.
3. Implement one actual per platform.
4. Keep business logic in shared layer.
5. Verify behavioral parity across platforms.

## Failure recovery
- Platform divergence: classify as parity defect, do not silently accept single-platform behavior.

## Validation
- [ ] No platform SDK imports in shared business code
- [ ] One contract, two actuals
- [ ] Parity checks documented

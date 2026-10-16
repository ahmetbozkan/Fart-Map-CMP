# Skill: Navigation and Session

## Purpose
Implement typed navigation and session-driven entry/exit behavior.

## Sources
- `planning/architecture/NavigationStrategy.md`
- ADR-0005, ADR-0006

## Procedure
1. Define typed route and args.
2. Emit nav events from ViewModel, execute in UI.
3. Map session states to start destination.
4. Verify sign-in/sign-out back-stack clearing rules.
5. Add serialization/deserialization route tests.

## Failure recovery
- Invalid args: route to safe fallback state, not crash-only behavior.

## Validation
- [ ] No ad-hoc string routes
- [ ] Start destination session-driven
- [ ] Back-stack rules tested

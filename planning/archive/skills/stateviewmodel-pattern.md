# Skill: StateViewModel Pattern

## Purpose
Apply the single presentation-state pattern (State/Action/Event) consistently.

## Sources
- `planning/architecture/Architecture.md`
- `planning/architecture/ArchitecturePrinciples.md`
- ADR-0004

## Procedure
1. Define sealed `State`, `Action`, and `Event` types for the screen.
2. Handle actions in ViewModel; mutate state deterministically.
3. Emit one-time effects as Events.
4. Map failures to typed error states/events; do not swallow.
5. Keep UI as pure state renderer + action dispatcher.

## Failure recovery
- If state transitions are unclear, write transition table before coding.

## Validation
- [ ] No alternate screen-state pattern introduced
- [ ] Errors become typed state/event
- [ ] Events are one-time effects only

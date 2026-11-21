# Skill: Architecture Governance

## Purpose
Apply architecture constraints before and during implementation/review.

## Sources
- `planning/architecture/ArchitecturePrinciples.md`
- `planning/architecture/Architecture.md`
- relevant ADRs in `planning/meta/decisions/`

## Procedure
1. Identify touched feature and layer boundaries.
2. Validate dependency direction (Presentation -> Domain <- Data).
3. Confirm existing pattern for concern (state/nav/repo/sync).
4. Reject or escalate any proposed second pattern.
5. Record architecture links in story/task evidence.

## Failure recovery
- If boundary ambiguity exists, stop implementation and map concern to owner doc.

## Validation
- [ ] No boundary violations introduced
- [ ] ADR constraints respected
- [ ] Pattern consistency preserved

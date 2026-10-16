# ADR-0009: Follow-Only Social Graph with Approval

- Status: Accepted
- Date: 2026-06-20
- Source: user decision

## Context
The planning system had an unresolved social-graph decision: Follow vs Friend vs both.

## Decision
Use a **follow-only** model with **follow request approval**:
- Follows are not automatic.
- Target user must approve incoming follow requests.
- Follower-scoped visibility is granted only to approved followers.

## Rationale
- Preserves social interactions while adding explicit privacy control.
- Fits map-based visibility sensitivity and content safety requirements.
- Keeps MVP simpler than supporting both follow + friend models.

## Consequences
- `Glossary.md`, `Features.md`, and `ContentSafety.md` should use this as authoritative.
- `SocialSystems.md` should define request lifecycle, approval states, and edge cases (cancel/revoke/block).

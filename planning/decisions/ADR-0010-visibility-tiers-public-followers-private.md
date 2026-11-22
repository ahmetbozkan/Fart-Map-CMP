# ADR-0010: MVP Visibility Tiers = Public / Followers / Private

- Status: Accepted
- Date: 2026-06-20
- Source: user decision

## Context
Visibility tiers were previously unresolved and impacted safety, social graph behavior, and data contracts.

## Decision
MVP visibility tiers are fixed to:
- **Public**
- **Followers** (approved followers only)
- **Private**

## Rationale
- Keeps MVP simple.
- Aligns with follow-request approval model.
- Preserves privacy control while enabling social/map behavior.

## Default (added 2026-11-22)
The tiers are unchanged. The *default* tier for new content is set by ADR-0023: PUBLIC in Phase 1 for the 18+ age band, FOLLOWERS for the 13–17 band, and FOLLOWERS for everyone once SOCIAL-03 ships; the user's last-used choice wins after the first log.

## Consequences
- `ContentSafety.md`, `Glossary.md`, and `features/` should use these tiers consistently.
- `LocalData.md` (formerly `DataModel.md`) and `DataContracts.md` must encode and round-trip these exact values.

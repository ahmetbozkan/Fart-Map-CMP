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

## Consequences
- `ContentSafety.md`, `Glossary.md`, and `features/` should use these tiers consistently.
- `DataModel.md` and `DataContracts.md` must encode and round-trip these exact values.

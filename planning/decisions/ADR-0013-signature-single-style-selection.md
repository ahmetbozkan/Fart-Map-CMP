# ADR-0013: Signature Composition — Single Style Selection

- Status: Accepted
- Date: 2026-06-20
- Source: user decision

## Context

The Signature and Style System (PROFILE-03) required a decision on how users compose their recurring visual identity: either by selecting one complete pre-composed style from a catalog, or by assembling individual parts (icon + color + effect + frame) into a custom combination.

## Decision

**Single style selection.** The user picks one complete, pre-composed signature from a catalog. There is no mix-and-match of individual components in MVP.

## Rationale

- Simpler to build: no composition engine, no conflict resolution between parts, no invalid-combination validation.
- Simpler to design: each signature is a fully art-directed artifact, not an arbitrary permutation of components.
- Simpler to monetize: signature packs are atomic units — buy a style, use a style.
- Composable parts are a larger design and engineering surface best deferred until user demand and catalog scale justify it.

## Consequences

- `features/` PROFILE-03 TBD note updated.
- `features/` MAP-02 marker styling TBD note updated.
- `features/` item 3 marked resolved.
- Catalog design must treat each signature as a complete, indivisible style. No partial unlocks.
- "Composable identity parts" listed under PROFILE-03 Future Expansion remain valid as a post-MVP direction only.

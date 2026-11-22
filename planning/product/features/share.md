# Sharing Systems

> Implementation spec split from the original Features.md (strategy fields such as pillar impact, personas, loops, monetization ideas and future expansion live in `planning/archive/product/Features.md`). Phase placement authority: `planning/product/Scope.md`.

## SHARE-01 Share Card System
> ADR-0023: a lite version ships in Phase 1 — one rendered card (marker on a stylized map snippet, coarse area name, avatar, flavor, caption, short link/QR; never precise coordinates) through the OS share sheet, from the log success state and the user's own detail sheet.

**Purpose**
Generate screenshot-worthy, context-free artifacts that drive the viral loop.

**Phase**
- Phase 2

**Dependencies**
- CREATE-01
- PROFILE-02
- PROG-03 leaderboards

**Notes / open questions**
- Must never expose precise location.

## SHARE-02 Fart Share Surfaces
**Purpose**
Let users share individual fart posts as the smallest viral unit.

**Phase**
- Phase 1: deep link (App Links / Universal Links on an owned domain), web landing page with store badges, Play Install Referrer attribution (ADR-0023)
- Phase 2: more share surfaces

**Dependencies**
- SHARE-01
- CREATE-03 detailed log quality

**Notes / open questions**
- Best used when content is funny with no explanation.

## SHARE-03 Profile, Milestone, and Rank Shares
**Purpose**
Turn profile identity and progression into social bragging artifacts.

**Phase**
- Phase 3

**Dependencies**
- PROFILE-02
- PROG-03
- ACH-02

**Notes / open questions**
- Should feel brag-worthy, not corporate or sterile.

## SHARE-04 Challenge and Community Result Shares
**Purpose**
Let group and challenge outcomes generate new awareness and reactivation.

**Phase**
- Phase 4

**Dependencies**
- PROG-04 challenges
- COMMUNITY-01 groups

**Notes / open questions**
- Keep readable outside in-app context.

---

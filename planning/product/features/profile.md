# Profile Systems

> Implementation spec split from the original Features.md (strategy fields such as pillar impact, personas, loops, monetization ideas and future expansion live in `planning/archive/product/Features.md`). Phase placement authority: `planning/product/Scope.md`.

## PROFILE-01 Avatar System
**Purpose**
Give each user a recognizable face/character identity beyond a username.

**Phase**
- Phase 1 (free tier plus a la carte purchases, ADR-0020); Phase 3 deeper economy

**Dependencies**
- PROFILE-02 public profile
- MON-01 cosmetic economy

**Notes / open questions**
- MVP should prefer controlled avatar customization over freeform uploads.

## PROFILE-02 Public Profile System
**Purpose**
Make the profile a social object containing history, stats, identity, and reputation.

**Phase**
- Phase 1 light
- Phase 3 deep

**Dependencies**
- PROFILE-04 stats/history
- ACH-01 achievements
- PROG-03 leaderboards

**Notes / open questions**
- Must preserve continuity and public meaning.

## PROFILE-03 Signature and Style System
**Purpose**
Give users a recurring, recognizable visual identity across markers, profile, and shares.

**Phase**
- Phase 3

**Dependencies**
- PROFILE-02
- CREATE-04 flavor system
- MON-01 cosmetic economy

**Notes / open questions**
- Signature composition: **resolved — single style selection.** The user selects one complete style from a catalog of pre-composed signatures. There is no mix-and-match of individual parts (icon + color + effect + frame) in MVP. Composable identity parts remain a future expansion opportunity only. See ADR-0013.

## PROFILE-04 Stats, History, and Reputation
**Purpose**
Translate repeated activity into visible proof of consistency, travel, and social standing.

**Phase**
- Phase 3 (`Scope.md` §5); Phase 2 shows only the streak foundation from PROG-01

**Dependencies**
- PROG-01 streaks
- PROG-02 levels
- ACH-01 achievements

**Notes / open questions**
- Reputation should be transparent enough to avoid black-box distrust.

## PROFILE-05 Collections System
**Purpose**
Let users collect meaningful sets across flavors, places, milestones, or seasons.

**Phase**
- Phase 3

**Dependencies**
- CREATE-04 flavor system
- MAP-06 map memory
- ACH-01 achievements

**Notes / open questions**
- Collections should reward exploration and creativity, not spam volume.

## PROFILE-06 Profile Share Surface
**Purpose**
Turn a profile into an outward-facing identity artifact users can share.

**Phase**
- Phase 3 (`Scope.md` §5)

**Dependencies**
- PROFILE-02
- SHARE-01 share cards

**Notes / open questions**
- Must remain clear and funny even out of context.

---

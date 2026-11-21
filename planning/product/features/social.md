# Social Systems

> Implementation spec split from the original Features.md (strategy fields such as pillar impact, personas, loops, monetization ideas and future expansion live in `planning/archive/product/Features.md`). Phase placement authority: `planning/product/Scope.md`.

## SOCIAL-01 Reactions
**Purpose**
Provide the fastest social response loop on content.

**Phase**
- Phase 1

**Dependencies**
- CREATE-01
- MOD-01 moderation controls

**Notes / open questions**
- Must remain lightweight and fast.

## SOCIAL-02 Comments
**Purpose**
Turn one-off reactions into conversation and relationship depth.

**Phase**
- Phase 2

**Dependencies**
- SOCIAL-01 reactions
- MOD-01 moderation controls

**Notes / open questions**
- Do not drift into generic long-form social discussion.

## SOCIAL-03 Follow Requests, Followers, and Following
**Purpose**
Create the privacy-aware social graph that powers follower-scoped visibility and social retention.

**Phase**
- Phase 2

**Dependencies**
- ADR-0009
- PRIV-01 visibility model
- NOTIF-01 notification types

**Notes / open questions**
- Model is fixed: follow-only with approval.

## SOCIAL-04 Map-Anchored Feed System
**Purpose**
Give users an alternate social consumption surface without undermining the map-first identity.

**Phase**
- Phase 2

**Dependencies**
- SOCIAL-03
- MAP-03 discovery filters
- CREATE-01 content engine

**Notes / open questions**
- Feed complements map; it must never replace map as the product center.

## SOCIAL-05 Social Discovery and Reactivation
**Purpose**
Help users find people worth following and bring them back together over time.

**Phase**
- Phase 2

**Dependencies**
- SOCIAL-03
- NOTIF-01 notifications

**Notes / open questions**
- Discovery must be privacy-aware and minor-safe.

---

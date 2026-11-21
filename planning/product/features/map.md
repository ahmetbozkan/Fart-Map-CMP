# Map Systems

> Implementation spec split from the original Features.md (strategy fields such as pillar impact, personas, loops, monetization ideas and future expansion live in `planning/archive/product/Features.md`). Phase placement authority: `planning/product/Scope.md`.

## MAP-01 Map Home Surface
**Purpose**
The map is the primary home, product center, and entry point into the world of Fart Map.

**Phase**
- Phase 1

**Dependencies**
- MAP-02 marker system
- MAP-03 map filters and layers
- PRIV-01 visibility model

**Notes / open questions**
- Must never be replaced by feed-first UX.

## MAP-02 Marker System
**Purpose**
Represent each Fart as a meaningful, expressive, tappable map artifact.

**Phase**
- Phase 1

**Dependencies**
- CREATE-01 Log Fart engine
- PROFILE-03 signature/style system
- PRIV-02 location privacy and precision control

**Notes / open questions**
- Marker styling depth: signature composition resolved as single style selection (ADR-0013). Marker style is one atomic selection, not a composable set.

## MAP-03 Map Layers, Filters, and Discovery Modes
**Purpose**
Allow users to shift their view between self, approved followers, and public/global discovery.

**Phase**
- Phase 1: all three filters present (ADR-0020); the Followers filter populates in Phase 2 with SOCIAL-03

**Dependencies**
- SOCIAL-03 follow requests
- PRIV-01 visibility model

**Notes / open questions**
- Phase 1 filters are `Mine / Followers / Everyone`. The Followers filter shows a humorous empty state until SOCIAL-03 ships (ADR-0020).

## MAP-04 Nearby Activity and Heatmaps (Highlight System)
**Purpose**
Ensure the map feels alive, especially in low-density environments.

**Phase**
- Phase 2 (`Scope.md` §5). In Phase 1 the Social tab's nearby public list (ADR-0020) covers the low-density case.

**Dependencies**
- MAP-01
- MOD-02 moderation baseline
- `Metrics.md` instrumentation (Observability event taxonomy)

**Notes / open questions**
- Must avoid fake-feeling activity; can use curated highlights before high density exists.

## MAP-05 Place Detail and Context Surface
**Purpose**
Turn a location into a social storytelling surface, not just a point on a map.

**Phase**
- Phase 2

**Dependencies**
- MAP-02
- SOCIAL-01 reactions
- SOCIAL-02 comments

**Notes / open questions**
- Must respect location fuzzing and private-zone rules.

## MAP-06 Map Memory and Travel History
**Purpose**
Give users a spatial history that reinforces identity and return value over time.

**Phase**
- Phase 3

**Dependencies**
- PROFILE-02 public profile
- PRIV-02 location history controls

**Notes / open questions**
- Public exposure must be optional and coarse.

---

# Monetization Systems

> Implementation spec split from the original Features.md (strategy fields such as pillar impact, personas, loops, monetization ideas and future expansion live in `planning/archive/product/Features.md`). Phase placement authority: `planning/product/Scope.md`.

## MON-01 Cosmetic Economy
**Purpose**
Monetize self-expression without harming fairness.

**Phase**
- Phase 1: a la carte avatars and sounds (ADR-0016, ADR-0020), Wardrobe shop, launch catalog (≥ 2 free + 8 paid avatars, 3 free + 9 paid sounds, 2 packs), starter pack after first log, Remote Config + RevenueCat Offerings (ADR-0024); Phase 3: catalog expansion; Phase 4: full catalog

**Dependencies**
- PROFILE-01 avatar system
- PROFILE-03 signature/style
- CREATE-07 sound selection

**Notes / open questions**
- Must remain cosmetic only.

## MON-02 Premium Personalization Subscription / Pass
**Purpose**
Offer a recurring premium layer for users who want deeper expression and status surfaces.

**Phase**
- Phase 2 (ADR-0024), gated on a catalog of ≥ 20 items; voice recording joins it when CREATE-08 ships (Phase 4)

**Dependencies**
- MON-01 cosmetic economy
- PROG-02 / ACH-02 mature identity surfaces

**Notes / open questions**
- Must never gate competitive power or essential social access.

## MON-03 Reward-Based Extras
**Purpose**
Give non-paying users optional access to lightweight extras without harming fairness.

**Phase**
- Phase 1 for avatars and sounds (ADR-0017, ADR-0020)

**Dependencies**
- MON-01

**Notes / open questions**
- Must not become ad-heavy or anti-fun.

---

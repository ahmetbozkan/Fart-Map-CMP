# Privacy Systems

> Implementation spec split from the original Features.md (strategy fields such as pillar impact, personas, loops, monetization ideas and future expansion live in `planning/archive/product/Features.md`). Phase placement authority: `planning/product/Scope.md`.

## PRIV-01 Visibility Model
**Purpose**
Let users control who can see each fart post.

**Phase**
- Phase 1

**Dependencies**
- ADR-0010
- SOCIAL-03 follow requests

**Notes / open questions**
- MVP tiers are fixed: `Public / Followers / Private`.

## PRIV-02 Location Privacy and Precision Control
**Purpose**
Preserve the fun of place without exposing precise real-world risk.

**Phase**
- Phase 1

**Dependencies**
- `ContentSafety.md`
- architecture location rules

**Notes / open questions**
- Public outputs must use fuzzed/coarse location.

## PRIV-03 Consent, Account, and Data Controls
**Purpose**
Give users control over permissions, export, deletion, and notification/privacy settings.

**Phase**
- Phase 1 baseline, Phase 2 richer settings

**Dependencies**
- PRIV-01 visibility model
- PRIV-02 location privacy and precision control
- NOTIF-02

**Notes / open questions**
- Must align with Firebase/data architecture docs later.

---

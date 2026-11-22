# Moderation Systems

> Implementation spec split from the original Features.md (strategy fields such as pillar impact, personas, loops, monetization ideas and future expansion live in `planning/archive/product/Features.md`). Phase placement authority: `planning/product/Scope.md`.

## MOD-01 User Safety Controls: Block, Report, Mute, Comment Controls
**Purpose**
Provide immediate user-level tools to stop abuse and unwanted interaction.

**Phase**
- Phase 1 report baseline
- Phase 2 full controls

**Dependencies**
- SOCIAL-01
- SOCIAL-02
- COMMUNITY-01 for later group-specific controls

**Notes / open questions**
- This is the minimum social safety floor, not an optional extra.

## MOD-02 Content Moderation Pipeline
**Purpose**
Define how unsafe content is flagged, reviewed, acted on, and appealed.

**Phase**
- Phase 1 baseline
- Phase 2 stronger tooling

**Dependencies**
- `ContentSafety.md`
- architecture data/security strategy

**Notes / open questions**
- Must cover captions, comments, profiles, audio, and future community content.

## MOD-03 Anti-Spam, Anti-Spoofing, and Integrity Layer
**Purpose**
Prevent fake location, spam logging, and manipulated progression.

**Phase**
- Phase 1 basic anti-spam
- Phase 3 full competitive integrity

**Dependencies**
- PROG-05 fairness layer
- architecture sync/data strategy

**Notes / open questions**
- Must explicitly defend against spam-grinder behavior.

## MOD-04 Group and Community Moderation
**Purpose**
Protect group spaces and event loops from harassment, pile-ons, and low-quality coordination.

**Phase**
- Phase 4

**Dependencies**
- COMMUNITY-01 groups
- MOD-01 base safety controls

**Notes / open questions**
- Required before community systems can scale.

---

# Notification Systems

> Implementation spec split from the original Features.md (strategy fields such as pillar impact, personas, loops, monetization ideas and future expansion live in `planning/archive/product/Features.md`). Phase placement authority: `planning/product/Scope.md`.

## NOTIF-01 Notification Type Framework
**Purpose**
Define the set of meaningful notification triggers that bring users back for value, not noise.

**Phase**
- Phase 1 minimal: reaction-received push only (ADR-0020); permission asked once after the first successful log (ADR-0023); Phase 2: full framework

**Dependencies**
- SOCIAL-01
- SOCIAL-03
- PROG-01
- PROG-03

**Notes / open questions**
- Core types: reaction, comment, follow request, follow approval, follower post, streak risk, rank movement, challenge/event updates.

## NOTIF-02 Notification Preferences and Safety Controls
**Purpose**
Keep notifications useful, trusted, and user-controlled.

**Phase**
- Phase 2

**Dependencies**
- NOTIF-01
- PRIV-03 privacy controls

**Notes / open questions**
- Critical for trust counter-metrics.

## NOTIF-03 Rank, Streak, and Challenge Return Hooks
**Purpose**
Plant concrete reasons to return after a session ends.

**Phase**
- Phase 2 groundwork, Phase 3 stronger

**Dependencies**
- PROG-01 streaks
- PROG-03 leaderboards
- PROG-04 challenges

**Notes / open questions**
- Must remain valuable, not manipulative.

---

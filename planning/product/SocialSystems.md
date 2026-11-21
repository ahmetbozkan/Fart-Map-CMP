# Social Systems — Fart Map

> **Authority level:** This document expands the SOCIAL-01–05 entries in `features/` with authoritative interaction models, state machines, data contracts, and implementation rules. When this document and `features/` conflict, this document wins for social-specific behavior. Always read `features/` SOCIAL-01–05 entries first for pillar scores, phase assignments, and loop mapping.

**Related documents:**
- `features/` Social Systems (SOCIAL-01–05)
- `ADR-0009` — Follow-only social graph with approval
- `ADR-0010` — MVP visibility tiers: Public / Followers / Private
- `Product.md` — L2, L4, L8
- `Product.md` — Stages 4, 6, 7, 8
- `ContentSafety.md` — social threat model
- `Glossary.md` — all social entity terms

---

## Contents

1. [Social Graph Model](#1-social-graph-model)
2. [SOCIAL-01 Reactions — Interaction Model](#2-social-01-reactions--interaction-model)
3. [SOCIAL-02 Comments — Interaction Model](#3-social-02-comments--interaction-model)
4. [SOCIAL-03 Follow System — Full Lifecycle](#4-social-03-follow-system--full-lifecycle)
5. [SOCIAL-04 Map-Anchored Feed — Feed Rules](#5-social-04-map-anchored-feed--feed-rules)
6. [SOCIAL-05 Social Discovery and Reactivation](#6-social-05-social-discovery-and-reactivation)
7. [Cross-System Data Contracts](#7-cross-system-data-contracts)
8. [Notification Triggers](#8-notification-triggers)
9. [Safety and Abuse Controls](#9-safety-and-abuse-controls)
10. [Implementation Validation Checklist](#10-implementation-validation-checklist)

---

## 1) Social Graph Model

**Authoritative source: ADR-0009.**

The Fart Map social graph is **follow-only with approval**. There is no symmetric "friend" relationship. All follower-scoped visibility behavior (ADR-0010) depends on the graph state defined here.

### Graph entity

```
FollowRelationship {
  from_user_id:    String       // requester / follower
  to_user_id:      String       // target / followee
  status:          Enum         // PENDING | APPROVED | REJECTED | REVOKED | BLOCKED
  requested_at:    Long         // epoch ms
  resolved_at:     Long?        // epoch ms; null if still PENDING
}
```

### Graph states

| Status | Meaning | Content access granted? |
|---|---|---|
| `PENDING` | Request sent; awaiting approval | No — treated as stranger |
| `APPROVED` | Approved by target user | Yes — follower-scoped content visible |
| `REJECTED` | Declined by target; relationship gone | No |
| `REVOKED` | Requester cancelled their own request | No |
| `BLOCKED` | Either party blocked the other | No; block is mutual in effect |

### Visibility resolution rule

When resolving whether user A can see content posted by user B with visibility `FOLLOWERS`:

```
canSee(A, B, FOLLOWERS) = 
  EXISTS FollowRelationship WHERE from_user_id=A AND to_user_id=B AND status=APPROVED
```

There is no group membership, no friend-of-friend, and no default-public fallback for follower content. If the relationship is not `APPROVED`, the content is invisible to A regardless of its actual visibility setting.

### Hard rules (do not relitigate)

- There is no mutual follow requirement. A can follow B without B following A.
- There is no bidirectional "friend" concept in MVP.
- Block is one-directional in intent but mutual in effect: if A blocks B, neither can see each other's content and B cannot send A a follow request.
- Territory is out of MVP scope (ADR-0011) and must not be introduced through social features.

---

## 2) SOCIAL-01 Reactions — Interaction Model

### Purpose

The fastest social feedback mechanism. Must complete in a single tap. Reactions are the primary fuel for L2 (Social Reaction Loop) and the first social reward a user experiences (Journey Stage 4).

### Reaction set (MVP)

| Reaction | Label | Notes |
|---|---|---|
| Laughing | "Dead" | Primary humor reaction |
| Fire | "Toxic" | Intensity/severity reaction |
| Skull | "RIP" | Hyperbolic victim reaction |
| Nose | "Smelled it" | Location-social reaction |
| Crown | "Legend" | Status/respect reaction |

**MVP reaction count: 5.** Premium reaction packs (animated, seasonal) are a Phase 2+ monetization opportunity. The MVP set must always remain free and must not be reduced.

### Interaction rules

1. **Single active reaction per user per fart** — selecting a second reaction replaces the first; it does not add to it.
2. **One tap to react, one tap to undo** — tapping the same active reaction removes it.
3. **Reaction count is always visible** — total reactions (sum of all types) shown on the marker and in the feed. Reaction breakdown by type shown on tap/expand.
4. **Reaction does not require following** — if the fart is `PUBLIC`, any authenticated user can react. If the fart is `FOLLOWERS`, only approved followers can react. `PRIVATE` farts cannot receive reactions from anyone except the author (author can see their own post).
5. **Reactions are not reversible on the backend after expiration** — for MVP, reactions can be removed by the user at any time. No expiry logic unless ContentSafety.md requires it.

### State machine

| State | Meaning |
|---|---|
| `NONE` | User has not reacted |
| `REACTING` | Tap registered; optimistic update applied; network call in flight |
| `REACTED` | Reaction confirmed on backend |
| `FAILED` | Network call failed; optimistic update rolled back; original state restored |

Optimistic update is mandatory for perceived responsiveness. Rollback on failure must be silent (no error toast for a reaction failure unless it persists after retry).

### Data contract

```
Reaction {
  fart_id:     String
  user_id:     String
  type:        Enum    // DEAD | TOXIC | RIP | SMELLED | LEGEND
  created_at:  Long    // epoch ms
}
```

Composite primary key: `(fart_id, user_id)` — enforces one reaction per user per fart at the database level.

---

## 3) SOCIAL-02 Comments — Interaction Model

### Purpose

Enable conversation depth around a fart moment. Comments convert a passive reaction into a social thread, sustaining L2 and seeding L4 (Social Connection Loop).

### Comment model

- **Flat threading for MVP.** No nested replies. A "reply" is a top-level comment that starts with a `@username` mention. Threaded comments are a Phase 3+ consideration.
- **Emoji-native.** Emoji-only comments are valid and encouraged. The humor identity of the product demands this.
- **No minimum length.** A single emoji or character is a valid comment.
- **Soft length limit:** 500 characters. UI should show a character counter starting at 400. Hard server-side limit at 1000 characters; server rejects above this.

### Visibility rules

- Comments on a `PUBLIC` fart are visible to all authenticated users.
- Comments on a `FOLLOWERS` fart are visible only to approved followers of the author.
- Comments on a `PRIVATE` fart are visible only to the author.
- Comment authors cannot override the parent fart's visibility. A follower-scoped fart's comments are follower-scoped even if the commenter's profile is public.

### Interaction rules

1. **Author can delete any comment on their own fart** at any time.
2. **Commenter can delete their own comment** at any time.
3. **No editing of published comments** in MVP. Delete-and-repost is the only correction path.
4. **Mentioning a user (`@username`) in a comment** triggers a notification to the mentioned user (NOTIF-01) regardless of whether they follow the post author.
5. **Moderation:** comments are subject to MOD-01 report flow. A reported comment is hidden pending review. False-positive restoration is a manual process in MVP.

### State machine

| State | Meaning |
|---|---|
| `COMPOSING` | User is typing; not yet submitted |
| `SUBMITTING` | Send tapped; network call in flight |
| `POSTED` | Comment confirmed on backend; visible in thread |
| `FAILED` | Network call failed; comment not posted; input preserved |
| `DELETED` | Soft-deleted; hidden from thread; backend record retained for moderation |

Draft persistence: if the user navigates away while composing, the draft is preserved locally (DataStore) until they return to the same fart's comment thread.

### Data contract

```
Comment {
  comment_id:  String
  fart_id:     String
  author_id:   String
  body:        String
  created_at:  Long    // epoch ms
  deleted_at:  Long?   // soft delete; null if not deleted
  mentions:    List<String>  // user_ids mentioned; derived at write time
}
```

---

## 4) SOCIAL-03 Follow System — Full Lifecycle

**Authoritative source: ADR-0009.**

### Request lifecycle

```
[Requester taps Follow]
        │
        ▼
    PENDING ──────────────────────────────► REVOKED  (requester cancels)
        │
        ├──► APPROVED  (target approves)
        │
        └──► REJECTED  (target declines)
                │
                └──► [relationship gone; requester may re-send later]
```

Additionally, from any non-BLOCKED state:

```
APPROVED / PENDING / REJECTED ──► BLOCKED  (either party blocks)
```

### State rules

**PENDING:**
- The follow request appears in the target's "Follow Requests" inbox (NOTIF-01).
- The requester sees a "Pending" state on the target's profile.
- The requester cannot send another request while one is pending.
- Content access is not granted until `APPROVED`.

**APPROVED:**
- Content access is granted immediately.
- Both users see each other in their followers/following lists.
- Target receives a notification confirming the approval (optional UX — not a hard requirement for MVP, but recommended).

**REJECTED:**
- The relationship record is removed or marked rejected.
- The requester is not notified of the rejection (privacy protection — do not reveal that a rejection occurred; show only that the request is no longer pending).
- The requester may send a new request after a cooldown period. Cooldown: provisional default 7 days; confirm before Phase 2 implementation.

**REVOKED:**
- The requester cancels before the target acts.
- The relationship record is removed.
- No notification to the target if they have not yet seen the request. If they have seen it (opened requests inbox), no notification of revocation.
- Requester may re-send immediately.

**BLOCKED:**
- Initiated by either party.
- Block is mutual in effect: neither party sees the other's content; neither can send follow requests.
- Block is not surfaced to the blocked user — they do not receive a notification. From their perspective, the content is simply unavailable.
- Blocking a user who is a current approved follower removes the follow relationship.
- Unblocking does not restore the prior follow relationship. The former follower must re-request.
- Block list is visible to the blocker in Privacy settings (PRIV-01).

### Follower management

- **Remove a follower:** the approved target can remove an approved follower at any time. The removed follower is not notified. From their perspective, follower-scoped content becomes invisible.
- **Following list:** visible to the user themselves only. Not shown on public profile by default. A user can opt in via a profile privacy setting ("Show who I follow on my profile") — default off. See ADR-0012.
- **Follower count:** always visible on public profile as a single integer. The full follower list is not publicly exposed.

### Data contract

```
FollowRelationship {
  from_user_id:   String
  to_user_id:     String
  status:         Enum    // PENDING | APPROVED | REJECTED | REVOKED | BLOCKED
  requested_at:   Long    // epoch ms
  resolved_at:    Long?   // epoch ms
}
```

Composite primary key: `(from_user_id, to_user_id)`.

Blocks are stored as a separate `Block` entity (`DataModel.md` §4.5, `DataContracts.md` §3.6) so blocking works without a prior follow request; `BLOCKED` on `FollowRelationship` is derived from it. Decided.

---

## 5) SOCIAL-04 Map-Anchored Feed — Feed Rules

### Core constraint

**The feed is never the product. The map is.** The feed is a convenience surface for social catch-up. Any design decision that would cause users to spend more time in the feed than on the map is a pillar violation.

### Feed composition rules

The feed shows farts from users the viewer has approved-follow relationships with, ordered by a combination of:

1. **Recency** — primary sort signal. Newer farts rank higher by default.
2. **Social activity** — farts with recent reactions or comments from people you follow are surfaced higher.
3. **Reaction heat** — farts with a rapid reaction spike in the last hour are eligible for temporary boost.

There is no algorithmic personalization in MVP. Feed is chronological with optional social-activity boost. Full algorithmic ranking is Phase 3.

### Visibility enforcement in feed

- `PUBLIC` farts from followed users: shown in feed.
- `FOLLOWERS` farts from followed users: shown in feed (requester is an approved follower).
- `PRIVATE` farts: **never shown in feed**, even to the author. Private farts are visible only on the author's own profile history.
- Farts from users the viewer has blocked or who have blocked the viewer: **never shown**.

### Feed items

Each feed item must display:

| Field | Source | Notes |
|---|---|---|
| Author avatar | `avatar_id` | Same avatar chosen at log time |
| Author username | user profile | Tappable → author profile |
| Fart timestamp | `timestamp` | Shown as relative time ("2h ago") |
| Location name | reverse geocode of `public_lat/lng` (fuzzed, ADR-0019) | Neighborhood or city level only; never precise address |
| Flavor label | `flavor_level` mapped to label | "Toxic", "Lethal", etc. |
| Description text | `description_text` | Optional; shown if present |
| Reaction counts | aggregated from Reaction collection | Show top 3 reaction types |
| Comment count | aggregated | Tappable → comment thread |
| "View on Map" CTA | deep link to map centered on fart location | Always visible; this is the map bridge |

**"View on Map" CTA is mandatory on every feed item.** Removing it is a pillar violation. Its label may vary ("See where it happened", "Open on Map") but the action must always exist.

### Feed pagination

- Initial load: 20 items.
- Infinite scroll: load 20 more on scroll-to-bottom.
- New content indicator: show a non-intrusive banner at the top ("X new farts") when new content arrives while feed is open. User taps to scroll to top; banner does not auto-scroll.
- Pull-to-refresh is supported.

### Feed empty states

| Condition | Empty state message |
|---|---|
| User follows nobody | "Follow some people to see what's happening near them. Start exploring the map." |
| User follows people but none have logged recently | "Your people have been quiet. Check the map for what's happening nearby." |
| All followed users' farts are private | "Nothing visible right now. (Some people keep their farts to themselves.)" |

Empty states must never be blank screens. Humor is required.

---

## 6) SOCIAL-05 Social Discovery and Reactivation

### Purpose

Prevent "empty graph" churn (new users who can't find anyone to follow) and "dead graph" churn (users whose followees have become inactive).

### Discovery surfaces (MVP)

| Surface | Trigger | Notes |
|---|---|---|
| Map cluster tap → profile | User taps a cluster of markers and sees multiple authors | Tapping an author name/avatar → their public profile |
| Feed item author tap | User taps an author's name or avatar in the feed | → profile with Follow CTA |
| Notification → profile | User taps a notification about another user's action | → that user's profile |
| Search by username | User types a username in a search bar | Returns exact-match and prefix-match results; privacy-safe (public profiles only) |

There are no "suggested follows" or algorithmic discovery recommendations in MVP. These are Phase 2–3 features.

### Reactivation triggers

Reactivation is addressed primarily through NOTIF-01 (notifications). The social system's role is to ensure the notification payload contains a strong social hook:

| Trigger | Notification hook | Loop served |
|---|---|---|
| A followed user logs a new PUBLIC or FOLLOWERS fart | "X just dropped something nearby" | L2, L4 |
| Someone reacts to the user's fart | "X reacted to your fart" | L2 |
| Someone comments on the user's fart | "X commented: [preview]" | L2, L4 |
| A follow request is approved | "X is now following you" | L4 |
| A follow request is received | "X wants to follow you" | L4 |
| A mentioned user | "X mentioned you in a comment" | L2, L4 |

Notification delivery is governed by NOTIF-01. This section defines only the social trigger events that must be sent to the notification system.

### Privacy constraints on discovery

- Search results must show only public profiles and profiles of users who have not opted out of search (PRIV-01 setting TBD).
- Location data must never be surfaced through discovery. A user's profile must not show their current or last-known location.
- Minors (users under 18 if age verification is implemented) must not appear in discovery surfaces. This is a ContentSafety.md requirement.

---

## 7) Cross-System Data Contracts

### Summary of social entities

| Entity | Fields | Primary key | Notes |
|---|---|---|---|
| `Reaction` | `fart_id, user_id, type, created_at` | `(fart_id, user_id)` | One per user per fart |
| `Comment` | `comment_id, fart_id, author_id, body, created_at, deleted_at, mentions` | `comment_id` | Soft delete |
| `FollowRelationship` | `from_user_id, to_user_id, status, requested_at, resolved_at` | `(from_user_id, to_user_id)` | May need separate Block entity |
| `Block` | `blocker_id, blocked_id, created_at` | `(blocker_id, blocked_id)` | If separated from FollowRelationship |

### Aggregated read models (for feed and map)

These are derived values that must be pre-computed or efficiently queryable. They must not be computed on the client for every render.

| Value | Derived from | Frequency |
|---|---|---|
| `reaction_count` on a fart | COUNT(Reaction WHERE fart_id=X) | Updated on every reaction write |
| `reaction_breakdown` on a fart | GROUP BY type on Reaction | Updated on every reaction write |
| `comment_count` on a fart | COUNT(Comment WHERE fart_id=X AND deleted_at IS NULL) | Updated on every comment write/delete |
| `follower_count` on a user | COUNT(FollowRelationship WHERE to_user_id=X AND status=APPROVED) | Updated on approval/removal |
| `following_count` on a user | COUNT(FollowRelationship WHERE from_user_id=X AND status=APPROVED) | Updated on approval/removal/block |

Pre-computing these as denormalized counters (Cloud Firestore counter pattern or equivalent) is strongly recommended. Live-counting from raw collections at query time will not scale.

---

## 8) Notification Triggers

The following social events must emit a notification to the appropriate user. Notification delivery format is defined in NOTIF-01. This table defines only which events require a notification and to whom.

| Event | Recipient | Priority |
|---|---|---|
| Reaction added to my fart | Fart author | Medium |
| Comment added to my fart | Fart author | High |
| `@mention` in a comment | Mentioned user | High |
| Follow request received | Target user | High |
| Follow request approved | Requester | Medium |
| Followed user posts a PUBLIC/FOLLOWERS fart | All approved followers | Low (batched) |

**Batching rule for new-fart notifications:** Do not send an individual push notification for every fart from a followed user. Batch into a daily or session-based digest if the user's followed network is active. Excessive push volume is the fastest way to lose notification permissions. Exact batching strategy is decided in the Phase 2 notification architecture pass.

---

## 9) Safety and Abuse Controls

Full threat model is in `ContentSafety.md`. This section defines only the social-system-specific controls.

### Reactions

- No reaction can expose the reactor's location or identity beyond their username.
- Reaction spam (same user reacting/unreacting repeatedly) should be rate-limited at the API level. Rate limit: provisional default max 10 reaction changes per fart per user per hour, enforced server-side; confirm before implementation.
- Reactions on `PRIVATE` farts from users other than the author must be blocked at the server. Client-side gating is not sufficient.

### Comments

- Comments must be screened against the profanity/content policy baseline defined in ContentSafety.md before being written to the database. Async post-write screening is acceptable for MVP; synchronous blocking is not required unless the moderation vendor supports it without latency impact.
- Report flow: any user can report a comment. Reported comments are soft-hidden pending review. Report data is stored and queryable by moderation tooling (MOD-01).
- A user who is blocked by the author cannot comment on the author's farts. Enforce at server level.

### Follow graph

- A blocked user cannot send a follow request to the blocker. Enforce at server level.
- A user with a pending follow request cannot send another request to the same target. Enforce at server level.
- Follow request spam (sending and revoking to generate notifications) must be rate-limited. Rate limit: provisional default max 3 requests to the same user per 24h after revoke/reject; confirm before Phase 2 implementation.

### Discovery

- Search must not return users who have blocked the searcher.
- Search must not return users whose accounts are suspended.
- No cross-user location correlation through social surfaces (cannot infer a user's location from who they follow or who follows them).

---

## 10) Implementation Validation Checklist

The following must be satisfied at implementation. These are not guidelines — they are gates.

**Social graph:**
- [ ] Follow request creates a `PENDING` relationship; does not grant content access until `APPROVED`
- [ ] `FOLLOWERS` content is invisible to users whose relationship status is not `APPROVED`
- [ ] Block prevents all content visibility and follow requests from both directions
- [ ] Unblock does not restore prior follow relationship
- [ ] Rejection is not communicated to the requester (privacy protection)
- [ ] Removing a follower does not notify the removed follower

**Reactions:**
- [ ] One reaction per user per fart enforced at database level (composite PK or equivalent)
- [ ] Optimistic update applied immediately; rolled back silently on network failure
- [ ] Reactions on non-visible farts blocked at server level

**Comments:**
- [ ] Draft preserved on navigation-away
- [ ] Author can delete any comment on their own fart
- [ ] Commenter can delete their own comment
- [ ] Soft-delete: record retained for moderation, not shown in thread
- [ ] Blocked user cannot comment on blocker's farts (server-enforced)

**Feed:**
- [ ] "View on Map" CTA present on every feed item
- [ ] Private farts never appear in feed
- [ ] Blocked user content never appears in feed
- [ ] Feed respects visibility tiers without client-side filtering (server returns only eligible content)
- [ ] Empty states are humorous and non-blank

**Discovery:**
- [ ] Search does not surface blocked users or suspended accounts
- [ ] No location data surfaced through any discovery path
- [ ] Search respects privacy opt-out setting (when implemented)

**Loops satisfied:**
- [ ] L2 Social Reaction Loop: reaction → notification → return → react again is fully closed
- [ ] L4 Social Connection Loop: discover person → request → approval → follower-content visible is fully closed
- [ ] L8 Viral Share: every social surface must have at least one share CTA available (see SHARE-01–04)

# Data Model: Fart Map (Local Persistence Contract)

> **Planning spine position:** authoritative local data model contract for Room.
> Upstream: `architecture/ArchitecturePrinciples.md`, `architecture/Architecture.md`, `architecture/SecurityAndSecrets.md`, `product/Features.md` (CREATE-03.3), `product/SocialSystems.md`, `product/ProgressionSystems.md`, ADR-0001/0007/0009/0010/0012/0014.
> Downstream: repository implementations, sync strategy internals, local migrations, tests.
> This document defines what local persistence stores and how local entities are structured. It does not define Firestore schema (see `DataContracts.md`).

---

## 1) Purpose

This document defines the local persistence contract for Fart Map.

It exists to guarantee:

- faithful field round-tripping across layers,
- deterministic offline-first behavior,
- strict alignment with product visibility/privacy rules,
- consistent Room modeling for all agents.

PoC failure reference: visibility was dropped during persistence and defaulted incorrectly. This contract prevents silent field loss.

---

## 2) Local Persistence Scope

Local storage is used for:

1. authoritative app read state (offline-first),
2. pending write queue for eventual sync,
3. user session-adjacent preferences (via DataStore where appropriate),
4. denormalized read models for responsive UI.

### Storage technologies

- **Room**: entity tables and relational-ish local read models.
- **DataStore**: lightweight user preferences and last-used UI settings.

---

## 3) Core Modeling Rules

1. **Domain-to-entity mapping lives in data layer only.**
2. **Every domain-required field must have a persisted representation.**
3. **Visibility defaults must be restrictive** (`FOLLOWERS` for first-time user behavior and persisted preference defaults).
4. **No sensitive fallback defaults that expand exposure** (never default to `PUBLIC` on parse failure).
5. **All entities include sync metadata where remote-backed.**
6. **Soft delete over hard delete when moderation/audit relevance exists.**
7. **No secrets/tokens in Room tables.**

---

## 4) Canonical Local Entities (MVP + Phase-Ready)

### 4.1 `FartLocalEntity`

Primary local representation of user-generated fart posts.

| Field | Type | Required | Notes |
|---|---|---|---|
| `id` | String | Yes | Primary key (UUID/string) |
| `author_user_id` | String | Yes | Owner uid |
| `public_lat` | Double | Yes | Fuzzed public coordinate (ADR-0019) |
| `public_lng` | Double | Yes | Fuzzed public coordinate (ADR-0019) |
| `private_lat` | Double? | No | Precise coordinate; populated only for the current user's own farts |
| `private_lng` | Double? | No | Precise coordinate; populated only for the current user's own farts |
| `coarse_location_key` | String | Yes | Public-safe location bucket key |
| `timestamp_epoch_ms` | Long | Yes | Event timestamp |
| `avatar_id` | String | Yes | Selected avatar |
| `flavor_level` | Int | Yes | 1–5 |
| `intensity_value` | Float | Yes | 0.0–1.0 normalized |
| `description_text` | String? | No | Optional text |
| `sound_id` | String? | No | Optional sound asset |
| `voice_url` | String? | No | Optional audio URL (ADR-0014) |
| `visibility_mode` | String enum | Yes | PUBLIC / FOLLOWERS / PRIVATE |
| `sync_state` | String enum | Yes | PENDING / SYNCED / FAILED / DELETED |
| `remote_updated_at` | Long? | No | Last known remote timestamp |
| `local_updated_at` | Long | Yes | Local mutation timestamp |
| `is_removed` | Boolean | Yes | Soft-remove marker |
| `is_hidden` | Boolean | Yes | Moderation auto-hide flag mirrored from backend (hidden after 3 reports) |
| `removed_reason` | String? | No | moderation / user_delete / policy |

Indexes:

- `idx_fart_author_time (author_user_id, timestamp_epoch_ms DESC)`
- `idx_fart_visibility_time (visibility_mode, timestamp_epoch_ms DESC)`
- `idx_fart_sync_state (sync_state)`
- `idx_fart_coarse_location_time (coarse_location_key, timestamp_epoch_ms DESC)`

---

### 4.2 `ReactionLocalEntity`

| Field | Type | Required | Notes |
|---|---|---|---|
| `fart_id` | String | Yes | Composite key part |
| `user_id` | String | Yes | Composite key part |
| `type` | String enum | Yes | Reaction type |
| `created_at` | Long | Yes | Epoch ms |
| `sync_state` | String enum | Yes | PENDING / SYNCED / FAILED / DELETED |
| `local_updated_at` | Long | Yes | Last local mutation |

Primary key: `(fart_id, user_id)`

---

### 4.3 `CommentLocalEntity`

| Field | Type | Required | Notes |
|---|---|---|---|
| `comment_id` | String | Yes | Primary key |
| `fart_id` | String | Yes | Parent |
| `author_id` | String | Yes | Comment author |
| `body` | String | Yes | Text body |
| `mentions_json` | String? | No | Serialized mention list |
| `created_at` | Long | Yes | Epoch ms |
| `deleted_at` | Long? | No | Soft delete timestamp |
| `sync_state` | String enum | Yes | PENDING / SYNCED / FAILED / DELETED |
| `local_updated_at` | Long | Yes | Last local mutation |

Indexes:

- `idx_comment_fart_time (fart_id, created_at ASC)`
- `idx_comment_sync_state (sync_state)`

---

### 4.4 `FollowRelationshipLocalEntity`

| Field | Type | Required | Notes |
|---|---|---|---|
| `from_user_id` | String | Yes | Requester/follower |
| `to_user_id` | String | Yes | Target/followee |
| `status` | String enum | Yes | PENDING / APPROVED / REJECTED / REVOKED / BLOCKED |
| `requested_at` | Long | Yes | Epoch ms |
| `resolved_at` | Long? | No | Epoch ms |
| `sync_state` | String enum | Yes | PENDING / SYNCED / FAILED / DELETED |
| `local_updated_at` | Long | Yes | Last local mutation |

Primary key: `(from_user_id, to_user_id)`

---

### 4.5 `BlockLocalEntity`

Dedicated block model to support block-without-prior-follow case.

| Field | Type | Required | Notes |
|---|---|---|---|
| `blocker_id` | String | Yes | Actor |
| `blocked_id` | String | Yes | Target |
| `created_at` | Long | Yes | Epoch ms |
| `sync_state` | String enum | Yes | PENDING / SYNCED / FAILED / DELETED |
| `local_updated_at` | Long | Yes | Last local mutation |

Primary key: `(blocker_id, blocked_id)`

---

### 4.6 `UserProfileLocalEntity`

| Field | Type | Required | Notes |
|---|---|---|---|
| `user_id` | String | Yes | Primary key |
| `display_name` | String | Yes | Public name |
| `avatar_id` | String | Yes | Selected avatar |
| `bio` | String? | No | Optional |
| `signature_style_id` | String? | No | ADR-0013 single style |
| `follower_count` | Int | Yes | Denormalized count |
| `following_count` | Int | Yes | Denormalized count |
| `is_following_list_public` | Boolean | Yes | ADR-0012 toggle |
| `is_account_suspended` | Boolean | Yes | Moderation flag |
| `last_synced_at` | Long? | No | cache staleness marker |
| `local_updated_at` | Long | Yes | local mutation |

---

### 4.7 `UserProgressLocalEntity`

| Field | Type | Required | Notes |
|---|---|---|---|
| `user_id` | String | Yes | Primary key |
| `total_xp` | Int | Yes | Aggregate |
| `current_level` | Int | Yes | Derived state |
| `xp_to_next` | Int | Yes | Derived state |
| `xp_today` | Int | Yes | Daily cap tracking |
| `current_streak_days` | Int | Yes | Streak value |
| `longest_streak_days` | Int | Yes | Personal best |
| `last_streak_action_at` | Long? | No | Streak maintenance timestamp |
| `trust_score` | Float | Yes | anti-abuse weighting |
| `local_updated_at` | Long | Yes | local mutation |

---

### 4.8 `AchievementLocalEntity`

| Field | Type | Required | Notes |
|---|---|---|---|
| `achievement_id` | String | Yes | Primary key |
| `title` | String | Yes | Display |
| `description` | String | Yes | Display |
| `category` | String enum | Yes | CONSISTENCY / EXPLORATION / HUMOR / SOCIAL / COMMUNITY / MILESTONE / CHALLENGE |
| `xp_reward` | Int | Yes | Progression reward |
| `badge_asset_id` | String | Yes | Asset reference |
| `is_secret` | Boolean | Yes | Secret achievement flag |

### 4.9 `UserAchievementLocalEntity`

| Field | Type | Required | Notes |
|---|---|---|---|
| `user_id` | String | Yes | Composite key part |
| `achievement_id` | String | Yes | Composite key part |
| `unlocked_at` | Long | Yes | Epoch ms |

Primary key: `(user_id, achievement_id)`

---

### 4.10 `ChallengeLocalEntity` and `UserChallengeProgressLocalEntity`

`ChallengeLocalEntity`:

| Field | Type | Required |
|---|---|---|
| `challenge_id` | String | Yes |
| `title` | String | Yes |
| `type` | String enum | Yes |
| `goal` | Int | Yes |
| `start_at` | Long | Yes |
| `end_at` | Long | Yes |
| `xp_reward` | Int | Yes |
| `badge_id` | String? | No |

`UserChallengeProgressLocalEntity`:

| Field | Type | Required |
|---|---|---|
| `user_id` | String | Yes |
| `challenge_id` | String | Yes |
| `progress_value` | Int | Yes |
| `is_completed` | Boolean | Yes |
| `completed_at` | Long? | No |

Primary key: `(user_id, challenge_id)`

---

### 4.11 `LeaderboardEntryLocalEntity`

Precomputed leaderboard row from backend.

| Field | Type | Required |
|---|---|---|
| `scope` | String enum | Yes | GLOBAL / LOCAL / FOLLOWERS |
| `period` | String enum | Yes | DAILY / WEEKLY / ALL_TIME |
| `user_id` | String | Yes |
| `rank` | Int | Yes |
| `score` | Float | Yes |
| `updated_at` | Long | Yes |

Primary key: `(scope, period, user_id)`

---

### 4.12 `PendingOperationLocalEntity`

Queue item for offline writes.

| Field | Type | Required | Notes |
|---|---|---|---|
| `operation_id` | String | Yes | Primary key |
| `entity_type` | String enum | Yes | FART / REACTION / COMMENT / FOLLOW / BLOCK / PROFILE / PROGRESS |
| `entity_id` | String | Yes | target id/composite key hash |
| `operation_type` | String enum | Yes | CREATE / UPDATE / DELETE |
| `payload_json` | String | Yes | Serialized operation payload |
| `attempt_count` | Int | Yes | retry counter |
| `next_retry_at` | Long | Yes | scheduled retry |
| `last_error_code` | String? | No | error classification |
| `created_at` | Long | Yes | queue insertion time |
| `updated_at` | Long | Yes | last update time |

Indexes:

- `idx_pending_next_retry (next_retry_at ASC)`
- `idx_pending_entity (entity_type, entity_id)`

---

### 4.13 `ModerationReportLocalEntity`

Local queue/outbox copy only. Canonical report data is backend-restricted.

| Field | Type | Required | Notes |
|---|---|---|---|
| `report_id` | String | Yes | Primary key |
| `reporter_user_id` | String | Yes | actor |
| `target_type` | String enum | Yes | FART / COMMENT / PROFILE / USER |
| `target_id` | String | Yes | target |
| `reason_code` | String | Yes | standardized reason |
| `created_at` | Long | Yes | epoch |
| `sync_state` | String enum | Yes | PENDING / SYNCED / FAILED |

---

### 4.14 `CosmeticItemLocalEntity`

Cache of the remote cosmetic catalog (`DataContracts.md` §3.13).

| Field | Type | Required | Notes |
|---|---|---|---|
| `item_id` | String | Yes | Primary key |
| `type` | String enum | Yes | AVATAR / SOUND |
| `name` | String | Yes | Display name (humor copy) |
| `asset_ref` | String | Yes | Bundled asset id or Storage path |
| `is_free` | Boolean | Yes | Free-tier guarantee (`Monetization.md` §3) |
| `store_product_id_android` | String? | No | Play product id |
| `store_product_id_ios` | String? | No | App Store product id |
| `sort_order` | Int | Yes | Catalog order |
| `is_active` | Boolean | Yes | Hidden when false |
| `updated_at` | Long | Yes | Remote timestamp |

### 4.15 `UserEntitlementLocalEntity`

Read-only cache of `user_entitlements/{userId}` (ADR-0016). Never the source of truth.

| Field | Type | Required | Notes |
|---|---|---|---|
| `item_id` | String | Yes | Composite key part |
| `user_id` | String | Yes | Composite key part |
| `source` | String enum | Yes | PURCHASE / REWARDED_AD / EARNED / SUBSCRIPTION |
| `granted_at` | Long | Yes | Epoch ms |
| `expires_at` | Long? | No | Subscription-bound items only |
| `synced_at` | Long | Yes | Cache staleness marker |

Primary key: `(user_id, item_id)`

### 4.16 `RewardedUnlockLocalEntity`

Cache of the user's unconsumed server-issued unlock tokens (ADR-0017).

| Field | Type | Required | Notes |
|---|---|---|---|
| `token_id` | String | Yes | Primary key (server-issued) |
| `item_id` | String | Yes | Unlocked item |
| `issued_at` | Long | Yes | Epoch ms |
| `expires_at` | Long | Yes | Token validity window |
| `is_consumed` | Boolean | Yes | Mirrored from backend |

## 5) DataStore (Preference) Keys

Use DataStore for non-relational user preferences and last-used UI state.

Minimum keys:

- `last_visibility_mode` (default `FOLLOWERS`)
- `play_marker_sounds_enabled`
- `map_filter_last_selected`
- `onboarding_completed`
- `show_following_list_public` (mirror of profile flag for UX)
- `notification_permission_prompted` (in-context prompt shown once)
- `account_link_nudge_shown_at` (ADR-0018 nudge throttle)
- `ads_consent_status` (UMP consent outcome mirror, ADR-0017)

Never store secrets or auth tokens in DataStore.

---

## 6) Enum Contracts

Room type converters must support explicit enum value mapping with safe fallback rules.

### Mandatory enums

- `VisibilityMode`: PUBLIC / FOLLOWERS / PRIVATE
- `SyncState`: PENDING / SYNCED / FAILED / DELETED
- `FollowStatus`: PENDING / APPROVED / REJECTED / REVOKED / BLOCKED
- `LeaderboardScope`: GLOBAL / LOCAL / FOLLOWERS
- `LeaderboardPeriod`: DAILY / WEEKLY / ALL_TIME
- `CosmeticType`: AVATAR / SOUND
- `EntitlementSource`: PURCHASE / REWARDED_AD / EARNED / SUBSCRIPTION
- `ReactionType`: DEAD / TOXIC / RIP / SMELLED / LEGEND

Fallback policy:

- Unknown persisted enum value must map to restrictive/safe behavior and log a structured parse warning.
- Unknown `VisibilityMode` fallback is `PRIVATE` in read path with alerting; write path must reject unknown values.

---

## 7) Local Privacy and Retention Rules

1. Precise coordinates (`private_lat`/`private_lng`) are stored only for the current user's own farts; other users' farts carry fuzzed public coordinates only (ADR-0019).
2. Cached follower-only/private content must be pruned or access-restricted when permission relationships change.
3. Session switch/sign-out must clear user-scoped caches or partition by user keyspace.
4. Moderation-removed content should be marked hidden locally and excluded from standard queries.

---

## 8) Query Model Requirements

Local queries must support:

- map feed by coarse location + visibility eligibility,
- author timeline,
- fart detail with aggregated reaction/comment counts,
- social graph status checks,
- notification source projections,
- leaderboard scope/period reads,
- pending operation dequeue in retry order.

No client-side full-collection scans for primary surfaces.

---

## 9) Migration Strategy Rules

1. Every schema change requires explicit Room migration plan.
2. Destructive migration is prohibited in production paths.
3. Migration tests are required for all non-trivial changes.
4. Privacy-sensitive fields cannot be silently dropped during migration.
5. Enum expansion must be forward-compatible with unknown value handling.

---

## 10) Testing Contract for Data Model

Minimum required tests:

1. **Field round-trip tests** for `FartLocalEntity`, including `visibility_mode`.
2. **Enum converter tests** for all enums + unknown value handling.
3. **Index/query correctness tests** for main read models.
4. **Pending operation ordering tests** (retry scheduling).
5. **Permission-change cache behavior tests** (block/unfollow/visibility changes).
6. **Migration tests** for each schema version increment.
7. **Entitlement cache tests**: a non-free item is never rendered as owned without a cached entitlement or unlock token, and the cache is cleared on sign-out.

---

## 11) Explicit Non-Goals

This document does not define:

- Firestore collection structure (see `DataContracts.md`),
- sync retry algorithm details (see `SyncStrategy.md`),
- analytics event schema (see `Observability.md`),
- monetization receipt validation flow details (see `FirebaseStrategy.md` + future purchase contract docs).

---

*This is the local persistence contract. Any local model that diverges from this document without ADR/update is a defect.*

# Data Contracts: Fart Map (Remote/Firestore Contract)

> **Planning spine position:** authoritative remote data contract and security-rule intent.
> Upstream: `architecture/Architecture.md`, `architecture/Security.md`, `architecture/LocalData.md`, `product/features/`, `product/SocialSystems.md`, `product/ProgressionSystems.md`, ADR-0002/0007/0009/0010/0012/0014.
> Downstream: Firestore schema implementation, security rules, repository remote mappers, sync policy implementation.
> This document defines canonical remote entities and rule intent. Documents defined here are also the local shape: Firestore's offline cache stores them as-is (ADR-0022, `LocalData.md`).

---

## 1) Purpose

This document defines the remote data model and access control contract for backend-backed features.

It ensures:

- consistent client-backend data shape,
- enforceable visibility/privacy boundaries,
- backend authority over sensitive decisions,
- reliable local-first synchronization mapping.

---

## 2) Contract Principles

1. **Backend is authoritative for access control.**
2. **Remote schema must round-trip all required product fields.**
3. **Visibility rules are explicit and enforceable (ADR-0010).**
4. **Follow approval gates follower-only content (ADR-0009).**
5. **Following-list privacy defaults private (ADR-0012).**
6. **No unlock-expiry time field for rewarded ads in MVP policy context (single-use action model).**
7. **Remote models are stable contracts; additive evolution only unless migration strategy approved.**

---

## 3) Canonical Collections and Documents

Collection names are canonical for planning; implementation may add namespace prefixes by environment.

## 3.1 `users/{userId}` (public profile, ADR-0025)

| Field | Type | Required | Notes |
|---|---|---|---|
| `user_id` | String | Yes | Must equal path `userId` |
| `display_name` | String | Yes | Public profile |
| `avatar_id` | String | Yes | Profile avatar |
| `bio` | String? | No | Optional |
| `signature_style_id` | String? | No | ADR-0013 single style |
| `follower_count` | Int | Yes | Denormalized, server-written |
| `following_count` | Int | Yes | Denormalized, server-written |
| `is_following_list_public` | Boolean | Yes | ADR-0012 flag; default false |
| `created_at` | Timestamp | Yes | Server timestamp |
| `updated_at` | Timestamp | Yes | Server timestamp |

Readable by any signed-in user. The owner creates it and edits only `display_name`, `avatar_id`, `bio`, `signature_style_id`, `is_following_list_public`.

**Creation (STORY-01-02-02):** the app creates the doc once the session is Active and the server confirms the doc is missing. It writes:
- a generated display name, `<Adjective> <Noun> <10–99>`;
- a free starter avatar.

It does not set `bio` or `signature_style_id`. The create is tried once per uid per app process.

### 3.1.1 `users/{userId}/private/account` (owner-only, ADR-0025)

| Field | Type | Required | Notes |
|---|---|---|---|
| `age_band` | String enum | Yes | AGE_13_17 / AGE_18_PLUS (under-13 is blocked at sign-up; `ContentSafety.md` §5). Set once by the owner, immutable |
| `linked_providers` | Array<String> | Yes | e.g. `google`, `apple`; empty for anonymous. Server-managed: `[]` on the client create, then derived from the Firebase Auth record by the `syncAccountProviders` callable (STORY-01-02-04) |
| `is_anonymous` | Boolean | Yes | ADR-0018; flips to false on account linking. Server-managed: set on the client create from the sign-in provider (rules check it), then re-derived by `syncAccountProviders`, which the client calls when a linked session's doc is stale |
| `is_suspended` | Boolean | Yes | Moderation enforcement. Server-managed |
| `created_at` | Timestamp | Yes | Server timestamp |
| `updated_at` | Timestamp | Yes | Server timestamp |

Readable by the owner only; never readable by other users (it reveals whether a user is a minor).

---

## 3.2 `farts/{fartId}`

| Field | Type | Required | Notes |
|---|---|---|---|
| `fart_id` | String | Yes | Must equal path `fartId` |
| `author_user_id` | String | Yes | Owner uid |
| `public_lat` | Double | Yes | Fuzzed coordinate (ADR-0019); never the true position |
| `public_lng` | Double | Yes | Fuzzed coordinate (ADR-0019) |
| `coarse_location_key` | String | Yes | Public-safe bucket: a geohash-6 cell (rules enforce the format) |
| `timestamp_epoch_ms` | Long | Yes | Event time |
| `avatar_id` | String | Yes | CREATE-03.3 |
| `flavor_level` | Int | Yes | 1..5 |
| `intensity_value` | Float | Yes | 0.0..1.0 |
| `description_text` | String? | No | Optional |
| `sound_id` | String? | No | Optional |
| `voice_url` | String? | No | Optional; ADR-0014 |
| `visibility_mode` | String enum | Yes | PUBLIC / FOLLOWERS / PRIVATE |
| `reaction_count` | Int | Yes | Denormalized |
| `reaction_breakdown` | Map<String, Int> | Yes | Denormalized by type |
| `comment_count` | Int | Yes | Denormalized |
| `report_count` | Int | Yes | Backend-written |
| `is_hidden` | Boolean | Yes | Backend-written; true after 3 distinct reports pending review |
| `is_removed` | Boolean | Yes | moderation/user delete |
| `removed_reason` | String? | No | audit reason |
| `created_at` | Timestamp | Yes | server set |
| `updated_at` | Timestamp | Yes | server set |

### 3.2.1 `farts/{fartId}/private/location`

| Field | Type | Required | Notes |
|---|---|---|---|
| `lat` | Double | Yes | Precise coordinate |
| `lng` | Double | Yes | Precise coordinate |
| `updated_at` | Timestamp | Yes | Server set |

Readable by the author only (ADR-0019). The author writes it exactly once, in the same batch that creates the fart. Rules require it there, and it can't be created later or updated (STORY-01-04-01; moving a fart is not in Phase 1). The `onFartCreated` trigger recomputes `public_lat`/`public_lng`/`coarse_location_key` from this document and overwrites the public fields on mismatch (`backend/functions/src/triggers/fartCreated.ts`; a fart that is already removed is skipped quietly). When the author soft-deletes the fart (`is_removed = true`, `removed_reason = USER_DELETED`), the `onFartRemoved` trigger deletes this document through the Admin SDK (`triggers/fartRemoved.ts`, STORY-01-04-05); clients never delete it.

**How the public fields are computed (ADR-0019 §3, STORY-01-03-01):**
- `coarse_location_key` = the geohash-6 cell of the precise point.
- `public_lat`/`public_lng` = the cell center plus `(u − 0.5) × 0.8 × cell size` on each axis. `u` = FNV-1a 32-bit of the UTF-8 bytes of `"<fartId>:lat"` (or `":lng"`) divided by 2³², so the point stays in the middle 80% of the cell and is stable per fart.
- If that point equals the precise one, the offset is mirrored (or, at offset 0, moved by +0.25 × cell size).
- Two implementations with identical results: `shared` `core/domain/location/LocationFuzzer` (client, at write time) and `backend/functions/src/lib/location.ts` (re-validation, admin scripts). Both test suites assert `backend/functions/test/fixtures/location-fuzz-vectors.json`. Changing the algorithm changes every stored public point, so it needs a superseding ADR.
- Dev test farts: `npm run seed:farts -- --project fart-map-dev` (dev and emulator only; `OwnerTodo.md` D).

---

## 3.3 `farts/{fartId}/reactions/{userId}`

| Field | Type | Required |
|---|---|---|
| `fart_id` | String | Yes |
| `user_id` | String | Yes |
| `type` | String enum | Yes — DEAD / TOXIC / RIP / SMELLED / LEGEND (`SocialSystems.md` §2) |
| `created_at` | Timestamp | Yes |

Document identity enforces one reaction per user per fart. `reaction_count` and `reaction_breakdown` on the parent are maintained by a Cloud Function trigger (ADR-0015). Read: the reacting user's own doc only (by document `get`, for the fart's readers); the reactor list is never readable, so reactor ids stay hidden.

---

## 3.4 `farts/{fartId}/comments/{commentId}`

| Field | Type | Required |
|---|---|---|
| `comment_id` | String | Yes |
| `fart_id` | String | Yes |
| `author_id` | String | Yes |
| `body` | String | Yes |
| `mentions` | Array<String> | No |
| `created_at` | Timestamp | Yes |
| `deleted_at` | Timestamp? | No |

---

## 3.5 `follow_relationships/{fromUserId_toUserId}`

| Field | Type | Required |
|---|---|---|
| `from_user_id` | String | Yes |
| `to_user_id` | String | Yes |
| `status` | String enum | Yes | PENDING / APPROVED / REJECTED / REVOKED / BLOCKED |
| `requested_at` | Timestamp | Yes |
| `resolved_at` | Timestamp? | No |

---

## 3.6 `blocks/{blockerId_blockedId}`

| Field | Type | Required |
|---|---|---|
| `blocker_id` | String | Yes |
| `blocked_id` | String | Yes |
| `created_at` | Timestamp | Yes |

---

## 3.7 `user_progress/{userId}`

| Field | Type | Required |
|---|---|---|
| `user_id` | String | Yes |
| `total_xp` | Int | Yes |
| `current_level` | Int | Yes |
| `xp_to_next` | Int | Yes |
| `xp_today` | Int | Yes |
| `current_streak_days` | Int | Yes |
| `longest_streak_days` | Int | Yes |
| `last_streak_action_at` | Timestamp? | No |
| `trust_score` | Float | Yes |
| `updated_at` | Timestamp | Yes |

---

## 3.8 `xp_events/{eventId}` (append-only)

| Field | Type | Required |
|---|---|---|
| `event_id` | String | Yes |
| `user_id` | String | Yes |
| `source` | String enum | Yes |
| `amount` | Int | Yes |
| `fart_id` | String? | No |
| `created_at` | Timestamp | Yes |

---

## 3.9 `achievements/{achievementId}` and `user_achievements/{userId_achievementId}`

`achievements` (static/reference):

| Field | Type | Required |
|---|---|---|
| `achievement_id` | String | Yes |
| `title` | String | Yes |
| `description` | String | Yes |
| `category` | String enum | Yes |
| `xp_reward` | Int | Yes |
| `badge_asset_id` | String | Yes |
| `is_secret` | Boolean | Yes |

`user_achievements`:

| Field | Type | Required |
|---|---|---|
| `user_id` | String | Yes |
| `achievement_id` | String | Yes |
| `unlocked_at` | Timestamp | Yes |

---

## 3.10 `challenges/{challengeId}` and `user_challenge_progress/{userId_challengeId}`

`challenges`:

| Field | Type | Required |
|---|---|---|
| `challenge_id` | String | Yes |
| `title` | String | Yes |
| `type` | String enum | Yes |
| `goal` | Int | Yes |
| `start_at` | Timestamp | Yes |
| `end_at` | Timestamp | Yes |
| `xp_reward` | Int | Yes |
| `badge_id` | String? | No |

`user_challenge_progress`:

| Field | Type | Required |
|---|---|---|
| `user_id` | String | Yes |
| `challenge_id` | String | Yes |
| `progress_value` | Int | Yes |
| `is_completed` | Boolean | Yes |
| `completed_at` | Timestamp? | No |

---

## 3.11 `leaderboard_entries/{scope_period_userId}`

Precomputed read model written by backend jobs/functions.

| Field | Type | Required |
|---|---|---|
| `scope` | String enum | Yes | GLOBAL / LOCAL / FOLLOWERS |
| `period` | String enum | Yes | DAILY / WEEKLY / ALL_TIME |
| `user_id` | String | Yes |
| `rank` | Int | Yes |
| `score` | Float | Yes |
| `updated_at` | Timestamp | Yes |

Client must not compute ranks.

---

## 3.12 `moderation_reports/{reportId}`

Restricted moderation collection. The document id is `{reporter_user_id}_{target_type}_{target_id}`, so one account files at most one report per target (rules enforce it); auto-hide counts distinct reporters.

| Field | Type | Required |
|---|---|---|
| `report_id` | String | Yes |
| `reporter_user_id` | String | Yes |
| `target_type` | String enum | Yes | FART / COMMENT / USER (Phase 1 clients file FART only; profiles join with EPIC-01-07) |
| `target_id` | String | Yes |
| `reason_code` | String | Yes | HARASSMENT / PRIVATE_INFO / MINOR_SAFETY / SEXUAL / ILLEGAL / SPAM / OTHER (`ReportReason`, STORY-01-05-03). Review MINOR_SAFETY first (`ContentSafety.md` §5) |
| `status` | String enum | Yes | OPEN / UNDER_REVIEW / ACTIONED / DISMISSED |
| `created_at` | Timestamp | Yes |
| `updated_at` | Timestamp | Yes |

**Auto-hide (`onReportCreated`, ADR-0015):** each created FART report increments the fart's `report_count`; at 3 the fart gets `is_hidden = true` and leaves public reads (its author still sees it). Reports by the fart's author, and reports on removed or missing farts, aren't counted. Only moderation (Admin SDK) un-hides, and then also resets `report_count` to 0, or the next report hides it again.

---

## 3.13 `cosmetic_items/{itemId}` (catalog, backend-managed)

| Field | Type | Required | Notes |
|---|---|---|---|
| `item_id` | String | Yes | Must equal path |
| `type` | String enum | Yes | AVATAR / SOUND |
| `name` | String | Yes | Humor copy |
| `asset_ref` | String | Yes | Bundled asset id or Storage path |
| `is_free` | Boolean | Yes | Free-tier guarantee |
| `store_product_id_android` | String? | No | Play product id |
| `store_product_id_ios` | String? | No | App Store product id |
| `rewarded_unlockable` | Boolean | Yes | Avatars and sounds only (`Monetization.md` §5) |
| `sort_order` | Int | Yes | |
| `is_active` | Boolean | Yes | |
| `updated_at` | Timestamp | Yes | |

Readable by all authenticated users; writable by backend/admin only.

**Free starter items:**
- They are defined once, in `backend/catalog/starter-catalog.json`.
- Seed them into dev or an emulator with `npm run seed:catalog -- --project <id>` in `backend/functions`. This uses Admin SDK + ADC, and the script only accepts `fart-map-dev` or `demo-*` emulator projects and free items.
- The app's starter avatar list must match the file; `StarterCatalogParityTest` checks this.
- The prod catalog ships with EPIC-01-06.

## 3.14 `user_entitlements/{userId}` (backend-written, ADR-0016)

| Field | Type | Required | Notes |
|---|---|---|---|
| `user_id` | String | Yes | Must equal path |
| `items` | Map<String, { `source`: PURCHASE / REWARDED_AD / EARNED / SUBSCRIPTION, `granted_at`: Timestamp, `expires_at`: Timestamp? }> | Yes | Keyed by `item_id` |
| `premium_active` | Boolean | Yes | False until MON-02 ships |
| `updated_at` | Timestamp | Yes | |

Written only by the Cloud Function that consumes RevenueCat webhooks. Readable by the owner.

## 3.15 `rewarded_unlocks/{userId}/tokens/{tokenId}` (backend-written, ADR-0017)

| Field | Type | Required | Notes |
|---|---|---|---|
| `token_id` | String | Yes | Issued by the SSV callback function |
| `item_id` | String | Yes | |
| `issued_at` | Timestamp | Yes | |
| `expires_at` | Timestamp | Yes | Short validity window for *redeeming* the token (anti-hoarding). It is not an unlock duration: a redeemed token unlocks exactly one fart and the item re-locks (the "no `unlock_expiry`" rule) |
| `consumed_by_fart_id` | String? | No | Set when consumed |

Readable by the owner; never client-written. The daily cap (3) is enforced in the issuing function.

## 3.16 `users/{userId}/devices/{deviceId}`

| Field | Type | Required | Notes |
|---|---|---|---|
| `fcm_token` | String | Yes | Sensitive identifier |
| `platform` | String enum | Yes | ANDROID / IOS |
| `updated_at` | Timestamp | Yes | |

Owner read/write only; removed on sign-out.

## 4) Contract-Level Validation Rules

1. `fart_id`, `author_user_id`, and path IDs must match identity context.
2. `visibility_mode` must be one of PUBLIC/FOLLOWERS/PRIVATE.
3. `flavor_level` range 1..5.
4. `intensity_value` range 0.0..1.0.
5. Voice recording URLs must reference allowed media storage path and validated media constraints.
6. Reaction doc identity enforces one reaction per user per fart.
7. Follow transitions must respect lifecycle and block state constraints.
8. XP and leaderboard writes are server-authoritative (not client-calculated final source).
9. A fart using a non-free `avatar_id` or `sound_id` is accepted only if `user_entitlements` grants the item or an unconsumed `rewarded_unlocks` token names it; the token is consumed by the write trigger (ADR-0016, ADR-0017).
10. `public_lat`/`public_lng` must lie inside the `coarse_location_key` cell; a function re-validates from the private location (ADR-0019).
11. `description_text` is rejected above 500 characters (`features/` CREATE-03.1 Step 6). The rules count UTF-16 code units, so an emoji counts 2. The client validates with the same unit (STORY-01-04-01).
12. `visibility_mode == PUBLIC` is accepted only when the author's `users/{uid}/private/account.age_band == AGE_18_PLUS` (ADR-0023 amendment); a missing account doc means no PUBLIC.

---

## 5) Firestore Security Rule Intent (Authoritative Behavior)

This section defines required behavior; exact rule code lives in implementation.

### 5.1 Authentication baseline

- All write operations require authenticated user context; anonymous Firebase sessions count as authenticated (ADR-0018). App Check is enforced on every client request.
- Reads of non-public user-specific data require authenticated user context and eligibility checks.

### 5.2 Fart visibility access

`canReadFart(requester, fart)` behavior:

1. If `fart.visibility_mode == PUBLIC`: allow read (subject to moderation removal flags and suspension policies).
2. If `fart.visibility_mode == FOLLOWERS`: allow only if approved follow relation exists from requester -> author and no block relation either direction.
3. If `fart.visibility_mode == PRIVATE`: allow only author.

**Writing PUBLIC:** only authors in the 18+ band (from `users/{uid}/private/account`) may create a PUBLIC fart or switch one to PUBLIC (ADR-0023 amendment, 2026-11-23).

**Queries (implemented in STORY-01-01-04):** rules can only authorize a query from its constraints, so list reads are allowed for exactly two shapes: `visibility_mode == PUBLIC && is_hidden == false && is_removed == false` (the map, plus the geohash range of EPIC-01-03) and `author_user_id == auth.uid` (own profile). Hidden or removed farts are visible to their author only. FOLLOWERS content is fetched per author once follows exist (SOCIAL-03, Phase 2). PUBLIC reads are not block-filtered by rules; the client hides blocked authors.

### 5.3 Follow and block constraints

- User can create follow request only as self (`from_user_id == auth.uid`).
- User can approve/reject only requests targeting self (`to_user_id == auth.uid`).
- Block relations can only be created/deleted by blocker identity.
- If block exists either direction, follower-only access denied regardless of previous approval state.

### 5.4 Comments/reactions constraints

- Reactions/comments require read eligibility for target fart.
- Comment author identity must equal auth uid.
- Soft-delete actions allowed for comment author or fart author, not arbitrary users.

### 5.5 User profile and following-list privacy

- `users/{userId}` (public profile fields only) is readable by any signed-in user; `users/{userId}/private/account` (age band, providers, anonymity, suspension) only by its owner (ADR-0025).
- Following list identities readable only when owner opted in (`is_following_list_public == true`) or by owner.
- Follower count may remain public.

### 5.6 Moderation and report isolation

- Report creation allowed for authenticated users.
- Report read/update restricted to moderation/admin path only.

---

### 5.7 Entitlements, unlocks and catalog

- `user_entitlements` and `rewarded_unlocks` are written by Cloud Functions only; the owner may read their own documents.
- `cosmetic_items` is readable by authenticated users and written by backend/admin only.
- Fart writes that use non-free items are validated by rules (entitlement read) and finalized by the write trigger (token consumption).

### 5.8 Private location

- `farts/{fartId}/private/location` is readable by the fart author only. The author creates it once, in the batch that creates the fart; a fart without it is denied. It has no update and no client delete. This narrows ADR-0019 §2 and doesn't reverse it.

### 5.9 Devices and hidden content

- `users/{userId}/devices/*` is owner-only.
- Farts with `is_hidden == true` are excluded from public reads except for the author and moderation paths.

## 6) Data Projection Rules (Privacy)

1. Public-facing reads must not include precise coordinates where not required.
2. Public/share surfaces use `coarse_location_key` and derived display values.
3. Private exact coordinates live only in `farts/{fartId}/private/location` (ADR-0019); public documents never carry them.
4. Analytics or notification payloads must not include precise coordinates.

---

## 7) Sync Contract Coupling Rules

1. Client DTOs in `firebaseData` map these documents 1:1; every field round-trips (ADR-0022: there is no separate local model).
2. Pending-write state comes from snapshot metadata (`hasPendingWrites`) and is never persisted as a document field.
3. Server timestamps are authoritative for conflict ordering where applicable.
4. Deleted/removed states must converge across local and remote with explicit reason semantics.

---

## 8) Versioning and Evolution Rules

1. Additive field evolution preferred.
2. Breaking contract changes require migration plan + ADR if cross-feature impact exists.
3. New enum values require forward-compatible parser behavior.
4. Rule-intent changes (access behavior changes) require explicit planning update and potential user approval depending on privacy/scope impact.

---

## 9) Test Contract for Remote Data Layer

Minimum required validation suite:

1. Visibility read rule tests for PUBLIC/FOLLOWERS/PRIVATE.
2. Follow approval and block precedence rule tests.
3. Comment/reaction authorization tests against hidden content.
4. Following-list privacy tests (ADR-0012 behavior).
5. Progression integrity tests (client cannot directly write protected ranking outcomes).
6. Report isolation tests (reporter cannot read others' reports).
7. Projection tests ensuring precise location not leaked in public read models.
8. Entitlement tests: a non-free cosmetic without entitlement or token is rejected; a token is single use.
9. Private-location tests: non-authors cannot read `farts/{fartId}/private/location`; a fart without it in the creating batch is denied; it can't be created later or updated.
10. Anonymous-session tests: anonymous uid can create and react; linking preserves ownership.

---

## 10) Explicit Non-Goals

This document does not define:

- local cache configuration and write pattern (`LocalData.md`),
- SDK initialization and environment wiring (`Backend.md`),
- analytics taxonomy (`Observability.md`).

---

*This is the remote data and access-control contract. Any backend implementation that diverges from this document without approved update is out of spec.*

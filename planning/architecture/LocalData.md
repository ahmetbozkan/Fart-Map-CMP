# Local Data: Fart Map

> Authority: ADR-0022 (accepted 2026-11-22). It replaces `DataModel.md` and `SyncStrategy.md`, which are archived in `planning/archive/architecture/`.
> Upstream: `Architecture.md` §8 (offline invariants), `DataContracts.md` (the remote schema, which is also the local shape). Downstream: `firebaseData` repository implementations, the app-scoped write launcher, tests.

## 1) Model

- **Firestore's persistent cache is the local layer** for Phases 1–2, on Android and iOS, through GitLive. There is no Room database, no PendingOperation table and no SyncEngine.
- **Documents are the local model.** Firestore documents (`DataContracts.md` §3) are read and written as `@Serializable` DTOs in `firebaseData` and mapped to domain types. There is no separate local entity layer.
- **DataStore** holds preferences and drafts (§5).
- **The rest of the app sees interfaces only.** Repository interfaces in `shared` hide all of this, so swapping one dataset to Room later (§8) touches only `firebaseData`.

## 2) Cache configuration

- Persistence is enabled with a cache size of **at least 200 MB** via GitLive `firestoreSettings` (or unlimited if measurement shows eviction of the user's own content). It is set once, at startup, before the first Firestore call: `FirestoreSetup.configure()` in `firebaseData`, called by `FartMapApplication` and umbrella `startApp`.
- **Repositories never hold a `FirebaseFirestore`.** They take `FirestoreProvider` and call `get()` per use: GitLive's `Firebase.firestore` returns the platform's current instance, and after `terminate()` (§7) that is a new instance.
- **Reads come from snapshot listeners** (`snapshots`). They emit from cache first, then from the server. A one-shot read uses the default source; `Source.CACHE` is used only for explicit offline-only paths.
- **No full-collection listeners.** The map listens to geohash-prefix range queries for the viewport (public geohash field in `DataContracts.md` §3.2; composite indexes in `backend/firestore.indexes.json`). The profile listens by `owner_id`.

## 3) Write pattern (the single remote write path)

1. **Generate the document id on the client** (`collection.document`, a random id). Retries and re-delivery stay idempotent. Reactions and follows use the deterministic composite ids from `DataContracts.md`.
2. **One batch per user action.** For a fart, the batch holds the public doc with fuzzed geohash-6 coordinates plus `farts/{id}/private/location` with the precise coordinates (ADR-0019).
3. **Launch the commit in the app-scoped `AppWriteScope`, never awaited on the UI path.** GitLive `set()`/`commit()` suspend until the server acknowledges, so awaiting them offline would hang. The repository returns `AppResult.Success` once the write is handed off. The cache then shows the doc immediately with `metadata.hasPendingWrites = true`.
4. **`PendingWriteMonitor`** observes every launched write (contracts in `core/domain/write`; `DefaultAppWriteScope` in `shared` implements both, with the Firestore error mapping from `firebaseData`):
   - **acknowledged:** emit a `write_acked` latency bucket;
   - **rejected** (permission denied, invalid argument): emit a typed `WriteRejected(op, AppError)` event that ViewModels and the UI can surface ("We couldn't post that"), plus a structured log. Never retry a rejection.
   - Transient failures are retried by the Firestore SDK itself.
5. **Limitation (accepted):** if the process dies before a queued write syncs, the SDK still delivers it on the next start, but any rejection after that restart is not reported to the user. Rules validation errors should therefore be caught by client-side validation first.
6. **Online-only operations are never routed through Firestore offline writes.** Purchases, rewarded-ad rewards, account linking and callable functions run online-only. They fail fast with a typed "needs connection" error.

## 4) Pending state in the UI

- A doc with `hasPendingWrites` is "posting". The marker shows immediately, with the subtle pending style from the screen spec.
- There is no user-facing "offline error" for transient conditions (`Architecture.md` §8).

## 5) DataStore keys

| Key | Default / notes |
|---|---|
| `first_open_done` | Device-scoped; gives `is_first_open` on `app_open` (Observability.md §11) |
| `last_visibility_mode` | Unset → the Phase 1 default **PUBLIC** for the 18+ age band (ADR-0023); the 13–17 band and an unknown band get FOLLOWERS and are never offered PUBLIC (enforced by rules, ADR-0023 amendment). Once the user picks a value, the last-used value wins if it is still allowed |
| `log_draft` | Serialized Log Fart draft (JSON); survives process death; cleared on successful hand-off (CREATE-03 `DRAFT`). User-scoped. Holds the owner uid, the cosmetic picks, flavor, intensity, caption and visibility, and the time only if the user picked it. **Never coordinates.** The caption can reach the user's own device backup (STORY-01-04-04) |
| `play_marker_sounds_enabled` | true |
| `map_filter_last_selected` | EVERYONE (MINE / FOLLOWERS / EVERYONE; unknown → EVERYONE). Device-scoped: kept by sign-out |
| `onboarding_completed` | false |
| `show_following_list_public` | Mirror of the profile flag for UX (ADR-0012) |
| `notification_permission_prompted` | false; the prompt shows once, after the first successful log (ADR-0023) |
| `account_link_nudge_shown_at` | ADR-0018 nudge throttle |
| `ads_consent_status` | UMP outcome mirror (ADR-0017) |
| `starter_pack_offer_shown` | false; the offer is shown once after the first successful log (ADR-0024) |

Never store secrets, auth tokens or precise coordinates of other users in DataStore.

## 6) Enums and decoding

- **Enum values:** `VisibilityMode` PUBLIC / FOLLOWERS / PRIVATE; `ReactionType` DEAD / TOXIC / RIP / SMELLED / LEGEND; `CosmeticType` AVATAR / SOUND; `EntitlementSource` PURCHASE / REWARDED_AD / EARNED / SUBSCRIPTION; plus the later-phase enums in `DataContracts.md`.
- **Unknown values on read** map to the safe value and log a parse warning: an unknown `VisibilityMode` becomes PRIVATE. Writes never send an unknown value.
- **Every DTO field has a default**, so missing fields decode instead of crashing. The mapper round-trips every field, and a mapper test covers each one (the `visibility_mode` regression test is mandatory).

## 7) Privacy, session and retention

- **Precise coordinates:** the user's own come from `private/location`, which only the owner can read (rules). Other users' farts carry fuzzed coordinates only.
- **Sign-out and account switch:** the `SignOut` use case:
  1. `SessionRepository.signOut()`; if it fails, nothing is wiped (the user is still signed in);
  2. `AppWriteScope.cancelPending()`, so the old account's writes neither race the wipe nor surface as the next user's rejections;
  3. `LocalDataReset`: `terminate()` → `clearPersistence()` → `FirestoreSetup.configure()` (settings are re-applied even after a failed wipe);
  4. clears the user-scoped DataStore keys (draft, nudges, offers, last visibility), always, even if the wipe failed. Device-scoped keys (`first_open_done`, `map_filter_last_selected`) are kept.

  **Rule for features:** Firestore listeners are scoped to the active session and stop when it leaves `Active`, so nothing touches Firestore during the reset. A failed wipe is returned as a typed failure for the caller (Settings, EPIC-01-07) to surface; there is no automatic retry yet. Pending unsynced writes from the old account are dropped. On sign-out of an anonymous account, warn the user first (ADR-0018).
- **Server-side changes:** content that loses visibility (block, unfollow, moderation hide, delete) disappears from listeners automatically. The server is authoritative and no tombstones are needed.
- **Entitlements and unlock tokens:** the listener mirrors of `user_entitlements/{uid}` and `rewarded_unlocks/{uid}/tokens`. They are read-only for the client and cleared with persistence on sign-out.

## 8) Triggers to add Room for a specific dataset (new ADR required)

1. Guaranteed offline retention beyond the cache is required.
2. A measured cache query is over 300 ms.
3. Listener re-read cost exceeds budget.
4. A Storage upload outbox is needed (voice clips, Phase 4). Storage has no offline queue; use WorkManager on Android and background `URLSession` on iOS with a small local table.

## 9) Tests (minimum)

1. Mapper round-trip for every DTO, including `visibility_mode` and unknown-enum decoding.
2. Repository with a fake Firestore gateway:
   - `add` returns success without awaiting the server;
   - the batch contains both the public doc and the private-location doc;
   - a rejection reaches `PendingWriteMonitor` as a typed event.
3. Visibility default resolution: unset → PUBLIC for the adult band and FOLLOWERS for 13–17 or unknown; the last-used value wins after that if allowed; PUBLIC is never offered below 18.
4. Sign-out clears persistence and the user-scoped keys.
5. Entitlement cache: a non-free item is never rendered as owned without a mirrored entitlement or token.

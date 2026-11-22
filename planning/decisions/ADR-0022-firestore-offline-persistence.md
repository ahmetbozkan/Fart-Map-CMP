# ADR-0022: Firestore Offline Persistence as the Local Data Layer (Phases 1–2)

- Status: Accepted (2026-11-22, owner)
- Date: 2026-11-21
- Supersedes: ADR-0001 (Room) for Phases 1–2. Amends ADR-0007: its principle (local-first reads with remote backfill) stands, its mechanism changes.

## Context
The requirements are: browse previously loaded farts offline, create farts offline, and see others' updates in real time.

The plan specified Room as the local source of truth, plus a custom PendingOperation queue and SyncEngine. Room was **not implemented**: it was a catalog entry only, and STORY-01-01-03 had not started.

The native Firestore SDKs that GitLive wraps already provide:
- a persistent cache;
- latency-compensated writes;
- a write queue that survives restarts;
- listeners served from cache first, then server deltas, including removals.

The owner's initial preference was "Room as the source of truth, with the Firestore cache also enabled". That hybrid means two caches and two write queues, which is the dual-sync-path anti-pattern the architecture bans. GitLive `set()` also suspends until the server acknowledges, so a SyncEngine that awaits it blocks offline while Firestore queues the same write.

## Decision (accepted)
1. **Local layer:** Firestore persistent cache (at least 200 MB, set at startup via GitLive `firestoreSettings`), behind the existing `shared` repository interfaces. DataStore holds drafts and preferences.
2. **Write pattern:**
   - a client-generated document id;
   - one batch per user action (for a fart: public doc with fuzzed coordinates + `private/location`);
   - launched in the app-scoped `AppWriteScope`, never awaited on the UI path;
   - a `PendingWriteMonitor` logs the result and emits a typed event on rejection.
3. **Pending state in the UI** comes from snapshot metadata (`hasPendingWrites`).
4. **Session change:** sign-out or account switch calls `terminate()` + `clearPersistence()`.
5. **Docs:** `DataModel.md` + `SyncStrategy.md` are replaced by `architecture/LocalData.md` (originals archived). STORY-01-01-03 becomes "Firestore persistence configuration, write pattern, monitor, tests".

## Alternatives
- **Room as source of truth + custom sync:** full retention control and SQL queries, at roughly 10 artifacts per entity, a sync engine, tombstones for deletes and moderation hides, migrations, and KSP on 3 targets. Its benefits only matter at volumes Phase 1–2 won't reach.
- **Hybrid (Room + Firestore cache):** rejected; it creates a dual sync path.

## Consequences
- **Gains:** large cuts in code, tests and docs; real-time and offline come from one stream.
- **Accepted risks:**
  - a rules rejection after a long offline period is reported only if the process is alive when the write syncs;
  - cache query speed at very large local volumes;
  - LRU eviction if the cache size isn't raised.
- **Triggers to add Room for a specific dataset** (new ADR; repository interfaces keep the change inside `firebaseData`):
  1. guaranteed offline retention beyond the cache is required;
  2. a measured cache query takes over 300 ms;
  3. listener re-read cost exceeds budget;
  4. a Storage upload outbox is needed (voice, Phase 4).
- **Applied on acceptance (2026-11-22):**
  - `LocalData.md` written; `Architecture.md` §4/§5/§8 updated;
  - references updated in `Backend.md`, `Security.md`, `Observability.md` and `DataContracts.md`;
  - `.claude/rules/kotlin-shared.md` and the `new-feature` templates updated;
  - `TechStack.md` and the version catalog updated (Room/KSP/SQLite removed);
  - STORY-01-01-03 rewritten in `planning/ROADMAP.md`;
  - ADR-0001/0007/0016/0021 notes added.

# Sync Strategy: Fart Map

> **Planning spine position:** authoritative offline sync and reconciliation contract.
> Upstream: `architecture/ArchitecturePrinciples.md`, `architecture/Architecture.md`, `architecture/DataModel.md`, `architecture/DataContracts.md`, ADR-0007.
> Downstream: sync engine implementation, repository write paths, retry workers, conflict tests.
> This document defines how local and remote state converge. It does not define UI copy.

---

## 1) Purpose

This document defines one sync strategy for all remote-backed writes.

Goals:

- keep user actions durable offline,
- guarantee eventual convergence,
- remove ambiguity about retries and conflicts,
- eliminate dual sync paths.

---

## 2) Canonical Sync Model

Fart Map uses a local-first, queue-backed, eventual-consistency model:

1. User action writes local state immediately.
2. A `PendingOperation` record is created/updated.
3. Sync engine processes queue when connectivity and auth are valid.
4. On remote success, local entities are reconciled to `SYNCED`.
5. On remote failure, operation is retried with backoff or marked terminal failure when non-retryable.

Reads always come from local store. Remote updates backfill local store.

Out of scope for the queue: purchases, rewarded-ad rewards and account linking are online-only operations handled by their adapters (ADR-0016, ADR-0017, ADR-0018); they are never enqueued and never retried by the sync engine.

---

## 3) Single Sync Path Rule

There is exactly one write-to-remote path: the sync engine consuming pending operations.

- Repositories may request an immediate sync tick after local write, but still through the same queue contract.
- Direct remote writes that bypass pending operations are prohibited.

This rule is mandatory to prevent PoC-style duplicate/competing writes.

---

## 4) Operation Lifecycle

Pending operation states:

1. `QUEUED` (or local equivalent: `PENDING`) - ready for first attempt.
2. `IN_FLIGHT` - currently being attempted (transient runtime state).
3. `RETRY_SCHEDULED` - failed retryable; has `next_retry_at`.
4. `SYNCED` - remote write acknowledged and local state reconciled.
5. `FAILED_TERMINAL` - non-retryable without user intervention.
6. `CANCELLED` - no longer valid due to newer superseding operation or permission revocation.

Durable fields are defined in `DataModel.md` (`PendingOperationLocalEntity`).

---

## 5) Retry Policy

### Retryable categories

- transient network failures,
- backend temporary unavailability,
- timeout conditions,
- token refresh race conditions where re-auth can recover.

### Non-retryable categories

- permission denied by security rules,
- invalid payload schema/validation failure,
- entity permanently removed or blocked by policy.

### Backoff contract

- Exponential backoff with jitter.
- Retry scheduling must use persisted `next_retry_at`.
- Retry count must increment atomically per attempt.
- Max attempt threshold is configurable; after threshold, set terminal state and surface typed error path.

---

## 6) Conflict Resolution Contract

### Baseline ordering

- Use server timestamps as authoritative ordering where available.
- Local `local_updated_at` is advisory for client intent sequencing.

### Content ownership rules

1. **User-owned content writes** (their own fart/comment/profile fields):
   - last valid write by that owner wins for mutable fields,
   - moderation/system-enforced fields override owner writes.
2. **Non-owned content** (others' posts/follow states controlled by counterpart):
   - remote canonical state wins.
3. **Visibility and safety-related fields**:
   - restrictive state wins when ambiguity exists,
   - rule-enforced backend result is final.

### Deletion and removal

- Soft remove propagates as authoritative state.
- Locally cached removed content must be excluded from normal read models.

---

## 7) Idempotency Requirements

Every operation type must be idempotent under retries:

- deterministic document IDs for create/update where possible,
- upsert semantics when safe,
- duplicate reaction/comment prevention by identity key strategy,
- follow/block operations keyed by composite identity.

Duplicate delivery must not create duplicate visible side effects.

---

## 8) Preconditions for Sync Execution

Sync engine may execute when all are true:

1. authenticated session exists,
2. connectivity is available (or operation type allows offline-deferred local processing only),
3. operation is due (`now >= next_retry_at`),
4. entity still valid (not superseded/cancelled).

If preconditions fail, operation remains queued without user-facing hard error.

---

## 9) Observability Coupling

Each attempt emits structured sync telemetry (see `Observability.md`):

- operation type,
- entity type,
- attempt count,
- result class (success/retry/terminal),
- error category (sanitized),
- latency bucket.

No sensitive payloads, precise coordinates, or secret values in logs.

---

## 10) User-Facing Behavior Contract

- Offline submission is accepted and acknowledged locally.
- Transient sync failures do not block normal app usage.
- Terminal failures that need user action surface explicit typed UI state with retry guidance.
- Users never lose already-entered content due to connectivity loss.

---

## 11) Testing Contract

Minimum required sync tests:

1. local write persists and enqueues operation when offline,
2. queued operation syncs when network returns,
3. retry backoff scheduling is respected,
4. non-retryable errors become terminal without infinite retries,
5. idempotency under duplicate delivery,
6. conflict scenarios for visibility/follow/block precedence,
7. sign-out/account switch clears or partitions queue safely.

---

## 12) Explicit Non-Goals

This document does not define:

- Firestore collection schemas (`DataContracts.md`),
- repository package naming (`RepositoryRules.md`),
- analytics taxonomy details (`Observability.md`).

---

*This is the sync contract. Any alternate unsanctioned sync path is out of spec.*

# Skill: Offline Sync Operations

## Purpose
Apply the queue-backed sync model for durable offline behavior.

## Sources
- `planning/architecture/SyncStrategy.md`
- `planning/architecture/DataModel.md`

## Procedure
1. Persist write locally first.
2. Create/update pending operation.
3. Trigger sync engine tick via single sync path.
4. Classify failure retryable vs terminal.
5. Reconcile local sync state on success.

## Failure recovery
- Retry storms: inspect backoff scheduling and idempotency keys.

## Validation
- [ ] No bypass remote writes
- [ ] Pending queue transitions valid
- [ ] Terminal failures surface typed state

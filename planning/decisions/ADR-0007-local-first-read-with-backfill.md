# ADR-0007: Local-First Reads with Remote Backfill

- Status: Accepted; mechanism amended by ADR-0022 (2026-11-22). The principle (local-first reads, remote backfill, offline writes never lost) stands; it is delivered by Firestore's persistent cache and write queue as specified in `architecture/LocalData.md`. The custom pending-operations/sync-engine strategy is archived.
- Date: 2026-06-20
- Source: `planning/archive/meta/archive/PlanningAuditReport.md` §8

## Context
PoC combines local flow and remote snapshots, then backfills local persistence.

## Decision
Keep **local-first read** principle with remote synchronization/backfill.

## Rationale
- Supports responsive UX and offline-first behavior.
- Aligns with technology vision in rebuild brief.
- Preserves user trust in low-connectivity conditions.

## Consequences
- `architecture/SyncStrategy.md` must define pending ops, retries, conflict policy, and idempotency (PoC was ad-hoc).
- Do not copy PoC sync implementation as-is; treat it as intent only.

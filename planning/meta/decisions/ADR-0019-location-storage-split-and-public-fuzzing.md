# ADR-0019: Precise Location Stored Owner-Only; Public Projection Is Fuzzed

- Status: Accepted (parameters confirmed by the owner on 2026-10-15)
- Date: 2026-10-14
- Source: closeout audit finding §5.2; architecture decision under `SecurityAndSecrets.md` §6; owner did not object

## Context
`SecurityAndSecrets.md` §6 forbids returning precise coordinates for content the requester does not own, while `DataContracts.md` §3.2 stored `location_lat/lng` on the same `farts` document that public readers fetch. Firestore has no field-level read rules, so the contract was unenforceable.

## Decision
1. The public `farts/{fartId}` document carries only `public_lat`, `public_lng` (fuzzed) and `coarse_location_key`.
2. Precise coordinates are stored only in `farts/{fartId}/private/location` (`lat`, `lng`), readable and writable by the author only.
3. `coarse_location_key` = geohash cell at **precision 6** (about 1.2 km × 0.6 km). The public point is the cell center plus a deterministic jitter derived from the fart id, so the point is stable and never equals the true position.
4. The fuzzed point and key are computed on the client at write time and re-validated by a Cloud Function trigger (recompute from the private location, overwrite on mismatch).
5. No publication delay in Phase 1. Public views show relative time at hour granularity and no "live now" indicator.
6. No location-history collection in Phase 1; the author's precise history is exactly their own `private/location` documents.

## Consequences
- `DataContracts.md` §3.2/§5/§6, `DataModel.md` §4.1, `SecurityAndSecrets.md` §3/§6, `ContentSafety.md` §3 and `DesignSpecification.md` §11.6 (the "precise location" toggle is removed) are updated.
- **Parameters confirmed (owner, 2026-10-15):** geohash precision 6 and no publication delay are settled and recorded in `ContentSafety.md` §3. Changing them is a privacy-default change under `AgentGuardrails.md` §5 and needs a superseding ADR.

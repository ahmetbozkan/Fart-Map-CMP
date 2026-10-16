# ADR-0002: GitLive Firebase as Single Backend SDK

- Status: Accepted
- Date: 2026-06-20
- Source: `planning/meta/archive/PlanningAuditReport.md` §8

## Context
PoC uses GitLive Firebase across auth/firestore/storage and validates this stack for Android and iOS.

## Decision
Adopt **GitLive Firebase SDK** as the default cross-platform Firebase integration layer.

## Rationale
- Shared API surface across KMP targets.
- Already validated in PoC architecture.
- Keeps backend integration consistent for autonomous agents.

## Consequences
- `architecture/FirebaseStrategy.md` should codify usage boundaries and security rules.
- If any Firebase product lacks adequate GitLive support, decide exceptions via new ADR.

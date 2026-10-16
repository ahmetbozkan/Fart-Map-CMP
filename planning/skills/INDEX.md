# Skills INDEX: Fart Map

> Purpose: skill registry for autonomous implementation work.
> Upstream: `planning/skills/SYSTEM.md`, architecture/product/execution owner docs.
> Downstream: implementation sessions that require repeatable technical playbooks.

---

## 1) Skill Registry

| Skill | Owns | Primary sources |
|---|---|---|
| `architecture-governance.md` | Applying architecture constraints during coding/review | `planning/architecture/ArchitecturePrinciples.md`, `planning/architecture/Architecture.md`, ADRs |
| `repository-and-mappers.md` | Repository implementation and domain/entity/DTO mapping | `planning/architecture/RepositoryRules.md`, `planning/architecture/DataModel.md`, `planning/architecture/DataContracts.md` |
| `offline-sync-operations.md` | Queue-backed write flows, retry, conflict handling | `planning/architecture/SyncStrategy.md`, `planning/architecture/DataModel.md` |
| `firebase-integration.md` | Auth/Firestore/Storage integration boundaries and rule-safe usage | `planning/architecture/FirebaseStrategy.md`, `planning/architecture/DataContracts.md`, `planning/architecture/SecurityAndSecrets.md` |
| `multiplatform-boundaries.md` | expect/actual seams and parity handling | `planning/architecture/MultiplatformStrategy.md`, ADR-0008 |
| `navigation-and-session.md` | Typed routes and session-driven flow implementation | `planning/architecture/NavigationStrategy.md`, ADR-0005/0006 |
| `stateviewmodel-pattern.md` | Screen State/Action/Event implementation pattern | `planning/architecture/Architecture.md`, ADR-0004 |
| `design-handoff-ui.md` | Applying design artifacts into implementable UI tasks | `planning/design/SYSTEM.md`, `planning/design/ImplementationHandoff.md`, `planning/design/DesignSpecification.md` |
| `testing-and-validation.md` | DoR/DoD-aligned verification steps and evidence capture | `planning/execution/DefinitionOfReady.md`, `planning/execution/DefinitionOfDone.md`, `planning/execution/ReviewChecklist.md` |
| `security-privacy-guardrails.md` | Secrets hygiene, privacy defaults, safe logging | `planning/architecture/SecurityAndSecrets.md`, `planning/product/ContentSafety.md`, `planning/architecture/Observability.md` |
| `incident-debug-recovery.md` | Failure triage and autonomous recovery loop | `planning/architecture/Observability.md`, `planning/execution/SYSTEM.md`, `planning/meta/AgentGuardrails.md` |
| `planning-and-scope-control.md` | Converting roadmap items into story/task plans with traceability | `planning/execution/StoryTemplate.md`, `planning/execution/TaskTemplate.md`, `planning/product/Prioritization.md` |
| `monetization-and-entitlements.md` | Purchases, rewarded ads and entitlement checks without client trust | `planning/product/Monetization.md`, ADR-0016, ADR-0017, `planning/architecture/DataContracts.md` §3.13–3.15 |

---

## 2) Skill Selection Guidance

1. New feature implementation: `planning-and-scope-control` + `stateviewmodel-pattern` + one domain skill.
2. Data/sync work: `repository-and-mappers` + `offline-sync-operations` + `firebase-integration`.
3. Cross-platform work: `multiplatform-boundaries` + `navigation-and-session` (if route-related).
4. Security-sensitive change: always include `security-privacy-guardrails`.
5. Debugging failures: start with `incident-debug-recovery`.
6. Purchases, ads or entitlement work: `monetization-and-entitlements` + `firebase-integration` + `security-privacy-guardrails`.

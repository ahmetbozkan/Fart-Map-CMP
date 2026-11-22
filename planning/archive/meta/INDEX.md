# Planning INDEX: Fart Map

> Purpose: machine-readable navigation map for the planning system.
> Use this first when entering the repo with no context.
> Rule: if two docs appear to overlap, follow this index's Owner and Source-of-Truth mapping.

---

## 1) Current Planning Tree (As Built, 2026-10-14)

```text
planning/
├── CLAUDE.md
│
├── product/
│   ├── Vision.md
│   ├── ProductPillars.md
│   ├── Personas.md
│   ├── UserMotivations.md
│   ├── UserJourney.md
│   ├── CoreLoops.md
│   ├── Glossary.md
│   ├── SuccessMetrics.md
│   ├── ContentSafety.md
│   ├── Prioritization.md
│   ├── Features.md
│   ├── SocialSystems.md
│   ├── ProgressionSystems.md
│   ├── Monetization.md
│   ├── ReleaseStrategy.md
│   └── ProductGovernance.md
│
├── architecture/
│   ├── ArchitecturePrinciples.md
│   ├── Architecture.md
│   ├── SecurityAndSecrets.md
│   ├── TechStack.md
│   ├── DataModel.md
│   ├── DataContracts.md
│   ├── RepositoryRules.md
│   ├── SyncStrategy.md
│   ├── FirebaseStrategy.md
│   ├── MultiplatformStrategy.md
│   ├── NavigationStrategy.md
│   └── Observability.md
│
├── execution/
│   ├── SYSTEM.md
│   ├── INDEX.md
│   ├── Workflow.md
│   ├── DefinitionOfReady.md
│   ├── DefinitionOfDone.md
│   ├── StoryTemplate.md
│   ├── TaskTemplate.md
│   ├── SprintRules.md
│   └── ReviewChecklist.md
│
├── roadmap/
│   ├── INDEX.md
│   ├── SYSTEM.md
│   ├── STATE.md
│   ├── PhaseTemplate.md
│   ├── EpicTemplate.md
│   ├── SprintTemplate.md
│   ├── phases/PHASE-01.md
│   ├── epics/EPIC-01-01.md
│   ├── stories/STORY-01-01-01.md
│   ├── tasks/            (created when a story enters Ready)
│   └── sprints/          (created from Ready stories)
│
├── design/
│   ├── SYSTEM.md
│   ├── DesignSpecification.md
│   ├── UXFlowCatalog.md
│   ├── ScreenSpecificationTemplate.md
│   ├── ImplementationHandoff.md
│   ├── HANDOFF-UXF-01-04-MVP.md
│   └── screens/
│       ├── AuthLoginScreen.md
│       ├── MapScreen.md
│       ├── LogFartScreen.md
│       ├── FartDetailSheet.md
│       ├── CosmeticUnlockSheet.md
│       ├── SocialTabScreen.md
│       └── RankTabScreen.md
│
├── skills/
│   ├── SYSTEM.md
│   ├── INDEX.md
│   ├── architecture-governance.md
│   ├── repository-and-mappers.md
│   ├── offline-sync-operations.md
│   ├── firebase-integration.md
│   ├── multiplatform-boundaries.md
│   ├── navigation-and-session.md
│   ├── stateviewmodel-pattern.md
│   ├── design-handoff-ui.md
│   ├── testing-and-validation.md
│   ├── security-privacy-guardrails.md
│   ├── incident-debug-recovery.md
│   ├── planning-and-scope-control.md
│   └── monetization-and-entitlements.md
│
└── meta/
    ├── INDEX.md
    ├── PlanningGlossary.md
    ├── AgentGuardrails.md
    ├── ChangeLog.md
    ├── decisions/
    │   ├── ADR-0001-room-over-sqldelight.md
    │   ├── ADR-0002-gitlive-firebase-single-sdk.md
    │   ├── ADR-0003-koin-with-expect-actual-platform-modules.md
    │   ├── ADR-0004-custom-stateviewmodel-pattern.md
    │   ├── ADR-0005-type-safe-compose-navigation.md
    │   ├── ADR-0006-session-driven-navigation.md
    │   ├── ADR-0007-local-first-read-with-backfill.md
    │   ├── ADR-0008-expect-actual-platform-boundary-pattern.md
    │   ├── ADR-0009-follow-only-with-approval.md
    │   ├── ADR-0010-visibility-tiers-public-followers-private.md
    │   ├── ADR-0011-territory-out-of-mvp.md
    │   ├── ADR-0012-following-list-private-by-default.md
    │   ├── ADR-0013-signature-single-style-selection.md
    │   ├── ADR-0014-audio-recording-10s-mvp-simple.md
    │   ├── ADR-0015-cloud-functions-and-firebase-product-scope.md
    │   ├── ADR-0016-purchases-and-entitlements-via-revenuecat.md
    │   ├── ADR-0017-rewarded-ads-admob-with-server-side-verification.md
    │   ├── ADR-0018-anonymous-accounts-can-create-content.md
    │   ├── ADR-0019-location-storage-split-and-public-fuzzing.md
    │   └── ADR-0020-phase-1-scope-additions-2026-10-14.md
    ├── audits/
    │   └── 2026-10-14-planning-closeout-audit.md
    └── archive/                      (history, never authority)
        ├── Planning-Rebuild.md
        ├── PlanningAuditReport.md
        ├── PlanningStructureReview.md
        ├── AIOperatingSystemReview.md
        └── PlanningDependencyGraph.md
```

---

## 2) Read Order (Fast Onboarding)

For any new human/agent entering with no context:

1. `planning/CLAUDE.md` (entrypoint and global constraints)
2. `planning/roadmap/STATE.md` (current repo status and authorized next work)
3. `planning/meta/AgentGuardrails.md` (operating constitution)
4. Product spine in order:
   - `planning/product/ProductGovernance.md` (read first: the constitution)
   - `planning/product/Vision.md`
   - `planning/product/ProductPillars.md`
   - `planning/product/Personas.md`
   - `planning/product/UserMotivations.md`
   - `planning/product/UserJourney.md`
   - `planning/product/CoreLoops.md`
   - `planning/product/Glossary.md`
   - `planning/product/SuccessMetrics.md`
   - `planning/product/ContentSafety.md`
   - `planning/product/Prioritization.md`
   - `planning/product/Features.md`
   - `planning/product/SocialSystems.md`
   - `planning/product/ProgressionSystems.md`
   - `planning/product/Monetization.md`
   - `planning/product/ReleaseStrategy.md`
5. Settled decisions: `planning/meta/decisions/ADR-0001` … `ADR-0020`
6. Architecture core:
   - `planning/architecture/ArchitecturePrinciples.md`
   - `planning/architecture/Architecture.md`
   - `planning/architecture/SecurityAndSecrets.md`
   - `planning/architecture/TechStack.md`
   - `planning/architecture/DataModel.md`
   - `planning/architecture/DataContracts.md`
   - `planning/architecture/RepositoryRules.md`
   - `planning/architecture/SyncStrategy.md`
   - `planning/architecture/FirebaseStrategy.md`
   - `planning/architecture/MultiplatformStrategy.md`
   - `planning/architecture/NavigationStrategy.md`
   - `planning/architecture/Observability.md`
7. Execution kernel:
   - `planning/execution/SYSTEM.md`
   - `planning/execution/INDEX.md`
   - `planning/execution/Workflow.md`
   - `planning/execution/DefinitionOfReady.md`
   - `planning/execution/DefinitionOfDone.md`
   - `planning/execution/StoryTemplate.md`
   - `planning/execution/TaskTemplate.md`
   - `planning/execution/SprintRules.md`
   - `planning/execution/ReviewChecklist.md`
8. Design baseline:
   - `planning/design/SYSTEM.md`
   - `planning/design/DesignSpecification.md`
   - `planning/design/UXFlowCatalog.md`
   - `planning/design/ScreenSpecificationTemplate.md`
   - `planning/design/ImplementationHandoff.md`
   - `planning/design/screens/*.md`
   - `planning/design/HANDOFF-*.md`
9. Skills layer:
   - `planning/skills/SYSTEM.md`
   - `planning/skills/INDEX.md`
   - load matching skill file(s) for the current technical concern
10. Roadmap governance and instances:
    - `planning/roadmap/SYSTEM.md`
    - `planning/roadmap/INDEX.md`
    - `planning/roadmap/PhaseTemplate.md`, `EpicTemplate.md`, `SprintTemplate.md`
    - `planning/roadmap/phases/*.md`, `epics/*.md`, `stories/*.md`, `tasks/*.md`, `sprints/*.md`

Not in the read chain: `planning/meta/archive/*` (how the system was built) and `planning/meta/audits/*` (evidence and decision logs). Open them only when you need the history behind a decision.

---

## 3) Document Registry (Owner + Source of Truth)

| Path | Owner concern | Source of truth for | Read when |
|---|---|---|---|
| `planning/CLAUDE.md` | Operating entrypoint | Session start and mandatory doc chain | Starting any planning/implementation session |
| `planning/roadmap/STATE.md` | Live state governance | Current maturity, authorization, next work | Deciding what can be worked on now |
| `planning/roadmap/SYSTEM.md` | Roadmap governance | Hierarchy, IDs, and traceability requirements for delivery artifacts | Creating roadmap phases/epics/stories/tasks/sprints |
| `planning/roadmap/INDEX.md` | Roadmap navigation | Roadmap file map, template ownership, and authoring sequence | Starting roadmap artifact authoring |
| `planning/roadmap/PhaseTemplate.md` | Roadmap template | Canonical schema for phase artifacts | Authoring phase instances |
| `planning/roadmap/EpicTemplate.md` | Roadmap template | Canonical schema for epic artifacts | Authoring epic instances |
| `planning/roadmap/SprintTemplate.md` | Roadmap template | Canonical schema for sprint artifacts (single owner) | Authoring sprint instances |
| `planning/roadmap/phases/PHASE-01.md` | Phase instance | Phase 1 goals, gates, planned epics | Any Phase 1 planning |
| `planning/roadmap/epics/EPIC-01-01.md` | Epic instance | Bootstrap and walking skeleton scope | Starting the new repository |
| `planning/roadmap/stories/STORY-01-01-01.md` | Story instance | Project bootstrap and build pipeline | First implementation story |
| `planning/meta/AgentGuardrails.md` | AI operating governance | Autonomy, escalation, forbidden behavior | Deciding how agent should operate |
| `planning/product/Vision.md` | Product | Product mission and north-star intent | Resolving product-direction ambiguity |
| `planning/product/ProductPillars.md` | Product governance | Pillar model, conflict order, phase focus (weights owned by ReleaseStrategy §3) | Strategic fit scoring |
| `planning/product/Personas.md` | Product | Target/anti personas and phase priority (six personas) | Audience fit decisions |
| `planning/product/UserMotivations.md` | Product psychology | M1-M8 user drives | Explaining return/share behavior |
| `planning/product/UserJourney.md` | Product flow | Lifecycle stages and drop-off map | Stage placement decisions |
| `planning/product/CoreLoops.md` | Product mechanics | L1-L8 loop definitions and interlocks (canonical loop ids) | Loop-strength validation |
| `planning/product/Glossary.md` | Product taxonomy | Domain term definitions | Naming/model alignment |
| `planning/product/SuccessMetrics.md` | Product analytics | KPI and counter-metric framework | Success/failure measurement |
| `planning/product/ContentSafety.md` | Product safety | Threat model and mandatory controls; age gate; fuzzing default | Any social/location/UGC work |
| `planning/product/Prioritization.md` | Product governance | Gate-first feature scoring framework | Ranking epics/features |
| `planning/product/Features.md` | Product scope | Canonical feature-system specification and feature ids/names | Defining what exists in product |
| `planning/product/SocialSystems.md` | Social systems detail | Interaction lifecycle and social contracts | Social feature design/implementation |
| `planning/product/ProgressionSystems.md` | Progression systems detail | XP/streak/ranking/challenge rules | Progression feature design |
| `planning/product/Monetization.md` | Monetization detail | Cosmetic economy, purchase and ad rules, monetization limits | Payment/unlock behavior decisions |
| `planning/product/ReleaseStrategy.md` | Release sequencing | Phase boundaries, per-phase weights, gates, phase assignment of every feature, non-goals | Phase and MVP scope decisions |
| `planning/product/ProductGovernance.md` | Product constitution | Non-negotiable product identity and guardrails | Any major product tradeoff |
| `planning/architecture/ArchitecturePrinciples.md` | Engineering constitution | Core engineering values and anti-patterns | Any architecture decision |
| `planning/architecture/Architecture.md` | System blueprint | Layer model, module strategy, dependency laws, canonical flows | Structuring implementation/doc contracts |
| `planning/architecture/SecurityAndSecrets.md` | Security architecture | Secrets, trust boundaries, data sensitivity, privacy constraints | Any security/privacy-sensitive work |
| `planning/architecture/TechStack.md` | Stack governance | Approved frameworks, bootstrap baseline policy, upgrade rules, CI minimum, iOS linking rule | Selecting/upgrading dependencies; bootstrapping |
| `planning/architecture/DataModel.md` | Local data contract | Room/DataStore local schema boundaries and round-trip rules | Local persistence design and migrations |
| `planning/architecture/DataContracts.md` | Remote data contract | Firestore entity shapes, catalog/entitlement/token/device collections, access-rule intent | Backend model/rule implementation |
| `planning/architecture/RepositoryRules.md` | Data boundary governance | Repository ownership, mapping boundaries, allowed dependencies | Implementing or reviewing repository code |
| `planning/architecture/SyncStrategy.md` | Offline sync governance | Queue lifecycle, retry and conflict policy, convergence behavior | Offline-first write/read path work |
| `planning/architecture/FirebaseStrategy.md` | Firebase integration governance | SDK usage boundaries, auth/firestore/storage/functions/messaging/app-check rules | Firebase integration work |
| `planning/architecture/MultiplatformStrategy.md` | Cross-platform governance | Shared vs platform code rules, parity expectations | Platform-boundary design decisions |
| `planning/architecture/NavigationStrategy.md` | Navigation governance | Typed route/graph/session-navigation contracts, four-tab root | Navigation graph and start flow work |
| `planning/architecture/Observability.md` | Telemetry governance | Typed error/logging/analytics/safety telemetry constraints; Phase 1 event taxonomy | Error handling and telemetry implementation |
| `planning/execution/SYSTEM.md` | Execution kernel | Closed control loop and decision authority matrix | Running work cycles |
| `planning/execution/INDEX.md` | Execution navigation | Ownership map of execution docs and lifecycle entrypoints | Creating or reviewing story/task/sprint artifacts |
| `planning/execution/Workflow.md` | Execution process | Work-item state machine and transitions | Moving item statuses |
| `planning/execution/DefinitionOfReady.md` | Readiness gate | Start criteria for work items | Moving Backlog -> Ready |
| `planning/execution/DefinitionOfDone.md` | Completion gate | Finish criteria and evidence requirements | Moving Review -> Done |
| `planning/execution/StoryTemplate.md` | Story contract | Required story schema and traceability/evidence fields | Authoring story artifacts |
| `planning/execution/TaskTemplate.md` | Task contract | Required task schema and validation/evidence fields | Authoring implementation tasks |
| `planning/execution/SprintRules.md` | Sprint governance | Capacity heuristic, sprint eligibility, and in-sprint discipline | Building sprint plans |
| `planning/execution/ReviewChecklist.md` | Review governance | Pre-Done reviewer script and pass/fail outcomes | Running Review -> Done decisions |
| `planning/design/SYSTEM.md` | Design governance | Design-layer process, ownership, Stitch screen-by-screen process, design-ready definition | Designing new screen/flow artifacts |
| `planning/design/DesignSpecification.md` | Design system baseline | UI tokens, component rules, four-tab navigation, screen behavior and Phase 1 variants | UI/UX implementation guidance |
| `planning/design/UXFlowCatalog.md` | Design traceability | Feature-to-flow mapping and flow registry (UXF-01…13) | Mapping stories to UX journeys |
| `planning/design/ScreenSpecificationTemplate.md` | Design template | Standard per-screen spec contract | Authoring screen specs |
| `planning/design/ImplementationHandoff.md` | Design handoff | Design-to-implementation gate and handoff package | Moving UI stories to Ready |
| `planning/design/screens/*.md` | Design screen specs | Concrete screen-level interaction/state contracts | Planning or implementing UI surfaces |
| `planning/design/HANDOFF-*.md` | Design handoff instance | Flow-specific handoff package and acceptance contract | Moving a specific UI flow into execution planning |
| `planning/skills/SYSTEM.md` | Skill governance | Skill ownership boundaries and minimum skill standard | Maintaining skill library |
| `planning/skills/INDEX.md` | Skill navigation | Required skill set and source links | Selecting skill(s) for implementation session |
| `planning/skills/*.md` (13 playbooks) | Skill playbooks | Task-time procedures, recovery and validation per concern | Implementation sessions |
| `planning/meta/PlanningGlossary.md` | Meta vocabulary | Planning-system terms and semantics | Interpreting status language |
| `planning/meta/ChangeLog.md` | Meta history | Chronological planning system changes | Auditing what changed and why |
| `planning/meta/decisions/*.md` | Immutable decisions | Settled architecture/product decisions (ADR-0001…0020) | Checking whether a decision is already locked |
| `planning/meta/audits/*.md` | Audit evidence | Closeout audits and decision logs (not authority) | Understanding why a reconciliation happened |
| `planning/meta/archive/*.md` | Process history | The 2026-06 rebuild brief and reviews (not authority) | Understanding how the system was built |

---

## 4) Dependency Rules (Enforced)

1. **No forward authoring:** do not author downstream docs without upstream inputs.
2. **Single-definition rule:** define each concept once in its owner doc and reference elsewhere. Feature ids and names are owned by `product/Features.md`; phase assignments and per-phase pillar weights are owned by `product/ReleaseStrategy.md`.
3. **Gate-before-score rule:** run hard product/safety gates before prioritization scoring.
4. **Safety-first rule:** social/location/UGC changes must satisfy `product/ContentSafety.md` and `architecture/SecurityAndSecrets.md`.
5. **ADR precedence:** accepted ADRs are binding until superseded by a new ADR.
6. **State synchronization rule:** after meaningful planning/execution changes, update `roadmap/STATE.md` and `meta/ChangeLog.md`.

---

## 5) Current Gaps and Next Build Targets

### Presently missing

- Roadmap instances beyond `PHASE-01`, `EPIC-01-01` and `STORY-01-01-01` are authored when the preceding story reaches Ready (`roadmap/STATE.md` §6).
- Stitch artifacts for every screen (placeholders `TBD-UXF-…` remain until the owner produces them screen by screen; non-blocking per `design/SYSTEM.md` §4).

### Current priority order

Use `planning/roadmap/STATE.md` as the definitive sequencing source.

---

*This INDEX is the navigation backbone of the planning system. Keep it synchronized with tree, authority, and workflow changes.*

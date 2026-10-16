# Planning Glossary: Fart Map

> Purpose: define planning-system terms used across docs so humans and AI agents interpret governance language consistently.
> Scope: process and planning semantics only. Domain/product terms belong in `planning/product/Glossary.md`.

---

## 1) Planning Artifact Terms

| Term | Definition | Owner doc |
|---|---|---|
| **Vision** | Long-term product purpose and north star intent. Highest-level product arbiter. | `planning/product/Vision.md` |
| **Product Pillars** | Stable strategic filters (Location, Social, Humor, Progress, Identity) used for scoring and trade-offs. | `planning/product/ProductPillars.md` |
| **Persona** | Motivation-based user archetype used for prioritization and phase fit, not demographics. | `planning/product/Personas.md` |
| **Motivation** | Psychological drive behind user action/return/share. | `planning/product/UserMotivations.md` |
| **Journey** | Lifecycle path and stage transitions users move through over time. | `planning/product/UserJourney.md` |
| **Core Loop** | Repeatable action→reward→reinvestment cycle producing retention/virality. | `planning/product/CoreLoops.md` |
| **Feature** | User-facing capability that powers one or more loop elements. | `planning/product/Features.md` |
| **System** | Multi-feature capability domain (social/progression/monetization) that governs related mechanics. | `planning/product/SocialSystems.md`, `ProgressionSystems.md`, `Monetization.md` |

---

## 2) Planning Process Terms

| Term | Definition |
|---|---|
| **Phase** | Strategic maturity stage of the product (Core Loop, Social Retention, Progression, Community). |
| **Epic** | A large outcome-oriented body of work spanning multiple stories. |
| **Story** | A vertical, user-value slice that can be completed end-to-end. |
| **Task** | Implementation unit contributing to a story. |
| **Sprint** | Time-boxed execution batch where selected stories/tasks are delivered. |
| **Definition of Ready (DoR)** | Start gate proving work is sufficiently specified and unblocked. |
| **Definition of Done (DoD)** | Completion gate proving work meets quality and acceptance requirements. |
| **Traceability** | Mandatory upward linkage from task/story/epic to feature→loop→pillar. |
| **Guardrail** | A non-negotiable operating boundary (e.g., safety, trust, secrets policy). |
| **Escalation trigger** | A condition requiring human review rather than autonomous agent decision. |

---

## 3) Decision & Governance Terms

| Term | Definition |
|---|---|
| **ADR (Architecture Decision Record)** | Immutable record of a significant decision, context, and consequences. |
| **Hard gate** | Pre-scoring validation rule that can reject a proposal immediately. |
| **Counter-metric** | Guardrail metric that must not degrade while optimizing a KPI. |
| **North Star metric** | Primary aggregate metric representing long-term product success. |
| **Pillar scorecard** | 1-5 scoring across the five product pillars. |
| **PriorityScore** | Quantified ranking output from the prioritization model. |
| **TBD** | Explicit unresolved decision. Features depending on it are blocked or conditional. |
| **Provisional** | Working definition suitable for planning but still open to refinement. |
| **Stable** | Agreed definition considered safe to build on. |

---

## 4) Status Vocabulary (Required)

Use these statuses consistently in planning docs:

| Status | Meaning | Action |
|---|---|---|
| **Draft** | Early version under active authoring | May change significantly |
| **Active** | Current authoritative version | Use as source of truth |
| **Superseded** | Replaced by a newer document/version | Keep for history, do not rely on it |
| **Blocked** | Cannot proceed due to unmet dependency/TBD | Resolve blocker before continuing |
| **Deprecated** | Intentionally retained but should not guide new work | Migrate references away |

---

## 5) Naming Conventions

1. Use singular nouns for concept docs (`UserJourney.md`, not `UserJourneys.md`) unless already standardized.
2. Use `TitleCase.md` for top-level planning files.
3. Prefix ADRs with zero-padded ids: `ADR-0001-...md`.
4. For matrices, prefer markdown tables (user preference).
5. When terms collide between domain and process, domain terms live in `product/Glossary.md`; process terms live here.

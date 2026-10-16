# Planning Structure Review — Fart Map

> Phase 2 deliverable of `meta/archive/Planning-Rebuild.md`.
> Purpose: critically evaluate the *proposed* `planning/` structure from the rebuild brief and recommend a final structure optimized for autonomous AI-agent development of a brand-new Compose Multiplatform project.
> Scope: structure, folders, document inventory, and governance coverage **only**. This is not implementation; no application code, stories, or tasks are produced here.

---

## 1) How to Read This Document

This review treats the planning system as the **operating system** future AI agents will live inside. A structure is "good" only if an agent with zero prior context can:

1. Find the single authoritative document for any decision (no ambiguity about *where truth lives*).
2. Traverse from product intent → architecture rule → executable work without guessing.
3. Avoid contradicting another document (no overlapping ownership of a topic).

Every recommendation below is justified against those three tests: **findability**, **traceability**, **non-contradiction**.

---

## 2) Verdict at a Glance

The proposed structure is **strong and ~85% correct**. The five-folder split (`product/`, `architecture/`, `execution/`, `skills/`, `roadmap/`, `design/`) is sound and maps cleanly to the three audiences of a planning system: *what to build* (product), *how it must be built* (architecture), *how work flows* (execution), *how to actually do a task* (skills), *what to do next* (roadmap), and *what it looks like* (design).

The gaps are not structural collapse — they are **missing governance documents**, a few **ownership ambiguities**, and the absence of **machine-readable indices** that autonomous agents need to navigate without re-reading everything.

| Question from brief | Short answer |
|---|---|
| 1. Is anything missing? | Yes — see §5 (15 additions across product, architecture, execution, meta). |
| 2. Is anything redundant? | Minor — see §6 (3 overlaps to resolve, 0 hard deletions). |
| 3. Better folder separations? | Yes — see §7 (split `skills/`, add `meta/`, clarify `design/` vs `architecture/DesignSystem.md`). |
| 4. Missing AI-agent docs? | Yes — see §8 (the most important gap: agent indices, decision records, guardrails). |
| 5. Missing architecture governance? | Yes — see §9 (security, observability, error model, data contracts). |
| 6. Missing product governance? | Yes — see §10 (metrics, content/safety, naming/taxonomy, prioritization). |

---

## 3) Evaluation of the Proposed Structure (Folder by Folder)

### 3.1 `product/`
**Keep.** Audience: product agents and humans defining *what* and *why*. The ordering Vision → Pillars → Personas → Motivations → Journey → CoreLoops → Features → Systems → Roadmap → Glossary is the correct dependency spine (validated further in Phase 4).

Issues:
- `Roadmap.md` sits in `product/` but `roadmap/` is also a top-level folder. This is a **naming collision** that will confuse agents (§6.1).
- Missing product *governance* docs (metrics, safety, prioritization) — §10.

### 3.2 `architecture/`
**Keep.** Audience: implementation agents needing binding technical rules. Coverage of the 10 listed docs is good and matches the implicit decisions extracted in the Audit (§8 of the Audit Report).

Issues:
- No security/secrets doc despite the Audit flagging **committed API keys** as the single highest risk. This is a mandatory addition (§9.1).
- No observability/error-model doc despite swallowed errors in the PoC (§9.2).
- `DesignSystem.md` here overlaps conceptually with the `design/` folder — ownership must be split cleanly (§7.3).

### 3.3 `execution/`
**Keep.** Audience: the agent's *process* layer. `SYSTEM.md`, `Workflow.md`, `DefinitionOfDone.md`, templates, and `SprintRules.md` are the right primitives.

Issues:
- Missing a **Definition of Ready** (the gate *before* work starts) to complement Definition of Done.
- Missing an explicit **agent guardrails / escalation** policy (when an agent must stop and ask a human) — critical for "minimal human intervention" without going off the rails (§8.3).

### 3.4 `skills/`
**Keep, but reorganize.** Audience: task-time "how-to" playbooks. The 14 proposed skills are good but mix two different kinds of skill: **layer/domain skills** (architecture, repository, sync, data-layer, presentation-layer) and **activity skills** (debugging, refactoring, planning). Flat is fine for 14 files; recommend light sub-grouping for scale (§7.2).

### 3.5 `roadmap/`
**Keep.** Audience: planning agents. The `phases/ epics/ stories/ tasks/ sprints/` hierarchy is correct and intentionally **empty for now** (the brief forbids generating these yet). Add an index/state file so agents can find the *current* sprint and *next* work without scanning directories (§8.1).

### 3.6 `design/`
**Keep.** Audience: UI agents. Per-screen design docs + principles + UX rules is a good split. Needs a clear boundary with `architecture/DesignSystem.md`: **DesignSystem = the reusable component/token contract; design/ = how each screen composes those components** (§7.3).

### 3.7 `CLAUDE.md` (root)
**Keep, and elevate.** This is the agent entrypoint. It should not be a loose root file by accident — it should be the *intentional front door* that routes an agent to every index. Pair it with a `meta/` folder (§7.1, §8).

---

## 4) Audience & Ownership Map (the test every doc must pass)

Each document must have exactly **one** owning concern. This table is the contract that prevents contradiction.

| Folder | Primary question it answers | Owns decisions about | Must NOT decide |
|---|---|---|---|
| `product/` | What & why are we building? | Features, value, users, systems, success | Tech choices, code patterns |
| `architecture/` | How must it be built (rules)? | Layers, data, sync, platform, security | Product scope, sprint order |
| `execution/` | How does work flow? | Process, DoD/DoR, templates, sprints | Technical patterns, product scope |
| `skills/` | How do I perform this task? | Step-by-step recipes, examples | Binding rules (those live in architecture) |
| `roadmap/` | What is next / current? | Sequencing, epics→tasks, sprint state | The "how" of building |
| `design/` | What does each screen look/feel like? | Screen layout, UX flows, states | Component internals (DesignSystem) |
| `meta/` (new) | How is the planning system itself governed? | Indexes, decision records, glossary-of-docs | Any product/tech content directly |

> **Rule for agents:** if two documents seem to answer the same question, the one named in this table wins; the other must link to it, not restate it.

---

## 5) Additions (Missing Documents)

Priority: **P0** = blocking for safe autonomy, **P1** = needed before implementation phases, **P2** = quality/scale.

### Product (`product/`)
| Doc | Priority | Why it's missing-critical |
|---|---|---|
| `SuccessMetrics.md` | P1 | Pillars define *what matters*; no doc defines *how we measure success* (activation, retention, virality K-factor, DAU/streak metrics). Agents cannot prioritize without target metrics. |
| `ContentSafety.md` | P1 | Location + social + user-generated humor = harassment, doxxing, NSFW, and minor-safety risks. A viral consumer app **must** encode safety/moderation policy before social features are designed. |
| `Prioritization.md` | P2 | Encodes the scoring model (e.g., pillar-weighted RICE) so agents rank features/epics consistently. Pillar weighting exists in ProductPillars.md but the *application method* is undocumented. |

### Architecture (`architecture/`)
| Doc | Priority | Why it's missing-critical |
|---|---|---|
| `SecurityAndSecrets.md` | P0 | Audit's #1 risk = committed secrets. The new repo needs an explicit secrets-management + auth-security + Firestore-rules policy from day one. |
| `Observability.md` | P1 | Covers logging, crash reporting, analytics events, and the error/`Result` model. PoC swallows errors; this prevents repeating that. |
| `DataContracts.md` (Firestore schema + security rules) | P1 | `DataModel.md` covers local Room; the **Firestore collection schema, indexes, and security rules** are a separate governance surface and were entirely absent in the PoC. |
| `ConcurrencyAndState.md` | P2 | Coroutine/dispatcher conventions, flow lifecycle, state-holder rules. Implied by `StateViewModel` but never codified. |

### Execution (`execution/`)
| Doc | Priority | Why it's missing-critical |
|---|---|---|
| `DefinitionOfReady.md` | P1 | DoD gates completion; nothing gates *start*. Prevents agents picking up under-specified work. |
| `AgentGuardrails.md` | P0 | Defines hard stops, escalation triggers, forbidden actions (e.g., never commit secrets, never break layer boundaries, when to ask a human). Essential for "minimal human intervention." |
| `ReviewChecklist.md` | P2 | Self-review gate an agent runs before marking work done (security, parity, tests, layering). |

### Meta (`meta/` — new folder, see §7.1)
| Doc | Priority | Why it's missing-critical |
|---|---|---|
| `INDEX.md` | P0 | Machine-readable map of every planning doc + its owning concern. The single artifact that makes the system navigable without full re-reads. |
| `DecisionRecords/` (ADR-style) | P1 | Append-only log of *why* decisions were made (Room over SQLDelight, GitLive, custom MVVM). Prevents agents re-litigating settled choices. |
| `PlanningGlossary.md` (docs/process terms) | P2 | Distinct from product `Glossary.md` (domain terms). Defines what "epic", "skill", "DoR" mean *in this system*. |
| `ChangeLog.md` | P2 | Tracks evolution of the planning system itself so agents know what changed and when. |

---

## 6) Redundancies & Overlaps (Resolve, Don't Necessarily Delete)

| # | Overlap | Problem | Resolution |
|---|---|---|---|
| 6.1 | `product/Roadmap.md` **vs** top-level `roadmap/` | Two things named "roadmap" with different meanings (strategic narrative vs work-item tree) | Rename `product/Roadmap.md` → `product/ReleaseStrategy.md` (the *narrative* of phases/releases). `roadmap/` stays as the *executable* tree. |
| 6.2 | `architecture/DesignSystem.md` **vs** `design/` | Both touch UI | Split ownership: `DesignSystem.md` = tokens + reusable components + theming contract; `design/` = per-screen composition & UX. Each links to the other (§7.3). |
| 6.3 | `skills/architecture.md`, `skills/repository.md`, `skills/sync.md`, etc. **vs** `architecture/*.md` | Same topic names in two folders | This is intentional and fine **iff** roles are explicit: `architecture/` = binding **rules**; `skills/` = **how-to recipes** that *reference* those rules. Add a one-line header to every skill: "Authoritative rules: see architecture/X.md." |

No documents are recommended for outright deletion. The proposed set is lean; the risk is overlap-without-ownership, not bloat.

---

## 7) Restructuring Suggestions

### 7.1 Add a top-level `meta/` folder
The planning system needs to govern *itself*. Indices, decision records, and a doc-glossary are not product/architecture/execution content — they're planning infrastructure. Putting them in `meta/` keeps `CLAUDE.md` as a thin entrypoint that points into `meta/INDEX.md`.

```
planning/
├── meta/
│   ├── INDEX.md
│   ├── PlanningGlossary.md
│   ├── ChangeLog.md
│   └── decisions/            # ADR-0001-room-over-sqldelight.md, ...
├── product/
├── architecture/
├── execution/
├── skills/
├── roadmap/
├── design/
└── CLAUDE.md
```

### 7.2 Lightly group `skills/` by kind
Flat works at 14 files, but as skills grow, separate **knowledge skills** (layer/domain) from **activity skills** (process). Optional now, recommended before implementation scales:

```
skills/
├── domain/        # architecture, repository, sync, firebase, multiplatform,
│                  # navigation, data-layer, presentation-layer, koin-di,
│                  # compose-ui-patterns
└── activity/      # debugging, refactoring, error-handling, planning
```

### 7.3 Cleanly partition UI ownership (`architecture/DesignSystem.md` ↔ `design/`)
- `architecture/DesignSystem.md`: design tokens (color/type/spacing/shape), reusable component contracts (the `ui/ds/*` kit from the PoC), theming, accessibility baseline. **Reusable + cross-screen.**
- `design/*Screen.md`: how each screen assembles components, screen states (empty/loading/error/success), navigation in/out, screen-specific UX. **Screen-specific.**
- `design/DesignPrinciples.md` + `design/UXRules.md`: the *philosophy*; both `DesignSystem.md` and per-screen docs must conform.

### 7.4 Move the three existing product docs into `product/`
`Vision.md`, `ProductPillars.md`, `Personas.md` currently live at `planning/` root. The target structure places them in `product/`. Defer the physical move to **Phase 5** (where the brief authorizes touching the product folder), but record the intent here so it isn't lost.

---

## 8) AI-Agent-Specific Document Gaps (Brief Question 4 — Highest Leverage)

Autonomous agents fail not from missing *content* but from missing *navigation and guardrails*. These are the docs that turn a pile of markdown into an operating system.

### 8.1 `meta/INDEX.md` + `roadmap/STATE.md` (navigation)
- `meta/INDEX.md`: every doc, its owning concern, and "read this when…" triggers. Lets an agent load only what's relevant.
- `roadmap/STATE.md`: pointer to *current phase / active sprint / next ready work*. Without it, an agent must scan directories to know what to do — fragile and slow.

### 8.2 `meta/decisions/` (memory)
ADR-style records make settled decisions immutable context. An agent reading `ADR-0001-room-over-sqldelight.md` won't waste a cycle proposing SQLDelight. Seed with the 8 decisions in Audit §8.

### 8.3 `execution/AgentGuardrails.md` (safety)
The contract that makes "minimal human intervention" safe. Must specify:
- **Hard stops** (commit secrets? break domain→data direction? skip tests? → never).
- **Escalation triggers** (ambiguous requirement, security-sensitive change, irreversible action → ask human).
- **Autonomy boundaries** (what an agent may decide alone vs. what needs an ADR).

### 8.4 `execution/SYSTEM.md` (the kernel)
Already in the proposed structure — flagged here because it is the **most important execution doc**: the agent's operating contract (role, loop, how to use INDEX/skills/guardrails, how to record decisions). Phase 3 will assess its sufficiency in depth.

---

## 9) Architecture Governance Gaps (Brief Question 5)

| Gap | Proposed doc | Rationale (tied to Audit) |
|---|---|---|
| Secrets & security | `architecture/SecurityAndSecrets.md` (P0) | Audit's top risk: committed Maps key, plist in VCS. Needs secret strategy + Firestore security rules + auth hardening (Apple/Google parity). |
| Error & observability model | `architecture/Observability.md` (P1) | PoC swallows errors (`printStackTrace`). Define `Result`/failure types, logging, crash + analytics events. |
| Backend data contract | `architecture/DataContracts.md` (P1) | `DataModel.md` = local Room only. Firestore schema/indexes/rules are a separate, currently-undocumented surface. |
| Concurrency/state conventions | `architecture/ConcurrencyAndState.md` (P2) | Dispatcher rules, flow lifecycle, state-holder scope — implied by `StateViewModel`, never written. |
| Dependency-direction enforcement | Fold into `architecture/RepositoryRules.md` | Audit found domain→data leak; rules doc must state the allowed direction explicitly. |

`TestingStrategy.md` already exists in the proposal — keep it; it covers the testing gap from Audit §7.

---

## 10) Product Governance Gaps (Brief Question 6)

| Gap | Proposed doc | Rationale |
|---|---|---|
| Success metrics & targets | `product/SuccessMetrics.md` (P1) | No measurable definition of success → agents can't prioritize or know "done at product level." Needs activation/retention/virality/streak KPIs. |
| Content safety & moderation | `product/ContentSafety.md` (P1) | Location + social + UGC humor demands explicit safety policy *before* social/map features are designed (doxxing, harassment, minors, NSFW, location privacy). |
| Prioritization method | `product/Prioritization.md` (P2) | Makes pillar-weighted scoring reproducible so feature/epic ranking is consistent across agents. |
| Domain taxonomy | `product/Glossary.md` (already proposed) | Audit flagged ambiguous terms (`signature`, `flavor`, `intensity`, `territory`, `tbd` ids). Keep and prioritize early — Systems/Features depend on it. |

> Note: `SocialSystems.md`, `ProgressionSystems.md`, `Monetization.md` already exist in the proposal and cover the major *system* surfaces. The gap is **measurement, safety, and prioritization governance**, not system definition.

---

## 11) Recommended Final Structure (Consolidated)

```
planning/
├── CLAUDE.md                      # thin entrypoint → meta/INDEX.md
│
├── meta/                          # NEW: governs the planning system itself
│   ├── INDEX.md                   # P0  map of all docs + owning concern + triggers
│   ├── PlanningGlossary.md        # P2  process/doc terms (epic, skill, DoR…)
│   ├── ChangeLog.md               # P2  planning-system evolution log
│   └── decisions/                 # P1  ADR-style records (seed from Audit §8)
│
├── product/
│   ├── Vision.md                  # (move from root in Phase 5)
│   ├── ProductPillars.md          # (move from root in Phase 5)
│   ├── Personas.md                # (move from root in Phase 5)
│   ├── UserMotivations.md         # ← Phase 5
│   ├── UserJourney.md             # ← Phase 5
│   ├── CoreLoops.md               # ← Phase 5
│   ├── Features.md                # later (NOT this phase)
│   ├── SocialSystems.md
│   ├── ProgressionSystems.md
│   ├── Monetization.md
│   ├── SuccessMetrics.md          # NEW P1
│   ├── ContentSafety.md           # NEW P1
│   ├── Prioritization.md          # NEW P2
│   ├── ReleaseStrategy.md         # RENAMED from product/Roadmap.md (§6.1)
│   └── Glossary.md                # domain terms (prioritize early)
│
├── architecture/
│   ├── Architecture.md
│   ├── TechStack.md
│   ├── DataModel.md               # local (Room)
│   ├── DataContracts.md           # NEW P1  Firestore schema/indexes/rules
│   ├── RepositoryRules.md         # + dependency-direction enforcement
│   ├── SyncStrategy.md
│   ├── FirebaseStrategy.md
│   ├── MultiplatformStrategy.md
│   ├── NavigationStrategy.md
│   ├── DesignSystem.md            # tokens + reusable components (§7.3)
│   ├── SecurityAndSecrets.md      # NEW P0
│   ├── Observability.md           # NEW P1  logging/errors/analytics/crash
│   ├── ConcurrencyAndState.md     # NEW P2
│   └── TestingStrategy.md
│
├── execution/
│   ├── SYSTEM.md                  # agent kernel (Phase 3 deep-dive)
│   ├── Workflow.md
│   ├── AgentGuardrails.md         # NEW P0
│   ├── DefinitionOfReady.md       # NEW P1
│   ├── DefinitionOfDone.md
│   ├── ReviewChecklist.md         # NEW P2
│   ├── StoryTemplate.md
│   ├── TaskTemplate.md
│   └── SprintRules.md
│
├── skills/                        # optional domain/ + activity/ grouping (§7.2)
│   ├── architecture.md  repository.md  sync.md  firebase.md
│   ├── multiplatform.md navigation.md  compose-ui-patterns.md
│   ├── presentation-layer.md  data-layer.md  koin-di.md
│   ├── debugging.md  refactoring.md  error-handling.md  planning.md
│
├── roadmap/
│   ├── STATE.md                   # NEW P0  current phase/sprint/next-ready pointer
│   ├── phases/  epics/  stories/  tasks/  sprints/   # empty until authorized
│
└── design/
    ├── MapScreen.md  LogFartScreen.md  SocialScreen.md
    ├── RankScreen.md  ProfileScreen.md
    ├── DesignPrinciples.md
    └── UXRules.md                 # per-screen = composition; DesignSystem = components
```

---

## 12) Change Summary

| Action | Items |
|---|---|
| **Add — P0** | `meta/INDEX.md`, `roadmap/STATE.md`, `architecture/SecurityAndSecrets.md`, `execution/AgentGuardrails.md` |
| **Add — P1** | `meta/decisions/` (ADRs), `product/SuccessMetrics.md`, `product/ContentSafety.md`, `architecture/Observability.md`, `architecture/DataContracts.md`, `execution/DefinitionOfReady.md` |
| **Add — P2** | `meta/PlanningGlossary.md`, `meta/ChangeLog.md`, `product/Prioritization.md`, `architecture/ConcurrencyAndState.md`, `execution/ReviewChecklist.md` |
| **Rename** | `product/Roadmap.md` → `product/ReleaseStrategy.md` (resolves collision with `roadmap/`) |
| **Restructure** | New `meta/` folder; optional `skills/{domain,activity}/`; split UI ownership `DesignSystem.md` ↔ `design/`; move existing 3 product docs into `product/` (in Phase 5) |
| **Remove** | None (lean as-is; resolve overlaps via ownership headers, not deletion) |

---

## 13) Open Questions for Phases 3–5

- **Phase 3** will validate whether `SYSTEM.md` + `Workflow.md` + `AgentGuardrails.md` + `INDEX.md` together are sufficient for full autonomy, or whether more agent-protocol detail is required.
- **Phase 4** will confirm the product dependency spine and decide where the new governance docs (`SuccessMetrics`, `ContentSafety`, `Prioritization`) sit in the build order.
- **Phase 5** will execute the physical move of the 3 product docs and create only `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md`.

---

## 14) Conclusion

The proposed structure is a solid foundation that needs **governance and navigation reinforcement**, not redesign. The decisive additions are the four **P0** docs — `meta/INDEX.md`, `roadmap/STATE.md`, `SecurityAndSecrets.md`, and `AgentGuardrails.md` — because they directly enable *safe, navigable autonomy*, which is the entire point of this planning system. With these in place, the structure satisfies all three tests: findability (INDEX), traceability (dependency spine + STATE), and non-contradiction (single-owner ownership map).

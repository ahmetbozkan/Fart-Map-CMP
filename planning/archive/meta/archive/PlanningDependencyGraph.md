# Planning Dependency Graph — Fart Map

> Phase 4 deliverable of `meta/archive/Planning-Rebuild.md`.
> Purpose: define the dependency order of product/planning artifacts so AI agents generate them in a sequence where every document rests on validated upstream inputs. Validate whether the brief's proposed order should change.
> No documents are created here except this map. (Phase 5 creates the first three downstream docs.)

---

## 1) Why a Dependency Graph Exists

In an autonomous system, generating a downstream document before its upstream inputs are stable produces **hallucinated rationale** — e.g., writing `Features.md` before `CoreLoops.md` forces an agent to invent the loops features must serve. The graph below makes those dependencies explicit so an agent always has the *justifying input* in context before it writes.

Two properties are required:
- **Acyclicity** — no document may depend (transitively) on itself. Cycles force guessing.
- **Single source of truth per fact** — each fact (a motivation, a metric, a term) is *defined* in exactly one document and *referenced* elsewhere.

---

## 2) The Brief's Proposed Chain (Restated)

```
Vision → Product Pillars → Personas → User Motivations → User Journeys
→ Core Loops → Features → Systems → Roadmap → Epics → Stories → Tasks
```

**Verdict:** the core spine is **correct and should be kept as the backbone.** It models the right causal flow: *why → what matters → who → what they want → how they move → repeatable engagement → capabilities → mechanisms → sequencing → execution.*

However, the linear chain is an **oversimplification** in three ways, corrected in §4:
1. Several governance documents (Glossary, SuccessMetrics, ContentSafety) are **cross-cutting** — they're not a single link in the chain but inputs to *many* links.
2. **Personas and Motivations are mutually reinforcing**, not strictly sequential.
3. **Systems are not strictly downstream of all Features** — social/progression systems co-evolve with the features that express them.

---

## 3) Node Inventory (Product Layer)

| Node | Doc | Defines (source of truth for) |
|---|---|---|
| Vision | `product/Vision.md` | Long-term purpose, ambition, north star |
| Pillars | `product/ProductPillars.md` | What matters / trade-off priorities / strategic phases |
| Personas | `product/Personas.md` | Who we serve, anti-personas |
| Glossary | `product/Glossary.md` | Domain terms (fart, signature, flavor, intensity, territory) |
| Motivations | `product/UserMotivations.md` | Why users act (drives, emotions, needs) |
| Journey | `product/UserJourney.md` | How users move through the product over time |
| Core Loops | `product/CoreLoops.md` | Repeatable engagement cycles (action→reward→reinvest) |
| Features | `product/Features.md` | Capabilities that enable loops/journeys |
| Social Systems | `product/SocialSystems.md` | Social mechanics (friends, sharing, reactions) |
| Progression Systems | `product/ProgressionSystems.md` | Ranks, streaks, unlocks |
| Monetization | `product/Monetization.md` | Revenue model |
| Success Metrics | `product/SuccessMetrics.md` | KPIs / measurable success (cross-cutting) |
| Content Safety | `product/ContentSafety.md` | Safety/moderation constraints (cross-cutting) |
| Prioritization | `product/Prioritization.md` | Scoring method (cross-cutting) |
| Release Strategy | `product/ReleaseStrategy.md` | Narrative phasing of releases |

Then the execution layer (`roadmap/`): Phases → Epics → Stories → Tasks → Sprints.

---

## 4) Corrected Dependency Graph

### 4.1 The spine (strict order)
```
Vision
  └─> ProductPillars
        └─> Personas  <──reinforces──>  UserMotivations
                                  └─> UserJourney
                                        └─> CoreLoops
                                              └─> Features
                                                    └─> Systems (Social, Progression, Monetization)
                                                          └─> ReleaseStrategy
                                                                └─> roadmap/phases
                                                                      └─> epics
                                                                            └─> stories
                                                                                  └─> tasks
                                                                                        └─> sprints
```

### 4.2 Cross-cutting inputs (feed many nodes, not a single link)
```
Glossary        ──────────► every product doc (defines shared vocabulary; needed early)
SuccessMetrics  ──────────► Features, Systems, ReleaseStrategy, Prioritization
ContentSafety   ──────────► Features, SocialSystems, UserJourney, design/*
Prioritization  ──────────► Features ranking, epics/stories ordering, sprints
```

### 4.3 Architecture & execution dependency (separate but parallel track)
The product spine feeds the **roadmap**, but the roadmap also depends on the **architecture** and **execution** layers being in place. These are not downstream of product — they are a parallel prerequisite for *implementation*, not for *product definition*.

```
Features + Systems ──┐
architecture/*  ─────┼──► roadmap/epics ──► stories ──► tasks ──► sprints
execution/* (kernel)─┘        (each work item links up to a feature/pillar
meta/INDEX + STATE ──────────────────────  per the Phase-3 traceability rule)
```

---

## 5) Validation: Should the Order Change?

| Proposed link | Keep? | Adjustment & rationale |
|---|---|---|
| Vision → Pillars | ✅ Keep | Pillars are derived trade-offs of the Vision. Strict. |
| Pillars → Personas | ✅ Keep | Who we serve follows from what matters. Strict. |
| Personas → Motivations | ⚠️ Soften to **bidirectional** | Personas describe *who*; motivations describe *why they act*. Each refines the other. Author Personas first (exists), then Motivations, then revisit Personas. |
| Motivations → Journey | ✅ Keep | Journeys are motivations expressed over time. Strict. |
| Journey → Core Loops | ✅ Keep | Loops are the repeatable engine inside the journey. Strict. |
| Core Loops → Features | ✅ Keep | Features exist to serve loops. Strict — this is the most important ordering to honor. |
| Features → Systems | ⚠️ Soften to **co-evolving** | Social/Progression systems are partly *defined by* features and partly *constrain* them. Draft Features first, then Systems, then reconcile Features. |
| Systems → Roadmap | ✅ Keep | Sequencing needs the full capability set. Strict. |
| Roadmap → Epics → Stories → Tasks | ✅ Keep | Standard decomposition. Strict. |
| (missing) Glossary placement | ➕ **Insert early** | Glossary should be authored right after Personas / alongside Motivations because every later doc uses its terms. The brief omits it from the chain. |
| (missing) Metrics/Safety/Prioritization | ➕ **Cross-cutting, author before Features** | These govern how Features and Systems are shaped and ranked; they must exist before Features to avoid rework. |

**Net change to the brief's order:** keep the spine; (a) insert **Glossary** early as a cross-cutting input; (b) author **SuccessMetrics, ContentSafety, Prioritization** before **Features**; (c) treat **Personas↔Motivations** and **Features↔Systems** as reconcile-after pairs rather than one-way edges.

---

## 6) Generation Order for Agents (Topologically Sorted)

This is the order an agent should create product documents, respecting all dependencies. **Phase 5 covers steps 4–6 only; everything after is out of scope for now.**

| Step | Document | Status / Authorization |
|---|---|---|
| 1 | Vision.md | ✅ Exists (review in Phase 5) |
| 2 | ProductPillars.md | ✅ Exists (review in Phase 5) |
| 3 | Personas.md | ✅ Exists (review in Phase 5) |
| 4 | **UserMotivations.md** | ▶ **Create in Phase 5** |
| 5 | **UserJourney.md** | ▶ **Create in Phase 5** |
| 6 | **CoreLoops.md** | ▶ **Create in Phase 5** — then STOP |
| 7 | Glossary.md | ⏸ Later (cross-cutting; can be drafted alongside 4–6 in a future phase) |
| 8 | SuccessMetrics.md | ⏸ Later (before Features) |
| 9 | ContentSafety.md | ⏸ Later (before social Features) |
| 10 | Prioritization.md | ⏸ Later (before ranking Features) |
| 11 | Features.md | ⛔ Explicitly forbidden until authorized |
| 12 | SocialSystems / ProgressionSystems / Monetization | ⛔ Later |
| 13 | ReleaseStrategy.md → roadmap/* | ⛔ Later (needs architecture + execution kernel first) |

---

## 7) Dependency Rules for Autonomous Agents

Encode these as machine-checkable rules (candidate for `execution/SYSTEM.md` / `meta/INDEX.md`):

1. **No-forward-reference rule:** an agent may not author a document whose upstream dependencies (per §4) are not yet marked complete.
2. **Cross-cutting-first rule:** Glossary, SuccessMetrics, ContentSafety, and Prioritization must exist before Features, because Features reference all four.
3. **Reconcile-pair rule:** after authoring Motivations, revisit Personas; after authoring Systems, revisit Features. Mark the upstream doc "reconciled" once consistent.
4. **Traceability rule (from Phase 3):** every roadmap work item must link up to a Feature → Core Loop → Pillar. A work item with a broken upward link is invalid.
5. **Single-definition rule:** a term/metric/motivation is defined once (its owning doc) and referenced elsewhere by link, never redefined.

---

## 8) Validation Gates Between Layers

Before crossing from one layer to the next, an agent runs a consistency check:

| Gate | From → To | Check |
|---|---|---|
| G1 | Motivations → Journey | Every journey stage maps to ≥1 motivation. |
| G2 | Journey → Core Loops | Every recurring journey segment is captured by a loop. |
| G3 | Core Loops → Features | Every feature serves ≥1 loop; every loop has ≥1 enabling feature. |
| G4 | Features → Systems | Every social/progression mechanic referenced by a feature is defined in a system doc. |
| G5 | Systems → Roadmap | Every feature/system has a place in a phase before stories are written. |
| G6 | Roadmap → Stories | Every story has a valid upward link (traceability rule) and meets Definition of Ready. |

Gates G3 and G6 are the highest-value: G3 prevents feature bloat (features with no loop), G6 prevents scope drift (work with no product justification).

---

## 9) Visual Summary

```
        ┌─────────────── cross-cutting governance ───────────────┐
        │  Glossary · SuccessMetrics · ContentSafety · Prioritization │
        └───────────────┬───────────────────────────┬────────────┘
                        │ (feed)                     │ (feed)
   Vision → Pillars → Personas ⇄ Motivations → Journey → CoreLoops → Features ⇄ Systems
                                                                          │
                                                                          ▼
                                              ReleaseStrategy → phases → epics → stories → tasks → sprints
                                                                          ▲
                        architecture/* + execution kernel + meta indices ─┘
                              (parallel prerequisite for implementation)
```

---

## 10) Conclusion

The brief's linear chain is a sound **backbone** and is preserved. The corrections are surgical: (1) add **cross-cutting governance** documents that feed many nodes rather than sitting at one link; (2) recognize two **reconcile-after pairs** (Personas↔Motivations, Features↔Systems) instead of strict one-way edges; (3) place **Glossary early** and **metrics/safety/prioritization before Features**; and (4) treat **architecture + execution + meta** as a parallel prerequisite for the roadmap, not a downstream product step.

For the immediate next phase, the order is unambiguous and authorized: **UserMotivations → UserJourney → CoreLoops**, after a review of the three existing product docs — then stop, exactly as the brief instructs.

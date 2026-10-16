# Prioritization: Fart Map

> **Planning spine position:** cross-cutting decision method; ranks everything downstream.
> Upstream: `Vision.md`, `ProductPillars.md`, `Personas.md`, `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md`, `Glossary.md`, `SuccessMetrics.md`, `ContentSafety.md`. Downstream: `Features.md`, Systems docs, `roadmap/*` (epics → stories), `execution/SprintRules.md`.
> This document makes feature/epic ranking **reproducible**: two agents scoring the same proposal should reach the same decision. It operationalizes the pillar scoring in `ProductPillars.md` §6 and the stage weighting in §10 into a single, auditable method (a gap flagged in `meta/archive/PlanningStructureReview.md` §10).

---

## 1) Purpose & How to Use

Fart Map's planning docs define *what matters* (pillars), *who* (personas), *why* (motivations), *what's measured* (metrics), and *what's forbidden* (safety). Prioritization is the **arithmetic that fuses them** into a ranked backlog so autonomous agents don't prioritize by intuition.

AI agents and planners use this document to answer:
- Given several proposals, which do we build first?
- Is this proposal's priority defensible and reproducible?
- Does this proposal even *qualify* for scoring (gates), or is it rejected outright?
- How does priority shift by strategic phase?

**Rules:**
- Every feature/epic gets a **PriorityScore** via the model in §4; ranking is by score within the current phase.
- **Gates (§3) run first.** A proposal failing a hard gate is rejected/redesigned *before* scoring — you never "score around" a safety or anti-pillar violation.
- Scores are **decision aids, not autopilots**: ties and edge cases defer to the conflict order in `ProductPillars.md` §5.
- All scoring is **recorded** (inputs + result + rationale) so future agents preserve consistency.

---

## 2) The Prioritization Pipeline

```
Proposal
  │
  ▼
[Step 1] HARD GATES  ──fail──►  Reject / Redesign  (no scoring)
  │ pass
  ▼
[Step 2] PILLAR SCORECARD (1–5 × 5 pillars)         ← ProductPillars.md §6
  │
  ▼
[Step 3] STAGE-WEIGHTED PILLAR VALUE                ← ProductPillars.md §10 weights
  │
  ▼
[Step 4] RICE-STYLE FACTORS (Reach, Impact, Confidence, Effort) ← SuccessMetrics.md
  │
  ▼
[Step 5] PERSONA & LOOP MULTIPLIERS                 ← Personas.md / CoreLoops.md
  │
  ▼
PriorityScore  ──►  Rank within current phase  ──►  Build now / later / reject
```

---

## 3) Step 1 — Hard Gates (Run Before Any Scoring)

A proposal must pass **all** gates to be scored. Failing any gate = reject or redesign.

| Gate | Source | Fail condition |
|---|---|---|
| **Safety gate** | `ContentSafety.md` | Creates an unmitigated threat (T1–T9) or lacks mandatory controls for its feature class (§9) |
| **Anti-pillar gate** | `ProductPillars.md` §7 | Pushes the product toward an anti-pillar (generic social, chat, utility, dating, productivity, health) |
| **Anti-persona gate** | `Personas.md` §3 | Strongest beneficiary is an anti-persona (utility seeker, dating user, spam grinder, etc.) |
| **Anti-motivation gate** | `UserMotivations.md` §9 | Strongest pull is an anti-motivation (doomscroll, compulsion, status-via-spam) |
| **Vision gate** | `Vision.md` §13/§17 | Contradicts "What Fart Map Is Not" or sacrifices vision for implementation convenience |
| **Trust gate** | `SuccessMetrics.md` §8 | Improves a KPI by breaching a trust counter-metric (pay-to-win, notif spam, dark pattern) |
| **Dependency gate** | `Glossary.md` §11 | Depends on a **TBD** term whose decision (e.g., Territory; the follow model is settled by ADR-0009) isn't recorded |

**Escalation:** if a proposal touches **minor safety, legal/compliance, or precise-location exposure**, it is not merely gated — it is escalated for human review (`ContentSafety.md` §5/§11).

---

## 4) The Scoring Model (Pillar-Weighted RICE)

For proposals that pass all gates:

```
PriorityScore = ( WeightedPillarValue × Reach × Impact × Confidence × LoopBonus × PersonaBonus )
                ─────────────────────────────────────────────────────────────────────────────────
                                              Effort
```

Each factor is defined below. Keep factors bounded so scores stay comparable.

### 4.1 WeightedPillarValue (strategic alignment)
1. Score the proposal 1–5 on each pillar (`ProductPillars.md` §6 definitions).
2. Multiply each by the **current stage weight** (`ProductPillars.md` §10).
3. Sum.

`WeightedPillarValue = Σ ( PillarScore_i × StageWeight_i )`

**Phase 1 weights** (`ReleaseStrategy.md` §3): Location 0.35, Humor 0.30, Social 0.20, Identity 0.10, Progress 0.05.
**Later phases:** use the per-phase weights in `ReleaseStrategy.md` §3.

> This is the mechanism that makes the same feature rank differently by phase — a Territory feature scores low in Phase 1 (Progress weighted 0.05) and higher in Phase 3.

### 4.2 Reach (how many users it touches)
From `SuccessMetrics.md` / persona breadth. Use a bounded scale:

| Reach | Meaning |
|---|---|
| 3 | Affects all/most users every session (e.g., core loop) |
| 2 | Affects a primary persona segment regularly |
| 1 | Affects a narrow/secondary segment or occasionally |

### 4.3 Impact (effect on a target metric)
The size of expected movement on the proposal's **primary metric** (`SuccessMetrics.md`):

| Impact | Meaning |
|---|---|
| 3 | Moves a Critical funnel (Activation/Habit) or the North Star materially |
| 2 | Moves a phase-lead KPI meaningfully |
| 1 | Minor or indirect metric effect |

### 4.4 Confidence (evidence quality)
Discount for uncertainty:

| Confidence | Meaning | Factor |
|---|---|---|
| High | Strong evidence (PoC-proven pattern, clear data, validated need) | 1.0 |
| Medium | Reasonable hypothesis | 0.7 |
| Low | Speculative | 0.4 |

### 4.5 Effort (cost)
Relative build cost including architecture/safety work (not just UI). Bounded:

| Effort | Meaning | Value |
|---|---|---|
| 1 | Small (days) | 1 |
| 2 | Medium (1–2 sprints) | 2 |
| 3 | Large (multi-sprint / new system) | 3 |
| 5 | Very large (cross-cutting platform work) | 5 |

> Effort must include the **mandatory safety controls** for the feature class (`ContentSafety.md` §9) — safety is not optional scope to be deferred to lower the estimate.

### 4.6 LoopBonus (compounding value)
From `CoreLoops.md` §9 interlock rule — reward features that strengthen loops and their interlocks:

| LoopBonus | Meaning |
|---|---|
| 1.5 | Strengthens a primary loop **and** an interlock |
| 1.2 | Strengthens one loop meaningfully |
| 1.0 | Touches no loop element (decorative — also a flag to question it) |

### 4.7 PersonaBonus (audience priority)
From `Personas.md` §5 priority tiers and §10 rule (benefit ≥2 primary personas):

| PersonaBonus | Meaning |
|---|---|
| 1.3 | Serves ≥2 **Primary** personas in the current phase |
| 1.1 | Serves 1 Primary persona |
| 1.0 | Serves only Secondary/Future personas (deprioritized unless Primary needs are met) |

---

## 5) Worked Example (Illustrative)

**Proposal:** "Reactions on Farts" — early-MVP phase.

1. **Gates:** passes all (with Block/Report required by `ContentSafety.md` §9 included in effort).
2. **Pillar scorecard:** Location 3, Social 5, Humor 4, Progress 2, Identity 3.
3. **WeightedPillarValue** (Phase 1 weights): (3×0.35)+(5×0.20)+(4×0.30)+(2×0.05)+(3×0.10) = 1.05+1.00+1.20+0.10+0.30 = **3.65**.
4. **RICE factors:** Reach 3 (every session), Impact 3 (First-Reward funnel — Critical), Confidence 1.0 (PoC-proven), Effort 2.
5. **Multipliers:** LoopBonus 1.5 (powers L2 + interlocks L4/L5), PersonaBonus 1.3 (Instant Joker + Social Reactor).

`PriorityScore = (3.65 × 3 × 3 × 1.0 × 1.5 × 1.3) / 2 = (3.65 × 9 × 1.95) / 2 ≈ 64.1 / 2 ≈ 32.0`

A high score, correctly reflecting that core social reaction is foundational in early phases. (Absolute numbers matter only **relative** to other proposals scored the same way.)

---

## 6) Decision Thresholds (Relative, Within Phase)

Because absolute scores depend on the scale, **rank relatively** within the current phase, then apply judgment bands:

| Band | Guidance |
|---|---|
| Top quartile of scored proposals | **Build now** — schedule into near sprints |
| Mid bands | **Build later** — backlog with clear phase trigger |
| Bottom quartile | **Redesign or defer** — weak alignment/impact |
| Failed a gate (§3) | **Reject** — never enters scoring |

Cross-check against `ProductPillars.md` §6 hard gates (e.g., Humor=1 **and** Social=1 → reject unless foundational infra; two pillars at 1 → redesign).

---

## 7) Phase-Aware Prioritization

Priority is **always computed for the current strategic phase** (`ProductPillars.md` §9). The same proposal is re-scored when the phase changes because:
- Stage **weights** change (§4.1).
- **Primary personas** change (`Personas.md` §6 phase coverage).
- **Lead metrics** change (`SuccessMetrics.md` §5).

**Rule (from Personas §5):** never rank a Secondary/Future-persona proposal above an unmet Primary-persona need in the current phase, regardless of raw score. The PersonaBonus encodes this, but it is also a hard override.

---

## 8) Tie-Breaking & Conflict Resolution

When scores are close or factors conflict, resolve in this order (inherited from `ProductPillars.md` §5):

1. **Vision alignment**
2. **User enjoyment**
3. **Social value**
4. **Progression value**
5. **Development simplicity** (prefer simpler only when 1–4 are equal)

Additional tie-breakers:
- Prefer the proposal that **defends a Critical drop-off** (`UserJourney.md` §6: Activation/Habit).
- Prefer the proposal that **closes an open loop** over one that adds a new loop (`CoreLoops.md` §10).
- Prefer **multi-pillar / multi-persona** breadth (Pillars §4, Personas §10).

---

## 9) What Prioritization Does NOT Do

- It does **not** override safety, anti-pillar, or trust gates — those are absolute.
- It does **not** generate scope — it ranks proposals derived from upstream product docs.
- It does **not** replace estimation/capacity planning — that lives in `execution/SprintRules.md` (this produces the *ordering*; sprint rules decide *how much* fits).
- It does **not** lock decisions — re-score on phase change or new evidence.

---

## 10) AI Decision Guidance (Mandatory for Autonomous Planning)

1. **Run gates (§3) before scoring.** A gate failure ends evaluation — never score around it.
2. Escalate (don't autonomously rank/ship) anything touching **minor safety, legal, or precise location**.
3. Compute **PriorityScore** with current-phase weights; re-score when the phase changes.
4. Include **mandatory safety controls** in Effort — never deflate estimates by deferring safety.
5. Honor the **Primary-over-Secondary** override even when a Secondary proposal scores higher.
6. Record every scoring (inputs, score, rationale) for consistency and future audit (feeds `meta/decisions/` when a notable trade-off is made).
7. Use scores to **rank**, then apply the **tie-break order (§8)** and human-judgment bands (§6) — scores inform, they don't dictate blindly.
8. Flag any proposal with **LoopBonus = 1.0** (touches no loop) as a candidate to cut or rethink.

---

## 11) Prioritization Record Template (Apply per Epic / Feature)

```
Proposal: <name>
Phase: <current strategic phase>
Gates: [Safety / Anti-pillar / Anti-persona / Anti-motivation / Vision / Trust / Dependency] → PASS/FAIL (+ notes)
Pillar scorecard: L_ S_ H_ P_ I_
WeightedPillarValue: <sum with stage weights>
Reach: _  Impact: _  Confidence: _  Effort: _
LoopBonus: _  PersonaBonus: _
PriorityScore: <computed>
Primary metric targeted: <from SuccessMetrics.md>
Counter-metric guardrail: <from SuccessMetrics.md §8>
Decision: Build now / later / redesign / reject
Rationale: <1–3 sentences>
```

> This template is the prioritization artifact every epic/feature carries into `roadmap/`. It is the evidentiary basis an agent uses to justify sequencing without a human.

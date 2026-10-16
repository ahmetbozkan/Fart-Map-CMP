# Success Metrics: Fart Map

> **Planning spine position:** cross-cutting measurement layer; feeds prioritization and roadmap.
> Upstream: `Vision.md`, `ProductPillars.md`, `Personas.md`, `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md`, `Glossary.md`. Downstream: `Prioritization.md`, `Features.md`, Systems docs, `roadmap/*`.
> This document defines **how Fart Map measures success** so AI agents can prioritize objectively and know when something is "done at the product level" (a gap flagged in `meta/archive/PlanningStructureReview.md` §10 and `meta/archive/AIOperatingSystemReview.md`).

---

## 1) Purpose & How to Use

Pillars define *what matters*; motivations/journeys/loops define *the mechanics*; this document defines *the measurable proof those mechanics work*. Without target metrics, agents cannot rank features, detect leaks, or decide build/iterate/kill.

AI agents and planners use this document to answer:
- What signal proves a feature/loop/stage is working?
- What is the target, and what counts as failure?
- Which metric does a proposed feature intend to move?
- Are we measuring the right thing for the current strategic phase?

**Rules:**
- Every feature proposal must name the **primary metric** it intends to move (ties to `Prioritization.md`).
- Every Journey stage success-signal and Core Loop health-bar maps to a metric here (single-source consistency).
- Metrics are **directional targets**, not contractual guarantees; refine with real data. Pre-launch targets are hypotheses to validate.
- Avoid vanity metrics (raw installs, total farts) as success criteria — prefer **rate** and **retention** metrics.

---

## 2) Metric Conventions

| Field | Meaning |
|---|---|
| **Metric** | The measurable signal |
| **Definition** | Precise calculation (uses `Glossary.md` terms) |
| **Target** | Directional goal (provisional until real data) |
| **Maps to** | Motivation / Loop / Journey stage it validates |
| **Counter-metric** | A guardrail metric that must NOT degrade (prevents gaming) |

**Tiers:** **North Star** (one) → **Primary KPIs** (per outcome) → **Loop/Stage diagnostics** (operational) → **Counter-metrics** (guardrails).

---

## 3) North Star Metric

> **North Star: Weekly Active Sharers-and-Reactors (WASR)** — the count of users who, in a 7-day window, both (a) return and (b) participate in a social or sharing action (Reaction, Comment, or external Share).

**Why this one:**
- It captures the intersection of the product's three success engines: **retention** (return), **social** (Pillars: Social/Humor), and **virality** (Share). A user who only logs once is not success; a user who returns *and* socially participates is.
- It resists vanity gaming: raw installs or solo logging don't move it.
- It directly expresses the Vision: "make people laugh **together**, every day."

**Counter-metrics for the North Star:** D1/D7 retention must not be bought with spam notifications (see §8 trust guardrails); WASR growth must not come with rising report/block rates (`ContentSafety.md`).

---

## 4) Primary KPIs by Business Outcome

Mapped to the outcome model in `UserMotivations.md` §7.

| Outcome | Primary KPI | Definition | Provisional target | Maps to |
|---|---|---|---|---|
| **Activation** | First-session loop completion | % of new users who complete `map → log → marker` in session 1 | ≥ 60% | L1, Journey S3, M1/M3 |
| **First Reward** | First social touch | % of new users who give or receive a Reaction within 24h | ≥ 40% | L2, Journey S4, M2 |
| **Retention** | D1 / D7 / D30 return | % returning 1/7/30 days after install | ≥ 40 / 20 / 10% | L5/L8, Journey S5, M8 |
| **Habit** | Streak adoption | % of D7-retained users with an active streak ≥ 3 | ≥ 30% | L5, Journey S5, M8 |
| **Social density** | Follow-graph formation | % of retained users with ≥ 3 connections | ≥ 50% | L4, Journey S6, M2 |
| **Virality** | K-factor | New installs attributable per existing user (share → install) | ≥ 0.4 (→ 1.0 long-term) | L8, Journey S8, M7 |
| **Identity** | Profile investment | % of retained users who customize Signature/Profile | ≥ 35% | Identity accretion, Journey S7, M4 |
| **Monetization** | Conversion / ARPDAU | % paying; avg revenue per daily active user | set post-PMF | M4/M6, Personas §8 |

> Targets are **provisional hypotheses** for a pre-launch product. The first job after launch is to replace them with observed baselines, then set improvement goals.

---

## 5) Metric → Strategic Phase Focus

Each phase optimizes a different metric set (aligned to `ProductPillars.md` §9–10 and `UserJourney.md` §4). Do not over-optimize a later-phase metric at the expense of the current phase's.

| Phase | Lead metric(s) | What we're proving | Secondary watch |
|---|---|---|---|
| **1 Core Loop** | Activation (loop completion), First social touch | The core loop is fun and place-anchored | Logging time, map-density perception |
| **2 Social Retention** | D1/D7 retention, Follow-graph formation, WASR | People bring people back | Notification value (open rate w/o churn) |
| **3 Progression** | Streak adoption, D30 retention | Progression turns engagement into habit | Rank fairness / abuse rate |
| **4 Community** | Group participation, K-factor, content per group | Community creates self-sustaining content | Moderation load, event consistency |

---

## 6) Loop Health Metrics (Operational Diagnostics)

Each Core Loop (`CoreLoops.md`) gets measurable health signals. A loop failing these is a priority defect (per `CoreLoops.md` §10).

| Loop | Health metric | Pass signal | Leak signal |
|---|---|---|---|
| **L1 Explore→Log→See** | Time-to-first-log; logs/session | <60s first log; healthy repeat logs | High log abandonment, empty-map sessions |
| **L2 Social Reaction** | Reaction reciprocity rate | Farts receive reactions; reactors return | "Void" farts (0 reactions), notif ignored |
| **L3 Discovery** | Map sessions / nearby opens; freshness | Recurring map-watch; fresh nearby content | Static-map sessions, low pan/zoom |
| **L4 Social Connection** | Connections added / week | Graph grows past 3+ | Stalled graph, failed people discovery |
| **L5 Progression/Streak** | Streak continuation rate | Streaks extend day-over-day | Mass streak breaks, grind complaints |
| **L6 Competition** | Rank-check frequency; season participation | Recurring rank engagement | Rank apathy, abuse reports |
| **L7 Community** | Active groups; events completed | Recurring event cadence | Dormant groups, event drop |
| **L8 Viral Share** | Shares/user; share→install conversion | K-factor climbs | Shares with no installs (weak artifact) |

---

## 7) Journey Stage Metrics (Drop-off Instrumentation)

Maps to `UserJourney.md` §6 drop-off map. Funnel conversion between stages is the key instrument.

| Stage transition | Conversion metric | Priority |
|---|---|---|
| Awareness → Acquisition | Share-view → install rate | High (virality) |
| Acquisition → Activation | Install → first-log rate | **Critical** |
| Activation → First Reward | First-log → first-reaction rate | High |
| First Reward → Habit | Session-1 → D1 return | **Critical** |
| Habit → Social Embedding | D7 → follow-approved rate | Medium |
| Social → Identity | Follow → profile-customized rate | Medium |
| Identity → Advocacy | Milestone → external-share rate | Medium |

**Rule:** the two **Critical** transitions (Activation and Habit) are the make-or-break funnels (`UserJourney.md` §6). Instrument and defend them first.

---

## 8) Counter-Metrics & Trust Guardrails

Growth metrics must never be bought by harming trust (Vision §17). These guardrails cap how success may be pursued.

| Pursued metric | Forbidden side effect | Guardrail counter-metric |
|---|---|---|
| Retention via notifications | Notification spam / annoyance | Notification opt-out rate, mute rate must stay low |
| Streak/habit (M8) | Dark-pattern compulsion | Negative-sentiment / uninstall-after-nag rate |
| WASR / engagement | Toxic interactions | Report rate, block rate, harassment reports (`ContentSafety.md`) |
| Rank/competition (M6) | Cheating / pay-to-win | Abuse-detection rate, fairness complaints |
| Virality (K-factor) | Spammy invite mechanics | Invite-spam reports, low-quality install churn |
| Logs/farts volume | Spam logging | % low-quality/duplicate farts; spam-grinder signals |

**Rule:** if a feature improves a primary KPI but degrades its counter-metric beyond threshold, it must be **redesigned or rejected** (mirrors Pillars anti-pattern logic).

---

## 9) Metric Anti-Patterns (Do NOT Use as Success Criteria)

| Vanity metric | Why it misleads | Use instead |
|---|---|---|
| Total installs | Ignores activation/retention | Activation rate, D7 retention |
| Total farts logged | Rewards spam; solo activity | Logs per *retained* user; WASR |
| Raw DAU | Can be inflated by notifications | WASR (active *participation*) |
| Total reactions | Gameable | Reaction reciprocity rate |
| Time in app | Time-sink ≠ value (anti-motivation) | Sessions with a completed loop |

---

## 10) Measurement Architecture Notes (Hand-off to Architecture)

These are **requirements for `architecture/Observability.md`**, not implementation here:

- An **analytics event taxonomy** must instrument each Loop element (Trigger/Action/Reward/Reinvestment) and each Journey stage transition.
- The PoC declared but **did not use** `firebase-analytics` (Audit §5). The new project must adopt analytics **intentionally** with this metric set as its spec, or drop the dependency.
- Events must be **privacy-respecting** (no precise location in analytics payloads — see `ContentSafety.md`).
- Counter-metrics (reports, blocks, opt-outs) require their own instrumented events, not just growth events.

---

## 11) AI Decision Guidance (Mandatory for Autonomous Planning)

1. Every feature proposal names a **primary metric** it intends to move and the **counter-metric** it must not harm.
2. Optimize the **current phase's lead metrics** (§5); never trade them for later-phase metrics.
3. Treat **Activation** and **Habit** funnel transitions as Critical; prioritize their defense.
4. Reject features that move a vanity metric (§9) without moving a real KPI.
5. Reject/redesign features that improve a KPI while breaching a **trust guardrail** (§8).
6. Map every new Loop/Stage element to a metric here; if it can't be measured, question whether it's real.
7. Keep targets as **provisional hypotheses** pre-launch; first post-launch task is to baseline, then set goals.
8. Feed these metrics into `Prioritization.md` as the "Reach/Impact" evidence base for scoring.

---

## 12) Metric Usage Checklist (Apply per Epic / Feature)

- [ ] Primary metric named?
- [ ] Counter-metric / guardrail named?
- [ ] Maps to a Loop health metric and/or Journey transition?
- [ ] Phase-appropriate lead metric (§5)?
- [ ] Not a vanity metric (§9)?
- [ ] Measurable via the analytics taxonomy (hand-off to `Observability.md`)?
- [ ] Provisional target stated (hypothesis to validate)?

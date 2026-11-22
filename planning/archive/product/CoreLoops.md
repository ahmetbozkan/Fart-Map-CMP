# Core Loops: Fart Map

> **Planning spine position:** the repeatable engines that make the product alive.
> Upstream: `Vision.md`, `ProductPillars.md`, `Personas.md`, `UserMotivations.md`, `UserJourney.md`. Downstream: `Features.md`, Systems docs, `roadmap/*`, `design/*`.
> Core loops are the *machinery* that satisfies motivations (`UserMotivations.md`) inside the session journey (`UserJourney.md` §5). Downstream: `Features.md`, the Systems docs and `roadmap/*` reference the loop ids defined here.

---

## 1) Purpose & How to Use

A **core loop** is a repeatable cycle of `Action → Reward → Reinvestment` that a user runs again and again, each pass making the next one more compelling. Loops — not features — are what produce retention, habit, and virality. Features exist to *power* loops; if a feature doesn't strengthen a loop, it is decorative.

AI agents and planners use this document to answer:
- Which loop does a feature feed? (If none, question it.)
- Is a loop *closed* (does the reward reinvest into the next action)?
- Which loop is under-built in the current phase?
- Where does a loop leak (drop the user instead of re-engaging them)?

Loops are validated against all upstream docs. They introduce **no new scope** — they are the structural expression of motivations and journeys.

---

## 2) Loop Model Conventions

Every loop is specified with a strict, AI-checkable structure:

| Element | Definition | Failure if missing |
|---|---|---|
| **Trigger** | What starts the loop (internal urge or external prompt) | Loop never begins; no re-entry |
| **Action** | The core thing the user does | No participation |
| **Reward** | The payoff (variable/social/progress) | Novelty churn; user stops |
| **Reinvestment** | How the reward loads the *next* trigger | Loop is open; runs once, then dies |
| **Loop closure** | The reinvestment must feed a future trigger | Open loop = leak |

**Loop health rules:**
- A loop is **closed** only if Reinvestment plausibly produces the next Trigger. Open loops are bugs.
- Rewards should be at least partly **variable** (uncertain) to sustain engagement — but never at the cost of fairness or trust (Vision §17).
- A loop must serve at least one **motivation** (M1–M8) and advance at least one **journey stage**.

### Loop Tiers
- **Primary loops** — run every session; the product dies without them. (Phase 1–2.)
- **Secondary loops** — run across days/weeks; create habit and depth. (Phase 2–3.)
- **Tertiary loops** — run across weeks/seasons; create community & network effects. (Phase 3–4.)
- **Meta loop** — the viral loop binding everything to growth.

---

## 3) Loop Map Overview

```
PRIMARY (per session)        SECONDARY (per day/week)        TERTIARY (per week/season)
─────────────────────        ────────────────────────        ──────────────────────────
L1 Explore→Log→See           L4 Social Connection            L6 Competition / Ranking
L2 Social Reaction           L5 Progression / Streak         L7 Community / Events
L3 Discovery / Map-watch     (Identity accretion)            (Seasons)          

                         META: L8 Viral Share Loop  ──► feeds new users into L1
```

Dependency: secondary loops require a healthy L1–L3; tertiary loops require healthy L4–L5; the meta loop requires shareable output from any loop.

---

## 4) Primary Loops (Per Session — Phase 1–2)

### L1 — Explore → Log → See (The Foundational Loop)
The single most important loop. Phase 1 success is defined by it (Pillars §9; Journey Stage 3).

| Element | Detail |
|---|---|
| **Trigger** | Opening the app; being somewhere; an urge to mark a funny moment (M1, M3) |
| **Action** | Log a fart at the current location (fast, one-tap-class) |
| **Reward** | Marker appears instantly on the living map; immediate comedic satisfaction |
| **Reinvestment** | The new marker enriches the map → invites more exploration → primes the next log |
| **Closure** | ✅ Closed: a denser, livelier map increases the urge to explore and log again |

**Motivations:** M1 Instant Amusement, M3 Curiosity. **Journey stages:** 3 Activation, 4 First Reward.
**Pillars:** Location (5), Humor (5).
**Leak points:** empty map, logging friction, slow/dull marker feedback → Activation drop-off (Journey §6 Critical).
**Health bar:** must complete in <60s on first session (Vision §16).

---

### L2 — Social Reaction Loop
Converts solitary logging into shared entertainment (Journey Stage 4).

| Element | Detail |
|---|---|
| **Trigger** | Seeing others' farts nearby/in feed; a notification that someone reacted to yours (M2) |
| **Action** | React / comment on a fart (low effort) |
| **Reward** | Reciprocal reactions/replies; the feeling of being seen; shared laughter (M2, M7) |
| **Reinvestment** | Each reaction notifies the other user → pulls them back → generates new reactions |
| **Closure** | ✅ Closed: reactions beget notifications beget return visits beget reactions |

**Motivations:** M2 Belonging, M7 Social Currency. **Journey stages:** 4, 6.
**Pillars:** Social (5), Humor (4).
**Leak points:** action ignored (no reciprocity), dead nearby activity → "void" feeling (Motivations M2 failure).
**Health:** reaction reciprocity rate positive; ≥40% of new users give or receive a reaction within 24h (`SuccessMetrics.md` §4, §6).

---

### L3 — Discovery / Map-Watch Loop
The exploration engine that keeps the map a living world (Journey Stage 2–3, Explorer persona).

| Element | Detail |
|---|---|
| **Trigger** | Curiosity — "what happened near me / here / where I'm traveling?" (M3) |
| **Action** | Pan/zoom the map; open nearby activity; check a new place |
| **Reward** | Fresh, place-anchored discoveries and stories |
| **Reinvestment** | New places & fresh activity create new curiosity → next watch session |
| **Closure** | ✅ Closed *iff* the map stays fresh; static map = open/dead loop |

**Motivations:** M3 Curiosity. **Journey stages:** 2, 3.
**Pillars:** Location (5).
**Leak points:** sparse/static map; map demoted behind a feed (Personas Explorer churn). **Map vitality is a hard product constraint.**

---

## 5) Secondary Loops (Per Day/Week — Phase 2–3)

### L4 — Social Connection Loop
Builds the graph that powers retention (Journey Stage 6).

| Element | Detail |
|---|---|
| **Trigger** | Followed-user activity; discovery suggestions; an invite (M2) |
| **Action** | Send follow requests; engage with their content; invite others |
| **Reward** | Denser personal feed, more reciprocal interaction, social belonging |
| **Reinvestment** | More approved follows → more activity → more triggers for L2 and return |
| **Closure** | ✅ Closed: graph density compounds social pull |

**Motivations:** M2, M6, M7. **Journey stage:** 6. **Pillars:** Social (5), Identity (4).
**Leak points:** dead graph, hard discovery (Journey G2 gate).

---

### L5 — Progression / Streak Loop
The habit engine that survives novelty (Journey Stage 5).

| Element | Detail |
|---|---|
| **Trigger** | Streak at risk; near-complete achievement; daily reset (M8, M5) |
| **Action** | Return and perform a qualifying action (log/react) to advance |
| **Reward** | Visible progress: streak extended, badge earned, stat grown |
| **Reinvestment** | Longer streak/history raises the stake → stronger next-day trigger (loss aversion) |
| **Closure** | ✅ Closed: accumulated progress increases return pressure |

**Motivations:** M5 Progress, M8 Habit. **Journey stage:** 5. **Pillars:** Progress (5), Identity (4).
**Guardrail:** reward *meaningful* participation, not spam; use loss-aversion ethically (Motivations §8, M8 failure mode). **Leak points:** grind-for-grind, unfair/opaque progress.

> **Identity accretion** runs alongside L5: every loop pass adds to profile history, feeding M4 Self-Expression and Journey Stage 7. It is not a standalone session loop but a continuous accumulation that makes L5/L6/L7 meaningful.

---

## 6) Tertiary Loops (Per Week/Season — Phase 3–4)

### L6 — Competition / Ranking Loop
Status engine for the Rival persona (Journey Stage 7).

| Element | Detail |
|---|---|
| **Trigger** | Rank movement; rival activity; season window; a challenge (M6) |
| **Action** | Compete — climb leaderboards, defend rank, accept challenges |
| **Reward** | Visible standing, acknowledgment, prestige |
| **Reinvestment** | Rank is defendable/losable → ongoing pressure; seasons reset the chase |
| **Closure** | ✅ Closed: standings + seasons create perpetual re-entry |

**Motivations:** M6 Status, M5. **Journey stage:** 7. **Pillars:** Progress (5), Social (4), Identity (4).
**Guardrail:** fairness & anti-abuse are first-class; **no pay-to-win** (Personas §7). **Leak points:** opaque/cheatable ranking.

---

### L7 — Community / Events Loop
Network-effects engine for the Community Organizer (Journey Stage 6–8, Phase 4).

| Element | Detail |
|---|---|
| **Trigger** | Group activity; event cycle; shared goal; organizer prompt (M2, M6) |
| **Action** | Join/run groups, participate in events/challenges, pursue shared goals |
| **Reward** | Belonging, collective achievement, visible leadership impact |
| **Reinvestment** | Recurring events create social rhythm → members reactivate each other |
| **Closure** | ✅ Closed: event cadence + group identity create self-sustaining content |

**Motivations:** M2, M6, M7. **Journey stage:** 6–8. **Pillars:** Social (5), Progress (4), Identity (4).
**Phase-gated:** requires established graph & tooling (Journey G2/G3; Personas Future tier). **Leak points:** shallow group tools, inconsistent events.

---

## 7) The Meta Loop

### L8 — Viral Share Loop (Binds All Loops to Growth)
The loop that turns output from L1–L7 into new users (Journey Stage 8 → Stage 1).

| Element | Detail |
|---|---|
| **Trigger** | A screenshot-worthy moment, milestone, rank climb, or event outcome (M7) |
| **Action** | Share externally (group chat, short-form platforms) |
| **Reward** | External laughs, replies, social status (social currency) |
| **Reinvestment** | Recipients become curious → install → enter L1 → eventually produce their own shares |
| **Closure** | ✅ Closed: Advocacy feeds Awareness — the growth flywheel (Vision §10–11) |

**Motivations:** M7 Social Currency, M4, M6. **Journey stages:** 8 → 1. **Pillars:** Humor (5), Social, Identity.
**Health:** shared artifacts must be understandable out of context and meme-native. **Leak points:** nothing worth sharing; moments that need explaining.

---

## 8) Loop → Motivation → Journey → Persona → Pillar Matrix

| Loop | Tier | Lead motivations | Journey stage(s) | Primary personas | Dominant pillars | Phase |
|---|---|---|---|---|---|---|
| L1 Explore→Log→See | Primary | M1, M3 | 3, 4 | Instant Joker, Explorer | Location, Humor | 1 |
| L2 Social Reaction | Primary | M2, M7 | 4, 6 | Instant Joker, Social Reactor | Social, Humor | 1–2 |
| L3 Discovery/Map-Watch | Primary | M3 | 2, 3 | Explorer | Location | 1 |
| L4 Social Connection | Secondary | M2, M6, M7 | 6 | Social Reactor, Community Org. | Social, Identity | 2 |
| L5 Progression/Streak | Secondary | M5, M8 | 5 | Rival, Identity Builder | Progress, Identity | 2–3 |
| L6 Competition/Ranking | Tertiary | M6, M5 | 7 | Rival | Progress, Social, Identity | 3 |
| L7 Community/Events | Tertiary | M2, M6, M7 | 6–8 | Community Organizer | Social, Progress | 4 |
| L8 Viral Share (meta) | Meta | M7, M4, M6 | 8→1 | Instant Joker, Identity Builder | Humor, Social, Identity | All |

---

## 9) Loop Interlock (How Loops Feed Each Other)

Loops are not independent; the product's strength is their interlock:

```
L3 Discovery ─► surfaces content ─► L1 Explore→Log→See ─► creates markers
   ▲                                      │
   │                                      ▼
   └──────── richer map ◄──── L2 Social Reaction ─► notifications ─► L4 Social Connection
                                          │                              │
                                          ▼                              ▼
                              L5 Progression/Streak ─► L6 Competition ─► L7 Community
                                          │                              │
                                          └──────────► L8 Viral Share ◄──┘ ─► new users ─► L1
```

| Interlock | Effect |
|---|---|
| L1 → L3 | Each log enriches the map, fueling more discovery |
| L1 → L2 | Logged content is the substrate reactions act on |
| L2 → L4 | Reactions reveal people worth connecting to |
| L2 → L5 | Qualifying actions advance progression |
| L4 → L2 | More approved follows → more reaction triggers |
| L5 → L6 | Accumulated progress becomes competitive standing |
| L6/L7 → L8 | Rank climbs & event wins become shareable artifacts |
| L8 → L1 | New users re-enter the foundational loop |

**Rule:** a feature that strengthens one loop *and* an interlock is higher value than one that strengthens a single loop in isolation (mirrors Pillars §4 multi-pillar rule).

---

## 10) Loop Health Diagnostics (For Autonomous Audits)

An agent can audit any loop with these checks:

| Check | Pass condition |
|---|---|
| Closed? | Reinvestment plausibly produces the next Trigger |
| Variable reward? | At least partly uncertain/novel (without harming fairness) |
| Serves a motivation? | Maps to ≥1 of M1–M8 |
| Advances a journey stage? | Maps to ≥1 lifecycle stage |
| Phase-appropriate? | Matches current strategic phase emphasis |
| No leak? | Drop-off points identified and defended |
| Ethical? | No dark patterns; fairness & trust preserved (Vision §17) |
| Interlocked? | Feeds or is fed by ≥1 other loop |

A loop failing "Closed?" or "No leak?" is a **priority defect** — open/leaking loops waste all the demand upstream docs generate.

---

## 11) AI Decision Guidance (Mandatory for Autonomous Planning)

1. For every feature, name the **loop(s)** it powers and the **element** it strengthens (Trigger/Action/Reward/Reinvestment). A feature touching no loop element is decorative — question it.
2. Prioritize **L1, L2, L3** (primary loops) in Phase 1; nothing else matters until these are healthy and closed.
3. Do not build **secondary/tertiary** loops on top of unhealthy primary loops (respect Journey gates §8).
4. Prefer features that strengthen a loop **and** an interlock (§9) over single-loop features.
5. Every loop must be **closed** before ship; an open loop is a defect, not an MVP.
6. Keep rewards partly **variable** but never sacrifice fairness/trust for engagement (esp. L5, L6, L8).
7. Protect the **meta loop (L8)**: shareable, context-free, meme-native output is the growth engine.
8. Run the **loop health diagnostics** (§10) on any proposed change before approval.
9. When loops compete for investment, weight by current strategic phase (Pillars §9–10) and Primary-persona coverage (Personas §5).
10. Downstream documents (`Features.md`, Systems docs, roadmap) now exist; this document remains the canonical loop registry they must reference.

---

## 12) Loop Usage Checklist (Apply per Epic / Feature)

- [ ] Which loop(s) does this power?
- [ ] Which loop element does it strengthen?
- [ ] Is the loop closed (reinvestment → next trigger)?
- [ ] Does it serve a motivation (M1–M8) and a journey stage?
- [ ] Phase-appropriate per the loop tier?
- [ ] Does it strengthen an interlock (§9)?
- [ ] Any leak introduced or fixed?
- [ ] Ethical (no dark patterns; fairness preserved)?
- [ ] Primary personas served before Secondary/Future?

---

## 13) Phase Boundary Note (Historical)

At the time this document was authored, `meta/archive/Planning-Rebuild.md` Phase 5 required stopping after CoreLoops. Since then, explicit user instruction authorized creation of `Glossary.md`, `SuccessMetrics.md`, `ContentSafety.md`, `Prioritization.md`, and `Features.md`.

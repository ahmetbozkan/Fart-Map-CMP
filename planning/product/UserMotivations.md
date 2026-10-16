# User Motivations: Fart Map

> **Planning spine position:** the *why behind the who*.
> Upstream: `Vision.md`, `ProductPillars.md`, `Personas.md`. Downstream: `UserJourney.md`, `CoreLoops.md`, `Features.md`.
> Reconcile-after pair with `Personas.md` (per `meta/archive/PlanningDependencyGraph.md` §5): after this document, Personas should be revisited for consistency.

---

## 1) Purpose & How to Use

This document defines **why users act** inside Fart Map — the psychological drives that cause install, first action, return, interaction, and sharing. Personas describe *who*; motivations describe the *forces* that move them.

AI agents and planners use this document to answer:
- What underlying drive does a feature satisfy? (If none, it is off-strategy.)
- Which motivation is under-served in the current phase?
- Why would a user return tomorrow, not just today?
- Which motivations convert to retention vs. virality vs. monetization?

Motivations are validated against `Vision.md` and `ProductPillars.md`. If a motivation conflicts with vision or pillars, vision and pillars win. Motivations do **not** introduce new product scope; they explain the demand that features must serve.

---

## 2) Motivation Model Conventions

Fart Map motivations are grouped using a **Drive → Trigger → Payoff → Reinforcement** model:

| Element | Meaning | Why it matters for design |
|---|---|---|
| **Drive** | The standing psychological need (e.g., belonging) | Determines whether demand is durable or one-off |
| **Trigger** | What activates the drive in a session (e.g., a notification about a followed user) | Determines how to bring users back |
| **Payoff** | The reward that satisfies the drive (e.g., a laugh, a rank-up) | Determines whether the loop closes |
| **Reinforcement** | What makes the user repeat it (e.g., visible streak) | Determines long-term retention |

A motivation is "well-served" only when all four elements exist in the product. A feature that creates a Drive/Trigger but no Payoff produces frustration; a Payoff with no Reinforcement produces novelty churn.

### Motivation Strength (1–5)
How strongly Fart Map should lean on this motivation.
- 1 = Marginal; nice-to-have
- 3 = Meaningful retention contributor
- 5 = Core engine; product fails without it

### Motivation Type
- **Intrinsic** = self-driven (fun, curiosity, mastery, expression)
- **Extrinsic** = externally driven (status, competition, social obligation)
- Fart Map should lead with **intrinsic** motivations and use extrinsic ones as accelerators, never as the only reason to return (avoids hollow, grind-driven churn).

---

## 3) Core Motivations

Each motivation maps to pillars (from `ProductPillars.md`) and primary personas (from `Personas.md`).

---

### M1 — Instant Amusement ("make me laugh now")
**Drive:** the need for fast, low-effort emotional relief and play.
**Trigger:** opening the app; seeing an absurd nearby marker; a funny push notification.
**Payoff:** a genuine laugh within seconds of the first action.
**Reinforcement:** the map keeps producing fresh absurd moments; reacting feels instantly rewarding.

| Type | Strength | Pillars | Primary personas |
|---|---|---|---|
| Intrinsic | 5 | Humor, Location | Instant Joker, Social Reactor |

**Design implication:** the first 10 seconds must deliver humor with zero setup. Any friction here destroys the foundational motivation.
**Failure mode:** the app is "interesting" but not *funny*; humor becomes a concept, not an experience.

---

### M2 — Belonging & Social Pull ("be in the loop with my people")
**Drive:** the need to participate in a shared group experience and not be left out.
**Trigger:** posts from followed users; reaction/comment on the user's content; "someone you follow just…" notification.
**Payoff:** reciprocal interaction — being seen and responded to by people who matter.
**Reinforcement:** ongoing comment threads, followed-user activity density, social obligation to reply.

| Type | Strength | Pillars | Primary personas |
|---|---|---|---|
| Intrinsic + Extrinsic | 5 | Social, Humor | Social Reactor, Community Organizer |

**Design implication:** social feedback must be reliable, timely, and visible. A dead follow graph kills this motivation faster than any other.
**Failure mode:** broadcast-only posting with no reciprocal loop; the user talks into a void.

---

### M3 — Curiosity & Discovery ("show me what happened where")
**Drive:** the exploratory urge to uncover what is happening in real-world places.
**Trigger:** opening the map; arriving somewhere new; travel; local curiosity.
**Payoff:** discovering fresh, place-anchored activity and stories.
**Reinforcement:** the map stays alive and changes; new places yield new discoveries.

| Type | Strength | Pillars | Primary personas |
|---|---|---|---|
| Intrinsic | 4 | Location, Humor | Explorer, Instant Joker |

**Design implication:** perceived map density and freshness is a hard constraint. Empty maps starve this motivation.
**Failure mode:** the map becomes static wallpaper behind a feed; geography stops mattering.

---

### M4 — Self-Expression ("this is so me")
**Drive:** the need to project personality and be recognized for a distinctive style.
**Trigger:** customizing a marker/profile; seeing others' expressive identities; a milestone worth showing.
**Payoff:** a profile/signature that feels personally meaningful and is visible to others.
**Reinforcement:** identity persists and evolves with history; style gets recognized socially.

| Type | Strength | Pillars | Primary personas |
|---|---|---|---|
| Intrinsic | 4 | Identity, Humor | Identity Builder, Social Reactor |

**Design implication:** expression must be visible and continuous, not buried. This is the strongest monetization motivation.
**Failure mode:** generic, homogenized profiles; expression that no one else sees.

---

### M5 — Progress & Mastery ("I'm building something")
**Drive:** the need to feel momentum, growth, and competence over time.
**Trigger:** a streak at risk; a near-complete achievement; a rank within reach.
**Payoff:** visible advancement — streak extended, badge earned, rank climbed.
**Reinforcement:** accumulating history, seasonal resets, escalating goals.

| Type | Strength | Pillars | Primary personas |
|---|---|---|---|
| Intrinsic + Extrinsic | 4 | Progress, Identity | Rival, Identity Builder |

**Design implication:** progress must be visible, fair, and reward meaningful participation — not spam. This is the primary *habit* engine.
**Failure mode:** grind-for-grind's-sake; progress that rewards volume over quality and feels hollow.

---

### M6 — Status & Competition ("I want to win and be seen winning")
**Drive:** the need for relative standing and recognized dominance among peers.
**Trigger:** a rival's rank movement; leaderboard refresh; a direct challenge; season end.
**Payoff:** measurable, visible superiority and the social acknowledgment of it.
**Reinforcement:** seasons, rivalries, callouts, defendable standings.

| Type | Strength | Pillars | Primary personas |
|---|---|---|---|
| Extrinsic | 3 | Progress, Social, Identity | Rival, Community Organizer |

**Design implication:** fairness and anti-abuse are first-class. Opaque or cheatable competition destroys trust instantly.
**Failure mode:** pay-to-win or exploitable ranking; competition that excludes casual users.

---

### M7 — Social Currency & Storytelling ("I have to send this")
**Drive:** the need to gain social capital by sharing funny, identity-affirming content.
**Trigger:** an exceptionally absurd moment; a milestone; a screenshot-worthy map state.
**Payoff:** laughs, replies, and status in external group chats and short-form platforms.
**Reinforcement:** sharing reliably earns social reward, prompting more sharing (viral loop).

| Type | Strength | Pillars | Primary personas |
|---|---|---|---|
| Intrinsic + Extrinsic | 5 | Humor, Social, Identity | Instant Joker, Identity Builder, Social Reactor |

**Design implication:** moments must be screenshot-ready, understandable out of context, and meme-native. This is the core **virality** engine (Vision §10–11).
**Failure mode:** content that needs explanation to be funny; nothing worth sending.

---

### M8 — Habit & Loss Aversion ("don't break the streak")
**Drive:** the need to protect an investment already made (streaks, rank, history).
**Trigger:** streak-expiry reminder; "you'll lose your rank" prompt; daily reset.
**Payoff:** relief from avoiding loss; satisfaction of maintained consistency.
**Reinforcement:** the longer the streak/history, the higher the perceived cost of stopping.

| Type | Strength | Pillars | Primary personas |
|---|---|---|---|
| Extrinsic | 3 | Progress, Identity | Rival, Identity Builder |

**Design implication:** use loss-aversion **ethically** — to reward consistency, never to manipulate or punish. Must respect user trust (Vision §17).
**Failure mode:** dark-pattern nagging that breeds resentment and uninstalls.

---

## 4) Motivation → Pillar Matrix

| Motivation | Location | Social | Humor | Progress | Identity |
|---|---|---|---|---|---|
| M1 Instant Amusement | 4 | 3 | 5 | 1 | 2 |
| M2 Belonging & Social Pull | 3 | 5 | 4 | 2 | 4 |
| M3 Curiosity & Discovery | 5 | 3 | 3 | 2 | 2 |
| M4 Self-Expression | 2 | 4 | 4 | 3 | 5 |
| M5 Progress & Mastery | 3 | 3 | 2 | 5 | 4 |
| M6 Status & Competition | 3 | 4 | 3 | 5 | 4 |
| M7 Social Currency | 3 | 5 | 5 | 2 | 4 |
| M8 Habit & Loss Aversion | 2 | 3 | 2 | 5 | 4 |

**Reading:** M1, M3, M7 are humor/location/virality engines (acquisition + delight). M2, M6 are social engines (retention). M4 is the identity/monetization engine. M5, M8 are habit engines. A healthy product serves at least one motivation per pillar.

---

## 5) Motivation → Persona Matrix

| Motivation | Instant Joker | Social Reactor | Explorer | Rival | Identity Builder | Community Organizer |
|---|---|---|---|---|---|---|
| M1 Instant Amusement | ●● | ● | ● | · | · | · |
| M2 Belonging | · | ●● | · | ● | ● | ●● |
| M3 Curiosity | ● | · | ●● | · | · | · |
| M4 Self-Expression | · | ● | · | · | ●● | ● |
| M5 Progress | · | · | · | ●● | ● | ● |
| M6 Status | · | · | · | ●● | ● | ● |
| M7 Social Currency | ●● | ● | ● | ● | ●● | ● |
| M8 Habit | · | ● | · | ●● | ● | ● |

`●●` = defining driver · `●` = secondary driver · `·` = minor/none.

**Insight:** M1 and M7 are the broadest motivations (touch nearly every persona) — they are the acquisition and virality backbone and must be protected in every phase. M2 is the retention backbone for the social personas.

---

## 6) Motivation → Strategic Phase Emphasis

Aligned to `ProductPillars.md` §9 phases and §10 weighting.

| Motivation | Phase 1: Core Loop | Phase 2: Social Retention | Phase 3: Progression | Phase 4: Community |
|---|---|---|---|---|
| M1 Instant Amusement | **Lead** | Maintain | Maintain | Maintain |
| M3 Curiosity & Discovery | **Lead** | Maintain | Maintain | Maintain |
| M7 Social Currency | **Lead** | Amplify | Maintain | Amplify |
| M2 Belonging | Seed | **Lead** | Maintain | Amplify |
| M4 Self-Expression | Light groundwork | Expand | **Lead** | Maintain |
| M5 Progress & Mastery | Light groundwork | Seed | **Lead** | Maintain |
| M6 Status & Competition | Defer | Seed | **Lead** | Maintain |
| M8 Habit & Loss Aversion | Seed (gentle) | Expand | **Lead** | Maintain |

**Rule:** never advance a later-phase motivation at the expense of an unmet earlier-phase motivation. Phase 1 lives or dies on M1, M3, M7.

---

## 7) Motivation → Outcome Mapping (Why Each Motivation Earns Its Place)

| Motivation | Primary business outcome | Secondary outcome |
|---|---|---|
| M1 Instant Amusement | Activation (first-session success) | Acquisition (word of mouth) |
| M2 Belonging | Retention (return for people) | Network density |
| M3 Curiosity | Activation + session frequency | Map vitality |
| M4 Self-Expression | Monetization | Identity retention |
| M5 Progress | Retention (habit) | Monetization (season passes) |
| M6 Status | Retention (competitive) | Monetization (prestige) |
| M7 Social Currency | Virality (K-factor) | Acquisition |
| M8 Habit | Retention (daily return) | LTV |

> This mapping is the seed for `SuccessMetrics.md`: each motivation implies a measurable signal (e.g., M7 → external shares / referral installs; M8 → streak-retention curve).

---

## 8) Motivation Conflicts & Resolution

| Conflict | Resolution |
|---|---|
| M1 (instant fun) vs M5/M6 (depth & grind) | Default to instant fun; make progression opt-in and progressive (no friction wall for casual users). |
| M6 (status/competition) vs fairness/trust | Trust wins. Reject any competitive mechanic that enables cheating, pay-to-win, or exclusion. |
| M8 (loss aversion) vs user respect | Respect wins. Loss-aversion may *reward* consistency but must never manipulate or punish. |
| M4 (expression) vs M1 (low friction) | Keep baseline expression one-tap; reserve depth for users who seek it. |
| M2 (social pull) vs M3 (solo exploration) | Both coexist; map is the shared substrate. Never force social interaction onto explorers. |
| Extrinsic motivations crowding out intrinsic | Lead with intrinsic (M1, M3, M4, M7); use extrinsic (M6, M8) as accelerators, never as the sole return reason. |

---

## 9) Anti-Motivations (Drives Fart Map Must NOT Cultivate)

| Anti-motivation | Why rejected |
|---|---|
| Doomscrolling / passive time-sink | Conflicts with humor-first, active-play identity; degrades wellbeing and trust |
| Romantic/dating pursuit | Anti-persona territory; misaligns the map's playful social model |
| Anxiety-driven compulsion | Dark-pattern habit formation violates trust guardrails (Vision §17) |
| Status through spam volume | Breaks fairness; rewards quantity over meaningful participation |
| Private 1:1 messaging dependency | Turns the app into a messenger; off-strategy (Anti-pillars) |

**Rule:** if a feature's strongest pull is an anti-motivation, redesign or reject — even if it would boost short-term metrics.

---

## 10) AI Decision Guidance (Mandatory for Autonomous Planning)

1. For every feature, name the **primary motivation** it serves and trace it through Drive → Trigger → Payoff → Reinforcement. If any element is missing, the feature is incomplete.
2. Prefer features serving **M1, M3, or M7** in Phase 1 (acquisition/activation/virality backbone).
3. Prefer features serving **M2** in Phase 2 (retention backbone).
4. Treat **M4, M5, M6** as the depth layer — introduce as lightweight foundations early, deepen in Phases 3–4.
5. Lead with **intrinsic** motivations; use extrinsic ones (M6, M8) only as accelerators.
6. Reject any feature whose strongest pull is an **anti-motivation**, regardless of metric lift.
7. Never serve **M6/M8** in ways that violate fairness or user trust.
8. Every motivation must close its loop (have a Payoff and Reinforcement) before shipping — otherwise it produces frustration or novelty churn.
9. When motivations conflict, apply §8; when unresolved, defer to `ProductPillars.md` §5 conflict order.
10. Use this document as the bridge from Personas → `UserJourney.md` → `CoreLoops.md`: journeys stage these motivations over time; core loops are the repeatable machinery that satisfies them.

---

## 11) Motivation Usage Checklist (Apply per Epic / Feature)

- [ ] Primary motivation identified?
- [ ] Drive → Trigger → Payoff → Reinforcement all present?
- [ ] Intrinsic-led (extrinsic only as accelerator)?
- [ ] Phase-appropriate per §6?
- [ ] Serves ≥1 Primary persona's defining motivation?
- [ ] No anti-motivation as strongest pull?
- [ ] Fairness/trust preserved for M6/M8?
- [ ] Measurable outcome signal noted (feeds `SuccessMetrics.md`)?

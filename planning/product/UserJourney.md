# User Journey: Fart Map

> **Planning spine position:** how users move through the product over time.
> Upstream: `Vision.md`, `ProductPillars.md`, `Personas.md`, `UserMotivations.md`. Downstream: `CoreLoops.md`, `Features.md`, `design/*`.
> Journeys stage the motivations from `UserMotivations.md` across time. Core loops (next doc) are the repeatable engines that live *inside* these stages.

---

## 1) Purpose & How to Use

This document maps the **end-to-end paths users take** through Fart Map — from never having heard of it to becoming a retained, sharing, identity-invested user. It exists to ensure features are placed at the right moment in the user's lifecycle, not just built in isolation.

AI agents and planners use this document to answer:
- At what stage does a feature matter, and which motivation does it serve there?
- Where are the highest-risk drop-off points, and what defends them?
- What must be true *before* a given stage can succeed?
- Which journey stage does the current strategic phase optimize?

Journeys are validated against upstream docs. They introduce **no new scope** — they sequence the demand defined in Motivations and the audiences defined in Personas.

---

## 2) Journey Model Conventions

Fart Map uses a **lifecycle journey** (macro: install → habit) composed of **session journeys** (micro: one app open). The macro lifecycle is where retention/virality live; the micro session is where the core loops (next doc) execute.

Each stage is described with:

| Field | Meaning |
|---|---|
| **Goal** | What the user is trying to get |
| **Driving motivation(s)** | From `UserMotivations.md` (M1–M8) |
| **Entry condition** | What must be true to reach this stage |
| **Success signal** | Observable proof the stage worked (seeds `SuccessMetrics.md`) |
| **Drop-off risk** | Why users leave here |
| **Defenses** | What the product must do to retain them |
| **Primary personas** | Who is most affected (from `Personas.md`) |

---

## 3) The Macro Lifecycle Journey (8 Stages)

```
Awareness → Acquisition → Activation → First Reward → Habit Formation
→ Social Embedding → Identity Investment → Advocacy
        ^------------------------------------------------|
        (Advocacy feeds new Awareness — the viral loop)
```

---

### Stage 1 — Awareness
**Goal (user):** understand, in one glance, "what is this absurd thing?"
**Driving motivation:** M7 Social Currency (someone shared it), M1 Instant Amusement (the premise is funny).
**Entry condition:** exposure to a share, screenshot, or short-form clip.
**Success signal:** the concept is understood without explanation; curiosity to install.
**Drop-off risk:** concept needs explaining → it isn't share-native.
**Defenses:** a premise instantly graspable out of context; screenshot-ready artifacts (Vision §10–11).
**Primary personas:** all (acquisition is universal); seeded by Instant Joker & Identity Builder shares.

---

### Stage 2 — Acquisition (Install & First Open)
**Goal:** get into the experience with minimal commitment.
**Driving motivation:** M1, M3 Curiosity.
**Entry condition:** install + first open.
**Success signal:** reaches the map/first action without abandoning setup.
**Drop-off risk:** heavy onboarding, forced account creation before value, permission walls.
**Defenses:** value-before-friction — show a living map first; defer/justify permissions (location) in context; lightweight or deferred account creation (Personas §7: allow lightweight start).
**Primary personas:** Instant Joker (lowest patience), Explorer.

---

### Stage 3 — Activation (First Meaningful Action)
**Goal:** do the core thing — log a fart and see it appear on the map.
**Driving motivation:** M1 Instant Amusement, M3 Curiosity.
**Entry condition:** location permission (or a graceful fallback) + a reachable log action.
**Success signal:** completes `map → log → marker appears` within the first session, ideally <60s (Vision §16; Personas Instant Joker).
**Drop-off risk:** empty map, logging friction, slow marker feedback, unfunny first impression.
**Defenses:** perceived map density from second one; instant, satisfying marker placement; comedic copy throughout (Pillars: Humor failure patterns).
**Primary personas:** Instant Joker, Explorer. **This is the single most important stage** — Phase 1 success is defined here.

---

### Stage 4 — First Reward (Social Feedback Taste)
**Goal:** feel that the action *mattered* — get a laugh, a reaction, a sense of being seen.
**Driving motivation:** M2 Belonging (first taste), M7 Social Currency.
**Entry condition:** activation complete + some ambient activity/reactions present.
**Success signal:** receives or gives a reaction; completes the `map → log → reaction` loop (Pillars §9 Phase 1 success).
**Drop-off risk:** the action feels like it went into a void; no reaction, no nearby life.
**Defenses:** ambient liveliness even at low density (seeded/aggregated activity, nearby highlights); fast, rewarding reaction UX.
**Primary personas:** Instant Joker, Social Reactor.

---

### Stage 5 — Habit Formation
**Goal:** build a reason to come back tomorrow, not just today.
**Driving motivation:** M8 Habit & Loss Aversion, M5 Progress, M2 Belonging.
**Entry condition:** ≥1 successful first-reward session + a return trigger (notification, streak, followed-user activity).
**Success signal:** repeat sessions across days; early streak/return behavior; D1/D7 return.
**Drop-off risk:** novelty fades; no progression or social pull to anchor return; weak/spammy notifications.
**Defenses:** lightweight progression foundations (streak foundation from Phase 2, stats from Phase 3) introduced gently; well-tuned, valuable notifications (Personas Social Reactor); answers to "what happened near me / did anyone react / can I keep my streak" (Vision §9).
**Primary personas:** Social Reactor, Rival (emerging), Explorer.

---

### Stage 6 — Social Embedding
**Goal:** connect to people — followed users, rivals, a group rhythm.
**Driving motivation:** M2 Belonging, M6 Status, M7 Social Currency.
**Entry condition:** people discovery available + enough graph density to matter.
**Success signal:** sends follow requests and gets approved; participates in threads; returns *because of other users* (Pillars §9 Phase 2 success).
**Drop-off risk:** dead follow graph; hard to find people; one-sided broadcasting.
**Defenses:** early, easy people discovery; reactivation loops that pull followers back together; reliable reciprocal interaction (Motivations M2 failure mode).
**Primary personas:** Social Reactor, Community Organizer (emerging), Rival.

---

### Stage 7 — Identity Investment
**Goal:** build a recognizable self — profile, style, history, reputation, rank.
**Driving motivation:** M4 Self-Expression, M5 Progress, M6 Status.
**Entry condition:** accumulated history + expression/progression systems available.
**Success signal:** customizes identity; defends rank/streak; profile becomes a social object (Pillars §9 Phase 3).
**Drop-off risk:** shallow/generic profiles; identity doesn't persist; unfair or opaque progression.
**Defenses:** profile depth & continuity; visible, fair progression; expressive (and monetizable) identity economy (Personas Identity Builder, Rival).
**Primary personas:** Identity Builder, Rival, Community Organizer.

---

### Stage 8 — Advocacy (Loops Back to Awareness)
**Goal:** broadcast funny moments and milestones outward, earning social currency.
**Driving motivation:** M7 Social Currency, M4 Self-Expression, M6 Status.
**Entry condition:** a screenshot-worthy moment, milestone, or rank climb.
**Success signal:** organic external shares → new installs (K-factor > 0; Vision §16).
**Drop-off risk:** nothing worth sharing; moments not screenshot-ready or not understandable out of context.
**Defenses:** meme-native, context-free shareable artifacts; milestone/rank share surfaces; community/event highlights (Vision §10–11).
**Primary personas:** Instant Joker, Identity Builder, Rival, Community Organizer.

---

## 4) Lifecycle Stage → Motivation → Phase → Persona Map

| Stage | Lead motivation(s) | Strategic phase that owns it | Primary personas |
|---|---|---|---|
| 1 Awareness | M7, M1 | All (virality is continuous) | All |
| 2 Acquisition | M1, M3 | Phase 1 | Instant Joker, Explorer |
| 3 Activation | M1, M3 | Phase 1 | Instant Joker, Explorer |
| 4 First Reward | M2, M7 | Phase 1 | Instant Joker, Social Reactor |
| 5 Habit Formation | M8, M5, M2 | Phase 2 | Social Reactor, Explorer, Rival |
| 6 Social Embedding | M2, M6, M7 | Phase 2 | Social Reactor, Community Organizer |
| 7 Identity Investment | M4, M5, M6 | Phase 3 | Identity Builder, Rival |
| 8 Advocacy | M7, M4, M6 | All (esp. 3–4) | Identity Builder, Rival, Community Organizer |

---

## 5) The Micro Session Journey (One App Open)

A single session is where core loops (next doc) execute. The canonical session:

```
Open ─► Orient (map + nearby) ─► Consume (see activity/reactions) ─► Act (log / react / comment)
     ─► Get feedback (marker / reaction / progress) ─► Optionally share ─► Exit with a reason to return
```

| Step | User goal | Motivation | Must-not-fail property |
|---|---|---|---|
| Open | Re-enter the world | M3, M8 | Fast cold-start; map loads alive |
| Orient | "What's near me / new?" | M3, M2 | Perceived density & freshness |
| Consume | Get a laugh / catch up | M1, M2 | Genuinely funny, low effort |
| Act | Log / react (comment from Phase 2) | M1, M2, M4 | One-tap-fast core action |
| Feedback | See it mattered | M1, M2, M5 | Instant, satisfying response |
| Share | Send the best moment | M7 | Screenshot-ready artifact |
| Exit | Leave with a hook | M8, M5, M2 | A concrete reason to return (streak/notification/followed-user activity) |

**Rule:** every session must end by *planting a return reason* (Stage 5 dependency). A session that closes the loop but plants no hook produces novelty churn.

---

## 6) Drop-off Risk Map (Where Users Leave & What Defends Them)

| Stage | Severity | Primary drop-off cause | Defended by (doc/system) |
|---|---|---|---|
| 2 Acquisition | **High** | Onboarding/permission/account friction | Onboarding design, `design/*`, permission strategy |
| 3 Activation | **Critical** | Empty map, logging friction, unfunny first impression | Map vitality system, logging UX, humor copy |
| 4 First Reward | **High** | Action feels ignored (no reaction/life) | Ambient activity, reaction system |
| 5 Habit | **High** | No return reason; weak notifications | Progression foundations, notification strategy |
| 6 Social Embedding | Medium | Dead follow graph; discovery friction | People discovery, reactivation loops |
| 7 Identity | Medium | Shallow/unfair identity & progression | Profile depth, fair progression, anti-abuse |
| 8 Advocacy | Medium | Nothing worth sharing | Shareable-moment design |

**The two make-or-break stages are 3 (Activation) and 5 (Habit):** Activation proves the product is fun; Habit proves it survives novelty. Phases 1–2 exist precisely to win these.

---

## 7) Journey Variations by Primary Persona

| Persona | Fast path through journey | Where they get stuck without defenses |
|---|---|---|
| Instant Joker | 1→2→3→4→8 (shares fast, may skip deep stages) | Stalls at 5 (Habit) — needs a light return hook |
| Social Reactor | 1→2→4→6→5 (people-first) | Stalls at 6 if the follow graph is dead |
| Explorer | 1→2→3→5 (map-first, solo) | Stalls at 4/6 — don't force social; reward exploration |
| Rival | enters at 5→6→7 once progression exists | Churns at 7 if progression is unfair/opaque |
| Identity Builder | enters at 7→8 once expression exists | Churns at 7 if profiles are shallow |
| Community Organizer | enters at 6→7→8 once graph & tools exist | Blocked until Phase 4 group tooling |

**Rule (from Personas §5):** never prioritize Secondary/Future persona journey stages (Rival/Identity/Community at 7–8) over unmet Primary stages (2–6). Win Activation and Habit for Instant Joker, Social Reactor, and Explorer first.

---

## 8) Stage Dependency Gates

A stage cannot reliably succeed until its predecessor is defended. Encode as planning gates:

| Gate | Rule |
|---|---|
| G1 | Don't optimize Habit (5) before Activation (3) and First Reward (4) are consistently winning. |
| G2 | Don't invest in Social Embedding (6) before there is enough graph density to avoid a dead-graph experience. |
| G3 | Don't push Identity Investment (7) depth before there is accumulated history worth expressing. |
| G4 | Don't rely on Advocacy (8) for growth before shareable moments exist (depends on 3, 4, 7). |
| G5 | Every session (micro journey) must plant a return reason before Habit (5) can form. |

---

## 9) AI Decision Guidance (Mandatory for Autonomous Planning)

1. For every feature, identify **which lifecycle stage** it serves and **which motivation** drives users there. A feature with no clear stage is likely premature or off-strategy.
2. Prioritize defenses for the **Critical/High** drop-off stages (2, 3, 4, 5) before polishing later stages.
3. Respect the **stage dependency gates** (§8); do not build downstream stages on undefended upstream ones.
4. For Primary personas, ensure the **Activation → First Reward → Habit** path is frictionless before serving Secondary/Future persona stages.
5. Ensure every **session** ends by planting a return reason (§5) — this is a hard requirement, not an enhancement.
6. Treat **Advocacy (8) → Awareness (1)** as the viral loop; protect shareable-moment quality as a growth lever, not an afterthought.
7. Map each stage's **success signal** to a measurable metric in `SuccessMetrics.md`.
8. When a feature spans stages, name its *primary* stage to avoid diffuse, unfocused scope.
9. Use this journey as the bridge to `CoreLoops.md`: the micro session journey (§5) is the skeleton that core loops flesh out into repeatable engines.

---

## 10) Journey Usage Checklist (Apply per Epic / Feature)

- [ ] Primary lifecycle stage identified?
- [ ] Driving motivation(s) named (M1–M8)?
- [ ] Strategic phase fit confirmed?
- [ ] Does it defend a Critical/High drop-off point?
- [ ] Stage dependency gates respected (§8)?
- [ ] Does it (or the session it touches) plant a return reason?
- [ ] Success signal defined for measurement?
- [ ] Primary persona path served before Secondary/Future?

# Personas: Fart Map

> **Planning spine position:** the *who*, derived from Vision + Pillars; reconciled with `UserMotivations.md`.
> Upstream: `Vision.md`, `ProductPillars.md`. Downstream: `UserMotivations.md` (reconcile-after pair), `UserJourney.md`, `CoreLoops.md`.
> The six personas (Instant Joker, Social Reactor, Explorer, Rival, Identity Builder, Community Organizer) and anti-personas defined here are the audiences every later product document must serve or explicitly defer.

## 1) Purpose & How to Use
This document defines the strategic user archetypes Fart Map is built for. It is a product decision system, not a marketing artifact.

AI agents and planners must use this document to answer:
- Who are we building for in this phase?
- Which roadmap items strengthen primary personas?
- Which proposals serve only future personas and should be deferred?
- Which proposals serve no defined persona and should be rejected?
- How should we resolve product conflicts between personas?

Personas are validated against `Vision.md` and `ProductPillars.md`. If a persona need conflicts with vision or pillars, vision and pillars win.

---

## 2) Persona Model Conventions

### Pillar Scoring (1–5)
Used to express how central a pillar is to that persona's motivation.

- 1 = Pillar is irrelevant to this persona
- 2 = Pillar is weakly supportive
- 3 = Pillar is meaningful but not primary
- 4 = Pillar is a strong driver
- 5 = Pillar is a primary, defining driver

### Monetization Potential
- Low = Unlikely to convert; value comes from acquisition or social density
- Medium = Will pay for low-friction expression or convenience
- High = Will pay for identity, status, progression, or exclusive experiences

### Persona vs Market Segment
Personas describe *product motivation*, not demographics. Age, geography, and platform are not persona-defining traits.

---

## 3) Anti-Personas (Who We Are NOT Building For)
Reject features that primarily serve these audiences.

| Anti-Persona | Why Rejected |
|---|---|
| The Utility Seeker | Wants a tracker/logger; demands seriousness that kills humor |
| The Dating User | Seeks romantic discovery; misaligned with playful map ecosystem |
| The Serious Networker | Expects LinkedIn/Instagram tone; flattens product personality |
| The Private Messenger | Wants 1:1 chat as primary loop; turns app into a messenger |
| The Health Optimizer | Wants quantified self-tracking; pushes toward sensitive utility framing |
| The Hardcore Gamer | Demands deep mechanics over playful accessibility |
| The Spam Grinder | Optimizes for volume over meaningful participation; breaks fairness |

Rule: If a feature mainly serves anyone in this table, redesign or reject.

---

## 4) Core Personas

---

### Persona A — The Instant Joker

**Summary**
Casual user who wants immediate laughs. Low patience for setup, high patience for absurdity.

**Core Motivation**
Fast emotional payoff — "make me laugh in 10 seconds."

**Why They Install**
- Viral short-form video
- Friend screenshot in a group chat
- Curiosity about the absurd premise

**Why They Stay**
- First session is funny and frictionless
- Map near them feels alive
- Reactions feel rewarding and quick

**Favorite Features**
1. Quick fart logging
2. Nearby map activity
3. Reactions
4. Funny highlights / featured moments
5. Minimal-touch profile basics

**Features They Ignore**
- Deep progression dashboards
- Group/event coordination
- Long-form profile customization

**Social Behavior**
Mostly reacts, occasionally comments, rarely initiates.

**Sharing Behavior**
Screenshots to group chats; reposts to stories when something is exceptionally absurd.

**Monetization Potential:** Medium
Pays for cosmetic flair if friction is near-zero.

**Pillar Alignment**
| Location | Social | Humor | Progress | Identity |
|---|---|---|---|---|
| 4 | 3 | 5 | 2 | 2 |

**Churn Triggers**
- Slow or serious onboarding
- Empty map on first session
- Loading or friction during logging

**Roadmap Implications**
- Optimize first 60 seconds aggressively
- Maintain perceived density of activity
- Protect comedic tone across UI copy

---

### Persona B — The Social Reactor

**Summary**
Socially motivated user who comes for *people*. Drives comment culture and follow-graph energy.

**Core Motivation**
Belonging and interaction — "I want to be in the loop with my people."

**Why They Install**
- Invited by people already active
- Sees followed users' activity in shares/notifications
- Wants a playful social alternative

**Why They Stay**
- Followed users post regularly
- Comments/reactions create ongoing threads
- Notifications create healthy social pull

**Favorite Features**
1. Follow graph (request and approval, ADR-0009)
2. Comments and reactions
3. Notifications
4. Social feed (map-anchored)
5. Profile-to-profile interaction

**Features They Ignore**
- Solo exploration without social context
- Territory min-maxing (deferred mechanic, ADR-0011)
- Deep solo collection mechanics

**Social Behavior**
High comment/reaction frequency; pulls others back into the app.

**Sharing Behavior**
Shares to private chats to provoke responses; uses externals as social proof.

**Monetization Potential:** Medium-High
Will pay for social expression and status visibility.

**Pillar Alignment**
| Location | Social | Humor | Progress | Identity |
|---|---|---|---|---|
| 3 | 5 | 4 | 3 | 4 |

**Churn Triggers**
- Low follow-graph activity / dead graph
- Weak notification value
- Hard-to-find people on the app

**Roadmap Implications**
- People discovery early
- Reliable, well-tuned notifications
- Reactivation loops that pull followers back together

---

### Persona C — The Explorer

**Summary**
Location-first user. Treats the map as a living world to traverse across neighborhoods, cities, and trips.

**Core Motivation**
Discovery — "show me what happened where."

**Why They Install**
- Map-based concept novelty
- Travel context
- Local curiosity about their area

**Why They Stay**
- Nearby activity stays fresh
- Personal map history accumulates
- New places yield new map stories

**Favorite Features**
1. Map exploration (local and global)
2. Nearby activity layers
3. Travel/location history
4. Territory map visibility (Phase 5 candidate, ADR-0011)
5. Place-based highlights

**Features They Ignore**
- Heavy feed scrolling
- Profile cosmetics
- Pure social chatter with no place context

**Social Behavior**
Lurker-leaning; interacts when content is place-relevant.

**Sharing Behavior**
"Look what happened here" snapshots; travel map highlights.

**Monetization Potential:** Medium
Pays for map personalization, history visualizations, travel-themed cosmetics.

**Pillar Alignment**
| Location | Social | Humor | Progress | Identity |
|---|---|---|---|---|
| 5 | 3 | 3 | 3 | 3 |

**Churn Triggers**
- Sparse map
- App drifting toward feed-first identity
- Geography becoming superficial

**Roadmap Implications**
- Protect map vitality as a hard constraint
- Invest in local relevance and travel memory systems
- Reject roadmap drift away from geography

---

### Persona D — The Rival

**Summary**
Competitive user driven by rank, streaks, and measurable dominance. Includes the collector instinct expressed competitively (climbing, completing seasons).

**Core Motivation**
Status and mastery — "I want to win and be seen winning."

**Why They Install**
- Follower challenge or rivalry
- Leaderboard screenshots
- Seasonal/event hype

**Why They Stay**
- Visible rank movement
- Streaks worth protecting
- Seasonal resets and new challenges

**Favorite Features**
1. Leaderboards
2. Streaks
3. Achievements
4. Territory control (Phase 5 candidate, ADR-0011)
5. Direct challenges

**Features They Ignore**
- Purely decorative social features
- Passive browsing
- One-off gimmicks with no measurable outcome

**Social Behavior**
Competitive callouts; activates peers via rivalry.

**Sharing Behavior**
Rank climbs, milestones, season wins.

**Monetization Potential:** High
Pays for prestige cosmetics, season passes, fair competitive expression.

**Pillar Alignment**
| Location | Social | Humor | Progress | Identity |
|---|---|---|---|---|
| 4 | 4 | 3 | 5 | 4 |

**Churn Triggers**
- Unfair or opaque progression
- Weak anti-abuse / cheating
- Slow leaderboard feedback

**Roadmap Implications**
- Fair progression architecture as a first-class concern
- Visible rank movement and season structure
- Anti-abuse investment scaled with competitive depth

---

### Persona E — The Identity Builder

**Summary**
Self-expression-driven user. Wants a recognizable, stylish, evolving public profile. Highest monetization persona.

**Core Motivation**
Self-expression and reputation — "I want my profile to mean something."

**Why They Install**
- Sees expressive profiles
- Wants playful identity platform
- Personal-brand curiosity

**Why They Stay**
- Profile evolves with history
- Style is visible and recognized
- Reputation signals carry weight

**Favorite Features**
1. Profile depth and history
2. Signature markers and visual style
3. Public milestones
4. Shareable profile views
5. Reputation indicators

**Features They Ignore**
- Anonymous-only flows
- Hidden progression
- Utility-style map tools without expression value

**Social Behavior**
Curated presence; engages where visibility matters.

**Sharing Behavior**
Shares profile milestones; screenshot-ready branded moments.

**Monetization Potential:** High
Top spender on identity expression, exclusivity, and prestige cosmetics.

**Pillar Alignment**
| Location | Social | Humor | Progress | Identity |
|---|---|---|---|---|
| 3 | 4 | 4 | 4 | 5 |

**Churn Triggers**
- Shallow or generic profiles
- Identity does not persist publicly
- Premium expression feels low-value

**Roadmap Implications**
- Profile depth, history continuity, and visual expression economy
- Public reputation mechanics
- Avoid features that erase or homogenize identity

---

### Persona F — The Community Organizer

**Summary**
Network-effects multiplier. Runs group rituals, events, and recurring social rhythm.

**Core Motivation**
Belonging through coordination — "I want to run the fun for my people."

**Why They Install**
- Wants group activity beyond passive scrolling
- Sees potential for group rituals
- Interested in hosting challenges

**Why They Stay**
- Group tools create recurring engagement
- Event cycles create social rhythm
- Their leadership has visible impact

**Favorite Features**
1. Groups
2. Challenges
3. Events / seasonal systems
4. Group leaderboards
5. Shared goals

**Features They Ignore**
- Pure solo loops
- Minimal-interaction browsing
- Features without group utility

**Social Behavior**
Initiator. Invites, organizes, reactivates members.

**Sharing Behavior**
Event highlights, group outcomes, recruitment posts.

**Monetization Potential:** Medium-High
Pays for community management and event-enhancement tools.

**Pillar Alignment**
| Location | Social | Humor | Progress | Identity |
|---|---|---|---|---|
| 4 | 5 | 4 | 4 | 4 |

**Churn Triggers**
- Shallow group tooling
- Inconsistent event systems
- Coordination friction

**Roadmap Implications**
- Reliable group/event frameworks (Phase 4)
- Invitation and reactivation loops
- Group progression visibility

---

## 5) Persona Priority Matrix

| Persona | Tier | Reason |
|---|---|---|
| Instant Joker | Primary | Validates core promise in first session |
| Social Reactor | Primary | Drives retention through people |
| Explorer | Primary | Protects core map differentiation |
| Rival | Secondary | Activates once progression matures |
| Identity Builder | Secondary | Activates once profile/expression systems mature |
| Community Organizer | Future | Activates once social graph and rituals are established |

Rule: Never prioritize Secondary or Future persona needs over unmet Primary persona needs.

---

## 6) Phase Coverage Matrix

| Persona | Phase 1: Core Loop | Phase 2: Social Retention | Phase 3: Progression | Phase 4: Community |
|---|---|---|---|---|
| Instant Joker | Must satisfy | Maintain | Maintain | Maintain |
| Social Reactor | Baseline support | Must satisfy | Maintain | Maintain |
| Explorer | Must satisfy | Maintain | Maintain | Maintain |
| Rival | Defer | Light groundwork | Must satisfy | Maintain |
| Identity Builder | Light groundwork | Light expansion | Must satisfy | Maintain |
| Community Organizer | Defer | Defer | Light groundwork | Must satisfy |

Coverage goals:
- **Phase 1**: prove the map → log → reaction loop is fun and place-anchored
- **Phase 2**: prove people bring people back
- **Phase 3**: prove progression turns engagement into habit
- **Phase 4**: prove community creates self-sustaining content

---

## 7) Persona Conflict Resolution Rules

| Conflict | Resolution |
|---|---|
| Casual simplicity vs Competitive depth | Default UX simple; depth opt-in and progressive |
| Anonymous access vs Identity reputation | Allow lightweight start; incentivize identity upgrade |
| Map-first exploration vs Feed convenience | Map is product center; feed complements, never replaces |
| Humor chaos vs Trust & safety | Preserve humor; reject mechanics enabling harassment |
| Individual prestige vs Group equality | Distinguish cosmetic status from competitive power; no pay-to-win |
| Primary persona needs vs Future persona ambitions | Primary always wins until satisfied |

---

## 8) Monetization Lens by Persona

| Persona | Potential | Most Likely Spend |
|---|---|---|
| Instant Joker | Medium | One-tap cosmetic flair |
| Social Reactor | Medium-High | Social expression, reaction packs |
| Explorer | Medium | Map personalization, travel themes |
| Rival | High | Season passes, prestige cosmetics |
| Identity Builder | High | Premium identity, exclusive style |
| Community Organizer | Medium-High | Group tools, event enhancements |

Rule: All monetization must remain additive and never pay-to-win.

---

## 9) Churn Risk Lens by Persona

| Persona | Primary Churn Trigger |
|---|---|
| Instant Joker | Slow or unfunny first session |
| Social Reactor | Weak social graph density |
| Explorer | Sparse or static map |
| Rival | Unfair or opaque progression |
| Identity Builder | Shallow profile, no continuity |
| Community Organizer | Inadequate group tooling |

---

## 10) AI Decision Guidance

Mandatory rules for autonomous planning:

1. Prioritize Primary personas first; Secondary next; Future last.
2. Reject roadmap items that serve only Future personas while Primary persona needs are unmet.
3. Use `ProductPillars.md` and `Vision.md` as final arbiters in any persona conflict.
4. Prefer features that benefit at least two Primary personas.
5. Reject features whose strongest beneficiary is an Anti-Persona.
6. Never reduce map relevance — protects Explorer and core differentiation.
7. Never flatten humor — protects Instant Joker and product personality.
8. Never break identity continuity — protects Identity Builder and long-term retention.
9. Treat Community Organizer systems as phase-gated; do not force early.
10. For every epic or feature proposal, document:
    - Target personas
    - Expected retention impact per persona
    - Churn risk if not built
    - Misalignment risk if built poorly

Non-negotiable: do not trade long-term persona alignment for short-term implementation convenience.

---

## 11) Persona Usage Checklist (Apply per Epic / Feature)

- [ ] Which personas does this serve? (list)
- [ ] Are any of them Primary in the current phase?
- [ ] Does it harm any persona (including Anti-Personas attraction)?
- [ ] Pillar scorecard recorded (from ProductPillars.md)?
- [ ] Phase fit confirmed against Phase Coverage Matrix?
- [ ] Conflict notes recorded if multiple personas pull different directions?
- [ ] Build now / later / reject recommendation stated?

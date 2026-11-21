# Product Brief: Fart Map

> Merged and condensed from Vision, ProductPillars, CoreLoops, Personas, UserMotivations, UserJourney, ProductGovernance and Prioritization (originals in `planning/archive/product/`). No decision was changed in the merge. Principles, non-goals and hard constraints are kept word for word in substance. Read this file for product judgement calls. For implementation, read the feature file under `features/` instead.

## 1) What Fart Map is

- **Category.** A location-based social entertainment game for mobile, on Android and iOS. Users drop humorous markers on a shared map, react to each other's moments, build an identity through participation, and compete in a playful shared world.
- **Core fantasy.** *"I can leave my mark on the world, get seen, and build my own ridiculous legend."*
- **Mission.** Make people laugh together, every day, through location-based social play.
- **Business goal.** The app is built to earn money through cosmetic monetization: a la carte purchases, opt-in rewarded ads, and later a subscription. Growth (sharing, store discoverability) is therefore a first-class concern; see `Growth.md`.
- **The single decision test.** *"Is this a location-based social entertainment game move?"*
  - If yes, evaluate it with §6.
  - If no, reject it.
  - If unclear, redesign it until the answer is clear.

## 2) What Fart Map is not (permanent exclusions)

| Category | Why excluded |
|---|---|
| Serious social network | Seriousness removes the humor identity that makes the product shareable |
| Chat / direct messaging | Decouples interaction from location; creates a harassment and stalking vector |
| Utility / productivity app | Utility removes playfulness |
| Dating platform | Safety and trust failure modes incompatible with location + humor |
| Health tracker | Health framing conflicts with playful self-expression |
| Content publishing platform | The product is map moments, not long-form media |
| Generic feed-first social app | Feed-first reduces location to a filter; the map must stay the game board |
| Pay-to-win game | See principle 5 and principle 6 |

**Anti-personas (reject features that mainly serve them):** Utility Seeker, Dating User, Serious Networker, Private Messenger, Health Optimizer, Hardcore Gamer, Spam Grinder.

## 3) The six permanent principles

Each principle can change only through a new ADR.

1. **Map First.** The map is the product, not a feature. Every action, social surface, progression system and identity expression is anchored to the map.
   - *Test:* if removing the map does not change a feature's value, the feature does not belong.
   - *Failure:* feed, leaderboard or profile as the launch destination.
2. **Humor First.** Humor is structural, not a tone setting. Every label, empty state, error, notification and achievement name carries the voice ("Logged. The world felt it." instead of "Post saved"). Humor is also the viral mechanism.
3. **Social Through Location.** Social actions get their meaning from place. Feed items, notifications and share cards keep the location context.
4. **Identity Through Participation.** Status is earned by activity. Cosmetics express an identity; they never create it.
   - *Reject:* bought followers, bought achievements, bought rank.
5. **Fair Competition.** Rank reflects activity, never payment or exploits. Anti-abuse ships before or with any leaderboard.
6. **Cosmetic Monetization Only.** Pay for expression, never for access or advantage. The free tier is a complete product.
   - *Reject:* paywalls on logging, the map, reacting or following; daily log limits; paid achievements.

**Hard constraints:**
1. The map is always the default launch destination.
2. Nothing gates fart creation behind payment.
3. No path to rank or XP through payment.
4. Safety controls ship with the feature they protect, never after.
5. Every feed item has a "View on Map" CTA.

## 4) Pillars and phase weights

Pillars: **Location** (the world is the game board), **Social** (better when shared), **Humor** (moments worth laughing about), **Progress** (building something over time), **Identity** (a recognizable profile and reputation). Every feature strengthens at least one.

| Phase | Weights |
|---|---|
| 1 Core Loop | Location 35, Humor 30, Social 20, Identity 10, Progress 5 |
| 2 Social Retention | Social 35, Location 25, Humor 20, Identity 10, Progress 10 |
| 3 Identity & Progression | Progress 30, Identity 25, Social 25, Location 15, Humor 5 |
| 4 Community | Social 30, Progress 25, Identity 20, Location 15, Humor 10 |

Anti-pillar test:
- If removing humor or removing location does not change a feature's value, the feature is likely off-strategy.
- The same holds if the feature's value depends on private messaging.

## 5) Personas and core loops

**Personas:**

| Persona | Tier | Wants |
|---|---|---|
| Instant Joker | Primary | Fast laughs, zero friction; validates the first session |
| Social Reactor | Primary | To be seen and to react; drives retention through people |
| Explorer | Primary | What's funny nearby; protects map differentiation |
| Rival | Secondary (Phase 3+) | Rank, competition |
| Identity Builder | Secondary (Phase 3+) | Recognition, profile expression |
| Community Organizer | Future (Phase 4) | Groups, events |

Never prioritize Secondary or Future persona needs over unmet Primary persona needs.

**Loops (ids are referenced across planning):**

| Loop | Phase | Shape | Health bar / leak points |
|---|---|---|---|
| L1 Explore → Log → See | 1 | open app → log → marker appears instantly → richer map → log again | First log in under 60 s on the first session. Leaks: empty map, logging friction, dull marker feedback |
| L2 Social Reaction | 1 seed, 2 full | see a fart / get a reaction push → react → the other user is notified → returns | 40% or more of new users give or receive a reaction within 24 h. Leak: no reciprocity ("void" feeling) |
| L3 Discovery / Map-Watch | 1 | curiosity → pan or zoom → fresh place-anchored stories | Map vitality is a hard constraint. Leak: a sparse, static map |
| L4 Social Connection | 2 | follow → richer feed → more L2 | Leaks: dead graph, hard discovery |
| L5 Progression / Streak | 2–3 | streak at risk → qualifying action → visible progress | Reward meaningful participation, not spam; use loss aversion ethically |
| L6 Competition / Ranking | 3–4 | rank movement → compete → standing | Fairness first; no pay-to-win |
| L7 Community / Events | 4 | group or event cadence → shared goals | Needs an established graph and tooling |
| L8 Viral Share | 2 (proposed 1, ADR-0023) | screenshot-worthy moment → share externally → recipient installs → L1 | Shared artifacts must be understandable out of context |

## 6) Making a product decision

1. **Hard gates (reject on failure):**
   - safety (`ContentSafety.md` §2);
   - anti-pillar;
   - vision (§2);
   - trust (a metric gained by breaking a trust guardrail, `Metrics.md` §8);
   - monetization (pay-to-win or pay-for-access, `Monetization.md` §2);
   - phase (`Scope.md` §6).
2. **Five questions.** Does it strengthen location, humor, identity, social interaction, progression?
3. **Pillar scorecard,** 1–5 on each pillar for a total out of 25:
   - 20–25: build soon;
   - 15–19: build with scope control;
   - 10–14: redesign;
   - below 10: reject.
4. **Loop and phase fit.** Which L1–L8 loop does it strengthen? Is it phase-appropriate? Does shipping it now weaken an unhealthy current-phase loop?
5. **Record it.** Write an ADR (`planning/decisions/`) for any change to scope, a default, monetization or a principle. Rejected ideas record the gate that rejected them.

**Owner approval is required for:**
- moving a feature across phases;
- monetization posture;
- privacy or safety defaults;
- any principle above.

## 7) Anti-patterns

Each one has caused failures elsewhere:
- feed replacing map;
- generic social creep;
- utility creep;
- punitive mechanics (energy systems, wait timers);
- progression inflation;
- notification spam as retention;
- monetization pressure (dark patterns, onboarding upsells);
- complexity for its own sake;
- safety deferred to "the next sprint".

## 8) North Star and phases

- **North Star.** Weekly Active Sharers-and-Reactors (WASR): users who return and participate socially or by sharing within 7 days.
- **Phases.** 1 Core Loop → 2 Social Retention → 3 Identity & Progression → 4 Community → 5 Expansion (undefined until Gate 4).
- Phase scope, the MVP list, gates and non-goals live in `Scope.md`. Metric definitions live in `Metrics.md`.

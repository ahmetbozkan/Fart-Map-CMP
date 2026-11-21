# Scope and Release Strategy: Fart Map

> **Planning spine position:** release sequencing authority; downstream of product definition, upstream of roadmap execution.
> Formerly `Scope.md`. Upstream: `Product.md`, `features/`, `Metrics.md`. Downstream: `planning/ROADMAP.md`, stories.
> Phase placement authority: when this file and a feature file disagree on a phase, this file wins and the feature file is corrected.
> **This document is not a roadmap.** It defines release philosophy, phase boundaries, MVP scope, and the measurable gates that authorize moving between phases. Specific epics, stories, and sprint assignments live in `roadmap/`.

---

## Contents

1. [Purpose](#1-purpose)
2. [Release Principles](#2-release-principles)
3. [Product Phase Definitions](#3-product-phase-definitions)
4. [MVP Definition](#4-mvp-definition)
5. [Feature Placement Matrix](#5-feature-placement-matrix)
6. [Launch Success Gates](#6-launch-success-gates)
7. [Explicit Non-Goals](#7-explicit-non-goals)
8. [AI Decision Rules for Release Planning](#8-ai-decision-rules-for-release-planning)

---

## 1) Purpose

Release sequencing is not project management convenience. It is product strategy.

The loops that make Fart Map work — Explore → Log → See, Social Reaction, Progression, Competition, Community — are not independent. Each loop depends on the previous one being healthy before it is meaningful. A user cannot have a social retention experience if the core creation loop is confusing. A user cannot form a streak habit if they did not have a compelling first session. A competition system built on a weak social graph is a leaderboard for ghosts.

Shipping in the wrong order produces one of two failure modes:

1. **Premature depth.** Features that require emotional investment from users who have not yet been activated. The feature lands in a cold, empty context and appears worthless. The team loses confidence in a good idea at the wrong time.
2. **Retention debt.** Shipping an activation layer and moving on without closing the social loop. Users activate, find the experience solo and shallow, and churn before social gravity can hold them.

This document defines the order in which depth is added, the minimum viable scope for each phase, and the measurable proof that one phase is healthy enough to authorize the next. It is the sequencing constitution that prevents both failure modes.

---

## 2) Release Principles

These principles are ordered. When two principles are in tension, the higher-numbered principle yields to the lower-numbered one.

### Principle 1 — Activation before retention

The core loop (Explore → Log → See → Laugh) must be fun, fast, and frictionless before any retention mechanism is introduced. A streak system layered on top of a frustrating logging experience does not fix logging friction — it punishes users for it.

**Consequence:** Phase 1 is not done until a meaningful share of new users complete the map → log → marker loop in their first session, without assistance or explanation.

### Principle 2 — Retention before progression

Progression systems (streaks, XP, levels, leaderboards) create reasons to return for users who already enjoy the product. They do not create enjoyment from nothing. Shipping progression before basic social retention is established produces grind, not habit.

**Consequence:** Phase 2 social loops (follow graph, reactions, feed, notifications) must show healthy D7 return rates before Phase 3 progression depth is prioritized.

### Principle 3 — Progression before competition

Competitive systems (leaderboards, ranking, challenges) require a user population with enough activity history for scores to be meaningful. A leaderboard populated by day-one users with identical stats is not competition — it is noise.

**Consequence:** Phase 3 progression systems (XP, streaks, levels) must mature before Phase 4 competitive features are built on top of them. Anti-abuse controls (PROG-05) must ship before any leaderboard (PROG-03).

### Principle 4 — Competition before community

Community systems (groups, events, collaborative challenges) require an active competitive and social layer to give them energy. A group of users who don't already interact around content has nothing to organize around.

**Consequence:** Phase 3 must demonstrate healthy social graph density and progression engagement before Phase 4 community infrastructure is built.

### Principle 5 — Community before expansion

Expansion features (territory systems, seasonal platforms, creator programs) require a thriving community to provide the population and content that makes expansion meaningful. Expansion without community is infrastructure with no users.

**Consequence:** Phase 4 must demonstrate network effects and self-sustaining content before Phase 5 scope is defined and prioritized.

### Principle 6 — Safety ships with the phase, never after

Every phase introduces new user-generated content surfaces. Safety controls, moderation tooling, privacy defaults, and content policies for those surfaces must ship in the same release — never as a follow-up. An unsafe surface that goes live even briefly creates trust damage that outlasts the fix.

**Consequence:** Phase gates include safety readiness as a hard criterion. A phase cannot be declared done if its UGC surfaces have unmitigated threat-model items (see `ContentSafety.md` §2).

### Principle 7 — Cut breadth, never pillar expression

When scope must be reduced, remove features at the edges of the phase before reducing the quality or depth of any feature that directly powers a core pillar. A polished core that delights is worth more than a wide surface that underwhelms.

**Consequence:** MVP scope reduction decisions must pass the pillar scorecard in `Product.md` Never cut a feature that scores 5 on Location, Humor, or Social to add capacity for a feature that scores 3 across the board.

---

## 3) Product Phase Definitions

### Phase 1 — Core Loop

**Goal:** Prove that users enjoy opening the app, placing themselves on the map, logging a moment, and seeing it appear. The experience must be instantly understandable and intrinsically funny before anything else is added.

**North Star for this phase:** First-session loop completion rate. A new user must be able to go from install to first marker on the map in under 60 seconds with no explanation.

**What this phase proves:** The foundational fun is real and accessible. Location and humor combine to produce a moment worth repeating.

**Loops served:** L1 (Explore → Log → See), L2 (Social Reaction, seed-level), L3 (Map Discovery, seed-level).

**Pillar weight:** Location 35%, Humor 30%, Social 20%, Identity 10%, Progress 5%.
*(Weighted toward the two pillars that must land on day one. If a new user finds the map alive and funny, everything else has a foundation.)*

**Phase is complete when:** Activation gate passes. See §6.

---

### Phase 2 — Social Retention

**Goal:** Give users a reason to come back because of other people, not only because of their own content. The social graph must form, notifications must pull users back, and the feed must provide passive social consumption between active sessions.

**North Star for this phase:** D7 return rate driven by social triggers (notification-open → return sessions attributed to social events).

**What this phase proves:** People bring people back. The social flywheel has started.

**Loops served:** L2 (Social Reaction, full closure), L4 (Social Connection), L8 (Viral Share, initial).

**Pillar weight:** Social 35%, Location 25%, Humor 20%, Identity 10%, Progress 10%.
*(Social becomes the lead pillar because retention at this phase is entirely social.)*

**Phase is complete when:** Retention gate passes. See §6.

---

### Phase 3 — Identity and Progression

**Goal:** Turn engagement into habit and turn habit into identity. Users should feel that they are building something visible and meaningful over time — a streak, a rank, a profile worth showing.

**North Star for this phase:** D30 retention + streak adoption rate. Users who stay 30 days have a streak or a profile they have invested in.

**What this phase proves:** Activity accumulates into something users protect, display, and compete over.

**Loops served:** L5 (Progression/Streak), L6 (Competition/Ranking, groundwork), Identity accretion (L5 interlock).

**Pillar weight:** Progress 30%, Identity 25%, Social 25%, Location 15%, Humor 5%.
*(Progression and identity now lead. Humor is still present in copy, achievements, and level labels — it is not reduced, only de-emphasized in weighting.)*

**Phase is complete when:** Progression gate passes. See §6.

---

### Phase 4 — Community

**Goal:** Create network effects and self-sustaining content loops. Users should form groups, participate in shared events, and generate content for each other — reducing dependence on individual creation as the only source of activity.

**North Star for this phase:** Group participation rate + K-factor approaching 0.4.

**What this phase proves:** Communities create their own reasons to exist. The product can grow through community output, not only new user acquisition.

**Loops served:** L7 (Community/Events), L6 (Competition, full depth), L8 (Viral Share, full depth).

**Pillar weight:** Social 30%, Progress 25%, Identity 20%, Location 15%, Humor 10%.
*(Social leads because community is a social outcome. Humor must not slip below visible expression in events, challenges, and group culture.)*

**Phase is complete when:** Community gate passes. See §6.

---

### Phase 5 — Expansion

**Scope: TBD.** Phase 5 is not defined until Phase 4 is healthy and the North Star metric (WASR) is growing consistently. Defining Phase 5 before Phase 4 is healthy is premature speculation that risks distracting from near-term execution.

**Known candidates (not committed):** Territory system (ADR-0011 — explicitly post-MVP), seasonal content platforms, creator program, international expansion, platform/API integrations.

**Gate to define Phase 5:** Phase 4 community gate must pass. WASR must show consistent week-over-week growth. Product leadership must review and commit to Phase 5 scope before any work begins.

---

## 4) MVP Definition

MVP is the minimum scope that makes Phase 1 meaningful, safe, and shippable — not the minimum scope that is technically functional. A technically functional app that is unsafe, joyless, or unfunny is not an MVP for Fart Map. It is a prototype.

### What must exist before launch

**Map**
- Full-screen interactive map centered on user location (MAP-01)
- Fart markers visible on map; tappable for preview (MAP-02)
- Public markers from other users visible within discovery radius
- Marker visual style reflects flavor level
- Filters Mine / Followers / Everyone; Followers shows an empty state until Phase 2 (MAP-03, ADR-0020)

**Creation**
- Quick Log: one-tap fart logging from the map (CREATE-01)
- Location auto-detection with manual adjustment (CREATE-02)
- Detailed Log screen with all 10 steps (CREATE-03): location, datetime, avatar, flavor, intensity, description, sound picker, voice recording (locked UI), visibility selector, submit (CREATE-03.1)
- Flavor stepper, 5 levels (CREATE-04)
- Intensity slider (CREATE-05)
- Description field with emoji support (CREATE-06)
- 2 free avatars minimum; remaining avatars purchasable a la carte (permanent, MON-01) or unlockable once via rewarded ad (MON-03) — ADR-0020
- ≥3 free sounds; remaining sounds purchasable or ad-unlockable the same way (CREATE-07 free set plus MON-01/MON-03)
- Visibility selector: Public / Followers / Private (ADR-0010), defaulting to last-used / Followers for new users
- Delete own fart (CREATE-09; edit is Phase 2)

**Social**
- Reactions: 5 types, one per user per fart, optimistic update (SOCIAL-01)
- No comments at MVP (Phase 2)

**Profile**
- Basic public profile: avatar, username, fart count (PROFILE-02); follower count hidden until Phase 2
- Avatar selection: free set, plus locked avatars purchasable or ad-unlockable from the profile sheet (PROFILE-01, MON-01/MON-03, ADR-0020)

**Privacy**
- Visibility controls on all created content (PRIV-01)
- Location precision fuzzing: public markers coarse only, exact coordinates private (PRIV-02)
- Block function (PRIV-01)

**Moderation**
- Report flow for farts and profiles (MOD-01)
- Content policy baseline enforced at account creation

**Notifications**
- Reaction received push notification (NOTIF-01 minimal, FCM/APNs, deep link to the marker; ADR-0020)

**Onboarding**
- First-launch flow: map orientation, first log guidance, concept explanation — runs on an anonymous session with full capability (ADR-0018); linking a Google or Apple account is nudged after the first log and never forced

**Monetization (ADR-0016, ADR-0017, ADR-0020)**
- A la carte purchase of avatars and sounds through RevenueCat over Apple IAP / Google Play Billing; entitlements verified server-side
- Rewarded ads (AdMob, server-side verified) for single-use avatar/sound unlocks; maximum 3 views per user per day
- No subscription, no passive ads

**Navigation (ADR-0020)**
- Four root tabs: Map (default) / Social / Rank / Profile; Log is the map FAB
- Social tab in Phase 1: nearby public activity list with "View on Map"; Rank tab in Phase 1: own stats plus leaderboard teaser

### What is intentionally excluded from MVP

The following are explicitly out of MVP scope. Any proposal to include them before Phase 1 gates pass must be rejected.

| Excluded feature | Why excluded | Phase it belongs to |
|---|---|---|
| Comments | Requires moderation maturity and social graph to be valuable | Phase 2 |
| Follow / follower system | Requires social graph scaffolding; empty graph at launch | Phase 2 |
| Feed | Requires follow graph to be non-empty | Phase 2 |
| Custom sounds (full library beyond the Phase 1 free + purchasable set) | Creation quality doesn't depend on sound variety at MVP | Phase 2 |
| Voice recording (unlocked) | Requires audio moderation pipeline and MON-02 | Phase 4 |
| Streak system | Requires D7+ retained user base to be meaningful | Phase 2–3 |
| XP and levels | Requires anti-abuse controls; no purpose without retention | Phase 3 |
| Leaderboards | Requires XP, anti-abuse, and minimum active population | Phase 3 |
| Achievement system | Requires activity history to award meaningfully | Phase 3 |
| Signature / style system | Requires cosmetic catalog depth to be expressive | Phase 3 |
| Profile stats / history | Requires accumulated activity to display | Phase 3 |
| Share cards | High value but not blocking for Phase 1 fun | Phase 2 |
| Groups | Requires active social graph and community critical mass | Phase 4 |
| Challenges | Requires leaderboard and competition infrastructure | Phase 4 |
| Events | Requires community infrastructure | Phase 4 |
| Premium subscription | Requires cosmetic catalog depth | Phase 4 |
| Territory | Explicitly post-MVP (ADR-0011) | Phase 5 TBD |
| Direct messaging | Explicitly a non-goal (see §7) | Never |
| Map heatmaps / density layers | Nice-to-have map feature | Phase 2 |
| Map history / personal replay | Requires activity accumulation | Phase 3 |
| Social discovery / search | Requires follow graph population | Phase 2 |
| Push notification system (full) | Requires social graph events to trigger | Phase 2 |

---

## 5) Feature Placement Matrix

This table covers every named feature system in `features/`. Phase = the primary phase in which the feature ships its core functionality. Where a feature spans phases, the phase of its MVP-viable version is listed.

### Map Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| MAP-01 Core Map View | 1 | The product's primary surface; nothing works without it | None |
| MAP-02 Marker Display System | 1 | Markers closing the L1 loop is the core product moment | MAP-01 |
| MAP-03 Map Layers, Filters, and Discovery Modes | 1 (all filters; Followers populates in 2) | Filters ship with the map; the Followers lens depends on the graph | MAP-01; SOCIAL-03 for Followers |
| MAP-04 Nearby Activity and Heatmaps (Highlight System) | 2 | Requires a critical mass of nearby farts to be meaningful | MAP-01, sufficient content |
| MAP-05 Place Detail and Context Surface | 2 | Turns a place into a story surface; needs reactions and comments | MAP-02, SOCIAL-01, SOCIAL-02 |
| MAP-06 Map Memory and Travel History | 3 | Requires accumulated personal activity to display | PROFILE-02, PRIV-02 |

### Content Creation Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| CREATE-01 Quick Log Engine | 1 | The primary content creation action; L1 loop depends on it | MAP-01, PRIV-01 |
| CREATE-02 Location Picker | 1 | Required for any fart to have a location | MAP-01 |
| CREATE-03 Detailed Log Screen | 1 (base), 2 (full) | Base fields ship Phase 1; audio/social-richer fields ship Phase 2 | CREATE-01/02, PRIV-01 |
| CREATE-04 Flavor System | 1 | Core comedic dimension of every fart; ships with CREATE-01 | CREATE-01 |
| CREATE-05 Intensity Slider | 1 | Captured at log time; stored for future progression use | CREATE-01 |
| CREATE-06 Story / Description | 1 | Optional text field; low cost, high humor value | CREATE-01 |
| CREATE-07 Custom Sound Selection | 1 (picker with ≥3 free + purchasable/ad-unlockable sounds, ADR-0020), 2 (full library) | Requires audio pipeline and cosmetic catalog baseline | MON-01 (free tier), audio infra |
| CREATE-08 Voice Recording | 4 | Premium-only; MON-02 is Phase 4; requires audio moderation coverage | MOD-02, MON-02, CREATE-03 |
| CREATE-09 Preview, Edit, Delete | 1 (delete), 2 (edit, preview) | Delete is a trust and privacy basic; edit follows | CREATE-01 |

### Social Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| SOCIAL-01 Reactions | 1 | Seed-level social feedback; required for the L2 loop to exist | CREATE-01, MOD-01 |
| SOCIAL-02 Comments | 2 | Requires moderation maturity; adds depth on top of reactions | SOCIAL-01, MOD-01/02 |
| SOCIAL-03 Follow System | 2 | Requires follow-request lifecycle and notification pipeline | ADR-0009, NOTIF-01, PRIV-01 |
| SOCIAL-04 Map-Anchored Feed | 2 | Requires non-empty follow graph to be useful | SOCIAL-03, CREATE-01 |
| SOCIAL-05 Social Discovery | 2 | Requires follow graph population to surface people worth finding | SOCIAL-03, NOTIF-01 |

### Profile Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| PROFILE-01 Avatar System (free tier) | 1 | Identity on map markers and in feed; 2 free avatars ship at MVP | CREATE-01 |
| PROFILE-02 Public Profile | 1 (basic), 2 (full) | Basic profile required for social recognition at launch | PROFILE-01 |
| PROFILE-03 Signature and Style | 3 | Requires cosmetic catalog depth; single-style selection (ADR-0013) | MON-01, PROFILE-02 |
| PROFILE-04 Stats, History, Reputation | 3 | Requires accumulated activity; displays streak, level, XP | PROG-01, PROG-02 |
| PROFILE-05 Collections | 3 | Requires achievement system to populate | ACH-01 |
| PROFILE-06 Profile Share Surface | 3 | Requires profile investment to be worth sharing | PROFILE-04, SHARE-01 |

### Progression Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| PROG-01 Streak System | 2 (foundation), 3 (full) | Foundation ships Phase 2 for early habit formation; milestones and full display in Phase 3 | NOTIF-02, PROFILE-04 |
| PROG-02 XP and Levels | 3 | Requires PROG-05 anti-abuse; no purpose without D30 retained users | PROG-05, ACH-01 |
| PROG-03 Leaderboards | 3 | Requires XP, anti-abuse, and active population; ranking model confirmed | PROG-02, PROG-05, NOTIF-03 |
| PROG-04 Challenges | 4 (groundwork Phase 3) | Full challenges require leaderboard and community infrastructure | PROG-03, COMMUNITY-01 |
| PROG-05 Fairness / Anti-Abuse | 3 (mandatory) | Must ship before or with PROG-02; leaderboards without this are a spam vector | MOD-01, architecture analytics |
| PROG-06 Territory | Phase 5 TBD | Explicitly post-MVP (ADR-0011) | Dedicated ADR, PROG-05 |

### Achievement Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| ACH-01 Achievement and Badge System | 3 | Requires activity history; meaningful only with XP and streak data | PROFILE-04, PROG-05 |
| ACH-02 Achievement Showcase | 3 | Requires ACH-01 to populate | ACH-01, PROFILE-02 |

### Sharing Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| SHARE-01 Share Card System | 2 | Screenshot-worthy artifact; L8 viral loop seed; high Phase 2 value | CREATE-01, design assets |
| SHARE-02 Fart Share Surfaces (incl. deep link / app-open attribution) | 2 | Smallest viral unit; required for the viral loop to produce installs | SHARE-01, attribution |
| SHARE-03 Profile, Milestone, and Rank Shares | 3 | Requires identity investment to be worth sharing | PROFILE-02, PROG-03, ACH-02 |
| SHARE-04 Challenge and Community Result Shares | 4 | Requires challenges and groups | PROG-04, COMMUNITY-01 |

### Monetization Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| MON-01 Cosmetic Economy (a la carte avatars and sounds) | 1 | Owner decision: purchase path live at launch (ADR-0016, ADR-0020) | PROFILE-01, CREATE-07 free set |
| MON-01 Cosmetic Economy (full catalog) | 4 | Deeper catalog expansion after Phase 3 foundations | MON-01 foundations |
| MON-02 Premium Subscription | 4 | Requires deep cosmetic catalog to justify recurring payment | MON-01 mature catalog |
| MON-03 Rewarded Ads | 1 | Single-use avatar/sound unlocks; AdMob with server-side verification (ADR-0017, ADR-0020) | MON-01 |

### Notification Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| NOTIF-01 Notification Type Framework | 1 (reaction-received push only), 2 (full) | Phase 1 needs the L2 return trigger; the full framework follows the social graph | SOCIAL-01; SOCIAL-03 for Phase 2 |
| NOTIF-02 Notification Preferences | 2 | Must ship with NOTIF-01; control is a trust requirement | NOTIF-01, PRIV-03 |
| NOTIF-03 Rank / Streak / Challenge Hooks | 2 (groundwork), 3 (full) | Streak reminders in Phase 2; rank and challenge hooks in Phase 3 | PROG-01, PROG-03 |

### Community Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| COMMUNITY-01 Groups | 4 | Requires active social graph and competitive layer | SOCIAL-03, PROG-03 |
| COMMUNITY-02 Group Challenges and Shared Goals | 4 | Requires groups and the challenge system | COMMUNITY-01, PROG-04 |
| COMMUNITY-03 Seasonal Events and Leagues | 4 | Requires leaderboards and groups | PROG-03, COMMUNITY-01 |

### Privacy Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| PRIV-01 Visibility Controls | 1 | Required for safe launch; visibility selector is in the MVP log flow | CREATE-01, ADR-0010 |
| PRIV-02 Location Privacy | 1 | Precision fuzzing and sensitive-zone protection must ship at launch | MAP-01, CREATE-01 |
| PRIV-03 Consent, Account, and Data Controls | 1 (baseline: permissions, age gate, sign-out, delete account), 2 (full settings center) | Trust basics ship at launch; social-graph settings follow | PRIV-01, PRIV-02; SOCIAL-03, NOTIF-02, ADR-0012 for Phase 2 |

### Moderation Systems

| Feature | Phase | Reason | Hard dependency |
|---|---|---|---|
| MOD-01 User Safety Controls (Block, Report; Mute and comment controls) | 1 (report + block), 2 (full) | Report and block must exist before any public content is live | CREATE-01, SOCIAL-01 |
| MOD-02 Content Moderation Pipeline | 1 (baseline: report intake, auto-hide at 3 reports, manual review), 2 (tooling); audio coverage before CREATE-08 | Baseline protects Phase 1 UGC; tooling scales with volume | MOD-01 |
| MOD-03 Anti-Spam, Anti-Spoofing, and Integrity | 1 (basic), 3 (full) | Basic spam limits at launch; competitive integrity before leaderboards | PROG-05 for Phase 3 |
| MOD-04 Group and Community Moderation | 4 | Group moderator roles require community infrastructure | COMMUNITY-01, MOD-02 |

---

## 6) Launch Success Gates

A phase gate is not a date. It is a set of measurable conditions that authorize proceeding. Shipping Phase 2 features before Phase 1 gates pass is a sequencing violation, regardless of how complete the Phase 2 code is.

All metric targets reference `Metrics.md` §4–5. Counter-metrics reference `Metrics.md` §8.

### Gate 1: Phase 1 → Phase 2

**Signal:** The core loop is fun and place-anchored for new users.

| Criterion | Target | Source |
|---|---|---|
| First-session loop completion | ≥ 60% of new installs complete map → log → marker in session 1 | `Metrics.md` Activation KPI |
| Time-to-first-log | Median < 60 seconds on first session | `Product.md` L1 health bar |
| First social touch | ≥ 40% of new users give or receive a reaction within 24h | `Metrics.md` First Reward KPI |
| L1 leak rate | Log abandonment < 30% on the detailed log screen | `Product.md` L1 leak points |
| Safety baseline | MOD-01 report flow operational; PRIV-01 and PRIV-02 validated; no unmitigated T1/T2 threats | `ContentSafety.md` §2 |
| Monetization integrity | Entitlement bypass impossible in rules tests; rewarded unlocks single-use and server-verified; daily cap enforced | ADR-0016, ADR-0017 |

**Hard blockers (must be zero):** Unmitigated Critical threats (T1 doxxing, T2 stalking, T4 minor safety, T9 secrets). Any of these present = gate does not pass regardless of loop metrics.

---

### Gate 2: Phase 2 → Phase 3

**Signal:** People bring people back. The social flywheel has started.

| Criterion | Target | Source |
|---|---|---|
| D7 return rate | ≥ 20% | `Metrics.md` Retention KPI |
| Social graph formation | ≥ 50% of D7-retained users have ≥ 3 approved followers | `Metrics.md` Social density KPI |
| WASR (North Star) | Measurable and growing week-over-week | `Metrics.md` §3 |
| L2 loop health | Reaction reciprocity rate positive (reactors receive reactions back) | `Product.md` L2 health |
| L4 loop seed | Follow graph growing; discovery surfaces used | `Product.md` L4 health |
| Notification trust | Notification opt-out rate < 20%; no spike in uninstalls after notification campaigns | `Metrics.md` §8 trust guardrail |

---

### Gate 3: Phase 3 → Phase 4

**Signal:** Activity accumulates into habit and identity users protect.

| Criterion | Target | Source |
|---|---|---|
| D30 return rate | ≥ 10% | `Metrics.md` Retention KPI |
| Streak adoption | ≥ 30% of D7-retained users have active streak ≥ 3 days | `Metrics.md` Habit KPI |
| Profile investment | ≥ 35% of retained users have customized signature or profile | `Metrics.md` Identity KPI |
| Leaderboard abuse rate | Anti-abuse detection rate low; no viral spam-farming reports | `ProgressionSystems.md` PROG-05 |
| L5 loop health | Streak continuation rate healthy; streak reminder open rate > 30% | `Product.md` L5 health |
| L6 loop seed | Rank-check frequency measurable; leaderboard sessions growing | `Product.md` L6 health |

---

### Gate 4: Phase 4 → Phase 5 (define scope)

**Signal:** Community creates self-sustaining content. Product can grow through community, not only acquisition.

| Criterion | Target | Source |
|---|---|---|
| WASR | Consistent week-over-week growth | `Metrics.md` §3 |
| K-factor | ≥ 0.4 | `Metrics.md` Virality KPI |
| Group participation | Measurable recurring activity in groups; events completed | `Metrics.md` §5 Phase 4 |
| L7 loop health | Active groups; recurring event cadence; low dormant-group rate | `Product.md` L7 health |
| Moderation load | Community moderation (MOD-04) handling group content sustainably | `ContentSafety.md` §8 |

**Note:** Passing Gate 4 does not define Phase 5 scope. It authorizes Phase 5 scope definition. The output of Gate 4 is a product leadership conversation about what Phase 5 should contain, using the known candidates (Territory, seasonal systems, creator program, international) as starting points.

---

## 7) Explicit Non-Goals

The following must never be built. They are not "deferred" or "Phase 5+" candidates — they are permanent exclusions because they conflict with the product's identity, pillars, or vision.

Any proposal touching these areas must be rejected at the hard gate stage in `Product.md` (Anti-pillar gate or Vision gate).

### Direct Messaging

**Why never:** Fart Map is a map-social product. Private one-to-one messaging decouples interaction from location and turns the product into a messaging app with a map layer. It creates a harassment and stalking vector that is qualitatively different from public social interaction. It fragments social energy away from the map.

**What to build instead if users want private communication:** Emoji reactions and comments on follower-scoped farts provide enough personal interaction. Challenges and group activity provide shared coordination space.

### Generic Social Networking

**Why never:** A generic social feed, an algorithmic interest graph, trending topics, and content discovery by category are all anti-Location, anti-Humor moves. The moment Fart Map becomes navigable without a map it has lost its core identity.

**The test:** If the feature would make equal sense in an app with no map, it does not belong in Fart Map.

### Feed-First Design

**Why never:** The map is the product. The feed is a convenience surface for catching up on follower activity between map sessions. Any design decision that makes the feed the primary surface — default tab, deeper feature investment than the map, or content types that only exist in the feed — is a pillar violation.

**Enforcement:** The map tab must always be the launch destination. Feed must never be the default screen.

### Pay-to-Win Mechanics

**Why never:** Purchasing XP, levels, ranks, achievement unlocks, extra fart logging capacity, or competitive advantages destroys trust and fairness simultaneously. It is permanently forbidden (see `Monetization.md` §2).

**What is allowed:** Cosmetic purchases (avatars, sounds, signatures, frames) that express identity without affecting scores, ranks, or content access.

### Utility and Productivity Features

**Why never:** Calendar integration, to-do lists, health tracking, commute logging, location history analysis for productivity purposes — all of these are anti-Humor anti-Identity moves that flatten the product into a utility. Fart Map is entertainment first.

### Energy Systems and Wait Timers

**Why never:** Limiting how often users can log farts (per day, per hour) through an energy system is the fastest way to kill the core loop. The loop depends on low-friction access. If a user wants to log three farts in a row during a funny moment, nothing should stop them except the daily XP soft cap that prevents gaming (not limits content creation).

### Interstitial and Banner Advertising

**Why never:** Passive ad placements (banners on the map, interstitials between sessions or actions) degrade the humor experience and erode trust. The only acceptable ad format is opt-in rewarded ads (Phase 1, ADR-0017/ADR-0020) where the user initiates the ad exchange.

### Loot Boxes and Randomized Purchases

**Why never:** Randomized cosmetic purchases (pay to receive a random item from a pool) are gambling-adjacent mechanics that build distrust. Users pay to get a specific item they chose, or they earn items through behavior. Randomization has no place in Fart Map's monetization.

---

## 7a) Resolve Before Phase N (known open conflicts, not decided here)

| Topic | Conflict | Resolve before |
|---|---|---|
| Push preferences | NOTIF-02 "must ship with NOTIF-01", but Phase 1 ships reaction push; Phase 1 relies on the OS permission | Phase 1 launch (owner) |
| Streak qualifier | CoreLoops: log or react qualifies; ProgressionSystems: react does not | Phase 2 streak foundation |
| Daily log cap | Monetization implies a log cap; ProgressionSystems has a 20/day soft XP cap; Product.md forbids log limits. Current reading: no log cap, XP soft cap only | Phase 3 |
| Report thresholds | Farts auto-hide at 3 reports (Phase 1); SocialSystems hides comments on 1 report | Phase 2 comments |
| Algorithmic feed | SocialSystems plans ranking in Phase 3; §7 bans algorithmic feeds | Phase 3 |
| Minor-safe defaults | Users aged 13–17 are permitted; minor-specific defaults are undefined | Phase 1 launch (owner) |
| Sensitive / private zones | ContentSafety requires them for logging; no mechanism is specified | Phase 1 launch |

## 8) AI Decision Rules for Release Planning

Future agents using this document to make release or sequencing decisions must follow these rules. They are not guidelines — they are constraints.

1. **Check phase gates before scoping.** Before proposing any Phase N feature, verify that Phase N-1 gates have passed (or been marked as tracking toward passing). If they have not, the proposal belongs in the backlog, not the next sprint.

2. **Features without a phase assignment are blocked.** Every feature in `features/` has a phase assignment. If a proposed feature is not in `features/` or does not have a clear phase assignment derivable from the Feature Placement Matrix (§5), it must be added to `features/` with an explicit phase before it enters planning.

3. **Safety cannot be scheduled away.** If a feature requires safety controls and those controls are not ready to ship, the feature is blocked — not the controls. Shipping the feature first and adding safety "in the next sprint" is not acceptable.

4. **Non-goals are hard stops.** If a proposal maps to any item in §7, reject it at the gate stage. Do not score it. Do not redesign it. The category is excluded.

5. **Phase 5 is not definable until Gate 4 passes.** Do not create Phase 5 roadmap items, epics, or stories until Gate 4 criteria are met and scope is formally defined by product leadership. The known candidates in §3 are placeholders, not commitments.

6. **Cut breadth, never depth.** If scope must be reduced for a phase, remove features at the outer edges of the phase (lower pillar scores, weaker loop connections) before reducing quality, polish, or safety of features at the core of the phase.

7. **Phase completion is measured, not declared.** A phase is not complete because the code is shipped or the sprint is done. It is complete when the gate criteria in §6 are measurably passing. Use `Metrics.md` as the measurement authority.

8. **When a gate is failing, fix the loop — do not add a new loop.** If Phase 1 activation is underperforming, the answer is to improve logging friction, marker feedback, or map density — not to ship Phase 2 social features to compensate. Adding a new loop to fix a broken loop creates compounding technical and product debt.

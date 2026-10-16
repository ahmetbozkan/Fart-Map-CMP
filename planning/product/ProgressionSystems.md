# Progression Systems — Fart Map

> **Authority level:** This document expands the PROG-01–05 (PROG-06 territory is deferred, ADR-0011) and ACH-01–02 entries in `Features.md` with authoritative XP models, streak rules, leaderboard design, anti-abuse controls, and achievement categories. When this document and `Features.md` conflict, this document wins for progression-specific behavior. Always read `Features.md` §10 and §11 first for pillar scores, phase assignments, and loop mapping.

**Related documents:**
- `Features.md` §10 Progression Systems (PROG-01–05)
- `Features.md` §11 Achievement Systems (ACH-01–02)
- `CoreLoops.md` — L5 (Progression/Streak), L6 (Competition/Ranking)
- `UserJourney.md` — Stage 5 (Habit), Stage 7 (Identity Investment)
- `ContentSafety.md` — anti-abuse and fairness requirements
- `Glossary.md` — progression entity terms
- Open TBDs: all five resolved (see §10)

---

## Contents

1. [Design Philosophy](#1-design-philosophy)
2. [PROG-01 Streak System](#2-prog-01-streak-system)
3. [PROG-02 Levels and XP System](#3-prog-02-levels-and-xp-system)
4. [PROG-03 Leaderboards](#4-prog-03-leaderboards)
5. [PROG-04 Challenges](#5-prog-04-challenges)
6. [PROG-05 Fairness and Anti-Abuse Layer](#6-prog-05-fairness-and-anti-abuse-layer)
7. [ACH-01 Achievement and Badge System](#7-ach-01-achievement-and-badge-system)
8. [ACH-02 Achievement Showcase](#8-ach-02-achievement-showcase)
9. [Cross-System Data Contracts](#9-cross-system-data-contracts)
10. [Resolved Decisions — Progression](#10-resolved-decisions--progression)
11. [Implementation Validation Checklist](#11-implementation-validation-checklist)

---

## 1) Design Philosophy

### The only acceptable progression is humor-forward and fairness-grounded.

Progression in Fart Map must never:
- Gate content creation behind a progression wall. Anyone can log a fart at level 1 or level 100. Progression rewards are cosmetic and social, never functional.
- Reward raw volume over quality. Spam-logging 100 farts to climb the leaderboard must be detectably less effective than meaningful participation.
- Punish casual users. Streak loss, ranking drops, or missed challenges must feel like a shrug, not a shame spiral. Non-punitive design is a hard rule (PROG-01 note).
- Create pay-to-win. Premium cosmetics tied to progression are acceptable. Premium XP boosts or purchasable rank are not acceptable — ever.

### Humor is the reward medium.

Level-up moments, achievement unlocks, streak milestone celebrations, and leaderboard rank changes must be communicated with humor. Generic "Congratulations!" dialogs are inadequate. The progression system's reward delivery is part of the product's humor identity.

### Phase gate reminder

Progression features are Phase 3. Do not build them before Phase 1 (core loop) and Phase 2 (retention/social) are healthy. Specifically:
- L1 (Explore → Log → See) must be closed and healthy.
- L2 (Social Reaction) must be active.
- PROG-05 (anti-abuse) must ship before PROG-02 (XP/levels) or PROG-03 (leaderboards).

---

## 2) PROG-01 Streak System

### What counts as a streak day

A streak is maintained by completing **at least one qualifying action** within a **calendar day** in the user's local timezone. Timezone must be resolved at log time, not server time. UTC-based streak days will punish users in different timezones and are not acceptable.

**Qualifying actions (Phase 2 definition):**

| Action | Qualifies for streak? |
|---|---|
| Log a fart (any visibility) | Yes |
| React to another user's fart | No (too passive) |
| Comment on another user's fart | No |
| Open the app | No |

**Rationale:** Streak must reward creation, not passive consumption. A streak driven by opens or reactions trivializes the metric and generates low-value behavior.

### Streak lifecycle

```
Day 0: first qualifying action → streak = 1
Day 1: qualifying action → streak = 2
...
Day N+1: no qualifying action → streak broken → streak = 0
```

- A streak is broken if the user completes no qualifying action for an **entire calendar day** in their timezone.
- There is no grace period. No streak freeze, no skip token, no premium recovery, ever (permanent closure, §10 TBD-4).

### Streak display and milestones

| Milestone | Celebration behavior |
|---|---|
| 3 days | Small in-app toast: "3 days straight. You have a problem." |
| 7 days | Full-screen moment + animated streak badge on profile |
| 30 days | Full-screen moment + "Chronic Offender" title unlocked |
| 100 days | Full-screen moment + permanent hall-of-fame badge |
| 365 days | Full-screen moment + "The Legend" title |

Labels above are examples. Final copy must be written by the humor/product owner before implementation. All labels must be humorous. All milestones must be acknowledged with a full-screen animation, not a toast, from the 7-day milestone onward.

### Streak data

```
UserStreak {
  user_id:           String
  current_streak:    Int       // consecutive qualifying days
  longest_streak:    Int       // all-time high; never resets
  last_qualified_at: Long      // epoch ms; last qualifying action time
  streak_started_at: Long      // epoch ms; start of current streak
}
```

`longest_streak` must persist even after a streak break. It is a profile stat (PROFILE-04) and an achievement signal (ACH-01).

### Notification hook

NOTIF-02 must send a streak reminder when:
- User has a streak ≥ 3 days AND
- Has not completed a qualifying action in the current calendar day AND
- It is within 4 hours of midnight in the user's timezone.

Notification copy must be humorous and escalate with streak length. Examples (not final):
- 3-day streak: "Don't let today be the day you stopped caring."
- 7-day streak: "7 days. One more and it's a lifestyle. Don't quit now."
- 30-day streak: "You've gone 30 days. The streak is more committed than most relationships."

---

## 3) PROG-02 Levels and XP System

> **Dependency:** PROG-05 (anti-abuse) must be designed in parallel and must ship together or before XP. XP without anti-abuse is an invitation to spam.

### XP philosophy

XP rewards **meaningful, diverse participation** — not raw volume. The XP formula must make it harder to game by spamming one action type than by playing naturally across multiple behaviors.

### XP sources

| Action | Base XP | Notes |
|---|---|---|
| Log a fart (PUBLIC) | 10 | Full value; social contribution |
| Log a fart (FOLLOWERS) | 8 | Slightly reduced; smaller social footprint |
| Log a fart (PRIVATE) | 3 | Minimal; private content has no social value |
| Receive a reaction | 2 per reaction | Capped at 20 XP per fart (anti-spam: receiving 100 reactions on one fart should not be a progression exploit) |
| Receive a comment | 5 per comment | Capped at 30 XP per fart |
| React to another user's fart | 1 | Minimal; passive social contribution |
| Comment on another user's fart | 3 | Encourages social engagement |
| Complete a challenge | 25–100 | Challenge-specific, defined at challenge creation |
| Earn an achievement | 15–50 | Achievement-specific, defined per badge |
| Maintain a streak milestone | 10–30 | Per milestone tier (3, 7, 30, etc.) |

**Daily XP cap:** 150 XP/day hard cap, enforced server-side; XP events above the cap are discarded (resolved, §10 TBD-3).

**Intensity weighting on XP:** **Resolved — mocked at 0% for MVP.** The `intensity_value` field is captured at log time and stored, but it contributes no XP in the current formula. Reserved for future reintroduction if an abuse-safe weighting model can be designed. Do not add intensity-based XP without a new explicit decision.

### Level thresholds

Levels use an exponential curve. Early levels must be achievable quickly (L1→L5 in the first week of normal use) to establish the reward loop. High levels must represent genuine long-term commitment.

| Level range | Approximate XP required per level |
|---|---|
| 1–10 | 100–300 XP |
| 11–25 | 300–800 XP |
| 26–50 | 800–2,000 XP |
| 51–100 | 2,000–10,000 XP |
| 100+ | Prestige territory (Phase 4+) |

Exact thresholds must be tuned during Phase 3 based on expected daily active behavior. The table above is a planning estimate, not a final value.

### Level identity and labels

Each level has a humorous label. Examples (not final — require humor/product copy owner sign-off):

| Level | Label |
|---|---|
| 1 | "First Offender" |
| 5 | "Repeat Offender" |
| 10 | "Chronic" |
| 20 | "Neighborhood Menace" |
| 50 | "Regional Legend" |
| 100 | "Global Disaster" |

Level label is displayed on the user's profile (PROFILE-02) and visible to others. It must always be readable as a humorous rank, not a generic "Level 10" badge.

### Level-up moment

Level-up must trigger a full-screen animated celebration, not a toast. The celebration must include:
- The new level label (e.g., "You are now a Regional Legend")
- A humorous one-liner specific to the level tier (not a generic congratulations message)
- Any cosmetic unlock granted by the level (if applicable; cosmetics on level-up are a Phase 3+ decision)

### XP data model

```
UserXP {
  user_id:        String
  total_xp:       Int         // cumulative all-time XP
  current_level:  Int
  xp_to_next:     Int         // XP remaining until next level
  xp_today:       Int         // resets at midnight local timezone
  last_xp_at:     Long        // epoch ms
}

XPEvent {
  event_id:       String
  user_id:        String
  source:         Enum        // LOG | REACTION_RECEIVED | COMMENT_RECEIVED | CHALLENGE | ACH | STREAK
  amount:         Int
  fart_id:        String?     // nullable; only when XP sourced from a fart interaction
  created_at:     Long        // epoch ms
}
```

`XPEvent` is a write-append log used for auditing, anti-abuse analysis, and streak/achievement source verification. It must not be deleted.

---

## 4) PROG-03 Leaderboards

> **Dependency:** PROG-05 anti-abuse and PROG-02 XP system must both be in place before leaderboards are shipped. A leaderboard without anti-abuse is a spam incentive.

### Leaderboard types (MVP)

| Type | Scope | Period |
|---|---|---|
| Global — All Time | All users | All time |
| Global — Weekly | All users | Rolling 7 days |
| Local — Weekly | Users within ~50km radius | Rolling 7 days |
| Followers — Weekly | Users with an approved follow relationship with the viewer in either direction | Rolling 7 days |

**Followers leaderboard is the most important for retention.** It serves L6 (Competition) and L4 (Social Connection) simultaneously. A user with zero approved followers cannot see a followers leaderboard — show an empty state with a "Follow people to compete with them" CTA linking to discovery.

### Ranking model

**Status: Confirmed.** The following signal weights are authoritative for MVP. Do not change without an explicit product decision.

| Signal | Weight | Anti-abuse consideration |
|---|---|---|
| XP earned in period | 50% | Capped per day (PROG-02); spam resistant |
| Received reactions in period | 20% | Capped per fart; rewards social resonance |
| Streak consistency in period | 15% | Rewards daily engagement; hard to fake |
| Challenge completions in period | 10% | Defined XP; tamper-resistant |
| Intensity weighting | **0% (mocked)** | Field is captured at log time but not used in ranking for MVP. Reserved for Phase 3+ if abuse-safe weighting can be designed. |

Effective total weight: 95%. The remaining 5% is unallocated until Intensity weighting is revisited. It must not be silently redistributed to other signals — leave the gap as-is.

**Anti-gaming constraint:** A user who logs 50 private farts in a single day to farm XP must rank lower than a user who logs 5 public farts and earns 10 reactions. The XP differential (10 vs 3 per fart, capped reactions) plus reaction signal should enforce this naturally, but must be verified in anti-abuse testing.

### Leaderboard display

Each leaderboard entry shows:
- Rank position
- User avatar and username
- Level label
- Score for the period (composite score per §9)
- Delta from last period (up/down/new)
- "View Profile" CTA

**User's own rank:** Always shown at the bottom of the visible list even if outside the top N, with surrounding context (rank N-1, N, N+1) so the user knows where they stand.

**Ranking is public for public-scope leaderboards.** Users on the global and local leaderboard cannot opt out of appearing — their score is derived from public activity. Users with no public activity will not appear.

**Followers leaderboard:** Appearance is limited to approved followers. No opt-out needed — it is inherently privacy-scoped.

### Leaderboard refresh cadence

- Global and local leaderboards: recalculated every 1 hour.
- Followers leaderboard: recalculated every 15 minutes (smaller scope, higher social sensitivity).
- All leaderboards show a "Last updated X minutes ago" timestamp.

---

## 5) PROG-04 Challenges

> **Phase assignment: Phase 4 (groundwork in Phase 3).** This section defines the challenge model for planning purposes. Do not build this until PROG-03 and PROG-05 are stable.

### Challenge types

| Type | Description | Scope |
|---|---|---|
| Solo | Individual user vs a defined target | Global/personal |
| Follower | Compete with your follower network | Followers leaderboard scope |
| Community | Group participates collectively | Phase 4, COMMUNITY-01 dependency |

### Challenge anatomy

```
Challenge {
  challenge_id:  String
  title:         String         // humorous, copy-owner defined
  description:   String
  type:          Enum           // SOLO | FOLLOWER | COMMUNITY
  goal:          ChallengeGoal  // see below
  start_at:      Long           // epoch ms
  end_at:        Long           // epoch ms
  xp_reward:     Int
  badge_id:      String?        // awarded on completion
}

ChallengeGoal {
  metric:        Enum   // FART_COUNT | REACTION_COUNT | STREAK_DAYS | LOCATION_VARIETY | FLAVOR_VARIETY
  target_value:  Int
  period_days:   Int    // rolling window
}
```

**MVP goal types:**

| Metric | Example challenge |
|---|---|
| `FART_COUNT` | "Log 5 farts this week" |
| `REACTION_COUNT` | "Earn 10 reactions this week" |
| `STREAK_DAYS` | "Maintain a 7-day streak" |
| `LOCATION_VARIETY` | "Log farts in 3 different neighborhoods" |
| `FLAVOR_VARIETY` | "Log every flavor level at least once" |

### Challenge reward rules

- XP reward is fixed at challenge creation. It cannot be purchased or boosted.
- Badge reward (if any) is a one-time award. Completing the same recurring challenge type again does not grant a second badge — only first-time completion grants it.
- Cosmetic track unlocks (e.g., a challenge-exclusive avatar or sound) are a Phase 4 monetization opportunity. They must be cosmetic only and not affect challenge scoring.

---

## 6) PROG-05 Fairness and Anti-Abuse Layer

> **This system is a hard prerequisite for PROG-02 and PROG-03.** Shipping XP or leaderboards without these controls is a product integrity failure.

### Abuse vectors and mitigations

| Vector | Description | Mitigation |
|---|---|---|
| Bulk private fart spam | User logs hundreds of private farts to farm XP | Reduced XP for private farts (3 vs 10); daily XP cap |
| Reaction ring | Group of users mutually react to each other's spam content | Reaction XP capped per fart; diminishing returns for reactions from same user |
| Location spoofing | User logs farts at fake locations to game local leaderboard | GPS verification heuristics; server-side coordinate validation; suspicious location flagging |
| Fake account amplification | Multiple accounts boosting one account's reactions | Account age gating for reaction XP contribution; rate limits |
| Challenge farming | Exploiting a challenge loop for unlimited XP | Challenge XP is fixed; one-time badge; XP daily cap applies |

### Rate limits (proposed — require sign-off before implementation)

| Action | Limit |
|---|---|
| Farts logged | 20 per user per day (soft; above this, XP = 0 but content is still posted) |
| Reactions sent | 100 per user per day |
| Comments sent | 50 per user per day |
| XP earned | 150 per user per day (hard cap; excess XP events are discarded) |

Rate limits are enforced server-side. Client-side enforcement is not sufficient.

### Anti-abuse signals for ranking exclusion

Users meeting any of the following criteria are excluded from public leaderboards (not from personal XP tracking):

- Account age < 7 days (prevents fake account farming)
- Flagged for location spoofing (by automated heuristic or manual moderation report)
- Account suspended or under active moderation review
- XP source mix is ≥80% from a single action type in the scoring period (gaming signal)

Excluded users see a "Your account is under review" message in place of their leaderboard rank. They are not publicly visible on leaderboards during the exclusion period.

### Trust score (internal, not user-facing)

A lightweight `trust_score` is maintained per user for internal moderation use only. It is never displayed to users or referenced in any user-visible surface. Inputs:

- Account age (higher age = higher trust)
- Diversity of XP sources (diverse = higher trust)
- Report history (reports against = lower trust)
- Location consistency (consistent = higher trust)

Trust score is used to weight reactions for XP purposes and to flag accounts for manual review. It is not a public ranking signal.

---

## 7) ACH-01 Achievement and Badge System

### Achievement philosophy

- Achievements must be **discoverable** (some are visible in the achievement list before unlock), **earnable** (no achievement is locked behind a purchase), and **displayable** (every achievement can be shown on profile).
- **Premium display frames** are acceptable monetization. **Paid achievements** (paying to unlock a badge you didn't earn) are not acceptable — ever.
- Achievements must span multiple behavior categories so that multiple persona types (Rival, Explorer, Identity Builder, Instant Joker) each have a meaningful progression path.

### Achievement categories

| Category | Description | Example achievements |
|---|---|---|
| **Consistency** | Streaks and session frequency | "7-Day Streak", "30-Day Streak", "Logged Every Day for a Year" |
| **Exploration** | Logging in new or diverse locations | "First Fart in 5 Neighborhoods", "Cross-Country Offender", "Airport Arrival" |
| **Humor / Flavor** | Flavor and content variety | "Try All 5 Flavors", "First Lethal", "Stinky Streak (7 days of Stinky+)" |
| **Social Impact** | Received social engagement | "First Reaction", "50 Reactions", "Viral Moment (100 reactions on one fart)" |
| **Community** | Social contribution | "First Follower", "10 Approved Followers", "Reacted to 50 Farts" |
| **Milestones** | Pure volume/time milestones | "First Fart", "10 Farts", "100 Farts", "1,000 Farts" |
| **Challenges** | Challenge-specific | "Completed First Challenge", "Won a Follower Challenge" |

**MVP minimum:** At least one achievement per category must be shippable with Phase 3. Achievement list may be sparse at launch; it grows over time.

### Achievement unlock flow

1. User completes qualifying action.
2. Backend evaluates achievement conditions asynchronously (not in the critical path of the action).
3. If a new achievement is unlocked: queue a push notification + in-app celebration.
4. In-app celebration: full-screen moment with the badge displayed + humorous copy + "Add to Showcase" CTA.
5. Achievement is immediately visible in the user's achievement collection (PROFILE-05).

### Achievement data model

```
Achievement {
  achievement_id:   String
  title:            String
  description:      String
  category:         Enum      // CONSISTENCY | EXPLORATION | HUMOR | SOCIAL | COMMUNITY | MILESTONE | CHALLENGE
  condition:        AchievementCondition   // defined at achievement creation
  xp_reward:        Int
  badge_asset_id:   String    // art asset reference
  is_secret:        Bool      // if true, not shown in list until unlocked
}

UserAchievement {
  user_id:          String
  achievement_id:   String
  unlocked_at:      Long      // epoch ms
}
```

### Secret achievements

A small set of achievements should be secret (`is_secret: true`) — not visible in the achievement list until unlocked. Secret achievements reward discovery and create organic sharing moments ("I just unlocked a secret badge"). Recommend 5–10 secret achievements at Phase 3 launch. Examples (not final):

- Log a fart at an airport
- Log a fart between midnight and 3am
- Receive a "Legend" reaction on your first post

Secret achievements must not be required for any achievement showcase slot — they are pure discovery rewards.

---

## 8) ACH-02 Achievement Showcase

### Purpose

Let users curate which achievements are publicly visible on their profile, turning badges from a personal log into a social identity signal.

### Showcase rules

- A fixed number of **showcase slots** are available on the profile (recommend 3–5 for MVP).
- The user selects which unlocked achievements fill their showcase slots.
- Empty showcase slots are displayed as locked/blank — they signal that there is room to fill, not that the profile is incomplete.
- Achievements not in the showcase are still accessible on the full achievement collection page (PROFILE-05).
- **Premium showcase themes** (background styles, animation frames for the showcase grid) are an acceptable monetization surface. The slots themselves are free.

### Showcase display

Showcase is visible on the user's public profile (PROFILE-02) to:
- The user themselves always.
- Approved followers if the profile is follower-scoped.
- All authenticated users if the profile is public.

Showcase must include a "Share" CTA (SHARE-01 integration) to generate a shareable achievement card.

---

## 9) Cross-System Data Contracts

### Summary of progression entities

| Entity | Key fields | Primary key | Notes |
|---|---|---|---|
| `UserStreak` | `user_id, current_streak, longest_streak, last_qualified_at, streak_started_at` | `user_id` | One record per user |
| `UserXP` | `user_id, total_xp, current_level, xp_to_next, xp_today, last_xp_at` | `user_id` | One record per user |
| `XPEvent` | `event_id, user_id, source, amount, fart_id?, created_at` | `event_id` | Append-only log |
| `Achievement` | `achievement_id, title, description, category, xp_reward, badge_asset_id, is_secret` | `achievement_id` | Static; defined at build |
| `UserAchievement` | `user_id, achievement_id, unlocked_at` | `(user_id, achievement_id)` | One per unlock |
| `Challenge` | `challenge_id, title, type, goal, start_at, end_at, xp_reward, badge_id?` | `challenge_id` | Time-bounded |

### Leaderboard read model

Leaderboard scores are derived, not stored raw. The score computation should run on a background job (Cloud Function or equivalent) that writes a pre-computed `LeaderboardEntry` collection. Client reads from the pre-computed collection — it must never compute leaderboard rank on the client.

```
LeaderboardEntry {
  board_id:        String     // GLOBAL_ALLTIME | GLOBAL_WEEKLY | LOCAL_WEEKLY | FOLLOWERS_WEEKLY
  user_id:         String
  score:           Int        // composite score for the period
  rank:            Int        // rank in this board
  delta:           Int        // change from last period (+N, -N, 0, null for new entries)
  computed_at:     Long       // epoch ms
}
```

---

## 10) Resolved Decisions — Progression

All five TBDs from the initial draft of this document have been resolved. Decisions are recorded below and reflected in the relevant sections. They are not open for re-discussion without a new explicit product decision session.

### TBD-1: Ranking model final weights — ✅ Resolved

**Decision:** Confirmed as proposed. Signal weights are 50% XP / 20% Reactions / 15% Streak / 10% Challenges. Intensity = 0% (mocked). Effective total = 95%. The unallocated 5% stays at zero until Intensity is revisited.

See §4 Ranking model table.

### TBD-2: Intensity weighting in XP and ranking — ✅ Resolved

**Decision:** Intensity is mocked at 0% for both XP and ranking in MVP. The field is captured and stored. It does not influence any score computation until a future explicit decision introduces it with abuse controls.

See §3 XP sources table and §4 Ranking model table.

### TBD-3: Daily XP cap value — ✅ Resolved

**Decision:** 150 XP/day hard cap. Enforced server-side. XP events above the cap are discarded (not queued, not deferred — discarded). Rate limiting is enforced at the action level (per PROG-05) before XP is even computed.

### TBD-4: Streak freeze — ✅ Resolved (closed permanently)

**Decision: No streak freeze. Ever.** This is not a deferral — it is a permanent closure. The streak must be earned through daily qualifying actions. No skip token, no freeze, no premium recovery mechanism. If future churn data creates pressure to revisit, that conversation must produce a new ADR, not a reopen of this TBD.

### TBD-5: Following list visibility — ✅ Resolved

**Decision:** Following list (the list of users a person follows) is **private by default**. It is never visible on a public profile without the user explicitly opting in. Only the **follower count** (a single integer) is publicly visible on the profile. The full following list is accessible only to the user themselves.

A toggle — "Show who I follow on my profile" — must be available in profile privacy settings (PRIV-01 / PROFILE-02). Default state: off. This is captured in ADR-0012.

---

## 11) Implementation Validation Checklist

**Streak:**
- [ ] Streak day is computed in user's local timezone, not server UTC
- [ ] Qualifying action is log-only; opens/reactions do not qualify
- [ ] `longest_streak` persists across streak breaks
- [ ] Streak milestones trigger full-screen celebration (7d and above), not toast
- [ ] NOTIF-02 reminder fires within 4 hours of midnight for users with active streak and no qualifying action

**XP and Levels:**
- [ ] PROG-05 ships concurrently with or before PROG-02
- [ ] Private fart XP is 3, not 10
- [ ] Reaction and comment XP caps per fart are enforced server-side
- [ ] Daily XP cap enforced server-side
- [ ] Level-up triggers full-screen celebration with humorous copy
- [ ] XPEvent log is append-only; no deletes

**Leaderboards:**
- [ ] PROG-05 ships before PROG-03
- [ ] Ranking model confirmed (50% XP / 20% Reactions / 15% Streak / 10% Challenges / 0% Intensity) — ✅ resolved
- [ ] Pre-computed LeaderboardEntry; no client-side rank calculation
- [ ] User's own rank always visible (with context rows) even if outside top N
- [ ] Followers leaderboard shows empty state with follow CTA for users with zero followers
- [ ] Accounts < 7 days old excluded from public leaderboards

**Anti-abuse:**
- [ ] Rate limits server-side (farts, reactions, comments, XP)
- [ ] Location spoofing detection heuristic implemented before leaderboard launch
- [ ] Trust score maintained internally; never user-facing
- [ ] XP source diversity check flags single-source farming

**Achievements:**
- [ ] At least one achievement per category at Phase 3 launch
- [ ] Paid achievement unlock is architecturally impossible (no SKU or purchase path for badge unlock)
- [ ] Secret achievements not shown in list until unlocked
- [ ] Achievement unlock is asynchronous; does not block the qualifying action
- [ ] "Add to Showcase" CTA shown immediately on unlock celebration

**No progression surface:**
- [ ] Gates content creation (anyone can log a fart regardless of level)
- [ ] Rewards raw volume equally to social/diverse participation
- [ ] Creates a pay-to-win path (premium = cosmetics only, never XP or rank)

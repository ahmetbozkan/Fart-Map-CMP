# ProductPillars.md

> **Planning spine position:** derived from `Vision.md`; governs everything downstream.
> Upstream: `Vision.md`. Downstream: `Personas.md`, `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md`, and all `Features.md` / Systems scoring.
> The five pillars (Location, Social, Humor, Progress, Identity) are the permanent scoring axes referenced by every later product document.

## Product Pillars for Fart Map

## 1) Purpose of Product Pillars
Product Pillars are the permanent decision system for Fart Map. They exist to ensure that product choices remain consistent over years of development, across changing features, changing roadmaps, and changing AI agents.

These pillars are not feature ideas. They are governance principles used to evaluate whether work should be approved, delayed, redesigned, or rejected.

Use this document to answer:
- Is this aligned with Fart Map's core purpose?
- Does this improve long-term engagement and social entertainment value?
- Is this a distraction that makes the product generic?
- Should this be built now, later, or never?

If a proposal cannot clearly map to the pillars, it should not be prioritized.

---

## 2) Pillar Hierarchy (Decision Filter System)
Pillars are filters, not decorations.

Rules:
- Every major feature must strengthen at least one core pillar.
- Strong features strengthen two or more pillars.
- Features that do not strengthen any pillar are non-strategic.
- Features that weaken a pillar must be rejected or redesigned.
- Roadmap priority goes to features with high multi-pillar impact.

Practical interpretation:
- **Single-pillar support** = possibly useful, needs strong justification.
- **Multi-pillar support** = high strategic value.
- **No pillar support** = likely off-strategy.
- **Anti-pillar direction** = reject.

---

## 3) Core Pillars

## A) Location
Location is the foundation of Fart Map. Without geography, Fart Map becomes a generic feed product.

Why this pillar exists:
- The world is the game board.
- Every meaningful action should feel anchored in place.
- Location turns events into exploration, memory, and context.

Long-term strategic outcomes:
- The map feels alive with activity.
- Users build a spatial history of their life and movement.
- Place creates curiosity, comparison, and return behavior.

Examples of location-driven value:
- Map exploration
- Territory systems (Phase 5 candidate, ADR-0011)
- Nearby activity
- Location memory/history
- Travel-based identity

AI decision questions:
- Does this make the world feel more alive?
- Does this use geography in a meaningful way?
- Does this create local context, not just global noise?
- Would this still matter if location were removed? (If yes, it may be off-pillar.)

Failure patterns to avoid:
- Features that could exist unchanged in any non-map app.
- UI decisions that hide or reduce the role of place.
- Feed-only behavior with weak map relevance.

---

## B) Social
Fart Map should feel participatory, not solitary. Social interaction converts isolated jokes into ongoing shared entertainment.

Why this pillar exists:
- Social feedback creates repeat usage.
- Interaction creates emotional investment.
- Communities and rivalries create durable retention loops.

Long-term strategic outcomes:
- Users react to each other, not only post and leave.
- Conversations and friendly competition emerge naturally.
- Shared rituals form across friends, groups, and communities.

Examples of social value:
- Reactions
- Comments
- Following/follower dynamics
- Groups
- Shared events and challenges

AI decision questions:
- Does this create interaction between people?
- Does this create conversation or response loops?
- Does this increase user-to-user visibility and connection?
- Does this strengthen social reasons to return?

Failure patterns to avoid:
- Broadcast-only features with no interaction layer.
- Passive consumption without social response.
- Social complexity without actual conversation value.

---

## C) Humor
Humor is the emotional engine of the product. If the app stops being funny, it loses its core identity.

Why this pillar exists:
- Fart Map is entertainment-first.
- Humor drives memorability and shareability.
- Absurdity differentiates Fart Map from standard social products.

Long-term strategic outcomes:
- The app consistently produces moments worth laughing at.
- Users feel playful, not performative.
- The product stays culturally internet-native and meme-friendly.

Examples of humor value:
- Absurd interactions
- Comedic framing and language
- Unexpected but delightful social moments
- Formats that produce memorable stories

AI decision questions:
- Is this actually funny in user experience, not just concept?
- Is this likely to create a memorable moment?
- Does this preserve the playful tone of Fart Map?
- Would users screenshot or retell this moment?

Failure patterns to avoid:
- Over-serious UX that strips personality.
- Generic design language with no playful signature.
- Features that increase utility but decrease delight.

---

## D) Progress
Progress gives users a reason to come back tomorrow. It turns one-off interactions into long-term journeys.

Why this pillar exists:
- Progress supports retention.
- Visible growth creates commitment.
- Goals create motivation beyond novelty.

Long-term strategic outcomes:
- Users feel momentum over days, weeks, and seasons.
- Activity becomes meaningful through accumulation.
- Competition and mastery systems emerge naturally.

Examples of progress value:
- Achievements
- Streaks
- Ranks/leaderboards
- Collections
- Territory growth (deferred, ADR-0011)

AI decision questions:
- Does the user feel growth over time?
- Is progress visible and understandable?
- Does this reward consistency and meaningful action?
- Does this create return motivation without grind abuse?

Failure patterns to avoid:
- Flat experiences with no sense of momentum.
- Progress systems that reward spam over meaningful participation.
- One-time novelty features with no long-term loop.

---

## E) Identity
Identity makes users recognizable and emotionally invested. It transforms usage into reputation.

Why this pillar exists:
- People return to protect and grow their public self-image.
- Identity creates social meaning and status.
- Profile history turns activity into narrative.

Long-term strategic outcomes:
- Users develop distinctive presence and style.
- Reputation emerges through behavior and history.
- Profiles become social objects, not static account pages.

Examples of identity value:
- Profiles
- Personal style and signature expression
- Reputation signals
- Public history and milestones

AI decision questions:
- Does this help users build a recognizable identity?
- Does this strengthen profile meaning or reputation?
- Does this increase continuity of personal story?
- Does this create something users are proud to show?

Failure patterns to avoid:
- Anonymous-only design that prevents reputation formation.
- Uniform experiences with no personal expression.
- Features that erase public continuity/history.

---

## 4) Pillar Relationships (How Pillars Compound)
The strongest product outcomes come from pillar combinations, not isolated pillar wins.

Key relationship patterns:

- **Location + Social**
  - Place-based interaction creates local relevance and social curiosity.
  - "What happened here?" becomes "Who did this?"

- **Location + Identity**
  - Geography becomes personal history.
  - Users build recognizable presence across cities and routes.

- **Social + Humor**
  - Laughter becomes shared, not private.
  - Reactions/comments multiply entertainment value.

- **Progress + Identity**
  - Streaks, ranks, achievements become reputation signals.
  - Growth is not just numeric; it becomes status and story.

- **Humor + Shareability**
  - Funny moments become screenshots, stories, and referrals.
  - Virality emerges from user-created social artifacts.

Planning rule:
- Prioritize concepts that trigger at least two of these relationship patterns.

---

## 5) Pillar Conflict Resolution
When pillars or constraints conflict, resolve in this order:

1. **Vision Alignment**
2. **User Enjoyment**
3. **Social Value**
4. **Progression Value**
5. **Development Simplicity**

Tradeoff principles:
- Never choose simplicity if it materially harms vision.
- Never optimize short-term speed by removing core product personality.
- If humor and utility conflict, prefer humor unless trust/safety is harmed.
- If social value conflicts with isolated progression, prefer social-first progression.
- If two options are equal on vision and enjoyment, choose the simpler one.

Decision protocol for conflicts:
- State the conflict explicitly.
- Score both options across all pillars.
- Apply priority order above.
- Record rationale so future agents preserve consistency.

---

## 6) Feature Evaluation Framework (1-5 Scoring)

For every proposed feature, AI agents must score:

- **Location Impact (1-5)**
- **Social Impact (1-5)**
- **Humor Impact (1-5)**
- **Progress Impact (1-5)**
- **Identity Impact (1-5)**

Score definitions:
- **1** = no meaningful contribution / possible pillar harm
- **2** = weak or indirect contribution
- **3** = clear but limited contribution
- **4** = strong contribution
- **5** = transformative contribution to that pillar

Decision thresholds:
- **20-25 total**: high strategic priority (build soon)
- **15-19 total**: viable (build with scope control)
- **10-14 total**: weak (redesign before committing)
- **<10 total**: off-strategy (reject)

Hard gates:
- Any feature with **Humor = 1** and **Social = 1** should be rejected unless it is foundational infrastructure.
- Any feature with **Location = 1** requires explicit justification, since map relevance is core identity.
- Any feature with two or more pillar scores at **1** should not enter sprint planning without redesign.

Now vs Later guidance:
- Build now: high score + supports current strategic phase.
- Build later: high potential but weak readiness/dependency fit.
- Reject: low score or anti-pillar trajectory.

---

## 7) Anti-Pillars: What Fart Map Should Never Become
Fart Map should never drift into:

- A generic social network
- A chat application
- A utility tool
- A dating platform
- A productivity app
- A health tracker

Anti-pillar test:
- If removing humor does not change feature value, it is likely off-strategy.
- If removing location does not change feature value, it is likely off-strategy.
- If feature value depends more on private messaging than map-social play, it is likely off-strategy.

These categories are strategic traps. Avoiding them protects differentiation.

---

## 8) AI Decision Rules (Mandatory for Autonomous Planning)
Future AI agents must follow these rules:

1. Prefer features that support multiple pillars.
2. Reject features that weaken identity continuity or reputation.
3. Reject features that flatten humor into generic utility.
4. Prefer systems that create stories users retell.
5. Prefer systems that generate visible social interaction.
6. Prefer systems that generate shareable moments.
7. Prefer map-native ideas over feed-generic ideas.
8. Prefer progression systems that reward meaningful participation, not spam.
9. Protect product personality even when simplifying scope.
10. Do not approve roadmap items that cannot explain pillar impact.
11. If a proposal is strategically ambiguous, redesign before implementation.
12. When forced to cut scope, cut breadth first, not core pillar expression.

Non-negotiable rule:
- Never sacrifice product vision solely to reduce implementation effort.

---

## 9) Strategic Phases

Product pillars remain stable over time, but planning emphasis shifts by phase.

### Phase 1: Core Loop

**Goal**
Prove that users enjoy opening the app, logging farts, and seeing them appear on the map.

**Focus**
- Map
- Logging
- Profiles
- Basic social feedback

**Success**
Users complete the `map -> fart -> reaction` loop.

---

### Phase 2: Social Retention

**Goal**
Create reasons to return.

**Focus**
- Following
- Feed
- Notifications
- Comments

**Success**
Users return because of other users.

---

### Phase 3: Identity and Progression

**Goal**
Turn engagement into habits.

**Focus**
- Streaks
- Achievements
- Leaderboards

**Success**
Users return because of progression.

---

### Phase 4: Community

**Goal**
Create network effects.

**Focus**
- Groups
- Challenges
- Events
- Seasonal systems

**Success**
Communities create content.

---

### Phase 5: Expansion

**Scope: TBD.** Defined only after the Phase 4 gate passes (`ReleaseStrategy.md` §3). Territory is a candidate, not a commitment (ADR-0011).

---

## 10) Pillar Weighting by Stage

Pillar weighting sets execution emphasis for each stage. It does not replace pillar governance.

### Phase 1 (Core Loop)

Per-phase weights are owned by `ReleaseStrategy.md` §3 and repeated here for scoring convenience:

- Location: 35%
- Humor: 30%
- Social: 20%
- Identity: 10%
- Progress: 5%

Planning interpretation:
- Prioritize map-native core loop quality.
- Keep social and humor present from day one.
- Introduce progression and identity as lightweight foundations that can scale later.

### Phases 2–4

Use the per-phase weights in `ReleaseStrategy.md` §3: Phase 2 Social 35 / Location 25 / Humor 20 / Identity 10 / Progress 10; Phase 3 Progress 30 / Identity 25 / Social 25 / Location 15 / Humor 5; Phase 4 Social 30 / Progress 25 / Identity 20 / Location 15 / Humor 10.

Planning interpretation:
- Increase investment in social systems and return loops.
- Expand progression and identity into deeper long-term systems.
- Preserve location and humor so growth does not dilute product differentiation.

---

## Operational Use in Planning
Use Product Pillars at all planning levels:
- Epic definition
- Story refinement
- Sprint planning
- UI flow decisions
- Monetization decisions
- Experiment prioritization
- Feature kill decisions

Every planning artifact should include:
- Pillar impact statement
- 1-5 pillar scorecard
- Conflict notes (if any)
- Build now/later/reject recommendation

This keeps long-term direction stable while allowing feature-level flexibility.

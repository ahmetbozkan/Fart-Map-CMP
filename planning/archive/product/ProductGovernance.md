# Product Governance: Fart Map

> **Planning spine position:** the permanent product constitution. Authority level: highest in the planning system. When this document and any other planning document conflict, this document wins.
> Upstream: `Vision.md`, `ProductPillars.md`, all ADRs. Downstream: every other planning document.
> This document protects product identity from drift. It does not specify features, architecture, or implementation. It defines what Fart Map fundamentally is, what it must never become, and the framework all future product decisions must pass through — regardless of who makes them.

---

## Contents

1. [Product Identity](#1-product-identity)
2. [Non-Negotiable Product Principles](#2-non-negotiable-product-principles)
3. [Product Decision Framework](#3-product-decision-framework)
4. [Anti-Patterns](#4-anti-patterns)
5. [Monetization Guardrails](#5-monetization-guardrails)
6. [Social Guardrails](#6-social-guardrails)
7. [AI Planning Guardrails](#7-ai-planning-guardrails)
8. [Change Management](#8-change-management)
9. [Final Product Constitution](#9-final-product-constitution)

---

## 1) Product Identity

### What Fart Map is

Fart Map is a **location-based social entertainment game** for mobile.

It is a product where people leave humorous, location-anchored marks on a shared map, react to each other's moments, build a social identity through participation, and compete and collaborate in a playful shared world.

The world is the game board. Every action is anchored to a place. Every interaction is designed to produce a laugh, a rivalry, or a story.

It starts as a joke anyone understands in five seconds. It grows into a social platform with genuine long-term retention loops.

The core user fantasy is: *"I can leave my mark on the world, get seen, and build my own ridiculous legend."*

### What Fart Map is not

Fart Map is not any of the following. These are categorical exclusions, not deferred features.

| Category | Why it is excluded |
|---|---|
| A serious social network | Seriousness removes the humor identity that makes the product unique and shareable |
| A chat / messaging application | Private messaging decouples interaction from location and creates a generic messaging layer |
| A utility application | Utility removes playfulness; productivity is anti-humor and anti-engagement |
| A dating platform | Dating dynamics create safety and trust failure modes incompatible with the location+humor stack |
| A health tracker | Health optimization framing conflicts with playful, humor-first self-expression |
| A content publishing platform | Fart Map is about map moments, not long-form content creation or media distribution |
| A generic feed-first social app | Feed-first design reduces location to a filter instead of the game board |

### Product category statement (for future agents)

When any planning decision is unclear, ask: *"Is this a location-based social entertainment game move, or is it something else?"* If the answer is "something else," it requires explicit justification against the product identity, not a default approval.

---

## 2) Non-Negotiable Product Principles

These six principles are the permanent operating constraints of Fart Map. They do not change with phases, roadmap pivots, or market conditions. They can only be changed by an explicit, documented product decision recorded in a new ADR and in this document.

### Principle 1 — Map First

The map is not a feature. The map is the product.

Every action, social surface, progression system, and identity expression must be anchored to the map — either because it happens on the map, references a location, or produces output that returns the user to the map.

The feed is a convenience layer. The profile is a record of map activity. The leaderboard is a rank derived from map-anchored behavior. Nothing in the product should make navigating the map optional.

**Governance test:** If removing the map from a feature does not change the feature's value, the feature does not belong in Fart Map.

**Failure pattern:** Any design that makes the feed, leaderboard, or profile the default launch destination instead of the map.

### Principle 2 — Humor First

If the product stops being funny, it loses its reason to exist.

Humor is not a tone setting. It is a structural requirement. Every screen, every label, every empty state, every achievement name, every error message, every notification copy must reflect the product's humor identity. The moment the product sounds like a generic app, it has drifted.

Humor is also the viral mechanism. Funny moments get shared. Shared moments bring new users. The humor identity of the product is its growth engine.

**Governance test:** If a feature increases utility but removes delight, it needs redesign. If a feature is technically correct but not funny, the copy and framing must be fixed before shipping.

**Failure pattern:** Generic UX language ("Post saved", "Upload complete", "Welcome back") where humorous alternatives are possible ("Logged. The world felt it.", "Uploaded. Science can't explain it.", "You're back. We smelled you coming.").

### Principle 3 — Social Through Location

Social interaction in Fart Map derives its meaning from place. Reactions, comments, rivalries, and challenges are not generic social actions — they are responses to things that happened somewhere specific.

"Someone reacted to your fart at the Eiffel Tower" is a socially meaningful statement in a way that "Someone liked your post" is not. Location is what turns generic engagement into a shared story.

Every social surface must preserve the location context of the content it surfaces. A feed item that strips location is weaker. A notification that omits location context is weaker. A share card with no location is missing its most powerful element.

**Governance test:** Does this social feature make more sense because of where the content happened? If not, it is a generic social feature sitting on top of a map product.

**Failure pattern:** Feed items, notifications, or share cards that omit the location context of the moment they reference.

### Principle 4 — Identity Through Participation

A user's identity in Fart Map is earned through what they do, not what they buy.

Level, rank, streak, and achievement history are the result of activity. Profile reputation is built through social engagement. A user who has logged 500 public farts and earned reactions from 200 people has a meaningful Fart Map identity. A user who purchased premium cosmetics but logs nothing does not.

Premium cosmetics enrich identity expression — they give users more ways to show who they are. They do not create identity from nothing. Purchased items decorate an identity that must be earned through participation.

**Governance test:** Does this feature let a user buy status, rank, or social credibility? If yes, reject it. Does it let a user buy a more expressive way to display earned status? If yes, it may be acceptable.

**Failure pattern:** Any mechanism that lets a user purchase social proof (purchased followers, purchased achievement display, purchased rank advancement).

### Principle 5 — Fair Competition

Status in Fart Map reflects activity. The user at the top of a leaderboard got there by logging farts, earning reactions, maintaining streaks, and completing challenges — not by spending money or exploiting a mechanical edge.

Fairness is a trust asset. If users believe the leaderboard is pay-to-win or exploit-vulnerable, the entire progression and competition system loses meaning. The value of a rank is entirely dependent on users believing it was earned fairly.

**Governance test:** Does this feature create any path to higher rank, more XP, or more achievements through payment or mechanical exploitation? If yes, reject it regardless of revenue potential.

**Failure pattern:** XP boosts, purchased rank resets, paid challenge entries with better rewards, or any mechanism where money produces a competitive advantage.

### Principle 6 — Cosmetic Monetization Only

Fart Map earns money by selling expression, not access or advantage.

The free tier is a complete product experience. Premium users have more ways to express themselves — more avatars, more sounds, voice recording, more signatures, profile frames. They do not get a functionally better product. They get a more expressive one.

This principle is not a moral stance. It is a product strategy. A free tier that feels crippled produces hostility toward monetization, churn at the paywall, and negative reviews. A free tier that feels complete produces users who enjoy the product, see premium as additive, and convert willingly.

**Governance test:** Does this require payment to perform a core action (logging, reacting, following, viewing the map)? If yes, reject it. Does it require payment for a cosmetic enhancement to a core action? If yes, it is acceptable.

**Failure pattern:** Locking the map to paying users, limiting daily fart logs without payment, requiring premium for follow functionality, or paid achievements.

---

## 3) Product Decision Framework

Every proposed feature, design change, or roadmap addition must pass through this framework before it is accepted. No exceptions for small changes — small drift accumulates into large drift.

### Step 1 — Hard gates (run first, before any scoring)

From `Prioritization.md` §3. A proposal failing any gate is rejected or sent back for redesign. It does not proceed to scoring.

| Gate | Check |
|---|---|
| Safety gate | Does this create an unmitigated safety or privacy threat? (`ContentSafety.md` §2) |
| Anti-pillar gate | Does this push the product toward a generic social network, chat app, utility, or dating product? (`ProductPillars.md` §7) |
| Vision gate | Does this contradict what Fart Map is not? (§1 of this document; `Vision.md` §13) |
| Trust gate | Does this improve a metric by breaking a trust guardrail? (`SuccessMetrics.md` §8) |
| Monetization gate | Does this create a pay-to-win or pay-for-access path? (§5 of this document; `Monetization.md` §2) |
| Phase gate | Is this proposal phase-appropriate given current gate status? (`ReleaseStrategy.md` §6) |

### Step 2 — Five product questions

Answer all five. If any answer is "no" or "unclear", the proposal needs redesign before scoring.

1. **Does it strengthen location?** Does the feature make the map more alive, more meaningful, or more tied to physical reality?
2. **Does it strengthen humor?** Does the feature produce moments worth laughing about, sharing, or retelling?
3. **Does it strengthen identity?** Does the feature help users build a recognizable, earned presence?
4. **Does it strengthen social interaction?** Does the feature create interaction between people, or does it only serve solitary use?
5. **Does it strengthen progression?** Does the feature give users a reason to return, grow, or improve over time?

### Step 3 — Pillar scorecard

Score the proposal on each pillar (1–5) using `ProductPillars.md` §6.

| Pillar | Score (1–5) |
|---|---|
| Location | |
| Social | |
| Humor | |
| Progress | |
| Identity | |
| **Total** | **/25** |

Decision thresholds:
- **20–25:** High strategic priority — build soon
- **15–19:** Viable — build with scope control
- **10–14:** Weak — redesign before committing
- **< 10:** Off-strategy — reject

### Step 4 — Loop and phase fit check

- Which core loop (L1–L8) does this proposal strengthen? (If none, question it strongly.)
- Is this proposal phase-appropriate? (Does it belong in the current phase or a future one?)
- Does shipping this now risk weakening a current-phase loop that is not yet healthy?

### Step 5 — Record the decision

Every accepted proposal records:
- Pillar scores
- Which loop it strengthens
- Phase assignment
- Primary metric it intends to move (`SuccessMetrics.md`)
- Counter-metric it must not harm
- Traceability chain (see §8)

Every rejected proposal records the gate or score that rejected it, so future agents do not re-propose the same rejected idea without new information.

---

## 4) Anti-Patterns

These are specific behaviors that damage Fart Map's product identity. They are not hypothetical — each one has a real-world analogue in products that drifted from their identity. Every planning agent must be able to recognize and reject these patterns without escalation.

### Anti-Pattern 1 — Feed replacing map

**What it looks like:** The feed becomes the most feature-rich surface. New content types exist only in the feed. The map is updated infrequently while the feed receives investment. The default launch screen drifts to the feed.

**Why it kills the product:** Fart Map without the map is a low-quality Twitter clone. The map is what makes every interaction meaningful and unique. Losing it loses the product.

**The early signal:** Feature proposals that only touch the feed and have no map-anchoring component.

### Anti-Pattern 2 — Generic social creep

**What it looks like:** Follow suggestions based on interest graph. Trending topics. Hashtag systems. "For You" algorithmic content discovery with no location component. Long-form text posts. Stories format.

**Why it kills the product:** Each of these individually seems harmless. Together, they turn Fart Map into a generic social app that happens to have a map tab. The product loses its identity before anyone notices.

**The early signal:** Any proposal that references "content discovery" without referencing location.

### Anti-Pattern 3 — Utility creep

**What it looks like:** Location check-ins for practical purposes (tracking where you parked, logging routes). Health integrations. Productivity features. Calendar events attached to farts. Location history as a utility tool.

**Why it kills the product:** Utility removes humor. The moment the app is useful for a serious purpose, the humor identity feels incongruous and the product loses its comedic permission.

**The early signal:** Any proposal framed around "being helpful" rather than "being funny."

### Anti-Pattern 4 — Punitive mechanics

**What it looks like:** Losing progress for missing a day. Streak-shaming notifications. Level-down mechanics. Rank penalties for inactivity. Loss-framing in UI copy.

**Why it kills the product:** Fart Map's tone is playful and self-deprecating. Punitive mechanics introduce stress and obligation, which are anti-humor and anti-retention. Users uninstall apps that make them feel bad.

**The early signal:** Any mechanic where user inaction causes something to be taken away rather than merely stopping progress.

### Anti-Pattern 5 — Progression inflation

**What it looks like:** Too many things reward XP. Every action gives a badge. Level milestones are frequent. Achievements are trivial to unlock.

**Why it kills the product:** Progression meaning depends on progression scarcity. If everything gives XP, nothing feels like an accomplishment. The signal that "this person is Level 50" becomes meaningless if Level 50 takes three days.

**The early signal:** XP being added to passive actions (opens, scrolls) or trivially easy behaviors.

### Anti-Pattern 6 — Notification spam as retention

**What it looks like:** High notification volume to drive D7/D30 numbers. Push notifications for weak signals (someone viewed your profile). Daily "check in" reminders with no specific social context. Streaks of notification types unrelated to real social events.

**Why it kills the product:** Users revoke notification permissions, then uninstall. Notification permission is a trust asset that takes one bad campaign to lose permanently. Every notification must earn its place with a specific, valuable, social, or progress-driven reason.

**The early signal:** Retention metrics improving while notification opt-out rate or uninstall-after-notification rates rise.

### Anti-Pattern 7 — Monetization pressure

**What it looks like:** Premium prompts on map load. Paywalls on core loop actions. Cosmetics shown as locked in contexts that interrupt the experience. Countdown timers on "limited" offers. Copy that shames free users.

**Why it kills the product:** Monetization pressure degrades the product experience for the majority of users who are not currently paying. Non-paying users are a product's most important acquisition channel. A free user who loves the product will eventually pay or bring someone who does. A free user who feels exploited uninstalls and warns others.

**The early signal:** Any monetization prompt that appears without explicit user action to open a cosmetic selector.

### Anti-Pattern 8 — Complexity for its own sake

**What it looks like:** Multi-step creation flows with no clear reward. Progression systems with too many overlapping currencies (XP, coins, tokens, gems). Achievement categories that feel arbitrary. Feature flags and settings that confuse rather than empower.

**Why it kills the product:** Fart Map's power is that the core is instantly understandable. Complexity must earn its place by producing genuine value for users who seek depth. Complexity that exists to appear sophisticated without improving the experience violates Principle 7 of `ReleaseStrategy.md` (cut breadth, never core expression).

**The early signal:** New users in usability tests spending more than 10 seconds trying to understand what a feature does before they try it.

### Anti-Pattern 9 — Safety deferred

**What it looks like:** "We'll add moderation in the next sprint." "We'll add the privacy setting after launch." "The block function is Phase 2." UGC surfaces going live without their required safety controls.

**Why it kills the product:** An unsafe experience creates real-world harm, generates press coverage and app store flags, and destroys trust. Trust is slower to rebuild than it is to lose.

**The early signal:** Any feature proposal that does not include its required safety controls in its own scope.

### Anti-Pattern 10 — Architecture decisions in product planning

**What it looks like:** Product planning documents specifying database schemas, API endpoints, SDK choices, or implementation details. Features described in terms of their implementation rather than their user value and loop contribution.

**Why it kills the product:** Architecture decisions belong in architecture documents. Product planning that specifies implementation creates brittle constraints that the architecture team cannot optimize. It also means the product plan becomes invalid every time an architecture decision changes.

**The early signal:** Feature descriptions in `Features.md` or planning documents that use technical terminology as their primary framing.

---

## 5) Monetization Guardrails

The full monetization specification is in `Monetization.md`. This section defines the constitutional guardrails — the allowed/forbidden binary that governs every monetization decision. When `Monetization.md` and this document conflict, this document wins.

### What monetization is allowed

| Allowed | Condition |
|---|---|
| Cosmetic item purchases (avatars, sounds, signatures, frames, marker themes) | Must be purely cosmetic; no functional advantage |
| Premium subscription for expanded cosmetic library access | Must not include any competitive or functional advantage |
| Rewarded ads for temporary cosmetic unlock (single-use one action) | Opt-in only; never equivalent to premium; unlock applies to one action only |
| Achievement display frames (cosmetic treatment of earned badges) | Frame is cosmetic; the achievement itself is always free-to-earn |
| Earned cosmetics via challenge/event completion | Permanently owned; not purchasable through payment |
| Seasonal limited cosmetic drops | Must be cosmetic only; must not produce FOMO pressure through functional scarcity |

### What monetization is forbidden

| Forbidden | Why |
|---|---|
| Purchasing XP, levels, or rank | Destroys fair competition (Principle 5) |
| Purchasing achievements or badges | Achievement must reflect earned behavior, not payment |
| Purchasing additional fart logging capacity | Core loop access must be free |
| Purchasing map access or map features | Map is the product; it must be free |
| Purchasing follower count or social proof | Violates Principle 4 (identity through participation) |
| Interstitial ads or banner ads | Degrades the experience for users who have not opted in |
| Loot boxes or randomized purchases | Gambling-adjacent; destroys trust |
| Dark patterns (countdown timers, false scarcity, shaming copy) | Violates trust guardrails (`SuccessMetrics.md` §8) |
| Any paywall on safety, privacy, or moderation features | Safety is never a premium product |
| Subscription that retroactively gates previously free functionality | Retroactive paywalling destroys trust permanently |

### The monetization test

Before any monetization mechanism is accepted: *"If a free user and a premium user play the same challenge, who has a better chance of winning?"*

If the answer is anything other than "the more active player," the mechanism is pay-to-win and must be rejected.

---

## 6) Social Guardrails

### Social graph model (ADR-0009)

The social graph is follow-only with approval. There is no bidirectional "friend" relationship. The target user must approve incoming follow requests. Approved follower status is required for follower-scoped content access.

This model is settled. Do not propose alternatives without a new ADR that addresses the privacy, safety, and social graph consequences.

### Visibility tiers (ADR-0010)

MVP visibility tiers are Public, Followers, and Private. No additional tiers exist without a new ADR. The safe default for new content is Followers — users opt into broader exposure, never out of protection.

### Following list privacy (ADR-0012)

The list of users a person follows is private by default. Only the follower count (a single integer) is publicly visible. Users may opt into making their following list visible via a profile privacy setting. This default protects against social graph surveillance and association-based harassment.

### Map-first social rule

Social interaction must be anchored to content that happened somewhere. Reactions, comments, and sharing actions reference specific farts at specific locations. Any social surface that strips location context weakens the social-through-location principle (Principle 3).

The feed must always include a "View on Map" CTA for every item. This is non-negotiable.

### Moderation philosophy

Moderation exists to protect users, not to sanitize the product's humor identity. The test for moderation action is harm potential (harassment, stalking, minor safety, illegal content), not whether the content is immature or gross. Immature and gross is the product's genre. Harmful is not.

Moderation controls must ship with every new UGC surface. There is no "add moderation later" path.

### Privacy-safe by default

The safe option is always the default. Users opt into exposure — they do not opt out of protection. This applies to:
- Content visibility (default: Followers, not Public)
- Location precision (default: coarse, not precise)
- Following list visibility (default: private, not public)
- Profile discoverability (default: discoverable; opt-out available)

---

## 7) AI Planning Guardrails

Future planning agents — whether generating roadmap items, proposing architecture, designing screens, or evaluating changes — must follow these rules without exception.

### Mandatory preservation checks

Before proposing any change, addition, or removal, an agent must verify:

1. **Vision preserved.** Does this proposal preserve the product vision from `Vision.md` §2, §5, and §13?
2. **Pillars preserved.** Does this proposal strengthen at least one pillar from `ProductPillars.md` §3? Does it avoid weakening any pillar below a score of 3 for the current phase?
3. **Core loops preserved.** Does this proposal preserve the integrity of L1–L3 (primary loops)? Does it not weaken L4–L5 (secondary loops) if they are currently healthy?
4. **Release sequencing respected.** Does this proposal respect the phase assignments and gates from `ReleaseStrategy.md`?
5. **Principles intact.** Does this proposal conform to all six principles in §2 of this document?
6. **Safety compliant.** Does this proposal satisfy the safety controls required by `ContentSafety.md` for its feature class?

If any check produces "no" or "unclear," the agent must either redesign the proposal or escalate for human review. An agent must not proceed with a proposal that fails a preservation check.

### Mandatory cross-reference before proposing features

Every feature proposal must reference:
- The core loop it strengthens (`CoreLoops.md`)
- The persona(s) it serves (`Personas.md`)
- The motivation(s) it satisfies (`UserMotivations.md`)
- The journey stage(s) it advances (`UserJourney.md`)
- The primary metric it intends to move (`SuccessMetrics.md`)
- Its phase assignment (`ReleaseStrategy.md` §5)

A proposal that cannot complete all six references is incomplete and must not be accepted.

### Decision recording requirement

Every planning decision — acceptance, rejection, redesign, or deferral — must be recorded with:
- The proposal that was evaluated
- The gate or score result that determined the outcome
- The rationale, stated in terms of Vision, Pillars, or Loops
- Who or what made the decision (agent, human, session date)

Decision recording is what prevents future agents from re-litigating settled questions and re-proposing rejected ideas.

### Escalation triggers

An agent must escalate to human review (cannot proceed autonomously) when:
- A proposal involves precise user location in any analytics or external payload
- A proposal touches minor safety, legal/compliance, or age verification
- A proposal would add a new monetization category not currently in `Monetization.md`
- A proposal would change a settled ADR decision
- A proposal would change any of the six principles in §2 of this document

---

## 8) Change Management

### The traceability requirement

Every proposed change to an existing product decision, feature, or system must be traceable back through the full product spine. A change that cannot complete this traceability chain is not considered until the chain is established.

**Traceability chain:**

```
Proposed change
    │
    ▼
Vision.md — Does this align with or contradict the product vision?
    │
    ▼
ProductPillars.md — Which pillars does this strengthen or weaken?
    │
    ▼
Personas.md — Which personas benefit? Which are harmed?
    │
    ▼
UserMotivations.md — Which motivations does this address?
    │
    ▼
UserJourney.md — Which stage(s) does this serve or disrupt?
    │
    ▼
CoreLoops.md — Which loop(s) does this strengthen, weaken, or introduce?
    │
    ▼
Features.md — What existing feature specification does this change?
    │
    ▼
Decision — Accept, redesign, defer, or reject. Record rationale.
```

A change that cannot be traced through all steps above is not a mature proposal. It is an idea. Ideas become proposals when they can complete the chain.

### Change categories

| Category | Definition | Required process |
|---|---|---|
| **Minor enhancement** | Adds depth or polish to an existing feature without changing its phase assignment, loop contribution, or pillar score | Traceability chain; record decision |
| **Scope addition** | Adds a new feature or system not currently in `Features.md` | Full decision framework (§3); traceability chain; add to `Features.md` with phase assignment |
| **Phase reassignment** | Moves an existing feature from one phase to another | Traceability chain; justification against `ReleaseStrategy.md` §6 gates; record decision |
| **Principle change** | Proposes modifying one of the six principles in §2 | Human review required; new ADR required; update this document |
| **ADR reversal** | Proposes reversing a settled ADR decision | Human review required; new ADR required; update all documents that reference the original ADR |
| **Non-goal addition** | Proposes building something in `ReleaseStrategy.md` §7 (non-goals) | Rejected at vision gate; if there is a genuinely compelling case, requires explicit human decision and amendment of this document |

### Change freeze triggers

The following events trigger a planning change freeze — no new feature proposals or scope changes are accepted until the freeze condition is resolved:

1. A phase gate (§6 of `ReleaseStrategy.md`) is not passing and the current phase's loop health is under target. All effort goes to loop health, not expansion.
2. A Critical safety threat (T1, T2, T4, T9 from `ContentSafety.md` §2) is identified in production. All effort goes to mitigation.
3. A trust counter-metric (report rate, block rate, notification opt-out) is spiking. All effort goes to diagnosis and repair before new features are added.

---

## 9) Final Product Constitution

*This section is a standalone summary. A future AI agent who has read no other planning document should be able to read only this section and understand the essence of Fart Map with enough fidelity to make defensible product decisions.*

---

**What Fart Map is:**
A location-based social entertainment game for mobile. Users drop humorous markers on a shared map, react to each other's moments, build a social identity through participation, and compete and collaborate in a playful shared world. The map is the game board. Humor is the engine. Social interaction is the fuel. Identity is what keeps users coming back.

**The core user fantasy:**
*"I can leave my mark on the world, get seen, and build my own ridiculous legend."*

**The product in one loop:**
Open map → find something funny nearby → log your own moment → marker appears instantly → someone reacts → you react back → you return tomorrow to keep your streak → you climb the leaderboard → you bring your friends → they bring theirs.

**The six permanent principles:**
1. **Map First.** The map is not a feature — it is the product. Nothing in the app should make the map optional.
2. **Humor First.** If it is not funny, it is not Fart Map. Humor is structural, not a tone setting.
3. **Social Through Location.** Social interactions derive meaning from where they happened. Strip location from a social action and it becomes generic.
4. **Identity Through Participation.** Status is earned by doing things, not purchasing things. Activity creates identity; cosmetics express it.
5. **Fair Competition.** The user at the top of the leaderboard got there by playing, not paying. This is a permanent, non-negotiable constraint.
6. **Cosmetic Monetization Only.** Pay for expression. Never pay for advantage or access to the core product.

**The five strategic phases:**
1. Core Loop — prove the foundational fun is real (map, log, see, react)
2. Social Retention — people bring people back (follow, feed, notifications)
3. Identity and Progression — activity becomes habit and reputation (streaks, XP, levels, achievements)
4. Community — network effects and self-sustaining content (groups, events, challenges)
5. Expansion — scope defined only after Phase 4 is healthy (territory, seasonal, creator, international — TBD)

**The five hard constraints:**
1. The map must be the default launch destination. Always.
2. No feature may gate core creation (logging a fart) behind payment.
3. No feature may create a path to higher rank or more XP through payment.
4. Safety controls must ship with the feature they protect. Never after.
5. The feed must always contain a "View on Map" CTA on every item.

**What Fart Map is not (permanent exclusions):**
Direct messaging. Generic social network. Chat application. Utility tool. Dating platform. Health tracker. Feed-first product. Pay-to-win game.

**The single decision test:**
*"Is this a location-based social entertainment game move?"*
If yes, evaluate it with the decision framework.
If no, reject it at the vision gate.
If unclear, redesign it until the answer is yes or no.

**Who this product is for:**
People who want to laugh with others, build a playful social identity, and experience the world as a shared game board. Primarily: the Instant Joker (wants fast laughs, low friction), the Social Reactor (wants to engage with others' moments), and the Explorer (wants to discover what's funny nearby). Secondarily: the Rival (wants to compete), and the Identity Builder (wants to be recognized); the Community Organizer is a future-tier persona for Phase 4 (`Personas.md`). Not for: utility seekers, productivity users, serious networkers, or anyone who would be embarrassed to show their friends an app called Fart Map.

**The North Star metric:**
Weekly Active Sharers-and-Reactors (WASR) — users who both return and participate socially or via sharing in a 7-day window. If this number is growing, the product is working. If it is flat or declining, the product has a problem that more features will not fix.

**The planning document that owns each concern:**
- Product vision and mission → `Vision.md`
- Feature scoring and strategic fit → `ProductPillars.md`
- Who the product is for → `Personas.md`
- Why users act the way they do → `UserMotivations.md`
- How a user's relationship with the product evolves → `UserJourney.md`
- The repeatable engines of retention → `CoreLoops.md`
- What gets built → `Features.md` + system detail docs
- In what order it gets built → `ReleaseStrategy.md`
- How success is measured → `SuccessMetrics.md`
- What must never be built → this document, §4 and §7 of `ReleaseStrategy.md`
- Settled architecture decisions → `meta/decisions/ADR-*.md`

# Glossary: Fart Map

> **Planning spine position:** cross-cutting vocabulary; feeds every product document.
> Upstream: `Vision.md`, `ProductPillars.md`, `Personas.md`, `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md`. Downstream: `Features.md`, Systems docs, `architecture/DataModel.md`, `design/*`.
> This document is the **single source of truth for domain terms**. Every later doc references these definitions rather than redefining them (per `meta/archive/PlanningDependencyGraph.md` §7, single-definition rule).

---

## 1) Purpose & How to Use

Fart Map's concept uses playful, ambiguous language ("fart", "flavor", "signature", "territory"). The PoC code left several of these as placeholders (`signatureId`, `feelingId`, `flavorId` marked `// tbd` — see `meta/archive/PlanningAuditReport.md` §6). Undefined terms cause inconsistent features, conflicting data models, and divergent UI copy.

This glossary fixes meaning so that:
- AI agents use one definition across product, architecture, and design.
- Data models (`DataModel.md`, `DataContracts.md`) derive field names and types from agreed concepts.
- UI copy stays tonally consistent (Humor pillar) without semantic drift.

**Rules:**
- A term is **defined here once**; other documents link to it.
- If a feature needs a new domain concept, it must be added here **before** the feature is specified.
- Terms marked **TBD (decision required)** must be resolved before any feature depending on them enters planning.
- This is the **domain** glossary. Process/planning terms (epic, story, DoR, skill) belong in `meta/PlanningGlossary.md`.

---

## 2) Conventions

| Marker | Meaning |
|---|---|
| **Status: Stable** | Definition agreed; safe to build on |
| **Status: Provisional** | Working definition; may refine but usable |
| **Status: TBD** | Decision required before dependent features are planned |
| *(pillar tags)* | Pillars the concept primarily serves (from `ProductPillars.md`) |

---

## 3) Core Domain Entities

### Fart
**Status: Stable.** *(Location, Humor)*
The atomic unit of content in Fart Map: a humorous moment logged by a user at a real-world location and time. Appears as a **Marker** on the map. A Fart is the object of nearly all interactions (reactions, comments, sharing). It is the "post" of Fart Map, but is always **place-anchored** — a Fart without a location is invalid.

**Minimum attributes (conceptual, not schema):** id, author, location (lat/lng), timestamp, visibility, and optional expressive attributes (Signature, Flavor, Feeling, Intensity, caption).

### Marker
**Status: Stable.** *(Location)*
The visual representation of a Fart on the map. One Fart = one Marker. Marker appearance may reflect the author's Signature/Flavor (expressive identity). The instant appearance of a Marker after logging is the Reward in Core Loop **L1**.

### Log (verb) / Logging
**Status: Stable.** *(Humor, Location)*
The act of creating a Fart at the user's current location. Must be fast and low-friction (one-tap-class) — the Action step of Loop **L1**. "Log a fart" is the canonical phrasing.

### Map
**Status: Stable.** *(Location)*
The real-world geographic surface that hosts all Markers. The product's center of gravity ("the world is the game board", Vision §1, Pillars §A). **Map vitality** (perceived density + freshness) is a hard product constraint; a sparse/static map starves Loop **L3**.

---

## 4) Expressive Attributes (Identity & Humor Layer)

These were the PoC's `// tbd` placeholders. Definitions below resolve them conceptually; data-layer decisions belong in `DataModel.md`.

### Signature
**Status: Provisional.** *(Identity, Humor)*
A user's personal, recurring visual/stylistic stamp applied to their Farts/Markers — the "this is so me" expression (Motivation **M4**). It is an **identity** concept (recognizable across a user's content), distinct from per-Fart mood. Prime monetization surface (Identity Builder persona). PoC field: `signatureId`.
**Resolved:** single style selection; no composable parts in MVP (ADR-0013).

### Flavor
**Status: Provisional.** *(Humor, Identity)*
A comedic **category/theme** of a Fart — the "type" of moment (e.g., silent-but-deadly, symphony, sneaky). Flavor classifies content for humor variety, filtering, and collection. Per-Fart (unlike Signature, which is per-user). PoC field: `flavorId`.
**Distinction:** *Signature* = who made it (identity); *Flavor* = what kind it is (content category).

### Feeling
**Status: Dropped from MVP (PoC concept).** *(Humor, Social)*
Not part of the Fart entity (`Features.md` CREATE-03.3, `DataModel.md`); retained only as a possible future expressive attribute. The emotional/reaction tone attached to a Fart by its author at creation (e.g., relieved, proud, embarrassed). Adds comedic narrative and personality. Distinct from **Reaction** (which is given by *other* users afterward). PoC field: `feelingId`.

### Intensity
**Status: Provisional.** *(Humor, Progress)*
A magnitude attribute of a Fart (stored as a normalized 0.0–1.0 value, `Features.md` CREATE-03.3). Used for humor framing and potentially for progression/scoring. Must not become a spam-incentive (Progress pillar guardrail: reward meaningful participation, not volume).
**Resolved:** humor-only; 0% weight in XP and ranking until a fair mechanic exists (`ProgressionSystems.md` §10).

### Caption / Comment-on-own
**Status: Provisional.** *(Humor)*
Optional short text a user attaches to their own Fart for comedic framing. Distinct from **Comment** (social interaction by others).

---

## 5) Social Concepts

### Reaction
**Status: Stable.** *(Social, Humor)*
A lightweight, low-effort response another user gives to a Fart (e.g., emoji-style). The Action step of Loop **L2**. Distinct from **Feeling** (author's own tone) and **Comment** (text reply).

### Comment
**Status: Stable.** *(Social)*
A text reply on a Fart by another user. Drives "comment culture" (Social Reactor persona). Heavier-weight than a Reaction.

### Follow (request-based)
**Status: Stable.** *(Social, Identity)*
The social-graph model is **follow-only** with **follow request approval**:
- User A sends a follow request to User B.
- User B must approve for User A to become a follower.
- Only approved followers can access follower-scoped visibility.

This model personalizes social context (Loop **L4**) while preserving privacy control.

### Feed
**Status: Provisional.** *(Social, Location)*
A map-anchored stream of relevant activity (followed users/nearby/notable). Complements the Map; **never replaces it** (Personas §7: "Map is product center; feed complements"). 

### Notification
**Status: Stable.** *(Social)*
An external/in-app prompt that re-triggers a user (a Reaction received, followed-user activity, streak risk). A primary **Trigger** for Loops L2, L4, L5. Must be valuable and well-tuned (Social Reactor churn trigger), never spammy (trust guardrail, Vision §17).

---

## 6) Progression & Competition Concepts

### Streak
**Status: Stable.** *(Progress, Identity)*
A count of consecutive periods (typically days) in which a user performed a qualifying action. Core of Loop **L5** and Motivation **M8** (loss aversion). Must reward consistency ethically, never manipulate.

### Achievement / Badge
**Status: Stable.** *(Progress, Identity)*
A milestone reward for reaching a defined goal. Visible identity/reputation signal. Should reward meaningful participation, not spam.

### Rank / Leaderboard
**Status: Stable.** *(Progress, Social, Identity)*
A relative standing of users (global, local, followers, or group). Engine of Loop **L6** and Motivation **M6** (status). **Fairness and anti-abuse are first-class**; no pay-to-win (Personas §7). Requires a defined scoring source (see Intensity TBD).

### Stat
**Status: Stable.** *(Progress)*
A countable measure of a user's activity (total farts, reactions received, places visited). Feeds profile identity and progression. PoC had a `UserStats` model.

### Season
**Status: Provisional.** *(Progress, Social)*
A recurring time-boxed cycle that resets/segments competition (Loop L6) and events (Loop L7). Creates renewed re-entry pressure. Phase 3–4 concept.

### Territory
**Status: Deferred (post-MVP).** *(Location, Progress, Identity)*
A location-based control/ownership concept where users "claim" or dominate areas through activity. Mentioned across Vision/Pillars/Personas as an aspiration, but its **mechanics are undefined**: Is it per-Fart density? Check-in based? Decaying? Contestable?
Territory is explicitly **out of MVP scope** and should not block MVP feature planning. Re-introduce only with a dedicated ADR covering fairness, anti-abuse, and conflict mechanics.

---

## 7) Identity & Account Concepts

### User / Account
**Status: Stable.** *(Identity)*
A registered participant. PoC supports Google/Apple auth (Apple incomplete — Audit §5). Lightweight start allowed; identity upgrade incentivized (Personas §7).

### Profile
**Status: Stable.** *(Identity, Social)*
A user's public presence: history, Stats, Signature, Achievements, Rank. A **social object**, not a static account page (Pillars §E). Depth/continuity is the Identity Builder's retention anchor.

### Reputation
**Status: Provisional.** *(Identity, Social)*
Emergent public standing derived from behavior and history (consistency, creativity, rank). Not a single field — an aggregate signal. Must persist publicly (Identity Builder churn trigger if it doesn't).

### Visibility
**Status: Stable (but PoC-broken).** *(Identity, Social, Safety)*
Per-Fart audience scope with MVP tiers:
- **Public**
- **Followers** (approved followers only)
- **Private**

**Critical:** the PoC dropped this field on persistence (always PRIVATE — Audit §6). The new data model **must round-trip Visibility faithfully**. Directly tied to privacy (`ContentSafety.md`).

---

## 8) Location Concepts

### Location / Place
**Status: Stable.** *(Location)*
A geographic coordinate (lat/lng) where a Fart is logged. The anchor for every Fart. Raw coordinate vs. human-readable place name may differ; precision handling is a **privacy** concern (`ContentSafety.md`: location fuzzing/precision).

### Nearby Activity
**Status: Stable.** *(Location, Social)*
The set of Farts/events around a user's current location. Powers Loops **L1/L3** and the perception of a living map. Key to Activation (Journey Stage 3).

### Location History / Map Memory
**Status: Provisional.** *(Location, Identity)*
A user's accumulated spatial record — "a spatial history of their life and movement" (Pillars §A). Explorer-persona retention driver. Privacy-sensitive (must be user-controlled).

---

## 9) Growth & Sharing Concepts

### Share (external)
**Status: Stable.** *(Humor, Social, Identity)*
The act of exporting a Fart/milestone/rank outside the app (group chat, short-form platforms). The Action of the meta loop **L8**. Shared artifacts must be screenshot-ready and understandable out of context (Vision §10–11).

### Shareable Moment / Artifact
**Status: Stable.** *(Humor)*
A product-generated, meme-native, context-free piece of content designed to travel and drive new installs. The Reward substrate of Loop L8.

### Viral Loop / K-factor
**Status: Stable.** *(Growth)*
The cycle where Advocacy (Journey Stage 8) produces new Awareness (Stage 1). K-factor = average new users generated per existing user; a target metric in `SuccessMetrics.md`.

---

## 10) Concept Distinctions (Frequently Confused Pairs)

These exist because confusing them produces bad data models and inconsistent UI.

| Pair | Distinction |
|---|---|
| **Signature** vs **Flavor** | Signature = per-*user* identity stamp; Flavor = per-*Fart* comedic category |
| **Feeling** vs **Reaction** | Feeling = author's own tone at creation; Reaction = other users' response after |
| **Caption** vs **Comment** | Caption = author's own framing text; Comment = others' text replies |
| **Follow** vs **Notification** | The relationship vs. the prompt the relationship generates |
| **Stat** vs **Achievement** | Stat = continuous count; Achievement = discrete milestone |
| **Rank** vs **Reputation** | Rank = explicit competitive position; Reputation = emergent aggregate standing |
| **Map** vs **Feed** | Map = product center (geographic); Feed = complementary stream (never replaces Map) |
| **Visibility** vs **Privacy** | Visibility = per-Fart audience setting; Privacy = the broader safety policy governing it (`ContentSafety.md`) |
| **Intensity** vs **Reaction count** | Author-declared magnitude vs. social validation volume |

---

## 11) Decisions Register (all resolved)

| Term | Decision | Status |
|---|---|---|
| **Territory** | Out of MVP; Phase 5 candidate only, needs a dedicated ADR | Resolved (ADR-0011) |
| **Intensity role** | Humor-only; 0% weight in XP and ranking | Resolved (`ProgressionSystems.md` §10) |
| **Signature composition** | Single style selection | Resolved (ADR-0013) |

**Rule:** an agent must not specify a feature depending on a TBD term until that term's decision is recorded as an ADR in `meta/decisions/`. No domain term is currently TBD.

---

## 12) AI Usage Guidance

1. Use these definitions verbatim across product, architecture, and design docs; do not redefine.
2. Before specifying a feature, confirm every domain term it uses is **Stable** or **Provisional** — never **TBD**.
3. If a feature needs a new concept, add it here first (with status + pillar tags + distinctions).
4. Check the decisions register (§11); any new TBD term needs an ADR before dependent planning.
5. Keep tone playful in user-facing copy, but keep these definitions precise — humor lives in the UI, not in the data model.
6. Honor the PoC lessons baked in here: **Visibility must round-trip**, ranking inputs need **anti-abuse**, and placeholder ids (`signatureId`/`flavorId`/`feelingId`) now have meaning — don't reintroduce them as undefined.

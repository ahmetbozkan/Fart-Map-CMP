# Features: Fart Map

> **Planning spine position:** authoritative product capability specification derived from vision, pillars, personas, motivations, journey, and loops.
> Upstream: `Vision.md`, `ProductPillars.md`, `Personas.md`, `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md`, `Glossary.md`, `SuccessMetrics.md`, `ContentSafety.md`, `Prioritization.md`.
> Downstream: `SocialSystems.md`, `ProgressionSystems.md`, `Monetization.md`, `ReleaseStrategy.md`, `roadmap/*`, `design/*`, and architecture planning.
> This document is the canonical source of truth for **what the product contains**, **why it exists**, **when it should appear**, and **what future planning depends on it**.

---

## 1) Purpose & How to Use

This document translates the product spine into concrete feature systems. It exists so future roadmap, epics, stories, design documents, and implementation plans all work from the same authoritative product capability set.

This document is intentionally more complete than a backlog inventory. It defines:
- the feature systems the product needs,
- the user value each system creates,
- the loop/journey/persona reason it exists,
- the dependencies and phase sequencing around it,
- and the monetization/future-expansion space around it.

**Rule:** a feature is only valid if it can explain its value through at least one of:
- a core loop (`CoreLoops.md`),
- a lifecycle stage (`UserJourney.md`),
- a motivation (`UserMotivations.md`),
- and a pillar score (`ProductPillars.md`).

---

## 2) Poop Map Baseline vs Fart Map Evolution

Poop Map proves a strong baseline demand for:
- map-based logging,
- privacy-controlled sharing,
- followers/following,
- likes/feed,
- notifications,
- stats,
- achievements,
- and lightweight competition/community.

Fart Map must evolve beyond that baseline into a **location-based social entertainment platform** by being:
- **more expressive**: richer content creation, avatar/signature/style, flavor/intensity/story/sound,
- **more social**: comments, follower-request dynamics, map-native feed, share-native moments,
- **more replayable**: streaks, levels, achievements, leaderboards, challenges, collections,
- **more viral**: screenshot-ready share cards and social bragging loops,
- **more identity-driven**: profiles as social objects, not just stat pages.

---

## 3) Gap Analysis of the Previous `Features.md` (historical)

> Historical record of the 2026-06 rewrite. It is not a current specification; skip it when implementing.

The previous file was a useful inventory, but not yet a full product specification. Main gaps were:

1. **`Log Fart` was under-scoped.** It was treated as a fast form, not the primary creation engine of the product.
2. **Map systems were too shallow.** The previous doc covered map presence and filters, but not map vitality, discovery layers, place context, or map-memory features needed by Explorer.
3. **Content expressiveness was under-defined.** Flavor, Intensity, Story/Description, sound, audio, avatar, signature, collections, and profile expression were not fully specified.
4. **Social loops were incomplete.** Reactions/comments existed, but follower approval, following/follower views, feed logic, discovery, and social reactivation were under-specified.
5. **Profile systems were too light.** The previous file had a profile surface, but not a full profile object with avatar, stats, history, collections, reputation, and shareable identity.
6. **Progression was too abstract.** Streaks, levels, achievements, collections, and leaderboards needed clearer roles and dependencies.
7. **Sharing was too narrow.** Share cards, profile shares, milestone shares, challenge/result shares, and map-state shares needed explicit treatment.
8. **Monetization was mostly implied.** Cosmetic economy, premium personalization, subscriptions/passes, and reward-based extras were not fully mapped.
9. **Retention systems were fragmented.** Notifications, return hooks, progression triggers, and social pull needed to be specified as a connected layer.
10. **Privacy/moderation were not integrated at feature level.** Safety docs existed, but feature definitions did not consistently embed them.

This rewrite closes those gaps.

---

## 4) Feature Definition Format

Every feature below includes:

| Field | Meaning |
|---|---|
| **Purpose** | Why the feature exists in the product |
| **User value** | What the user gets from it |
| **Pillar impact** | 1-5 impact across Location / Social / Humor / Progress / Identity |
| **Target personas** | Primary beneficiary personas |
| **Primary loops / journey** | Core loops and lifecycle stages served |
| **Phase assignment** | Earliest recommended phase |
| **Dependencies** | Required upstream systems or decisions |
| **Monetization opportunities** | Acceptable monetization surfaces, if any |
| **Future expansion opportunities** | Natural next-step evolution |
| **TBD / notes** | Unresolved design or governance questions |

Phase legend:
- **Phase 1** = Core Loop
- **Phase 2** = Social Retention
- **Phase 3** = Progression
- **Phase 4** = Community
- **Post-MVP** = explicitly out of MVP / later

---

## 5) System Map

This specification is organized into the required product systems:

1. Map systems
2. Content creation systems
3. Social systems
4. Profile systems
5. Progression systems
6. Achievement systems
7. Sharing systems
8. Monetization systems
9. Notification systems
10. Community systems
11. Privacy systems
12. Moderation systems

---

## 6) Map Systems

### MAP-01 Map Home Surface
**Purpose**
The map is the primary home, product center, and entry point into the world of Fart Map.

**User value**
Users immediately understand the concept, see activity around them, and orient to what is happening where.

**Pillar impact**
- Location 5
- Social 3
- Humor 3
- Progress 1
- Identity 2

**Target personas**
- Instant Joker
- Explorer
- Social Reactor

**Primary loops / journey**
- L1 Explore -> Log -> See
- L3 Discovery / Map-Watch
- Journey Stages 2, 3

**Phase assignment**
- Phase 1

**Dependencies**
- MAP-02 marker system
- MAP-03 map filters and layers
- PRIV-01 visibility model

**Monetization opportunities**
- Map theme packs later
- Premium map skins later

**Future expansion opportunities**
- Heatmaps
- travel highlights
- event overlays

  **TBD / notes**
- Must never be replaced by feed-first UX.

### MAP-02 Marker System
**Purpose**
Represent each Fart as a meaningful, expressive, tappable map artifact.

**User value**
The marker is the visible proof that a user's action mattered and is now part of the shared world.

**Pillar impact**
- Location 5
- Social 3
- Humor 4
- Progress 1
- Identity 3

**Target personas**
- Instant Joker
- Explorer
- Identity Builder

**Primary loops / journey**
- L1 Reward
- L3 Discovery
- Journey Stage 3

**Phase assignment**
- Phase 1

**Dependencies**
- CREATE-01 Log Fart engine
- PROFILE-03 signature/style system
- PRIV-02 location privacy and precision control

**Monetization opportunities**
- marker cosmetics
- premium marker styles

**Future expansion opportunities**
- animated markers
- rarity tiers
- seasonal marker effects

**TBD / notes**
- Marker styling depth: signature composition resolved as single style selection (ADR-0013). Marker style is one atomic selection, not a composable set.

### MAP-03 Map Layers, Filters, and Discovery Modes
**Purpose**
Allow users to shift their view between self, approved followers, and public/global discovery.

**User value**
Users can explore the map through different social and relevance lenses without losing place context.

**Pillar impact**
- Location 5
- Social 4
- Humor 2
- Progress 1
- Identity 2

**Target personas**
- Explorer
- Social Reactor

**Primary loops / journey**
- L3 Discovery
- L4 Social Connection
- Journey Stages 3, 6

**Phase assignment**
- Phase 1: all three filters present (ADR-0020); the Followers filter populates in Phase 2 with SOCIAL-03

**Dependencies**
- SOCIAL-03 follow requests
- PRIV-01 visibility model

**Monetization opportunities**
- none direct in MVP
- premium discovery lenses later

**Future expansion opportunities**
- "near me now"
- travel mode
- challenge mode

**TBD / notes**
- Phase 1 filters are `Mine / Followers / Everyone`. The Followers filter shows a humorous empty state until SOCIAL-03 ships (ADR-0020).

### MAP-04 Nearby Activity and Heatmaps (Highlight System)
**Purpose**
Ensure the map feels alive, especially in low-density environments.

**User value**
New users avoid the dead-map problem and returning users always have something to discover nearby.

**Pillar impact**
- Location 5
- Social 3
- Humor 4
- Progress 1
- Identity 1

**Target personas**
- Instant Joker
- Explorer

**Primary loops / journey**
- L3 Discovery
- L2 Social Reaction
- Journey Stages 3, 4

**Phase assignment**
- Phase 2 (`ReleaseStrategy.md` §5). In Phase 1 the Social tab's nearby public list (ADR-0020) covers the low-density case.

**Dependencies**
- MAP-01
- MOD-02 moderation baseline
- `SuccessMetrics.md` instrumentation (Observability event taxonomy)

**Monetization opportunities**
- sponsored themed events later only if humor-safe and non-intrusive

**Future expansion opportunities**
- featured highlights
- location-based spotlights
- weekly absurd moments

**TBD / notes**
- Must avoid fake-feeling activity; can use curated highlights before high density exists.

### MAP-05 Place Detail and Context Surface
**Purpose**
Turn a location into a social storytelling surface, not just a point on a map.

**User value**
Users can understand what happened at a place, who was active there, and why it matters.

**Pillar impact**
- Location 5
- Social 3
- Humor 3
- Progress 2
- Identity 2

**Target personas**
- Explorer
- Social Reactor

**Primary loops / journey**
- L3 Discovery
- L2 Reaction
- Journey Stages 3, 4

**Phase assignment**
- Phase 2

**Dependencies**
- MAP-02
- SOCIAL-01 reactions
- SOCIAL-02 comments

**Monetization opportunities**
- premium travel maps later

**Future expansion opportunities**
- place collections
- city recaps
- trip story reels

**TBD / notes**
- Must respect location fuzzing and private-zone rules.

### MAP-06 Map Memory and Travel History
**Purpose**
Give users a spatial history that reinforces identity and return value over time.

**User value**
Users can relive where they were active and build a ridiculous personal geography.

**Pillar impact**
- Location 5
- Social 2
- Humor 2
- Progress 3
- Identity 4

**Target personas**
- Explorer
- Identity Builder

**Primary loops / journey**
- L3 Discovery
- L5 Identity accretion
- Journey Stage 7

**Phase assignment**
- Phase 3

**Dependencies**
- PROFILE-02 public profile
- PRIV-02 location history controls

**Monetization opportunities**
- premium travel recap themes
- memory exports

**Future expansion opportunities**
- yearly recaps
- trip maps
- city badges

**TBD / notes**
- Public exposure must be optional and coarse.

---

## 7) Content Creation Systems

### CREATE-01 Log Fart Engine
**Purpose**
This is the primary content creation system of the entire product, not a simple form. It converts a passing real-world moment into a location-based social object.

**User value**
Users can quickly transform a funny or memorable moment into a visible, interactive, identity-bearing post on the map.

**Pillar impact**
- Location 5
- Social 4
- Humor 5
- Progress 3
- Identity 4

**Target personas**
- Instant Joker
- Explorer
- Social Reactor
- Identity Builder

**Primary loops / journey**
- L1 foundational action
- feeds L2, L5, L8
- Journey Stages 3, 4, 8

**Phase assignment**
- Phase 1

**Dependencies**
- MAP-01 map home
- MAP-02 marker system
- PRIV-01 visibility model
- MOD-01 / MOD-02 safety baseline (`ContentSafety.md` §10)

**Monetization opportunities**
- cosmetic creation packs later
- premium sound/style packs later

**Future expansion opportunities**
- challenge-linked creation
- collaborative creation formats
- AI-generated humorous captions later if tone-safe

**TBD / notes**
- This system must support both **quick log** and **expressive log** paths.

### CREATE-02 Quick Log Mode
**Purpose**
Provide the fastest possible path from urge to posted marker.

**User value**
Users get the M1 payoff in seconds without losing the joke to friction.

**Pillar impact**
- Location 5
- Social 2
- Humor 5
- Progress 2
- Identity 2

**Target personas**
- Instant Joker
- Explorer

**Primary loops / journey**
- L1 Action
- Journey Stage 3

**Phase assignment**
- Phase 1

**Dependencies**
- CREATE-01
- PRIV-01 visibility defaults

**Monetization opportunities**
- none direct

**Future expansion opportunities**
- one-tap presets
- favorite flavors/sounds

**TBD / notes**
- Should default to the user's last-used safe settings where appropriate.

### CREATE-03 Detailed Log Fart Screen
**Purpose**
Offer a richer creation mode for users who want humor, identity, and story depth beyond a quick drop.

**User value**
Users can turn a fart into a crafted social artifact rather than a bare marker.

**Pillar impact**
- Location 4
- Social 4
- Humor 5
- Progress 2
- Identity 4

**Target personas**
- Social Reactor
- Identity Builder
- Instant Joker (optional use)

**Primary loops / journey**
- L1 Action
- supports L2 and L8 quality
- Journey Stages 3, 4, 8

**Phase assignment**
- Phase 1 base, Phase 2 richer version

**Dependencies**
- CREATE-04 Flavor
- CREATE-05 Intensity
- CREATE-06 Story/Description
- CREATE-07 sound selection
- CREATE-08 audio recording
- PRIV-01 visibility controls

**Monetization opportunities**
- premium creation cosmetics
- premium sticker/frame packs later

**Future expansion opportunities**
- templates
- collaborative challenges
- branded seasonal creation modes

**TBD / notes**
- Suggested screen structure:
  - Where did it happen?
  - When did it happen?
  - Flavor
  - Intensity
  - Story/Description
  - Sound selection
  - Optional audio recording
  - Visibility
  - Preview and post

---

#### CREATE-03.1 — Log Fart Detailed Interaction Model

The Log Fart screen is a **10-step sequential creation flow**. Each step is defined below. Steps may render as a single scrollable screen or a guided stepper — that is a UI/UX decision for implementation. This spec defines the logical steps and constraints, not the visual layout.

**Design constraint:** The complete flow must be completable in under 60 seconds on first session (L1 Action health bar requirement from CoreLoops.md). Every step must have a sensible default so the user can tap through without making a decision.

---

**Step 1 — Location Header**

- Auto-detects GPS at screen open; displays result as a small map thumbnail (coarse only — exact lat/lng is never shown to user, PRIV-02 compliance).
- "Adjust Location" CTA opens a full-screen map picker modal where user can drag a pin.
- Location must resolve (GPS fix obtained or manual pin placed) before the Submit button is enabled.
- If GPS is denied: show a text fallback ("Location unavailable") and require the user to place a pin manually before submitting.

**Step 2 — Date & Time**

- Defaults to the current device time at screen open.
- User-editable via a standard date/time picker.
- Future timestamps: not a priority for MVP but the UI must not hard-block them (do not add a max-date validation unless there is a specific abuse reason to do so).

**Step 3 — Avatar / Identity Selector**

- Displayed as a horizontal scrollable row or a grid.
- Exactly **2 avatars are always free** and permanently accessible — no paywall, no ad requirement.
- All remaining avatars are locked. Tapping a locked avatar shows a bottom sheet with the unlock options for the current phase (CREATE-03.2):
  - **"Buy"** — permanent a la carte purchase through the store (MON-01, ADR-0016); the price is shown from the store offering.
  - **"Watch Ad to Try Once"** — triggers a rewarded ad (MON-03, ADR-0017; server-side verified); on completion, grants a single-use one-action unlock.
  - **"Go Premium"** — added when the subscription ships (MON-02, Phase 4).
- (Phase 4) If the user has an active premium subscription: rewarded-ad UI is hidden entirely for avatar items. No conflict surface.
- The selected avatar determines:
  - The marker icon rendered on the map for this fart.
  - The identity displayed in the feed for this post.

**Step 4 — Flavor Stepper**

- A 5-step labeled stepper (not a slider):
  `Fresh → Hmm → Stinky → Toxic → Lethal`
- Visual styling of the stepper must reflect comedic escalation (color gradient, icons, or both — implementation decision).
- Selection affects:
  - Marker visual styling on the map (color or icon overlay).
  - Reserved for profile stats weighting if the progression system uses flavor data later.
- Default: step 3 ("Stinky") — middle of the range, never the mildest or most extreme.

**Step 5 — Intensity Slider**

- Continuous range from **Whisper** (min) to **Thunderclap** (max).
- Stored as a normalized Float (0.0–1.0) or an Int — implementation decision.
- Leaderboard/ranking mapping is reserved for later; the field must be captured now.
- Intensity contributes 0% to XP and ranking in MVP (resolved, §19 item 2).
- Default: midpoint (0.5).

**Step 6 — Description Field**

- Free text, optional.
- Full emoji support — this is critical for humor virality (L8 Social Currency loop).
- Placeholder text rotates between humorous prompts:
  - "Was it loud?"
  - "Silent but deadly?"
  - "Any casualties?"
  - "What happened?"
- Soft limit of 280 characters with a visible counter; the backend rejects bodies above 500 characters.

**Step 7 — Custom Sound Picker**

- Displays a curated list of sound options.
- Locked items follow the same unlock rules as avatars (Step 3): purchase (permanent) or rewarded ad (single-use one action); Phase 4 adds the premium path.
- At least 3 sound options must always be free.
- Tapping a locked sound shows the same bottom sheet as locked avatars.
- Selected sound plays when a user taps this fart's marker on the map.
- A user-facing toggle ("Play sounds on tap") must be available in the sound picker or in app settings to disable all marker sounds globally.

**Step 8 — Voice Recording**

- **Premium-only. No rewarded-ad unlock path for this feature.**
- For premium users: a record button with a **10-second hard limit** (ADR-0014). MVP scope: record and attach only — no editing, no trimming, no effects.
- For non-premium users: show a locked UI — display an audio waveform placeholder and a "Premium — coming later" CTA that opens an informational sheet until MON-02 ships (Phase 4). Do not silently hide the field; show it as locked to surface the premium value.
- Voice clip is uploaded to remote storage; persisted as a URL (`voice_url`) on the Fart entity.

**Step 9 — Visibility Selector**

- Three options: **Public / Followers / Private** (ADR-0010).
- Must default to the user's **last-used setting** (persisted in DataStore or equivalent).
- For a first-time user with no stored preference: default is **Followers**.
- Selected value must round-trip faithfully to the entity field `visibility_mode` — this was a known PoC failure; it must be covered by a unit test at implementation.

**Step 10 — Submit**

- CTA label: **"Let It Rip"** (not "Post", not "Submit" — tone must match product humor).
- Triggers a loading state with two parallel progress indicators:
  - (a) Uploading media (sound clip / voice clip if present).
  - (b) Resolving / confirming location (final GPS coordinate committed here).
- On success:
  - Dismiss the Log Fart screen.
  - Animate a marker drop onto the map at the confirmed location.
  - Clear the local draft.
- On failure:
  - Show a retry CTA with an error reason (generic "Something went wrong" is acceptable for MVP).
  - Auto-save draft locally so no user input is lost.
  - Do not clear any fields.
- On offline (ADR-0007 local-first):
  - Accept the submission; store as a pending operation locally.
  - Show a non-blocking toast: "Saved locally — will post when you're back online."
  - Auto-sync on reconnect. User does not need to take any action.

---

#### CREATE-03.2 — Monetization Binding Rules

The following table defines which creation features are gated and by which unlock mechanism. This table is the single source of truth for Log Fart monetization. Any change here must also be reflected in MON-01, MON-02, and MON-03.

| Feature | Free tier | Purchase (a la carte, permanent) — Phase 1 | Rewarded Ad (single use) — Phase 1 | Premium tier — Phase 4 |
|---|---|---|---|---|
| Avatars | 2 permanently free | Any avatar, owned forever | 1 item unlocked for one action | Full library while subscribed |
| Sounds | ≥3 permanently free | Any sound, owned forever | 1 item unlocked for one action | Full library while subscribed |
| Voice Recording | Not available (locked UI shown) | Not purchasable a la carte | Not unlockable via ads | Unlocked, 10-second hard limit (Phase 4) |

**Mandatory UX rules for locked items (no silent defaults):**

1. Tapping a locked avatar or sound always shows a bottom sheet. In Phase 1 the sheet offers exactly two options: "Buy" (permanent) and "Watch Ad to Try Once". When MON-02 ships (Phase 4) a third option, "Go Premium", is added. No dismiss-only sheet.
2. Tapping the voice record button when locked always shows a bottom sheet. Until MON-02 ships it is informational ("Premium — coming later"); from Phase 4 it offers "Go Premium" only. No ad option is ever shown.
3. If the user has an active premium subscription, rewarded-ad UI is hidden entirely for all items that premium unlocks. There must be no surface where premium and rewarded-ad options are shown together.
4. After a rewarded-ad single-use unlock is consumed, the item must re-lock. It must never remain permanently unlocked as a result of watching an ad. This is a hard product rule (anti-pay-to-win / anti-subscription-undermining).
5. Rewarded-ad unlock behavior is fixed as **single-use one action**. No session-duration or time-duration unlock state is used.
6. Entitlements are verified server-side against the mirrored purchase state (ADR-0016); rewarded unlock tokens are issued and consumed server-side (ADR-0017). The client never grants itself an unlock.

---

#### CREATE-03.3 — Data Model Mapping

Every field submitted from the Log Fart screen must persist faithfully to the Fart entity. The following table is the authoritative mapping. Implementation must unit-test that each field round-trips without data loss. The visibility field failure from the PoC (always persisted as PRIVATE regardless of selection) is the canonical example of what this table is designed to prevent.

| Screen field | Entity field | Type | Required |
|---|---|---|---|
| Detected / adjusted location (lat) | `private_lat` (owner-only; derives `public_lat` and `coarse_location_key`, ADR-0019) | Double | Yes |
| Detected / adjusted location (lng) | `private_lng` (owner-only; derives `public_lng`, ADR-0019) | Double | Yes |
| Date & time | `timestamp` | Long (epoch ms) | Yes |
| Selected avatar | `avatar_id` | String | Yes |
| Flavor stepper value | `flavor_level` | Int (1–5) | Yes |
| Intensity slider value | `intensity_value` | Float (0.0–1.0 normalized) | Yes |
| Description text | `description_text` | String? | No |
| Selected sound | `sound_id` | String? | No |
| Recorded voice clip | `voice_url` | String? (remote URL after upload) | No |
| Visibility selection | `visibility_mode` | Enum: PUBLIC / FOLLOWERS / PRIVATE | Yes |

**Submission state machine**

The Log Fart screen must implement the following states. Each state must be a distinct UI state managed by the ViewModel — no implicit or inferred state transitions.

| State | Meaning | Required behavior |
|---|---|---|
| `DRAFT` | Screen open; fields partially or fully filled; not yet submitted | Persisted locally (DataStore or Room); survives app kill and process death |
| `UPLOADING` | Submission in-flight | Dual progress indicators shown (media + location); Submit button disabled; back navigation disabled or shows a confirmation dialog |
| `SUCCESS` | Fart posted; marker is live on map | Dismiss screen; animate marker drop at confirmed location; clear local draft |
| `FAILED` | Upload or location resolution failed | Show retry CTA with error reason; preserve draft; do not clear any field |
| `OFFLINE` | Device has no connectivity at submission time | Accept submission; persist as pending local operation; auto-sync on reconnect per ADR-0007; show non-blocking toast |

**Validation checklist (must be satisfied at implementation, not deferred):**

- [ ] L1 Action element completable in <60s on first session (CoreLoops.md health bar)
- [ ] Serves Journey Stage 3 (Activation) and Stage 4 (First Reward) per UserJourney.md
- [ ] PRIV-01: visibility selector present with correct defaults (last-used or Followers for new users)
- [ ] PRIV-02: precise lat/lng never displayed to user; only coarse map thumbnail shown
- [ ] MON-01 / MON-02 / MON-03: all locked items are cosmetic only; no creation capability is gated behind monetization; only cosmetic selection within creation is gated
- [ ] Unit test: visibility field round-trips faithfully (regression for PoC failure)
- [ ] Unit test: all required entity fields are non-null after a successful submission
- [ ] Unit test: DRAFT state survives simulated process death

---

### CREATE-04 Flavor System
**Purpose**
Categorize fart moments into playful comedic types.

**User value**
Users get expressive humor, recognizable categories, and collectible variety.

**Pillar impact**
- Location 1
- Social 3
- Humor 5
- Progress 2
- Identity 3

**Target personas**
- Instant Joker
- Identity Builder
- Social Reactor

**Primary loops / journey**
- L1 reward quality
- L8 shareability
- Journey Stages 3, 8

**Phase assignment**
- Phase 1

**Dependencies**
- CREATE-01
- PROFILE-03 signature/style for visual expression

**Monetization opportunities**
- premium flavor packs
- seasonal flavor bundles

**Future expansion opportunities**
- limited-time flavors
- collection sets
- flavor achievements

**TBD / notes**
- MVP can ship with a small curated flavor set inspired by PoC strings: `Fresh`, `Hmm`, `Stinky`, `Toxic`, `Lethal`.

### CREATE-05 Intensity System
**Purpose**
Add magnitude and comedic emphasis to a fart moment.

**User value**
Users can make posts feel more dramatic, personal, and expressive.

**Pillar impact**
- Location 1
- Social 2
- Humor 4
- Progress 3
- Identity 2

**Target personas**
- Instant Joker
- Rival
- Identity Builder

**Primary loops / journey**
- L1 quality
- potentially L6 if ranking uses it
- Journey Stages 3, 7

**Phase assignment**
- Phase 1 for creation
- Phase 3 for any competitive use

**Dependencies**
- CREATE-01
- PROG-05 fairness / anti-abuse

**Monetization opportunities**
- premium visual intensity effects later

**Future expansion opportunities**
- intensity-based achievements
- intensity-based flavor variants

**TBD / notes**
- Ranking influence resolved: 0% weight in MVP (`ProgressionSystems.md` §10).

### CREATE-06 Story / Description System
**Purpose**
Let users add a short caption or story to frame the moment.

**User value**
Users can make moments funnier, more personal, and more share-worthy.

**Pillar impact**
- Location 2
- Social 4
- Humor 5
- Progress 1
- Identity 4

**Target personas**
- Social Reactor
- Identity Builder
- Community Organizer

**Primary loops / journey**
- L2 reactions/comments
- L8 sharing
- Journey Stages 4, 8

**Phase assignment**
- Phase 1 optional

**Dependencies**
- CREATE-01
- MOD-01 moderation controls

**Monetization opportunities**
- text styles and frames later

**Future expansion opportunities**
- story prompts
- event templates
- recap generation

**TBD / notes**
- Keep short and meme-native in MVP; avoid long-form social-network drift.

### CREATE-07 Sound Selection System
**Purpose**
Attach a stylized or comedic sound identity to a fart post.

**User value**
Users can make posts more ridiculous, distinctive, and identity-rich without requiring live recording.

**Pillar impact**
- Location 1
- Social 3
- Humor 5
- Progress 1
- Identity 4

**Target personas**
- Instant Joker
- Identity Builder

**Primary loops / journey**
- L1 reward quality
- L8 sharing
- Journey Stages 3, 8

**Phase assignment**
- Phase 2

**Dependencies**
- CREATE-03 detailed log
- PROFILE-03 style system

**Monetization opportunities**
- sound packs
- seasonal sound libraries
- premium creator bundles

**Future expansion opportunities**
- unlockable sound tiers
- community-voted sound packs

**TBD / notes**
- Prefer curated selectable sounds before user-upload complexity.

### CREATE-08 Audio Recording System
**Purpose**
Allow users to attach a short audio clip when the moment benefits from raw authenticity.

**User value**
Adds a higher-ceiling expressive and viral layer for standout moments.

**Pillar impact**
- Location 1
- Social 4
- Humor 5
- Progress 1
- Identity 4

**Target personas**
- Identity Builder
- Social Reactor
- Community Organizer

**Primary loops / journey**
- L8 viral share
- L2 social reaction
- Journey Stages 4, 8

**Phase assignment**
- Phase 4 (premium-only; requires MON-02 and MOD-02 audio coverage)

**Dependencies**
- CREATE-03 detailed log
- MOD-02 moderation pipeline (audio coverage)
- PRIV-03 consent and sharing controls

**Monetization opportunities**
- creator packs
- enhanced audio cosmetic effects later

**Future expansion opportunities**
- voice reactions
- event soundboards
- remix formats

**TBD / notes**
- Max duration: **resolved — 10 seconds.** Keep this feature as simple as possible for MVP: record button, 10-second hard limit, attach to fart, play on marker tap. No editing, no trimming, no effects, no external share by default. Moderation is async post-write (same as comments). See ADR-0014.

### CREATE-09 Creation Preview, Edit, and Delete Controls
**Purpose**
Give users confidence and control over what they publish.

**User value**
Users can correct mistakes, preview share-worthiness, and feel safe experimenting.

**Pillar impact**
- Location 2
- Social 2
- Humor 3
- Progress 1
- Identity 3

**Target personas**
- Identity Builder
- Social Reactor
- Instant Joker

**Primary loops / journey**
- supports L1 quality
- supports Journey Stage 3 trust

**Phase assignment**
- Phase 1 (delete own fart, ADR-0020); Phase 2 (edit, preview)

**Dependencies**
- CREATE-01
- PRIV-01 visibility

**Monetization opportunities**
- none direct

**Future expansion opportunities**
- draft libraries
- reusable templates

**TBD / notes**
- Delete/edit rules must align with moderation and sync strategy.

---

## 8) Social Systems

### SOCIAL-01 Reactions
**Purpose**
Provide the fastest social response loop on content.

**User value**
Users get quick validation, humor payoff, and reciprocity with almost no effort.

**Pillar impact**
- Location 2
- Social 5
- Humor 4
- Progress 1
- Identity 2

**Target personas**
- Instant Joker
- Social Reactor

**Primary loops / journey**
- L2 core
- Journey Stages 4, 6

**Phase assignment**
- Phase 1

**Dependencies**
- CREATE-01
- MOD-01 moderation controls

**Monetization opportunities**
- premium reaction packs later

**Future expansion opportunities**
- seasonal reactions
- animated reactions

**TBD / notes**
- Must remain lightweight and fast.

### SOCIAL-02 Comments
**Purpose**
Turn one-off reactions into conversation and relationship depth.

**User value**
Users can talk around a moment, tell jokes, call out friends, and sustain the social loop.

**Pillar impact**
- Location 2
- Social 5
- Humor 4
- Progress 1
- Identity 3

**Target personas**
- Social Reactor
- Community Organizer

**Primary loops / journey**
- L2
- L4
- Journey Stages 4, 6

**Phase assignment**
- Phase 2

**Dependencies**
- SOCIAL-01 reactions
- MOD-01 moderation controls

**Monetization opportunities**
- cosmetic comment styles later only if non-intrusive

**Future expansion opportunities**
- threaded jokes
- highlighted creator replies

**TBD / notes**
- Do not drift into generic long-form social discussion.

### SOCIAL-03 Follow Requests, Followers, and Following
**Purpose**
Create the privacy-aware social graph that powers follower-scoped visibility and social retention.

**User value**
Users can decide who gets access to their follower-visible activity while still building a social circle.

**Pillar impact**
- Location 2
- Social 5
- Humor 2
- Progress 1
- Identity 4

**Target personas**
- Social Reactor
- Identity Builder
- Community Organizer

**Primary loops / journey**
- L4 Social Connection
- Journey Stage 6

**Phase assignment**
- Phase 2

**Dependencies**
- ADR-0009
- PRIV-01 visibility model
- NOTIF-01 notification types

**Monetization opportunities**
- none direct

**Future expansion opportunities**
- recommended follows
- creator/fan dynamics

**TBD / notes**
- Model is fixed: follow-only with approval.

### SOCIAL-04 Map-Anchored Feed System
**Purpose**
Give users an alternate social consumption surface without undermining the map-first identity.

**User value**
Users can catch up on follower and relevant activity when browsing socially is more efficient than raw map scanning.

**Pillar impact**
- Location 3
- Social 5
- Humor 3
- Progress 2
- Identity 3

**Target personas**
- Social Reactor
- Rival

**Primary loops / journey**
- L2
- L4
- Journey Stages 5, 6

**Phase assignment**
- Phase 2

**Dependencies**
- SOCIAL-03
- MAP-03 discovery filters
- CREATE-01 content engine

**Monetization opportunities**
- none in MVP
- sponsored event rows later only if humor/location-safe

**Future expansion opportunities**
- personalized rankings in feed
- event feeds
- challenge feeds

**TBD / notes**
- Feed complements map; it must never replace map as the product center.

### SOCIAL-05 Social Discovery and Reactivation
**Purpose**
Help users find people worth following and bring them back together over time.

**User value**
Users avoid dead-graph experiences and are given social reasons to return.

**Pillar impact**
- Location 2
- Social 5
- Humor 2
- Progress 1
- Identity 3

**Target personas**
- Social Reactor
- Community Organizer

**Primary loops / journey**
- L4
- Journey Stage 6

**Phase assignment**
- Phase 2

**Dependencies**
- SOCIAL-03
- NOTIF-01 notifications

**Monetization opportunities**
- none direct

**Future expansion opportunities**
- suggested creators
- local social clusters
- challenge invitations

**TBD / notes**
- Discovery must be privacy-aware and minor-safe.

---

## 9) Profile Systems

### PROFILE-01 Avatar System
**Purpose**
Give each user a recognizable face/character identity beyond a username.

**User value**
Users become recognizable, expressive, and more emotionally invested in their presence.

**Pillar impact**
- Location 1
- Social 3
- Humor 4
- Progress 2
- Identity 5

**Target personas**
- Identity Builder
- Social Reactor
- Instant Joker

**Primary loops / journey**
- supports L4, L5, L8
- Journey Stage 7

**Phase assignment**
- Phase 1 (free tier plus a la carte purchases, ADR-0020); Phase 3 deeper economy

**Dependencies**
- PROFILE-02 public profile
- MON-01 cosmetic economy

**Monetization opportunities**
- avatar cosmetics
- exclusive avatar items

**Future expansion opportunities**
- themed avatars
- animated avatars
- seasonal collectibles

**TBD / notes**
- MVP should prefer controlled avatar customization over freeform uploads.

### PROFILE-02 Public Profile System
**Purpose**
Make the profile a social object containing history, stats, identity, and reputation.

**User value**
Users can build a visible personal narrative instead of a disposable account page.

**Pillar impact**
- Location 2
- Social 4
- Humor 3
- Progress 4
- Identity 5

**Target personas**
- Identity Builder
- Rival
- Social Reactor

**Primary loops / journey**
- L5 identity accretion
- L6 status
- Journey Stage 7

**Phase assignment**
- Phase 1 light
- Phase 3 deep

**Dependencies**
- PROFILE-04 stats/history
- ACH-01 achievements
- PROG-03 leaderboards

**Monetization opportunities**
- premium profile themes
- profile cards

**Future expansion opportunities**
- web-view profile pages
- public highlights
- creator profile modes

**TBD / notes**
- Must preserve continuity and public meaning.

### PROFILE-03 Signature and Style System
**Purpose**
Give users a recurring, recognizable visual identity across markers, profile, and shares.

**User value**
Users feel that their content is unmistakably theirs.

**Pillar impact**
- Location 2
- Social 3
- Humor 4
- Progress 2
- Identity 5

**Target personas**
- Identity Builder
- Instant Joker

**Primary loops / journey**
- L5 identity accretion
- L8 sharing
- Journey Stage 7, 8

**Phase assignment**
- Phase 3

**Dependencies**
- PROFILE-02
- CREATE-04 flavor system
- MON-01 cosmetic economy

**Monetization opportunities**
- signature packs
- prestige styles
- limited-time effects

**Future expansion opportunities**
- composable identity parts
- creator bundles
- seasonal style lines

**TBD / notes**
- Signature composition: **resolved — single style selection.** The user selects one complete style from a catalog of pre-composed signatures. There is no mix-and-match of individual parts (icon + color + effect + frame) in MVP. Composable identity parts remain a future expansion opportunity only. See ADR-0013.

### PROFILE-04 Stats, History, and Reputation
**Purpose**
Translate repeated activity into visible proof of consistency, travel, and social standing.

**User value**
Users see their identity, effort, and history turning into something meaningful.

**Pillar impact**
- Location 3
- Social 3
- Humor 2
- Progress 5
- Identity 5

**Target personas**
- Rival
- Identity Builder
- Explorer

**Primary loops / journey**
- L5
- L6
- Journey Stage 7

**Phase assignment**
- Phase 3 (`ReleaseStrategy.md` §5); Phase 2 shows only the streak foundation from PROG-01

**Dependencies**
- PROG-01 streaks
- PROG-02 levels
- ACH-01 achievements

**Monetization opportunities**
- premium recap exports
- enhanced stat visualizations

**Future expansion opportunities**
- yearly recaps
- travel achievements
- social comparison cards

**TBD / notes**
- Reputation should be transparent enough to avoid black-box distrust.

### PROFILE-05 Collections System
**Purpose**
Let users collect meaningful sets across flavors, places, milestones, or seasons.

**User value**
Users get low-pressure progression and identity depth beyond pure rank competition.

**Pillar impact**
- Location 3
- Social 2
- Humor 3
- Progress 4
- Identity 4

**Target personas**
- Explorer
- Identity Builder
- Rival

**Primary loops / journey**
- L5 progression
- Journey Stage 7

**Phase assignment**
- Phase 3

**Dependencies**
- CREATE-04 flavor system
- MAP-06 map memory
- ACH-01 achievements

**Monetization opportunities**
- premium collection displays
- themed collection packs

**Future expansion opportunities**
- seasonal collections
- travel collections
- group collections

**TBD / notes**
- Collections should reward exploration and creativity, not spam volume.

### PROFILE-06 Profile Share Surface
**Purpose**
Turn a profile into an outward-facing identity artifact users can share.

**User value**
Users can show off milestones, style, stats, and status outside the app.

**Pillar impact**
- Location 2
- Social 4
- Humor 3
- Progress 4
- Identity 5

**Target personas**
- Identity Builder
- Rival

**Primary loops / journey**
- L8 Viral Share
- Journey Stage 8

**Phase assignment**
- Phase 3 (`ReleaseStrategy.md` §5)

**Dependencies**
- PROFILE-02
- SHARE-01 share cards

**Monetization opportunities**
- premium profile card themes

**Future expansion opportunities**
- web share pages
- seasonal profile frames

**TBD / notes**
- Must remain clear and funny even out of context.

---

## 10) Progression Systems

### PROG-01 Streak System
**Purpose**
Create a lightweight daily/periodic habit anchor.

**User value**
Users have a simple, understandable reason to come back tomorrow.

**Pillar impact**
- Location 2
- Social 2
- Humor 1
- Progress 5
- Identity 4

**Target personas**
- Rival
- Identity Builder
- Social Reactor

**Primary loops / journey**
- L5 Progression / Streak
- Journey Stage 5

**Phase assignment**
- Phase 2 foundation, Phase 3 full importance

**Dependencies**
- NOTIF-02 streak reminders
- PROFILE-04 stats/history

**Monetization opportunities**
- streak cosmetics
- optional vanity streak frames

**Future expansion opportunities**
- streak freezes if ever considered, but only if non-pay-to-win and trust-safe

**TBD / notes**
- Must stay ethical and non-punitive.

### PROG-02 Levels and XP System
**Purpose**
Convert ongoing activity into a visible long-term growth track.

**User value**
Users feel they are building a ridiculous legend over time.

**Pillar impact**
- Location 2
- Social 2
- Humor 2
- Progress 5
- Identity 4

**Target personas**
- Rival
- Identity Builder

**Primary loops / journey**
- L5
- L6
- Journey Stage 7

**Phase assignment**
- Phase 3

**Dependencies**
- PROG-05 anti-abuse
- ACH-01 achievements

**Monetization opportunities**
- cosmetic unlock tracks
- season pass tie-in later

**Future expansion opportunities**
- themed level tracks
- prestige tiers

**TBD / notes**
- XP sources must reward meaningful participation, not raw spam.

### PROG-03 Leaderboards
**Purpose**
Make progress socially legible and competitive.

**User value**
Users can compare themselves globally, locally, and against approved followers.

**Pillar impact**
- Location 3
- Social 4
- Humor 2
- Progress 5
- Identity 4

**Target personas**
- Rival
- Identity Builder

**Primary loops / journey**
- L6 Competition / Ranking
- Journey Stage 7

**Phase assignment**
- Phase 3

**Dependencies**
- PROG-05 anti-abuse
- NOTIF-03 rank movement notifications

**Monetization opportunities**
- prestige cosmetics only

**Future expansion opportunities**
- seasonal leaderboards
- challenge-specific boards
- group boards

**TBD / notes**
- Ranking model and Intensity weighting: resolved. See `ProgressionSystems.md` §4 and §10.

### PROG-04 Challenges
**Purpose**
Create discrete, time-boxed goals that generate social reactivation and competitive storytelling.

**User value**
Users get clear, exciting targets beyond open-ended accumulation.

**Pillar impact**
- Location 3
- Social 4
- Humor 3
- Progress 5
- Identity 3

**Target personas**
- Rival
- Community Organizer
- Social Reactor

**Primary loops / journey**
- L6
- L7
- Journey Stages 6, 7, 8

**Phase assignment**
- Phase 4 (with groundwork in Phase 3)

**Dependencies**
- PROG-03 leaderboards
- COMMUNITY-01 groups for group challenges

**Monetization opportunities**
- cosmetic challenge reward tracks
- optional premium challenge themes

**Future expansion opportunities**
- seasonal challenge ladders
- creator-hosted challenges

**TBD / notes**
- Solo/follower/group challenge modes are all valid; start simple.

### PROG-05 Fairness and Anti-Abuse Layer
**Purpose**
Protect every progression surface from spam, spoofing, and low-integrity ranking.

**User value**
Users can trust that status actually means something.

**Pillar impact**
- Location 2
- Social 3
- Humor 1
- Progress 5
- Identity 4

**Target personas**
- Rival
- Identity Builder

**Primary loops / journey**
- L5
- L6
- Journey Stage 7

**Phase assignment**
- Phase 3 mandatory

**Dependencies**
- MOD-03 anti-spam and integrity controls
- architecture sync and analytics strategy

**Monetization opportunities**
- none direct

**Future expansion opportunities**
- trust score signals for internal moderation only

**TBD / notes**
- Required before competitive systems can be considered complete.

### PROG-06 Territory (Deferred)
**Purpose**
Potential future location-control mechanic.

**User value**
- not part of MVP

**Pillar impact**
- Location 5
- Social 3
- Humor 2
- Progress 4
- Identity 3

**Target personas**
- Rival
- Explorer

**Primary loops / journey**
- potential L6/L7 extension

**Phase assignment**
- Post-MVP only

**Dependencies**
- dedicated ADR
- PROG-05

**Monetization opportunities**
- none until fairness is solved

**Future expansion opportunities**
- city rivalries
- team territory events

**TBD / notes**
- Explicitly out of MVP scope.

---

## 11) Achievement Systems

### ACH-01 Achievement and Badge System
**Purpose**
Reward milestones with explicit, identity-visible proof of accomplishment.

**User value**
Users feel progress, recognition, and variety in what the app celebrates.

**Pillar impact**
- Location 3
- Social 2
- Humor 3
- Progress 5
- Identity 4

**Target personas**
- Rival
- Identity Builder
- Explorer

**Primary loops / journey**
- L5
- Journey Stage 7

**Phase assignment**
- Phase 3

**Dependencies**
- PROFILE-04 stats/history
- PROFILE-05 collections

**Monetization opportunities**
- premium display frames only, not paid achievements

**Future expansion opportunities**
- seasonal achievements
- event badges
- city/travel achievements

**TBD / notes**
- Achievement categories should include humor, consistency, exploration, and social impact.

### ACH-02 Achievement Showcase
**Purpose**
Make achievements socially visible and profile-relevant instead of hidden system clutter.

**User value**
Users can curate what they are proud of and show it publicly.

**Pillar impact**
- Location 1
- Social 3
- Humor 2
- Progress 4
- Identity 5

**Target personas**
- Identity Builder
- Rival

**Primary loops / journey**
- L5 identity accretion
- L8 sharing

**Phase assignment**
- Phase 3

**Dependencies**
- ACH-01
- PROFILE-02 profile system

**Monetization opportunities**
- premium showcase slots/themes later

**Future expansion opportunities**
- shareable achievement cards
- group achievement walls

**TBD / notes**
- Showcase should support curation, not dump every badge equally.

---

## 12) Sharing Systems

### SHARE-01 Share Card System
**Purpose**
Generate screenshot-worthy, context-free artifacts that drive the viral loop.

**User value**
Users can share moments that are instantly understandable and funny outside the app.

**Pillar impact**
- Location 3
- Social 5
- Humor 5
- Progress 3
- Identity 4

**Target personas**
- Instant Joker
- Identity Builder
- Rival

**Primary loops / journey**
- L8 Viral Share
- Journey Stage 8

**Phase assignment**
- Phase 2

**Dependencies**
- CREATE-01
- PROFILE-02
- PROG-03 leaderboards

**Monetization opportunities**
- premium share themes
- creator-style templates

**Future expansion opportunities**
- animated cards
- event cards
- recap cards

**TBD / notes**
- Must never expose precise location.

### SHARE-02 Fart Share Surfaces
**Purpose**
Let users share individual fart posts as the smallest viral unit.

**User value**
Users can send the funniest single moment instantly.

**Pillar impact**
- Location 3
- Social 4
- Humor 5
- Progress 1
- Identity 3

**Target personas**
- Instant Joker
- Social Reactor

**Primary loops / journey**
- L8
- Journey Stage 8

**Phase assignment**
- Phase 2

**Dependencies**
- SHARE-01
- CREATE-03 detailed log quality

**Monetization opportunities**
- premium card frames

**Future expansion opportunities**
- audio-inclusive shares
- reaction summary shares

**TBD / notes**
- Best used when content is funny with no explanation.

### SHARE-03 Profile, Milestone, and Rank Shares
**Purpose**
Turn profile identity and progression into social bragging artifacts.

**User value**
Users can show growth, status, and personality outside the app.

**Pillar impact**
- Location 2
- Social 4
- Humor 3
- Progress 4
- Identity 5

**Target personas**
- Identity Builder
- Rival

**Primary loops / journey**
- L8
- Journey Stage 8

**Phase assignment**
- Phase 3

**Dependencies**
- PROFILE-02
- PROG-03
- ACH-02

**Monetization opportunities**
- premium visual themes

**Future expansion opportunities**
- end-of-season shares
- annual recap shares

**TBD / notes**
- Should feel brag-worthy, not corporate or sterile.

### SHARE-04 Challenge and Community Result Shares
**Purpose**
Let group and challenge outcomes generate new awareness and reactivation.

**User value**
Users can celebrate wins, losses, chaos, and group culture publicly.

**Pillar impact**
- Location 2
- Social 5
- Humor 4
- Progress 4
- Identity 4

**Target personas**
- Rival
- Community Organizer

**Primary loops / journey**
- L7
- L8
- Journey Stages 8

**Phase assignment**
- Phase 4

**Dependencies**
- PROG-04 challenges
- COMMUNITY-01 groups

**Monetization opportunities**
- seasonal group frames later

**Future expansion opportunities**
- league recap cards
- event posters

**TBD / notes**
- Keep readable outside in-app context.

---

## 13) Monetization Systems

### MON-01 Cosmetic Economy
**Purpose**
Monetize self-expression without harming fairness.

**User value**
Users can deepen their identity and humor style through purely cosmetic upgrades.

**Pillar impact**
- Location 1
- Social 3
- Humor 4
- Progress 2
- Identity 5

**Target personas**
- Identity Builder
- Instant Joker
- Social Reactor
- Explorer

**Primary loops / journey**
- supports L5 identity accretion
- supports L8 sharing
- Journey Stage 7

**Phase assignment**
- Phase 1: a la carte avatars and sounds (ADR-0016, ADR-0020); Phase 3: catalog expansion; Phase 4: full catalog

**Dependencies**
- PROFILE-01 avatar system
- PROFILE-03 signature/style
- CREATE-07 sound selection

**Monetization opportunities**
- avatar cosmetics
- signature packs
- marker themes
- sound packs
- profile frames

**Future expansion opportunities**
- limited drops
- collaborative collections
- themed seasons

**TBD / notes**
- Must remain cosmetic only.

### MON-02 Premium Personalization Subscription / Pass
**Purpose**
Offer a recurring premium layer for users who want deeper expression and status surfaces.

**User value**
Heavy users get more ways to personalize and showcase identity.

**Pillar impact**
- Location 1
- Social 2
- Humor 3
- Progress 3
- Identity 5

**Target personas**
- Identity Builder
- Rival
- Community Organizer

**Primary loops / journey**
- supports L5/L6/L8 indirectly

**Phase assignment**
- Phase 4 or later

**Dependencies**
- MON-01 cosmetic economy
- PROG-02 / ACH-02 mature identity surfaces

**Monetization opportunities**
- subscription
- season pass

**Future expansion opportunities**
- VIP cosmetics
- creator collections
- recap extras

**TBD / notes**
- Must never gate competitive power or essential social access.

### MON-03 Reward-Based Extras
**Purpose**
Give non-paying users optional access to lightweight extras without harming fairness.

**User value**
Users can sample fun extras without pressure.

**Pillar impact**
- Location 1
- Social 1
- Humor 3
- Progress 1
- Identity 3

**Target personas**
- Instant Joker
- Identity Builder

**Primary loops / journey**
- supports L8 share quality and L5 identity indirectly

**Phase assignment**
- Phase 1 for avatars and sounds (ADR-0017, ADR-0020)

**Dependencies**
- MON-01

**Monetization opportunities**
- rewarded unlocks

**Future expansion opportunities**
- promotional event unlocks

**TBD / notes**
- Must not become ad-heavy or anti-fun.

---

## 14) Notification Systems

### NOTIF-01 Notification Type Framework
**Purpose**
Define the set of meaningful notification triggers that bring users back for value, not noise.

**User value**
Users return because something socially or progress-wise meaningful happened.

**Pillar impact**
- Location 1
- Social 4
- Humor 2
- Progress 4
- Identity 2

**Target personas**
- Social Reactor
- Rival
- Identity Builder

**Primary loops / journey**
- L2
- L4
- L5
- L6
- Journey Stage 5

**Phase assignment**
- Phase 1 minimal: reaction-received push only (ADR-0020); Phase 2: full framework

**Dependencies**
- SOCIAL-01
- SOCIAL-03
- PROG-01
- PROG-03

**Monetization opportunities**
- none direct

**Future expansion opportunities**
- event alerts
- group reminder digests

**TBD / notes**
- Core types: reaction, comment, follow request, follow approval, follower post, streak risk, rank movement, challenge/event updates.

### NOTIF-02 Notification Preferences and Safety Controls
**Purpose**
Keep notifications useful, trusted, and user-controlled.

**User value**
Users avoid spam and can tune the product to their tolerance and interest.

**Pillar impact**
- Location 1
- Social 2
- Humor 1
- Progress 2
- Identity 1

**Target personas**
- all

**Primary loops / journey**
- protects L2/L4/L5 without breaking trust
- Journey Stage 5

**Phase assignment**
- Phase 2

**Dependencies**
- NOTIF-01
- PRIV-03 privacy controls

**Monetization opportunities**
- none

**Future expansion opportunities**
- digest modes
- smart notification bundling

**TBD / notes**
- Critical for trust counter-metrics.

### NOTIF-03 Rank, Streak, and Challenge Return Hooks
**Purpose**
Plant concrete reasons to return after a session ends.

**User value**
Users know what they would lose, miss, or gain by coming back.

**Pillar impact**
- Location 1
- Social 3
- Humor 1
- Progress 5
- Identity 3

**Target personas**
- Rival
- Social Reactor
- Identity Builder

**Primary loops / journey**
- L5
- L6
- Journey Stage 5

**Phase assignment**
- Phase 2 groundwork, Phase 3 stronger

**Dependencies**
- PROG-01 streaks
- PROG-03 leaderboards
- PROG-04 challenges

**Monetization opportunities**
- none direct

**Future expansion opportunities**
- seasonal urgency hooks

**TBD / notes**
- Must remain valuable, not manipulative.

---

## 15) Community Systems

### COMMUNITY-01 Groups
**Purpose**
Create recurring social spaces where users organize and sustain rituals.

**User value**
Users can play with "their people" instead of only broadcasting into the general world.

**Pillar impact**
- Location 3
- Social 5
- Humor 4
- Progress 3
- Identity 4

**Target personas**
- Community Organizer
- Social Reactor

**Primary loops / journey**
- L7 Community / Events
- Journey Stages 6, 8

**Phase assignment**
- Phase 4

**Dependencies**
- SOCIAL-03 follow graph maturity
- MOD-04 group moderation

**Monetization opportunities**
- group themes
- organizer tools later

**Future expansion opportunities**
- private groups
- city groups
- event groups

**TBD / notes**
- Must not be rushed before strong Phase 2 social density exists.

### COMMUNITY-02 Group Challenges and Shared Goals
**Purpose**
Turn groups into engines of recurring content and coordinated return.

**User value**
Users gain a reason to come back that depends on other people.

**Pillar impact**
- Location 3
- Social 5
- Humor 3
- Progress 4
- Identity 3

**Target personas**
- Community Organizer
- Rival

**Primary loops / journey**
- L7
- L6
- Journey Stage 6, 8

**Phase assignment**
- Phase 4

**Dependencies**
- COMMUNITY-01
- PROG-04 challenges

**Monetization opportunities**
- cosmetic event packs

**Future expansion opportunities**
- creator-hosted competitions
- seasonal league formats

**TBD / notes**
- Shared goals should create content, not busywork.
### COMMUNITY-03 Seasonal Events and Leagues
**Purpose**
Create periodic spikes of activity and resettable social momentum.

**User value**
Users get fresh reasons to re-engage and compare themselves repeatedly.

**Pillar impact**
- Location 2
- Social 4
- Humor 3
- Progress 5
- Identity 3

**Target personas**
- Rival
- Community Organizer

**Primary loops / journey**
- L6
- L7
- Journey Stage 8

**Phase assignment**
- Phase 4

**Dependencies**
- PROG-03 leaderboards
- COMMUNITY-01 groups

**Monetization opportunities**
- seasonal passes
- event cosmetics

**Future expansion opportunities**
- city tournaments
- themed seasons

**TBD / notes**
- Inspired partly by Poop Map's leagues, but should be more expressive and social.

---

## 16) Privacy Systems

### PRIV-01 Visibility Model
**Purpose**
Let users control who can see each fart post.

**User value**
Users feel safe and in control of their social exposure.

**Pillar impact**
- Location 2
- Social 3
- Humor 1
- Progress 1
- Identity 3

**Target personas**
- all

**Primary loops / journey**
- supports all content/society layers

**Phase assignment**
- Phase 1

**Dependencies**
- ADR-0010
- SOCIAL-03 follow requests

**Monetization opportunities**
- none

**Future expansion opportunities**
- advanced visibility presets later only if simplicity is preserved

**TBD / notes**
- MVP tiers are fixed: `Public / Followers / Private`.

### PRIV-02 Location Privacy and Precision Control
**Purpose**
Preserve the fun of place without exposing precise real-world risk.

**User value**
Users can participate in a location-based app without feeling tracked or exposed.

**Pillar impact**
- Location 5
- Social 2
- Humor 1
- Progress 1
- Identity 2

**Target personas**
- all

**Primary loops / journey**
- protects L1/L3 and trust across all stages

**Phase assignment**
- Phase 1

**Dependencies**
- `ContentSafety.md`
- architecture location rules

**Monetization opportunities**
- none

**Future expansion opportunities**
- user-controlled memory visibility
- private zones

**TBD / notes**
- Public outputs must use fuzzed/coarse location.

### PRIV-03 Consent, Account, and Data Controls
**Purpose**
Give users control over permissions, export, deletion, and notification/privacy settings.

**User value**
Users trust the app because they can see and control what it knows and shows.

**Pillar impact**
- Location 1
- Social 1
- Humor 1
- Progress 1
- Identity 2

**Target personas**
- all

**Primary loops / journey**
- supports safe movement through Stages 2-8

**Phase assignment**
- Phase 1 baseline, Phase 2 richer settings

**Dependencies**
- PRIV-01 visibility model
- PRIV-02 location privacy and precision control
- NOTIF-02

**Monetization opportunities**
- none

**Future expansion opportunities**
- privacy presets
- transparency summaries

**TBD / notes**
- Must align with Firebase/data architecture docs later.

---

## 17) Moderation Systems

### MOD-01 User Safety Controls: Block, Report, Mute, Comment Controls
**Purpose**
Provide immediate user-level tools to stop abuse and unwanted interaction.

**User value**
Users stay in control of their experience and can protect themselves without waiting on staff intervention.

**Pillar impact**
- Location 1
- Social 3
- Humor 1
- Progress 1
- Identity 2

**Target personas**
- all

**Primary loops / journey**
- protects L2, L4, L7

**Phase assignment**
- Phase 1 report baseline
- Phase 2 full controls

**Dependencies**
- SOCIAL-01
- SOCIAL-02
- COMMUNITY-01 for later group-specific controls

**Monetization opportunities**
- none

**Future expansion opportunities**
- creator moderation preferences
- group moderator roles

**TBD / notes**
- This is the minimum social safety floor, not an optional extra.

### MOD-02 Content Moderation Pipeline
**Purpose**
Define how unsafe content is flagged, reviewed, acted on, and appealed.

**User value**
Users trust that the platform will not reward harmful behavior.

**Pillar impact**
- Location 1
- Social 2
- Humor 1
- Progress 1
- Identity 1

**Target personas**
- all

**Primary loops / journey**
- trust-enabling across all loops

**Phase assignment**
- Phase 1 baseline
- Phase 2 stronger tooling

**Dependencies**
- `ContentSafety.md`
- architecture data/security strategy

**Monetization opportunities**
- none

**Future expansion opportunities**
- trust dashboards
- moderator analytics

**TBD / notes**
- Must cover captions, comments, profiles, audio, and future community content.

### MOD-03 Anti-Spam, Anti-Spoofing, and Integrity Layer
**Purpose**
Prevent fake location, spam logging, and manipulated progression.

**User value**
Users can trust the map, ranking, and community dynamics.

**Pillar impact**
- Location 3
- Social 2
- Humor 1
- Progress 5
- Identity 3

**Target personas**
- Rival
- Explorer
- Identity Builder

**Primary loops / journey**
- protects L1, L5, L6

**Phase assignment**
- Phase 1 basic anti-spam
- Phase 3 full competitive integrity

**Dependencies**
- PROG-05 fairness layer
- architecture sync/data strategy

**Monetization opportunities**
- none

**Future expansion opportunities**
- internal trust scoring

**TBD / notes**
- Must explicitly defend against spam-grinder behavior.

### MOD-04 Group and Community Moderation
**Purpose**
Protect group spaces and event loops from harassment, pile-ons, and low-quality coordination.

**User value**
Users can participate in community systems without chaos overwhelming the fun.

**Pillar impact**
- Location 1
- Social 4
- Humor 1
- Progress 2
- Identity 2

**Target personas**
- Community Organizer
- Social Reactor

**Primary loops / journey**
- protects L7

**Phase assignment**
- Phase 4

**Dependencies**
- COMMUNITY-01 groups
- MOD-01 base safety controls

**Monetization opportunities**
- none direct

**Future expansion opportunities**
- moderator roles
- event admin tools

**TBD / notes**
- Required before community systems can scale.

---

## 18) Feature Dependencies and Phase Logic

### Core dependency spine
- `MAP-01`, `MAP-02`, `MAP-03`, `CREATE-01`–`CREATE-06`, `CREATE-09` (delete), `SOCIAL-01`, `PROFILE-01`, `PROFILE-02` (light), `PRIV-01`, `PRIV-02`, `PRIV-03` (baseline), `MOD-01`, `MOD-02` (baseline), `MON-01` (a la carte), `MON-03`, `NOTIF-01` (minimal) are the **Phase 1 kernel** (ADR-0020).
- `SOCIAL-03`, `SOCIAL-04`, `NOTIF-01`, `NOTIF-02`, `SHARE-01`, `SHARE-02` are the **Phase 2 retention/virality layer**.
- `PROG-01`, `PROG-02`, `PROG-03`, `ACH-01`, `PROFILE-03`, `PROFILE-04`, `PROG-05` are the **Phase 3 depth layer**.
- `COMMUNITY-01`, `COMMUNITY-02`, `COMMUNITY-03`, `MOD-04` are the **Phase 4 network-effect layer**.

### Hard sequencing rules
1. Do not deepen progression before Activation and First Reward are healthy.
2. Do not ship competitive ranking without fairness/anti-abuse.
3. Do not build community loops before follower graph and notification loops are working.
4. Do not add monetization that weakens humor, fairness, or trust.
5. Do not let feed/social surfaces weaken map centrality.

---

## 19) Open Decisions / TBD Register

These do not block the feature specification itself, but they do affect future system detail documents.

### Product-level TBDs
1. **Ranking model** — ✅ Resolved. Signal weights: 50% XP / 20% Reactions / 15% Streak / 10% Challenges / 0% Intensity (mocked). See `ProgressionSystems.md` §4 and §10.

2. **Intensity weighting in progression** — ✅ Resolved. Intensity is mocked at 0% for MVP in both XP and ranking. Field is captured and stored. No weight until a future decision introduces it with abuse controls. See `ProgressionSystems.md` §10.

3. **Signature composition model** — ✅ Resolved. Single style selection. No composable parts in MVP. User picks one complete pre-composed signature from a catalog. Composable parts are a future expansion only. See ADR-0013.

4. **Audio recording constraints** — ✅ Resolved. Max duration = 10 seconds (hard limit). MVP scope: record, attach, play on marker tap. No editing, no trimming, no effects, no external share by default. Async post-write moderation. See ADR-0014.

5. **Following list visibility** — ✅ Resolved. Following list is private by default. Only follower count is publicly visible. User may opt in via profile privacy setting. See ADR-0012.

### Deferred items
6. **Territory** is explicitly post-MVP only.

### Phase decisions
7. **Phase 1 scope additions** (a la carte purchases, rewarded ads, reaction push, delete, all filters, four-tab navigation) — ✅ Resolved by owner decision 2026-10-14. See ADR-0020.

---

## 20) AI Decision Rules for Using This Spec

1. Treat `CREATE-01 Log Fart Engine` as the primary content creation system of the product, not a one-off form.
2. When decomposing any feature, preserve the map-first identity and the `Open -> Orient -> Consume -> Act -> Feedback -> Share -> Exit with return hook` session journey.
3. Every feature proposal derived from this doc must record:
   - Purpose
   - User value
   - Pillar impact
   - Target personas
   - Phase assignment
   - Dependencies
   - Monetization opportunities
   - Future expansion opportunities
4. Prefer features that strengthen a loop **and** an interlock.
5. Reject features that flatten humor into utility, turn the app into generic social networking, or remove place relevance.
6. If a feature touches UGC, visibility, social exposure, or competition, bind it to `ContentSafety.md` and moderation/privacy systems.
7. If a future roadmap item cannot be traced back to a system in this document, question whether it belongs in Fart Map.

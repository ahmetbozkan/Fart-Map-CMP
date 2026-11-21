# Content Creation Systems

> Implementation spec split from the original Features.md (strategy fields such as pillar impact, personas, loops, monetization ideas and future expansion live in `planning/archive/product/Features.md`). Phase placement authority: `planning/product/Scope.md`.

## CREATE-01 Log Fart Engine
**Purpose**
This is the primary content creation system of the entire product, not a simple form. It converts a passing real-world moment into a location-based social object.

**Phase**
- Phase 1

**Dependencies**
- MAP-01 map home
- MAP-02 marker system
- PRIV-01 visibility model
- MOD-01 / MOD-02 safety baseline (`ContentSafety.md` §10)

**Notes / open questions**
- This system must support both **quick log** and **expressive log** paths.

## CREATE-02 Quick Log Mode
**Purpose**
Provide the fastest possible path from urge to posted marker.

**Phase**
- Phase 1

**Dependencies**
- CREATE-01
- PRIV-01 visibility defaults

**Notes / open questions**
- Should default to the user's last-used safe settings where appropriate.

## CREATE-03 Detailed Log Fart Screen
**Purpose**
Offer a richer creation mode for users who want humor, identity, and story depth beyond a quick drop.

**Phase**
- Phase 1 base, Phase 2 richer version

**Dependencies**
- CREATE-04 Flavor
- CREATE-05 Intensity
- CREATE-06 Story/Description
- CREATE-07 sound selection
- CREATE-08 audio recording
- PRIV-01 visibility controls

**Notes / open questions**
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

### CREATE-03.1 — Log Fart Detailed Interaction Model

The Log Fart screen is a **10-step sequential creation flow**. Each step is defined below. Steps may render as a single scrollable screen or a guided stepper — that is a UI/UX decision for implementation. This spec defines the logical steps and constraints, not the visual layout.

**Design constraint:** The complete flow must be completable in under 60 seconds on first session (L1 Action health bar requirement from Product.md). Every step must have a sensible default so the user can tap through without making a decision.

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

### CREATE-03.2 — Monetization Binding Rules

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

### CREATE-03.3 — Data Model Mapping

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

- [ ] L1 Action element completable in <60s on first session (Product.md health bar)
- [ ] Serves Journey Stage 3 (Activation) and Stage 4 (First Reward) per Product.md
- [ ] PRIV-01: visibility selector present with correct defaults (last-used or Followers for new users)
- [ ] PRIV-02: precise lat/lng never displayed to user; only coarse map thumbnail shown
- [ ] MON-01 / MON-02 / MON-03: all locked items are cosmetic only; no creation capability is gated behind monetization; only cosmetic selection within creation is gated
- [ ] Unit test: visibility field round-trips faithfully (regression for PoC failure)
- [ ] Unit test: all required entity fields are non-null after a successful submission
- [ ] Unit test: DRAFT state survives simulated process death

---

## CREATE-04 Flavor System
**Purpose**
Categorize fart moments into playful comedic types.

**Phase**
- Phase 1

**Dependencies**
- CREATE-01
- PROFILE-03 signature/style for visual expression

**Notes / open questions**
- MVP can ship with a small curated flavor set inspired by PoC strings: `Fresh`, `Hmm`, `Stinky`, `Toxic`, `Lethal`.

## CREATE-05 Intensity System
**Purpose**
Add magnitude and comedic emphasis to a fart moment.

**Phase**
- Phase 1 for creation
- Phase 3 for any competitive use

**Dependencies**
- CREATE-01
- PROG-05 fairness / anti-abuse

**Notes / open questions**
- Ranking influence resolved: 0% weight in MVP (`ProgressionSystems.md` §10).

## CREATE-06 Story / Description System
**Purpose**
Let users add a short caption or story to frame the moment.

**Phase**
- Phase 1 optional

**Dependencies**
- CREATE-01
- MOD-01 moderation controls

**Notes / open questions**
- Keep short and meme-native in MVP; avoid long-form social-network drift.

## CREATE-07 Sound Selection System
**Purpose**
Attach a stylized or comedic sound identity to a fart post.

**Phase**
- Phase 1: picker with ≥3 free sounds plus purchasable / ad-unlockable sounds (CREATE-03.1 step 7, ADR-0020)
- Phase 2: full library

**Dependencies**
- CREATE-03 detailed log
- MON-01 / MON-03 for locked sounds (Phase 1)
- PROFILE-03 style system (Phase 3 enhancement only)

**Notes / open questions**
- Prefer curated selectable sounds before user-upload complexity.

## CREATE-08 Audio Recording System
**Purpose**
Allow users to attach a short audio clip when the moment benefits from raw authenticity.

**Phase**
- Phase 4 (premium-only; requires MON-02 and MOD-02 audio coverage)

**Dependencies**
- CREATE-03 detailed log
- MOD-02 moderation pipeline (audio coverage)
- PRIV-03 consent and sharing controls

**Notes / open questions**
- Max duration: **resolved — 10 seconds.** Keep this feature as simple as possible for MVP: record button, 10-second hard limit, attach to fart, play on marker tap. No editing, no trimming, no effects, no external share by default. Moderation is async post-write (same as comments). See ADR-0014.

## CREATE-09 Creation Preview, Edit, and Delete Controls
**Purpose**
Give users confidence and control over what they publish.

**Phase**
- Phase 1 (delete own fart, ADR-0020); Phase 2 (edit, preview)

**Dependencies**
- CREATE-01
- PRIV-01 visibility

**Notes / open questions**
- Delete/edit rules must align with moderation and sync strategy.

---

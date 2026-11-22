# Archived: DesignSpecification §11 Screen Specifications

> Moved out on 2026-11-21. Superseded by `planning/design/screens/*.md` and `planning/product/features/create.md` CREATE-03 (which win on conflicts: intensity 0.0–1.0, flavor stepper, description 280 soft / 500 hard, voice locked in Phase 1, last-used visibility, "Continue without account"). History only.

## 11) Screen Specifications

### 11.1 Auth / Login Screen

**Purpose:** Onboard new users and authenticate returning users.

**Layout structure:**
- Full-screen centering
- App logo/icon at top (64dp illustration)
- Welcome title (Headline Large)
- Tagline subtitle (Body Large, On Surface Variant)
- Sign-in button(s) centered below

**Content hierarchy:**
1. App icon/illustration (playful branded illustration)
2. "Fart Map" title (Display Small)
3. "Leave your mark on the world." tagline (Body Large)
4. Google Sign-In button (primary button style with Google logo)
5. Apple Sign-In button (secondary button style with Apple logo)
6. "Continue as guest" text button below

**Components used:**
- Custom illustration
- Primary button (Google)
- Secondary button (Apple)
- Text button (guest)

**User actions:**
- Tap Google Sign-In → Firebase Auth flow → navigate to Map
- Tap Apple Sign-In → Firebase Auth flow → navigate to Map
- Tap Continue as Guest → set anonymous session → navigate to Map

**Empty state:** N/A (this is the entry point)

**Loading state:** Button shows loading indicator during auth. App icon may pulse gently.

**Error state:** Error message below button: "Could not sign in. Please try again." with retry. Authentication-specific errors (account exists with different method) show specific instructions.

**Accessibility notes:**
- All buttons have content descriptions
- Sign-in buttons include provider name in description
- Focus order: app icon → buttons → guest option
- Dynamic text supported

---

### 11.2 Map Screen (Home)

**Purpose:** Primary screen. Explore content, navigate to other actions.

**Layout structure:**
```
┌──────────────────────────┐
│  [Top App Bar (overlay)] │  ← transparent, map visible behind
│  [Search] [Filter] [Notif]│
├──────────────────────────┤
│                          │
│                          │
│       MAP (full area)    │  ← markers rendered on map
│                          │
│                          │
├──────────────────────────┤
│              [     FAB ] │  ← Log Fart button
├──────────────────────────┤
│  [Map][Social][Rank][Profile] │  ← Bottom nav (ADR-0020)
└──────────────────────────┘
```

**Content hierarchy:**
1. **Map layer** (full background): Google Maps / MapKit with markers
2. **Top app bar layer** (transparent overlay): Search icon, Filter chip row, Notification bell
3. **Marker popup layer** (when marker tapped): Bottom sheet over map
4. **FAB layer** (bottom-right, over map): Primary action
5. **Bottom navigation layer** (persistent)

**Components used:**
- Native map view (platform-specific)
- Transparent top app bar with icons
- Filter chips (horizontal scrollable row)
- FAB (extended variant: "Log")
- Bottom navigation
- Bottom sheet (fart detail)
- Skeleton loading (initial marker load)

**User actions:**
- Pan/zoom map
- Tap marker → open fart detail bottom sheet
- Tap FAB → navigate to Log Fart screen
- Tap filter chips → filter map markers
- Tap search → open search (future)
- Bottom nav tab switching

**Empty state:** First-time user sees empty map with: "No farts near you yet... Be the first!" — with an animated encouragement indicator pointing at the FAB.

**Loading state:** Map renders immediately (base map tiles). Markers appear as they load. Skeleton pins during initial load.

**Error state:** "Could not load map data" snackbar with retry. If map SDK fails, show error state with "Could not load map. Please check your connection."

**Accessibility notes:**
- Map content descriptions for location context
- Markers are tappable and announced by screen readers
- Filter state is announced on change
- FAB labeled "Log a fart"

---

### 11.3 Log Fart Screen

**Purpose:** The core creation screen. Must be fast, fun, and frictionless.

**Layout structure:**
```
┌──────────────────────────┐
│ ← Cancel       Log Fart  │  ← Top app bar with draft status
├──────────────────────────┤
│                          │
│    ┌────────────────┐    │
│    │    MAP VIEW    │    │  ← Small map preview (200dp height)
│    │  (confirm loc) │    │
│    └────────────────┘    │
│                          │
│  [Avatar selector]       │  ← Horizontal scrollable avatar picker
│                          │
│  Flavor: [Fresh][Hmm][Stinky][Toxic][Lethal]  ← Chip row
│                          │
│  Intensity: ─────●────   │  ← Slider
│                          │
│  Sound: [None ▼]        │  ← Dropdown or chip selector  
│                          │
│  Description:            │
│  ┌──────────────────┐   │
│  │ [optional text]   │   │  ← Outlined text field
│  └──────────────────┘   │
│                          │
│  Visibility: [Public] [Followers] [Private]  ← Chip row
│                          │
├──────────────────────────┤
│        [  LET IT RIP  ] │  ← Primary button, full width
└──────────────────────────┘
```

**Content hierarchy:**
1. **Map preview** (top, 200dp) — confirms location, user can tap to adjust
2. **Avatar** — horizontal scrollable row, selectable, current selection highlighted
3. **Flavor** — horizontal chip row, single select, emoji + label
4. **Intensity** — continuous slider 0–10, with emoji labels at ends (🌬️ → 💥)
5. **Sound / Voice** — chip selector with optional voice recording
6. **Description** — optional multiline text field
7. **Visibility** — chip row, three options, default "Followers"
8. **Submit button** — full-width primary button

**Components used:**
- Small map preview (interactive, tappable)
- Horizontal scrollable avatar row (40dp circular avatars)
- Filter chips (flavor selection)
- Slider (intensity)
- Dropdown / chips (sound selection)
- Outlined text field (description, multiline, max 280 chars)
- Chip row (visibility)
- Primary button (submit, full-width)
- Voice recording button (microphone icon, 10s max)

**User actions:**
- Adjust location (tap map preview → full map for pin adjustment)
- Select avatar (a locked avatar opens the Cosmetic Unlock Sheet: Buy / Watch Ad to Try Once — CREATE-03.2, `screens/CosmeticUnlockSheet.md`)
- Select flavor (single select)
- Adjust intensity slider
- Select sound or record voice
- Enter description text
- Set visibility
- Tap "Let It Rip" to submit
- Tap cancel → confirm discard draft dialog

**Empty state:** Default selection state with Followers visibility, first avatar selected, intensity at 5, no description.

**Loading state:** Submit button transitions to loading state. Progress bar shows: media upload → location confirmation → posting.

**Error state:**
- Location error: "Could not detect location. Tap to set manually."
- Upload error: "Could not post. Retry or save as draft." — saves draft on decline.
- Validation error: inline below relevant field.

**State management (from features/ CREATE-03.3):**
- `DRAFT`: Screen open, fields filled -> persisted locally, survives kill
- `UPLOADING`: Submission in-flight -> dual progress (media + location)
- `SUCCESS`: Posted -> animate marker drop, clear draft, navigate to map
- `FAILED`: Error -> show retry CTA, preserve draft
- `OFFLINE`: No connectivity -> accept submission, notify "Posted offline, will sync"

**Accessibility notes:**
- Flavor chips announce flavor name + emoji
- Intensity slider announces value as percentage
- All selectors have clear labels
- Voice recording has start/stop distinct states
- Submit button labeled with action, not generic "Submit"

---

### 11.4 Fart Detail Bottom Sheet

**Purpose:** View fart details when tapping a map marker.

> **Phase 1 variant (ADR-0020, `screens/FartDetailSheet.md`):** user row, flavor/intensity, sound playback, description, coarse location, reaction bar (5 free reactions), Share (system share of the deep link; share cards are Phase 2), Report, Block, and Delete for own farts. The comments section and the Follow action are Phase 2 and are not rendered in Phase 1.

**Layout structure:**
```
┌──────────────────────────┐
│ ═══  (drag handle)       │  ← drag handle centered
├──────────────────────────┤
│                          │
│  ┌────────────────────┐  │
│  │  Avatar  UserName  │  │  ← User info row
│  │         2h ago     │  │
│  └────────────────────┘  │
│                          │
│  🌸 Fresh / 💥 7/10      │  ← Flavor + Intensity row
│                          │
│  [Sound play button]     │  ← If sound attached
│                          │
│  "Description text..."   │  ← Body Large
│                          │
│  📍 Approximate area     │  ← Coarse location
│                          │
│  ─────────────────────   │  ← Divider
│                          │
│  [😆] [😂] [💀] [🔥]    │  ← Reaction bar
│   42    13    8     3    │
│                          │
│  Comments (tap to expand)│  ← Expandable section
│                          │
│  ┌────────────────────┐  │
│  │  Add a comment...  │  │  ← Inline comment input
│  └────────────────────┘  │
│                          │
├──────────────────────────┤
│ [Share] [Report] [Follow]│  ← Action row
└──────────────────────────┘
```

**Content hierarchy:**
1. Drag handle
2. User row (avatar + username + relative timestamp)
3. Content metadata (flavor, intensity, sound)
4. Description text
5. Location (coarse area name, not coordinates)
6. Reaction bar (emoji reactions with counts)
7. Comments section (collapsible, with inline add)
8. Action row (Share, Report, Follow)

**Components used:**
- Bottom sheet
- Avatar (40dp)
- Body text
- Reaction chips (toggleable, with counts)
- Expandable section
- Inline text field (comment input)
- Icon buttons (share, report, follow)

**User actions:**
- Drag down to dismiss
- Tap reaction to react (or un-react)
- Expand comments section
- Add comment inline
- Tap Share → system share sheet
- Tap Follow/Unfollow → follow action
- Tap Report → report confirmation

**Empty state:** N/A (content is present by definition)

**Loading state:** Content fades in. Skeleton for comments if loading.

**Error state:** "Could not load details" snackbar. Report failure: snackbar confirmation or error.

**Accessibility notes:**
- Sheet announces when opened
- Reaction toggles announce selected/unselected state
- Comments section announces count on expand

---

### 11.5 Profile Screen

**Purpose:** User identity, stats, history, and settings access.

> **Phase 1 variant (ADR-0020):** avatar, username, fart count and reactions received; Edit Profile (name, avatar from the catalog — locked avatars open the Cosmetic Unlock Sheet); Share Profile (system share); Settings entry; for anonymous sessions a persistent "Keep your farts — link an account" card (ADR-0018). Stats row, XP, rank, achievements and the recent-activity list are Phase 3 and not rendered in Phase 1; the Rank tab carries the Phase 1 stats (`screens/RankTabScreen.md`).

**Layout structure:**
```
┌──────────────────────────┐
│ ← Back         ⚙️ Share │  ← Top app bar
├──────────────────────────┤
│                          │
│       [  Avatar 64dp  ]  │  ← Large avatar
│       UserName           │  ← Display Small
│       Bio / Tagline      │  ← Body Large
│                          │
│  ┌──┐ ┌──┐ ┌──┐ ┌──┐   │
│  │42│ │12│ │7 │ │1 │   │  ← Stat row (horizontal)
│  │dys│ │wk│ │mo│ │yr│   │
│  └──┘ └──┘ └──┘ └──┘   │
│                          │
│  [Edit Profile] [Share]  │  ← Action buttons row
│                          │
│  ──── Stats ────         │  ← Section header
│  Total farts: 142        │
│  Streak record: 42       │
│  XP: 3,450               │
│  Rank: #42               │
│                          │
│  ──── Achievements ────  │
│  [Badge][Badge][Badge]   │  ← Horizontal scrollable badges
│  [+3 locked]             │
│                          │
│  ──── Recent Activity ────│
│  [Fart item] [Fart item] │  ← Vertical fart history list
│  [Fart item]             │
│                          │
└──────────────────────────┘
```

**Content hierarchy:**
1. Top app bar (back if not own profile, share button)
2. Avatar (64dp, circular)
3. Username (Display Small)
4. Bio/tagline (Body Large)
5. Stats row (streak, weekly, monthly, yearly counts)
6. Action buttons (Edit Profile / Share Profile)
7. Stats section (total farts, record, XP, rank)
8. Achievements section (horizontal scrollable badge row)
9. Recent Activity section (scrollable fart history)

**Components used:**
- Top app bar
- Avatar (64dp)
- Stat cards (compact number + label)
- Buttons (primary + outline)
- Section headers
- Badge grid (horizontal scrollable chips or small cards)
- Fart history list items

**User actions:**
- Tap Edit Profile → edit screen (name, bio, avatar, signature)
- Tap Share Profile → system share sheet with profile card
- Tap achievement → detail tooltip
- Tap fart → fart detail
- Scroll to view all history
- Tap Follow (if viewing another user's profile)

**Empty state (own profile, first use):** "No farts yet. Go make history!" with prominent FAB reference. No achievements. "Complete your first streak to earn a badge."

**Loading state:** Skeleton cards for stats and history.

**Error state:** "Could not load profile" snackbar with retry.

**Accessibility notes:**
- Profile stats announced as grouped data
- Achievements announce name and unlock status
- Fart history items announce flavor, date, location area

---

### 11.6 Settings Screen

**Purpose:** App settings, privacy controls, account management.

> **Phase 1 variant:** Account (Link account for anonymous sessions, Sign out with the anonymous-loss warning, Delete account), Privacy (Default visibility), Content (Play marker sounds, Clear draft cache), About. "Show on leaderboard" (Phase 3) and "Following list" (Phase 2) are not rendered in Phase 1. There is no precise-location toggle: precise coordinates are never shown to others and the owner always sees their own exact pin (ADR-0019).

**Layout structure:**
```
┌──────────────────────────┐
│ ← Settings               │  ← Top app bar
├──────────────────────────┤
│                          │
│  ──── Account ────       │
│  [Profile info]     >    │  ← Action card
│  [Notifications]    >    │
│                          │
│  ──── Privacy ────       │
│  Default visibility [Followers ▼]│  ← Dropdown
│  Show on leaderboard [🔘]│
│  Following list    [🔘]  │  ← Following list visibility toggle (per ADR-0012)
│                          │
│  ──── Content ────       │
│  Clear draft cache       │  ← Action card
│  Download my data        │
│                          │
│  ──── About ────         │
│  Version 1.0.0           │  ← Body Small
│  Terms of Service        │  ← Text button
│  Privacy Policy          │
│                          │
│  ──────────────────────── │
│  [Sign Out]               │  ← Destructive button
│  [Delete Account]         │
│                          │
└──────────────────────────┘
```

**Components used:**
- Top app bar
- Section headers
- Action cards (with trailing chevron or switch)
- Switch toggles
- Icon buttons
- Destructive buttons (outlined error style)

**User actions:**
- Toggle switches for privacy settings
- Tap action cards → navigate to sub-settings
- Sign Out → confirmation dialog → navigate to Login
- Delete Account → confirmation dialog → API call → navigate to Login

**Empty state:** N/A

**Loading state:** Individual toggle updates with inline loading indicator.

**Error state:** "Could not save setting" snackbar. "Failed to sign out" snackbar.

**Accessibility notes:**
- Toggle switches have clear on/off labels
- Sign Out and Delete Account have confirmation dialogs
- All action items have proper content descriptions

---

### 11.7 Feed Screen (Phase 2)

**Purpose:** Timeline of followed users' fart activity.

**Layout structure:**
```
┌──────────────────────────┐
│ [For You] [Following]    │  ← Tab row
├──────────────────────────┤
│                          │
│  ┌── Fart Item ────────┐ │
│  │ Avatar  User  · 2h  │ │
│  │ Fresh / Intensity 7  │ │
│  │ "description..."     │ │
│  │ 😆 42  💬 12        │ │
│  └─────────────────────┘ │
│                          │
│  ┌── Fart Item ────────┐ │
│  │ ...                  │ │
│  └─────────────────────┘ │
│                          │
└──────────────────────────┘
```

**Components used:**
- Tab row (For You / Following)
- Fart feed cards
- Reaction chips
- Comment count

---

### 11.8 Notifications Screen (Phase 2)

**Purpose:** Alert user to social interactions and progression events.

**Layout structure:**
```
┌──────────────────────────┐
│ ← Notifications          │  ← Top app bar
├──────────────────────────┤
│                          │
│  Today                   │
│  ┌── Notification ─────┐ │
│  │ [Avatar] User liked  │ │
│  │         your fart    │ │
│  │         2m ago       │ │
│  └─────────────────────┘ │
│                          │
│  ┌── Notification ─────┐ │
│  │ [Avatar] User        │ │
│  │   followed you       │ │
│  │         15m ago      │ │
│  └─────────────────────┘ │
│                          │
│  This Week               │
│  ...                     │
└──────────────────────────┘
```

**Components used:**
- Section headers (date grouping)
- Notification list items
- Avatar (24dp)
- Body Medium text
- Timestamp (Body Small)

---

### 11.9 Social Tab (Phase 1)

**Purpose:** Map-anchored list of nearby public activity so the Social tab has value before the follow graph exists (ADR-0020). In Phase 2 it becomes the home of SOCIAL-04 feed tabs (Following / Nearby).

**Layout structure:** standard top app bar titled "Social"; list of fart cards (avatar, username, relative time, flavor + intensity, description, reaction counts, coarse area name) sorted by recency within the user's coarse area and neighbours; every card carries a **"View on Map"** CTA (mandatory, `Product.md`); tapping a card opens the Fart Detail Sheet over the Map tab.

**States:** loading skeleton; empty ("Nobody has let one rip nearby yet. Be the first.") with a Log CTA; error with retry; offline shows cached items with a sync-later note.

**Safety:** only PUBLIC, non-hidden farts; coarse location only; report/block reachable from the detail sheet.

### 11.10 Rank Tab (Phase 1)

**Purpose:** Personal stats surface and leaderboard teaser until PROG-03 ships (Phase 3), so the four-tab navigation is stable from launch (ADR-0020).

**Layout structure:** top app bar "Rank"; stat cards (farts logged, reactions received, days active); a humorous teaser card ("Leaderboards are brewing. Keep logging.") with no CTA to purchase anything; for anonymous sessions the account-link nudge card.

**States:** loading skeleton; zero-state copy for new users; error with retry.

**Rules:** no ranking, no comparison with other users, no purchase prompt (`Monetization.md` §6). Numeric values use JetBrains Mono.


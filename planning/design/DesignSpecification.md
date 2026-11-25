# Design Specification: Fart Map

> **Authority level:** Master design specification. All design tokens, components, and screen layouts derive from this document.
> Upstream: `product/Product.md`, `product/features/`.
> Downstream: all screen-level design files, all Compose Multiplatform UI code, all accessibility decisions.
> **When any downstream design artifact conflicts with this document, this document wins.**

---

## Contents

1. [Product Overview](#1-product-overview)
2. [Design Language](#2-design-language)
3. [Theme System](#3-theme-system)
4. [Typography System](#4-typography-system)
5. [Spacing System](#5-spacing-system)
6. [Shape System](#6-shape-system)
7. [Elevation System](#7-elevation-system)
8. [Iconography](#8-iconography)
9. [Component Library](#9-component-library)
10. [Navigation Architecture](#10-navigation-architecture)
11. [Screen Specifications](#11-screen-specifications)
12. [Motion & Animation](#12-motion--animation)
13. [Accessibility](#13-accessibility)
14. [Responsive Behavior](#14-responsive-behavior)
15. [Compose Implementation Notes](#15-compose-implementation-notes)
16. [Consistency Rules](#16-consistency-rules)

---

## 1) Product Overview

### App purpose

Fart Map is a location-based social entertainment application. Users log fart moments anchored to real-world locations, and those moments appear as playful markers on a shared map. The map becomes a living, funny social canvas where users explore, react, compete, and build identity.

### Target audience

- **Primary:** 18–34 year-olds who enjoy humor apps, meme culture, and light social competition
- **Secondary:** Friend groups looking for a shared inside-joke platform
- **Anti-personas:** Users seeking serious social networking, productivity tools, or professional networking

### Primary user goals

1. Log a funny moment quickly and see it appear on the map (<60s first-time flow)
2. Explore the map and discover funny content near them
3. React to and engage with followed users' and strangers' content
4. Build personal identity through activity, style, and progression
5. Share funny moments outside the app

### Key user flows

1. **Open → Map → Log → See:** User opens app, sees the map, taps to log, fills a fast form, submits, marker appears immediately
2. **Explore → Discover → React:** User pans the map, sees markers, taps one to see details, reacts or comments
3. **Profile → History → Identity:** User views their profile, sees accumulated stats, history, and reputation
4. **Social → Follow → Feed:** User discovers another user, follows them, sees their content in a feed

### Design principles

| Principle | Explanation |
|---|---|
| **Playful but polished** | The app is about farts, but the design is not childish. Humor lives in content, not in broken UI. Every interaction feels intentional. |
| **Map-first** | The map is the product center, not the feed. The map occupies maximum screen space. Navigation never obscures the map unnecessarily. |
| **Fast and frictionless** | The core action — logging a fart — must be completable in under 60 seconds on the first session. The design removes every non-essential tap. |
| **Warm and welcoming** | Colors, shapes, and micro-interactions feel friendly and approachable. The app invites play, not performance. |
| **Confident whitespace** | Content breathes. Information density is low. Every element earns its place on screen. |
| **Privacy-respecting** | Sensitive controls (visibility, location precision) are visible but not intrusive. The safe option is always the default. |

---

## 2) Design Language

### Design inspiration

The visual language draws from three sources:
- **Modern Material 3** — foundation for layout, motion, and component behavior
- **Playful cartography** — vintage map aesthetics (warm paper tones, contour lines, badge-like markers) inform color and shape choices
- **Internet-native humor culture** — sticker-like avatars, meme-friendly typographic moments, reaction GIF-like interaction patterns

### Visual personality

```
Playful ──────────────────────────────────── Professional
    ●                                      ○
  Fart Map                             Banking app

Warm ────────────────────────────────────── Cool
    ●                                      ○
  Fart Map                          Productivity app

Bold ────────────────────────────────────── Minimal
    ●                                      ○
  Fart Map                            Luxury retail

Social ──────────────────────────────────── Solo
    ●                                      ○
  Fart Map                            Note-taking app
```

### Mood board (textual)

- Warm amber sunset tones contrasted against cool teal nights
- Soft, pill-shaped everything — no sharp corners
- Markers that feel like collectible enamel pins or stickers
- Map as a living canvas, not a static utility
- Light and dark themes that feel like day (warm paper) and night (deep indigo)

### Brand feeling

**"A warm, funny place where you leave your mark on the world."** The app feels like: a coffee shop bulletin board, a shared inside joke, a playful competition with friends, and a personal scrapbook of ridiculous moments — all at once. It does not feel: cold, corporate, childish, or utilitarian.

### Interaction philosophy

- **Direct manipulation:** Tapping on the map positions a marker. Sliding adjusts intensity. Gestures replace buttons where natural.
- **Immediate feedback:** Every action produces a visible response within 100ms. The app never leaves the user wondering if something happened.
- **Forgiving:** Destructive actions (delete, discard) require confirmation. Navigation back is always available. Drafts are preserved.
- **Delightful micro-interactions:** Markers animate onto the map. Reactions produce subtle particle effects. Streak milestones get a celebratory moment. These are small and infrequent — never exhausting.

---

## 3) Theme System

### Light Theme

| Token | Color | Hex | Usage |
|---|---|---|---|
| **Primary** | Warm Amber | `#FF8C00` | Primary actions, active states, key accents |
| **On Primary** | White | `#FFFFFF` | Text/icons on primary backgrounds |
| **Primary Container** | Light Amber | `#FFE0B2` | Filled variant backgrounds for primary |
| **On Primary Container** | Dark Amber | `#4E2600` | Text on primary container |
| **Secondary** | Deep Teal | `#00796B` | Secondary actions, links, secondary buttons |
| **On Secondary** | White | `#FFFFFF` | Text/icons on secondary backgrounds |
| **Secondary Container** | Light Teal | `#B2DFDB` | Filled variant backgrounds for secondary |
| **On Secondary Container** | Dark Teal | `#00332D` | Text on secondary container |
| **Tertiary** | Soft Clay | `#D97777` | Contrast accent, special highlights |
| **On Tertiary** | White | `#FFFFFF` | Text/icons on tertiary backgrounds |
| **Background** | Warm Off-White | `#FFF8F0` | App background — warm paper tone |
| **On Background** | Near Black | `#1A1A1A` | Primary body text |
| **Surface** | White | `#FFFFFF` | Card and sheet backgrounds |
| **On Surface** | Near Black | `#1A1A1A` | Text on surfaces |
| **Surface Variant** | Warm Grey | `#F0EBE3` | Subtle surface differentiation |
| **On Surface Variant** | Medium Grey | `#6B6258` | Secondary text, captions |
| **Outline** | Warm Border | `#D4CDBF` | Borders and dividers |
| **Outline Variant** | Lighter Border | `#E8E2D6` | Subtle borders |
| **Error** | Red | `#D32F2F` | Error states, destructive actions |
| **On Error** | White | `#FFFFFF` | Text on error backgrounds |
| **Error Container** | Light Red | `#FFCDD2` | Error background surfaces |
| **Inverse Surface** | Dark Grey | `#2C2C2C` | Snackbar, tooltip backgrounds |
| **Inverse On Surface** | White | `#FFFFFF` | Text on inverse surfaces |

### Dark Theme

| Token | Color | Hex | Usage |
|---|---|---|---|
| **Primary** | Warm Amber | `#FFB74D` | Primary actions, active states (brighter for dark bg) |
| **On Primary** | Near Black | `#1A1A1A` | Text/icons on primary backgrounds |
| **Primary Container** | Dark Amber | `#5C3400` | Filled variant backgrounds |
| **On Primary Container** | Light Amber | `#FFE0B2` | Text on primary container |
| **Secondary** | Teal Accent | `#4DB6AC` | Secondary actions, links |
| **On Secondary** | Near Black | `#1A1A1A` | Text on secondary backgrounds |
| **Secondary Container** | Dark Teal | `#004D40` | Filled variant backgrounds |
| **On Secondary Container** | Light Teal | `#B2DFDB` | Text on secondary container |
| **Tertiary** | Soft Clay | `#EF9A9A` | Contrast accent |
| **On Tertiary** | Near Black | `#1A1A1A` | Text on tertiary backgrounds |
| **Background** | Deep Indigo | `#121218` | App background — night sky tone |
| **On Background** | Off-White | `#F0EBE3` | Primary body text |
| **Surface** | Dark Grey | `#1E1E26` | Card and sheet backgrounds |
| **On Surface** | Off-White | `#F0EBE3` | Text on surfaces |
| **Surface Variant** | Darker Grey | `#2A2A33` | Subtle surface differentiation |
| **On Surface Variant** | Medium Grey | `#999489` | Secondary text, captions |
| **Outline** | Border Grey | `#44424A` | Borders and dividers |
| **Outline Variant** | Darker Border | `#33323A` | Subtle borders |
| **Error** | Light Red | `#EF5350` | Error states |
| **On Error** | Near Black | `#1A1A1A` | Text on error backgrounds |
| **Error Container** | Dark Red | `#5C1010` | Error background surfaces |

### Semantic Colors

| Token | Light Theme | Dark Theme | Usage |
|---|---|---|---|
| **Success** | `#2E7D32` | `#66BB6A` | Successful operations, positive progression |
| **On Success** | `#FFFFFF` | `#1A1A1A` | Text on success |
| **Warning** | `#F57C00` | `#FFA726` | Warning states, near-limits |
| **On Warning** | `#FFFFFF` | `#1A1A1A` | Text on warning |
| **Error** | `#D32F2F` | `#EF5350` | Error states |
| **On Error** | `#FFFFFF` | `#1A1A1A` | Text on error |
| **Info** | `#1565C0` | `#42A5F5` | Informational states |
| **On Info** | `#FFFFFF` | `#1A1A1A` | Text on info |
| **Neutral** | `#9E9E9E` | `#757575` | Placeholder, disabled, inactive |
| **On Neutral** | `#FFFFFF` | `#FFFFFF` | Text on neutral |

---

## 4) Typography System

Font family: **Inter** (primary), **JetBrains Mono** (mono/numeric data).

Inter is chosen for its tall x-height, excellent readability at all sizes, wide language support, and clean contemporary feel. It balances warmth (soft curves on `a`, `g`, `e`) with clarity.

| Token | Font | Weight | Size | Line Height | Letter Spacing | Usage |
|---|---|---|---|---|---|---|
| **Display Large** | Inter | Bold (700) | 57sp | 64sp | -0.25sp | Hero moments, empty states, milestone celebrations |
| **Display Medium** | Inter | Bold (700) | 45sp | 52sp | 0sp | Large headers, profile name |
| **Display Small** | Inter | SemiBold (600) | 36sp | 44sp | 0sp | Screen titles, section headers |
| **Headline Large** | Inter | SemiBold (600) | 32sp | 40sp | 0sp | Feature headers, dialog titles |
| **Headline Medium** | Inter | SemiBold (600) | 28sp | 36sp | 0sp | Card titles, screen section headers |
| **Headline Small** | Inter | Medium (500) | 24sp | 32sp | 0sp | Subsection headers, list item titles |
| **Title Large** | Inter | Medium (500) | 22sp | 28sp | 0sp | Bottom sheet headers, expanded state titles |
| **Title Medium** | Inter | Medium (500) | 16sp | 24sp | 0.15sp | Standard card titles, list item titles |
| **Title Small** | Inter | Medium (500) | 14sp | 20sp | 0.1sp | Chip labels, small card titles |
| **Body Large** | Inter | Regular (400) | 16sp | 24sp | 0.5sp | Primary reading text, descriptions |
| **Body Medium** | Inter | Regular (400) | 14sp | 20sp | 0.25sp | Secondary text, comments, feed content |
| **Body Small** | Inter | Regular (400) | 12sp | 16sp | 0.4sp | Captions, timestamps, helper text |
| **Label Large** | Inter | Medium (500) | 14sp | 20sp | 0.1sp | Button text, tab labels, bottom nav labels |
| **Label Medium** | Inter | Medium (500) | 12sp | 16sp | 0.5sp | Small button text, chip text |
| **Label Small** | Inter | Medium (500) | 11sp | 16sp | 0.5sp | Badge text, small metadata |

### Numeric data (streaks, XP, stats)

When displaying numeric progression data (streak counts, XP totals, leaderboard ranks), use **JetBrains Mono** to ensure consistent digit widths. This prevents jarring layout shifts as numbers change and improves data readability.

| Token | Font | Weight | Size | Usage |
|---|---|---|---|---|
| **Numeric Display** | JetBrains Mono | Bold | Match surrounding text | Streak counts, XP totals |
| **Numeric Body** | JetBrains Mono | Regular | Match surrounding body | Stats, timestamps, coordinates |

---

## 5) Spacing System

| Token | Value | Usage |
|---|---|---|
| **Space 0** | 0dp | No spacing |
| **Space 2** | 2dp | Micro spacing inside components (icon-to-text padding) |
| **Space 4** | 4dp | Tight spacing, inline element gaps |
| **Space 8** | 8dp | Standard small spacing between related elements |
| **Space 12** | 12dp | Default padding for cards, content-to-border |
| **Space 16** | 16dp | Standard screen-edge padding, between sections |
| **Space 20** | 20dp | Generous spacing between distinct content groups |
| **Space 24** | 24dp | Large spacing, section separators |
| **Space 32** | 32dp | Extra-large spacing, between major screen sections |
| **Space 48** | 48dp | Top-level padding, dialog margins |
| **Space 64** | 64dp | Maximum spacing between hero content |

### Spacing conventions

- Screen edge padding: always **16dp** horizontal
- Between cards in a list: **12dp**
- Between content within a card: **16dp**
- Card inner padding: **16dp**
- Bottom sheet inner padding: **24dp**
- Bottom navigation bar height: **80dp** (including safe area)
- Top app bar height: **56dp** (compact) / **64dp** (with search)
- Minimum touch target: **48dp**

---

## 6) Shape System

| Token | Value | Usage |
|---|---|---|
| **Shape Extra Small** | 4dp | Checkboxes, switches, small chips |
| **Shape Small** | 8dp | Text field borders, standard buttons, cards in lists |
| **Shape Medium** | 12dp | Elevated cards, dialogs, bottom sheets |
| **Shape Large** | 16dp | Top-level containers, full-screen sheets |
| **Shape Extra Large** | 24dp | FAB, modals |
| **Shape Full** | 50% of height | Pill-shaped badges, avatar containers |

### Shape rules

- All corners use the same radius value (symmetric) — no mixed corner radii on a single component
- Cards always have rounded corners — never sharp
- Bottom sheets have rounded top corners only (16dp), flat bottom (fills screen)
- Dialogs have rounded corners on all four corners (16dp)
- Buttons are pill-shaped (Shape Full) for primary, small radius (8dp) for secondary

---

## 7) Elevation System

| Token | Value | Usage |
|---|---|---|
| **Elevation 0** | 0dp | Flat surfaces, cards without elevation |
| **Elevation 1** | 1dp | Subtle surface separation, pressed states |
| **Elevation 2** | 2dp | Standard cards, dropdown menus, chips |
| **Elevation 3** | 4dp | Elevated cards, bottom sheets (resting) |
| **Elevation 4** | 8dp | FAB (resting), dialogs, bottom sheets (expanded) |
| **Elevation 5** | 12dp | Top app bar, modal bottom sheets |

### Shadow color

Use a warm-toned shadow effectively tinting the shadow. In light theme: `#1A0A00` at varying opacity. In dark theme: `#000000` at higher opacity to create depth against dark backgrounds:

| Level | Light Opacity | Dark Opacity |
|---|---|---|
| 1dp | 8% | 12% |
| 2dp | 12% | 16% |
| 4dp | 16% | 20% |
| 8dp | 24% | 28% |
| 12dp | 32% | 36% |

---

## 8) Iconography

### Icon style

- **Material Symbols (Outlined)** — primary icon set. Outlined style is used for all navigation icons, action icons, and information icons.
- **Filled variant** — used only for active states (filled bookmark, filled bottom navigation tab when selected)
- **Custom emoji-styled icons** — for humor-specific content: flavors, reactions, avatar expressions. These are not part of Material Symbols but follow the same weight and sizing conventions.

### Stroke weight

- Material Symbols: **300 weight** (thin but legible)
- Custom icons: **2dp stroke**, matching the 300-weight feel

### Icon sizes

| Token | Value | Usage |
|---|---|---|
| **Icon Tiny** | 12dp | Badge indicators, inline metadata |
| **Icon Small** | 16dp | Inline with body text, list item secondary actions |
| **Icon Medium** | 20dp | Action icons in top app bar, chip icons |
| **Icon Standard** | 24dp | Bottom navigation, FAB icons, primary actions |
| **Icon Large** | 32dp | Section header icons, empty state hero icons |
| **Icon Extra Large** | 48dp | Empty state primary illustration |

### Icon usage rules

- Icons are always left-aligned to labels in buttons and list items
- Icons in navigation are centered (no label for bottom nav when using icons only)
- Decorative icons (empty states) may use custom illustration-style icons
- Icons never substitute for text labels in critical action areas

---

## 9) Component Library

### 9.1 Buttons

#### Primary Button

| Property | Specification |
|---|---|
| **Purpose** | Primary call-to-action. One per screen maximum. |
| **Shape** | Pill (Shape Full), 56dp height |
| **Colors** | Primary background, On Primary text |
| **Typography** | Label Large, all caps optional |
| **Padding** | Horizontal 24dp minimum, vertical 16dp |
| **Icon slot** | Leading icon optional, 20dp |

**States:**

| State | Visual |
|---|---|
| Enabled | Primary fill, On Primary text |
| Hovered/pressed | Primary + 12% overlay (darker) |
| Disabled | Surface Variant fill, On Surface Variant text at 38% opacity |
| Loading | Primary fill with centered CircularProgressIndicator (onPrimary color) |

**Accessibility:** Minimum touch target 48dp. Content description required when no visible label.

#### Secondary Button

| Property | Specification |
|---|---|
| **Purpose** | Secondary action, non-primary CTA |
| **Shape** | Small radius (8dp), 40dp height |
| **Colors** | Surface (outlined variant) or Surface Variant (filled variant) |
| **Typography** | Label Medium |
| **Border** | 1.5dp Outline stroke (outlined variant) |

#### Text Button

| Property | Specification |
|---|---|
| **Purpose** | Tertiary action, less emphasis |
| **Shape** | None (transparent background) |
| **Colors** | Primary text, no background |
| **Typography** | Label Large |
| **Min width** | 48dp |

#### Icon Button

| Property | Specification |
|---|---|
| **Purpose** | Toolbar actions, secondary controls |
| **Size** | 48dp touch target, 24dp icon |
| **Colors** | On Surface text, no background |
| **States** | Same as text button |

#### Floating Action Button

| Property | Specification |
|---|---|
| **Purpose** | Primary action on map screen (Log Fart) |
| **Shape** | Extra Large (24dp) or pill-shaped extended variant |
| **Size** | Standard: 56dp. Extended: 56dp height × auto width |
| **Elevation** | 4 (resting), 8 (pressed) |
| **Colors** | Primary container, On Primary Container icon |
| **Position** | Bottom-right, 16dp from edges, 80dp above bottom nav if present |

---

### 9.2 Text Inputs

#### Filled Text Field (preferred)

| Property | Specification |
|---|---|
| **Shape** | Small (8dp) top corners, flat bottom |
| **Colors** | Surface Variant fill, On Surface Variant label/input |
| **Active indicator** | Primary 2dp bottom line |
| **Error indicator** | Error 2dp bottom line + Error label below |
| **Height** | 56dp standard, 48dp compact |
| **Typography** | Body Large input, Body Small label/helper |

#### Outlined Text Field

| Property | Specification |
|---|---|
| **Shape** | Small (8dp) all corners |
| **Colors** | Surface fill, Outline border |
| **Active border** | Primary 2dp |
| **Usage** | Longer form inputs where visual separation matters (Log Fart description) |

#### Search Field

| Property | Specification |
|---|---|
| **Shape** | Full pill (Shape Full) |
| **Icon** | Leading search icon (20dp), trailing clear icon when text present |
| **Colors** | Surface Variant fill |
| **Behavior** | Debounced 300ms on text change |

#### Password Field

| Property | Specification |
|---|---|
| **Variant** | Filled Text Field |
| **Trailing icon** | Visibility toggle (eye icon) |
| **Autofill** | Configure autofill hints |

---

### 9.3 Cards

#### Standard Card

| Property | Specification |
|---|---|
| **Shape** | Medium (12dp) |
| **Elevation** | 1 |
| **Inner padding** | 16dp |
| **Colors** | Surface background |
| **Usage** | Fart detail cards, profile info sections |

#### Elevated Card

| Property | Specification |
|---|---|
| **Shape** | Medium (12dp) |
| **Elevation** | 3 |
| **Usage** | Fart marker cards (when opened from map), featured content |

#### Action Card

| Property | Specification |
|---|---|
| **Shape** | Small (8dp) |
| **Elevation** | 1 |
| **Usage** | Tappable list items with chevron, settings items |
| **Trailing element** | Chevron icon, switch, or badge |

---

### 9.4 Navigation

#### Top App Bar

| Property | Specification |
|---|---|
| **Height** | 56dp (compact), 64dp (with search) |
| **Elevation** | 0 (flat on map), 5 (on non-map screens) |
| **Colors** | Surface with scrim below when elevated |
| **Content** | Title (Headline Small) centered or start-aligned, action icons end-aligned |

**Variants:**
- **Map Top App Bar:** Transparent background, no elevation, overlaid on map with scrim for legibility
- **Standard Top App Bar:** Surface background, 5dp elevation, standard content layout
- **Large Top App Bar:** 64dp height, Display Small title, used for profile screen only

#### Bottom Navigation

| Property | Specification |
|---|---|
| **Height** | 64dp + safe area (total ~80dp) |
| **Elevation** | 5 |
| **Items** | 4–5 items maximum |
| **Active indicator** | Primary icon + label (Label Medium, Medium weight) |
| **Inactive** | On Surface Variant icon only (38% opacity), no label |
| **Badge** | Small red dot (8dp) for unread notifications |

**Tab order (ADR-0020):** Map | Social | Rank | Profile. Log Fart is the map FAB, never a tab. The Phase 2 feed lives inside the Social tab; notifications are reached from the top-bar bell, not a tab.

#### Navigation Rail (tablet)

| Property | Specification |
|---|---|
| **Width** | 72dp |
| **Position** | Left edge, below top app bar |
| **Items** | Icons only, 48dp touch targets |
| **Active indicator** | Primary pill background on icon |
| **Usage** | Replaces bottom navigation on >=600dp width screens |

---

### 9.5 Feedback Components

#### Snackbar

| Property | Specification |
|---|---|
| **Shape** | Small (8dp) |
| **Elevation** | 5 |
| **Colors** | Inverse Surface background, Inverse On Surface text |
| **Position** | Bottom, 16dp from edges, above bottom nav |
| **Duration** | 4 seconds (informational), indefinite (with action) |
| **Action** | Text button (Label Medium, Primary or Secondary color) |
| **Usage** | Non-blocking operation feedback, sync status |

#### Toast Equivalent (Anchored Snackbar)

No Android Toast. All transient messages are Compose Snackbars anchored above the bottom nav.

#### Dialog

| Property | Specification |
|---|---|
| **Shape** | Large (16dp) |
| **Elevation** | 8 |
| **Max width** | 320dp on phone, 400dp on tablet |
| **Inner padding** | 24dp |
| **Title** | Headline Medium |
| **Body** | Body Medium |
| **Actions** | Text buttons end-aligned (minimum 2, maximum 3) |
| **Scrim** | Background at 60% opacity |
| **Dismiss** | Tap scrim to dismiss (unless confirmation required) |

#### Bottom Sheet

| Property | Specification |
|---|---|
| **Shape** | Large (16dp) top, flat bottom |
| **Elevation** | 4 (resting), 8 (expanded) |
| **Drag handle** | 32dp × 4dp rounded rectangle, Surface Variant, centered top |
| **Peek height** | Defined per use case |
| **Content padding** | 24dp |
| **Scrim** | Background at 40% opacity |
| **Usage** | Fart detail, flavor picker, filter selection |

#### Loading State (Skeleton)

| Property | Specification |
|---|---|
| **Shape** | Matches the content component shape |
| **Animation** | Shimmer — gradient sweep left to right (1.5s cycle) |
| **Colors** | Surface Variant base with Surface shimmer highlight |
| **Opacity** | 60% of normal content |
| **Usage** | All async content areas while data loads |

---

### 9.6 Lists

#### List Item

| Property | Specification |
|---|---|
| **Height** | 56dp minimum (one-line), 72dp (two-line) |
| **Inner padding** | 16dp start, 12dp end |
| **Leading element** | Icon (24dp) or avatar (40dp) |
| **Trailing element** | Chevron, switch, badge, or timestamp |
| **Divider** | Full-width Outline Variant, 0.5dp, no start inset |
| **Selection** | Surface Variant background on selected/tapped |

#### Section Header

| Property | Specification |
|---|---|
| **Typography** | Label Large, On Surface Variant color |
| **Padding** | 16dp horizontal, 8dp top (section separator) |
| **Trailing action** | Optional text button (See All) |

#### Expandable Item

| Property | Specification |
|---|---|
| **Variant** | List Item |
| **Trailing icon** | Chevron that rotates 180° on expand |
| **Expand content** | Body Medium, 16dp inner padding, 8dp animation duration |

---

### 9.7 Selection Components

#### Checkbox

| Property | Specification |
|---|---|
| **Size** | 20dp box, 48dp touch target |
| **Shape** | Extra Small (4dp) |
| **Colors** | Primary checked, Outline unchecked |
| **States** | Checked, unchecked, indeterminate, disabled |

#### Radio Button

| Property | Specification |
|---|---|
| **Size** | 20dp outer circle, 10dp inner fill, 48dp touch target |
| **Colors** | Primary selected, Outline unselected |

#### Switch

| Property | Specification |
|---|---|
| **Size** | 32dp × 20dp (thumb 16dp) |
| **Colors** | Primary on, Surface Variant off |
| **Usage** | Binary settings toggles |

#### Chips

| Property | Specification |
|---|---|
| **Shape** | Extra Small (4dp) or pill (Shape Full) |
| **Height** | 32dp |
| **Colors** | Surface Variant default, Primary selected |
| **Variants** | Filter chip (with close icon when selected), Input chip (with leading avatar), Action chip (no close) |

---

### 9.8 Media Components

#### Avatar

| Property | Specification |
|---|---|
| **Sizes** | 24dp (inline), 32dp (comment), 40dp (list item), 64dp (profile header), 96dp (profile hero) |
| **Shape** | Shape Full (circle) |
| **Fallback** | First letter of username, Primary Container background, On Primary Container text |

#### Image Card

| Property | Specification |
|---|---|
| **Shape** | Small (8dp) |
| **Aspect ratio** | 16:9 default, 1:1 for thumbnails |
| **Overlay** | Bottom gradient (80% → 0% black) for text on images |
| **Placeholder** | Surface Variant with icon |

#### Thumbnail

| Property | Specification |
|---|---|
| **Size** | 56dp × 56dp standard |
| **Shape** | Small (8dp) |
| **Usage** | Map marker image preview in lists |

---

### 9.9 Empty States

| Property | Specification |
|---|---|
| **Layout** | Centered vertically and horizontally |
| **Icon** | Custom illustration, Large (48dp) or Extra Large (48dp) |
| **Title** | Headline Small |
| **Body** | Body Medium, On Surface Variant color, max 280dp width |
| **Action** | Optional primary button below body |
| **Usage** | Empty map, empty feed, no results, first-login welcome |

Empty state illustrations should be playful and on-brand. Use custom emoji-styled illustrations rather than generic icons. Example: an empty map shows a confused cloud with a question mark.

---

### 9.10 Error States

| Property | Specification |
|---|---|
| **Layout** | Centered vertically and horizontally |
| **Icon** | Warning or error icon, Large (48dp) |
| **Title** | "Something went wrong" or context-specific |
| **Body** | Body Medium, user-friendly explanation |
| **Action** | "Try again" primary button |
| **Secondary** | "Contact support" text button (future) |

Error states should:
- Never show raw error codes or stack traces to users
- Provide a clear recovery path
- Be dismissable (user can navigate away)
- Not imply user error unless user error caused the issue

---

## 10) Navigation Architecture

### Navigation philosophy

The app uses a **map-centered hub-and-spoke model.** The map is the permanent hub. All other screens are spokes that return to the map.

```
                    [Settings]
                        │
              [Profile] │
                  │     │
    [Feed] ──── [MAP] ──── [Fart Detail]
                  │
            [Log Fart]
                  │
           [Flavor picker]
           [Intensity slider]
```

### Hierarchy

| Level | Screens | Behavior |
|---|---|---|
| **Hub** | Map | Always in back stack. Cannot be removed. |
| **Modal spokes** | Log Fart, Fart Detail | Pushed on top of map. Dismissed via back or swipe down. |
| **Tab spokes** | Social, Rank, Profile | Switched via bottom nav. Each has its own back stack within the tab. |
| **Full-screen** | Settings, Auth/Login | Covers entire screen, replaces tab context. |

### Back behavior

| From | Back action |
|---|---|
| Modal spoke (Log Fart) | Save draft, dismiss to map |
| Tab spoke (Feed → Post detail) | Return to feed within tab |
| Tab switching | Preserve individual tab back stacks |
| Full-screen (Settings) | Return to previous screen |
| Auth screen (post-auth) | Remove auth from back stack (can't go back to login) |

### Deep linking considerations

- **Map with coordinates:** `fartmap://map?lat=X&lng=Y` — opens map centered on coordinates
- **Fart detail:** `fartmap://fart/{fartId}` — opens fart detail bottom sheet
- **Profile:** `fartmap://user/{userId}` — opens user profile
- **URLs must contain only public data** — no private user identifiers in paths

---

## 11) Screen Specifications

Screen-level specs live in `screens/*.md` (one per screen) and the Log Fart interaction model in `planning/product/features/create.md` (CREATE-03.1). The previous in-document re-specification was archived (`planning/archive/design/DesignSpecification-section11-screens.md`) because it conflicted with those sources.

## 12) Motion & Animation

### Principles

- **Subtle by default** — animations are 200–300ms, easing curves are standard deceleration
- **Purposeful** — every animation communicates a state change or spatial relationship
- **Never blocking** — animations are non-blocking; user interaction is never queued behind an animation
- **Consistent curves** — use Material 3 standard easing throughout

### Timing

| Type | Duration | Easing |
|---|---|---|
| Micro-interaction (hover, press) | 50–100ms | Fast-out linear-in |
| Component enter/exit | 200ms | Standard deceleration |
| Screen transition | 300ms | Standard deceleration |
| Sheet expand/collapse | 300ms | Standard deceleration |
| Loading shimmer | 1.5s cycle | Linear |
| Marker drop animation | 400ms | Overshoot (spring-like) |

### Screen transitions

| Transition | Direction | Duration | Easing |
|---|---|---|---|
| Push (forward navigation) | Slide in from right | 300ms | Standard deceleration |
| Pop (back navigation) | Slide out to right | 300ms | Standard acceleration |
| Modal present | Slide up from bottom | 300ms | Standard deceleration |
| Modal dismiss | Slide down | 250ms | Standard acceleration |
| Bottom sheet present | Slide up | 300ms | Standard deceleration |

### Component animations

| Component | Trigger | Animation |
|---|---|---|
| **Marker appear** | After fart submission | Drop from top of screen at marker position, bounce settle, 400ms. Followed by a small radial ripple expanding outward. |
| **Reaction toggle** | Tap reaction | Scale up 1.2x → scale back to 1.0x over 150ms. |
| **FAB press** | Tap | Scale down to 0.95x → back to 1.0x over 100ms. |
| **Bottom sheet** | Open | Slide up, content fades in with 50ms stagger per row. |
| **Switch toggle** | Tap | Thumb slides 16dp, background color transitions, 200ms. |
| **Card press** | Tap | Elevation increase from 1→3, scale 1.0→0.98, 100ms. |
| **Like/reaction burst** | Tap reaction | 3–5 small particles emanate from reaction icon, fade out over 300ms. |
| **Badge earned** | Achievement unlock | Badge scales from 0→1.2→1.0 over 400ms with a brief glow. |
| **Streak milestone** | Day 7, 30, etc. | Brief full-screen celebration (subtle confetti-like particles, 1.5s) — used sparingly. |

### Loading animations

| Type | Animation |
|---|---|
| **Skeleton shimmer** | Gradient sweep left to right over content-shaped placeholders, 1.5s cycle |
| **Button loading** | Text fades to CircularProgressIndicator (20dp) centered in button, 200ms |
| **Pull to refresh** | Standard Material pull indicator, colored Primary |
| **Image loading** | Crossfade from placeholder to loaded image, 300ms |

### Gesture behavior

| Gesture | Result |
|---|---|
| **Pull down (map)** | Refresh markers |
| **Pull down (feed)** | Refresh feed |
| **Swipe left/right (carousel)** | Navigate between carousel items |
| **Long press (map)** | Drop pin for location adjustment |
| **Swipe down (bottom sheet)** | Dismiss sheet |
| **Pinch (map)** | Zoom in/out |

---

## 13) Accessibility

### Minimum touch targets

All interactive elements must have a minimum touch target of **48dp × 48dp**, regardless of visual size. If a visual element is smaller (e.g., a 24dp icon button), the touch target extends invisibly to 48dp.

### Contrast requirements

- Text-level contrast ratio: **minimum 4.5:1** (normal text), **3:1** (large text ≥18sp bold or ≥24sp regular)
- All interactive element borders must meet 3:1 contrast against their background
- Primary text on backgrounds: always meet AA standard
- Placeholder and disabled text: minimum 3:1 ratio against background

### Dynamic font support

- All text uses `sp` units and scales with system font size settings
- Layouts must accommodate up to 200% font scaling without text truncation or overlapping
- Cards and containers use min-height rather than fixed height to accommodate scaled text

### Screen reader support

- All icons must have `contentDescription` (descriptive) or `contentDescription = null` (decorative)
- Bottom navigation tabs announce: "Map tab, selected" / "Feed tab, not selected"
- Map markers announce: "Fart by [username], [flavor], [time ago]". Interim (STORY-01-03-03): "A level-[flavor] fart, [time ago]" ("Your …" for your own), because the public fart doc carries no display name; the username joins when the detail sheet loads the author (EPIC-01-05)
- Reaction toggles announce: "Laugh reaction, 42 people, double tap to react"
- Sliders announce current value on focus and on change
- Bottom sheets announce their content role on open
- Navigation actions announce: "Back, double tap to navigate back"

### Color blindness

- No information is conveyed through color alone. All color-coded states have redundant indicators:
  - Visibility: icon + label (not just color)
  - Reaction state: filled/unfilled icon + count change
  - Active/inactive: shape/icon change + color change
  - Error states: icon + text + border change

### Focus management

- Focus order follows visual order (left to right, top to bottom)
- Modal screens trap focus until dismissed
- Bottom sheets move focus to first interactive element on open
- When a dialog closes, focus returns to the element that triggered it

---

## 14) Responsive Behavior

### Phone portrait (primary target)

| Property | Value |
|---|---|
| **Width** | 360–428dp |
| **Orientation** | Portrait (locked for most screens except map) |
| **Edge padding** | 16dp |
| **Bottom nav** | Bottom navigation bar visible |
| **Top app bar** | Standard height (56dp) |
| **Map** | Full width, full remaining height |

### Phone landscape

| Property | Value |
|---|---|
| **Width** | 568–926dp |
| **Orientation** | Landscape supported on map screen only |
| **Bottom nav** | Optional auto-hide. Navigation rail if width >= 600dp. |
| **Map** | Full screen with overlay controls |
| **Log Fart** | Portrait-locked (creation is portrait-only) |

### Foldable (posture-aware)

| Property | Value |
|---|---|
| **Tabletop mode** | Map on top half, controls on bottom half |
| **Dual screen** | Map on left screen, fart detail / feed on right screen |
| **Hinge** | Content avoids hinge area |
| **Continuity** | Screen state preserved across fold/unfold |

### Tablet (≥600dp)

| Property | Value |
|---|---|
| **Layout** | Master-detail where applicable |
| **Navigation** | Side navigation rail (72dp) replaces bottom nav |
| **Map** | Map occupies left 60%, right panel shows detail/context |
| **Log Fart** | Centered modal with max width 480dp |
| **Feed** | Two-column grid layout instead of single column list |
| **Edge padding** | 24dp (more breathing room) |

---

## 15) Compose Implementation Notes

### Design token structure

Define design tokens as a Kotlin object hierarchy for type-safe access:

```kotlin
// Color.kt
object FartMapColors {
    // Core palette
    val Primary = Color(0xFFFF8C00)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFFFE0B2)
    val OnPrimaryContainer = Color(0xFF4E2600)
    // ... all theme colors
    
    // Semantic
    val Success = Color(0xFF2E7D32)
    val Warning = Color(0xFFF57C00)
    val Error = Color(0xFFD32F2F)
    val Info = Color(0xFF1565C0)
}

// Typography.kt
val FartMapTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default, // Inter applied at theme level
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    // ... all type styles
)
```

### Theme architecture

```kotlin
// Theme.kt
@Composable
fun FartMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme(/* FartMapDarkColors */) 
                      else lightColorScheme(/* FartMapLightColors */)
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FartMapTypography,
        content = content
    )
}
```

Apply the Inter font family globally at the `MaterialTheme` level using `Typography.Default`:

```kotlin
val FartMapTypography = Typography(
    displayLarge = TextStyle(fontFamily = InterFontFamily),
    // ... where InterFontFamily is loaded at theme initialization
)
```

### Component architecture

Each component follows this structure:

```kotlin
@Composable
fun FartMapButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    // Internal composable, never exposes raw Material Button
}
```

Rules:
- No composable exposes raw Material 3 component types as public API
- All composables accept `modifier: Modifier = Modifier` for external layout control
- Loading states are built into components, not layered externally
- Color overrides are not exposed — colors come from the theme

### Reusable composable strategy

| Pattern | When to use |
|---|---|
| **Atom composable** | Single component (Button, Chip, TextField) — `core/design/` |
| **Molecule composable** | Composed atom group (AvatarWithName, FartMetadataRow) — `core/design/` |
| **Organism composable** | Feature-specific composite (FartCard, ProfileHeader) — `feature/*/presentation/` |
| **Screen composable** | Full screen layout (MapScreen, LogFartScreen) — `feature/*/presentation/` |

### State handling recommendations

- Screens observe ViewModel state via `collectAsStateWithLifecycle()`
- Local UI state (text field values, scroll position) uses `remember` / `mutableStateOf`
- Animations use `animateXAsState` for simple transitions, `Animatable` for more control
- Bottom sheet state managed via `rememberBottomSheetState()` + `BottomSheetScaffold`
- Snackbar state managed via `rememberSnackbarHostState()`

### Material 3 usage guidelines

- Use `MaterialTheme.colorScheme` for all colors — never hardcode color values in components
- Use `MaterialTheme.typography` for all text — never hardcode font sizes in composables
- Use `M3.*` shapes through `Shape` tokens — define `FartMapShapes` and reference them
- Use `MaterialTheme.shapes` for all shape references
- Prefer Material 3 adaptive components (`BottomSheetScaffold`, `NavigationSuiteScaffold`) where available
- Avoid experimental Material 3 APIs in production code unless documented

---

## 16) Consistency Rules

These rules are mandatory. Every screen must pass all rules before it is considered complete.

### Spacing

- [ ] Screen content uses 16dp horizontal edge padding (exception: full-bleed map)
- [ ] Cards have 16dp inner padding
- [ ] Between stacked cards: 12dp
- [ ] Between related elements: 8dp
- [ ] Between section headers and content: 16dp
- [ ] Bottom sheets have 24dp inner padding

### Typography

- [ ] Screen titles use Display Small (36sp) or Headline Large (32sp) — not custom sizes
- [ ] Body text uses Body Large (16sp) for primary content, Body Medium (14sp) for secondary
- [ ] Button text uses Label Large (14sp)
- [ ] Captions and timestamps use Body Small (12sp)
- [ ] No text uses font sizes outside the defined typography scale
- [ ] Numeric data (counts, XP, streaks) uses JetBrains Mono

### Icons

- [ ] Navigation icons: 24dp, Outlined variant, inactive = On Surface Variant 38% opacity, active = Primary
- [ ] Action icons in top app bar: 24dp, On Surface color
- [ ] Inline icons with labels: 20dp, label uses matching typography
- [ ] Decorative icons (empty states): 48dp+, custom illustration style
- [ ] No icon substitutes for a critical text label

### Button hierarchy

- [ ] Maximum one Primary Button per screen
- [ ] Destructive actions use Error-colored Outline Button, never Primary Button
- [ ] Primary = filled pill, Secondary = outlined small radius, Tertiary = text button
- [ ] FAB used only on map screen for the Log Fart action

### Navigation

- [ ] Bottom nav bar visible on all main tab screens (Map, Social, Rank, Profile)
- [ ] Log Fart is a modal sheet launched from map FAB — not a bottom nav tab
- [ ] Back navigation preserves draft state
- [ ] Map is always in the back stack, never removed
- [ ] Settings and Auth are full-screen overlays, not tab destinations

### Interaction patterns

- [ ] Every tappable element has a visual press state (scale, background tint, or elevation change)
- [ ] Every action shows feedback within 100ms (optimistic UI for data changes)
- [ ] Confirm destructive actions with a dialog
- [ ] Swipe-to-dismiss available on bottom sheets and modals
- [ ] Loading states (skeleton or spinner) shown for all async content areas
- [ ] Error states always provide a recovery action

### Color

- [ ] No information conveyed through color alone — always pair with icon, label, or shape
- [ ] Disabled components use 38% opacity on On Surface Variant
- [ ] Link text uses Primary color
- [ ] Error text uses Error color
- [ ] Success text uses Success color

### Animation

- [ ] Duration: 200–300ms for most transitions, 100ms for micro-interactions
- [ ] Easing: Material 3 standard easing (deceleration for enter, acceleration for exit)
- [ ] Animations never block user interaction
- [ ] Skeleton shimmer uses 1.5s cycle
- [ ] Celebratory animations (badge, milestone) used sparingly — maximum once per session

---

*This design specification is the authoritative source for all visual and interaction design decisions. Every screen, component, and animation must be consistent with this document. Inconsistencies between this document and any downstream design artifact are resolved in favor of this document.*

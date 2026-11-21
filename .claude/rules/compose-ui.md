---
paths:
  - "shared/src/**/presentation/**"
  - "shared/src/**/core/design/**"
  - "shared/src/commonMain/composeResources/**"
---

# Compose UI rules

- **Tokens only.** Colors, type, spacing, shapes and elevation come from `core/design` (generated from `planning/design/DesignSpecification.md` §3–§7). Never hardcode a hex color, dp size or font.
- **Every screen spec state is implemented:** Loading / Empty / Success / Error / Offline, as listed in `planning/design/screens/<Screen>.md`. Empty and error states carry humorous copy.
- **Strings come from Compose resources** (`composeResources/values/strings.xml`), never literals in composables. The copy follows the product voice ("Logged. The world felt it.", not "Post saved").
- **Composables are stateless.** `XScreen(state, onAction)` plus a thin `XRoute` that collects the ViewModel. Hoist state, pass lambdas, and give `Modifier` a default value as the first optional parameter.
- **Previews:** one `@Preview` per meaningful state, fed with fake State objects (no Koin in previews).
- **Accessibility:**
  - content descriptions on icons and markers;
  - touch targets of 48 dp or more;
  - no meaning carried by color alone;
  - text scales.
- **No precise coordinates** in any UI for content the user doesn't own. Show the coarse area only.
- **No purchase prompts** on the map, reactions, comment threads or privacy/moderation screens (`planning/product/Monetization.md` §6). Locked items stay visible; a tap opens the unlock sheet and never auto-opens a paywall.
- **Platform conventions** (sheets, back gesture) may differ; behavior may not.

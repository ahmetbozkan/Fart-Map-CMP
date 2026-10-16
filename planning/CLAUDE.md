# CLAUDE.md — Agent Entrypoint: Fart Map

> **Read this file first.** Before reading any other planning document, before writing any code, before making any decision. This is the front door.
> If you have already read this file in the current session, do not re-read it. Proceed to the task-relevant document.

---

## 1) What This Project Is

**Fart Map** is a location-based social entertainment mobile application. Users log fart moments with location, humor metadata, and optional audio. Those moments appear as markers on a shared map. The app layers social interaction, progression, and identity on top of that map.

Target platforms: **Android and iOS**, built from a shared Kotlin/Compose Multiplatform codebase.

This repository (`FartMap-CMP`) is a **proof-of-concept reference only**. The PoC is not being continued. A new project will be built from scratch by AI agents using the documents in `planning/`. Owner decision 2026-10-14: the owner creates the new repository, the `dev`/`prod` Firebase projects and the store/ad/purchase consoles, then copies `planning/` into the new repository; until that copy exists, this repository is the home of the planning system and no application code is written.

The planning system is the specification for that new project. It must be readable and actionable by an AI agent with no prior context.

---

## 2) Your Role

You are an **implementation agent**. You write code. You do not override product decisions. You do not relitigate architecture decisions. You do not add features not in the spec.

You work inside the boundaries defined by:
1. The product layer (`planning/product/`) — what the product is and does
2. The architecture layer (`planning/architecture/`) — how it must be built
3. The execution layer (`planning/execution/`) — how work is organized
4. The ADRs (`planning/meta/decisions/`) — settled decisions, not re-opened

Your job is to implement faithfully, flag blockers honestly, and escalate when a real conflict between the spec and reality arises. See `planning/meta/AgentGuardrails.md` for full escalation rules.

For implementation sessions requiring deeper playbooks, use `planning/skills/INDEX.md` to select the relevant skill workflow(s).

---

## 3) Mandatory Reading Before Writing Any Code

Read in this order. Do not skip steps.

### Step 1 — Orientation (read once per session, before anything else)

1. This file (`planning/CLAUDE.md`) — you are here
2. `planning/roadmap/STATE.md` — current phase, last completed task, next ready task
3. `planning/meta/AgentGuardrails.md` — hard stops and forbidden actions

### Step 2 — Architecture (read once per session, or when entering a new layer)

4. `planning/architecture/ArchitecturePrinciples.md` — engineering constitution
5. `planning/architecture/Architecture.md` — layered system design
6. `planning/architecture/SecurityAndSecrets.md` — read before writing any code that touches credentials, Firebase config, or API keys
7. `planning/execution/INDEX.md` — execution lifecycle navigation and required templates

### Step 3 — Task-specific (read before implementing a specific feature or layer)

| Working on | Read |
|---|---|
| Any new feature | `planning/product/Features.md` (find the feature system) |
| Social features | `planning/product/SocialSystems.md` |
| Progression/XP/streaks | `planning/product/ProgressionSystems.md` |
| Monetization surfaces | `planning/product/Monetization.md` |
| Local database / Room | `planning/architecture/DataModel.md` |
| Firestore schema / security rules | `planning/architecture/DataContracts.md` |
| Sync / offline behavior | `planning/architecture/SyncStrategy.md` |
| Firebase SDK usage | `planning/architecture/FirebaseStrategy.md` |
| Navigation | `planning/architecture/NavigationStrategy.md` |
| Error handling / logging | `planning/architecture/Observability.md` |
| Platform-specific code | `planning/architecture/MultiplatformStrategy.md` |
| Layer boundaries / dependencies | `planning/architecture/RepositoryRules.md` |
| Tech stack versions | `planning/architecture/TechStack.md` |
| Story authoring / traceability | `planning/execution/StoryTemplate.md` |
| Task authoring / validation | `planning/execution/TaskTemplate.md` |
| Sprint planning discipline | `planning/execution/SprintRules.md` |
| Review protocol | `planning/execution/ReviewChecklist.md` |
| UX flow to screen mapping | `planning/design/UXFlowCatalog.md` |
| Screen-level UX contract | `planning/design/ScreenSpecificationTemplate.md` |
| Design-to-dev handoff | `planning/design/ImplementationHandoff.md` |
| Skill selection for current task | `planning/skills/INDEX.md` |
| Delivery artifact structure | `planning/roadmap/SYSTEM.md` and `planning/roadmap/INDEX.md` |
| A settled decision | `planning/meta/decisions/ADR-*.md` |
| Purchases, rewarded ads, entitlements | `planning/product/Monetization.md`, ADR-0016, ADR-0017, `planning/skills/monetization-and-entitlements.md` |
| Backend rules and functions | `planning/architecture/FirebaseStrategy.md` §6a–§7, `planning/architecture/DataContracts.md` §5 |
| Session, guest and account linking | ADR-0018, `planning/architecture/NavigationStrategy.md` §4 |
| Location storage and fuzzing | ADR-0019, `planning/architecture/DataContracts.md` §3.2 |

### Step 4 — Definition of done

Before marking any task complete, verify against `planning/execution/DefinitionOfDone.md`.

---

## 4) The Three Non-Negotiable Constraints

Memorize these. They override everything else — any local reasoning, any clever shortcut, any "it's just for now" justification.

1. **No secrets in version control.** No API key, credential, token, config file, or `.plist` file may exist in any file tracked or trackable by git. Read `planning/architecture/SecurityAndSecrets.md` before writing any code that touches a secret.

2. **No error swallowing.** Every caught exception must produce either a typed error state visible to the UI or an explicit observability event. `e.printStackTrace()`, silent catches, and empty catch blocks are prohibited.

3. **No dependency direction violations.** The direction is: Presentation → Domain ← Data ← Platform ← Infrastructure. Domain never imports Data. Presentation never imports Data directly. Any dependency that runs against this direction is rejected before any other evaluation.

---

## 5) The Settled Decisions

Do not re-litigate these. They are settled. If you believe new information genuinely changes a tradeoff, write an ADR proposal and flag it for human review. Do not implement against a settled decision without that step.

| ADR | Decision |
|---|---|
| ADR-0001 | Room for local persistence |
| ADR-0002 | GitLive Firebase as the single backend SDK |
| ADR-0003 | Koin for dependency injection, with expect/actual platform modules |
| ADR-0004 | Custom StateViewModel as the only presentation pattern |
| ADR-0005 | Type-safe Compose Navigation with serializable routes |
| ADR-0006 | Session-driven start destination |
| ADR-0007 | Local-first reads with remote backfill |
| ADR-0008 | expect/actual pattern for all platform-specific code |
| ADR-0009 | Follow-only social graph with approval |
| ADR-0010 | Visibility tiers: Public / Followers / Private |
| ADR-0011 | Territory system out of MVP |
| ADR-0012 | Following list private by default, user opt-in toggle |
| ADR-0013 | Signature: single style selection (no composable parts in MVP) |
| ADR-0014 | Audio recording: 10 seconds max, record → attach → play only |
| ADR-0015 | Cloud Functions as backend compute; Firebase scope adds Messaging, Crashlytics, App Check |
| ADR-0016 | RevenueCat KMP SDK for purchases; entitlements mirrored server-side |
| ADR-0017 | AdMob rewarded ads with server-side verification; single-use unlock tokens |
| ADR-0018 | Anonymous sessions are full sessions; Google / Apple / Anonymous only |
| ADR-0019 | Precise location owner-only; public projection fuzzed (geohash-6) |
| ADR-0020 | Phase 1 scope additions: purchases, rewarded ads, reaction push, delete, all filters, four tabs |

Full ADR documents: `planning/meta/decisions/`

---

## 6) Product Constraints You Must Enforce in Code

These are product-level rules that must be enforced architecturally, not just honored in design.

| Rule | Enforcement |
|---|---|
| Visibility defaults to `FOLLOWERS` for new users | Room entity default field value |
| Content visibility is enforced server-side | Firestore security rules — client check is UX only |
| Free users can always log a fart | No capability gate on creation flow |
| Free users can always view the full map | No visibility gate on map rendering |
| Free users can always react to public content | No paywall on reaction action |
| Rewarded ad unlocks are single-use / one action | No `unlock_expiry` field; no persistent session flag |
| No XP can be purchased | No monetization surface in progression layer |
| Intensity field must be stored (mocked at 0% ranking weight) | `intensity_value` in entity; 0.0 contribution in scoring |
| Following list is private by default | API/Firestore query must require opt-in flag before returning list |
| Precise lat/lng never displayed to user | Only coarse map thumbnail shown in UI |
| Precise coordinates are owner-only (ADR-0019) | Written only to `farts/{id}/private/location`; public documents carry fuzzed coordinates |
| Anonymous sessions are full sessions (ADR-0018) | Session model `Active(anonymous | linked)`; linking preserves the uid |
| Non-free cosmetics need a server-verified entitlement or unlock token (ADR-0016/0017) | Rules and function checks; the client never grants itself an unlock |
| Rewarded ads: single use, max 3 views/day | Cap and consumption enforced in Cloud Functions |
| Minimum age 13, age band only | `users.age_band`; under-13 blocked at sign-up; no date of birth stored |

---

## 7) Escalation Rules (Summary)

Full rules in `planning/meta/AgentGuardrails.md`. Summary:

**Stop and flag to human when:**
- A spec requirement is technically impossible with the current stack
- Two planning documents give contradictory instructions for the same behavior
- Implementing a task requires committing a secret
- A task requires reversing a settled ADR
- A task requires adding a feature not present in any planning document

**Never do these without explicit human instruction:**
- Commit any file that contains an API key, credential, or configuration secret
- Add a feature not in `planning/product/Features.md`
- Change a product default (visibility, privacy, monetization surface)
- Push to the remote repository
- Change a product, architecture, privacy, monetization or security decision recorded in `planning/` without approval (decisions are frozen during implementation). Required state updates (`roadmap/STATE.md`, `meta/ChangeLog.md`, work items under `roadmap/`) and editorial fixes are mandatory, not forbidden (`AgentGuardrails.md` §9)
- Delete test coverage that was previously passing

---

## 8) Architecture in One Paragraph

Five layers. One direction. Presentation observes ViewModels. ViewModels expose typed State, consume typed Actions, emit one-time Events. Domain contains pure business logic and defines interfaces. Data implements those interfaces, owns local persistence (Room) and remote sync (Firestore via GitLive Firebase). Platform provides expect/actual implementations of shared contracts. Infrastructure initializes everything. Layer dependencies point downward only: Presentation → Domain ← Data ← Platform ← Infrastructure. The Domain layer is the center of gravity — it defines contracts, never implements infrastructure.

---

## 9) Planning System Quick Reference

| Concern | Document |
|---|---|
| Why this product exists | `product/Vision.md` |
| What the product is and is not | `product/ProductGovernance.md` |
| Feature scope and spec | `product/Features.md` |
| Social system rules | `product/SocialSystems.md` |
| Progression and XP rules | `product/ProgressionSystems.md` |
| Monetization rules | `product/Monetization.md` |
| Release phases and gates | `product/ReleaseStrategy.md` |
| Content safety rules | `product/ContentSafety.md` |
| Engineering constitution | `architecture/ArchitecturePrinciples.md` |
| Layered architecture | `architecture/Architecture.md` |
| Tech stack versions | `architecture/TechStack.md` |
| Secrets management | `architecture/SecurityAndSecrets.md` |
| Local data schema | `architecture/DataModel.md` |
| Remote data schema | `architecture/DataContracts.md` |
| Repository / layer rules | `architecture/RepositoryRules.md` |
| Sync contract | `architecture/SyncStrategy.md` |
| Firebase conventions | `architecture/FirebaseStrategy.md` |
| Cross-platform boundaries | `architecture/MultiplatformStrategy.md` |
| Navigation | `architecture/NavigationStrategy.md` |
| Error handling / analytics | `architecture/Observability.md` |
| Hard stops and escalation | `meta/AgentGuardrails.md` |
| Workflow and task management | `execution/Workflow.md` |
| Execution document map | `execution/INDEX.md` |
| Story authoring contract | `execution/StoryTemplate.md` |
| Task authoring contract | `execution/TaskTemplate.md` |
| Sprint planning constraints | `execution/SprintRules.md` |
| Review pass/fail checklist | `execution/ReviewChecklist.md` |
| Definition of ready | `execution/DefinitionOfReady.md` |
| Definition of done | `execution/DefinitionOfDone.md` |
| UX flow registry | `design/UXFlowCatalog.md` |
| Screen spec template | `design/ScreenSpecificationTemplate.md` |
| Design-to-dev handoff | `design/ImplementationHandoff.md` |
| Skill library entrypoint | `skills/INDEX.md` |
| Roadmap hierarchy and IDs | `roadmap/SYSTEM.md` |
| Roadmap templates and navigation | `roadmap/INDEX.md` |
| Current state / next task | `roadmap/STATE.md` |
| All settled decisions | `meta/decisions/ADR-*.md` |
| Full planning navigation | `meta/INDEX.md` |
| Audit reports and decision logs | `meta/audits/` |
| Archived process documents (history, not authority) | `meta/archive/` |

---

*This file is the agent front door. It does not contain product decisions, architecture decisions, or implementation details. Those live in the documents it points to.*

# Planning System ChangeLog

Tracks structural and governance changes to the planning system itself.

## 2026-10-21 (session 13 — STORY-01-01-01 implemented)

### Context
- Owner said "go" for STORY-01-01-01 and commits the changes himself; the agent implemented TASK-01-01-01-03 → 02 → 01 → 04 → 06 → 05 and validated on both platforms and the Firestore emulator. Nothing committed or pushed by the agent.
- Result: both Android flavors and both iOS schemes build and launch to the placeholder (environment + Firebase project) on the emulator and the iPhone 17 simulator, each bundle with exactly one Firebase config; shared tests pass on the Android host and the iOS simulator; lint has 0 errors; backend lint/unit/rules tests pass. CI workflows exist but have not run (first push pending).

### Decisions recorded
- ADR-0021 (Accepted): three Kotlin modules — `shared` without Firebase types, `firebaseData` with the GitLive adapters, `umbrella` producing the iOS framework — so shared tests never load Firebase and the iOS link is explicit (resolves the PoC's `FirebaseCore not found` failure by construction).
- Crashlytics Gradle plugin 3.0.8 applied to `androidApp` (required once the Crashlytics SDK is on the classpath).
- Toolchain baseline recorded in `TechStack.md` §3.1: Kotlin 2.4.21, Compose Multiplatform 1.12.2 (Material3 1.12.0-alpha03, no stable exists), AGP 9.4.1 / Gradle 9.8.2 (raised from the template's 9.1.1 / 9.5.1 on day one), Koin 4.2.2, Navigation 2.9.2, Lifecycle 2.11.0, GitLive 2.7.0, Firebase BOM 35.0.2, Firebase iOS SDK 13.0.1, Node 22 / firebase-tools 15.
- `AppEnvironment` is parsed in `shared` from a string the shells supply (`BuildConfig.APP_ENV`, `Info.plist` `APP_ENV`) instead of `expect`/`actual` (TASK-01 deviation, recorded in the task).

### Added
- `planning/meta/decisions/ADR-0021-shared-module-layout-for-firebase-isolation.md`
- Repository: `firebaseData/` (with Android/iOS actuals for the project id), `umbrella/`, `scripts/inject-config.sh`, `scripts/check-no-secrets.sh`, `.gitleaks.toml`, config templates (`google-services.json.template` ×2, `GoogleService-Info-{dev,prod}.plist.template`, `secrets.properties.template`, `local.properties.example`), `iosApp/Configuration/{Dev,Prod}.xcconfig`, shared schemes `FartMap Dev` / `FartMap Prod`, `iosApp/iosApp/AppDelegate.swift`, `backend/` (rules, indexes, `firebase.json`, `.firebaserc`, functions project with tests), `.github/workflows/ci.yml` and `deploy-backend.yml`, `shared/.../core/platform/AppEnvironment.kt` with its test

### Changed
- `planning/architecture/Architecture.md` — §6 module layout and paths (ADR-0021), §11 row
- `planning/architecture/TechStack.md` — §3.1 baseline table filled with notes; §6.2 references ADR-0021
- `planning/roadmap/stories/STORY-01-01-01.md` — Review; acceptance criterion 1 named by the real modules; completion evidence; defects found and fixed
- `planning/roadmap/tasks/TASK-01-01-01-01…06.md` — Review with completion evidence (TASK-05 explicitly unverified until CI runs)
- `planning/roadmap/epics/EPIC-01-01.md` — story status, module names in scope and acceptance condition 1
- `planning/roadmap/STATE.md` — snapshot, matrix, locked decisions (ADR-0021, baseline), owner items regrouped (CI/deploy gates, local toolchain, later epics), next authorized work (owner commit + CI, then Mode 4 and the next stories)
- Root `CLAUDE.md` and `README.md` — modules, toolchain, commands, config injection

### Defects found while validating (all fixed, none committed)
- AGP 9 disables `buildFeatures.resValues`; flavor `resValue` calls failed configuration.
- Kotlin cannot infer the framework build type from the custom Xcode configuration names; `KOTLIN_FRAMEWORK_BUILD_TYPE` now set per configuration in `Config.xcconfig`.
- An Xcode membership exception on the `Configuration` folder bundled both environment plists into every app; removed, each bundle carries one config.
- `backend/functions/.gitignore` ignored `src/lib/` through an unanchored `lib/`; anchored to `/lib/`.
- Both Android flavors aborted at start in `FirebaseInitProvider` ("The Crashlytics build ID is missing") because `:firebaseData` brings the Crashlytics SDK without its Gradle plugin; `com.google.firebase.crashlytics` 3.0.8 applied to `androidApp` (catalog `firebaseCrashlytics`), verified by launching both flavors side by side on the emulator (dev → `fart-map-dev`, prod → `fart-map-1a558`).
- iOS crashed at first composition with `kotlin.NullPointerException` in GitLive 2.7.0 `FirebaseApp.options` (Apple implementation force-unwraps `databaseURL`, which projects without Realtime Database do not have). `FirebaseProjectInfo` now uses platform actuals (`FIRApp.defaultApp()?.options?.projectID` through the FirebaseCore cinterop on iOS; GitLive on Android); both schemes relaunched on the simulator and stay alive. Noted in `TechStack.md` §3.1.

### Not verified
- Acceptance criterion 4 (CI within one workflow) and the dev deploy job: need the owner's push, the GitHub secrets and the Blaze plan.
- Nothing else: the manual launch checks (side-by-side Android flavors on the emulator; both iOS schemes on the simulator) were run by the agent and are recorded in TASK-01-01-01-01/04.

## 2026-10-18 (session 12 — new repository, bootstrap planning)

### Context
- Owner created `github.com/ahmetbozkan/Fart-Map-CMP` from the current Kotlin Multiplatform template (Kotlin 2.4.20, Compose Multiplatform 1.12.1, AGP 9.1.1, Gradle 9.5.1 with a Java 21 daemon JVM), created Firebase projects `fart-map-dev` and `fart-map-1a558`, registered `com.ahmetbozkan.fartmap` and `com.ahmetbozkan.fartmap.dev` on both platforms, and copied `planning/` unchanged.
- The four Firebase config files were found staged (not committed, not pushed); they were unstaged, kept on disk, and `.gitignore` rules were added. Recorded in STORY-01-01-01.

### Added
- Repository root `CLAUDE.md` (thin entrypoint: pointer to `planning/CLAUDE.md`, toolchain, commands, non-negotiables)
- `planning/roadmap/tasks/TASK-01-01-01-01` … `-06` (environment model, toolchain baseline, secrets hygiene, iOS Firebase linking, CI, backend scaffold)

### Changed
- `planning/roadmap/stories/STORY-01-01-01.md` — Ready; dependencies met; task list and recommended order; incident note
- `planning/roadmap/epics/EPIC-01-01.md` — Active; module names and package root settled (`shared`/`androidApp`/`iosApp`, `com.ahmetbozkan.fartmap`)
- `planning/roadmap/phases/PHASE-01.md` — Entry Gate 1 marked met for bootstrap; remaining owner items listed per epic
- `planning/roadmap/STATE.md` — snapshot, open items (owner to-dos), next authorized work (Mode 3 on "go")

### Notes
- Baseline build of the untouched template was run with JDK 17 launcher; result recorded in the session report and to be captured in `TechStack.md` §3.1 by TASK-02.
## 2026-10-14 (session 11 — planning closeout)

### Decisions recorded (owner answers to the closeout questions)
- Repository role confirmed: this PoC is frozen; the owner creates the new repository, Firebase `dev`/`prod` projects and consoles, then copies `planning/`.
- ADR-0015 Cloud Functions as backend compute; Firebase scope adds Messaging, Crashlytics, App Check.
- ADR-0016 RevenueCat KMP SDK for purchases; entitlements mirrored server-side.
- ADR-0017 AdMob rewarded ads with server-side verification; single-use unlock tokens; daily cap 3.
- ADR-0018 Anonymous sessions are full sessions; providers Google / Apple / Anonymous only; linking preserves the uid; age gate 13+ with an age band.
- ADR-0019 Precise location stored owner-only; public projection fuzzed (geohash precision 6, no delay — provisional parameters).
- ADR-0020 Phase 1 scope additions: a la carte purchases, rewarded ads, reaction push notification, delete own fart, all three map filters, four-tab navigation Map / Social / Rank / Profile.
- Also recorded: reports auto-hide at three distinct reports; no server-side rate limits in Phase 1; MapKit on iOS and Google Maps on Android; GitHub Actions with iOS on CI; Firebase kept behind interfaces for shared tests; dependency bootstrap on the latest stable compatible set.

### Added
- `planning/meta/decisions/ADR-0015` … `ADR-0020`
- `planning/meta/audits/2026-10-14-planning-closeout-audit.md` — Stage A/B audit, build verification, decision log
- `planning/meta/archive/` — the five 2026-06 process documents moved out of the read chain (`Planning-Rebuild`, `PlanningAuditReport`, `PlanningStructureReview`, `AIOperatingSystemReview`, `PlanningDependencyGraph`)
- `planning/design/screens/FartDetailSheet.md`, `CosmeticUnlockSheet.md`, `SocialTabScreen.md`, `RankTabScreen.md`
- `planning/skills/monetization-and-entitlements.md`
- `planning/roadmap/phases/PHASE-01.md`, `planning/roadmap/epics/EPIC-01-01.md`, `planning/roadmap/stories/STORY-01-01-01.md` (Backlog)

### Changed
- `planning/CLAUDE.md` — §1 new-repository flow; §5 ADR table to 0020; §6 enforcement rows for anonymous sessions, location split, entitlements, ad cap, age band; §7 freeze rule narrowed to decisions (state and work-item updates are mandatory); §3/§9 pointers to monetization, backend, session, audits and archive
- `planning/meta/INDEX.md` — tree, read order (root process docs removed; ProductGovernance first in the product spine; ADRs before architecture), registry rows, single-definition rule now names Features (ids) and ReleaseStrategy (phases, weights)
- `planning/meta/AgentGuardrails.md` — archive and audits declared non-authoritative
- `planning/meta/PlanningGlossary.md` — Systems docs no longer "future"
- `planning/meta/decisions/ADR-0001…0008` — source path moved to the archive; ADR-0004/0007 status qualifiers marked satisfied
- `planning/roadmap/STATE.md` — full refresh: closeout complete, implementation authorized for STORY-01-01-01 in the new repository, open items reduced to owner deliverables and three confirmations
- `planning/product/ReleaseStrategy.md` — §4 MVP list (filters, purchases/ads, delete, push, anonymous onboarding, monetization and navigation blocks); §5 matrix reconciled to Features ids/names (MAP-03/04/05/06, SHARE-02/03/04, MOD-01…04, PRIV-03, COMMUNITY-02/03) and to ADR-0020 phases (MON-01/03, NOTIF-01, CREATE-08 → Phase 4, CREATE-09 split); Gate 1 monetization-integrity criterion
- `planning/product/Features.md` — §3 marked historical; MAP-03/04 phases and names; undefined ids (CONTENT-09, SAFE-01, METRICS, SAFE/MOD) replaced; CREATE-03.1 Steps 3/5/6/7/8/10 (unlock sheet options, intensity resolved, 280-character limit, "Let It Rip"); CREATE-03.2 table and rules rewritten for purchase + rewarded ad in Phase 1; CREATE-05/08/09, PROFILE-01/04/06, PROG-05, MON-01/03, NOTIF-01 phases; §18 kernel; §19 item 7; stray line removed
- `planning/product/Monetization.md` — phase update note; §3 purchase path and entitlement source of truth; §4 platform payments via RevenueCat; §5 Phase 1 and AdMob decided; §9 checklist rows
- `planning/product/ContentSafety.md` — §3 fuzzing default and no-delay decision; §5 minimum age 13 with age band; §7 stale reference; §10 Phase 1 baseline (report + block, age gate, App Check, entitlement checks)
- `planning/product/Glossary.md` — resolved Signature/Intensity questions; Intensity scale; "Feeling" marked dropped; §11 register all resolved; follow wording
- `planning/product/SocialSystems.md` — Block entity decided; cooldown and rate limits marked provisional defaults
- `planning/product/Prioritization.md` — Phase 1 weights from ReleaseStrategy; worked example recomputed; dependency-gate wording
- `planning/product/SuccessMetrics.md`, `Vision.md`, `Personas.md`, `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md` — "friend" wording replaced by follow/follower terms; territory labelled as Phase 5 candidate; stale "future doc" and authorization text removed; L2 health line added; Vision §6 pointed at the loop registry
- `planning/product/ProductPillars.md` — Friends and Territory removed from phase focus; Phase 3 renamed; Phase 5 pointer; §10 weights aligned to ReleaseStrategy §3
- `planning/product/ProgressionSystems.md` — XP cap, streak freeze and score basis contradictions fixed; TOC anchor; "Friends" board renamed Followers; PROG-06 header
- `planning/product/ProductGovernance.md` — §9 names the Community Organizer as future-tier
- `planning/architecture/TechStack.md` — rewritten: settled stack additions, bootstrap baseline policy and table, backend toolchain, iOS linking rule, CI definition
- `planning/architecture/Architecture.md` — cloud services and external services; feature packages (monetization Phase 1, notifications); platform examples; Flow 1/4 reworded to the single sync path; §10/§11/§14 rows for ADR-0015…0019
- `planning/architecture/ArchitecturePrinciples.md` — §13 ADR registry wording
- `planning/architecture/SecurityAndSecrets.md` — anonymous sessions; location row; entitlement and token rows; config files never committed; §5 and §6 updates
- `planning/architecture/DataModel.md` — public/private coordinates; `is_hidden`; catalog, entitlement and unlock cache entities; DataStore keys; enums; tests
- `planning/architecture/DataContracts.md` — users fields; farts public coordinates and private subdocument; report fields; reaction enum; catalog, entitlements, tokens, devices collections; validation rules; rule intent §5.1/5.7/5.8/5.9; projection; tests
- `planning/architecture/FirebaseStrategy.md` — product scope; auth providers and linking; §6a Functions, §6b Messaging, §6c App Check/Crashlytics; environments; tests
- `planning/architecture/NavigationStrategy.md` — session states with anonymous Active; four-tab root; deep link; tests
- `planning/architecture/MultiplatformStrategy.md`, `SyncStrategy.md`, `Observability.md` — platform capability areas; online-only operations; crash reporting, monetization/notification telemetry, Phase 1 event taxonomy
- `planning/execution/SprintRules.md` — §7 now references `roadmap/SprintTemplate.md`
- `planning/skills/INDEX.md` — stale "missing files" section removed; monetization skill registered
- `planning/design/SYSTEM.md` — Stitch screen-by-screen process, optional Figma import, design-ready definition
- `planning/design/DesignSpecification.md` — four-tab navigation; Phase 1 variants for detail sheet, profile, settings; precise-location toggle removed; §11.9 Social tab, §11.10 Rank tab; checklist
- `planning/design/UXFlowCatalog.md` — UXF-11 Cosmetic Unlock, UXF-12 Reaction Push Return, UXF-13 Anonymous Start and Account Linking
- `planning/design/HANDOFF-UXF-01-04-MVP.md` — rewritten for all Phase 1 surfaces and the new ADRs
- `planning/design/screens/AuthLoginScreen.md`, `LogFartScreen.md`, `MapScreen.md` — anonymous start, unlock sheet, fuzzed markers

### Removed from the repository (owner-approved)
- `session-ses_11f4.md` (tracked chat transcript)
- `composeApp/secrets.properties`, `composeApp/google-services.json`, `iosApp/GoogleService-Info.plist` untracked (local copies kept); `.gitignore` extended

### Addendum 2026-10-15
- Owner confirmed the three interpretations: C3 means the locked cosmetics (avatars and sounds); ADR-0019 parameters (geohash precision 6, no publication delay) are settled; Phase 1 Social and Rank tab content stands as specified. ADR-0019, ADR-0020, `ContentSafety.md` §3, `roadmap/STATE.md` §3.2/§5 and the audit decision log updated. No open planning confirmations remain.

### Notes
- Build verification on 2026-10-14 (JDK 17): Android assemble and unit tests pass; iOS framework links; the shared iOS test binary and the Xcode app build fail with `framework 'FirebaseCore' not found` (documented in the audit and in `TechStack.md` §6.2).
- Owner answer C3 was read as the locked cosmetics (avatars and sounds); the five MVP reactions stay free. Confirmation requested in `STATE.md` §5.
- Documentation expansion stops with this session; further changes are driven by implementation stories.

## 2026-09-02 (session 10)

### Added
- `planning/roadmap/INDEX.md` - roadmap navigation, template ownership, and authoring sequence contract
- `planning/roadmap/PhaseTemplate.md` - canonical phase artifact schema with gate and traceability fields
- `planning/roadmap/EpicTemplate.md` - canonical epic artifact schema with parent-phase and acceptance-boundary fields
- `planning/roadmap/SprintTemplate.md` - canonical sprint artifact schema aligned to sprint-rule eligibility and evidence capture

### Changed
- `planning/CLAUDE.md` - synchronized read chain and quick-reference mappings to execution templates, design handoff artifacts, skills entrypoint, and roadmap governance/template docs
- `planning/meta/INDEX.md` - synchronized roadmap tree/read-order/registry entries to include roadmap index and phase/epic/sprint templates; clarified that roadmap delivery instances remain uninstantiated
- `planning/roadmap/STATE.md` - updated status snapshot, completion matrix, and next authorized work to reflect roadmap templating milestone and remaining implementation blockers

### Notes
- Governance synchronization progressed: entrypoint and index now reflect expanded execution/design/skills and roadmap template system.
- Roadmap delivery instances remain intentionally ungenerated; current additions are structure and contracts only.
- First design artifact population has started with MVP auth/map/log screen specs and initial flow handoff package; Stitch references remain placeholders pending concrete exports.
- Next milestone focus remains additional design artifact coverage and first execution-planning pass with DoR evidence.

## 2026-09-02 (session 9)

### Added
- `planning/execution/INDEX.md` - execution-layer navigation and ownership map
- `planning/execution/StoryTemplate.md` - story authoring contract with required traceability/evidence fields
- `planning/execution/TaskTemplate.md` - task authoring contract with dependency and validation fields
- `planning/execution/SprintRules.md` - sprint eligibility, capacity heuristic, selection algorithm, and in-sprint discipline
- `planning/execution/ReviewChecklist.md` - pre-Done review protocol aligned with DoD
- `planning/design/SYSTEM.md` - design-layer operating contract and Stitch integration governance
- `planning/design/UXFlowCatalog.md` - feature-to-flow mapping and MVP-critical flow registry
- `planning/design/ScreenSpecificationTemplate.md` - standardized per-screen specification template
- `planning/design/ImplementationHandoff.md` - design-to-implementation handoff gate and record template
- `planning/skills/SYSTEM.md` - skills-layer operating contract and skill-quality standard
- `planning/skills/INDEX.md` - required skill registry and source linkage map
- `planning/skills/architecture-governance.md` - architecture compliance skill playbook
- `planning/skills/repository-and-mappers.md` - repository/mapping skill playbook
- `planning/skills/offline-sync-operations.md` - sync queue/retry/conflict skill playbook
- `planning/skills/firebase-integration.md` - Firebase boundary and rule-safe integration skill playbook
- `planning/skills/multiplatform-boundaries.md` - expect/actual and parity skill playbook
- `planning/skills/navigation-and-session.md` - typed navigation and session-start skill playbook
- `planning/skills/stateviewmodel-pattern.md` - State/Action/Event presentation skill playbook
- `planning/skills/design-handoff-ui.md` - design artifact to UI implementation skill playbook
- `planning/skills/testing-and-validation.md` - DoR/DoD-aligned testing and evidence skill playbook
- `planning/skills/security-privacy-guardrails.md` - privacy/secrets/safety enforcement skill playbook
- `planning/skills/incident-debug-recovery.md` - autonomous repro/fix/regression skill playbook
- `planning/skills/planning-and-scope-control.md` - feature-to-story/task decomposition skill playbook
- `planning/roadmap/SYSTEM.md` - roadmap hierarchy/traceability/ID conventions contract

### Changed
- `planning/meta/INDEX.md` - synchronized planning tree, read order, and registry to include new execution/design/skills/roadmap-system artifacts; removed stale "skills missing" note
- `planning/roadmap/STATE.md` - updated completion matrix and next authorized work based on newly completed execution/design/skills foundations

### Notes
- Planning system now has complete foundations for execution control, design handoff, and skill-driven autonomous implementation support.
- Roadmap delivery instances (phases/epics/stories/tasks/sprints) remain intentionally ungenerated; only governance/system contracts were added.
- Next milestone should focus on governance synchronization (`planning/CLAUDE.md`) and initialization of roadmap folder scaffolding without generating delivery content prematurely.

## 2026-07-02 (session 8)

### Added
- `planning/architecture/TechStack.md` — technology/version governance contract covering approved stack, version policy, prohibited alternatives, and upgrade verification rules
- `planning/architecture/RepositoryRules.md` — repository boundary contract covering interface/implementation ownership, mapping placement, offline-first repository method rules, and cross-feature dependency restrictions
- `planning/architecture/SyncStrategy.md` — offline sync contract covering single sync path, pending operation lifecycle, retry/backoff policy, conflict resolution, idempotency requirements, and convergence behavior
- `planning/architecture/FirebaseStrategy.md` — Firebase integration contract covering SDK boundaries, auth/firestore/storage conventions, rules governance, and environment/config safety
- `planning/architecture/MultiplatformStrategy.md` — shared-vs-platform boundary contract covering expect/actual rules, parity obligations, and DI composition across platforms
- `planning/architecture/NavigationStrategy.md` — navigation contract covering type-safe routes, graph ownership, session-driven start mapping, and back stack rules
- `planning/architecture/Observability.md` — error/logging/analytics telemetry contract covering typed error modeling, structured/redacted logs, sync/auth telemetry requirements, and anti-pattern prohibitions

### Changed
- `planning/CLAUDE.md` — corrected guardrails references from non-existent `planning/execution/AgentGuardrails.md` to canonical `planning/meta/AgentGuardrails.md`
- `planning/product/ProductGovernance.md` — aligned rewarded-ad policy wording with resolved single-use one-action decision
- `planning/product/Features.md` — removed session/24h rewarded-ad TBD language and aligned monetization binding rules to single-use one-action unlock behavior
- `planning/product/Monetization.md` — removed session/24h rewarded-ad language and aligned unlock lifecycle/duration/checklist wording to single-use one-action behavior
- `planning/meta/INDEX.md` — synchronized architecture tree/read-order/registry entries with complete architecture contract set; removed outdated "missing architecture contracts" gap note
- `planning/roadmap/STATE.md` — updated architecture status to complete, revised snapshot/completion matrix/open questions/next authorized work sequence

### Notes
- Architecture contract layer is now complete.
- Cross-document contradiction resolved: rewarded-ad unlock behavior now consistently documented as single-use one-action in governance and product architecture touchpoints.
- Execution docs still exist in repo despite earlier "do not create execution docs yet" instruction from a later prompt; this remains a process-history inconsistency to be acknowledged in readiness review.

## 2026-07-02 (session 7)

### Added
- `planning/execution/SYSTEM.md` — execution kernel contract: closed control loop, work-item schema, decision authority matrix (Decide / Record / Escalate), escalation triggers, dependency sequencing rules, verification contract, state synchronization requirements
- `planning/execution/Workflow.md` — canonical work-item state machine: Backlog/Ready/In Progress/Blocked/Review/Done/Verified/Cancelled, allowed transitions, blocker protocol, required artifact updates per transition, workflow anti-patterns
- `planning/execution/DefinitionOfReady.md` — start gate criteria for all work types: mandatory readiness gates, type-specific gates (feature/bug/refactor/security/docs), read-before-start checklist, Ready evidence template
- `planning/execution/DefinitionOfDone.md` — completion gate criteria: mandatory done gates, evidence requirements by work type, post-done verification (`Done -> Verified`), state synchronization requirements
- `planning/architecture/DataModel.md` — local persistence contract for Room/DataStore: canonical local entities, enum contracts, privacy/retention rules, migration rules, required data-model test contract
- `planning/architecture/DataContracts.md` — remote/Firestore contract: canonical collections/doc fields, access-control intent, visibility and social graph enforcement contract, projection/privacy rules, remote test contract

### Changed
- `planning/meta/INDEX.md` — fully refreshed to current repository reality: updated planning tree, read order, registry owner mapping, dependency rules, and current gap list
- `planning/roadmap/STATE.md` — updated project snapshot, completion matrix, authoritative/provisional artifact lists, open questions, and next authorized work sequence after execution-kernel + data-contract milestones

### Notes
- Execution kernel now exists and is usable for execution planning.
- Implementation remains blocked pending remaining architecture contracts (`TechStack.md`, `RepositoryRules.md`, `SyncStrategy.md`, `FirebaseStrategy.md`, `MultiplatformStrategy.md`, `NavigationStrategy.md`, `Observability.md`) and final governance sync.
- Design layer authority remains provisional until explicitly finalized in governance chain.

## 2026-07-02 (session 6)

### Added
- `planning/meta/AgentGuardrails.md` — AI operating constitution with source-of-truth hierarchy, autonomy/approval boundaries, conflict resolution protocol, PoC usage policy, maintenance obligations, and session-end checklist
- `planning/roadmap/STATE.md` — live repository operating ledger with status snapshot, planning completion matrix, authoritative artifacts, locked constraints, open questions, and next authorized work sequencing
- `planning/architecture/SecurityAndSecrets.md` — architecture-level security and privacy contract: data sensitivity classification, secrets handling rules, trust boundaries, Firebase/backend posture, offline/cache safety, UGC/audio moderation security, logging redaction constraints

### Notes
- Rewarded ad unlock handling captured as single-use / one-action for downstream contract implications.
- These docs establish the governance and security baseline required before deeper execution/implementation phases.

## 2026-06-20 (session 5)

### Added
- `planning/product/ReleaseStrategy.md` — 8 sections: release philosophy, 7 sequencing principles, all 5 phase definitions (Phase 5 = TBD), full MVP definition (included and explicitly excluded features), Feature Placement Matrix covering all 50+ features across 12 systems with phase/reason/dependency, 4 phase-transition success gates tied to SuccessMetrics.md, 7 explicit non-goals (permanent exclusions), 8 AI decision rules for release planning
- `planning/product/ProductGovernance.md` — 9 sections: product identity (is/is-not), 6 non-negotiable principles (Map First, Humor First, Social Through Location, Identity Through Participation, Fair Competition, Cosmetic Monetization Only), 5-step decision framework, 10 named anti-patterns, monetization guardrails (allowed/forbidden binary table), social guardrails (graph model, visibility, map-first social rule, moderation philosophy), AI planning guardrails (mandatory preservation checks + escalation triggers), change management (traceability chain + change categories + freeze triggers), Final Product Constitution (standalone one-page summary for agent cold-start)

### Changed
- `planning/meta/INDEX.md` — tree, read order, and document registry updated; §5 gap note updated to reflect product layer completion

### Notes
- Product planning layer is now complete. 16 product documents authored, all TBDs resolved, product constitution finalized.
- Next layer: architecture/ (SecurityAndSecrets, Architecture, TechStack, DataModel, DataContracts, RepositoryRules, SyncStrategy, FirebaseStrategy, MultiplatformStrategy, NavigationStrategy, Observability)
- One remaining open architecture TBD: rewarded-ad unlock duration (session vs 24h) — must be resolved before DataContracts.md is authored.

## 2026-06-20 (session 4)

### Resolved decisions
- **Signature composition (TBD-3):** Single style selection. No composable parts in MVP. Each signature is one complete pre-composed style. See ADR-0013.
- **Audio recording constraints (TBD-4):** Max duration = 10 seconds (hard limit, server-side enforced). MVP scope: record → attach → play on marker tap only. No editing, trimming, or effects. No raw audio external share. Async post-write moderation. See ADR-0014.

### Added
- `planning/meta/decisions/ADR-0013-signature-single-style-selection.md`
- `planning/meta/decisions/ADR-0014-audio-recording-10s-mvp-simple.md`

### Changed
- `planning/product/Features.md` — PROFILE-03 TBD note updated (signature = single style); MAP-02 marker TBD note updated; CREATE-08 TBD note updated (10s, simple); CREATE-03.1 Step 8 duration updated (TBD → 10s); CREATE-03.2 voice row updated; §19 items 3 and 4 marked resolved
- `planning/meta/INDEX.md` — ADR-0013 and ADR-0014 added to tree

### Notes
- §19 Open Decisions register is now fully resolved. All six product-level TBDs are closed.
- One architecture-level TBD remains open: rewarded-ad unlock duration (session vs 24h) — deferred to implementation architecture stage.

## 2026-06-20 (session 3)

### Resolved decisions
- **Ranking model (TBD-1):** Confirmed as proposed — 50% XP / 20% Reactions / 15% Streak / 10% Challenges / 0% Intensity. Effective weight 95%. Unallocated 5% stays at zero.
- **Intensity weighting (TBD-2):** Mocked at 0% for MVP in both XP and ranking. Field captured and stored; no score contribution until future decision.
- **Daily XP cap (TBD-3):** Confirmed at 150 XP/day hard cap, server-side enforced.
- **Streak freeze (TBD-4):** Permanently closed — no streak freeze, ever. Not a deferral.
- **Following list visibility (TBD-5):** Following list is private by default; user-controlled opt-in toggle in profile privacy settings. Follower count remains publicly visible. Captured in ADR-0012.

### Added
- `planning/meta/decisions/ADR-0012-following-list-private-by-default.md`

### Changed
- `planning/product/ProgressionSystems.md` — §4 ranking model table updated to confirmed weights; §3 intensity XP note updated; §10 all five TBDs resolved; §11 checklist updated
- `planning/product/SocialSystems.md` — §4 following list visibility updated to reflect ADR-0012
- `planning/product/Features.md` — §19 TBDs 1, 2, 5 marked resolved; PROG-03 TBD note updated
- `planning/meta/INDEX.md` — ADR-0012 added to tree

## 2026-06-20 (session 2)

### Added
- `planning/product/Features.md` — CREATE-03.1 (10-step Log Fart interaction model), CREATE-03.2 (monetization binding rules), CREATE-03.3 (data model mapping + state machine + validation checklist) inserted after CREATE-03, before CREATE-04
- `planning/product/SocialSystems.md` — full expansion of SOCIAL-01–05: reaction interaction model, comment interaction model, follow system lifecycle (ADR-0009), map-anchored feed rules, social discovery surfaces, cross-system data contracts, notification triggers, safety controls, implementation checklist
- `planning/product/ProgressionSystems.md` — full expansion of PROG-01–05 and ACH-01–02: streak design, XP sources and level curve, leaderboard design and open ranking-model TBDs, challenge anatomy, anti-abuse controls, achievement categories, data contracts, open TBDs (ranking model, intensity weighting, daily XP cap, streak freeze, following list visibility)
- `planning/product/Monetization.md` — full expansion of MON-01–03: cosmetic economy (item categories, free-tier guarantees, locked item UX), premium subscription definition (what's in/out, lapse behavior), rewarded ad constraints (opt-in only, frequency limits, prohibited surfaces), monetization surface map, pricing philosophy, anti-patterns, hard rules (10 immutable constraints)
- `planning/meta/INDEX.md` — updated tree, read order, and document registry to include new system detail docs

### Notes
- Open TBDs from ProgressionSystems.md: ranking model weights (TBD-1), intensity weighting in XP/rank (TBD-2), daily XP cap value (TBD-3), streak freeze decision (TBD-4), following list visibility (TBD-5). All block Phase 3 implementation.
- Rewarded ad unlock duration (session vs 24h) remains TBD — referenced in both Monetization.md and CREATE-03.2.
- Architecture/execution/skills/roadmap/design folders are still pending authoring.

## 2026-06-20

### Added
- `planning/Planning Audit Report.md` (Phase 1 output)
- `planning/PlanningStructureReview.md` (Phase 2 output)
- `planning/AIOperatingSystemReview.md` (Phase 3 output)
- `planning/PlanningDependencyGraph.md` (Phase 4 output)
- `planning/product/UserMotivations.md`
- `planning/product/UserJourney.md`
- `planning/product/CoreLoops.md`
- `planning/product/Glossary.md`
- `planning/product/SuccessMetrics.md`
- `planning/product/ContentSafety.md`
- `planning/product/Prioritization.md`
- `planning/product/Features.md`
- `planning/meta/INDEX.md`
- `planning/meta/PlanningGlossary.md`
- `planning/meta/ChangeLog.md`
- `planning/meta/decisions/ADR-0001-room-over-sqldelight.md`
- `planning/meta/decisions/ADR-0002-gitlive-firebase-single-sdk.md`
- `planning/meta/decisions/ADR-0003-koin-with-expect-actual-platform-modules.md`
- `planning/meta/decisions/ADR-0004-custom-stateviewmodel-pattern.md`
- `planning/meta/decisions/ADR-0005-type-safe-compose-navigation.md`
- `planning/meta/decisions/ADR-0006-session-driven-navigation.md`
- `planning/meta/decisions/ADR-0007-local-first-read-with-backfill.md`
- `planning/meta/decisions/ADR-0008-expect-actual-platform-boundary-pattern.md`
- `planning/meta/decisions/ADR-0009-follow-only-with-approval.md`
- `planning/meta/decisions/ADR-0010-visibility-tiers-public-followers-private.md`
- `planning/meta/decisions/ADR-0011-territory-out-of-mvp.md`

### Changed
- Moved `planning/Vision.md` -> `planning/product/Vision.md`
- Moved `planning/ProductPillars.md` -> `planning/product/ProductPillars.md`
- Moved `planning/Personas.md` -> `planning/product/Personas.md`
- Added dependency-spine headers to:
  - `planning/product/Vision.md`
  - `planning/product/ProductPillars.md`
  - `planning/product/Personas.md`

### Notes
- iOS Swift LSP diagnostics are known local-toolchain noise and not part of planning scope.
- Architecture/execution/skills/roadmap/design folders are still pending authoring.

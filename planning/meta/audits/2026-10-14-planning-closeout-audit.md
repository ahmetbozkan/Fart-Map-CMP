# Planning Closeout Audit — 2026-10-14

> Mode 1 (Review / Planning Closeout) deliverable. Stages A–E of the handover brief.
> Status: **closed 2026-10-14**. The owner answered §8 and Stage D reconciliation was applied (see `meta/ChangeLog.md`, session 11, and §9 below). This file is evidence, not authority.
> Method: every file under `planning/` was read in full (seven upstream product docs through a structured delegated pass, see §4.7); every Kotlin/Swift source under `composeApp/src` and `iosApp/` was read; build commands were executed, not inferred.

---

## 1) Repository state (facts)

| Item | Verified value |
|---|---|
| Remote / branch | `github.com/ahmetbozkan/FartMap-CMP`, `master`, HEAD `a1c4d46 update ai schema` |
| Working tree | Dirty. Staged: new `execution/` templates, `roadmap/` templates, `skills/`, `design/` files. Unstaged: `planning/CLAUDE.md`, `meta/ChangeLog.md`, `meta/INDEX.md`, `roadmap/STATE.md`. Untracked: `roadmap/{phases,epics,stories,tasks,sprints}/` (`.gitkeep` only). |
| Planning tree | 97 files (92 markdown + 5 `.gitkeep`), 18,774 markdown lines |
| PoC sources | 125 files under `composeApp/src`, 106 Kotlin files (6,191 lines), single Gradle module |
| Toolchain | Gradle 8.14.3, AGP 8.11.2, Kotlin 2.2.20, CMP 1.9.0, Room 2.8.2, Koin 4.1.1, GitLive 2.4.0, Firebase BOM 34.4.0, Firebase iOS SDK 12.6.0 (SPM), minSdk 24, compile/target 36, JVM 17 |
| JDKs on machine | Default `java` = Oracle JDK 20.0.1; Homebrew OpenJDK 17.0.17 (used); Corretto 11. Xcode 26.4.1; iPhone 17 simulators available |
| Firebase project | `google-services.json` and `GoogleService-Info.plist` → `fart-map-fm38`; `.firebaserc` → `bloc-3564f` (mismatch confirmed) |
| `functions/` | Tracked Firebase template (`index.js`, `package.json`, lockfile); no real functions |
| Tracked secrets / config | `composeApp/secrets.properties` (tracked before being gitignored), `composeApp/google-services.json`, `iosApp/GoogleService-Info.plist`, Maps API key literal in `AndroidManifest.xml`, Google OAuth client ID literal in `GoogleAuthProvider.android.kt`. Values were not printed. |
| Other hygiene | `session-ses_11f4.md` (586 KB transcript) tracked at root; `composeApp/schemas/` has two stale schema folders (old package path, v2 json) while `@Database(version = 1)`; Xcode scheme `iosApp` is a user scheme (not shared); `Info.plist` has location usage only (no microphone); `allowBackup="true"` |

## 2) Build verification (JDK 17, 2026-10-14)

| Command | Result |
|---|---|
| `:composeApp:assembleDebug` | **PASS** (`composeApp-debug.apk`, 34 MB) |
| `:composeApp:testDebugUnitTest` | **PASS** (1 test, the template test) |
| `:composeApp:compileKotlinIosSimulatorArm64` + `linkDebugFrameworkIosSimulatorArm64` | **PASS** (`ComposeApp.framework` produced) |
| `:composeApp:iosSimulatorArm64Test` | **FAIL** at `linkDebugTestIosSimulatorArm64`: `ld: framework 'FirebaseCore' not found`. The Kotlin/Native test executable cannot link GitLive Firebase because the Firebase iOS frameworks are only available through the Xcode SPM graph. Shared iOS tests are not runnable as configured. |
| `xcodebuild … -scheme iosApp -destination 'iOS Simulator,name=iPhone 17'` | see §2.1 |
| Lint / detekt | Not configured. Not run. |

Warnings observed: D8 stack-map warnings from `play-services-auth 21.5.0`; Compose Material3 1.9.0 compiled against an older `kotlinx-datetime` (Instant moved to `kotlin.time` in 0.7.x) → "unlinked class symbol" notes; "Cannot infer a bundle ID" for the framework.

### 2.1 Xcode build

`xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 17' CODE_SIGNING_ALLOWED=NO build` (fresh derived data, JDK 17 exported): **FAIL** (exit 65). SPM resolution succeeds (Firebase iOS SDK 12.6.0), the Gradle `embedAndSignAppleFrameworkForXcode` phase runs, then the app link step fails:

```
ld: warning: Could not find or use auto-linked framework 'FirebaseCore': framework 'FirebaseCore' not found
clang: error: linker command failed with exit code 1
```

Probable cause (not fixed, PoC is frozen): the Xcode target links only the `FirebaseCore` SPM product (static library targets), while the Kotlin/Native framework built by GitLive auto-links `-framework FirebaseCore` (and Auth/Firestore/Storage). The same symptom breaks the shared iOS test link. The last PoC commits (2026-09-02) may have been validated on Android only. Consequence for the new project: decide the iOS Firebase linking strategy and the shared-test isolation strategy at bootstrap (see §8 L3).

## 3) PoC implementation state (from source, not docs)

| Area | State | Classification |
|---|---|---|
| Auth | GitLive `AuthRepositoryImpl`: anonymous + Google(idToken) wrapped in `runCatching` (errors swallowed); `signInWithApple()` is `TODO()`. Android Credential Manager provider (catch → `printStackTrace` → null); iOS provider returns `null`. `LoginViewModel` has an empty Apple branch and no error state. | Evolve (provider seam), Reject (error handling) |
| Session | `SessionManager`: `authState.flatMapLatest` → `getOrCreateUser` + `observeUser` → `StateFlow<SessionData>`; `App.kt` navigates on status change with `popUpTo(0)`. | Preserve (session-as-flow), Evolve (typed errors, guest state) |
| Navigation | `@Serializable` routes; `NavGraphs` sealed interface; `MainNavHost` hosts Start/Login/Map+LogFart/Profile only — Social and Rank tabs navigate to unhosted graphs; `StartScreen` empty; bottom bar `selected=false` always. | Preserve (typed routes), Reject (unhosted graphs) |
| Map | Android `GoogleMap` with a `Marker` per fart (snippet exposes precise lat/lng); iOS `MKMapView` shows user location only, ignores markers; map FAB `onFartClick = {}`; filters are UI-only state; stats cards hardcoded. | Evolve (expect/actual map composable), Reject (precise coordinates in UI, parity gap) |
| Log Fart | Date/time, marker icon from Firestore `marker_icons` + Storage, comment (120 max), intensity 0–10 default 5, flavor enum FRESH…LETHAL, placeholder "feeling" list, voice recorder UI only, location label hardcoded, visibility hardcoded PUBLIC, `signatureId` = marker icon id, `feelingId` = random UUID. | Evolve (screen structure), Reject (hardcoded visibility, placeholder ids) |
| Persistence | Room v1: `farts` (no visibility column; `toFart()` always PRIVATE), `marker_icons`; `LocalDateTime` converter in UTC; DataStore wired, `PreferencesManager` empty. | Preserve (Room/KSP setup), Reject (field loss) |
| Sync | `addFart`: local insert → remote `set` → `markAsSynced`; `syncFarts()` second path never called; `getAllFarts` combines the local flow with the entire remote `farts` collection and writes remote into local inside `combine`. | Reject (dual path, unfiltered collection read) |
| Profile | Observes session; stats from `users/{uid}.stats`; mock achievements; hardcoded values; client computes streaks and writes stats (client-trusted). | Evolve (screen), Reject (client-side progression) |
| DI | Koin `sharedModule` + `expect/actual platformModule`; app scope with a logging-only `CoroutineExceptionHandler`. | Preserve |
| ViewModel base | `StateViewModel.performAction` catches → `printStackTrace`; `EventStateViewModel` channel events. | Preserve (shape), Reject (error swallowing) |
| Layering | Layer-first packages; `domain/Fart.kt` imports `FartEntity`; use cases import `data.remote`; UI uses `data.remote.model.User`. | Reject |
| Observability | `AppLogger` expect/actual; analytics dependency unused. | Evolve |
| Monetization | `MarkerIcon` carries `isPremium` / `requiredEntitlement` from Firestore, no gating. | Evolve (catalog idea), decision pending (§8 J) |

## 4) Per-document audit

Classification key: **A** authoritative & complete · **P** incomplete/provisional · **O** outdated/stale sections · **C** contradicts another doc or code · **D** duplicates another doc · **H** historical (not needed in the read chain). "Action" is the proposed Stage D treatment.

### 4.1 Entrypoint and meta

| Doc | Class | Needed | Findings | Action |
|---|---|---|---|---|
| `CLAUDE.md` | A, C | yes | §7 forbids modifying `planning/` during implementation; AgentGuardrails §9, execution/SYSTEM §6/§9, DoD §7, Workflow §7 require STATE/ChangeLog/work-item updates → hard contradiction in operating rules. §1 states PoC-only role. | Narrow §7 to "no product/architecture/security decision changes without approval; state, changelog and work-item updates are mandatory". |
| `meta/INDEX.md` | A, O | yes | §2 read order makes five root process docs mandatory (positions 4–8). §5 accurate. Tree accurate. | Add "Archive / history" section; drop root docs from mandatory chain. |
| `meta/AgentGuardrails.md` | A | yes | Consistent. §7 PoC policy used in §3 above. | Keep. |
| `meta/ChangeLog.md` | A, O | yes | Sessions 1–10 present (session 10 unstaged). Never records `design/DesignSpecification.md`. | Add note on DesignSpecification origin; add closeout entry in Stage D. |
| `meta/PlanningGlossary.md` | A, O | yes | §1 "System → future product/*Systems.md" stale. | One-line fix. |
| `meta/decisions/ADR-0001…0008` | A, O | yes | Source cites `meta/archive/PlanningAuditReport.md` §8 (history doc). ADR-0004 "with hardening required", ADR-0007 "strategy incomplete" — now satisfied by Observability/SyncStrategy. | Mark qualifiers as satisfied; keep sources (archive path). |
| `meta/decisions/ADR-0009…0014` | A | yes | Consistent with product docs. | Keep. |
| `roadmap/STATE.md` | A, O | yes | §1/§6 say changelog close-out pending although ChangeLog session 10 exists; §7.3 requires reading `meta/archive/AIOperatingSystemReview.md` (history). | Update after Stage D; remove history doc from checklist. |

### 4.2 Root process documents

| Doc | Class | Needed | Findings | Action |
|---|---|---|---|---|
| `meta/archive/Planning-Rebuild.md` | H | no | Original brief; recommendations implemented. | Move to `planning/meta/archive/`. |
| `meta/archive/PlanningAuditReport.md` | H | reference | PoC audit; cited by ADR-0001…0008 and ArchitecturePrinciples. Still accurate. | Archive; keep links. |
| `meta/archive/PlanningStructureReview.md` | H | no | Recommended `TestingStrategy.md`, `ConcurrencyAndState.md`, `DesignSystem.md` — not created (intentionally absorbed elsewhere). | Archive. |
| `meta/archive/AIOperatingSystemReview.md` | H | no | P0/P1 items implemented except "skills with canonical examples". | Archive. |
| `meta/archive/PlanningDependencyGraph.md` | H | no | Spine implemented. | Archive. |

### 4.3 Product

| Doc | Class | Needed | Findings | Action |
|---|---|---|---|---|
| `ProductGovernance.md` | A | yes | Top of hierarchy. §6 "location precision default coarse" needs the §8 E decisions to be concrete. | Keep. |
| `ReleaseStrategy.md` | A, C | yes | Sequencing authority. §4 MVP includes NOTIF-01 (matrix says Phase 2), "follower count" on MVP profile (follow graph is Phase 2), Followers default with no follow graph. §5 matrix IDs/names/phases diverge from Features.md for MAP-04/05/06, SHARE-02/03/04, MOD-02/03, PRIV-03, COMMUNITY-02/03, PROFILE-01/04/06, MAP-03. | Reconcile with Features (RS wins on phase; Features owns IDs/names). Resolve §8 C questions. |
| `Features.md` | A, O, C | yes | ID/name/phase drift vs ReleaseStrategy (above); undefined IDs `CONTENT-09`, `SAFE-01`, `METRICS`; stale TBD text in CREATE-03.1 Step 5 and CREATE-05 (resolved 0%); §3 is history; CTA copy "Save Fart" vs design "Let It Rip"; description limit "no hard limit / 280 soft" vs design "max 280"; CREATE-03.2 locked-item rules assume MON-02/03 which are Phase 4 (§8 J). CREATE-03.3 is the authoritative field map and matches DataModel. | Fix IDs, phases, stale TBDs, copy; archive §3. |
| `ContentSafety.md` | A, P | yes | Fuzzing granularity and "no real-time presence" delay unspecified; age gating undecided (§8 E/F). Stale refs to Glossary §11 open decisions. | Record §8 answers; fix refs. |
| `Glossary.md` | A, O | yes | §4 Signature/Intensity "open question" (resolved ADR-0013, 0%); Intensity "1–5" vs 0.0–1.0; §11 register lists resolved items; "Feeling" is Provisional but absent from Features/DataModel; "friends" wording; §1 "future PlanningGlossary". | Mark resolved; decide Feeling = dropped PoC concept. |
| `SocialSystems.md` | A, O | yes | Reaction enum DEAD/TOXIC/RIP/SMELLED/LEGEND (not enumerated in DataContracts); §4 "may need separate Block entity — decide at data layer" stale (DataModel/DataContracts decided); rate limits/cooldowns "TBD (recommend …)" are Phase 2 defaults; §7 counters imply server-side compute. | Enumerate reactions in DataContracts; mark Block decided; label rate limits provisional defaults. |
| `Monetization.md` | A | later phases | Consistent; Phase 4. Entitlement/catalog contract absent from architecture (§8 J). | Keep; add catalog contract when MON-01 is planned. |
| `SuccessMetrics.md` | A, O | yes | "friend" terminology (L4, §4, §7); §10 demands an analytics taxonomy that Observability does not contain. | Editorial; add Phase-1 event list to Observability. |
| `Prioritization.md` | A, O | yes | §3 dependency gate cites "Follow/Friend" TBD (resolved). | One-line fix. |
| `ProgressionSystems.md` | see §4.7 | later phases | — | — |
| `Vision.md`, `ProductPillars.md`, `Personas.md`, `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md` | see §4.7 | yes | — | — |

### 4.4 Architecture

| Doc | Class | Needed | Findings | Action |
|---|---|---|---|---|
| `ArchitecturePrinciples.md` | A, O | yes | §13 says "Fourteen ADRs" but lists eight. | One-line fix. |
| `Architecture.md` | A, O | yes | §8 Flow 1/4 read as "push to Firestore if online, else SyncEngine retries" with an `isSynced` flag → looks like a second write path; SyncStrategy §3 mandates one queue path; DataModel uses `sync_state`. §6 package root `com.fartmap` (identifier undecided, §8 A3). §3 lists FCM/APNs; TechStack does not. | Reword flows to "enqueue + immediate sync tick"; align field names. |
| `SecurityAndSecrets.md` | A, C | yes | §6 "no query returns precise location for non-owned content" vs DataContracts §3.2 storing precise coords on the public `farts` doc (Firestore has no field-level read rules) → hard gap (§8 E1). §4 wording on client config files softer than CLAUDE.md §4. | Record E1 decision; align config-file rule. |
| `TechStack.md` | A, P | yes | Pins PoC "x.y lines" from 2025; no bootstrap-baseline policy for the new project; no Cloud Functions / Messaging / crash reporting / App Check (§8 H). §6 CI minimum is good. | Add bootstrap policy + backend compute decision (ADR). |
| `DataModel.md` | A | yes | Resolves PoC placeholders (avatar_id, flavor_level, intensity_value, visibility_mode). `coarse_location_key` undefined (§8 E2). No catalog/entitlement entities (§8 J). | Define coarse key; add catalog when decided. |
| `DataContracts.md` | A, C | yes | Precise coords on public doc (E1); reaction type enum not listed; server-authoritative counters/XP/leaderboards imply Cloud Functions (H2); no anonymous-read rule intent (D1). | Patch after §8 answers. |
| `RepositoryRules.md` | A | yes | Consistent. | Keep. |
| `SyncStrategy.md` | A | yes | Consistent; single path. | Keep. |
| `FirebaseStrategy.md` | A, P | yes | §2 scope omits Functions/Messaging; §5.2 says anonymous flows "must be modeled" without deciding capability (D1). | Patch after §8. |
| `MultiplatformStrategy.md` | A | yes | Consistent; parity rule applies to PoC map gap. | Keep. |
| `NavigationStrategy.md` | A, P | yes | §4 session states lack a guest/anonymous state (D1). | Add after D1. |
| `Observability.md` | A, P | yes | No event taxonomy (SuccessMetrics §10); no crash-reporting tool named. | Add Phase-1 taxonomy; name crash reporting in TechStack. |

### 4.5 Execution and roadmap

| Doc | Class | Needed | Findings | Action |
|---|---|---|---|---|
| `execution/SYSTEM.md` | A | yes | Decision classes and loop are clear; §3 `links.*` schema vs prose fields in templates (same intent). | Keep. |
| `execution/Workflow.md` | A | yes | Consistent. | Keep. |
| `execution/DefinitionOfReady.md` | A | yes | Consistent. | Keep. |
| `execution/DefinitionOfDone.md` | A | yes | Consistent. Requires STATE/ChangeLog updates (conflicts with CLAUDE.md §7). | Keep; CLAUDE.md changes. |
| `execution/INDEX.md` | A | yes | Consistent. | Keep. |
| `execution/StoryTemplate.md` | A | yes | Consistent. | Keep. |
| `execution/TaskTemplate.md` | A | yes | Consistent. | Keep. |
| `execution/SprintRules.md` | A, D | yes | §7 contains a sprint artifact template that duplicates `roadmap/SprintTemplate.md` with a different shape. | Replace §7 with a reference to `roadmap/SprintTemplate.md`. |
| `execution/ReviewChecklist.md` | A | yes | Consistent. | Keep. |
| `roadmap/SYSTEM.md`, `INDEX.md` | A | yes | Consistent; instance folders empty. | Keep. |
| `roadmap/PhaseTemplate.md`, `EpicTemplate.md`, `SprintTemplate.md` | A | yes | Consistent. | Keep. |

### 4.6 Design and skills

| Doc | Class | Needed | Findings | Action |
|---|---|---|---|---|
| `design/SYSTEM.md` | A, P | yes | Stitch named as source; all artifact ids TBD (§8 K). | Record K1 answer. |
| `design/DesignSpecification.md` | A, C | yes | Not in ChangeLog. §9.4 nav "Map / Feed / Log (FAB) / Notif / Profile" (Feed/Notif are Phase 2) vs §16 "Log Fart is modal, not a tab"; no Phase-1 nav; §11.4 Fart Detail includes comments/follow (Phase 2) and §11.5 Profile shows XP/rank/achievements (Phase 3) — no Phase-1 variants; §11.6 "Precise location" switch conflicts with "precise never displayed" (E4); description max 280 vs Features; CTA "LET IT RIP" vs Features. | Add Phase-1 variants (nav, detail sheet, profile); fix conflicts. |
| `design/UXFlowCatalog.md` | A | yes | Consistent. | Keep. |
| `design/ScreenSpecificationTemplate.md` | A | yes | Consistent. | Keep. |
| `design/ImplementationHandoff.md` | A | yes | Consistent. | Keep. |
| `design/HANDOFF-UXF-01-04-MVP.md` | P | yes | Covers UXF-01/02/03/04/08 with three screen specs; no screen spec for the Fart Detail Sheet (UXF-04, MVP-critical). | Add `screens/FartDetailSheet.md` (Phase-1 variant). |
| `design/screens/AuthLoginScreen.md`, `MapScreen.md`, `LogFartScreen.md` | A, P | yes | Good; Stitch ids TBD. | Keep. |
| `skills/SYSTEM.md` | A | yes | §3 requires an "escalation triggers" section; playbooks fold it into "Failure recovery". | Minor. |
| `skills/INDEX.md` | A, O | yes | §2 "Missing Skill Files" is stale (all 12 exist). | Delete §2. |
| 12 skill playbooks | P | yes | 23–25 lines each: purpose, sources, procedure, recovery, validation. Usable as checklists; no code-shaped canonical examples (AIOperatingSystemReview P1 #6 unmet). | Keep thin; add canonical examples from the first real implementation, not invented now. |

### 4.7 Upstream product docs (structured pass; cited lines spot-checked against source)

| Doc | Class | Needed | Findings | Action |
|---|---|---|---|---|
| `Vision.md` | A, O | yes | Predates ADR-0009/0011: "friends" (lines 75, 85, 113, 136, 164) and "territory" as planned (77, 114, 139); §6 un-numbered loop list duplicates CoreLoops L1–L8. §2/§5/§13/§17 are cited everywhere and intact. | Wording fixes; label territory as Phase 5 TBD; point §6 at CoreLoops. |
| `ProductPillars.md` | A, C | yes | §9 Phase 2 focus lists "Friends" (366) next to "Following"; Phase 3 focus lists "Territory" (382) — conflicts with ADR-0011; §10 stage weights (Early-MVP L35/S25/H20/P10/I10) differ from ReleaseStrategy §3 (L35/H30/S20/I10/P5); four-phase naming, no Phase 5. | Remove Friends/Territory; align weights/phase names with ReleaseStrategy. |
| `Personas.md` | A, C | yes | Six personas (adds Community Organizer, Future tier) vs five in ProductGovernance §9; "friend graph" wording (122, 138, 164–169, 240); territory as persona favourite (197, 253). | Add Community Organizer to Governance §9 as Future-tier; wording fixes. |
| `UserMotivations.md` | A, O | yes | "future SuccessMetrics.md" (243); friend wording (72–80). | One-line fixes. |
| `UserJourney.md` | A, O | yes | "future doc" wording (180, 231); friend wording (108–113, 186, 199); Stage 5 cites stats (Phase 3) while stage is Phase 2; canonical session includes "comment" (Phase 2). | Fix refs and phase notes. |
| `CoreLoops.md` | A, O | yes | Canonical L1–L8 registry. Defects: §3 diagram labels "(Territory / Seasons)" (55); L4 "add friends" (126); L2 has leak points but **no health line** although ReleaseStrategy §6 cites "L2 health"; stale authorization rules ("last document authorized", "do not proceed to Features.md") at 5 and 282. | Add L2 health line; remove territory label; delete stale rules. |
| `ProgressionSystems.md` | A, C | Phase 2–3 | Internally inconsistent: line 147 "Daily XP cap: TBD — soft 150" vs §10 "150 hard cap, server-side"; line 82 "no streak freeze… may be reconsidered in Phase 3" vs §10 "No streak freeze. Ever."; line 250 "XP-based or composite — TBD" vs §9 composite; TOC anchor (27) points at a renamed §10; header claims PROG-01–06 but PROG-06 absent (correct per ADR-0011). "Friends" leaderboard (224–277, `FRIENDS_WEEKLY` 476) names a relation that does not exist; direction ambiguous. Server-side computation stated for rate limits, leaderboard rank, XP cap, achievements; streak/XP award computation location and per-user timezone handling unspecified. Engineering values present: XP table, 150/day cap, 20 farts/day soft, 100 reactions/day, 50 comments/day, ~50 km local radius, board refresh 1h/15min, 7-day account age for boards. | Fix the four inline contradictions; rename board to Followers (define direction); note server-side computation gaps for the Phase 3 architecture pass. |

No direct-messaging leakage and no non-canonical visibility tiers were found in these seven docs. Loop numbering is consistent across them.

## 5) Contradictions and gaps (classified per AgentGuardrails §6)

| # | Issue | Class | Resolution path |
|---|---|---|---|
| 1 | CLAUDE.md §7 freeze vs mandatory state updates | hard contradiction (operating rules) | Editorial narrowing of CLAUDE.md §7 (no decision change) |
| 2 | Precise coordinates on the public `farts` document vs "never returned for non-owned content" | hard gap (privacy posture) | Owner decision §8 E1, then DataContracts/DataModel/SecurityAndSecrets patch |
| 3 | Followers default in Phase 1 without a follow graph | unresolved product semantics | Owner decision §8 C1 |
| 4 | Anonymous/guest capability undefined (read-only onboarding vs PoC anonymous create) | unresolved decision | Owner decision §8 D1; NavigationStrategy/FirebaseStrategy/DataContracts patch |
| 5 | Backend compute (Cloud Functions), Messaging, crash reporting absent from TechStack while contracts require server authority | unresolved stack decision | Owner decision §8 H2; ADR-0015 |
| 6 | Features.md vs ReleaseStrategy IDs/names/phases | stale doc | Reconcile (RS wins on phase; Features owns IDs) |
| 7 | NOTIF-01 in MVP vs Phase 2 | unresolved scope | Owner decision §8 C2 |
| 8 | CREATE-03.2 locked-item sheets require MON-02/03 (Phase 4) in Phase 1 | unresolved product rule | Owner decision §8 J1 |
| 9 | Architecture Flow 1/4 wording vs SyncStrategy single path | stale wording | Editorial |
| 10 | SprintRules §7 duplicate sprint template | duplication | Editorial |
| 11 | Stale TBD/"future" text in Glossary, Features, Prioritization, PlanningGlossary, skills/INDEX, STATE | stale doc | Editorial |
| 12 | Root process docs mandatory in read chain | stale navigation | Archive section |
| 13 | DesignSpecification Phase-1 variants missing; Fart Detail screen spec missing | incomplete | Design additions (authorized scope) |
| 14 | TechStack frozen to 2025 lines | incomplete policy | Add bootstrap policy |
| 15 | Age gating / fuzzing granularity / real-time delay undecided | unresolved (safety) | Owner decisions §8 E2, E5, F1 |
| 16 | Shared iOS tests cannot link Firebase | engineering fact | New project: test configuration decision (link Firebase xcframeworks for tests or isolate Firebase behind interfaces so shared tests need no Firebase) |

## 6) Missing requirements or engineering rules

1. Backend code location and deployment flow (rules, indexes, functions) for the new project.
2. Firebase environment model in practice (which projects exist, who creates them).
3. Guest/anonymous session state and capability.
4. Precise-vs-public location storage split and the coarse key definition.
5. Phase-1 cosmetic catalog source and entitlement placeholder fields.
6. Phase-1 analytics event taxonomy (activation funnel + counter-metrics).
7. New-project dependency bootstrap policy and JDK baseline.
8. CI provider and the iOS-on-CI decision.
9. Test strategy for shared iOS targets given the Firebase link constraint.
10. Minimum age / age-assurance for Phase 1.

## 7) Planning completion assessment (Stage E, final)

§5 items 1–8 were closed on 2026-10-14 (see §9). The system now supports scope clarification, phase/epic/story/task/sprint planning with the existing templates, design-driven implementation, autonomous code changes, build and test validation, debugging and recovery, progress tracking, documentation maintenance and safe handling of product, architecture and security decisions. **Documentation expansion stops here.** The only remaining additions are the new repository's entrypoint README with verified commands (STORY-01-01-01) and canonical code examples in the skills once real code exists. Roadmap instances are created one epic at a time (`roadmap/STATE.md` §6).

## 8) Questions for the owner (Stage C)

Recommendation first, then the main alternative. "Affects" names the owner documents that change once answered. Answers are recorded in those documents during Stage D; this section then becomes the decision log.

### A — Repository role and home of the new project (blocking)
- **A1 Repository role.** Confirm: this repo stays a PoC/reference; the product is built from scratch. Recommend a **new, separate repository** with clean history; `planning/` is copied into it at bootstrap and this repo's copy is frozen with a pointer; this repo is archived read-only afterwards. Alternative: an orphan branch in this repo (history still carries secrets; two codebases confuse agents). Affects CLAUDE.md §1, AgentGuardrails §7, STATE, ChangeLog.
- **A2 Identifiers.** Package root (`com.fartmap` per Architecture §6), Android applicationId, iOS bundle id, display name. Recommend `com.fartmap`; you supply the ids. Affects Architecture §6, TechStack.
- **A3 Firebase projects.** Recommend new `dev` and `prod` projects for the new app (FirebaseStrategy §8); `fart-map-fm38` stays with the PoC. Alternative: reuse `fart-map-fm38` as dev. You create them.

### B — Committed secrets incident (blocking)
- **B1 Rotation and history.** Recommend rotating the Google Maps Android key now (restrict the replacement by package + SHA-1) and restricting the Firebase API keys; keep git history as is because the PoC is retired, unless the repository is or will be public, in which case purge history. Is the repository public?
- **B2 PoC working tree.** Recommend untracking `secrets.properties`, `google-services.json`, `GoogleService-Info.plist` (`git rm --cached`, local copies kept) in your next commit. Alternative: leave the PoC untouched.
- **B3 New-project config policy.** Confirm the absolute rule: `google-services.json` / `GoogleService-Info.plist` are never committed; templates are committed; CI and local setup inject them. Affects SecurityAndSecrets §4 wording.

### C — MVP semantics and first slice
- **C1 Followers default without a follow graph (Phase 1).** Recommend **keep ADR-0010** (Followers default); the Phase 1 map shows Public posts plus the user's own; follower count hidden until Phase 2; accept that first-social-touch depends on Public posts. Alternative: pull SOCIAL-03 (follow + approval) into Phase 1 (scope move, roughly one extra epic). Affects ReleaseStrategy §4, Features PRIV-01, DesignSpec 11.5.
- **C2 Reaction notification in MVP.** Recommend **in-app only** (unread indicator on next open); push notifications arrive with NOTIF-01 in Phase 2. Alternative: push in Phase 1 (FCM/APNs, permission prompt, a Cloud Function trigger). Affects ReleaseStrategy §4, Features NOTIF-01.
- **C3 Phase 1 cosmetics and entitlement scaffolding.** CREATE-03.2 requires "Go Premium / Watch Ad" sheets but MON-02/03 are Phase 4. Recommend Phase 1 ships only the free set (2 avatars, 3 sounds) bundled in the app; locked items stay visible with a "Coming soon" sheet; no IAP or ad SDK; `premium_status` and `owned_cosmetic_ids` reserved as optional fields. This is a Phase-1-only exception to CREATE-03.2 rule 1 and needs your OK. Alternative: hide locked items in Phase 1 (contradicts Monetization §3). Affects Features CREATE-03.1/03.2, DataContracts.
- **C4 Small scope clarifications (I apply unless you object):** delete own fart in Phase 1, edit in Phase 2 (CREATE-09); Phase 1 map filters = Mine / Everyone (MAP-03); CTA copy "Let It Rip"; description soft limit 280 with counter; follower count hidden in Phase 1.

### D — Authentication and guest mode
- **D1 Guest mode.** Recommend guest = Firebase anonymous auth, **read-only** (map and marker details); logging, reacting and reporting require linking Google or Apple, and linking preserves the uid. Alternative: anonymous users can create content (PoC behaviour; orphaned content and abuse risk). Affects NavigationStrategy §4 (Guest state), FirebaseStrategy §5, DataContracts §5, Features onboarding.
- **D2 Providers.** Recommend Google + Apple only at MVP (Apple is required once Google is offered); no email/password. Confirm.

### E — Location privacy (blocking for data contracts)
- **E1 Storage split.** Recommend the public `farts/{id}` document carries only fuzzed `public_lat/lng` and `coarse_location_key`; precise coordinates live in an owner-only `farts/{id}/private/location` document. Alternative: keep precise coordinates on the public document (contradicts SecurityAndSecrets §6). Affects DataContracts §3.2/§5/§6, DataModel §4.1, SecurityAndSecrets §3.
- **E2 Fuzzing granularity.** Recommend geohash precision 6 cells (about 1.2 km × 0.6 km) with deterministic in-cell jitter; precise coordinates retained only on the user's own farts; no location-history collection in Phase 1. Alternative: precision 7 (about 150 m) for more "fun" at higher risk. Affects ContentSafety §3, DataModel.
- **E3 "Precise location" settings toggle (DesignSpec 11.6).** Recommend removing it (others never see precise; the owner always sees their own exact pin). Alternative: define it as an owner-only precision display toggle.
- **E4 Real-time presence.** Recommend no publication delay; public views show relative time at hour granularity and no "live now" indicator in Phase 1. Alternative: 15–60 minute delay for public markers. Affects ContentSafety §3.

### F — Moderation and minor-safety baseline (Phase 1)
- **F1 Minimum age.** Recommend 13+ via self-declared birth year at sign-up, under-13 blocked, storing only an age band (not the date of birth). Options: 13 / 16 / 18. This is a ContentSafety §5 escalation item; your call. Affects ContentSafety §5, DataContracts users doc, Features onboarding.
- **F2 Report handling.** Recommend reports write to `moderation_reports`; you review manually in the Firebase console in Phase 1; content auto-hides after 3 distinct reports pending review. Alternative: no auto-hide.
- **F3 Rate limits.** Recommend no server-enforced limits in Phase 1 beyond a soft client limit (20 farts/day) and monitoring; ProgressionSystems limits arrive with Phase 3 backend work. Confirm.

### H — Backend code, rules, functions, environments (blocking for the stack)
- **H1 Backend home.** Recommend a `backend/` folder in the new repo (`firestore.rules`, `firestore.indexes.json`, `functions/`), deployed by the Firebase CLI from CI; remove `functions/`, `.firebaserc`, `firebase.json` from the PoC. Alternative: separate backend repo.
- **H2 Cloud Functions from Phase 1.** Recommend adopting Cloud Functions (2nd gen, TypeScript) now for denormalized reaction counters and report auto-hide, later XP/leaderboards; record as ADR-0015 and add to TechStack/FirebaseStrategy. Alternative: no backend compute in Phase 1 (client transactions guarded by rules; weaker integrity).
- **H3 App Check.** Recommend enabled from Phase 1 (Play Integrity / App Attest, debug providers in dev). Alternative: Phase 2.
- **H4 Crash reporting.** Recommend Firebase Crashlytics (GitLive has a module) plus Firebase Analytics from day one; add both to TechStack. Alternative: analytics only.

### I — Platform parity and maps
- **I1 Map SDKs.** Recommend keeping MapKit on iOS and Google Maps on Android behind one expect/actual map contract with marker parity (PoC pattern, fixed). Alternative: Google Maps SDK on both (consistent styling, needs an iOS Maps key).

### K — Design source and readiness
- **K1 Stitch or Figma.** design/SYSTEM names Stitch; a Figma connector is available in this session. Which do you use? Recommend naming the real tool and defining "design ready" for the first slice as DesignSpecification tokens + screen spec + handoff record, with visual exports attached when available rather than blocking.
- **K2 Phase 1 navigation.** Recommend bottom nav Map + Profile with the Log FAB; Feed/Notifications tabs are added in Phase 2. I will write the Phase 1 variants (navigation, Fart Detail Sheet, Profile) in Stage D.

### L — Toolchain, CI and validation for the new project
- **L1 Dependency baseline.** Recommend bootstrapping on the latest stable, mutually compatible set verified by building both targets and running tests on day one; exact versions recorded in TechStack as "baseline (date)"; JDK 21 LTS if supported at bootstrap, else 17. No decision needed unless you want pins.
- **L2 CI.** Recommend GitHub Actions: shared unit tests, Android `assembleDebug`, iOS framework link and an `xcodebuild` simulator build on a macOS runner per PR. macOS minutes cost money; confirm iOS-on-CI from day one. Alternative: Android-only CI, iOS checked locally.
- **L3 Shared tests on iOS.** Recommend keeping Firebase behind interfaces so shared tests run without it (the architecture wants this anyway); alternative: link Firebase xcframeworks into the test binary. I decide unless you object.
- **L4 Device checks.** Which Android emulator/API level do you use? I will use the iPhone 17 simulator and the available emulator.

### N — PoC repository hygiene (destructive actions need your approval)
- **N1** Remove `session-ses_11f4.md` (`git rm`; stays in history). Recommend remove. Alternative: move to `planning/meta/archive/`.
- **N2** Move the five root process documents to `planning/meta/archive/` and demote them in INDEX. Recommend.
- **N3** Remove `functions/`, `.firebaserc`, `firebase.json` from the PoC. Recommend remove; alternative: fix `.firebaserc` to `fart-map-fm38`.
- **N4** Commit the staged planning milestone yourself (I do not commit). Suggest one commit for the milestone, a second for Stage D.
- **N5** Keep this audit file under `planning/meta/audits/` and commit it with Stage D. Recommend.

### Editorial fixes I will make in Stage D unless you object (no decision changes)
CLAUDE.md §7 narrowing; SprintRules §7 → reference `roadmap/SprintTemplate.md`; skills/INDEX §2 removal; STATE close-out and read-chain cleanup; INDEX archive section; Architecture Flow 1/4 wording; ADR-0004/0007 qualifiers; ArchitecturePrinciples §13 count; stale TBD text (Glossary §4/§11, Features CREATE-03.1 Step 5 and CREATE-05, Prioritization §3, PlanningGlossary §1, ProgressionSystems lines 27/82/147/250, CoreLoops lines 5/282); "friend" → follow/follower wording across Vision, Pillars, Personas, Motivations, Journey, CoreLoops, SuccessMetrics, ProgressionSystems ("Friends" board → "Followers" with direction defined); Territory removed from Pillars §9 Phase 3 and the CoreLoops §3 label per ADR-0011; Pillars §10 weights and phase names aligned to ReleaseStrategy §3; Community Organizer added to ProductGovernance §9 as Future tier; L2 health line in CoreLoops; Features ↔ ReleaseStrategy ID/name/phase reconciliation (ReleaseStrategy wins on phase, listed in ChangeLog); reaction enum in DataContracts; Block note in SocialSystems; TechStack bootstrap policy; SecurityAndSecrets config-file wording; DesignSpec Phase 1 variants and `screens/FartDetailSheet.md`; Phase 1 analytics event list in Observability; Glossary "Feeling" marked as a dropped PoC concept.

---

## 9) Decision log (owner answers of 2026-10-14 and how they were recorded)

| Item | Owner answer | Recorded in |
|---|---|---|
| A1 | New repository, owner-created; planning copied over afterwards | CLAUDE.md §1, STATE §4 |
| A2 | `com.fartmap`; ids supplied by owner at bootstrap | TechStack, PHASE-01 entry gate |
| A3 | New Firebase projects (`dev`, `prod`) | ADR-0015, FirebaseStrategy §8 |
| B1 | PoC retired; new Firebase project and keys | STATE §4; no history purge |
| B2 | Untrack config files | done (`git rm --cached`), `.gitignore` extended |
| B3 | Config files never committed | SecurityAndSecrets §4, FirebaseStrategy §8 |
| C1 | Keep Followers default; follower count hidden | ReleaseStrategy §4, ADR-0020 |
| C2 | Reaction notification **included** (push) | ADR-0020, ReleaseStrategy §4/§5, FirebaseStrategy §6b |
| C3 | Locked items unlockable via rewarded ads (one time) and purchases (permanent) in Phase 1 | ADR-0016, ADR-0017, ADR-0020, Monetization, Features CREATE-03.2. **Interpretation:** "reactions" read as the locked cosmetics (avatars, sounds); the five reactions stay free. **Confirmed by the owner 2026-10-15.** |
| C4 | All three filters; edit Phase 2 (delete Phase 1 accepted) | ADR-0020, Features MAP-03/CREATE-09 |
| D1 | Anonymous users can create content | ADR-0018, NavigationStrategy §4, DataContracts §5.1 |
| D2 | Google, Apple, Anonymous only | ADR-0018, FirebaseStrategy §5 |
| E1–E4 | Not answered | ADR-0019 applied as the architecture necessity (split); precision 6 and no delay recorded as provisional defaults in ContentSafety §3; settings toggle removed. **Confirmed by the owner 2026-10-15** (geohash precision 6, no delay). |
| F1 | 13+ | ContentSafety §5, DataContracts users.age_band |
| F2 | Auto-hide at 3 reports | DataContracts §3.2/§5.9, ADR-0015 |
| F3 | No server rate limits in Phase 1 | STATE §4; Features MOD-03 "basic" |
| H1 | Backend in new repo; nothing removed from the PoC | ADR-0015 (functions/.firebaserc left in place) |
| H2–H4 | Cloud Functions, App Check, Crashlytics + Analytics | ADR-0015, TechStack §2, FirebaseStrategy §2/§6 |
| I1 | MapKit + Google Maps via expect/actual | TechStack §2/§3, MultiplatformStrategy |
| K1 | Stitch, screen by screen; Figma import possible later | design/SYSTEM §4 |
| K2 | Four tabs: Map, Social, Rank, Profile | ADR-0020, DesignSpecification §9.4/§11.9/§11.10, NavigationStrategy §4. **Phase 1 tab content** (Social = nearby public list; Rank = own stats + teaser) was the agent's proposal; **confirmed by the owner 2026-10-15.** |
| L1–L4 | Recommended options | TechStack §3/§4/§6 |
| N1–N5 | Recommended options, except N3 overridden by H1 | transcript removed; root docs archived; audit file kept |
| "Every other necessary change" | Applied as the editorial list in §8 plus the reconciliation recorded in `meta/ChangeLog.md` session 11 | |

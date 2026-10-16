# Planning Audit Report — Fart Map

> Phase 1 deliverable of `meta/archive/Planning-Rebuild.md`.
> Purpose: audit the PoC repository and existing planning artifacts to determine what is useful, obsolete, duplicated, or missing — as input for building the AI planning system that a brand-new Compose Multiplatform project will use.

---

## 1) Audit Scope & Method

Audited:
- Source code under `composeApp/src` (commonMain, androidMain, iosMain).
- Build configuration (`gradle/libs.versions.toml`, `build.gradle.kts`, plugins).
- iOS host project (`iosApp/`).
- Existing planning artifacts under `planning/`.

Method: static inspection of architecture, data flow, navigation, DI, persistence, sync, and platform boundaries. No code was modified.

---

## 2) Reality Check vs. the Rebuild Brief

The brief (`meta/archive/Planning-Rebuild.md`) describes existing planning state as:
- Completed: Vision.md, ProductPillars.md, Personas.md
- Partially completed: Architecture.md, Workflow.md, SYSTEM.md, Skills
- Outdated: Epics, Stories, Tasks, Sprint Structure

**Audit finding:** Only the three completed product documents and the rebuild brief exist in `planning/`. None of the "partially completed" or "outdated" artifacts exist in the repository.

| Brief claims | Actual state |
|---|---|
| Vision.md | ✅ Exists, strong |
| ProductPillars.md | ✅ Exists, strong |
| Personas.md | ✅ Exists, strong |
| Architecture.md | ❌ Does not exist |
| Workflow.md | ❌ Does not exist |
| SYSTEM.md | ❌ Does not exist |
| Skills (repo-local) | ❌ Not in repo (only user-global `~/.agents/skills` unrelated to Fart Map) |
| Epics / Stories / Tasks / Sprints | ❌ Do not exist |

Implication: this is a **greenfield planning build**, not a cleanup of stale planning. The "architecture decisions" to extract live in the **code**, not in planning docs. This audit therefore focuses on extracting intent from the PoC source.

---

## 3) Repository Architecture Snapshot (Extracted Intent)

### 3.1 Tech stack actually in use
- Kotlin `2.2.20`, Compose Multiplatform `1.9.0`, AGP `8.11.2`.
- Targets: Android + iOS (no desktop/web despite README boilerplate mention).
- DI: Koin `4.1.1` (`viewModelOf`, named scopes).
- Persistence: **Room** `2.8.2` (KMP) + DataStore `1.1.7` for preferences.
- Backend: **GitLive Firebase SDK** `2.4.0` (auth, firestore, storage, analytics).
- Maps: Google Maps Compose `6.12.2` (Android); iOS map is a custom UIKit interop.
- Location/permissions: MOKO permissions `0.20.1`, MOKO geo `0.8.0`.
- Images: Coil 3 `3.3.0` + Ktor `3.3.3`.
- Navigation: Compose Navigation `2.9.0` with type-safe serializable routes.

This validates the brief's Technology Vision, and resolves the open "Room or SQLDelight" question in favor of **Room** (already integrated and working across KMP).

### 3.2 Architecture pattern observed
- MVVM via custom `StateViewModel<State, Action>` base (`ui/util/base/StateViewModel.kt`) and an `EventStateViewModel` variant.
- Repository pattern: `FartRepository`, `AuthRepository`, `UserRepository` (interface + Impl).
- Use cases present but inconsistent (some real, some empty placeholders).
- `SessionManager` derives `SessionData` from `authState` and drives top-level navigation.
- Offline-first is **partially** implemented for farts (local write + remote write + remote→local backfill).

---

## 4) What Is Useful (Extract & Carry Forward)

| Asset | Why it's valuable | Carry-forward action |
|---|---|---|
| `StateViewModel` / `EventStateViewModel` base classes | Clean, minimal MVVM contract (State + Action + Event) | Document as the canonical presentation pattern; fix error handling |
| Type-safe navigation (`navigation/destinations/*`, serializable routes) | Modern, refactor-safe nav | Codify in NavigationStrategy.md |
| Koin module structure (`sharedModule` + `expect/actual platformModule`) | Working KMP DI seam | Codify in koin-di skill |
| `SessionManager` session-as-flow pattern | Elegant auth→session→nav derivation | Keep; document as session strategy |
| Repository interfaces | Correct seam for testability | Keep interfaces; fix Impl hidden deps |
| Room + DAO + entity + converters setup | Proven KMP local persistence | Codify in data-layer skill / DataModel.md |
| GitLive Firebase integration (auth, firestore, storage) | Validates backend choice | Codify in FirebaseStrategy.md |
| Design system primitives (`ui/ds/*`: buttons, text, slider, pickers, scaffold) | Reusable, consistent UI kit | Codify in DesignSystem.md |
| Domain models (`Fart`, `User`, `UserStats`, `FartVisibility`) | Captures product data intent | Use as seed for DataModel.md (after cleanup) |
| String resources (`strings.xml`) | Reveals product tone & feature intent (streak, rank, flavor, symphony) | Mine for Features.md later |

---

## 5) What Is Obsolete / Risky (Do NOT Carry Forward As-Is)

| Issue | Location | Risk | Recommendation |
|---|---|---|---|
| Committed Google Maps API key | `composeApp/secrets.properties`, `AndroidManifest.xml` | Secret leak | New project: secrets via untracked config + CI injection; document in FirebaseStrategy/TechStack |
| `GoogleService-Info.plist` committed | `iosApp/` | Config/secret in VCS | Decide policy explicitly; keep out or document |
| Apple sign-in unimplemented | `AuthRepository` (`TODO`) | Broken auth path on iOS | Track as known gap, not a pattern to copy |
| iOS Google sign-in returns null | `GoogleAuthProvider.ios.kt` | Broken auth path | Same |
| iOS map renders no markers | `MapView.ios.kt` | Platform parity gap | Document parity requirement in MultiplatformStrategy |
| `firebase-analytics` dependency unused | `libs.versions.toml` | Dead dependency | Add intentionally with an analytics plan or drop |
| `GetUserUseCase` empty placeholder | `domain/GetUserUseCase.kt` | Misleading scaffolding | Don't replicate empty use cases |
| Social/Rank nav graphs declared but no host | `NavGraphs.kt` vs `MainNavHost.kt` | Runtime/nav inconsistency | Don't copy; design nav holistically |
| Map "fart" FAB wired to `{}` | `MapScreen.kt` | Dead interaction | Reflects unfinished core loop, not a model |
| `secrets.properties` ignored yet present | `.gitignore` vs working tree | Inconsistent secret hygiene | Establish clean policy in new repo |

---

## 6) What Is Duplicated / Inconsistent

| Duplication / inconsistency | Detail | Resolution |
|---|---|---|
| Two offline-write paths for farts | `addFart()` writes remote inline; `syncFarts()` re-pushes unsynced | Define a single Sync strategy (one writer + reconciler) in SyncStrategy.md |
| Domain → data dependency leak | `domain/Fart.kt` imports `data.local.db.model.FartEntity` (`toFartEntity`) | Enforce dependency direction in RepositoryRules.md (mapping lives in data layer) |
| `User` model in `data/remote/model` used directly by UI | Couples UI to data DTO | Define domain model + UI mapping boundary |
| Mixed date/time helpers | `ui/util/DateUtils`, `ui/util/FormatUtils`, `util/DateExt`, `util/DateExt` | Consolidate date/time utilities |
| `visibility` field not persisted | `FartEntity` mapping drops `FartVisibility` (always PRIVATE) | Data model must round-trip all domain fields |
| Placeholder identifiers | `signatureId`, `feelingId`, `flavorId` marked `// tbd` | Resolve in DataModel.md before implementation |
| Swallowed errors | `StateViewModel.performAction` catches → `printStackTrace()` | Define error-handling contract (error state + logging) |

---

## 7) What Is Missing (Gaps to Fill in Planning)

### Product gaps
- No `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md`, `Features.md`.
- No definition of social, progression, or monetization systems.
- No roadmap / phase breakdown as artifacts (phases exist only as prose in ProductPillars.md).
- No glossary (terms like "fart", "signature", "flavor", "intensity", "territory" are undefined/ambiguous).

### Architecture gaps
- No written architecture, data model, repository rules, sync strategy, Firebase strategy, multiplatform strategy, navigation strategy, design system, or testing strategy.
- No error-handling, logging, or `Result`/failure modeling convention.
- No offline-first contract (pending operations, retry, conflict policy) — code has only a partial ad-hoc version.
- No Firestore data schema / security model documentation.

### Execution / AI-OS gaps
- No `SYSTEM.md` (agent operating contract), `Workflow.md`, `DefinitionOfDone.md`, templates, or sprint rules.
- No repo-local skills.
- No `CLAUDE.md` / agent entrypoint.

### Testing gaps
- Only the default `ComposeAppCommonTest.kt` stub exists. No domain/repository/sync tests, no test strategy.

---

## 8) Extracted Architecture Decisions (Implicit → Make Explicit)

These were discovered in code and should become explicit governance:

1. **Room over SQLDelight** — decided implicitly; honor it.
2. **GitLive Firebase** as the single backend SDK across platforms.
3. **Koin** for DI with `expect/actual` platform modules.
4. **Custom MVVM base** (`StateViewModel`) instead of a framework — keep, but harden.
5. **Type-safe Compose Navigation** with serializable destinations + nested graphs.
6. **Session-driven navigation** (auth state → session → start destination).
7. **Local-first reads** via `combine(localFlow, remoteSnapshots)` then backfill local.
8. **`expect/actual` for platform features**: maps, location, auth provider, logger, datastore, DB builder, string formatting.

---

## 9) Risk Summary

| Severity | Risk | Mitigated by (future doc) |
|---|---|---|
| High | Secrets committed to VCS | TechStack.md / FirebaseStrategy.md (secret policy) |
| High | Ad-hoc sync without retry/conflict contract | SyncStrategy.md |
| High | Domain depends on data layer | RepositoryRules.md / Architecture.md |
| Medium | iOS/Android feature parity gaps | MultiplatformStrategy.md |
| Medium | Swallowed errors, no error model | error-handling skill / Architecture.md |
| Medium | Placeholder data fields (`tbd`) | DataModel.md / Glossary.md |
| Low | Dead dependency (analytics) | TechStack.md |

---

## 10) Audit Conclusion

The PoC is a **valuable reference, not a foundation**. It validates the entire technology vision and yields a set of proven patterns (MVVM base, type-safe nav, Koin seams, Room+Firebase, session-as-flow) worth codifying. However, its sync layer, error handling, layer boundaries, platform parity, and secret hygiene are **not** production patterns and must be redesigned in the planning system rather than copied.

Most importantly: the planning system is effectively **greenfield**. Three strong product docs exist; everything in `architecture/`, `execution/`, `skills/`, `roadmap/`, and `design/` must be created. Phases 2–5 proceed on that basis.

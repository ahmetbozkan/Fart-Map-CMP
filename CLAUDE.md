# Fart Map

A location-based social entertainment app: users log fart moments on a shared map, react to each other, and build a funny identity. Kotlin/Compose Multiplatform for Android and iOS, with Firebase as the backend. **The app exists to make money:** cosmetic purchases, opt-in rewarded ads, a subscription later. Growth and store presence matter as much as code. This repo is the product; the PoC repo is frozen reference only.

## Start here
1. `planning/STATE.md` (the SessionStart hook prints its "Now" block) → the story in `planning/stories/` → only the spec the story needs (`planning/README.md` is the map).
2. The path-scoped rules in `.claude/rules/` load automatically when you touch matching files. The skills in `.claude/skills/` hold the procedures (`/implement-story`, `/verify`, `new-feature`, `firestore-change`, …).
3. Don't read `planning/archive/`. It is history, not authority.

## Modules (ADR-0021)
| Module | Contents |
|---|---|
| `shared` | Compose UI, ViewModels, domain, repository interfaces. **No Firebase types**; tests run without Firebase |
| `firebaseData` | GitLive Firebase implementations of the `shared` contracts (the only Kotlin module with Firebase) |
| `umbrella` | iOS only; builds the static `Shared` framework plus the iOS Koin composition |
| `androidApp` | Shell; flavors `dev` / `prod` |
| `iosApp` | SwiftUI shell; `Debug-dev` / `Release-dev` / `Debug-prod` / `Release-prod`; schemes `FartMap Dev` / `FartMap Prod` |
| `backend/` | Firestore/Storage rules, indexes, Cloud Functions (TypeScript, Node 22) |

Package root: `com.ahmetbozkan.fartmap`, with `feature/<name>/{presentation,domain,data}` and `core/{domain,data,presentation,navigation,design,di,platform,observability}`.

## Commands
The owner works on macOS and Windows. Use bash (Git Bash on Windows) for scripts; in PowerShell use `gradlew.bat` in place of `./gradlew`.
```bash
scripts/verify.sh            # picks checks from what changed; --full runs everything. iOS only on macOS
./gradlew :androidApp:assembleDevDebug :androidApp:assembleProdDebug :androidApp:lintDevDebug
./gradlew :shared:testAndroidHostTest      # fast shared tests (any OS); :shared:allTests adds iOS sim (macOS)
./gradlew detekt                           # static analysis; Konsist architecture tests run in testAndroidHostTest
./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64                                         # macOS
xcodebuild -project iosApp/iosApp.xcodeproj -scheme "FartMap Dev" -destination 'platform=iOS Simulator,name=iPhone 17' build   # macOS
(cd backend/functions && npm ci && npm run build && npm run lint && npm test)  # Java 21 on PATH for emulators
scripts/check-no-secrets.sh
```
- **iOS on Windows:** can't be built locally; the macOS CI job on the draft PR is the iOS check.
- **Firebase configs:** injected, never committed (`scripts/inject-config.sh`, see README).

## Work loop (details: `planning/WorkingAgreement.md`)
1. Use plan mode for anything non-trivial.
2. Branch `NN-short-name` from `main`.
3. Implement, then make `scripts/verify.sh` green.
4. Tick the story, add evidence, update `planning/STATE.md`.
5. Commit, push the branch, open a **draft** PR with `gh pr create --draft`.
6. **Never** push to `main`, force-push, or skip hooks. In an interactive session the owner merges. Under autopilot (`scripts/autopilot.sh` → `/autopilot-story`) the agent merges only with `gh pr merge <n> --squash --match-head-commit <sha>` after review, and the guard hook refuses it unless GitHub reports all required checks green on that sha.
7. Keep a decision you'd change as a **Proposed** ADR (`adr` skill); don't apply it.
8. Autopilot retries a technical failure at most twice, each time with a different approach, then escalates through a `needs-owner` GitHub issue. Owner decisions (see below) escalate at once.

## Non-negotiables
- **No secrets in git.** `google-services.json`, `GoogleService-Info*.plist` and `secrets.properties` are never tracked, read or printed; hooks block them.
- **No swallowed errors.** Every catch becomes a typed State/Event or a structured log. No `printStackTrace`, no empty catch.
- **Layering.** Presentation → Domain ← Data. Domain imports nothing from data, Firebase or platform code. Features never import each other. Firebase lives only in `firebaseData`.
- **One pattern per concern:** StateViewModel, typed routes, Koin, `AppResult<T>`. A new pattern needs an ADR first.
- **Privacy defaults:**
  - visibility default (ADR-0023): PUBLIC in Phase 1 for the 18+ age band, FOLLOWERS for 13–17 and for everyone once SOCIAL-03 ships; last-used thereafter. PUBLIC is adults-only, enforced by rules (13–17 can never post PUBLIC);
  - unknown values map to PRIVATE;
  - precise coordinates are owner-only (`farts/{id}/private/location`) and public docs carry fuzzed geohash-6 coordinates;
  - minimum age 13 (an age band, never a date of birth).
- **The server is authoritative:** rules and functions enforce visibility, counters, entitlements, unlock tokens and the 3/day ad cap. Client checks are UX only, and the client never grants itself an unlock.
- **Monetization hard rules:**
  - cosmetics only;
  - logging, the map, reacting and safety tools stay free;
  - no XP, rank or achievements for sale;
  - rewarded ads are opt-in and single-use;
  - no banners or interstitials;
  - no dark patterns.
- **Map-first and humor-first.** The map is the launch screen. Every string carries the product voice.
- **Parity:** a feature is done only when it works on Android and iOS.

## Stop and ask the owner when
- privacy or safety defaults, monetization posture, a product principle, or phase scope would change;
- an accepted ADR would be reversed;
- a feature isn't in `planning/product/features/`;
- two authoritative docs conflict;
- a task needs a secret committed.

## Where things live
| Topic | File |
|---|---|
| State / roadmap / stories | `planning/STATE.md`, `planning/ROADMAP.md`, `planning/stories/` |
| Owner's console/account to-do (with instructions) | `planning/OwnerTodo.md` |
| Product brief, scope and phases, features | `planning/product/Product.md`, `Scope.md`, `features/` |
| Monetization / growth | `planning/product/Monetization.md`, `Growth.md` |
| Architecture / Firestore schema + rules / local data / security | `planning/architecture/Architecture.md`, `DataContracts.md`, `LocalData.md`, `Security.md` |
| Stack versions and toolchain gotchas | `planning/architecture/TechStack.md`, `gradle/libs.versions.toml` |
| Design tokens / screens | `planning/design/DesignSpecification.md`, `planning/design/screens/` |
| Decisions | `planning/decisions/README.md` |
| How this Claude setup works | `.claude/README.md` |

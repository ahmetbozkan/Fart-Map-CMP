# STATE: Fart Map Planning Operating Ledger

> **Purpose:** live snapshot of the repository's planning and readiness state.
> This is not a product roadmap and not a release plan. It is the current operating state for contributors.
> Update this file whenever planning status, authority, or next authorized work changes.
> Last updated: 2026-10-21 (session 13: STORY-01-01-01 implemented and validated locally; Review).

---

## 1) Project Status Snapshot

| Area | Current status | Notes |
|---|---|---|
| Overall project status | **Bootstrap story implemented locally; in Review** | Repository `github.com/ahmetbozkan/Fart-Map-CMP` (branch `main`, one template commit; session 13 changes uncommitted, owner commits); both Android flavors and both iOS schemes build and launch to the placeholder, shared tests, lint and the backend emulator tests pass on the recorded baseline (`TechStack.md` §3.1); CI not yet run |
| Product planning | **Complete and reconciled** | Owner decisions of 2026-10-14 recorded (ADR-0018, ADR-0020); Features ↔ ReleaseStrategy ids and phases reconciled |
| Architecture planning | **Complete and reconciled** | ADR-0015 (backend compute, Firebase scope), ADR-0016 (purchases), ADR-0017 (rewarded ads), ADR-0019 (location split), ADR-0021 (three-module layout `shared` / `firebaseData` / `umbrella`) applied to the contracts; `TechStack.md` §3.1 baseline filled 2026-10-21 |
| Governance/meta layer | **Complete** | CLAUDE.md freeze rule narrowed; root process docs archived; read chain cleaned |
| Execution docs | **Complete** | Sprint template duplication removed; no other change |
| Design | **Phase 1 coverage complete at spec level** | Seven screen specs, 13 flows, handoff record; Stitch artifacts arrive screen by screen (non-blocking) |
| Roadmap instances | **Active** | `PHASE-01` Active, `EPIC-01-01` Active, `STORY-01-01-01` Review with tasks `TASK-01-01-01-01…06` all in Review (evidence recorded) |
| Implementation authorization | **STORY-01-01-01 in Review; next story authored after the owner's commit and a green CI run** | Work happens in this repository only; the PoC stays frozen |

---

## 2) Planning Completion Matrix

| Layer | Status | Detail |
|---|---|---|
| Product (`planning/product/`) | **Complete** | 16 docs; stale "friend"/"territory"/TBD wording removed; six personas acknowledged in governance |
| Architecture (`planning/architecture/`) | **Complete** | 12 contracts; TechStack carries the bootstrap policy, the proven baseline (§3.1) and the iOS linking rule; Architecture §6 names the three modules (ADR-0021); DataContracts carries catalog, entitlement, token, device and private-location collections |
| Meta / governance (`planning/meta/`) | **Complete** | 21 ADRs; archive and audits folders |
| Execution (`planning/execution/`) | **Complete** | Kernel, workflow, DoR/DoD, templates, sprint and review rules |
| Skills (`planning/skills/`) | **Complete for Phase 1** | 13 playbooks (monetization added); canonical code examples are added from the first real implementation, not invented |
| Roadmap (`planning/roadmap/`) | **Instances active** | Phase 1, first epic and first story (Review) with six tasks (Review); STORY-01-01-02…04 authored next |
| Design (`planning/design/`) | **Complete for Phase 1 at spec level** | Visual artifacts pending from the owner's Stitch process |
| Implementation readiness | **Bootstrap done locally** | Owner items in §5 gate CI, deploy and later epics |

---

## 3) Authoritative Artifacts

### 3.1 Authoritative now

- Product constitution and scope: `ProductGovernance.md`, `Features.md`, `SocialSystems.md`, `ProgressionSystems.md`, `Monetization.md`, `ReleaseStrategy.md`, `ContentSafety.md`
- Architecture constitution and contracts: all twelve files under `planning/architecture/`
- Execution kernel: `SYSTEM.md`, `Workflow.md`, `DefinitionOfReady.md`, `DefinitionOfDone.md`
- Design for Phase 1: `design/DesignSpecification.md`, `design/UXFlowCatalog.md`, `design/screens/*.md`, `design/HANDOFF-UXF-01-04-MVP.md`
- Settled decisions: `planning/meta/decisions/ADR-0001` through `ADR-0021`
- Operating entrypoint and guardrails: `planning/CLAUDE.md`, `planning/meta/AgentGuardrails.md`, `planning/meta/INDEX.md`
- Roadmap instances: `roadmap/phases/PHASE-01.md`, `roadmap/epics/EPIC-01-01.md`, `roadmap/stories/STORY-01-01-01.md`, `roadmap/tasks/TASK-01-01-01-01…06.md`
- Repository entrypoint and commands: root `CLAUDE.md`, `README.md`, `backend/README.md`

### 3.2 Provisional

- Rate limits and cooldowns in `SocialSystems.md` §4/§9 (provisional defaults for Phase 2; confirm before the Phase 2 social epics).

### 3.3 History and evidence (never authority)

- `planning/meta/archive/*` and `planning/meta/audits/*`.

---

## 4) Critical Decisions Already Locked In

### Product-level locked constraints

- Map is the product center; humor-first identity; no pay-to-win; core capabilities free; privacy-safe defaults.
- Follow-only graph with approval (ADR-0009); visibility tiers Public / Followers / Private with Followers default (ADR-0010); following list private by default (ADR-0012); territory out of MVP (ADR-0011); single-style signature (ADR-0013); audio 10 s (ADR-0014).
- **Owner decisions of 2026-10-14:** anonymous sessions are full sessions, providers Google / Apple / Anonymous only (ADR-0018); Phase 1 includes a la carte cosmetic purchases, rewarded ads, the reaction push notification, delete own fart, all three map filters and the four-tab navigation Map / Social / Rank / Profile (ADR-0020); minimum age 13 with an age band; reports auto-hide at three; no server rate limits in Phase 1 beyond a soft client limit.

### Architecture/platform locked decisions

- Room (ADR-0001); GitLive Firebase (ADR-0002); Koin with expect/actual modules (ADR-0003); StateViewModel (ADR-0004); type-safe navigation (ADR-0005) with session-driven start (ADR-0006); local-first reads (ADR-0007); expect/actual boundary (ADR-0008).
- Cloud Functions as backend compute; Messaging, Crashlytics, App Check in scope; backend lives in `backend/` of the new repository; `dev` and `prod` Firebase projects (ADR-0015).
- RevenueCat KMP SDK for purchases with server-mirrored entitlements (ADR-0016); AdMob rewarded ads with server-side verification and single-use tokens (ADR-0017).
- Precise location in an owner-only subdocument; public projection fuzzed (ADR-0019).
- MapKit on iOS and Google Maps on Android behind one map contract; Firebase kept behind interfaces so shared tests run without it; GitHub Actions with iOS builds on a macOS runner; bootstrap on the latest stable compatible dependency set, recorded in `TechStack.md` §3.1 (Kotlin 2.4.21, CMP 1.12.2, AGP 9.4.1, Gradle 9.8.2, GitLive 2.7.0, Firebase iOS SDK 13.0.1; verified 2026-10-21).
- Three Kotlin modules: `shared` (no Firebase types), `firebaseData` (GitLive adapters), `umbrella` (iOS framework `Shared`); Android flavors `dev`/`prod`; Xcode configurations `Debug-dev`/`Debug-prod`/`Release-dev`/`Release-prod` with schemes `FartMap Dev`/`FartMap Prod` (ADR-0021, TASK-01-01-01-01/04).

### Repository decisions

- This repository (`Fart-Map-CMP`) is the product; the PoC is frozen reference. Firebase configs and secrets are injected (`scripts/inject-config.sh`), never tracked; templates are committed; `scripts/check-no-secrets.sh` and gitleaks guard CI. The agent never commits or pushes; the owner commits.

---

## 5) Known Open Questions / Unresolved Decisions

1. **Owner items that gate the story's Done (acceptance criterion 4 and the deploy path):** review and commit the session 13 changes, push, open a draft PR so `.github/workflows/ci.yml` runs; add repository secrets `GOOGLE_SERVICES_JSON_DEV`, `GOOGLE_SERVICES_JSON_PROD`, `GOOGLE_SERVICE_INFO_PLIST_DEV`, `GOOGLE_SERVICE_INFO_PLIST_PROD` (base64 of the four config files) and the environment secret `FIREBASE_SERVICE_ACCOUNT` in GitHub environments `dev` and `prod` (prod with a required reviewer); upgrade `fart-map-dev` and `fart-map-1a558` to the Blaze plan and enable Firestore (native mode, location `europe-west1` to match the functions region) and Storage in both; confirm the repository is private.
2. **Owner items for the local toolchain:** install JDK 21 (`brew install openjdk@21`; Gradle provisions one meanwhile, the Firestore emulator needs it on `PATH`) and Node.js 22 (local Node is 20.11; `backend/functions` declares 22).
3. **Owner items for later epics:** Auth providers Google/Apple/Anonymous and App Check registration (EPIC-01-02), RevenueCat project and AdMob app with test ad units (EPIC-01-06), App Store Connect / Play Console apps and store products (EPIC-01-06); minimum iOS version decision (template 18.2; see `TechStack.md` §3.1 notes).
4. **Design artifacts:** Stitch screens arrive one by one; stories do not block on them (`design/SYSTEM.md` §4).

No product-level MVP-critical TBD is open.

---

## 6) Next Authorized Work

### Immediate

1. **Owner:** review the staged session 13 changes, commit and push; open a draft PR; add the GitHub secrets (§5.1).
2. **Agent (Mode 4):** when CI reports, fix anything red in the workflows; then move STORY-01-01-01 and its tasks to Done/Verified with the CI link as evidence.
3. **Agent (Mode 2):** author STORY-01-01-02 (DI, navigation shell, theme, observability base), STORY-01-01-03 (Room bootstrap) and STORY-01-01-04 (real rules) under EPIC-01-01, then EPIC-01-02 (session and identity) using the templates.

### Should wait

- Epics EPIC-01-02 … EPIC-01-09 are instantiated one at a time as the previous one reaches Review; no backlog generation beyond that.
- No planning document expansion unless an implementation story exposes a contract gap (then: update the owner document, record in ChangeLog).

### Start execution when

- Execution planning is authorized now with `execution/SYSTEM.md`, `Workflow.md`, `DefinitionOfReady.md`, `DefinitionOfDone.md`.

### Start implementation when

1. Product layer complete (true)
2. Architecture contract layer complete (true)
3. Governance/meta layer synchronized (true)
4. Execution kernel complete (true)
5. First slice passes Definition of Ready (STORY-01-01-01: Ready 2026-10-18, implemented 2026-10-21)
6. The work happens in this repository (`Fart-Map-CMP`), never in the PoC (owner decision)

Current state: **STORY-01-01-01 implemented and validated locally (Review). Next implementation story is authored after the owner's commit and the first CI run.**

---

## 7) Read-Before-Work Checklists

### 7.1 Before touching product docs

- [ ] Read `planning/product/ProductGovernance.md`
- [ ] Read `planning/product/ReleaseStrategy.md`
- [ ] Read relevant system doc (`SocialSystems`, `ProgressionSystems`, `Monetization`)
- [ ] Check ADRs for already-settled decisions
- [ ] Confirm change does not violate non-negotiable principles

### 7.2 Before touching architecture docs

- [ ] Read `planning/architecture/ArchitecturePrinciples.md`
- [ ] Read `planning/architecture/Architecture.md`
- [ ] Read `planning/architecture/SecurityAndSecrets.md`
- [ ] Read `planning/product/ContentSafety.md`
- [ ] Check relevant ADRs (`0001`–`0021`)
- [ ] Confirm no layer-boundary or privacy default violation

### 7.3 Before touching execution docs

- [ ] Read `planning/execution/SYSTEM.md`
- [ ] Read `planning/meta/AgentGuardrails.md`
- [ ] Read current `planning/roadmap/STATE.md`
- [ ] Confirm workflow proposal maps to authority hierarchy

### 7.4 Before touching design docs

- [ ] Read `planning/product/Vision.md` and `ProductPillars.md`
- [ ] Read `planning/product/Features.md` and `ReleaseStrategy.md`
- [ ] Read `planning/product/ContentSafety.md`
- [ ] Read `planning/design/SYSTEM.md` and `DesignSpecification.md`
- [ ] Read `planning/architecture/Architecture.md` and relevant architecture contracts

### 7.5 Before touching implementation

- [ ] Read `planning/CLAUDE.md`
- [ ] Read `planning/meta/AgentGuardrails.md`
- [ ] Read current `planning/roadmap/STATE.md`
- [ ] Read execution kernel docs
- [ ] Confirm Definition of Ready is met
- [ ] Confirm implementation is authorized in this file and that you are in the new repository

---

## 8) STATE Maintenance Rule

Update this file immediately when any of the following occurs:

1. A planning layer status changes.
2. A new authoritative doc is added or authority source changes.
3. A blocking unresolved decision is resolved (or a new blocker appears).
4. Authorization boundary changes.
5. Next authorized work sequence changes.

### Update format requirements

- Update snapshot and completion matrix first.
- Update next authorized work sequence.
- Keep open-questions list limited to real active blockers.
- Add matching entry to `planning/meta/ChangeLog.md` in same session.

---

*This file is the live operating ledger for repo state. If it is stale, autonomous work quality degrades quickly. Keep it current.*

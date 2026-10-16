# STATE: Fart Map Planning Operating Ledger

> **Purpose:** live snapshot of the repository's planning and readiness state.
> This is not a product roadmap and not a release plan. It is the current operating state for contributors.
> Update this file whenever planning status, authority, or next authorized work changes.
> Last updated: 2026-10-15 (owner confirmations recorded, session 11 close).

---

## 1) Project Status Snapshot

| Area | Current status | Notes |
|---|---|---|
| Overall project status | **Planning closeout complete; waiting for the new repository** | Every layer audited and reconciled on 2026-10-14 (`meta/audits/2026-10-14-planning-closeout-audit.md`); documentation expansion stops here |
| Product planning | **Complete and reconciled** | Owner decisions of 2026-10-14 recorded (ADR-0018, ADR-0020); Features ↔ ReleaseStrategy ids and phases reconciled |
| Architecture planning | **Complete and reconciled** | ADR-0015 (backend compute, Firebase scope), ADR-0016 (purchases), ADR-0017 (rewarded ads), ADR-0019 (location split) applied to the contracts |
| Governance/meta layer | **Complete** | CLAUDE.md freeze rule narrowed; root process docs archived; read chain cleaned |
| Execution docs | **Complete** | Sprint template duplication removed; no other change |
| Design | **Phase 1 coverage complete at spec level** | Seven screen specs, 13 flows, handoff record; Stitch artifacts arrive screen by screen (non-blocking) |
| Roadmap instances | **Started** | `PHASE-01`, `EPIC-01-01`, `STORY-01-01-01` (Backlog) |
| Implementation authorization | **Authorized for the new repository only, starting with STORY-01-01-01 once its dependencies are met** | No application code is written in this PoC repository |

---

## 2) Planning Completion Matrix

| Layer | Status | Detail |
|---|---|---|
| Product (`planning/product/`) | **Complete** | 16 docs; stale "friend"/"territory"/TBD wording removed; six personas acknowledged in governance |
| Architecture (`planning/architecture/`) | **Complete** | 12 contracts; TechStack carries the bootstrap policy and iOS linking rule; DataContracts carries catalog, entitlement, token, device and private-location collections |
| Meta / governance (`planning/meta/`) | **Complete** | 20 ADRs; archive and audits folders |
| Execution (`planning/execution/`) | **Complete** | Kernel, workflow, DoR/DoD, templates, sprint and review rules |
| Skills (`planning/skills/`) | **Complete for Phase 1** | 13 playbooks (monetization added); canonical code examples are added from the first real implementation, not invented |
| Roadmap (`planning/roadmap/`) | **Instances started** | Phase 1, first epic and first story authored; further epics authored as the preceding story reaches Ready |
| Design (`planning/design/`) | **Complete for Phase 1 at spec level** | Visual artifacts pending from the owner's Stitch process |
| Implementation readiness | **Blocked on owner deliverables** | See §5 |

---

## 3) Authoritative Artifacts

### 3.1 Authoritative now

- Product constitution and scope: `ProductGovernance.md`, `Features.md`, `SocialSystems.md`, `ProgressionSystems.md`, `Monetization.md`, `ReleaseStrategy.md`, `ContentSafety.md`
- Architecture constitution and contracts: all twelve files under `planning/architecture/`
- Execution kernel: `SYSTEM.md`, `Workflow.md`, `DefinitionOfReady.md`, `DefinitionOfDone.md`
- Design for Phase 1: `design/DesignSpecification.md`, `design/UXFlowCatalog.md`, `design/screens/*.md`, `design/HANDOFF-UXF-01-04-MVP.md`
- Settled decisions: `planning/meta/decisions/ADR-0001` through `ADR-0020`
- Operating entrypoint and guardrails: `planning/CLAUDE.md`, `planning/meta/AgentGuardrails.md`, `planning/meta/INDEX.md`
- Roadmap instances: `roadmap/phases/PHASE-01.md`, `roadmap/epics/EPIC-01-01.md`, `roadmap/stories/STORY-01-01-01.md`

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
- MapKit on iOS and Google Maps on Android behind one map contract; Firebase kept behind interfaces so shared tests run without it; GitHub Actions with iOS builds on a macOS runner; bootstrap on the latest stable compatible dependency set, recorded in `TechStack.md` §3.1.

### Repository decisions

- This repository is a frozen PoC and the current home of `planning/`. The owner creates the new repository and consoles and copies `planning/` into it. Secrets and Firebase config files were untracked here on 2026-10-14; the owner replaces the Firebase project and keys. Nothing else is removed from the PoC.

---

## 5) Known Open Questions / Unresolved Decisions

1. **Owner deliverables (block STORY-01-01-01):** new repository with GitHub Actions; `dev` and `prod` Firebase projects with App Check; applicationId, iOS bundle id, display name; RevenueCat project; AdMob app with test ad units; App Store Connect and Play Console apps and Phase 1 store products; copy of `planning/` into the new repository.
2. **Design artifacts:** Stitch screens arrive one by one; stories do not block on them (`design/SYSTEM.md` §4).

No product-level MVP-critical TBD is open.

---

## 6) Next Authorized Work

### Immediate

1. **Owner:** commit the closeout milestone in this repository; create the new repository, Firebase projects and consoles; copy `planning/` into the new repository.
2. **Agent (Mode 2, then Mode 3) in the new repository:** when item 1 is done, move `STORY-01-01-01` to Ready (DoR evidence: dependencies met) and implement it; author `TASK-01-01-01-01…06` from its task breakdown.
3. **Agent:** on STORY-01-01-01 Done, author STORY-01-01-02/03/04 under EPIC-01-01, then EPIC-01-02 (session and identity) using the templates.

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
5. First slice passes Definition of Ready (STORY-01-01-01: pending owner deliverables; all planning confirmations closed 2026-10-15)
6. The work happens in the new repository, never in this PoC (owner decision)

Current state: **Implementation authorized for STORY-01-01-01 in the new repository once its dependencies are met.**

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
- [ ] Check relevant ADRs (`0001`–`0020`)
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

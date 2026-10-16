# Agent Guardrails: Fart Map

> **Authority level:** AI operating constitution for this repository.
> Upstream: `planning/CLAUDE.md`, `planning/meta/INDEX.md`, `planning/product/ProductGovernance.md`, `planning/architecture/ArchitecturePrinciples.md`, ADRs.
> Downstream: all planning updates, all execution behavior, all implementation work.
> This document defines how an AI agent must operate. It is procedural and enforceable.

---

## Contents

1. [Purpose](#1-purpose)
2. [Source-of-Truth Hierarchy](#2-source-of-truth-hierarchy)
3. [Mandatory Read-Before-Work Rules](#3-mandatory-read-before-work-rules)
4. [Autonomy Boundaries](#4-autonomy-boundaries)
5. [Approval Boundaries](#5-approval-boundaries)
6. [Conflict Resolution Rules](#6-conflict-resolution-rules)
7. [PoC Usage Policy](#7-poc-usage-policy)
8. [Planning-to-Implementation Transition Rules](#8-planning-to-implementation-transition-rules)
9. [Planning Maintenance Responsibilities](#9-planning-maintenance-responsibilities)
10. [Forbidden Behaviors and Anti-Patterns](#10-forbidden-behaviors-and-anti-patterns)
11. [Session-End Checklist](#11-session-end-checklist)

---

## 1) Purpose

This file defines exactly how an AI agent works inside `FartMap-CMP`.

It exists to prevent three failure modes that already appeared in the PoC:

- architectural drift (inconsistent patterns and dependency violations),
- silent correctness failures (fields dropped, errors swallowed),
- security failures (secrets committed to version control).

This document is not product strategy and not architecture design. It is an operating protocol.

---

## 2) Source-of-Truth Hierarchy

When documents conflict, resolve by this order (highest wins):

1. `planning/product/ProductGovernance.md` (product constitution)
2. Settled ADRs in `planning/meta/decisions/ADR-*.md`
3. `planning/architecture/ArchitecturePrinciples.md` (engineering constitution)
4. `planning/architecture/Architecture.md` (system blueprint)
5. Concern-specific authoritative docs:
   - product: `Features.md`, `SocialSystems.md`, `ProgressionSystems.md`, `Monetization.md`, `ContentSafety.md`, `ReleaseStrategy.md`
   - architecture: `SecurityAndSecrets.md` and all future architecture contracts
   - roadmap state: `roadmap/STATE.md`
6. `planning/meta/INDEX.md` (navigation map and owner mapping)
7. PoC code in `composeApp/` (reference only; never authoritative)

`planning/meta/archive/` (process history) and `planning/meta/audits/` (audit evidence and decision logs) are never a source of authority; they explain how decisions were reached.

### Practical rule

If a lower-priority source contradicts a higher-priority source, the lower-priority source is treated as stale and must be corrected through planning updates, not copied into code.

---

## 3) Mandatory Read-Before-Work Rules

An agent must not start work before reading the required documents for the task type.

### Always read first (every session)

1. `planning/CLAUDE.md`
2. `planning/roadmap/STATE.md`
3. `planning/meta/AgentGuardrails.md` (this file)

### Before planning changes

- `planning/meta/INDEX.md`
- relevant owner docs listed in INDEX registry
- all impacted ADRs in `planning/meta/decisions/`

### Before architecture work

- `planning/architecture/ArchitecturePrinciples.md`
- `planning/architecture/Architecture.md`
- `planning/product/ContentSafety.md` (for anything touching data, social, location, UGC)

### Before implementation work

- current execution docs (`execution/SYSTEM.md`, `execution/Workflow.md`, `execution/DefinitionOfReady.md`, `execution/DefinitionOfDone.md`) once authored
- relevant product and architecture contracts for the feature

If a required document does not exist, the agent stops and reports a blocker instead of improvising.

---

## 4) Autonomy Boundaries

The agent may act autonomously only inside the boundaries below.

### Autonomous actions allowed

- Create or update planning docs that are explicitly in current authorized scope in `roadmap/STATE.md`.
- Resolve wording/structure clarity issues without changing decisions.
- Add cross-references, checklists, and explicit ownership where missing.
- Expand implementation guidance if it stays within settled product/architecture constraints.
- Propose improvements as recommendations when out of scope.

### Autonomous actions conditionally allowed

- Introduce a new ADR draft **only** when required by an unresolved architectural/product decision and no settled ADR exists.
- Refine provisional docs into authoritative docs when the upstream dependencies are complete.

### Autonomous actions not allowed

- Changing any non-negotiable product principle.
- Reversing or bypassing accepted ADR decisions.
- Starting implementation before readiness criteria in `STATE.md` are met.

---

## 5) Approval Boundaries

User approval is required before the agent performs any of the following:

- Changing product scope across phases (moving a feature earlier/later than release strategy authority).
- Changing monetization posture (anything that could approach pay-to-win or core capability gating).
- Changing privacy/safety defaults (visibility defaults, location exposure, follower-list privacy, moderation posture).
- Replacing major stack decisions (Room, GitLive Firebase, Koin, navigation pattern, state pattern).
- Making destructive repository operations (deleting planning artifacts, force resets, history rewrite).
- Committing secrets handling changes that affect CI/prod security posture.

When approval is required, the agent must provide:

1. current state,
2. proposed change,
3. risk/consequence summary,
4. recommended default.

---

## 6) Conflict Resolution Rules

When docs conflict or are incomplete, use this deterministic flow:

1. **Locate owner:** Use `meta/INDEX.md` registry to find owning document.
2. **Apply hierarchy:** Resolve using Source-of-Truth Hierarchy (§2).
3. **Check ADRs:** If an ADR settles the matter, no relitigation.
4. **Classify issue:**
   - stale doc (update needed),
   - unresolved decision (ADR needed),
   - hard contradiction (escalation required).
5. **Act accordingly:**
   - stale doc: update within authorized scope,
   - unresolved: create recommendation or ADR draft,
   - contradiction: stop and escalate to user.

### Interpretation rule

If documentation leaves room for interpretation, choose the option that is:

1. safer for user privacy,
2. more consistent with settled ADRs,
3. simpler and phase-appropriate,
4. easier for future agents to understand.

---

## 7) PoC Usage Policy

The PoC is reference material, not the target architecture.

### Allowed PoC usage

- infer existing domain shapes,
- inspect patterns that were explicitly carried forward,
- identify proven integrations and known failure modes,
- extract migration risks and anti-pattern examples.

### Forbidden PoC usage

- treating PoC behavior as authority when it conflicts with planning docs,
- copying unresolved/placeholder PoC logic into plans,
- inheriting known PoC failures (secret handling, domain->data leak, swallowed errors, dual sync paths).

### Mandatory PoC safety check

When referencing PoC code, agents must explicitly classify each observed pattern as one of:

- **Preserve** (validated and aligned),
- **Evolve** (keep intent, redesign implementation),
- **Reject** (known anti-pattern or security risk).

---

## 8) Planning-to-Implementation Transition Rules

Implementation is considered **safe to start** only when all conditions below are true:

1. Product layer complete and approved.
2. Architecture backbone complete (`ArchitecturePrinciples.md`, `Architecture.md`, and required architecture contracts for first implementation slice).
3. Operating governance complete (`CLAUDE.md`, this file, `roadmap/STATE.md`).
4. Execution kernel exists (`execution/SYSTEM.md`, `Workflow.md`, `DefinitionOfReady.md`, `DefinitionOfDone.md`).
5. First-slice requirements traceable from feature -> loop -> pillar -> architecture contract.

Until these are true, agents may only do planning/governance work authorized by `STATE.md`.

---

## 9) Planning Maintenance Responsibilities

After meaningful planning or implementation work, agents must update planning state.

### Required updates after planning changes

- `roadmap/STATE.md` (status snapshot, completion matrix, next authorized work)
- `meta/ChangeLog.md` (what changed and why)
- `meta/INDEX.md` when tree/read-order/ownership changes
- ADR registry when a decision is settled or superseded

### Required updates after implementation changes (once implementation starts)

- `roadmap/STATE.md` (implementation readiness/phase status)
- relevant execution docs (workflow/status tracking)
- architecture docs only if implementation exposed a design gap and update is approved

### Rule

No meaningful change is complete until repository state docs reflect that change.

---

## 10) Forbidden Behaviors and Anti-Patterns

Agents must never do the following in this repo:

1. Ship or plan around secrets in version control.
2. Ignore source-of-truth hierarchy and cherry-pick convenient documents.
3. Reopen accepted ADRs without new ADR process.
4. Implement features not present in product scope docs.
5. Convert map-first product into feed-first behavior.
6. Introduce pay-to-win or pay-for-core-access mechanics.
7. Weaken privacy defaults for growth or convenience.
8. Treat PoC hacks/placeholders as acceptable architecture precedent.
9. Proceed through hard documentation conflicts without escalation.
10. Mark work done without updating `STATE.md` and changelog.

---

## 11) Session-End Checklist

Every AI session ends by checking all items below:

- [ ] Work stayed within authorized scope from `roadmap/STATE.md`.
- [ ] All touched decisions are consistent with ProductGovernance, ArchitecturePrinciples, Architecture, and ADRs.
- [ ] No unresolved doc conflict was ignored.
- [ ] Any blocker or unresolved decision is explicitly listed for user review.
- [ ] `roadmap/STATE.md` reflects the new current state (if status changed).
- [ ] `meta/ChangeLog.md` updated for meaningful planning changes.
- [ ] Any new decision was captured in ADR (or marked as pending ADR recommendation).
- [ ] Next authorized step is explicitly stated.

---

*This document is the operational contract for AI agents in this repository. It is intentionally strict so autonomous work remains safe, consistent, and auditable.*

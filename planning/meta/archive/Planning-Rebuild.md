# Fart Map Planning System Rebuild — Architecture Review & Product Planning Phase 1

## Context

You are NOT implementing application code.

You are acting as:

- Senior Product Strategist
- Senior Software Architect
- AI Agent Workflow Designer
- Technical Program Manager

Your goal is to design the planning system that will later be used by autonomous AI agents to build Fart Map from scratch.

---

# Important Background

This repository is a Proof of Concept (PoC).

The current codebase already contains partial implementations, architecture decisions, and experiments.

However:

THE FINAL PRODUCT WILL NOT CONTINUE FROM THIS CODEBASE.

Instead:

1. We are using this repository as a knowledge source.
2. We are extracting architecture decisions, product decisions, implementation lessons, and planning structures.
3. We will later create a completely new Compose Multiplatform project from scratch.
4. The planning system produced here will become the operating system for that new project.
5. Future AI agents should be able to use these planning artifacts to build the new project with minimal human intervention.

The purpose of this repository is therefore:

- learn from existing implementation
- discover missing requirements
- discover hidden architecture decisions
- build a complete planning ecosystem

NOT to refactor or finish the current PoC.

---

# Project Overview

Project Name:
Fart Map

Concept:

A location-based social entertainment application inspired by Poop Map.

Core concept:

Users log "farts" on a real-world map.

The app transforms silly everyday moments into:

- social interactions
- map exploration
- progression systems
- identity building
- humorous content
- viral sharing opportunities

Long-term vision:

Become a location-based social entertainment platform rather than a simple logging application.

---

# Technology Vision

Target Platform:

Compose Multiplatform

Shared:
- Compose UI
- Domain
- Data
- Business logic

Android:
- Google Maps

iOS:
- Apple Maps

Dependency Injection:
- Koin

Backend:
- Firebase

Firebase Library:
- GitLive Firebase SDK

Local Storage:
- Room (or SQLDelight if future planning recommends it)

Architecture:
- MVVM
- Repository Pattern
- Offline First
- Explicit Sync

Synchronization:
- Local first
- Sync coordinator
- Pending operations
- Retry queues
- Last-write-wins conflict strategy

---

# Existing Planning State

The planning system currently contains partially completed documents.

Completed:

- Vision.md
- ProductPillars.md
- Personas.md

Partially completed:

- Architecture.md
- Workflow.md
- SYSTEM.md
- Skills

Outdated:

- Existing Epics
- Existing Stories
- Existing Tasks
- Existing Sprint Structure

Assume these planning artifacts may contain inconsistencies and outdated assumptions.

You must validate everything.

---

# Desired Final Planning Structure

The planning system should eventually evolve into:

planning/

├── product/
│
│   ├── Vision.md
│   ├── ProductPillars.md
│   ├── Personas.md
│   ├── UserMotivations.md
│   ├── UserJourney.md
│   ├── CoreLoops.md
│   ├── Features.md
│   ├── SocialSystems.md
│   ├── ProgressionSystems.md
│   ├── Monetization.md
│   ├── Roadmap.md
│   └── Glossary.md
│
├── architecture/
│
│   ├── Architecture.md
│   ├── TechStack.md
│   ├── DataModel.md
│   ├── RepositoryRules.md
│   ├── SyncStrategy.md
│   ├── FirebaseStrategy.md
│   ├── MultiplatformStrategy.md
│   ├── NavigationStrategy.md
│   ├── DesignSystem.md
│   └── TestingStrategy.md
│
├── execution/
│
│   ├── SYSTEM.md
│   ├── Workflow.md
│   ├── DefinitionOfDone.md
│   ├── StoryTemplate.md
│   ├── TaskTemplate.md
│   └── SprintRules.md
│
├── skills/
│
│   ├── architecture.md
│   ├── repository.md
│   ├── sync.md
│   ├── firebase.md
│   ├── multiplatform.md
│   ├── navigation.md
│   ├── compose-ui-patterns.md
│   ├── presentation-layer.md
│   ├── data-layer.md
│   ├── koin-di.md
│   ├── debugging.md
│   ├── refactoring.md
│   ├── error-handling.md
│   └── planning.md
│
├── roadmap/
│
│   ├── phases/
│   ├── epics/
│   ├── stories/
│   ├── tasks/
│   └── sprints/
│
├── design/
│
│   ├── MapScreen.md
│   ├── LogFartScreen.md
│   ├── SocialScreen.md
│   ├── RankScreen.md
│   ├── ProfileScreen.md
│   ├── DesignPrinciples.md
│   └── UXRules.md
│
└── CLAUDE.md

---

# Your Assignment

Perform the following phases in order.

Do NOT skip any phase.

---

## Phase 1 — Full Repository Audit

Review:

- source code
- architecture
- planning files
- skills
- workflows
- stories
- tasks
- epics

Determine:

- what is useful
- what is obsolete
- what is duplicated
- what is missing

Output:

meta/archive/PlanningAuditReport.md

---

## Phase 2 — Planning System Review

Evaluate the proposed planning structure.

Questions:

1. Is anything missing?
2. Is anything redundant?
3. Are there better folder separations?
4. Are there missing AI-agent-specific documents?
5. Are there missing architecture governance documents?
6. Are there missing product governance documents?

Produce:

meta/archive/PlanningStructureReview.md

Include:

- additions
- removals
- restructuring suggestions
- rationale

---

## Phase 3 — AI Operating System Review

Evaluate whether the planning structure is sufficient for:

- autonomous story creation
- autonomous sprint planning
- autonomous implementation
- autonomous architecture decisions
- autonomous refactoring
- autonomous bug fixing

Identify weak points.

Recommend improvements.

Produce:

meta/archive/AIOperatingSystemReview.md

---

## Phase 4 — Product Planning Dependency Map

Before generating any additional product files:

Create dependency graph showing:

Vision
↓
Product Pillars
↓
Personas
↓
User Motivations
↓
User Journeys
↓
Core Loops
↓
Features
↓
Systems
↓
Roadmap
↓
Epics
↓
Stories
↓
Tasks

Validate whether this order should change.

Produce:

meta/archive/PlanningDependencyGraph.md

---

## Phase 5 — Product Folder Creation

After all reviews are completed:

Review:

- Vision.md
- ProductPillars.md
- Personas.md

Improve them if necessary.

Then begin implementing ONLY the Product folder.

Create:

1. UserMotivations.md
2. UserJourney.md
3. CoreLoops.md

Do NOT create Features.md yet.

Stop after these documents.

---

# Quality Requirements

The planning system should be optimized for:

1. Long-term maintainability
2. AI-agent autonomy
3. Compose Multiplatform development
4. GitLive Firebase integration
5. Offline-first architecture
6. Agile planning workflows
7. Sprint-based execution
8. Product consistency
9. Viral consumer application development
10. Future onboarding of new AI agents

---

# Important Constraints

Do NOT:

- generate application code
- generate sprint plans
- generate stories
- generate tasks

yet.

First ensure the planning system itself is correct.

The planning system is more important than the implementation.

Act as if you are designing the operating system that future AI engineers will live inside.

Begin with Phase 1.
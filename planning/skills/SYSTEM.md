# Skills SYSTEM: Fart Map

> Authority: skill-layer operating contract.
> Upstream: `planning/architecture/*`, `planning/product/*`, `planning/execution/*`, `planning/meta/AgentGuardrails.md`.
> Downstream: all skill playbooks under `planning/skills/` and autonomous implementation sessions.

---

## 1) Purpose

Skills define repeatable execution recipes for recurring technical work.

They convert architecture/product/execution rules into practical "how to do it" sequences.
They do not redefine architecture, product scope, or workflow states.

---

## 2) Ownership Boundaries

## Skills own

- task-time step-by-step implementation playbooks
- common failure recovery patterns
- verification command/check patterns

## Skills do not own

- product decisions (product docs)
- architecture contracts (architecture docs)
- work-item lifecycle rules (execution docs)

---

## 3) Skill Standard (Mandatory)

Every skill file must include:

1. Purpose and usage trigger
2. Authoritative source links (rules it follows)
3. Step-by-step procedure
4. Common failure modes and recovery path
5. Validation checklist
6. Escalation triggers

If a skill cannot cite an authoritative source, it is incomplete.

---

## 4) Skill Invocation Rules for AI Agents

1. Start from `planning/skills/INDEX.md` to choose the smallest matching skill.
2. Use one primary skill per task; compose additional skills only when needed.
3. If skill guidance conflicts with authoritative docs, authoritative docs win.
4. If no skill matches, create a new skill draft only after confirming the pattern is recurring.

---

## 5) Validation Checklist

- [ ] Skill has explicit source links to owner docs
- [ ] Skill is actionable (not prose-only)
- [ ] Skill includes recovery paths
- [ ] Skill includes stop/escalate conditions
- [ ] Skill does not introduce new product or architecture rules

---
name: adr
description: Write an Architecture/Product Decision Record in planning/decisions with status Proposed, update the ADR index and STATE pending decisions. Use when a change would alter a settled decision, introduce a new pattern or dependency, or change a product/privacy/monetization default.
---

# ADR

1. Next number: the highest `planning/decisions/ADR-*.md` plus 1. File: `ADR-NNNN-kebab-title.md`.
2. Content (under about 40 lines):
   ```
   # ADR-NNNN: <Title>
   - Status: Proposed
   - Date: YYYY-MM-DD
   - Supersedes / affects: ADR-xxxx (if any)
   ## Context        what forces the decision (facts, measurements, constraints)
   ## Decision       what we do, concretely
   ## Alternatives   each with why not
   ## Consequences   docs/code that change, risks, triggers to revisit
   ## Owner choice   only if options remain for the owner; list them with a recommended default
   ```
3. Add a row to `planning/decisions/README.md` with status **Proposed**. Add a line under "Pending owner decisions" in `planning/STATE.md`.
4. Don't implement against an accepted ADR until the owner accepts the new one. Work that doesn't depend on it may continue.
5. **When the owner accepts:**
   - set `Status: Accepted (YYYY-MM-DD, owner)`;
   - mark the superseded ADR `Superseded by ADR-NNNN`;
   - update the docs listed under Consequences in the same PR.

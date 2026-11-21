---
paths:
  - "planning/**"
---

# Planning docs rules

- `planning/README.md` is the map and `planning/WorkingAgreement.md` §1 is the authority order. Never edit or cite `planning/archive/`.
- Editorial fixes are always allowed: stale wording, broken references, contradictions resolved toward the higher authority. Changing a **decision** is not. Write a Proposed ADR (`adr` skill) and list it under "Pending owner decisions" in `planning/STATE.md`.
- Each fact lives once. Link to the owning doc instead of restating rules. Product rules live in `planning/product/`, technical contracts in `planning/architecture/`, the work state in `STATE.md` / `ROADMAP.md` / `stories/`.
- `STATE.md` stays under about 40 lines: Now / Next / Pending decisions / Owner actions. History belongs in git and PR descriptions, not a changelog.
- A story follows the template in `WorkingAgreement.md` §4. Tasks are checkboxes inside the story, never separate files.
- A feature spec lives in `planning/product/features/<system>.md`. Phase placement is owned by `planning/product/Scope.md`; on a conflict, fix the feature file.
- After moving or renaming a doc, run `scripts/check-doc-links.sh`.

# Screen Specification Template: Fart Map

> Authority: standard contract for per-screen design specs.
> Upstream: `planning/design/SYSTEM.md`, `planning/design/DesignSpecification.md`, `planning/product/Features.md`, `planning/product/UserJourney.md`, `planning/product/CoreLoops.md`, relevant architecture docs.
> Downstream: screen-specific design docs, Stitch mapping records, implementation stories/tasks.

---

## 1) Purpose

This template standardizes per-screen specifications so implementation agents can build UI without ambiguity.

It owns screen-level behavior, states, and traceability.
It does not restate global tokens or component internals already owned by `DesignSpecification.md`.

---

## 2) Template

```md
# <ScreenName>

## Ownership
- Status: draft | active | superseded
- Owner concern: design/screen behavior
- Upstream dependencies:
- Downstream consumers:

## Traceability
- Feature link(s):
- Loop link(s):
- Journey stage(s):
- Persona focus:
- Architecture link(s):

## Screen Goal
- Primary user outcome:
- Success signal:

## Entry/Exit
- Entry routes:
- Exit routes:
- Navigation/back behavior:

## Layout Structure
- Top-level regions:
- Primary CTA:
- Secondary actions:

## UI States
- Loading:
- Empty:
- Success:
- Error:
- Offline:

## Interactions
- User actions:
- Feedback/animation notes:
- Validation behavior:

## Safety and Privacy UX
- Visibility/privacy defaults shown:
- Reporting/blocking affordance:
- Sensitive data redaction/display rules:

## Platform Differences
- Android behavior:
- iOS behavior:
- Shared behavior constraints:

## Stitch Mapping (if used)
- stitch_artifact_id:
- stitch_version:
- mapped_frames:
- unresolved deltas:

## Implementation Handoff
- Required components:
- State model expectations:
- Test/validation notes:
```

---

## 3) Validation Checklist

- [ ] Traceability complete (feature/loop/journey)
- [ ] All required UI states specified
- [ ] Privacy/safety UX requirements explicit
- [ ] Platform differences documented or marked N/A
- [ ] Stitch mapping present if artifact exists
- [ ] Handoff section actionable for story/task creation

---

## 4) AI Usage Guidance

1. Create a screen spec only when a screen has behavior beyond what `DesignSpecification.md` already covers.
2. Keep references by link instead of duplicating global rules.
3. If screen behavior conflicts with architecture navigation or data constraints, escalate before handoff.

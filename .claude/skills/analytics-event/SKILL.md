---
name: analytics-event
description: Add or change a product analytics event (Firebase Analytics) or Crashlytics key through the typed event contract, keeping PII and coordinates out. Use when instrumenting a funnel, KPI, experiment, or monetization step.
---

# Analytics event

1. **Find the metric** it serves in `planning/product/Metrics.md` (Gate 1: first-session loop completion, time-to-first-log, first social touch, log abandonment; plus monetization and growth funnels). No metric, no event.
2. **Taxonomy:** add or adjust the row in `planning/architecture/Observability.md` §11:
   - name in `snake_case` `object_action` (e.g. `fart_logged`, `unlock_sheet_shown`);
   - parameters with types;
   - the phase.
3. **Code:** add a case to the sealed `AnalyticsEvent` in `core/observability` and emit it through the `Analytics` contract (implemented in `firebaseData`). Never call the Firebase Analytics SDK from features.
4. **Privacy:**
   - no coordinates (send a coarse region bucket at most);
   - no free text, emails, receipt or token values;
   - user properties limited to an anonymous-vs-linked flag, age band and environment;
   - respect consent (UMP / ATT) for anything ad-related.
5. **Test:** the ViewModel test asserts the event is emitted once on the Action, using a fake `Analytics`.
6. **Experiments:** if the event belongs to an experiment, also follow the `growth-experiment` skill.

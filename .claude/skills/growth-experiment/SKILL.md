---
name: growth-experiment
description: Design and implement a growth or monetization experiment (price test, paywall/offer variant, rewarded-ad cap, onboarding or share prompt timing) with Firebase Remote Config / A/B Testing or RevenueCat Experiments, analytics events and a readout plan. Use when testing prices, offers, prompts, or any variant.
---

# Growth experiment

Only within accepted rules: experiments may tune prices, copy, timing and caps. They may never test forbidden mechanics (pay-to-win, passive ads, dark patterns, onboarding upsell). Remote Config and offer mechanics are authorized by ADR-0024 (accepted 2026-11-22).

1. **Hypothesis card** in `planning/product/growth/experiments/EXP-NNN.md`:
   - hypothesis;
   - primary metric and guardrail metrics (from `planning/product/Metrics.md`: retention, uninstall, reviews, notification opt-out);
   - variants;
   - audience (new users only?);
   - minimum sample and duration;
   - the decision rule.
2. **Mechanism:**
   - prices or offerings → RevenueCat Offerings / Experiments;
   - app behavior (caps, copy, timing) → Remote Config key with a safe in-app default (`core/platform` `RemoteConfig` contract, implemented in `firebaseData`);
   - never ship a variant that only works when Remote Config is reachable.
3. **Events:** use the `analytics-event` skill for exposure + conversion events. Exposure is logged when the variant is actually shown.
4. **Implement** behind the config key, with a ViewModel test for each variant.
5. **Readout:** after the planned duration, record the result and the decision in the card. Clean up the losing variant code in a follow-up story.

# ADR-0026: Log Fart location precision (coarse fix + pin, no fine permission)
- Status: Proposed
- Date: 2026-11-27
- Supersedes / affects: narrows how ADR-0019's "precise point" is obtained; no change to ADR-0019 itself

## Context
- ADR-0019 stores a precise point owner-only and publishes a geohash-6 cell (about 1.2 km × 0.6 km) with a fuzzed point.
- STORY-01-03-04 asks Android for `ACCESS_COARSE_LOCATION` only (the map only centers). iOS asks when-in-use at hundred-metre accuracy, and the user keeps the system Precise toggle. STATE left it to EPIC-01-04 to decide whether logging adds fine location.
- An Android coarse fix can be off by about 2 km, so the "precise" point and its public cell can be wrong unless the user moves the pin. CREATE-03.1 step 1 already has an "Adjust location" pin picker.
- `ContentSafety.md` treats location as the highest-risk surface. WorkingAgreement §6 breaks ties toward privacy.

## Decision (proposed)
1. Android keeps `ACCESS_COARSE_LOCATION` only. No `ACCESS_FINE_LOCATION`, no background location. iOS is unchanged.
2. Log Fart (STORY-01-04-02) treats the device fix as a starting suggestion. When the fix is approximate (Android coarse, or iOS reduced accuracy), the location step says so ("Roughly here. Move the pin if this isn't right.") and offers the pin picker. Submitting is never blocked by it.
3. The permission stays behind `LocationProvider`, so switching to fine later changes only the platform implementation, the manifest and the ask.

## Alternatives
- **A. Add `ACCESS_FINE_LOCATION` with an in-context ask on Log Fart.** The point is accurate without user effort, and quick log stays quick. The cost is a stronger permission and a second prompt (Android 12+ lets the user choose approximate anyway), plus more precise data held, though owner-only.
- **B. Require a pin placement on every Android log.** Accurate, but it slows the under-60-seconds first log (Product.md L1).

## Consequences
- -02 builds the "approximate" hint and the pin picker. It ships with today's permissions, so it doesn't depend on this ADR. Accepting option A would add the fine ask on top.
- Docs to update on acceptance: `create.md` CREATE-03.1 step 1 (the approximate hint), `screens/LogFartScreen.md`, `Security.md` if it lists permissions.
- Revisit if test users often post in the wrong cell (the EPIC-01-04 test plan checks it).

## Owner choice
- **Recommended:** this decision (coarse + hint + pin).
- Or option A (fine location asked in context on Log Fart).
- Or option B (pin required on Android).

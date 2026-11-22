---
name: upgrade-deps
description: Upgrade Kotlin, Compose Multiplatform, AGP, Gradle, Firebase/GitLive, Koin, RevenueCat, Ads or other catalog dependencies safely with a compatibility check and regression pass. Use when asked to update dependencies or when a Renovate/Dependabot PR needs review.
---

# Dependency upgrade

Policy: `planning/architecture/TechStack.md` §4.2 and the prohibited additions in §5.

1. **Plan.** List the current versions in `gradle/libs.versions.toml` and the target versions. Check each maintainer's compatibility statement: Kotlin ↔ CMP ↔ Compose compiler; AGP ↔ Gradle; GitLive ↔ Firebase BoM ↔ Firebase iOS SDK; Koin; RevenueCat KMP. Prefer patch and minor bumps. A major bump needs a written compatibility note in the PR.
2. **One concern per PR** (e.g. the Kotlin + CMP line, or the Firebase line). Never add a competing library for a settled concern.
3. **Change** only the catalog (no per-module versions). For Firebase iOS, update the SPM requirement in Xcode and commit `Package.resolved`.
4. **Regression pass:** `scripts/verify.sh --full`, plus on macOS both schemes. Purchase and ad smoke flows when RevenueCat or Ads changed. Run the app on Android (dev) and check the logcat for startup crashes (the Crashlytics plugin, the GitLive iOS `options` crash; see TechStack §3.1 notes).
5. **Record** the new versions and date in TechStack §3.1, plus any new gotcha in its notes.
6. Open a draft PR with the compatibility notes and the evidence.

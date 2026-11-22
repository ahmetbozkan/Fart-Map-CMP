---
paths:
  - "androidApp/**"
---

# Android shell rules

- The shell stays logic-free: `MainActivity` hosts `App()`, starts Koin with the shared + Android platform modules, and passes `BuildConfig` values (`APP_ENV`, keys) inward. Product logic belongs in `shared`.
- Flavors: `dev` (`applicationIdSuffix ".dev"`, name "Fart Map Dev") and `prod`. Config files live at `androidApp/src/{dev,prod}/google-services.json`. They are injected, untracked, and never read or printed.
- The Maps key comes from untracked `androidApp/secrets.properties` (see the template) into a `BuildConfig` / manifest placeholder. Never hardcode it.
- The Crashlytics Gradle plugin must stay applied while the Crashlytics SDK is on the classpath; without it the app crashes at start (TechStack §3.1 notes).
- Permissions: request location, notifications and microphone **in context** at the moment of use, never at launch. Foreground location only.
- Verify with `./gradlew :androidApp:assembleDevDebug :androidApp:assembleProdDebug :androidApp:lintDevDebug`.

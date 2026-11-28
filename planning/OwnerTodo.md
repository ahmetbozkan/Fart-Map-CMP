# Owner to-do

> The owner's checklist of work only a human can do: console setup, accounts, keys, devices. Each item says why it exists, what it blocks and how to do it. This file is the single list; `STATE.md` points here and keeps only owner *decisions*. Agents add new items here (autopilot-story §1) and never tick them; the owner ticks them, or tells an agent to.
>
> Ids: `dev` = Firebase project `fart-map-dev`, `prod` = `fart-map-1a558`. Android ids: `com.ahmetbozkan.fartmap.dev` (dev flavor) and `com.ahmetbozkan.fartmap` (prod). iOS bundle ids: the same two.

## How the pieces fit (read once)

- **Two Firebase projects, two app variants.** Every console step done for `dev` has a twin for `prod`. So far only `dev` is set up, so `prod` builds compile (CI injects configs) but nothing works against the prod backend yet. Section C is that twin.
- **Config files carry console state.** `google-services.json` and `GoogleService-Info-*.plist` are snapshots: after enabling a sign-in provider or adding a SHA, download them again, or the app keeps the old OAuth client ids. Never commit them. Put them in `~/.fartmap/` (names in the root `README.md`), run `bash scripts/inject-config.sh`, and update the matching GitHub secret (base64) so CI gets the same file:
  ```bash
  base64 -w 0 ~/.fartmap/google-services-dev.json | gh secret set GOOGLE_SERVICES_JSON_DEV   # Git Bash
  base64 -i ~/.fartmap/GoogleService-Info-dev.plist | gh secret set GOOGLE_SERVICE_INFO_PLIST_DEV   # macOS
  ```
- **SHA fingerprints = "which signing keys may use Google sign-in".** Google checks the APK's signing certificate against the SHA-1/SHA-256 you register. Every keystore is a different identity: the Windows debug keystore, the Mac debug keystore, your release upload key, and the Play App Signing key Google holds. Each one that should sign in needs its SHAs on the right Firebase app. Print the ones Gradle knows with `./gradlew :androidApp:signingReport`. A missing SHA shows up as Google sign-in failing with `DEVELOPER_ERROR` (code 10).
- **iOS has no SHA.** Google sign-in on iOS needs the plist's `CLIENT_ID` / `REVERSED_CLIENT_ID`. A build phase copies the reversed id into the app's URL schemes (STORY-01-02-03), so a re-downloaded plist is all it takes.

## A. Now: unblocks the current work

- [x] **CI minutes (issue #17).** Done 2026-11-25: the repo is public. GitHub Free gives a private repo 2,000 Actions minutes a month, and the macOS iOS job counts 10x, so about 26 CI runs use it all. Decide: make the repo public (unlimited free minutes), or trim CI and run iOS on your own Mac as a self-hosted runner. Until then, merge a PR whose `scripts/verify.sh --full` is green locally yourself.
- [x] **Dev catalog seed.** Profile creation on dev is rejected until the catalog exists (`write_rejected` in logs).
  1. `gcloud auth application-default login` (lets the script act as you, locally);
  2. `npm --prefix backend/functions run seed:catalog -- --project fart-map-dev`;
  3. fresh dev install → check that `users/{uid}` appears in the Firestore console.
- [x] **Windows: local dev configs.** Copy the re-downloaded dev configs into `~/.fartmap/` and run `bash scripts/inject-config.sh`, so local builds carry the Google OAuth client. The GitHub secrets only reach CI.

## B. On the Mac: iOS has only been checked by CI

Windows can't build iOS, so nobody has run the iOS app by hand since the shell. CI proves it compiles, not that it works.

- [ ] Put the dev plist (and the prod one, once C is done) in `~/.fartmap/` and run `bash scripts/inject-config.sh`.
- [ ] Run `FartMap Dev` on a simulator. Check:
  - the shell and the age gate;
  - Google sign-in;
  - Profile → Link Google (a guest keeps the same uid).
- [ ] Commit the `Package.resolved` change Xcode makes (GoogleSignIn + FirebaseFunctions pins). It pins the exact iOS dependency versions, like a Gradle lockfile. Commit it in a small PR.
- [ ] Optional: register the **Mac's Android debug SHA-1/SHA-256** on the dev Android app (Firebase → Project settings → Your apps), so Google sign-in also works in Android builds made on the Mac. Then download the dev configs again (see "How the pieces fit").
- [ ] Decide the minimum iOS version (STATE "Pending owner decisions"; the template uses 18.2). It decides which devices can install the app.

## C. Prod parity: before the first prod release

The prod Firebase project exists but has only the basics. Repeat what dev has.

- [ ] **Backend deploy setup on `prod`** (same as dev, `backend/README.md`):
  - Blaze plan;
  - APIs including Cloud Run, Eventarc and Cloud Billing;
  - a deploy service account with the deploy roles, including Service Account User;
  - the three service-agent roles.

  Expect one retry for Eventarc permission propagation on the first deploy.
- [ ] **GitHub `prod` environment:**
  - create it with a **required reviewer** (Settings → Environments), so prod deploys wait for your approval;
  - add its `FIREBASE_SERVICE_ACCOUNT` secret;
  - then run Actions → Deploy backend → Run workflow → `prod` once.
- [ ] **Authentication on `prod`:** enable Anonymous and Google.
- [ ] **Android release signing:**
  - create an upload keystore once (Android Studio → Build → Generate Signed Bundle) and keep it, plus its passwords, outside git with a backup (losing it is painful);
  - register its SHAs on the prod Android app;
  - once the app exists in Play Console with Play App Signing, also register the **App signing key** SHAs from Play Console → Setup → App signing. Store installs are signed with Google's key, not yours.
- [ ] **iOS prod app:** make sure `com.ahmetbozkan.fartmap` is registered in the prod Firebase project.
- [ ] **Download the prod configs again** after the steps above, update `~/.fartmap/` and the `GOOGLE_SERVICES_JSON_PROD` / `GOOGLE_SERVICE_INFO_PLIST_PROD` secrets. CI has older prod files today, enough to compile but without the Google client.
- [ ] **Seed the prod catalog.** The seed script only targets dev and emulators today. Extending it is an EPIC-01-06 code task; running it is yours.
- [ ] **Maps key for prod:** a separate key restricted to `com.ahmetbozkan.fartmap` + the release/Play SHAs (`MAPS_API_KEY_PROD`). See D.

## D. Map epic (EPIC-01-03)

- [x] **Google Maps API key (Android only; iOS uses MapKit, no key):** done for dev (owner, 2026-11-25). The prod key is item C.
  1. Google Cloud console for `dev` → enable "Maps SDK for Android";
  2. create an API key and restrict it to *Android apps*: package `com.ahmetbozkan.fartmap.dev` + your debug SHA-1(s), API = Maps SDK for Android. A restricted key is useless to anyone who pulls it out of the APK.
  3. put it in `androidApp/secrets.properties` as `MAPS_API_KEY_DEV=` (template next to it; never committed).

  Without it the map code still builds; tiles are blank.
- [x] **Dev test farts for the map (STORY-01-03-01).** Needed before the EPIC-01-03 test plan: there's no Log Fart flow yet (EPIC-01-04), so without these the map has no markers.
  1. Seed the catalog first (section A), because the farts use the starter avatars and sounds;
  2. `gcloud auth application-default login` (once), then `npm --prefix backend/functions run seed:farts -- --project fart-map-dev`;
  3. optional: `--center <lat>,<lng>` (default Istanbul) and `--count <n>` (default 30, max 200). Re-running replaces the same `seed_fart_*` docs, so nothing piles up.

  These are PUBLIC farts by a fake author, `seed_author_dev`, around the center. Only `fart-map-dev` and emulator projects are accepted, never prod.

## E. Paid Apple Developer account (99 USD/year): unlocks a group of iOS items

A free account can run the app on a simulator and on your own phone for 7 days. These need the paid account:

- [ ] **Sign in with Apple (Phase 2).** App Store guideline 4.8: an app that offers Google sign-in must also offer Apple's or a similar privacy-focused login. Not needed to build, but it **blocks the iOS store release** (or ship iOS without Google sign-in). Steps:
  - enable the capability on both bundle ids;
  - create a Services ID and a Sign in with Apple key;
  - enter them in Firebase Auth → Apple on dev and prod.
- [ ] **App Check (STORY-01-02-05, deferred, release-blocking per ADR-0015).** App Check proves requests come from your real app, not a script reusing your API keys.
  - **App Attest** (iOS): enable the capability, register in Firebase App Check;
  - **Play Integrity** (Android): needs the app in Play Console, then register it in App Check;
  - **debug tokens:** dev builds print one; add it under App Check → Manage debug tokens.

  Tell the autopilot when done so it picks up -05.
- [ ] **Push (EPIC with notifications):** an APNs auth key (.p8) uploaded to Firebase Cloud Messaging, for dev and prod.
- [ ] **App Store Connect app record + TestFlight** for testers.

## F. Monetization and launch (EPIC-01-06, EPIC-01-10)

- [ ] **Google Play developer account** (25 USD once): app record, internal testing track, Play App Signing (see C), store products for the catalog.
- [ ] **App Store Connect** products for the catalog (needs E).
- [ ] **RevenueCat:** project with the Play and App Store apps, Offerings. Prices are an owner decision (STATE).
- [ ] **AdMob:** app ids for Android and iOS, test rewarded ad units first, server-side verification callback URL (a Cloud Function).
- [ ] **Remote Config template** in dev and prod.
- [ ] **Font files:** Inter and JetBrains Mono (OFL) for `core/design`, or tell an agent to download them.
- [ ] **Domain + landing page** for App Links / Universal Links (EPIC-01-10, ADR-0023/0024).

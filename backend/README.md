# backend

Firebase rules, indexes and Cloud Functions for Fart Map (`planning/architecture/Backend.md` §6a–§8, `DataContracts.md` §5).

- Projects: `dev` = `fart-map-dev` (default alias), `prod` = `fart-map-1a558` (`.firebaserc`).
- Runtime: Cloud Functions 2nd generation, TypeScript, Node.js 22 (`firebase.json`). Install Node 22 LTS locally (macOS `brew install node@22`, Windows `winget install OpenJS.NodeJS.LTS`, or nvm). The emulator suite needs Java 21 on `PATH`/`JAVA_HOME` (`firebase-tools` 15 refuses older JDKs); the same JDK 21 the Gradle daemon uses works.
- Region: `europe-west1` for functions (`functions/src/region.ts`) and for the Firestore database (`firestore.location` in `firebase.json`, used when a deploy has to create the database). Keep the two equal; a database's location can't be changed after creation.

## Commands

```bash
cd backend/functions
npm ci                      # install
npm run build               # type-check and compile to lib/
npm run lint                # eslint
npm test                    # unit + rules tests against the emulator suite (starts the Firestore emulator on a demo project)
```

Deploy (CI does this; locally only with explicit intent):

```bash
cd backend
firebase deploy --only firestore:rules,firestore:indexes,storage -P dev
firebase deploy --only functions -P dev
```

Both projects must be on the Blaze plan before `functions` can be deployed. Service-account credentials for CI live in the GitHub environment secret `FIREBASE_SERVICE_ACCOUNT`, set separately in the `dev` and `prod` environments (`.github/workflows/deploy-backend.yml`); never commit them.

## Layout

- `firestore.rules`, `storage.rules`, `firestore.indexes.json` — default deny; feature stories add rules above the catch-all.
- `functions/src/index.ts` — exported functions; `src/lib/` — idempotency and redacted logging helpers.
- `functions/test/` — vitest; `rules.test.ts` runs against the emulator through `firebase emulators:exec`.

# ADR-0018: Anonymous Accounts Are Full Sessions

- Status: Accepted
- Date: 2026-10-14
- Source: owner decision (closeout questions D1, D2)

## Context
`Scope.md` §4 described a read-only onboarding before sign-up. The PoC allowed anonymous Firebase sessions to create content. `Architecture.md` modeled only Loading / LoggedOut / LoggedIn.

## Decision
1. Sign-in providers are **Google, Apple and Anonymous** only. No email/password.
2. An **anonymous Firebase session is a full session**: the user can log farts, react, report, block, watch rewarded ads and purchase cosmetics.
3. The anonymous uid is persistent on the device. Linking Google or Apple **upgrades the same uid** (Firebase account linking); content, entitlements and settings carry over. The app nudges linking after the first successful log and from the profile; it never blocks.
4. Signing out of an anonymous session requires a confirmation that warns the content will no longer be reachable. Uninstalling loses the anonymous uid; the content remains in the backend under that uid (orphaned, still moderatable).
5. Safeguards: App Check is mandatory (ADR-0015); report and block operate on uids regardless of provider; the age gate (13+, self-declared birth year, stored as an age band) applies to anonymous sessions too.

## Consequences
- `Architecture.md` session states become Loading / SignedOut / Active(anonymous | linked); the start destination for any Active session is the map.
- `Backend.md` §5, `DataContracts.md` (`users.is_anonymous`, `users.linked_providers`, `users.age_band`), `features/` onboarding and `Scope.md` §4 updated.
- The "read-only guest" wording is superseded.

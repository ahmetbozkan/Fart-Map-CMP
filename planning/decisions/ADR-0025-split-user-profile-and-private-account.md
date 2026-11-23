# ADR-0025: Split the user document into a public profile and an owner-only private account

Status: Accepted (owner, 2026-11-22, during STORY-01-01-04)
Date: 2026-11-22

## Context
`users/{userId}` (DataContracts.md §3.1) mixed public profile fields (`display_name`, `avatar_id`, `bio`, counters) with fields that must not be public: `age_band`, `linked_providers`, `is_anonymous`, `is_suspended`. Firestore rules authorize whole documents, never single fields, so any user able to read a profile would also learn whether its owner is a minor. That is a safety risk (ContentSafety.md, minimum age 13, minors protected by default).

## Decision
- `users/{userId}` holds only public profile data: `user_id`, `display_name`, `avatar_id`, `bio`, `signature_style_id`, `follower_count`, `following_count`, `is_following_list_public`, `created_at`, `updated_at`. Readable by any signed-in user; the owner edits only the profile fields; counters are server-written.
- `users/{userId}/private/account` holds `age_band`, `linked_providers`, `is_anonymous`, `is_suspended`, `created_at`, `updated_at`. Readable by the owner only. The owner may create it once with the self-declared `age_band` (immutable afterwards); `linked_providers`, `is_anonymous` and `is_suspended` are server-managed.
- Server code (functions, rules lookups) reads the private doc where it needs the age band or suspension state.

## Alternatives
- One document readable by its owner only: safe, but blocks public profiles and author names on fart details (EPIC-01-05/07).
- One public document: exposes minors. Rejected.

## Consequences
- DataContracts.md §3.1 and §5.5 are updated. EPIC-01-02 writes both documents at sign-up; EPIC-01-07 reads the public one for profiles.
- Rules and emulator tests ship in STORY-01-01-04.

---
name: firestore-change
description: Change the Firestore data model or security rules safely - update DataContracts, rules, indexes, client DTOs and emulator rules tests together. Use when adding a collection or field, changing who can read or write something, or touching firestore.rules / storage.rules / indexes.
---

# Firestore schema / rules change

1. **Contract first.** Update `planning/architecture/DataContracts.md`: §3 collection/field table, §5 rules intent, §9 rules matrix row. Server-only fields are marked as such.
2. **Privacy check (stop and ask if any answer is "no"):**
   - Precise coordinates appear only in `farts/{id}/private/location` (owner-only).
   - Visibility is enforced by rules for Public / Followers / Private.
   - The following list stays private unless the owner opts in (ADR-0012).
   - No PII beyond what DataContracts allows. Age is an age band, never a date of birth.
3. **Rules** in `backend/firestore.rules` (or `storage.rules`):
   - deny by default; helper functions for `isOwner`, `isSignedIn`, `visibleTo(uid)`;
   - validate field types and allowed keys on create and update (`request.resource.data.keys().hasOnly([...])`);
   - reject client writes to server-only fields.
4. **Tests** in `backend/functions/test/`, using `@firebase/rules-unit-testing`:
   - one test per allowed case and per denied case (owner, other signed-in user, anonymous session, signed-out);
   - include an attempt to read the private location as a non-owner.
5. **Indexes** for new composite queries go in `backend/firestore.indexes.json`.
6. **Client side:**
   - DTO + mapper in `firebaseData` round-trip every field (add a test);
   - domain entity default values match the restrictive defaults.
7. **Run:** `cd backend/functions && npm test` (Java 21 on PATH), then `/verify`.
8. **Deploy** happens through CI only. Mention a rules change prominently in the PR body.

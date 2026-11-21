# ADR-0012: Following List Private by Default

- Status: Accepted
- Date: 2026-06-20
- Source: user decision

## Context

The social graph model (ADR-0009) is follow-only with approval. After the graph is established, two distinct pieces of information exist on a user's profile:
- **Follower count** — how many people follow this user.
- **Following list** — the specific list of users this person follows.

The following list is sensitive: it reveals who a user has chosen to maintain a relationship with and can be used for social inference, stalking vectors, or embarrassment. The visibility of this list was left as TBD in the initial social system design.

## Decision

- The **following list** is **private by default**. It is never shown on a public profile or to another user unless the account owner explicitly opts in.
- A user may opt in via a toggle in their profile privacy settings: **"Show who I follow on my profile"**. Default state: off.
- The **follower count** (a single integer — how many approved followers the user has) is **always publicly visible** on the profile. The full follower list (the specific identities) is not publicly exposed.

## Rationale

- A user's choice of who to follow is a form of expressed association. Exposing it by default creates social pressure, stalking risk, and potential embarrassment.
- Follower count is a social credibility signal and is expected to be public (consistent with all comparable social products).
- Opt-in exposure is privacy-safe and aligns with the product's privacy-first-by-default stance.

## Consequences

- `SocialSystems.md` §4 (Follow System) and `ProgressionSystems.md` §10 (TBD-5) reference this ADR.
- `PROFILE-02` (Public Profile System) must implement the following list as hidden by default, with a visible toggle in privacy settings.
- `PRIV-01` (Privacy Controls feature) must include "Show who I follow" as a user-controlled setting.
- The data contract does not change — the following list exists in the database. The change is entirely in read access control and UI.

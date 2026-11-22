# UX Flow Catalog: Fart Map

> Authority: flow-level design mapping and traceability index.
> Upstream: `planning/product/features/`, `planning/product/Product.md`, `planning/design/DesignSpecification.md`.
> Downstream: per-screen specs, Stitch artifacts, UI-related stories/tasks.

---

## 1) Purpose

This catalog maps product features to concrete UX flows and screens so agents can trace implementation work without inventing flow structure.

It owns flow inventory and cross-links.
It does not own full visual specification details (owned by `DesignSpecification.md` and screen specs).

---

## 2) Flow Registry

| Flow ID | Flow name | Primary feature link | Loop link | Journey stage | Primary screens | Phase |
|---|---|---|---|---|---|---|
| UXF-01 | First Open and Entry | MAP-01, CREATE-01 | L1 | 2 -> 3 | Auth/Login, Map | 1 |
| UXF-02 | Quick Log Fart | CREATE-01, CREATE-02 | L1 | 3 | Map, Log Fart | 1 |
| UXF-03 | Detailed Log Fart | CREATE-03, CREATE-04/05/06/07/08 | L1, L8 | 3 -> 4 -> 8 | Log Fart, Map | 1/2 |
| UXF-04 | Marker Detail and React | MAP-02, SOCIAL-01 | L2 | 4 | Map, Fart Detail Sheet | 1 |
| UXF-05 | Follow and Approval | SOCIAL-03 | L4 | 6 | Profile, Notifications | 2 |
| UXF-06 | Feed Catch-up to Map | SOCIAL-04 | L2, L4 | 5 -> 6 | Feed, Map, Profile | 2 |
| UXF-07 | Profile Identity Loop | PROFILE-01/02/03/04 | L5, L8 | 7 -> 8 | Profile, Settings | 2/3 |
| UXF-08 | Safety Controls | PRIV-01/02/03, MOD-01 | Cross-cutting | 3 -> 8 | Log Fart, Detail Sheet, Settings, Profile | 1/2 |
| UXF-09 | Progress and Competition | PROG-01/02/03, ACH-01/02 | L5, L6 | 5 -> 7 | Profile, Leaderboards, Notifications | 3 |
| UXF-10 | Community Events | COMMUNITY-01/02/03 | L7, L8 | 6 -> 8 | Groups, Events, Feed, Map | 4 |
| UXF-11 | Cosmetic Unlock (purchase / rewarded ad) | MON-01, MON-03, CREATE-03.2 | L1, L5 | 3 -> 7 | Log Fart, Profile, Cosmetic Unlock Sheet | 1 |
| UXF-12 | Reaction Push Return | NOTIF-01 (minimal), SOCIAL-01 | L2 | 5 | Push notification, Map, Fart Detail Sheet | 1 |
| UXF-13 | Anonymous Start and Account Linking | ADR-0018, PRIV-03 | L1, L5 | 2 -> 7 | Auth/Login, Map, Profile, Settings | 1 |

---

## 3) Flow Contracts (MVP-Critical)

## UXF-01 First Open and Entry

- Goal: get user to map context quickly with minimal friction
- Must preserve map-first identity and value-before-friction onboarding
- Safety note: permissions requested in context, not as blanket upfront wall

## UXF-02 Quick Log Fart

- Goal: complete map -> log -> marker reward in under 60s first session
- Must include safe default visibility and clear success feedback
- Failure handling: preserve draft or fallback path, do not lose user input

## UXF-03 Detailed Log Fart

- Goal: support expressive creation depth without breaking quick-path speed
- Must follow CREATE-03.1 interaction contract
- Monetization note: locked cosmetic interactions follow CREATE-03.2

## UXF-04 Marker Detail and React

- Goal: convert creation into social reward loop
- Must make reaction action one-tap and visible
- Safety note: report/block access surfaced where relevant

## UXF-11 Cosmetic Unlock

- Goal: let a user buy (permanent) or try once (rewarded ad) a locked avatar or sound without leaving the creation or profile flow
- Must follow CREATE-03.2: sheet shows exactly Buy + Watch Ad to Try Once in Phase 1; price from the store; never an automatic paywall
- Safety/trust note: the unlocked state appears only after the server-mirrored entitlement or token exists; a failed or cancelled ad or purchase closes the sheet with no error beyond a humor-consistent note

## UXF-12 Reaction Push Return

- Goal: bring the author back to the exact marker after a reaction
- Must request notification permission in context (after the first reaction received), never at launch
- Deep link `fartmap://fart/{fartId}` opens the Map tab with the Fart Detail Sheet; payload carries no private text or coordinates

## UXF-13 Anonymous Start and Account Linking

- Goal: zero-friction first session; preserve everything when the user links Google or Apple later
- Must present "Continue without account" next to the providers; after the first successful log show the link nudge once; Profile and Rank carry a persistent link card for anonymous sessions
- Linking keeps the uid, content, entitlements and settings (ADR-0018); signing out of an anonymous session warns about loss

## UXF-08 Safety Controls

- Goal: enforce privacy-safe defaults and user protection UX controls
- Must include: visibility selector, coarse location display, block/report paths
- This flow is mandatory in any social/location surface touching user-generated content

---

## 4) Stitch Handoff Mapping Fields

For each flow represented in Stitch, record:

- `stitch_artifact_id`
- `stitch_version`
- `mapped_flow_id`
- `mapped_screen_ids`
- `state_coverage` (loading/empty/error/success)
- `platform_notes`
- `handoff_status` (draft/ready)

---

## 5) Validation Checklist

- [ ] Every active phase feature has at least one mapped UX flow
- [ ] Every mapped flow links to loop and journey stage
- [ ] MVP-critical flows (UXF-01/02/03/04/08/11/12/13) are fully specified
- [ ] Safety/privacy controls are represented in relevant flows
- [ ] Stitch mappings include versioned artifact metadata when used

---

## 6) AI Usage Guidance

1. Start UI story decomposition from this flow registry, not from ad-hoc screen guesses.
2. If a feature lacks a mapped flow, add flow mapping before planning stories.
3. Keep flow contracts short and traceable; detailed visuals belong in screen specs.

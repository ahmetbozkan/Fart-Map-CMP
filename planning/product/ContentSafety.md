# Content Safety: Fart Map

> **Planning spine position:** cross-cutting safety/privacy governance; constrains all social, map, and identity features.
> Upstream: `Vision.md`, `ProductPillars.md`, `Personas.md`, `UserMotivations.md`, `UserJourney.md`, `CoreLoops.md`, `Glossary.md`. Downstream: `Features.md`, `SocialSystems.md`, `architecture/DataModel.md`, `architecture/DataContracts.md`, `architecture/SecurityAndSecrets.md`, `design/*`.
> A viral, **location-based, social, user-generated-content** app carries real-world harm potential (doxxing, harassment, stalking, minor-safety, NSFW). This document encodes the non-negotiable safety, privacy, and moderation policy that features must obey **before** they are designed (per `meta/archive/PlanningStructureReview.md` §10).

---

## 1) Purpose & How to Use

Fart Map combines the three highest-risk ingredients in consumer apps: **precise location + open social interaction + user-generated humor**. Humor culture can mask harassment; location can enable stalking; virality amplifies harm fast. This document is the **safety constitution**: it defines what the product must protect, what it must never enable, and what every social/map/identity feature must implement.

AI agents and planners use this document to answer:
- Does this feature create a doxxing/stalking/harassment vector?
- What safety controls must ship *with* this feature (not later)?
- How is location exposed, and is it privacy-safe by default?
- What is the moderation path for content this feature creates?

**Rules:**
- Safety is a **hard gate**, not a feature. A social/map/identity feature without its required safety controls is **incomplete** and must not ship.
- When safety conflicts with any other goal (humor, growth, engagement), **safety and trust win** (Vision §17; Pillars §5; Personas §7 "Humor chaos vs Trust & safety → preserve humor; reject mechanics enabling harassment").
- **Privacy-safe by default**: the safe option is the default; users opt *into* exposure, never out of protection.
- This document governs **policy**; enforcement mechanisms live in `architecture/SecurityAndSecrets.md`, `DataContracts.md` (Firestore security rules), and moderation tooling.

---

## 2) Threat Model (What Can Go Wrong)

| # | Threat | Vector | Severity |
|---|---|---|---|
| T1 | **Doxxing / location exposure** | Precise Fart location reveals home/work/routine | **Critical** |
| T2 | **Stalking / real-world following** | Location History + real-time Nearby reveals movement patterns | **Critical** |
| T3 | **Harassment / targeted abuse** | Comments, Reactions, callouts, rivalry weaponized | High |
| T4 | **Minor safety** | Underage users exposed to adults / unsafe contact | **Critical** |
| T5 | **NSFW / illegal content** | Captions, images, profile content | High |
| T6 | **Spam / manipulation** | Spam-grinder farts, fake engagement, invite spam | Medium |
| T7 | **Impersonation / identity abuse** | Fake profiles, stolen Signatures/identity | Medium |
| T8 | **Sensitive-location misuse** | Logging at schools, hospitals, private homes, conflict zones | High |
| T9 | **Data breach / secret leak** | Exposed keys/config (PoC committed Maps key + plist — Audit §5) | **Critical** |

This threat model is the checklist every feature is audited against.

---

## 3) Location Privacy (The Highest-Risk Surface)

Location is the product's core (Pillars §A) **and** its biggest danger (T1, T2, T8). The product must make geography fun without making users trackable.

### Mandatory location rules
| Rule | Requirement |
|---|---|
| **Precision fuzzing** | Public Farts must NOT expose exact coordinates. Snap/jitter to a coarse area (e.g., neighborhood-level) for public visibility. Exact coordinates are private to the author. **Decision (ADR-0019):** precise coordinates live in an owner-only subdocument; the public point is the geohash precision-6 cell center plus deterministic jitter. Parameters confirmed by the owner on 2026-10-15. |
| **No real-time presence** | Markers must not broadcast "user is here *right now*" in a way that enables interception. **Decision (ADR-0019, confirmed 2026-10-15):** no publication delay; public views show relative time at hour granularity and no "live now" indicator. Delay/aggregation may be introduced later only as a privacy-default change with a superseding ADR. |
| **Home/sensitive-zone protection** | Detect and protect frequently-logged private locations (likely home/work). Offer a "private zone" the user can blur/exclude. |
| **Sensitive-place guardrails** | Discourage/limit logging at schools, hospitals, places of worship, private residences (T8). |
| **History is private by default** | Location History / Map Memory (`Glossary.md`) is the user's private record; public exposure is opt-in and coarse. |
| **Analytics carry no precise location** | Telemetry payloads must never include exact coordinates (hand-off to `Observability.md`). |

### Visibility model (critical PoC lesson)
The PoC **dropped the Visibility field** on persistence (every Fart became PRIVATE — Audit §6). The new system must:
- **Faithfully round-trip Visibility** through the data model (`DataModel.md`).
- Make **Visibility explicit at log time**, with the privacy-safest tier as default.
- Use fixed MVP visibility tiers: **Public / Followers / Private**. Public visibility implies location fuzzing (above).

---

## 4) Social Safety (Harassment, Abuse, Rivalry)

Fart Map *wants* friendly competition and callouts (Loops L2/L6, Motivation M6) — but the line between banter and harassment must be enforced.

| Control | Requirement | Ships with |
|---|---|---|
| **Block** | Any user can block any user (removes visibility/interaction both ways) | Any social feature (L2/L4) |
| **Report** | Report Farts, Comments, profiles, users; routed to moderation | Any UGC feature |
| **Mute** | Silence a user/thread without blocking | Comments/notifications |
| **Anti-harassment in rivalry** | Competitive callouts must not enable targeted pile-ons; rate-limit and dampen abuse vectors | L6 Competition |
| **Comment controls** | Authors can limit/disable comments on their Farts | Comments feature |
| **Notification safety** | No mechanism that lets a user spam-notify or "ping-bomb" another | Notifications |

**Rule:** **no social feature ships without Block + Report.** These are the floor, not enhancements.

---

## 5) Minor Safety (T4 — Zero-Tolerance Surface)

Location + social + minors is the most dangerous combination and carries legal weight (COPPA/GDPR-K and equivalents).

| Requirement | Rule |
|---|---|
| **Age gating** | Minimum age **13**. Self-declared birth year at first session, including anonymous sessions (ADR-0018); under-13 is blocked; only an age band is stored (`DataContracts.md` users.age_band). Owner decision 2026-10-14. |
| **Default protection for minors** | If minors are permitted, their default Visibility, discoverability, and location precision are the most restrictive. |
| **No adult→minor contact vectors** | Limit how unknown adults can discover/contact younger users. |
| **Escalation path** | Child-safety reports are highest priority and may require legal/authority escalation. |

**Rule:** any feature that could expose minors to location or stranger contact requires explicit minor-safety review and is an **escalation trigger** for human/legal review (ties to future `AgentGuardrails.md`). Agents must not autonomously ship minor-affecting changes.

---

## 6) Content Moderation (UGC Governance)

Every concept in `Glossary.md` that users author (Fart, Caption, Comment, Signature, Profile, images) is moderatable content.

### Moderation model
| Layer | Mechanism |
|---|---|
| **Prevention** | Input constraints, banned-term filters, image checks (where images exist), rate limits |
| **Detection** | User reports (T3/T5) + automated signals (spam, abuse, NSFW) |
| **Response** | Content removal, warnings, temporary restrictions, bans |
| **Appeal** | A path to contest moderation actions (fairness/trust) |
| **Audit** | Moderation actions are logged for consistency and accountability |

### Content policy lines (what is never allowed)
- Sexual content involving minors (zero tolerance, legal escalation).
- Targeted harassment, threats, hate speech.
- Doxxing / sharing others' private information or precise location.
- Illegal content/activity coordination.
- NSFW content outside policy (define the explicit line before image features ship).

**Humor boundary:** crude/absurd humor is **welcome** (it's the product); content that targets, endangers, or harasses real people is **not**. The distinction: *laughing with the absurd* vs. *weaponizing content against a person*.

---

## 7) Anti-Abuse & Fairness (Protecting Progression Integrity)

Ties to the Rival persona, Loop L6, and the Spam-Grinder anti-persona (`Personas.md` §3).

| Abuse | Control |
|---|---|
| Spam logging to farm Stats/Rank (T6) | Rate limits; quality/duplicate detection; meaningful-participation scoring (Progress pillar guardrail) |
| Fake engagement (reaction/comment rings) | Anomaly detection; weight authentic interaction |
| Location spoofing (fake-GPS to claim places/Territory) | Plausibility checks on location/movement; flag impossible travel |
| Pay-to-win | Hard product rule: monetization is cosmetic/additive only (Personas §7) — enforced in `Monetization.md` |
| Invite spam (T6) | Rate-limit invites; detect low-quality install churn (`SuccessMetrics.md` §8) |

**Rule:** if **Intensity** or any author-declared value feeds Rank/Progress (resolved: 0% weight, `ProgressionSystems.md` §10), it **requires anti-abuse before shipping**. Default: keep such values humor-only until a fair, abuse-resistant mechanic exists.

---

## 8) Data Privacy & Compliance

| Requirement | Rule |
|---|---|
| **Data minimization** | Collect only what features need; precise location stored privately, exposed coarsely |
| **User data rights** | Support account deletion and data export (GDPR/CCPA-style); deletion removes/anonymizes user content |
| **Consent** | Explicit, in-context consent for location and notifications (don't front-load permission walls — `UserJourney.md` Stage 2) |
| **Secret hygiene** | No secrets in VCS. The PoC committed a Maps API key and `GoogleService-Info.plist` (Audit §5, T9) — the new project uses untracked config + CI injection (`SecurityAndSecrets.md`) |
| **Firestore security rules** | Backend access governed by security rules enforcing Visibility, ownership, and minor protections (`DataContracts.md`) |
| **Third-party data** | GitLive Firebase usage must respect the same privacy posture across Android/iOS |

---

## 9) Safety Requirements by Feature Class

A lookup an agent uses when specifying a feature: "what safety controls are mandatory for this *kind* of feature?"

| Feature class | Mandatory safety controls |
|---|---|
| **Logging a Fart (L1)** | Explicit Visibility (safe default), location fuzzing for public, sensitive-zone guardrails |
| **Map / Nearby (L3)** | Coarse public locations, no real-time interception, private-zone protection |
| **Reactions/Comments (L2)** | Block, Report, Mute, comment controls |
| **Follow requests (L4)** | Block, privacy-aware discovery, minor protections, no forced exposure, explicit follow-approval flow |
| **Profile/Identity (S7)** | Impersonation protection, report profile, control over public history |
| **Rank/Competition (L6)** | Anti-abuse, anti-spoofing, anti-pile-on, fairness |
| **Community/Events (L7)** | Group moderation roles, report-in-group, event abuse limits |
| **Share (L8)** | No precise location in shared artifacts; no exposure of others without consent |
| **Notifications** | No ping-bombing; opt-out respected |

---

## 10) Safety as a Phase Constraint

Safety scales with social surface area. It is not deferrable to "later."

| Phase | Minimum safety baseline |
|---|---|
| **1 Core Loop** | Location fuzzing (ADR-0019) + Visibility round-trip + secret hygiene + Report and Block + age gate + App Check + server-side entitlement checks for purchased/ad-unlocked cosmetics (ADR-0015/0016/0017) |
| **2 Social Retention** | Block + Report + Mute fully live; minor-safety in discovery; notification safety |
| **3 Progression** | Anti-abuse/anti-spoofing for Rank; appeal path |
| **4 Community** | Group moderation; escalation tooling; scaled moderation throughput |

**Rule:** a phase cannot be considered done if its safety baseline is unmet — even if its feature metrics are green.

---

## 11) AI Decision Guidance (Mandatory for Autonomous Planning)

1. Audit every social/map/identity feature against the **threat model** (§2) before specifying it.
2. Treat **Block + Report** as the floor for any social feature; **location fuzzing + explicit Visibility** as the floor for any public map feature.
3. **Privacy-safe by default** — the safe option is the default; exposure is opt-in.
4. When safety conflicts with humor/growth/engagement, **safety wins** — redesign or reject.
5. **Escalate, do not autonomously ship**, anything affecting **minor safety** (T4), **legal/compliance**, or **precise-location exposure** — these are human-review triggers (ties to `AgentGuardrails.md`).
6. Never let author-declared values (Intensity) feed Rank without anti-abuse; default to humor-only.
7. Enforce the **PoC lessons**: Visibility must round-trip; no secrets in VCS.
8. Ensure each phase's **safety baseline** (§10) is met before the phase is "done."
9. Every safety control implies a **counter-metric** in `SuccessMetrics.md` §8 (report/block/abuse rates) — instrument them.

---

## 12) Safety Usage Checklist (Apply per Epic / Feature)

- [ ] Which threats (T1–T9) does this feature touch?
- [ ] Required safety controls for its feature class (§9) included?
- [ ] Location exposed? If so, fuzzed + explicit Visibility + safe default?
- [ ] Block/Report/Mute present (if social)?
- [ ] Minor-safety reviewed (if discovery/contact/location)?
- [ ] Moderation path defined for any UGC it creates?
- [ ] Anti-abuse present (if it feeds Stats/Rank/Territory)?
- [ ] Counter-metrics instrumented (`SuccessMetrics.md` §8)?
- [ ] Any escalation trigger (minor/legal/precise-location)? If yes, flag for human review.

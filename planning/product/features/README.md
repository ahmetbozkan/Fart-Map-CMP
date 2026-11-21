# Features

One file per system. Each feature keeps: purpose, phase, dependencies, behavior/acceptance where specified, open questions.

- [Map Systems](map.md)
- [Content Creation Systems](create.md)
- [Social Systems](social.md)
- [Profile Systems](profile.md)
- [Progression Systems](progression.md)
- [Achievement Systems](achievements.md)
- [Sharing Systems](share.md)
- [Monetization Systems](monetization.md)
- [Notification Systems](notifications.md)
- [Community Systems](community.md)
- [Privacy Systems](privacy.md)
- [Moderation Systems](moderation.md)

## Feature Dependencies and Phase Logic

### Core dependency spine
- `MAP-01`, `MAP-02`, `MAP-03`, `CREATE-01`–`CREATE-06`, `CREATE-09` (delete), `SOCIAL-01`, `PROFILE-01`, `PROFILE-02` (light), `PRIV-01`, `PRIV-02`, `PRIV-03` (baseline), `MOD-01`, `MOD-02` (baseline), `MON-01` (a la carte), `MON-03`, `NOTIF-01` (minimal) are the **Phase 1 kernel** (ADR-0020).
- `SOCIAL-03`, `SOCIAL-04`, `NOTIF-01`, `NOTIF-02`, `SHARE-01`, `SHARE-02` are the **Phase 2 retention/virality layer**.
- `PROG-01`, `PROG-02`, `PROG-03`, `ACH-01`, `PROFILE-03`, `PROFILE-04`, `PROG-05` are the **Phase 3 depth layer**.
- `COMMUNITY-01`, `COMMUNITY-02`, `COMMUNITY-03`, `MOD-04` are the **Phase 4 network-effect layer**.

### Hard sequencing rules
1. Do not deepen progression before Activation and First Reward are healthy.
2. Do not ship competitive ranking without fairness/anti-abuse.
3. Do not build community loops before follower graph and notification loops are working.
4. Do not add monetization that weakens humor, fairness, or trust.
5. Do not let feed/social surfaces weaken map centrality.

---

## Open Decisions / TBD Register

These do not block the feature specification itself, but they do affect future system detail documents.

### Product-level TBDs
1. **Ranking model** — ✅ Resolved. Signal weights: 50% XP / 20% Reactions / 15% Streak / 10% Challenges / 0% Intensity (mocked). See `ProgressionSystems.md` §4 and §10.

2. **Intensity weighting in progression** — ✅ Resolved. Intensity is mocked at 0% for MVP in both XP and ranking. Field is captured and stored. No weight until a future decision introduces it with abuse controls. See `ProgressionSystems.md` §10.

3. **Signature composition model** — ✅ Resolved. Single style selection. No composable parts in MVP. User picks one complete pre-composed signature from a catalog. Composable parts are a future expansion only. See ADR-0013.

4. **Audio recording constraints** — ✅ Resolved. Max duration = 10 seconds (hard limit). MVP scope: record, attach, play on marker tap. No editing, no trimming, no effects, no external share by default. Async post-write moderation. See ADR-0014.

5. **Following list visibility** — ✅ Resolved. Following list is private by default. Only follower count is publicly visible. User may opt in via profile privacy setting. See ADR-0012.

### Deferred items
6. **Territory** is explicitly post-MVP only.

### Phase decisions
7. **Phase 1 scope additions** (a la carte purchases, rewarded ads, reaction push, delete, all filters, four-tab navigation) — ✅ Resolved by owner decision 2026-10-14. See ADR-0020.

---

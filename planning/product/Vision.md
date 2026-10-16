# Vision: Fart Map

> **Planning spine position:** root of the product dependency graph.
> Upstream: none. Downstream: `ProductPillars.md` → `Personas.md` → `UserMotivations.md` → `UserJourney.md` → `CoreLoops.md` → `Features.md`.
> This document is the first decision filter. If anything below contradicts it, this wins.

## 1. Why This Product Exists
Most social apps optimize for polished highlights. Real life is messier, more spontaneous, and often funnier in small moments.
Fart Map exists to transform mundane everyday moments into shared social experiences.

It turns silly local moments into:
- laughter,
- interaction,
- identity,
- progress,
- and shareable stories.

This is the primary reason the product should exist, and the first filter for future decisions.

## 2. Product Vision
Fart Map becomes the world's funniest social map: a playful place where everyday moments turn into shared stories, friendly competition, and internet-native culture.
It starts as a joke users instantly understand, then grows into a long-term social entertainment platform with real retention loops.

## 3. Product Mission
Make people laugh together, every day, through location-based social play.
We turn "I should post this" moments into map moments: fast to create, fun to react to, and rewarding to revisit.

## 4. Product Pillars
Every feature should strengthen at least one pillar. Strong features strengthen multiple pillars.

1. **Location**
   The world is the game board. Meaningful moments happen somewhere.

2. **Social**
   The experience gets better when shared, reacted to, and discussed.

3. **Humor**
   The product consistently creates moments worth laughing about.

4. **Progress**
   Users should feel they are building something over time.

5. **Identity**
   Users should develop a recognizable profile and reputation.

## 5. Core User Fantasy
"I can leave my mark on the world, get seen, and build my own ridiculous legend."

Users feel like they are:
- part of a global inside joke,
- building a visible identity through map activity,
- competing with friends and strangers,
- collecting proof of consistency, creativity, and chaos.

## 6. Core User Loop

> The numbered loop registry is `CoreLoops.md` (L1–L8); this section is the narrative summary and defines no loop ids.
1. Open app
2. Explore map
3. Log fart
4. Marker appears immediately
5. Interact with others
6. Earn achievements and stats
7. Share funny moments
8. Return to maintain streaks and rankings

## 7. Emotional Value
Fart Map delivers:
- Instant humor: low-friction laughs in seconds.
- Light relief: a playful break from serious feeds.
- Self-expression: personality through style, comments, reactions, and profile identity.
- Progress pride: visible growth through streaks, rank, and achievements.

## 8. Social Value
Fart Map creates social glue through:
- shared map context ("this happened here"),
- group dynamics (followers, rivalries, challenges, team goals),
- conversation triggers (comments, reactions, remixes, callouts),
- status signals (leaderboards, badges, consistency; territory is a Phase 5 candidate only, ADR-0011).

## 9. Why Users Return
Users return because the product repeatedly answers:
- "What happened near me?"
- "Did anyone react to my post?"
- "Can I keep my streak alive?"
- "Can I climb higher today?"
- "Did the people I follow beat me?"

Retention is driven by novelty + progress + social pull.

## 10. Why Users Share
Users share Fart Map moments because they are:
- funny enough to send instantly,
- easy to understand out of context,
- identity-building ("this is so me"),
- social currency in group chats and short-form platforms,
- naturally screenshot-worthy and meme-ready.

## 11. Viral Design Philosophy
Fart Map is built to spread through user behavior, not explanation.
Every major feature should satisfy at least one viral outcome:

- Creates screenshot moments.
- Creates stories worth sharing.
- Creates friendly competition.
- Creates social bragging rights.
- Creates curiosity ("what is this app?").

If a feature cannot produce stories, interaction, or sharing behavior, it should be questioned.

## 12. Differentiation From Poop Map
Fart Map is not only a logging utility; it is a social entertainment system.

- More expressive: personality, style, reactions, humor formats.
- More social: following with approval (ADR-0009), group loops, comment culture.
- More game-like: streaks, achievements, rankings, challenges (territory deferred, ADR-0011).
- More share-native: moments designed for virality, not just record-keeping.
- More alive: reasons to return even when not posting.

## 13. What Fart Map Is Not
Fart Map is not:
- a serious social network,
- a dating platform,
- a utility application,
- a messaging app,
- a health tracker.

The goal is entertainment first.
All future features should support entertainment, humor, exploration, identity, or social interaction.

## 14. Long-Term Product Direction
Fart Map evolves in layers:

1. **Core Loop Strength**
   Map exploration, instant logging, basic social feedback.

2. **Social Graph Depth**
   Following graph, richer profile identity, stronger feed dynamics.

3. **Competitive Systems**
   Leaderboards, streak mastery, seasonal challenges (territory only if ADR-0011 is superseded).

4. **Community Systems**
   Groups, themed events, collaborative missions, shared goals.

5. **Monetization Without Killing Fun**
   Premium personalization, optional subscriptions, reward-based extras.

## 15. Long-Term Scale Vision
The long-term vision is a playful location-based social platform where users:
- build history,
- build identity,
- build reputation,
- build rivalries,
- build communities,

through a shared map of ridiculous moments.

## 16. Product Success Criteria
Fart Map is successful when:

- New users understand the concept and first action within minutes.
- A meaningful share of users complete the map -> log -> react loop in week one.
- Retention is driven by social and progression loops, not only novelty.
- Users organically share moments outside the app.
- Followed users and challenges measurably increase repeat sessions.
- Monetization feels additive, optional, and never pay-to-win.
- The product remains simple to explain, even as depth increases.

## 17. AI Product Decision Guidelines
When multiple solutions exist, AI agents must prioritize in this order:

1. Preserve product vision first.
2. Preserve user enjoyment second.
3. Preserve long-term maintainability third.
4. Optimize development speed fourth.

Never sacrifice product vision solely to reduce implementation effort.

Additional guardrails:
- Every major feature must strengthen at least one Product Pillar.
- Prefer features that increase social interaction and replayability.
- Reject features that drift toward generic social networking.
- Keep onboarding simple, but never flatten product personality.
- Reward creativity and consistency, not spam.
- Protect trust: users should feel safe, respected, and in control.

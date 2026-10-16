# ADR-0014: Audio Recording Constraints — 10 Seconds, MVP Simple

- Status: Accepted
- Date: 2026-06-20
- Source: user decision

## Context

The Audio Recording System (CREATE-08) required decisions on three open questions: maximum clip duration, moderation workflow, and whether audio is shareable externally by default.

## Decision

1. **Maximum duration: 10 seconds.** Hard limit. No override, no premium extension in MVP.
2. **MVP scope is minimal by design:**
   - Record → attach to fart → play on marker tap.
   - No in-app editing, trimming, or effects.
   - No waveform visualization beyond a basic playback indicator.
   - No external share of the audio clip by default (audio is tied to the fart; sharing the fart shares the marker, not a raw audio file).
3. **Moderation: async post-write.** Audio is accepted at upload time and screened asynchronously. This is the same pattern as comments. Synchronous blocking moderation is not required for MVP.

## Rationale

- Shorter clips reduce storage cost, moderation surface area, and abuse potential.
- 10 seconds is enough for a genuine audio moment (reaction, ambient sound, spoken one-liner) without enabling extended content.
- Keeping MVP scope to record-attach-play avoids building a mini audio editor. Complexity can be added if user demand warrants it.
- Async moderation is consistent with the existing UGC moderation approach and does not block the creation flow.

## Consequences

- `Features.md` CREATE-08 TBD note updated.
- `Features.md` CREATE-03.1 Step 8 voice recording duration updated from TBD to 10 seconds.
- `Features.md` CREATE-03.2 voice recording row updated.
- `Features.md` §19 item 4 marked resolved.
- Implementation must enforce the 10-second limit server-side at upload time, not only client-side.
- The `voice_url` field in the Fart entity (CREATE-03.3) stores the remote URL of the uploaded clip after server-side acceptance. No duration metadata needs to be stored separately in MVP.
- Audio is not independently shareable as a raw file. Sharing always goes through the fart sharing surface (SHARE-01–04).

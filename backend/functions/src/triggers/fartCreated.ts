import { FieldValue, type Firestore } from "firebase-admin/firestore";
import { runOnce, triggerKey } from "../lib/idempotency";
import { projectPublicLocation } from "../lib/location";
import { logError, logInfo } from "../lib/log";

/** Same tolerance as the Kotlin ↔ TS fuzz vectors (`test/fixtures/location-fuzz-vectors.json`). */
const COORD_EPSILON = 1e-9;

type SkipReason = "fart_missing" | "location_missing" | "location_invalid";

function validPoint(point: { lat?: unknown; lng?: unknown }): point is { lat: number; lng: number } {
  const { lat, lng } = point;
  return typeof lat === "number" && typeof lng === "number"
    && Number.isFinite(lat) && Number.isFinite(lng)
    && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
}

/**
 * Handler behind `onFartCreated`: re-validates the public projection (ADR-0019 §4). The client computes the fuzzed
 * `public_lat`/`public_lng`/`coarse_location_key` at write time; this recomputes them from the owner-only
 * `private/location` (the rules require it in the creating batch) and overwrites the public fields on any mismatch.
 *
 * - Read or write failures propagate: `runOnce` releases the claim and the trigger (deployed with `retry: true`) retries.
 * - A missing fart or a missing/invalid private location can only come from Admin SDK writes; it is logged as an error
 *   and nothing is written.
 * - Logs carry the fart id and outcome only, never coordinates.
 */
export async function handleFartCreated(db: Firestore, eventId: string, fartId: string): Promise<"ran" | "skipped"> {
  const result = await runOnce(db, triggerKey("onFartCreated", eventId), async () => {
    const fartRef = db.doc(`farts/${fartId}`);
    const [fart, location] = await Promise.all([fartRef.get(), fartRef.collection("private").doc("location").get()]);

    const skip = (reason: SkipReason) => logError("fart_projection_skipped", { fartId, reason });
    if (!fart.exists) return skip("fart_missing");
    if (!location.exists) return skip("location_missing");
    const point = location.data() ?? {};
    if (!validPoint(point)) return skip("location_invalid");

    const expected = projectPublicLocation(fartId, point.lat, point.lng);
    const stored = fart.data() ?? {};
    const matches = stored.coarse_location_key === expected.coarse_location_key
      && typeof stored.public_lat === "number" && Math.abs(stored.public_lat - expected.public_lat) < COORD_EPSILON
      && typeof stored.public_lng === "number" && Math.abs(stored.public_lng - expected.public_lng) < COORD_EPSILON;

    if (!matches) {
      await fartRef.update({
        public_lat: expected.public_lat,
        public_lng: expected.public_lng,
        coarse_location_key: expected.coarse_location_key,
        updated_at: FieldValue.serverTimestamp(),
      });
    }
    logInfo("fart_projection_checked", { fartId, corrected: !matches });
  });
  logInfo("onFartCreated_done", { fartId, result });
  return result;
}

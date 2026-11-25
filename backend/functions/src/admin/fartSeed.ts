import { projectPublicLocation } from "../lib/location";

/**
 * Dev-only test farts for the map (STORY-01-03-01). Written with the Admin SDK, which skips the rules: the seed
 * author has no account doc, so these PUBLIC docs bypass the 18+ check on purpose (ADR-0023). They are not
 * evidence for the client create path; the emulator test proves their shape passes `allow create` separately.
 */
export const SEED_AUTHOR_ID = "seed_author_dev";
/** Each fart is 2 writes in one batch; Firestore caps a batch at 500, so this must stay ≤ 250. */
export const MAX_SEED_FARTS = 200;
export const DEFAULT_SEED_COUNT = 30;
/** Istanbul, the same area the backend test helpers use. */
export const DEFAULT_SEED_CENTER = { lat: 41.0082, lng: 28.9784 };

const SPREAD_KM = 5;
const KM_PER_DEGREE_LAT = 111.32;
const WEEK_MS = 7 * 24 * 60 * 60 * 1000;
const INTENSITIES = [0.15, 0.35, 0.55, 0.75, 0.95]; // never 0 or 1: Node would store an integer (§3.2 says Float)

export interface FartSeedOptions {
  count: number;
  center: { lat: number; lng: number };
  nowMs: number;
  avatars: string[];
  sounds: string[];
}

export function seedFartId(index: number): string {
  return `seed_fart_${String(index + 1).padStart(3, "0")}`;
}

/** Spreads points on a golden-angle spiral up to [SPREAD_KM] from the centre; no randomness, so locations repeat. */
function spiralPoint(index: number, count: number, center: { lat: number; lng: number }): { lat: number; lng: number } {
  const radiusKm = SPREAD_KM * Math.sqrt((index + 0.5) / count);
  const angle = index * 2.399963229728653;
  const lat = center.lat + (radiusKm * Math.cos(angle)) / KM_PER_DEGREE_LAT;
  const kmPerDegreeLng = KM_PER_DEGREE_LAT * Math.max(Math.cos((center.lat * Math.PI) / 180), 0.01);
  const rawLng = center.lng + (radiusKm * Math.sin(angle)) / kmPerDegreeLng;
  const lng = ((((rawLng + 180) % 360) + 360) % 360) - 180;
  return { lat: Math.max(-90, Math.min(90, lat)), lng };
}

/**
 * Firestore docs keyed by path: `farts/{id}` with the full DataContracts.md §3.2 create field set and fuzzed public
 * fields, plus `farts/{id}/private/location` with the precise point. [serverTs] stamps `created_at`/`updated_at`.
 */
export function fartSeedDocs<T>(options: FartSeedOptions, serverTs: T): Record<string, Record<string, unknown>> {
  const { count, center, nowMs, avatars, sounds } = options;
  if (!Number.isInteger(count) || count < 1 || count > MAX_SEED_FARTS) {
    throw new Error(`seed:farts --count must be 1..${MAX_SEED_FARTS}`);
  }
  if (!Number.isFinite(center.lat) || center.lat < -85 || center.lat > 85) {
    throw new Error("seed:farts --center latitude must be within -85..85");
  }
  if (!Number.isFinite(center.lng) || center.lng < -180 || center.lng > 180) {
    throw new Error("seed:farts --center longitude must be within -180..180");
  }
  if (avatars.length === 0 || sounds.length === 0) throw new Error("seed:farts needs starter avatars and sounds");

  const docs: Record<string, Record<string, unknown>> = {};
  for (let index = 0; index < count; index++) {
    const fartId = seedFartId(index);
    const precise = spiralPoint(index, count, center);
    const fart: Record<string, unknown> = {
      fart_id: fartId,
      author_user_id: SEED_AUTHOR_ID,
      ...projectPublicLocation(fartId, precise.lat, precise.lng),
      timestamp_epoch_ms: Math.round(nowMs - (WEEK_MS * (index + 1)) / (count + 1)),
      avatar_id: avatars[index % avatars.length],
      flavor_level: (index % 5) + 1,
      intensity_value: INTENSITIES[index % INTENSITIES.length],
      visibility_mode: "PUBLIC",
      reaction_count: 0,
      reaction_breakdown: {},
      comment_count: 0,
      report_count: 0,
      is_hidden: false,
      is_removed: false,
      created_at: serverTs,
      updated_at: serverTs,
    };
    if (index % 3 === 0) fart.sound_id = sounds[index % sounds.length];
    docs[`farts/${fartId}`] = fart;
    docs[`farts/${fartId}/private/location`] = { lat: precise.lat, lng: precise.lng, updated_at: serverTs };
  }
  return docs;
}

/** Parses `--center <lat>,<lng>`; throws on anything else. */
export function parseCenter(value: string | undefined): { lat: number; lng: number } {
  if (value === undefined) return DEFAULT_SEED_CENTER;
  const parts = value.split(",").map((part) => Number(part.trim()));
  if (parts.length !== 2 || parts.some((part) => !Number.isFinite(part))) {
    throw new Error("seed:farts --center must be <lat>,<lng>");
  }
  return { lat: parts[0], lng: parts[1] };
}

/** Parses `--count <n>`; the range is checked by [fartSeedDocs]. */
export function parseCount(value: string | undefined): number {
  if (value === undefined) return DEFAULT_SEED_COUNT;
  const count = Number(value);
  if (!Number.isInteger(count)) throw new Error("seed:farts --count must be an integer");
  return count;
}

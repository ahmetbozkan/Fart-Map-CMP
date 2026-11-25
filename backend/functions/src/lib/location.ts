// Public location projection (ADR-0019 §3), the TypeScript twin of the Kotlin
// `core/domain/location/LocationFuzzer`. Both must give identical results; the shared vectors in
// `test/fixtures/location-fuzz-vectors.json` are asserted by both test suites. Keep the operation order in sync.

/** `coarse_location_key` precision: a geohash-6 cell, about 1.2 km × 0.6 km (ADR-0019, owner-confirmed). */
export const GEOHASH_PRECISION = 6;

const BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
const JITTER_SPAN = 0.8; // the public point stays in the middle 80% of the cell on each axis
const SAME_POINT_EPSILON = 1e-9;
const CENTER_FALLBACK = 0.25;

export interface GeoBounds {
  latMin: number;
  latMax: number;
  lngMin: number;
  lngMax: number;
}

/** The fields the public `farts/{id}` doc carries instead of the precise point. */
export interface PublicLocation {
  public_lat: number;
  public_lng: number;
  coarse_location_key: string;
}

function assertCoordinates(lat: number, lng: number): void {
  if (!Number.isFinite(lat) || lat < -90 || lat > 90) throw new Error("latitude must be within -90..90");
  if (!Number.isFinite(lng) || lng < -180 || lng > 180) throw new Error("longitude must be within -180..180");
}

/** Standard geohash: interleaved bisection, even bits longitude; a point on a split goes to the upper half. */
export function encodeGeohash(lat: number, lng: number, precision: number = GEOHASH_PRECISION): string {
  assertCoordinates(lat, lng);
  if (!Number.isInteger(precision) || precision < 1 || precision > 12) throw new Error("precision must be 1..12");
  let latMin = -90;
  let latMax = 90;
  let lngMin = -180;
  let lngMax = 180;
  let isLng = true;
  let bit = 0;
  let value = 0;
  let cell = "";
  while (cell.length < precision) {
    if (isLng) {
      const mid = (lngMin + lngMax) / 2;
      if (lng >= mid) {
        value = value * 2 + 1;
        lngMin = mid;
      } else {
        value = value * 2;
        lngMax = mid;
      }
    } else {
      const mid = (latMin + latMax) / 2;
      if (lat >= mid) {
        value = value * 2 + 1;
        latMin = mid;
      } else {
        value = value * 2;
        latMax = mid;
      }
    }
    isLng = !isLng;
    bit++;
    if (bit === 5) {
      cell += BASE32[value];
      bit = 0;
      value = 0;
    }
  }
  return cell;
}

/** The lat/lng box of a geohash cell. Throws on an empty cell or a character outside the geohash alphabet. */
export function geohashBounds(cell: string): GeoBounds {
  if (cell.length === 0 || cell.length > 12) throw new Error("geohash cell must have 1..12 characters");
  let latMin = -90;
  let latMax = 90;
  let lngMin = -180;
  let lngMax = 180;
  let isLng = true;
  for (const char of cell) {
    const value = BASE32.indexOf(char);
    if (value < 0) throw new Error("geohash cell has a character outside the geohash alphabet");
    for (let shift = 4; shift >= 0; shift--) {
      const upper = ((value >> shift) & 1) === 1;
      if (isLng) {
        const mid = (lngMin + lngMax) / 2;
        if (upper) lngMin = mid;
        else lngMax = mid;
      } else {
        const mid = (latMin + latMax) / 2;
        if (upper) latMin = mid;
        else latMax = mid;
      }
      isLng = !isLng;
    }
  }
  return { latMin, latMax, lngMin, lngMax };
}

/** Edge-inclusive: a precise point may sit exactly on a cell edge. */
export function boundsContain(bounds: GeoBounds, lat: number, lng: number): boolean {
  return lat >= bounds.latMin && lat <= bounds.latMax && lng >= bounds.lngMin && lng <= bounds.lngMax;
}

/** FNV-1a, 32 bit, over the UTF-8 bytes of [text]. */
export function fnv1a32(text: string): number {
  let hash = 2166136261;
  for (const byte of new TextEncoder().encode(text)) {
    hash = Math.imul(hash ^ byte, 16777619) >>> 0;
  }
  return hash >>> 0;
}

function unit(text: string): number {
  return fnv1a32(text) / 4294967296;
}

/**
 * The public point for a fart: the centre of the geohash-6 cell holding the precise point, plus a deterministic
 * jitter derived from the fart id that stays inside the middle 80% of the cell. Never equals the precise point.
 */
export function projectPublicLocation(fartId: string, lat: number, lng: number): PublicLocation {
  if (fartId.trim().length === 0) throw new Error("fart id must not be blank");
  const cell = encodeGeohash(lat, lng, GEOHASH_PRECISION);
  const [publicLat, publicLng] = jitteredPoint(geohashBounds(cell), unit(`${fartId}:lat`), unit(`${fartId}:lng`), lat, lng);
  return { public_lat: publicLat, public_lng: publicLng, coarse_location_key: cell };
}

/**
 * The jitter step on its own, with the hash fractions [uLat]/[uLng] in [0, 1) passed in, so the "never the precise
 * point" branches are testable. Exported for tests only.
 */
export function jitteredPoint(bounds: GeoBounds, uLat: number, uLng: number, lat: number, lng: number): [number, number] {
  const latSize = bounds.latMax - bounds.latMin;
  const lngSize = bounds.lngMax - bounds.lngMin;
  const centerLat = (bounds.latMin + bounds.latMax) / 2;
  const centerLng = (bounds.lngMin + bounds.lngMax) / 2;
  const offsetLat = (uLat - 0.5) * JITTER_SPAN * latSize;
  const offsetLng = (uLng - 0.5) * JITTER_SPAN * lngSize;
  const publicLat = centerLat + offsetLat;
  const publicLng = centerLng + offsetLng;
  if (Math.abs(publicLat - lat) >= SAME_POINT_EPSILON || Math.abs(publicLng - lng) >= SAME_POINT_EPSILON) {
    return [publicLat, publicLng];
  }
  if (offsetLat === 0 && offsetLng === 0) {
    return [centerLat + CENTER_FALLBACK * latSize, centerLng + CENTER_FALLBACK * lngSize];
  }
  return [centerLat - offsetLat, centerLng - offsetLng];
}

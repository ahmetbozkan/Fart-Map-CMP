import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import {
  boundsContain,
  encodeGeohash,
  geohashBounds,
  jitteredPoint,
  projectPublicLocation,
  type GeoBounds,
} from "../src/lib/location";

interface Vector {
  fart_id: string;
  lat: number;
  lng: number;
  coarse_location_key: string;
  public_lat: number;
  public_lng: number;
}

/** Deterministic pseudo-random numbers (mulberry32), so property failures are reproducible. */
function rng(seed: number): () => number {
  let state = seed >>> 0;
  return () => {
    state = (state + 0x6d2b79f5) >>> 0;
    let t = state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function insideWithMargin(bounds: GeoBounds, lat: number, lng: number): boolean {
  const latMargin = 0.1 * (bounds.latMax - bounds.latMin);
  const lngMargin = 0.1 * (bounds.lngMax - bounds.lngMin);
  return (
    lat > bounds.latMin + latMargin - 1e-12 &&
    lat < bounds.latMax - latMargin + 1e-12 &&
    lng > bounds.lngMin + lngMargin - 1e-12 &&
    lng < bounds.lngMax - lngMargin + 1e-12
  );
}

describe("encodeGeohash", () => {
  // Independent of this code: Wikipedia's "Geohash" example, and cells derived by hand from the bisection definition.
  it.each([
    [57.64911, 10.40744, "u4pruy"],
    [90, 180, "zzzzzz"],
    [-90, -180, "000000"],
    [0, 0, "s00000"],
    [0, -180, "800000"],
  ])("encodes %f, %f as %s, and the point lies in its bounds", (lat, lng, cell) => {
    expect(encodeGeohash(lat, lng, 6)).toBe(cell);
    expect(boundsContain(geohashBounds(cell), lat, lng)).toBe(true);
  });

  it("matches the longer Wikipedia reference", () => {
    expect(encodeGeohash(57.64911, 10.40744, 11)).toBe("u4pruydqqvj");
  });

  it("rejects invalid coordinates and precision", () => {
    expect(() => encodeGeohash(90.1, 0)).toThrow();
    expect(() => encodeGeohash(0, -180.1)).toThrow();
    expect(() => encodeGeohash(Number.NaN, 0)).toThrow();
    expect(() => encodeGeohash(0, Number.POSITIVE_INFINITY)).toThrow();
    expect(() => encodeGeohash(0, 0, 0)).toThrow();
  });
});

describe("geohashBounds", () => {
  it("decodes a cell to a box of the geohash-6 size", () => {
    const bounds = geohashBounds("sxk973");
    expect(bounds.lngMax - bounds.lngMin).toBeCloseTo(360 / 2 ** 15, 12);
    expect(bounds.latMax - bounds.latMin).toBeCloseTo(180 / 2 ** 15, 12);
  });

  it("rejects an empty cell and characters outside the alphabet", () => {
    expect(() => geohashBounds("")).toThrow();
    expect(() => geohashBounds("sxk9wa")).toThrow();
    expect(() => geohashBounds("SXK973")).toThrow();
  });
});

describe("projectPublicLocation (ADR-0019)", () => {
  it("keys the precise cell, stays inside it with a 10% margin, is deterministic and never the precise point", () => {
    const random = rng(19);
    for (let i = 0; i < 2000; i++) {
      const lat = random() * 180 - 90;
      const lng = random() * 360 - 180;
      const fartId = `fart_${i}_${Math.floor(random() * 1e9)}`;
      const projected = projectPublicLocation(fartId, lat, lng);
      expect(projected.coarse_location_key).toBe(encodeGeohash(lat, lng, 6));
      expect(insideWithMargin(geohashBounds(projected.coarse_location_key), projected.public_lat, projected.public_lng)).toBe(true);
      expect(projectPublicLocation(fartId, lat, lng)).toEqual(projected);
      expect(projected.public_lat === lat && projected.public_lng === lng).toBe(false);
    }
  });

  it("gives different ids different public points", () => {
    const points = new Set<string>();
    for (let i = 0; i < 1000; i++) {
      const projected = projectPublicLocation(`id-${i}`, 41.0082, 28.9784);
      points.add(`${projected.public_lat},${projected.public_lng}`);
    }
    expect(points.size).toBe(1000);
  });

  it("moves away when the jittered point would be the precise point", () => {
    const first = projectPublicLocation("f_mirror", 41.0082, 28.9784);
    const again = projectPublicLocation("f_mirror", first.public_lat, first.public_lng);
    expect(again.coarse_location_key).toBe(first.coarse_location_key);
    expect(again.public_lat === first.public_lat && again.public_lng === first.public_lng).toBe(false);
    expect(insideWithMargin(geohashBounds(again.coarse_location_key), again.public_lat, again.public_lng)).toBe(true);
  });

  it("uses the fixed fallback when the precise point is the centre and the offset is zero", () => {
    const bounds = geohashBounds("sxk973");
    const centerLat = (bounds.latMin + bounds.latMax) / 2;
    const centerLng = (bounds.lngMin + bounds.lngMax) / 2;
    const [lat, lng] = jitteredPoint(bounds, 0.5, 0.5, centerLat, centerLng);
    expect(lat).toBe(centerLat + 0.25 * (bounds.latMax - bounds.latMin));
    expect(lng).toBe(centerLng + 0.25 * (bounds.lngMax - bounds.lngMin));
    expect(insideWithMargin(bounds, lat, lng)).toBe(true);
  });

  it("mirrors a non-zero offset that lands on the precise point", () => {
    const bounds = geohashBounds("sxk973");
    const [hitLat, hitLng] = jitteredPoint(bounds, 0.7, 0.2, 0, 0);
    const [lat, lng] = jitteredPoint(bounds, 0.7, 0.2, hitLat, hitLng);
    expect(lat).not.toBe(hitLat);
    expect(lng).not.toBe(hitLng);
    expect(insideWithMargin(bounds, lat, lng)).toBe(true);
  });

  it("rejects a blank fart id and invalid coordinates", () => {
    expect(() => projectPublicLocation("", 0, 0)).toThrow();
    expect(() => projectPublicLocation("   ", 0, 0)).toThrow();
    expect(() => projectPublicLocation("f", -90.5, 0)).toThrow();
    expect(() => projectPublicLocation("f", 0, 181)).toThrow();
    expect(() => projectPublicLocation("f", Number.NaN, 0)).toThrow();
  });

  it("reproduces every shared vector (the Kotlin LocationFuzzVectorsParityTest asserts the same file)", () => {
    const file = resolve(__dirname, "fixtures/location-fuzz-vectors.json");
    const { vectors } = JSON.parse(readFileSync(file, "utf8")) as { vectors: Vector[] };
    expect(vectors.length).toBeGreaterThanOrEqual(20);
    for (const vector of vectors) {
      const projected = projectPublicLocation(vector.fart_id, vector.lat, vector.lng);
      expect(projected.coarse_location_key).toBe(vector.coarse_location_key);
      expect(Math.abs(projected.public_lat - vector.public_lat)).toBeLessThan(1e-9);
      expect(Math.abs(projected.public_lng - vector.public_lng)).toBeLessThan(1e-9);
    }
  });
});

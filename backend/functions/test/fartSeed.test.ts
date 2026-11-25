import { describe, expect, it } from "vitest";
import {
  DEFAULT_SEED_CENTER,
  fartSeedDocs,
  parseCenter,
  parseCount,
  SEED_AUTHOR_ID,
  type FartSeedOptions,
} from "../src/admin/fartSeed";
import { assertEmulatorForDemo, assertSeedTarget } from "../src/admin/starterCatalog";
import { boundsContain, encodeGeohash, geohashBounds, projectPublicLocation } from "../src/lib/location";

// The farts create rule's required fields (backend/firestore.rules). The emulator test (fartSeed.rules.test.ts)
// creates a seed doc through the real rule, so this copy cannot drift silently.
const REQUIRED = [
  "fart_id", "author_user_id", "public_lat", "public_lng", "coarse_location_key", "timestamp_epoch_ms",
  "avatar_id", "flavor_level", "intensity_value", "visibility_mode", "reaction_count", "reaction_breakdown",
  "comment_count", "report_count", "is_hidden", "is_removed", "created_at", "updated_at",
].sort();

const NOW = 1_764_000_000_000;
const options = (overrides: Partial<FartSeedOptions> = {}): FartSeedOptions => ({
  count: 30,
  center: DEFAULT_SEED_CENTER,
  nowMs: NOW,
  avatars: ["avatar_free_bean", "avatar_free_cloud"],
  sounds: ["sound_free_classic", "sound_free_squeak", "sound_free_thunder"],
  ...overrides,
});

const TS = "server-ts";

function farts(docs: Record<string, Record<string, unknown>>): [string, Record<string, unknown>][] {
  return Object.entries(docs).filter(([path]) => !path.endsWith("/private/location"));
}

describe("fartSeedDocs", () => {
  const docs = fartSeedDocs(options(), TS);

  it("writes one public doc and one private location per fart, with stable ids", () => {
    expect(farts(docs).map(([path]) => path)).toEqual(
      Array.from({ length: 30 }, (_, i) => `farts/seed_fart_${String(i + 1).padStart(3, "0")}`),
    );
    for (const [path] of farts(docs)) expect(docs[`${path}/private/location`]).toBeDefined();
    expect(fartSeedDocs(options(), TS)).toEqual(docs);
  });

  it("uses exactly the rules' create field set, with sound_id as the only optional field", () => {
    for (const [, fart] of farts(docs)) {
      const keys = Object.keys(fart).filter((key) => key !== "sound_id").sort();
      expect(keys).toEqual(REQUIRED);
      expect(fart).not.toHaveProperty("description_text");
    }
    expect(farts(docs).some(([, fart]) => "sound_id" in fart)).toBe(true);
  });

  it("is PUBLIC, visible, zeroed and within the contract ranges", () => {
    for (const [path, fart] of farts(docs)) {
      expect(fart.fart_id).toBe(path.split("/")[1]);
      expect(fart.author_user_id).toBe(SEED_AUTHOR_ID);
      expect(fart.visibility_mode).toBe("PUBLIC");
      expect(fart).toMatchObject({ reaction_count: 0, reaction_breakdown: {}, comment_count: 0, report_count: 0 });
      expect(fart).toMatchObject({ is_hidden: false, is_removed: false, created_at: TS, updated_at: TS });
      expect(fart.flavor_level as number).toBeGreaterThanOrEqual(1);
      expect(fart.flavor_level as number).toBeLessThanOrEqual(5);
      expect(Number.isInteger(fart.intensity_value)).toBe(false);
      expect(fart.intensity_value as number).toBeGreaterThan(0);
      expect(fart.intensity_value as number).toBeLessThan(1);
      expect(Number.isInteger(fart.timestamp_epoch_ms)).toBe(true);
      expect(fart.timestamp_epoch_ms as number).toBeLessThan(NOW);
      expect(fart.timestamp_epoch_ms as number).toBeGreaterThan(NOW - 7 * 24 * 60 * 60 * 1000);
    }
  });

  it("projects the public fields from the private point and keeps the points near the centre", () => {
    for (const [path, fart] of farts(docs)) {
      const precise = docs[`${path}/private/location`] as { lat: number; lng: number; updated_at: string };
      expect(Object.keys(precise).sort()).toEqual(["lat", "lng", "updated_at"]);
      const expected = projectPublicLocation(fart.fart_id as string, precise.lat, precise.lng);
      expect(fart).toMatchObject(expected);
      expect(fart.coarse_location_key).toBe(encodeGeohash(precise.lat, precise.lng, 6));
      expect(boundsContain(geohashBounds(expected.coarse_location_key), expected.public_lat, expected.public_lng)).toBe(true);
      expect(Math.abs(precise.lat - DEFAULT_SEED_CENTER.lat)).toBeLessThan(0.05);
      expect(Math.abs(precise.lng - DEFAULT_SEED_CENTER.lng)).toBeLessThan(0.07);
    }
  });

  it("rejects bad counts, centres and empty catalogs", () => {
    expect(() => fartSeedDocs(options({ count: 0 }), TS)).toThrow();
    expect(() => fartSeedDocs(options({ count: 201 }), TS)).toThrow();
    expect(() => fartSeedDocs(options({ count: 1.5 }), TS)).toThrow();
    expect(() => fartSeedDocs(options({ center: { lat: 86, lng: 0 } }), TS)).toThrow();
    expect(() => fartSeedDocs(options({ center: { lat: 0, lng: 181 } }), TS)).toThrow();
    expect(() => fartSeedDocs(options({ avatars: [] }), TS)).toThrow();
    expect(() => fartSeedDocs(options({ sounds: [] }), TS)).toThrow();
    expect(Object.keys(fartSeedDocs(options({ count: 200 }), TS))).toHaveLength(400);
  });
});

describe("seed:farts arguments", () => {
  it("parses the centre and count with defaults", () => {
    expect(parseCenter(undefined)).toEqual(DEFAULT_SEED_CENTER);
    expect(parseCenter("52.52, 13.405")).toEqual({ lat: 52.52, lng: 13.405 });
    expect(() => parseCenter("52.52")).toThrow();
    expect(() => parseCenter("a,b")).toThrow();
    expect(parseCount(undefined)).toBe(30);
    expect(parseCount("12")).toBe(12);
    expect(() => parseCount("ten")).toThrow();
  });

  it("refuses a demo-* project unless the emulator host is set", () => {
    expect(() => assertEmulatorForDemo("demo-fartmap", {})).toThrow(/FIRESTORE_EMULATOR_HOST/);
    expect(() => assertEmulatorForDemo("demo-fartmap", { FIRESTORE_EMULATOR_HOST: "127.0.0.1:8080" })).not.toThrow();
    expect(() => assertEmulatorForDemo("fart-map-dev", {})).not.toThrow();
  });

  it("refuses prod and a missing project, naming the script", () => {
    expect(() => assertSeedTarget("fart-map-1a558", "seed:farts")).toThrow(/seed:farts/);
    expect(() => assertSeedTarget(undefined, "seed:farts")).toThrow(/seed:farts needs --project/);
    expect(assertSeedTarget("fart-map-dev", "seed:farts")).toBe("fart-map-dev");
  });
});

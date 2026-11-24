import { describe, expect, it } from "vitest";
import {
  assertSeedTarget, assertStarterItemsFree, catalogDocs, loadStarterCatalog, validateCatalog,
} from "../src/admin/starterCatalog";

describe("starter catalog (backend/catalog/starter-catalog.json)", () => {
  const items = loadStarterCatalog();

  it("meets the free-tier minimums: at least 2 avatars and 3 sounds, all free and active", () => {
    const freeActive = items.filter((item) => item.is_free && item.is_active);
    expect(freeActive.filter((item) => item.type === "AVATAR").length).toBeGreaterThanOrEqual(2);
    expect(freeActive.filter((item) => item.type === "SOUND").length).toBeGreaterThanOrEqual(3);
    expect(items.every((item) => item.is_free && item.is_active && !item.rewarded_unlockable)).toBe(true);
  });

  it("keys each doc by its item_id and stamps updated_at", () => {
    const docs = catalogDocs(items, "now");
    expect(Object.keys(docs).sort()).toEqual(items.map((item) => `cosmetic_items/${item.item_id}`).sort());
    for (const [path, data] of Object.entries(docs)) {
      expect(path).toBe(`cosmetic_items/${data.item_id}`);
      expect(data.updated_at).toBe("now");
    }
  });
});

describe("validateCatalog", () => {
  const valid = {
    item_id: "avatar_x", type: "AVATAR", name: "X", asset_ref: "avatar_x",
    is_free: true, rewarded_unlockable: false, sort_order: 1, is_active: true,
  };

  it("accepts a well-formed item", () => {
    expect(validateCatalog([valid])).toEqual([valid]);
  });

  it("rejects duplicate ids, unknown types, missing fields and bad ids", () => {
    expect(() => validateCatalog([valid, valid])).toThrow(/duplicate/);
    expect(() => validateCatalog([{ ...valid, type: "FRAME" }])).toThrow(/type/);
    expect(() => validateCatalog([{ ...valid, name: "" }])).toThrow(/name/);
    expect(() => validateCatalog([{ ...valid, is_free: "yes" }])).toThrow(/is_free/);
    expect(() => validateCatalog([{ ...valid, sort_order: 1.5 }])).toThrow(/sort_order/);
    expect(() => validateCatalog([{ ...valid, item_id: "has/slash" }])).toThrow(/item_id/);
  });
});

describe("assertSeedTarget (allowlist)", () => {
  it("accepts dev and emulator projects, normalized", () => {
    expect(assertSeedTarget("fart-map-dev")).toBe("fart-map-dev");
    expect(assertSeedTarget(" Fart-Map-Dev ")).toBe("fart-map-dev");
    expect(assertSeedTarget("demo-fartmap")).toBe("demo-fartmap");
  });

  it("refuses a missing project, prod, and anything else", () => {
    expect(() => assertSeedTarget(undefined)).toThrow(/--project/);
    expect(() => assertSeedTarget(" ")).toThrow(/--project/);
    expect(() => assertSeedTarget("fart-map-1a558")).toThrow(/only seeds/);
    expect(() => assertSeedTarget("FART-MAP-1A558")).toThrow(/only seeds/);
    expect(() => assertSeedTarget("fart-map-staging")).toThrow(/only seeds/);
  });
});

describe("assertStarterItemsFree", () => {
  const free = {
    item_id: "avatar_x", type: "AVATAR" as const, name: "X", asset_ref: "avatar_x",
    is_free: true, rewarded_unlockable: false, sort_order: 1, is_active: true,
  };

  it("passes free items and refuses paid or rewarded ones", () => {
    expect(assertStarterItemsFree([free])).toEqual([free]);
    expect(() => assertStarterItemsFree([{ ...free, is_free: false }])).toThrow(/avatar_x/);
    expect(() => assertStarterItemsFree([{ ...free, rewarded_unlockable: true }])).toThrow(/free-only/);
  });
});

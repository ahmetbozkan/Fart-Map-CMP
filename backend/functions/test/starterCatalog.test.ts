import { describe, expect, it } from "vitest";
import {
  PROD_PROJECT_ID, assertSeedTarget, catalogDocs, loadStarterCatalog, validateCatalog,
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

describe("assertSeedTarget", () => {
  it("returns a dev project", () => {
    expect(assertSeedTarget("fart-map-dev")).toBe("fart-map-dev");
  });

  it("refuses a missing project and prod", () => {
    expect(() => assertSeedTarget(undefined)).toThrow(/--project/);
    expect(() => assertSeedTarget(" ")).toThrow(/--project/);
    expect(() => assertSeedTarget(PROD_PROJECT_ID)).toThrow(/prod/);
  });
});

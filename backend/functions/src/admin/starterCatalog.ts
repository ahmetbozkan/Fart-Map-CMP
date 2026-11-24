import { readFileSync } from "node:fs";
import { resolve } from "node:path";

/** One `cosmetic_items/{itemId}` doc as stored, minus `updated_at` (DataContracts.md §3.13). */
export interface CatalogItem {
  item_id: string;
  type: "AVATAR" | "SOUND";
  name: string;
  asset_ref: string;
  is_free: boolean;
  rewarded_unlockable: boolean;
  sort_order: number;
  is_active: boolean;
}

const ITEM_TYPES = new Set(["AVATAR", "SOUND"]);
const ITEM_ID = /^[a-z0-9_]{1,64}$/;

/** `backend/catalog/starter-catalog.json`; the same relative path works from `src/admin` and `lib/admin`. */
export const STARTER_CATALOG_PATH = resolve(__dirname, "../../../catalog/starter-catalog.json");

/** Reads and validates the starter catalog. Throws on any malformed item. */
export function loadStarterCatalog(path: string = STARTER_CATALOG_PATH): CatalogItem[] {
  const parsed: unknown = JSON.parse(readFileSync(path, "utf8"));
  const items = (parsed as { items?: unknown }).items;
  if (!Array.isArray(items)) throw new Error("starter catalog: `items` must be an array");
  return assertStarterItemsFree(validateCatalog(items));
}

/** Checks every field and the uniqueness of ids; returns the items typed. */
export function validateCatalog(items: unknown[]): CatalogItem[] {
  const seen = new Set<string>();
  return items.map((raw, index) => {
    const item = raw as Partial<CatalogItem>;
    const where = `starter catalog item ${index}`;
    if (typeof item.item_id !== "string" || !ITEM_ID.test(item.item_id)) throw new Error(`${where}: bad item_id`);
    if (seen.has(item.item_id)) throw new Error(`${where}: duplicate item_id ${item.item_id}`);
    seen.add(item.item_id);
    if (typeof item.type !== "string" || !ITEM_TYPES.has(item.type)) throw new Error(`${where}: bad type`);
    if (typeof item.name !== "string" || item.name.length === 0) throw new Error(`${where}: missing name`);
    if (typeof item.asset_ref !== "string" || item.asset_ref.length === 0) throw new Error(`${where}: missing asset_ref`);
    for (const flag of ["is_free", "rewarded_unlockable", "is_active"] as const) {
      if (typeof item[flag] !== "boolean") throw new Error(`${where}: ${flag} must be a boolean`);
    }
    if (typeof item.sort_order !== "number" || !Number.isInteger(item.sort_order)) {
      throw new Error(`${where}: sort_order must be an integer`);
    }
    return {
      item_id: item.item_id,
      type: item.type as CatalogItem["type"],
      name: item.name,
      asset_ref: item.asset_ref,
      is_free: item.is_free as boolean,
      rewarded_unlockable: item.rewarded_unlockable as boolean,
      sort_order: item.sort_order,
      is_active: item.is_active as boolean,
    };
  });
}

/** Firestore docs keyed by path, each stamped with [updatedAt] (a server timestamp when seeding). */
export function catalogDocs<T>(items: CatalogItem[], updatedAt: T): Record<string, CatalogItem & { updated_at: T }> {
  const docs: Record<string, CatalogItem & { updated_at: T }> = {};
  for (const item of items) docs[`cosmetic_items/${item.item_id}`] = { ...item, updated_at: updatedAt };
  return docs;
}

/**
 * Projects the seed may write to: dev and local emulator projects (`demo-*`) only. An allowlist, so a typo, a new
 * staging project or prod (whose catalog ships with EPIC-01-06) can never receive privileged catalog writes.
 */
export const DEV_PROJECT_ID = "fart-map-dev";

/** Returns the normalized project to seed, or throws when it is missing or not allowed. */
export function assertSeedTarget(project: string | undefined): string {
  const id = project?.trim().toLowerCase() ?? "";
  if (id.length === 0) throw new Error("seed:catalog needs --project <id>");
  if (id !== DEV_PROJECT_ID && !id.startsWith("demo-")) {
    throw new Error(`seed:catalog only seeds ${DEV_PROJECT_ID} or demo-* emulator projects, not "${id}"`);
  }
  return id;
}

/** The starter catalog is free items only; a paid or rewarded item here would be a free grant to everyone. */
export function assertStarterItemsFree(items: CatalogItem[]): CatalogItem[] {
  const paid = items.filter((item) => !item.is_free || item.rewarded_unlockable);
  if (paid.length > 0) {
    throw new Error(`starter catalog must be free-only; offending: ${paid.map((item) => item.item_id).join(", ")}`);
  }
  return items;
}

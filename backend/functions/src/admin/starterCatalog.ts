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
  return validateCatalog(items);
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

/** Prod's catalog (paid items, Offerings) ships with EPIC-01-06; this seed is for dev and emulators only. */
export const PROD_PROJECT_ID = "fart-map-1a558";

/** Returns the project to seed, or throws when it is missing or prod. */
export function assertSeedTarget(project: string | undefined): string {
  if (!project || project.trim().length === 0) throw new Error("seed:catalog needs --project <id>");
  if (project === PROD_PROJECT_ID) throw new Error(`seed:catalog refuses prod (${PROD_PROJECT_ID}); see EPIC-01-06`);
  return project;
}

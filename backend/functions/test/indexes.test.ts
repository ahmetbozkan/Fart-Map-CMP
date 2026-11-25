import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";

// The emulator does not enforce composite indexes, so a wrong firestore.indexes.json would only show up after the
// dev deploy (FAILED_PRECONDITION on the map). These are the shapes FirestoreMapFartRepository sends (STORY-01-03-03):
// equality filters first, then the coarse_location_key range.
interface IndexField { fieldPath: string; order?: string }
interface Index { collectionGroup: string; queryScope: string; fields: IndexField[] }

const file = resolve(__dirname, "../../firestore.indexes.json");
const indexes = (JSON.parse(readFileSync(file, "utf8")) as { indexes: Index[] }).indexes;

const shape = (index: Index) => index.fields.map((f) => `${f.fieldPath}:${f.order}`);

describe("firestore.indexes.json: map queries", () => {
  it.each([
    ["public cells", ["visibility_mode", "is_hidden", "is_removed", "coarse_location_key"]],
    ["Mine cells", ["author_user_id", "is_removed", "coarse_location_key"]],
  ])("has the %s index", (_name, fields) => {
    const expected = fields.map((f) => `${f}:ASCENDING`);
    const match = indexes.find((index) =>
      index.collectionGroup === "farts" && index.queryScope === "COLLECTION"
      && JSON.stringify(shape(index)) === JSON.stringify(expected));
    expect(match, `missing farts index ${expected.join(", ")}`).toBeDefined();
  });
});

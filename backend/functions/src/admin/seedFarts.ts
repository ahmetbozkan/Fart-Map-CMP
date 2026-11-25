// Seeds dev test farts for the map (STORY-01-03-01). Not exported from index.ts, so it is never deployed. Run by
// the owner with Application Default Credentials, after the catalog seed:
//   gcloud auth application-default login
//   npm run seed:farts -- --project fart-map-dev [--center 41.0082,28.9784] [--count 30]
// Upserts seed_fart_001..N: ids and locations are stable across runs; timestamps and created_at are refreshed.
// A re-run with a smaller --count leaves the higher-numbered docs in place (dev data; delete them in the console
// if needed). Only fart-map-dev, or demo-* with the emulator running, is accepted. Logs no coordinates.
import { initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logError, logInfo } from "../lib/log";
import { fartSeedDocs, parseCenter, parseCount } from "./fartSeed";
import { assertEmulatorForDemo, assertSeedTarget, loadStarterCatalog } from "./starterCatalog";

function arg(argv: string[], name: string): string | undefined {
  const index = argv.indexOf(name);
  return index >= 0 ? argv[index + 1] : undefined;
}

async function main(): Promise<void> {
  const argv = process.argv.slice(2);
  const projectId = assertSeedTarget(arg(argv, "--project"), "seed:farts");
  assertEmulatorForDemo(projectId);
  const catalog = loadStarterCatalog();
  const docs = fartSeedDocs(
    {
      count: parseCount(arg(argv, "--count")),
      center: parseCenter(arg(argv, "--center")),
      nowMs: Date.now(),
      avatars: catalog.filter((item) => item.type === "AVATAR").map((item) => item.item_id),
      sounds: catalog.filter((item) => item.type === "SOUND").map((item) => item.item_id),
    },
    FieldValue.serverTimestamp(),
  );
  const db = getFirestore(initializeApp({ projectId }));
  const batch = db.batch();
  for (const [path, data] of Object.entries(docs)) batch.set(db.doc(path), data);
  await batch.commit();
  logInfo("farts_seeded", { project: projectId, farts: Object.keys(docs).length / 2 });
}

main().catch((error: unknown) => {
  logError("fart_seed_failed", { message: error instanceof Error ? error.message : String(error) });
  process.exitCode = 1;
});

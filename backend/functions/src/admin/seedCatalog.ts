// Seeds the free starter cosmetics into a Firebase project (STORY-01-02-02). Not exported from index.ts, so it is
// never deployed. Run by the owner with Application Default Credentials:
//   gcloud auth application-default login
//   npm run seed:catalog -- --project fart-map-dev
// Idempotent: upserts each starter item and leaves every other catalog doc alone. Only fart-map-dev or demo-*
// emulator projects are accepted, and only free items are written.
import { initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logError, logInfo } from "../lib/log";
import { assertSeedTarget, catalogDocs, loadStarterCatalog } from "./starterCatalog";

function projectArg(argv: string[]): string | undefined {
  const index = argv.indexOf("--project");
  return index >= 0 ? argv[index + 1] : undefined;
}

async function main(): Promise<void> {
  const projectId = assertSeedTarget(projectArg(process.argv.slice(2)));
  const items = loadStarterCatalog();
  const db = getFirestore(initializeApp({ projectId }));
  const batch = db.batch();
  for (const [path, data] of Object.entries(catalogDocs(items, FieldValue.serverTimestamp()))) {
    batch.set(db.doc(path), data, { merge: true });
  }
  await batch.commit();
  logInfo("catalog_seeded", { project: projectId, items: items.length });
}

main().catch((error: unknown) => {
  logError("catalog_seed_failed", { message: error instanceof Error ? error.message : String(error) });
  process.exitCode = 1;
});

import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { afterAll, beforeAll, describe, it } from "vitest";
import {
  assertFails,
  initializeTestEnvironment,
  type RulesTestEnvironment,
} from "@firebase/rules-unit-testing";

let env: RulesTestEnvironment;

beforeAll(async () => {
  env = await initializeTestEnvironment({
    projectId: "demo-fartmap",
    firestore: {
      rules: readFileSync(resolve(__dirname, "../../firestore.rules"), "utf8"),
      host: "127.0.0.1",
      port: 8080,
    },
  });
});

afterAll(async () => {
  await env.cleanup();
});

describe("firestore rules: default deny", () => {
  it("denies unauthenticated reads and writes", async () => {
    const db = env.unauthenticatedContext().firestore();
    await assertFails(db.collection("farts").doc("x").get());
    await assertFails(db.collection("farts").doc("x").set({ a: 1 }));
  });

  it("denies authenticated access to unknown paths", async () => {
    const db = env.authenticatedContext("user-1").firestore();
    await assertFails(db.collection("anything").doc("x").get());
    await assertFails(db.collection("anything").doc("x").set({ a: 1 }));
    await assertFails(db.collection("users").doc("user-1").set({ display_name: "me" }));
  });
});

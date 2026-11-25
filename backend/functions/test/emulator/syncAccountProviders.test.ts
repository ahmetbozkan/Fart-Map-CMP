import { deleteApp, initializeApp, type App } from "firebase-admin/app";
import { getFirestore, Timestamp, type Firestore } from "firebase-admin/firestore";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { handleSyncAccountProviders, syncAccountProviders } from "../../src/callables/syncAccountProviders";

// Admin SDK against the Firestore emulator (FIRESTORE_EMULATOR_HOST is set by `firebase emulators:exec`).
let app: App;
let db: Firestore;

const CREATED = Timestamp.fromMillis(1_700_000_000_000);
const guestAccount = () => ({
  age_band: "AGE_13_17",
  linked_providers: [],
  is_anonymous: true,
  is_suspended: false,
  created_at: CREATED,
  updated_at: CREATED,
});

beforeAll(() => {
  process.env.FIRESTORE_EMULATOR_HOST ??= "127.0.0.1:8080";
  app = initializeApp({ projectId: "demo-fartmap" }, "sync-account-providers-test");
  db = getFirestore(app);
});
afterAll(async () => {
  await deleteApp(app);
});
beforeEach(async () => {
  await db.recursiveDelete(db.collection("users"));
});

describe("syncAccountProviders", () => {
  it("flips a linked guest to non-anonymous and keeps every other field", async () => {
    await db.doc("users/guest-1/private/account").set(guestAccount());

    expect(await syncAccountProviders(db, "guest-1", ["google.com"])).toBe("updated");

    const data = (await db.doc("users/guest-1/private/account").get()).data();
    expect(data?.is_anonymous).toBe(false);
    expect(data?.linked_providers).toEqual(["google"]);
    expect(data?.age_band).toBe("AGE_13_17");
    expect(data?.is_suspended).toBe(false);
    expect((data?.created_at as Timestamp).isEqual(CREATED)).toBe(true);
    expect((data?.updated_at as Timestamp).toMillis()).toBeGreaterThan(CREATED.toMillis());
  });

  it("never creates a missing account doc", async () => {
    expect(await syncAccountProviders(db, "nobody", ["google.com"])).toBe("missing");
    expect((await db.doc("users/nobody/private/account").get()).exists).toBe(false);
  });

  it("is idempotent", async () => {
    await db.doc("users/guest-1/private/account").set(guestAccount());
    await syncAccountProviders(db, "guest-1", ["google.com"]);
    await syncAccountProviders(db, "guest-1", ["google.com"]);
    const data = (await db.doc("users/guest-1/private/account").get()).data();
    expect(data?.linked_providers).toEqual(["google"]);
    expect(data?.is_anonymous).toBe(false);
  });

  it("writes only the caller's doc through the callable handler", async () => {
    await db.doc("users/guest-1/private/account").set(guestAccount());
    await db.doc("users/other/private/account").set(guestAccount());

    const result = await handleSyncAccountProviders(
      {
        lookupProviders: async () => ["google.com"],
        writeAccount: (uid, ids) => syncAccountProviders(db, uid, ids),
      },
      { uid: "guest-1" },
    );

    expect(result).toEqual({ status: "updated" });
    expect((await db.doc("users/guest-1/private/account").get()).data()?.is_anonymous).toBe(false);
    expect((await db.doc("users/other/private/account").get()).data()?.is_anonymous).toBe(true);
  });
});

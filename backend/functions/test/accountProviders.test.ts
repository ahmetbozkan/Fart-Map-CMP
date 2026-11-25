import { logger } from "firebase-functions";
import { HttpsError } from "firebase-functions/v2/https";
import { afterEach, describe, expect, it, vi } from "vitest";
import { accountProvidersFrom, handleSyncAccountProviders, type SyncDeps } from "../src/callables/syncAccountProviders";

describe("accountProvidersFrom", () => {
  it("is anonymous when no sign-in provider is linked", () => {
    expect(accountProvidersFrom([])).toEqual({ is_anonymous: true, linked_providers: [] });
  });

  it("maps Google and Apple to their wire names, sorted and de-duplicated", () => {
    expect(accountProvidersFrom(["google.com"])).toEqual({ is_anonymous: false, linked_providers: ["google"] });
    expect(accountProvidersFrom(["google.com", "apple.com", "google.com"]))
      .toEqual({ is_anonymous: false, linked_providers: ["apple", "google"] });
  });

  it("ignores providers Fart Map doesn't offer", () => {
    expect(accountProvidersFrom(["password", "phone"])).toEqual({ is_anonymous: true, linked_providers: [] });
  });
});

describe("handleSyncAccountProviders", () => {
  afterEach(() => vi.restoreAllMocks());

  const deps = (overrides: Partial<SyncDeps> = {}): SyncDeps => ({
    lookupProviders: vi.fn(async () => ["google.com"]),
    writeAccount: vi.fn(async () => "updated" as const),
    ...overrides,
  });

  it("refuses an unauthenticated call without touching anything", async () => {
    const d = deps();
    await expect(handleSyncAccountProviders(d, undefined)).rejects.toMatchObject({ code: "unauthenticated" });
    expect(d.lookupProviders).not.toHaveBeenCalled();
    expect(d.writeAccount).not.toHaveBeenCalled();
  });

  it("uses only the caller's uid", async () => {
    const d = deps();
    await expect(handleSyncAccountProviders(d, { uid: "alice" })).resolves.toEqual({ status: "updated" });
    expect(d.lookupProviders).toHaveBeenCalledWith("alice");
    expect(d.writeAccount).toHaveBeenCalledWith("alice", ["google.com"]);
  });

  it("maps a lookup failure to internal and logs no identifier", async () => {
    const errors = vi.spyOn(logger, "error").mockImplementation(() => undefined);
    const failure = Object.assign(new Error("There is no user record for alice@example.com"), {
      code: "auth/user-not-found",
    });
    const d = deps({ lookupProviders: vi.fn(async () => Promise.reject(failure)) });

    const call = handleSyncAccountProviders(d, { uid: "alice" });
    await expect(call).rejects.toBeInstanceOf(HttpsError);
    await expect(call).rejects.toMatchObject({ code: "internal" });
    expect(d.writeAccount).not.toHaveBeenCalled();
    const logged = JSON.stringify(errors.mock.calls);
    expect(logged).toContain("auth/user-not-found");
    expect(logged).not.toContain("alice");
  });

  it("maps a write failure to internal", async () => {
    vi.spyOn(logger, "error").mockImplementation(() => undefined);
    const d = deps({ writeAccount: vi.fn(async () => Promise.reject(new Error("boom"))) });
    await expect(handleSyncAccountProviders(d, { uid: "alice" })).rejects.toMatchObject({ code: "internal" });
  });
});

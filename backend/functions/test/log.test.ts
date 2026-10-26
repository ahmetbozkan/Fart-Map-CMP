import { describe, expect, it } from "vitest";
import { redact } from "../src/lib/log";

describe("redact", () => {
  it("replaces forbidden keys and keeps the rest", () => {
    const out = redact({ fartId: "f1", lat: 48.2, fcm_token: "abc", visibility: "FOLLOWERS" });
    expect(out).toEqual({ fartId: "f1", lat: "[redacted]", fcm_token: "[redacted]", visibility: "FOLLOWERS" });
  });
});

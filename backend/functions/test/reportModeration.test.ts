import { describe, expect, it } from "vitest";
import { AUTO_HIDE_THRESHOLD, handleReportCreated, moderationUpdate } from "../src/triggers/reportCreated";
import type { Firestore } from "firebase-admin/firestore";

// The pure decision behind onReportCreated (MOD-02 baseline): count, then hide at the third distinct reporter.
describe("moderationUpdate", () => {
  it("hides at three distinct reports", () => {
    expect(AUTO_HIDE_THRESHOLD).toBe(3);
    expect(moderationUpdate(0, false)).toEqual({ reportCount: 1, hide: false });
    expect(moderationUpdate(1, false)).toEqual({ reportCount: 2, hide: false });
    expect(moderationUpdate(2, false)).toEqual({ reportCount: 3, hide: true });
  });

  it("keeps counting past the threshold without hiding again", () => {
    expect(moderationUpdate(3, true)).toEqual({ reportCount: 4, hide: false });
  });

  it("hides a fart that reached the threshold without being hidden yet", () => {
    expect(moderationUpdate(5, false)).toEqual({ reportCount: 6, hide: true });
  });

  it("treats a negative count as zero", () => {
    expect(moderationUpdate(-2, false)).toEqual({ reportCount: 1, hide: false });
  });
});

describe("handleReportCreated input checks", () => {
  // Rejected before any database access, so a stub that throws proves nothing is read or written.
  const untouchable = new Proxy({}, {
    get() {
      throw new Error("database touched");
    },
  }) as Firestore;

  it("skips non-fart targets and malformed reports", async () => {
    expect(await handleReportCreated(untouchable, "e1", { target_type: "USER", target_id: "u", reporter_user_id: "r" }))
      .toBe("skipped_target");
    expect(await handleReportCreated(untouchable, "e2", { target_type: "FART", target_id: "", reporter_user_id: "r" }))
      .toBe("skipped_target");
    expect(await handleReportCreated(untouchable, "e3", { target_type: "FART", target_id: "f" }))
      .toBe("skipped_target");
    expect(await handleReportCreated(untouchable, "e4", undefined)).toBe("skipped_target");
  });
});

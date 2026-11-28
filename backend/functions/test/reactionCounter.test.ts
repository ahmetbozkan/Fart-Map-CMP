import { describe, expect, it } from "vitest";
import { counterDelta } from "../src/triggers/reactionWritten";

// The pure delta behind onReactionWritten (SOCIAL-01): create, delete, replace, and the no-op cases.
describe("counterDelta", () => {
  it("counts a new reaction", () => {
    expect(counterDelta(undefined, { type: "DEAD" })).toEqual({ count: 1, breakdown: { DEAD: 1 } });
  });

  it("uncounts a removed reaction", () => {
    expect(counterDelta({ type: "TOXIC" }, undefined)).toEqual({ count: -1, breakdown: { TOXIC: -1 } });
  });

  it("moves one unit when a reaction is replaced, without touching the total", () => {
    expect(counterDelta({ type: "RIP" }, { type: "LEGEND" })).toEqual({
      count: 0,
      breakdown: { RIP: -1, LEGEND: 1 },
    });
  });

  it("changes nothing when the type is unchanged", () => {
    expect(counterDelta({ type: "DEAD" }, { type: "DEAD" })).toEqual({ count: 0, breakdown: {} });
  });

  it("ignores unknown types, never counting them", () => {
    expect(counterDelta(undefined, { type: "WOW" })).toEqual({ count: 0, breakdown: {} });
    expect(counterDelta({ type: "WOW" }, undefined)).toEqual({ count: 0, breakdown: {} });
    expect(counterDelta({ type: "WOW" }, { type: "SMELLED" })).toEqual({ count: 1, breakdown: { SMELLED: 1 } });
  });

  it("does nothing for a delete of nothing", () => {
    expect(counterDelta(undefined, undefined)).toEqual({ count: 0, breakdown: {} });
  });
});

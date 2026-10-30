import assert from "node:assert/strict";
import test from "node:test";
import { getSafeReturnLocation } from "../src/utils/navigation.js";

test("uses the home page when there is no saved destination", () => {
  assert.deepEqual(getSafeReturnLocation(undefined), {
    pathname: "/",
    search: "",
    hash: "",
  });
});

test("preserves an internal destination including query and fragment", () => {
  assert.deepEqual(getSafeReturnLocation({
    pathname: "/checkout",
    search: "?coupon=SAVE",
    hash: "delivery",
  }), {
    pathname: "/checkout",
    search: "?coupon=SAVE",
    hash: "delivery",
  });
});

test("rejects protocol-relative external destinations", () => {
  assert.equal(getSafeReturnLocation("//example.com").pathname, "/");
});

test("does not redirect back to login or registration", () => {
  assert.equal(getSafeReturnLocation("/login").pathname, "/");
  assert.equal(getSafeReturnLocation("/register").pathname, "/");
});

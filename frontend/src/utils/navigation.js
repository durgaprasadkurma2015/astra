export function getSafeReturnLocation(requestedLocation) {
  const requestedPath =
    typeof requestedLocation === "string"
      ? requestedLocation
      : requestedLocation?.pathname;

  const pathname =
    typeof requestedPath === "string" &&
    requestedPath.startsWith("/") &&
    !requestedPath.startsWith("//") &&
    requestedPath !== "/login" &&
    requestedPath !== "/register"
      ? requestedPath
      : "/";

  return {
    pathname,
    search:
      typeof requestedLocation === "object"
        ? requestedLocation.search || ""
        : "",
    hash:
      typeof requestedLocation === "object"
        ? requestedLocation.hash || ""
        : "",
  };
}

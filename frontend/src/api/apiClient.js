
import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  "http://localhost:8080/api/v1";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

let refreshPromise;

const MONITOR_ENDPOINT = `${API_BASE_URL.replace(/\/$/, "")}/monitoring/frontend`;
const MAX_LOG_SIZE = 8000;

// Sensitive fields must never be stored in API monitoring logs.
const SENSITIVE_KEY_PATTERN =
  /authorization|password|passwd|secret|token|cookie|session|otp|cvv|cvc|card.?number|card.?holder|account.?number|refresh.?token|access.?token/i;

const SENSITIVE_QUERY_PATTERN =
  /token|password|secret|key|otp|session|code/i;

function redactValue(value, depth = 0) {
  if (depth > 6) return "[MAX_DEPTH]";

  if (value === null || value === undefined) {
    return value;
  }

  if (typeof value === "string") {
    return value.length > 2000
      ? `${value.substring(0, 2000)}...[TRUNCATED]`
      : value;
  }

  if (Array.isArray(value)) {
    return value.slice(0, 50).map((item) =>
      redactValue(item, depth + 1)
    );
  }

  if (typeof value === "object") {
    const sanitized = {};

    Object.entries(value).slice(0, 100).forEach(([key, item]) => {
      sanitized[key] = SENSITIVE_KEY_PATTERN.test(key)
        ? "[REDACTED]"
        : redactValue(item, depth + 1);
    });

    return sanitized;
  }

  return value;
}

function sanitizeUrl(url) {
  if (!url) return "";

  try {
    const parsed = new URL(url, API_BASE_URL);

    for (const key of parsed.searchParams.keys()) {
      if (SENSITIVE_QUERY_PATTERN.test(key)) {
        parsed.searchParams.set(key, "[REDACTED]");
      }
    }

    return `${parsed.pathname}${parsed.search}`;
  } catch {
    return String(url).split("?")[0];
  }
}

function sanitizeHeaders(headers) {
  if (!headers) return {};

  let source = headers;

  if (typeof headers.toJSON === "function") {
    source = headers.toJSON();
  }

  const sanitized = {};

  Object.entries(source).forEach(([key, value]) => {
    sanitized[key] = SENSITIVE_KEY_PATTERN.test(key)
      ? "[REDACTED]"
      : redactValue(value);
  });

  return sanitized;
}

function sanitizeBody(body) {
  if (body === undefined || body === null) return null;

  if (
    typeof FormData !== "undefined" &&
    body instanceof FormData
  ) {
    return "[FORM_DATA_REDACTED]";
  }

  if (
    typeof Blob !== "undefined" &&
    body instanceof Blob
  ) {
    return "[BINARY_DATA_REDACTED]";
  }

  if (typeof body === "string") {
    try {
      return redactValue(JSON.parse(body));
    } catch {
      return body.length > 2000
        ? `${body.substring(0, 2000)}...[TRUNCATED]`
        : body;
    }
  }

  return redactValue(body);
}

function limitLogSize(value) {
  try {
    const serialized = JSON.stringify(value);

    if (serialized.length <= MAX_LOG_SIZE) {
      return value;
    }

    return {
      truncated: true,
      preview: serialized.substring(0, MAX_LOG_SIZE),
    };
  } catch {
    return "[UNSERIALIZABLE_DATA]";
  }
}

// Send monitoring logs separately so monitoring does not trigger
// another Axios interceptor or create an infinite logging loop.
function sendMonitoringLog(log) {
  try {
    const token =
      localStorage.getItem("astra_access_token") ||
      sessionStorage.getItem("astra_access_token");

    const headers = {
      "Content-Type": "application/json",
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    fetch(MONITOR_ENDPOINT, {
      method: "POST",
      headers,
      credentials: "include",
      keepalive: true,
      body: JSON.stringify(limitLogSize(log)),
    }).catch(() => {
      // Monitoring must never affect the original API request.
    });
  } catch {
    // Ignore monitoring errors.
  }
}

function recordFrontendRequest(config, status, responseData, responseHeaders, error) {
  if (!config || config.skipApiMonitor) return;

  const metadata = config._astraMonitor || {};
  const duration = metadata.startedAt
    ? Math.max(0, Date.now() - metadata.startedAt)
    : 0;

  const log = {
    source: "FRONTEND",
    timestamp: new Date().toISOString(),
    method: (config.method || "GET").toUpperCase(),
    endpoint: sanitizeUrl(
      `${config.baseURL || API_BASE_URL}/${config.url || ""}`
    ),
    status: status || 0,
    durationMs: duration,
    success: !error && status >= 200 && status < 400,
    requestHeaders: sanitizeHeaders(config.headers),
    requestBody: sanitizeBody(config.data),
    responseHeaders: sanitizeHeaders(responseHeaders),
    responseBody: sanitizeBody(responseData),
    error: error
      ? String(error.message || "Request failed").substring(0, 500)
      : null,
  };

  sendMonitoringLog(log);
}

// Attach the current access token and capture request start time.
apiClient.interceptors.request.use((config) => {
  const isPublicAuthRequest =
    /\/auth\/(refresh|logout|login|register|verify-registration-otp|resend-registration-otp|forgot-password|verify-otp|reset-password)(\/|$|\?)/i.test(
      config.url || ""
    );

  if (isPublicAuthRequest) {
    delete config.headers?.Authorization;
  } else {
    const token =
      localStorage.getItem("astra_access_token") ||
      sessionStorage.getItem("astra_access_token");

    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
  }

  if (!config.skipApiMonitor) {
    config._astraMonitor = {
      startedAt: Date.now(),
    };
  }

  return config;
});

// Capture successful responses.
apiClient.interceptors.response.use(
  (response) => {
    recordFrontendRequest(
      response.config,
      response.status,
      response.data,
      response.headers,
      null
    );

    return response;
  },

  async (error) => {
    const status = error.response?.status || 0;
    const requestUrl = error.config?.url || "";

    const isAuthenticationEndpoint =
      /\/auth\/(login|logout|refresh|register|verify-registration-otp|resend-registration-otp|verify-email|verify-mobile|forgot-password|verify-otp|reset-password)(\/|$|\?)/i.test(
        requestUrl
      );

    const token =
      localStorage.getItem("astra_access_token") ||
      sessionStorage.getItem("astra_access_token");

    // Record the failed request before attempting token refresh.
    recordFrontendRequest(
      error.config,
      status,
      error.response?.data,
      error.response?.headers,
      error
    );

    if (
      status === 401 &&
      !isAuthenticationEndpoint &&
      token &&
      !error.config?._retry
    ) {
      error.config._retry = true;

      try {
        refreshPromise ||= apiClient
          .post(
            "/auth/refresh",
            {},
            {
              skipAuthRefresh: true,
            }
          )
          .finally(() => {
            refreshPromise = undefined;
          });

        const response = await refreshPromise;
        const nextToken = response.data?.accessToken;

        if (!nextToken) {
          throw new Error(
            "Refresh response did not contain an access token."
          );
        }

        if (localStorage.getItem("astra_access_token")) {
          localStorage.setItem("astra_access_token", nextToken);
        } else {
          sessionStorage.setItem("astra_access_token", nextToken);
        }

        error.config.headers = error.config.headers || {};
        error.config.headers.Authorization = `Bearer ${nextToken}`;

        return apiClient(error.config);
      } catch (refreshError) {
        localStorage.removeItem("astra_access_token");
        sessionStorage.removeItem("astra_access_token");

        window.dispatchEvent(
          new CustomEvent("astra:session-expired")
        );

        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;

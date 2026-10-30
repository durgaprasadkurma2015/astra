
import axios from "axios";

const apiClient = axios.create({
  baseURL:
    import.meta.env.VITE_API_BASE_URL ||
    "http://localhost:8080/api/v1",
  timeout: 15000,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

let refreshPromise;

// Attach the current access token to API requests.
apiClient.interceptors.request.use((config) => {
  const isPublicAuthRequest = /\/auth\/(refresh|logout|login|register|verify-registration-otp|resend-registration-otp|forgot-password|verify-otp|reset-password)(\/|$|\?)/i.test(
    config.url || ""
  );
  if (isPublicAuthRequest) {
    delete config.headers?.Authorization;
    return config;
  }

  const token =
    localStorage.getItem("astra_access_token") ||
    sessionStorage.getItem("astra_access_token");

  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// Detect unauthorized responses and notify the session timeout manager.
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error.response?.status;
    const requestUrl = error.config?.url || "";

    const isAuthenticationEndpoint =
      /\/auth\/(login|logout|refresh|register|verify-registration-otp|resend-registration-otp|verify-email|verify-mobile|forgot-password|verify-otp|reset-password)(\/|$|\?)/i.test(
        requestUrl
      );

    const token =
      localStorage.getItem("astra_access_token") ||
      sessionStorage.getItem("astra_access_token");

    if (status === 401 && !isAuthenticationEndpoint && token && !error.config?._retry) {
      error.config._retry = true;
      try {
        refreshPromise ||= apiClient.post("/auth/refresh", {}, {
          skipAuthRefresh: true,
        }).finally(() => { refreshPromise = undefined; });
        const response = await refreshPromise;
        const nextToken = response.data?.accessToken;
        if (!nextToken) throw new Error("Refresh response did not contain an access token.");

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
        window.dispatchEvent(new CustomEvent("astra:session-expired"));
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;

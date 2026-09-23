import axios from "axios";

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1",
  headers: { "Content-Type": "application/json" }
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("astra_access_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let refreshing = false;
let queued = [];

const resolveQueue = (error, token = null) => {
  queued.forEach(({ resolve, reject }) => error ? reject(error) : resolve(token));
  queued = [];
};

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    if (error.response?.status !== 401 || original?._retry || original?.url?.includes("/auth/refresh")) {
      return Promise.reject(error);
    }

    const refreshToken = localStorage.getItem("astra_refresh_token");
    if (!refreshToken) return Promise.reject(error);

    if (refreshing) {
      return new Promise((resolve, reject) => queued.push({ resolve, reject }))
        .then((token) => {
          original.headers.Authorization = `Bearer ${token}`;
          return http(original);
        });
    }

    original._retry = true;
    refreshing = true;
    try {
      const { data } = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1"}/auth/refresh`,
        { refreshToken }
      );
      localStorage.setItem("astra_access_token", data.accessToken);
      if (data.refreshToken) localStorage.setItem("astra_refresh_token", data.refreshToken);
      resolveQueue(null, data.accessToken);
      original.headers.Authorization = `Bearer ${data.accessToken}`;
      return http(original);
    } catch (refreshError) {
      resolveQueue(refreshError);
      localStorage.removeItem("astra_access_token");
      localStorage.removeItem("astra_refresh_token");
      localStorage.removeItem("astra_user");
      window.location.href = "/login";
      return Promise.reject(refreshError);
    } finally {
      refreshing = false;
    }
  }
);

export default http;
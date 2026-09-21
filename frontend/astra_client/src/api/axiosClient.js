import axios from "axios";

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1",
  headers: { "Content-Type": "application/json" }, timeout: 15000,
});

let refreshing = null;
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("astra_access_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  const requestId = crypto?.randomUUID?.() || `${Date.now()}-${Math.random()}`;
  config.headers["X-Request-Id"] = requestId;
  return config;
});

axiosClient.interceptors.response.use((response) => response, async (error) => {
  const original = error.config;
  if (error.response?.status !== 401 || original?._retry || original?.url?.includes("/auth/")) throw error;
  const refresh = localStorage.getItem("astra_refresh_token");
  if (!refresh) throw error;
  original._retry = true;
  try {
    refreshing ||= axios.post(`${axiosClient.defaults.baseURL}/auth/refresh`, { refreshToken: refresh }, { headers: { "Content-Type": "application/json" } });
    const { data } = await refreshing; refreshing = null;
    localStorage.setItem("astra_access_token", data.accessToken);
    localStorage.setItem("astra_refresh_token", data.refreshToken);
    const { accessToken, refreshToken, ...user } = data;
    localStorage.setItem("astra_user", JSON.stringify(user));
    original.headers.Authorization = `Bearer ${accessToken}`;
    return axiosClient(original);
  } catch (refreshError) {
    refreshing = null;
    localStorage.removeItem("astra_access_token"); localStorage.removeItem("astra_refresh_token"); localStorage.removeItem("astra_user");
    window.dispatchEvent(new Event("astra:session-expired"));
    throw refreshError;
  }
});
export default axiosClient;

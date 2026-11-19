
import apiClient from "../api/apiClient";

const BASE_PATH = "/admin/monitoring";

const monitorConfig = {
  skipApiMonitor: true,
};

const apiMonitorService = {
  async getStats() {
    const response = await apiClient.get(
      `${BASE_PATH}/stats`,
      monitorConfig
    );

    return response.data;
  },

  async getLogs({
    page = 0,
    size = 20,
    source = "ALL",
    status,
    search,
  } = {}) {
    const params = {
      page,
      size,
      source: source === "ALL" ? undefined : source,
      status: status === "ALL" ? undefined : status,
      search: search?.trim() || undefined,
    };

    const response = await apiClient.get(
      `${BASE_PATH}/logs`,
      {
        ...monitorConfig,
        params,
      }
    );

    return response.data;
  },

  async getLogDetails(id) {
    const response = await apiClient.get(
      `${BASE_PATH}/logs/${id}`,
      monitorConfig
    );

    return response.data;
  },
};

export default apiMonitorService;

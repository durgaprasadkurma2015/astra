
import api from "./api";

const inventoryService = {
  async getWarehouses() {
    const response = await api.get("/admin/inventory/warehouses");
    return response.data;
  },

  async getWarehouseStock(warehouseId) {
    const response = await api.get(
      `/admin/inventory/warehouses/${warehouseId}/stock`
    );
    return response.data;
  },

  async createWarehouse(data) {
    const response = await api.post("/admin/inventory/warehouses", data);
    return response.data;
  },

  async transferStock(data) {
    const response = await api.post("/admin/inventory/transfers", data);
    return response.data;
  },

  async getForecast(leadTimeDays = 14) {
    const response = await api.get("/admin/inventory/forecast", {
      params: { leadTimeDays },
    });
    return response.data;
  },
};

export default inventoryService;

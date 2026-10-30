
import api from "./api";

const adminService = {
  async registerAdmin(adminData) {
    const response = await api.post("/admin/admins", adminData);
    return response.data;
  },
};

export default adminService;

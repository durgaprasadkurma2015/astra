import api from "./api";

const addressService = {
  async list() {
    const response = await api.get("/addresses");
    return response.data;
  },
  async create(address) {
    const response = await api.post("/addresses", address);
    return response.data;
  },
  async update(id, address) {
    const response = await api.put(`/addresses/${encodeURIComponent(id)}`, address);
    return response.data;
  },
  async makeDefault(id) {
    const response = await api.put(`/addresses/${encodeURIComponent(id)}/default`);
    return response.data;
  },
  async remove(id) {
    await api.delete(`/addresses/${encodeURIComponent(id)}`);
  },
};

export default addressService;


import api from "./api";

const cartService = {
  async getCart() {
    const response = await api.get("/cart");
    return response.data;
  },

  async addItem(productId, quantity = 1) {
    const response = await api.post("/cart/items", {
      productId,
      quantity,
    });

    return response.data;
  },

  async updateQuantity(productId, quantity) {
    const response = await api.put(
      `/cart/items/${encodeURIComponent(productId)}`,
      { quantity }
    );

    return response.data;
  },

  async removeItem(productId) {
    const response = await api.delete(
      `/cart/items/${encodeURIComponent(productId)}`
    );

    return response.data;
  },
};

export default cartService;

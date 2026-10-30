import api from "./api";

const wishlistService = {
  async getWishlist() {
    const response = await api.get("/wishlist");
    return response.data;
  },

  async addItem(productId) {
    if (productId == null) {
      throw new Error("Product ID is required to add an item to the wishlist.");
    }

    const response = await api.post(
      `/wishlist/items/${encodeURIComponent(productId)}`
    );

    return response.data;
  },

  async removeItem(productId) {
    if (productId == null) {
      throw new Error("Product ID is required to remove an item from the wishlist.");
    }

    const response = await api.delete(
      `/wishlist/items/${encodeURIComponent(productId)}`
    );

    return response.data;
  },

  async checkItem(productId) {
    if (productId == null) {
      throw new Error("Product ID is required to check the wishlist.");
    }

    const response = await api.get(
      `/wishlist/items/${encodeURIComponent(productId)}/exists`
    );

    return response.data;
  },
};

export default wishlistService;
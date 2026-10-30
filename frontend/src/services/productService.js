
import api from "./api";

const productService = {
  
async updateStock(id, stockQuantity, reason) {
  const response = await api.patch(
    `/admin/products/${encodeURIComponent(id)}/stock`,
    { stockQuantity, reason }
  );
  return response.data;
},

async getStockMovements(id, page = 0, size = 20) {
  const response = await api.get(
    `/admin/products/${encodeURIComponent(id)}/stock-movements`,
    { params: { page, size } }
  );
  return response.data;
},

  // Public product APIs
  async getProducts() {
    const response = await api.get("/products");
    return response.data;
  },

  async getProduct(id) {
    const response = await api.get(
      `/products/${encodeURIComponent(id)}`
    );
    return response.data;
  },

  async getCategories() {
    const response = await api.get("/products/categories");
    return response.data;
  },

  async getNewArrivals() {
    const response = await api.get("/products/new-arrivals");
    return response.data;
  },

  async getSubcategories(category) {
    const response = await api.get("/products/subcategories", {
      params: { category },
    });
    return response.data;
  },

  async searchProducts(filters = {}) {
    const params = {
      page: filters.page ?? 0,
      size: filters.size ?? 12,
      sortBy: filters.sortBy ?? "id",
      direction: filters.direction ?? "asc",
    };

    if (filters.keyword?.trim()) {
      params.keyword = filters.keyword.trim();
    }

    if (filters.category) {
      params.category = filters.category;
    }

    if (filters.subcategory) {
      params.subcategory = filters.subcategory;
    }

    if (filters.minPrice !== "" && filters.minPrice != null) {
      params.minPrice = filters.minPrice;
    }

    if (filters.maxPrice !== "" && filters.maxPrice != null) {
      params.maxPrice = filters.maxPrice;
    }

    if (filters.inStock) {
      params.inStock = true;
    }

    const response = await api.get("/products/search", { params });
    return response.data;
  },

  // Admin product APIs
  async getAdminProducts() {
    const response = await api.get("/admin/products");
    return response.data;
  },

  async createProduct(productData) {
    const response = await api.post("/admin/products", productData);
    return response.data;
  },

  async updateProduct(id, productData) {
    const response = await api.put(
      `/admin/products/${encodeURIComponent(id)}`,
      productData
    );
    return response.data;
  },

  async updateProductStatus(id, active) {
    const response = await api.patch(
      `/admin/products/${encodeURIComponent(id)}/active`,
      { active }
    );
    return response.data;
  },
};

export default productService;

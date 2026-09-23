import http from "./http";

export const authApi = {
  register: (data) => http.post("/auth/register", data),
  login: (data) => http.post("/auth/login", data),
  verifyOtp: (data) => http.post("/auth/verify-otp", data),
  refresh: (data) => http.post("/auth/refresh", data),
  logout: (data) => http.post("/auth/logout", data),
  sendSmsOtp: (data) => http.post("/auth/sms/send-otp", data),
  verifySmsOtp: (data) => http.post("/auth/sms/verify-otp", data),
  forgotPassword: (data) => http.post("/auth/forgot-password", data),
  resetPassword: (data) => http.post("/auth/reset-password", data),
  google: (data) => http.post("/auth/google", data)
};

export const productApi = {
  list: (params = {}) => http.get("/products", { params }),
  get: (id) => http.get(`/products/${id}`),
  create: (data) => http.post("/products", data),
  update: (id, data) => http.put(`/products/${id}`, data),
  remove: (id) => http.delete(`/products/${id}`),
  search: (params) => http.get("/products/search", { params }),
  featured: () => http.get("/products/featured"),
  bestSellers: () => http.get("/products/best-sellers"),
  topRated: () => http.get("/products/top-rated")
};

export const categoryApi = {
  list: () => http.get("/categories"),
  get: (id) => http.get(`/categories/${id}`),
  create: (data) => http.post("/categories", data),
  update: (id, data) => http.put(`/categories/${id}`, data),
  remove: (id) => http.delete(`/categories/${id}`)
};

export const searchApi = {
  products: (params) => http.get("/search/products", { params }),
  autocomplete: (q) => http.get("/search/autocomplete", { params: { q } }),
  suggestions: (q) => http.get("/search/suggestions", { params: { q } }),
  reindex: () => http.post("/admin/search/reindex/products")
};

export const cartApi = {
  get: () => http.get("/cart"),
  add: (data) => http.post("/cart/items", data),
  update: (itemId, data) => http.put(`/cart/items/${itemId}`, data),
  remove: (itemId) => http.delete(`/cart/items/${itemId}`),
  clear: () => http.delete("/cart")
};

export const wishlistApi = {
  get: () => http.get("/wishlist"),
  add: (data) => http.post("/wishlist/items", data),
  removeItem: (itemId) => http.delete(`/wishlist/items/${itemId}`),
  removeProduct: (productId) => http.delete(`/wishlist/products/${productId}`),
  exists: (productId) => http.get(`/wishlist/products/${productId}/exists`),
  clear: () => http.delete("/wishlist"),
  moveToCart: (itemId) => http.post(`/wishlist/items/${itemId}/cart`)
};

export const orderApi = {
  create: (data) => http.post("/orders", data),
  list: (params) => http.get("/orders", { params }),
  get: (id) => http.get(`/orders/${id}`),
  cancel: (id) => http.put(`/orders/${id}/cancel`),
  status: (id) => http.get(`/orders/${id}/status`)
};

export const paymentApi = {
  initiate: (orderId, data = {}) => http.post(`/payments/orders/${orderId}/initiate`, data),
  get: (paymentId) => http.get(`/payments/${paymentId}`),
  byOrder: (orderId) => http.get(`/payments/orders/${orderId}`),
  success: (paymentId, data = {}) => http.post(`/payments/${paymentId}/success`, data),
  fail: (paymentId, data = {}) => http.post(`/payments/${paymentId}/fail`, data),
  refund: (paymentId, data = {}) => http.post(`/payments/${paymentId}/refund`, data)
};

export const reviewApi = {
  list: (productId, params) => http.get(`/products/${productId}/reviews`, { params }),
  get: (productId, reviewId) => http.get(`/products/${productId}/reviews/${reviewId}`),
  mine: (productId) => http.get(`/products/${productId}/reviews/me`),
  summary: (productId) => http.get(`/products/${productId}/reviews/summary`),
  create: (productId, data) => http.post(`/products/${productId}/reviews`, data),
  update: (productId, reviewId, data) => http.put(`/products/${productId}/reviews/${reviewId}`, data),
  remove: (productId, reviewId) => http.delete(`/products/${productId}/reviews/${reviewId}`)
};

export const userApi = {
  me: () => http.get("/users/me"),
  update: (data) => http.put("/users/me/profile", data),
  password: (data) => http.put("/users/me/password", data),
  addresses: () => http.get("/users/me/addresses"),
  addAddress: (data) => http.post("/users/me/addresses", data),
  updateAddress: (id, data) => http.put(`/users/me/addresses/${id}`, data),
  removeAddress: (id) => http.delete(`/users/me/addresses/${id}`),
  defaultAddress: (id) => http.put(`/users/me/addresses/${id}/default`)
};

export const addressApi = {
  list: () => http.get("/addresses"),
  create: (data) => http.post("/addresses", data),
  update: (id, data) => http.put(`/addresses/${id}`, data),
  remove: (id) => http.delete(`/addresses/${id}`),
  setDefault: (id) => http.put(`/addresses/${id}/default`)
};

export const notificationApi = {
  list: (params) => http.get("/notifications", { params }),
  unread: () => http.get("/notifications/unread"),
  count: () => http.get("/notifications/unread/count"),
  read: (id) => http.put(`/notifications/${id}/read`),
  readAll: () => http.put("/notifications/read-all"),
  send: (data) => http.post("/notifications/send", data)
};

export const recommendationApi = {
  trending: () => http.get("/recommendations/trending"),
  forYou: () => http.get("/recommendations/for-you"),
  recentlyViewed: () => http.get("/recommendations/recently-viewed"),
  recordView: (slug) => http.post(`/recommendations/recently-viewed/${slug}`),
  similar: (id) => http.get(`/recommendations/similar/${id}`)
};

export const inventoryApi = {
  product: (id) => http.get(`/inventory/products/${id}`),
  productMovements: (id) => http.get(`/inventory/products/${id}/movements`),
  orderMovements: (id) => http.get(`/inventory/orders/${id}/movements`),
  restock: (id, data) => http.post(`/inventory/products/${id}/restock`, data),
  adjust: (id, data) => http.put(`/inventory/products/${id}/adjust`, data)
};

export const shipmentApi = {
  create: (orderId, data) => http.post(`/shipments/orders/${orderId}`, data),
  mine: () => http.get("/shipments/my"),
  get: (id) => http.get(`/shipments/${id}`),
  byOrder: (id) => http.get(`/shipments/orders/${id}`),
  track: (trackingNumber) => http.get(`/shipments/track/${trackingNumber}`),
  updateStatus: (id, data) => http.put(`/shipments/${id}/status`, data),
  byStatus: (status) => http.get(`/shipments/status/${status}`)
};

export const mediaApi = {
  upload: (formData) => http.post("/media/upload", formData, { headers: { "Content-Type": "multipart/form-data" } })
};

export const productImageApi = {
  upload: (productId, formData) => http.post(`/products/${productId}/images`, formData, { headers: { "Content-Type": "multipart/form-data" } }),
  addUrl: (productId, data) => http.post(`/products/${productId}/images/url`, data),
  list: (productId) => http.get(`/products/${productId}/images`),
  update: (productId, imageId, data) => http.put(`/products/${productId}/images/${imageId}`, data),
  remove: (productId, imageId) => http.delete(`/products/${productId}/images/${imageId}`),
  primary: (productId, imageId) => http.put(`/products/${productId}/images/${imageId}/primary`),
  reorder: (productId, data) => http.put(`/products/${productId}/images/reorder`, data)
};

export const sellerApi = {
  register: (data) => http.post("/sellers/register", data),
  me: () => http.get("/sellers/me")
};

export const adminApi = {
  dashboard: () => http.get("/admin/dashboard"),
  users: (params) => http.get("/admin/users", { params }),
  user: (id) => http.get(`/admin/users/${id}`),
  enableUser: (id) => http.put(`/admin/users/${id}/enable`),
  disableUser: (id) => http.put(`/admin/users/${id}/disable`),
  products: (params) => http.get("/admin/products", { params }),
  product: (id) => http.get(`/admin/products/${id}`),
  activateProduct: (id) => http.put(`/admin/products/${id}/activate`),
  deactivateProduct: (id) => http.put(`/admin/products/${id}/deactivate`),
  featuredProduct: (id) => http.put(`/admin/products/${id}/featured`),
  restockProduct: (id, data) => http.post(`/admin/products/${id}/inventory/restock`, data),
  adjustInventory: (id, data) => http.put(`/admin/products/${id}/inventory/adjust`, data),
  orders: (params) => http.get("/admin/orders", { params }),
  order: (id) => http.get(`/admin/orders/${id}`),
  updateOrderStatus: (id, data) => http.put(`/admin/orders/${id}/status`, data),
  sellers: (params) => http.get("/admin/sellers", { params }),
  seller: (id) => http.get(`/admin/sellers/${id}`),
  approveSeller: (id) => http.put(`/admin/sellers/${id}/approve`),
  rejectSeller: (id) => http.put(`/admin/sellers/${id}/reject`),
  suspendSeller: (id) => http.put(`/admin/sellers/${id}/suspend`),
  activateSeller: (id) => http.put(`/admin/sellers/${id}/activate`),
  closeSeller: (id) => http.put(`/admin/sellers/${id}/close`),
  audit: (params) => http.get("/admin/audit", { params }),
  auditItem: (id) => http.get(`/admin/audit/${id}`)
};
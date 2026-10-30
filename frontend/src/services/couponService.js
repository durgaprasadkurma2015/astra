import api from "./api";

const BASE_URL = "/admin/coupons";

const couponService = {
  async getAllCoupons() {
    const response = await api.get(BASE_URL);
    return response.data;
  },

  async createCoupon(coupon) {
    const response = await api.post(BASE_URL, coupon);
    return response.data;
  },

  async updateActiveStatus(id, active) {
    const response = await api.patch(`${BASE_URL}/${id}/active`, {
      active,
    });

    return response.data;
  },

  async applyCoupon(couponCode, subtotal) {
    const response = await api.post("/coupons/apply", {
      couponCode,
      subtotal,
    });

    return response.data;
  },
};

export default couponService;
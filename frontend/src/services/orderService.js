
import api from "./api";

const CHECKOUT_KEY_STORAGE = "astra_pending_checkout_key";

function getOrCreateCheckoutKey() {
  let key = sessionStorage.getItem(CHECKOUT_KEY_STORAGE);

  if (!key) {
    key =
      typeof crypto !== "undefined" &&
      typeof crypto.randomUUID === "function"
        ? crypto.randomUUID()
        : `${Date.now()}-${Math.random().toString(36).slice(2)}`;

    sessionStorage.setItem(CHECKOUT_KEY_STORAGE, key);
  }

  return key;
}

const orderService = {
  async checkout(
    customerName,
    phoneNumber,
    shippingAddress,
    couponCode = "",
    paymentMethod = "COD"
  ) {
    const payload = {
      customerName,
      phoneNumber,
      shippingAddress,
      paymentMethod,
    };

    if (couponCode.trim()) {
      payload.couponCode = couponCode.trim().toUpperCase();
    }

    const idempotencyKey = getOrCreateCheckoutKey();

    const response = await api.post("/orders/checkout", payload, {
      headers: {
        "Idempotency-Key": idempotencyKey,
      },
    });

    // Clear the key only after a successful checkout response.
    if (paymentMethod !== "RAZORPAY") {
      sessionStorage.removeItem(CHECKOUT_KEY_STORAGE);
    }

    return response.data;
  },

  async getOrder(orderNumber) {
    const response = await api.get(
      `/orders/${encodeURIComponent(orderNumber)}`
    );

    return response.data;
  },

  async getMyOrders(page = 0, size = 10) {
    const response = await api.get("/orders/my-orders/page", {
      params: { page, size },
    });
    return response.data;
  },

  async cancelOrder(orderNumber) {
    const response = await api.put(
      `/orders/${encodeURIComponent(orderNumber)}/cancel`
    );

    return response.data;
  },

  async verifyRazorpayPayment(orderNumber, payment) {
    const response = await api.post(
      `/orders/${encodeURIComponent(orderNumber)}/payment/verify`,
      {
        razorpayOrderId: payment.razorpay_order_id,
        razorpayPaymentId: payment.razorpay_payment_id,
        razorpaySignature: payment.razorpay_signature,
      }
    );
    sessionStorage.removeItem(CHECKOUT_KEY_STORAGE);
    return response.data;
  },

  async getAdminOrderDetails(orderNumber) {
    if (!orderNumber) {
      throw new Error("Order number is required.");
    }

    const response = await api.get(
      `/admin/orders/${encodeURIComponent(orderNumber)}`
    );

    return response.data;
  },

  async getAdminOrders({ page = 0, size = 20, query = "", status = "ALL" } = {}) {
    const response = await api.get("/admin/orders/page", {
      params: { page, size, query: query || undefined, status },
    });
    return response.data;
  },

  async updateAdminOrderStatus(orderNumber, status) {
    if (!orderNumber) {
      throw new Error("Order number is required.");
    }

    const response = await api.put(
      `/admin/orders/${encodeURIComponent(orderNumber)}/status`,
      { status }
    );

    return response.data;
  },
};

export default orderService;

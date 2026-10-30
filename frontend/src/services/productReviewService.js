
import api from "./api";

const productReviewService = {
  async getReviews(productId) {
    const response = await api.get(
      `/products/${encodeURIComponent(productId)}/reviews`
    );

    return response.data;
  },

  async getSummary(productId) {
    const response = await api.get(
      `/products/${encodeURIComponent(productId)}/reviews/summary`
    );

    return response.data;
  },

  async createReview(productId, reviewData) {
    const response = await api.post(
      `/products/${encodeURIComponent(productId)}/reviews`,
      {
        rating: reviewData.rating,
        comment: reviewData.comment,
      }
    );

    return response.data;
  },
};

export default productReviewService;

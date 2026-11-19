import apiClient from "../api/apiClient";

export const notificationService = {

  async getNotifications() {
    const response = await apiClient.get("/notifications");
    return response.data;
  },

  async getUnreadCount() {
    const response = await apiClient.get(
      "/notifications/unread-count"
    );

    return response.data.count;
  },

  async markAsRead(id) {
    const response = await apiClient.patch(
      `/notifications/${id}/read`
    );

    return response.data;
  },

  async markAllAsRead() {
    const response = await apiClient.patch(
      "/notifications/read-all"
    );

    return response.data;
  }

};
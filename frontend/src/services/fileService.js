import api from "./api";

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  "http://localhost:8080/api/v1";

function absoluteFileUrl(path) {
  return new URL(path, `${API_BASE.replace(/\/+$/, "")}/`).toString();
}

const fileService = {
  async uploadProductImage(productId, file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await api.post(
      "/files/admin/upload",
      formData,
      {
        params: {
          purpose: "PRODUCT_IMAGE",
          entityId: productId,
        },
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 60000,
      }
    );

    return {
      ...response.data,
      contentUrl: absoluteFileUrl(response.data.contentUrl),
    };
  },

  async uploadProfilePicture(file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await api.post(
      "/files/me/profile-picture",
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 60000,
      }
    );

    return response.data;
  },

  async uploadDocument(file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await api.post(
      "/files/me/documents",
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 60000,
      }
    );

    return response.data;
  },

  async getMyFiles() {
    const response = await api.get("/files/me");
    return response.data;
  },

  async getAdminFiles() {
    const response = await api.get("/files/admin");
    return response.data;
  },

  async deleteMyFile(id) {
    await api.delete(`/files/me/${encodeURIComponent(id)}`);
  },

  async deleteAdminFile(id) {
    await api.delete(`/files/admin/${encodeURIComponent(id)}`);
  },

  async downloadFile(id) {
    const response = await api.get(
      `/files/${encodeURIComponent(id)}/download`,
      { responseType: "blob", timeout: 60000 }
    );

    return response.data;
  },

  async downloadProductImage(id) {
    const response = await api.get(
      `/files/public/${encodeURIComponent(id)}/download`,
      { responseType: "blob" }
    );

    return response.data;
  },

  async getProfilePictureContent(id) {
    const response = await api.get(
      `/files/${encodeURIComponent(id)}/download`,
      { responseType: "blob" }
    );

    return response.data;
  },
};

export default fileService;
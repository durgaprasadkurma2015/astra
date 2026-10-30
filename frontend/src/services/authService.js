
import api from "./api";

const authService = {
  register: async (data) => {
    const response = await api.post("/auth/register", data);
    return response.data;
  },

  verifyRegistrationOtp: async (email, otp) => {
    const response = await api.post("/auth/verify-registration-otp", {
      email,
      otp,
    });
    return response.data;
  },
verifyOtp: async (email, otp) => {
  const response = await api.post("/auth/verify-otp", {
    email,
    otp,
  });
  return response.data;
},

resetPassword: async (resetToken, newPassword) => {
  const response = await api.post("/auth/reset-password", {
    resetToken,
    newPassword,
  });
  return response.data;
},

  resendRegistrationOtp: async (email) => {
    const response = await api.post("/auth/resend-registration-otp", {
      email,
    });
    return response.data;
  },

  login: async (credentials) => {
    const response = await api.post("/auth/login", credentials);
    return response.data;
  },

  logout: async () => {
    await api.post("/auth/logout");
  },

  

  
getCurrentUser: async () => {
  const response = await api.get("/auth/me");
  return response.data;
},

updateProfile: async (profile) => {
  const response = await api.put("/auth/me", profile);
  return response.data;
},

changePassword: async (currentPassword, newPassword) => {
  const response = await api.post("/auth/change-password", {
    currentPassword,
    newPassword,
  });

  return response.data;
},

  forgotPassword: async (email) => {
  const response = await api.post("/auth/forgot-password", {
    email,
  });
  return response.data;
},
};




export default authService;

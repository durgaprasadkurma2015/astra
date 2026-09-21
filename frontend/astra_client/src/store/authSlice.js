import { createSlice } from "@reduxjs/toolkit";

const storedUser = localStorage.getItem("astra_user");

const initialState = {
  user: storedUser ? JSON.parse(storedUser) : null,
  accessToken: localStorage.getItem("astra_access_token"),
  refreshToken: localStorage.getItem("astra_refresh_token"),
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setSession(state, action) {
      const { accessToken, refreshToken, ...user } = action.payload;

      state.user = user;
      state.accessToken = accessToken;
      state.refreshToken = refreshToken;

      localStorage.setItem("astra_user", JSON.stringify(user));
      localStorage.setItem("astra_access_token", accessToken);
      localStorage.setItem("astra_refresh_token", refreshToken);
    },
    logout(state) {
      state.user = null;
      state.accessToken = null;
      state.refreshToken = null;

      localStorage.removeItem("astra_user");
      localStorage.removeItem("astra_access_token");
      localStorage.removeItem("astra_refresh_token");
    },
  },
});

export const { setSession, logout } = authSlice.actions;
export default authSlice.reducer;

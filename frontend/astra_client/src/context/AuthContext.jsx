import {
  useEffect,
  useState
} from "react";

import {
  authApi,
  userApi
} from "../api/api";

import { AuthContext } from "./authContext";


export default function AuthProvider({
  children
}) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(
        localStorage.getItem(
          "astra_user"
        ) || "null"
      );
    } catch {
      return null;
    }
  });


  const saveAuth = (data) => {
    localStorage.setItem(
      "astra_access_token",
      data.accessToken
    );

    localStorage.setItem(
      "astra_refresh_token",
      data.refreshToken
    );

    const nextUser = {
      id: data.id,
      name: data.name,
      email: data.email,
      phone: data.phone,
      role: data.role
    };

    localStorage.setItem(
      "astra_user",
      JSON.stringify(nextUser)
    );

    setUser(nextUser);
  };


  const login = async (credentials) => {
    const response =
      await authApi.login(credentials);

    saveAuth(response.data);

    return response.data;
  };


  const register = async (data) => {
    return authApi.register(data);
  };


  const logout = async () => {
    const refreshToken =
      localStorage.getItem(
        "astra_refresh_token"
      );

    try {
      if (refreshToken) {
        await authApi.logout({
          refreshToken
        });
      }
    } catch (error) {
      console.error(
        "Logout request failed:",
        error
      );
    } finally {
      localStorage.removeItem(
        "astra_access_token"
      );

      localStorage.removeItem(
        "astra_refresh_token"
      );

      localStorage.removeItem(
        "astra_user"
      );

      setUser(null);
    }
  };


  useEffect(() => {
    const token =
      localStorage.getItem(
        "astra_access_token"
      );

    if (!token) {
      return;
    }

    const storedUser =
      localStorage.getItem(
        "astra_user"
      );

    if (storedUser) {
      return;
    }

    userApi
      .me()
      .then(({ data }) => {
        localStorage.setItem(
          "astra_user",
          JSON.stringify(data)
        );

        setUser(data);
      })
      .catch((error) => {
        console.error(
          "Unable to restore user:",
          error
        );
      });
  }, []);


  const value = {
    user,
    login,
    register,
    logout,
    saveAuth
  };


  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}


import {
  createContext,
  useState,
  useEffect,
  useCallback,
} from "react";

import authService from "../services/authService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function restoreSession() {
      const token =
        localStorage.getItem("astra_access_token") ||
        sessionStorage.getItem("astra_access_token");

      if (!token) {
        if (!cancelled) {
          setUser(null);
          setLoading(false);
        }
        return;
      }

      try {
        const profile = await authService.getCurrentUser();

        if (!cancelled) {
          setUser(profile);
        }
      } catch {
        localStorage.removeItem("astra_access_token");
        sessionStorage.removeItem("astra_access_token");

        if (!cancelled) {
          setUser(null);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    restoreSession();

    return () => {
      cancelled = true;
    };
  }, []);

  const login = useCallback(async (credentials, rememberMe = false) => {
    const result = await authService.login({ ...credentials, rememberMe });

    if (!result.accessToken) {
      throw new Error("Login succeeded without an access token.");
    }

    // Keep a single token copy so opting out of "Remember me" always
    // gives the user a tab-scoped session.
    localStorage.removeItem("astra_access_token");
    sessionStorage.removeItem("astra_access_token");
    const storage = rememberMe ? localStorage : sessionStorage;
    storage.setItem("astra_access_token", result.accessToken);

    setUser({
      id: result.userId,
      fullName: result.fullName,
      email: result.email,
      role: result.role,
    });

    return result;
  }, []);

  const logout = useCallback(() => {
    void authService.logout().catch(() => {});
    sessionStorage.removeItem("astra_access_token");
    localStorage.removeItem("astra_access_token");
    sessionStorage.removeItem("astra_password_reset_token");
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export default AuthContext;

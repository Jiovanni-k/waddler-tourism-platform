import { createContext, useContext, useState, useEffect } from "react";
import axiosInstance from "../api/axiosInstance";
import { login as apiLogin, googleLogin as apiGoogleLogin } from "../api/authApi";

// ─── Roles (match Spring Security roles exactly) ─────────────────────────────
export const ROLES = {
  ADMIN:   "ADMIN",
  MANAGER: "HOTEL_MANAGER",
  USER:    "USER",
};

// ─── Context ──────────────────────────────────────────────────────────────────
const AuthContext = createContext(null);

// Strip "ROLE_" prefix that Spring Security sometimes adds (e.g. ROLE_ADMIN → ADMIN)
const normalizeUser = (userData) => ({
  ...userData,
  role: userData?.role?.replace(/^ROLE_/, "") ?? userData?.role,
});

// ── Dev bypass ────────────────────────────────────────────────────────────────
const DEV_BYPASS = import.meta.env.DEV && import.meta.env.VITE_DEV_ADMIN_BYPASS === "true";
const DEV_FAKE_ADMIN = { id: 0, email: "dev@admin.local", firstName: "Dev", lastName: "Admin", role: "ADMIN" };

export const AuthProvider = ({ children }) => {
  const [user,    setUser]    = useState(DEV_BYPASS ? DEV_FAKE_ADMIN : null);
  const [loading, setLoading] = useState(!DEV_BYPASS); // skip loading if bypassing

  // Rehydrate from localStorage on page refresh
  useEffect(() => {
    const savedToken = localStorage.getItem("waddler_token");
    const savedUser  = localStorage.getItem("waddler_user");
    if (savedToken && savedUser) {
      axiosInstance.defaults.headers.common["Authorization"] = `Bearer ${savedToken}`;
      setUser(normalizeUser(JSON.parse(savedUser)));
    }
    setLoading(false);
  }, []);

  // ── Login ────────────────────────────────────────────────────────────────
  // Backend returns: { accessToken, refreshToken, tokenType, expiresInSeconds }
  // There is NO user object in the login response — we call /auth/me after.
  const login = async (credentials) => {
    console.log("[Auth] Attempting login with:", credentials.email);
    const res = await apiLogin(credentials);
    const { accessToken, refreshToken } = res.data;
    console.log("[Auth] Login success — tokens received, accessToken:", accessToken?.slice(0, 20) + "...");

    // Store tokens
    localStorage.setItem("waddler_token",        accessToken);
    localStorage.setItem("waddler_refreshToken", refreshToken || "");

    // Attach to axios so the /auth/me call is authenticated
    axiosInstance.defaults.headers.common["Authorization"] = `Bearer ${accessToken}`;

    // Fetch the full user profile
    const meRes  = await axiosInstance.get("/auth/me");
    console.log("[Auth] /auth/me raw response:", meRes.data);
    const userData = normalizeUser(meRes.data);
    console.log("[Auth] Normalized user:", userData);
    console.log("[Auth] Role being used for redirect:", userData.role);

    localStorage.setItem("waddler_user", JSON.stringify(userData));
    setUser(userData);

    return userData; // caller uses role to redirect
  };

  // ── Google Login ─────────────────────────────────────────────────────────
  const loginWithGoogle = async (idToken) => {
    const res = await apiGoogleLogin(idToken);
    const { accessToken, refreshToken } = res.data;

    localStorage.setItem("waddler_token",        accessToken);
    localStorage.setItem("waddler_refreshToken", refreshToken || "");
    axiosInstance.defaults.headers.common["Authorization"] = `Bearer ${accessToken}`;

    const meRes   = await axiosInstance.get("/auth/me");
    const userData = normalizeUser(meRes.data);
    localStorage.setItem("waddler_user", JSON.stringify(userData));
    setUser(userData);

    return userData;
  };

  // ── Logout ───────────────────────────────────────────────────────────────
  const logout = () => {
    localStorage.removeItem("waddler_token");
    localStorage.removeItem("waddler_refreshToken");
    localStorage.removeItem("waddler_user");
    delete axiosInstance.defaults.headers.common["Authorization"];
    setUser(null);
  };

  // ── Helper ───────────────────────────────────────────────────────────────
  // user.role comes back from /auth/me as e.g. "USER", "HOTEL_MANAGER", "ADMIN"
  const hasRole = (role) => user?.role === role;

  return (
    <AuthContext.Provider value={{ user, loading, login, loginWithGoogle, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
};

// ─── Hook ─────────────────────────────────────────────────────────────────────
export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
};

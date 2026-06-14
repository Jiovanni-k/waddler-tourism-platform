import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

// ── Dev bypass ────────────────────────────────────────────────────────────────
// Set VITE_DEV_ADMIN_BYPASS=true in .env.local to skip all auth checks.
// Remove it when auth is working correctly.
const DEV_BYPASS = import.meta.env.DEV && import.meta.env.VITE_DEV_ADMIN_BYPASS === "true";
const DEV_FAKE_ADMIN = {
  id: 0,
  email: "dev@admin.local",
  firstName: "Dev",
  lastName: "Admin",
  role: "ADMIN",
};

/**
 * Usage:
 *   <ProtectedRoute />                       — any logged-in user
 *   <ProtectedRoute roles={["ADMIN"]} />     — admin only
 */
const ProtectedRoute = ({ children, roles = [] }) => {
  const { user, loading } = useAuth();

  // Skip all checks in dev bypass mode
  if (DEV_BYPASS) {
    if (import.meta.env.DEV) {
      console.warn("[ProtectedRoute] DEV_BYPASS active — skipping auth. Remove VITE_DEV_ADMIN_BYPASS from .env.local when done.");
    }
    return children;
  }

  if (loading) return <div className="loading-screen">Loading...</div>;

  if (!user) return <Navigate to="/login" replace />;

  if (roles.length > 0 && !roles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default ProtectedRoute;

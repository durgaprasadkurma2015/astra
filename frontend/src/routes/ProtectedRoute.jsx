
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/useAuth.js";

export default function ProtectedRoute({
  children,
  allowedRoles,
}) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-950 text-white">
        <div className="text-center">
          <div className="mx-auto h-10 w-10 animate-spin rounded-full border-4 border-violet-400/30 border-t-violet-400" />
          <p className="mt-4 text-sm text-slate-300">
            Restoring your ASTRA session...
          </p>
        </div>
      </div>
    );
  }

  if (!user) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location }}
      />
    );
  }

  if (
    allowedRoles &&
    !allowedRoles.includes(
      String(user.role || "").replace(/^ROLE_/, "").toUpperCase()
    )
  ) {
    return <Navigate to="/" replace />;
  }

  return children ?? <Outlet />;
}

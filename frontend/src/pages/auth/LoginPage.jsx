
import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Eye, EyeOff, LockKeyhole, Mail } from "lucide-react";

import { useAuth } from "../../context/useAuth.js";
import AuthLayout from "../../layouts/AuthLayout";
import Button from "../../components/common/Button";
import { getSafeReturnLocation } from "../../utils/navigation.js";

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, loading } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const registered = location.state?.registered;
  const registeredEmail = location.state?.email;
  const passwordReset = location.state?.passwordReset;

  async function handleSubmit(event) {
    event.preventDefault();

    setError("");
    setSuccess("");
    setSubmitting(true);

    try {
      const result = await login(
        {
          email: email.trim(),
          password,
        },
        rememberMe
      );

      setSuccess("Login successful!");

      const role = String(result?.role || "")
        .replace(/^ROLE_/i, "")
        .toUpperCase();

      if (role === "ADMIN") {
        navigate("/admin", { replace: true });
        return;
      }

      navigate(getSafeReturnLocation(location.state?.from), {
        replace: true,
      });
    } catch (err) {
      const status = err.response?.status;

      if (status === 401 || status === 403) {
        setError("Invalid email or password.");
      } else if (status === 400) {
        setError("Please check your email and password.");
      } else if (!err.response) {
        setError(
          "Cannot connect to ASTRA server. Check if the backend is running."
        );
      } else {
        setError(
          err.response?.data?.detail ||
            err.response?.data?.message ||
            "Login failed. Please try again."
        );
      }
    } finally {
      setSubmitting(false);
    }
  }

  const isSubmitting = loading || submitting;

  return (
    <AuthLayout>
      <div className="mb-7 text-center">
        <h1 className="text-3xl font-bold tracking-tight">
          Welcome Back
        </h1>

        <p className="mt-2 text-sm text-slate-400">
          Sign in to continue to ASTRA
        </p>
      </div>

      {registered && (
        <div
          role="status"
          className="mb-5 rounded-xl border border-emerald-400/20 bg-emerald-500/10 p-3 text-sm text-emerald-300"
        >
          Account created successfully.

          {registeredEmail && (
            <span className="mt-1 block text-emerald-200">
              You can now log in with {registeredEmail}.
            </span>
          )}
        </div>
      )}

      {passwordReset && (
        <div
          role="status"
          className="mb-5 rounded-xl border border-emerald-400/20 bg-emerald-500/10 p-3 text-sm text-emerald-300"
        >
          Password reset successfully. Please log in with your new
          password.
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="space-y-2">
          <label
            htmlFor="email"
            className="block text-sm font-medium text-slate-200"
          >
            Email address
          </label>

          <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 focus-within:border-violet-400 focus-within:ring-2 focus-within:ring-violet-500/20">
            <Mail size={18} className="shrink-0 text-slate-400" />

            <input
              id="email"
              type="email"
              autoComplete="email"
              placeholder="you@example.com"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              className="w-full bg-transparent py-3.5 text-sm text-white outline-none placeholder:text-slate-500"
            />
          </div>
        </div>

        <div className="space-y-2">
          <label
            htmlFor="password"
            className="block text-sm font-medium text-slate-200"
          >
            Password
          </label>

          <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 focus-within:border-violet-400 focus-within:ring-2 focus-within:ring-violet-500/20">
            <LockKeyhole
              size={18}
              className="shrink-0 text-slate-400"
            />

            <input
              id="password"
              type={showPassword ? "text" : "password"}
              autoComplete="current-password"
              placeholder="Enter your password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              className="w-full bg-transparent py-3.5 text-sm text-white outline-none placeholder:text-slate-500"
            />

            <button
              type="button"
              onClick={() => setShowPassword((value) => !value)}
              aria-label={
                showPassword ? "Hide password" : "Show password"
              }
              className="text-slate-400 transition hover:text-white"
            >
              {showPassword ? (
                <EyeOff size={18} />
              ) : (
                <Eye size={18} />
              )}
            </button>
          </div>
        </div>

        <div className="flex items-center justify-between gap-3 text-sm">
          <label className="flex cursor-pointer items-center gap-2 text-slate-300">
            <input
              type="checkbox"
              checked={rememberMe}
              onChange={(event) =>
                setRememberMe(event.target.checked)
              }
              className="h-4 w-4 accent-violet-500"
            />

            Remember me
          </label>

          <Link
            to="/forgot-password"
            className="font-medium text-violet-300 hover:text-violet-200"
          >
            Forgot password?
          </Link>
        </div>

        {error && (
          <div
            role="alert"
            className="rounded-xl border border-red-400/20 bg-red-500/10 p-3 text-sm text-red-300"
          >
            {error}
          </div>
        )}

        {success && (
          <p role="status" className="text-sm text-emerald-300">
            {success}
          </p>
        )}

        <Button
          type="submit"
          disabled={isSubmitting}
          className="w-full"
        >
          {isSubmitting ? "Signing in..." : "Login"}
        </Button>
      </form>

      <div className="my-6 flex items-center gap-4">
        <div className="h-px flex-1 bg-white/10" />

        <span className="text-xs text-slate-500">NEW TO ASTRA?</span>

        <div className="h-px flex-1 bg-white/10" />
      </div>

      <p className="text-center text-sm text-slate-300">
        Don't have an account?{" "}
        <Link
          to="/register"
          className="font-semibold text-violet-300 hover:text-violet-200"
        >
          Create one
        </Link>
      </p>
    </AuthLayout>
  );
}

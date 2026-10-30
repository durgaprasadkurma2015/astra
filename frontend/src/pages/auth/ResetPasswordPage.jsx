
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Eye,
  EyeOff,
  LockKeyhole,
  CheckCircle2,
  Circle,
} from "lucide-react";

import AuthLayout from "../../layouts/AuthLayout";
import Button from "../../components/common/Button";
import authService from "../../services/authService";

function getPasswordRules(password) {
  return [
    {
      label: "At least 8 characters",
      valid: password.length >= 8 && password.length <= 72,
    },
    {
      label: "One uppercase letter",
      valid: /[A-Z]/.test(password),
    },
    {
      label: "One lowercase letter",
      valid: /[a-z]/.test(password),
    },
    {
      label: "One number",
      valid: /\d/.test(password),
    },
    {
      label: "One special character",
      valid: /[^A-Za-z0-9]/.test(password),
    },
  ];
}

function passwordIsStrong(password) {
  return getPasswordRules(password).every((rule) => rule.valid);
}

function getPasswordStrength(password) {
  if (!password) return 0;

  const rules = getPasswordRules(password);
  return rules.filter((rule) => rule.valid).length;
}

function PasswordField({
  id,
  label,
  value,
  onChange,
  showPassword,
  onToggleVisibility,
  placeholder,
  autoComplete,
}) {
  return (
    <div>
      <label
        htmlFor={id}
        className="mb-2 block text-sm font-semibold text-slate-200"
      >
        {label}
      </label>

      <div className="relative">
        <LockKeyhole
          size={19}
          aria-hidden="true"
          className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
        />

        <input
          id={id}
          type={showPassword ? "text" : "password"}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          autoComplete={autoComplete}
          required
          maxLength={72}
          className="w-full rounded-xl border border-white/10 bg-slate-800/80 py-3.5 pl-12 pr-12 text-sm text-white outline-none transition placeholder:text-slate-500 focus:border-violet-400 focus:ring-2 focus:ring-violet-400/20"
        />

        <button
          type="button"
          onClick={onToggleVisibility}
          aria-label={showPassword ? "Hide password" : "Show password"}
          className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 transition hover:text-white"
        >
          {showPassword ? (
            <EyeOff size={19} />
          ) : (
            <Eye size={19} />
          )}
        </button>
      </div>
    </div>
  );
}

export default function ResetPasswordPage() {
  const navigate = useNavigate();

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const passwordRules = getPasswordRules(password);
  const strength = getPasswordStrength(password);
  const passwordsMatch =
    confirmPassword.length > 0 && password === confirmPassword;

  const strengthLabel =
    strength <= 2 ? "Weak" : strength <= 4 ? "Medium" : "Strong";

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    const resetToken = sessionStorage.getItem(
      "astra_password_reset_token"
    );

    if (!resetToken) {
      setError("Your reset session has expired. Please start again.");
      return;
    }

    if (!passwordIsStrong(password)) {
      setError(
        "Please meet all password requirements before continuing."
      );
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);

    try {
      await authService.resetPassword(resetToken, password);

      sessionStorage.removeItem("astra_password_reset_token");

      navigate("/login", {
        replace: true,
        state: {
          passwordReset: true,
        },
      });
    } catch (err) {
      setError(
        err.response?.data?.detail ||
          err.response?.data?.message ||
          "Could not reset your password. Please request a new code."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <div className="mb-7">
        <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-2xl bg-violet-500/15 text-violet-300">
          <LockKeyhole size={24} />
        </div>

        <h1 className="text-3xl font-bold">Reset Password</h1>

        <p className="mt-3 text-sm leading-6 text-slate-400">
          Create a new, strong password for your ASTRA account.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        <PasswordField
          id="newPassword"
          label="New password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          showPassword={showPassword}
          onToggleVisibility={() =>
            setShowPassword((value) => !value)
          }
          placeholder="Enter new password"
          autoComplete="new-password"
        />

        {/* Password strength indicator */}
        <div aria-live="polite">
          <div className="mb-2 flex gap-1.5">
            {Array.from({ length: 5 }, (_, index) => (
              <div
                key={index}
                className={`h-1.5 flex-1 rounded-full transition-colors ${
                  index < strength
                    ? strength <= 2
                      ? "bg-red-500"
                      : strength <= 4
                        ? "bg-amber-400"
                        : "bg-emerald-500"
                    : "bg-slate-700"
                }`}
              />
            ))}
          </div>

          <p className="text-xs text-slate-400">
            Password strength:{" "}
            <span
              className={`font-semibold ${
                strength <= 2
                  ? "text-red-400"
                  : strength <= 4
                    ? "text-amber-400"
                    : "text-emerald-400"
              }`}
            >
              {password ? strengthLabel : "Not entered"}
            </span>
          </p>
        </div>

        {/* Password requirements */}
        <div className="grid grid-cols-1 gap-3 rounded-2xl bg-slate-800/70 p-4 sm:grid-cols-2">
          {passwordRules.map((rule) => (
            <div
              key={rule.label}
              className={`flex items-center gap-2 text-xs ${
                rule.valid ? "text-emerald-400" : "text-slate-400"
              }`}
            >
              {rule.valid ? (
                <CheckCircle2 size={16} className="shrink-0" />
              ) : (
                <Circle size={16} className="shrink-0" />
              )}
              <span>{rule.label}</span>
            </div>
          ))}
        </div>

        <PasswordField
          id="confirmNewPassword"
          label="Confirm new password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          showPassword={showConfirm}
          onToggleVisibility={() =>
            setShowConfirm((value) => !value)
          }
          placeholder="Re-enter new password"
          autoComplete="new-password"
        />

        {confirmPassword.length > 0 && (
          <p
            aria-live="polite"
            className={`flex items-center gap-2 text-xs ${
              passwordsMatch ? "text-emerald-400" : "text-red-400"
            }`}
          >
            {passwordsMatch ? (
              <CheckCircle2 size={15} />
            ) : (
              <Circle size={15} />
            )}
            {passwordsMatch
              ? "Passwords match"
              : "Passwords do not match"}
          </p>
        )}

        {error && (
          <p
            role="alert"
            className="rounded-xl border border-red-400/20 bg-red-400/10 p-3 text-sm text-red-300"
          >
            {error}
          </p>
        )}

        <Button
          type="submit"
          disabled={
            loading ||
            !passwordIsStrong(password) ||
            !passwordsMatch
          }
          className="w-full"
        >
          {loading ? "Updating password..." : "Reset Password"}
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-400">
        Remember your password?{" "}
        <Link
          to="/login"
          className="font-semibold text-violet-300 hover:text-violet-200"
        >
          Login
        </Link>
      </p>
    </AuthLayout>
  );
}

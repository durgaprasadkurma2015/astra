
import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowRight,
  Eye,
  EyeOff,
  CheckCircle2,
  Circle,
  LockKeyhole,
  Mail,
  UserRound,
} from "lucide-react";

import AuthLayout from "../../layouts/AuthLayout";
import authService from "../../services/authService";

function getPasswordRules(password) {
  return [
    { label: "At least 8 characters", valid: password.length >= 8 },
    { label: "One uppercase letter", valid: /[A-Z]/.test(password) },
    { label: "One lowercase letter", valid: /[a-z]/.test(password) },
    { label: "One number", valid: /\d/.test(password) },
    { label: "One special character", valid: /[^A-Za-z0-9]/.test(password) },
  ];
}

function PasswordRules({ password }) {
  const rules = getPasswordRules(password);

  return (
    <ul className="grid gap-2 rounded-xl border border-white/5 bg-black/10 p-3 sm:grid-cols-2">
      {rules.map((rule) => (
        <li
          key={rule.label}
          className={`flex items-center gap-2 text-xs ${
            rule.valid ? "text-emerald-400" : "text-slate-400"
          }`}
        >
          {rule.valid ? (
            <CheckCircle2 size={14} />
          ) : (
            <Circle size={14} />
          )}
          {rule.label}
        </li>
      ))}
    </ul>
  );
}

function PasswordStrength({ password }) {
  const rules = getPasswordRules(password);
  const score = rules.filter((rule) => rule.valid).length;

  const strengths = [
    { label: "Enter a password", color: "bg-slate-700" },
    { label: "Very weak", color: "bg-red-500" },
    { label: "Weak", color: "bg-orange-500" },
    { label: "Fair", color: "bg-yellow-400" },
    { label: "Good", color: "bg-lime-500" },
    { label: "Strong", color: "bg-emerald-500" },
  ];

  const strength = strengths[password ? score : 0];

  return (
    <div className="space-y-2">
      <div
        className="flex gap-1.5"
        aria-label={`Password strength: ${strength.label}`}
      >
        {rules.map((rule, index) => (
          <div
            key={rule.label}
            className={`h-1.5 flex-1 rounded-full ${
              index < score ? strength.color : "bg-white/10"
            }`}
          />
        ))}
      </div>

      <p className="text-xs text-slate-400">
        Password strength:{" "}
        <span className="font-medium text-slate-200">
          {strength.label}
        </span>
      </p>
    </div>
  );
}

function InputField({
  id,
  label,
  icon: Icon,
  type = "text",
  value,
  onChange,
  placeholder,
  autoComplete,
  maxLength,
  invalid,
  trailing,
}) {
  return (
    <div className="space-y-2">
      <label
        htmlFor={id}
        className="block text-sm font-medium text-slate-200"
      >
        {label}
      </label>

      <div
        className={`flex items-center gap-3 rounded-2xl border bg-white/[0.045] px-4 transition focus-within:ring-2 focus-within:ring-violet-500/15 ${
          invalid
            ? "border-red-400/60"
            : "border-indigo-300/15 focus-within:border-violet-400/70"
        }`}
      >
        <Icon size={19} className="shrink-0 text-slate-300" />

        <input
          id={id}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          autoComplete={autoComplete}
          maxLength={maxLength}
          aria-invalid={Boolean(invalid)}
          required
          className="min-w-0 w-full bg-transparent py-3.5 text-sm text-white outline-none placeholder:text-slate-500"
        />

        {trailing}
      </div>
    </div>
  );
}

export default function RegisterPage() {
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const rules = useMemo(() => getPasswordRules(password), [password]);

  const passwordsMatch =
    confirmPassword.length > 0 && password === confirmPassword;

  async function handleSubmit(event) {
    event.preventDefault();

    setSubmitted(true);
    setError("");

    const normalizedName = fullName.trim();
    const normalizedEmail = email.trim();
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!normalizedName) {
      setError("Please enter your full name.");
      return;
    }

    if (normalizedName.length > 100) {
      setError("Full name cannot exceed 100 characters.");
      return;
    }

    if (!emailPattern.test(normalizedEmail)) {
      setError("Please enter a valid email address.");
      return;
    }

    if (normalizedEmail.length > 255) {
      setError("Email cannot exceed 255 characters.");
      return;
    }

    if (password.length < 8 || password.length > 72) {
      setError("Password must be between 8 and 72 characters.");
      return;
    }

    if (rules.some((rule) => !rule.valid)) {
      setError("Please satisfy all password requirements.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);

    try {
      const response = await authService.register({
        fullName: normalizedName,
        email: normalizedEmail,
        password,
      });

      navigate("/register/verify-otp", {
        replace: true,
        state: {
          email: normalizedEmail,
          message: response.message,
        },
      });
    } catch (err) {
      const status = err.response?.status;

      if (status === 409) {
        setError(
          "An account with this email already exists. Try logging in or verifying your email."
        );
      } else if (status === 400 || status === 422) {
        setError(
          err.response?.data?.detail ||
            err.response?.data?.message ||
            "Please check your registration details."
        );
      } else if (status === 429) {
        setError("Too many requests. Please wait before trying again.");
      } else if (!err.response) {
        setError("Cannot connect to ASTRA server. Please try again.");
      } else {
        setError(
          err.response?.data?.detail ||
            err.response?.data?.message ||
            "Registration failed. Please try again."
        );
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <div className="mb-6 text-center">
        <h1 className="text-3xl font-extrabold tracking-tight sm:text-4xl">
          Create Your Account
        </h1>

        <p className="mt-2 text-sm text-slate-400">
          Join ASTRA and start your shopping journey
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        <InputField
          id="fullName"
          label="Full name"
          icon={UserRound}
          value={fullName}
          onChange={(event) => setFullName(event.target.value)}
          placeholder="Enter your full name"
          autoComplete="name"
          maxLength={100}
          invalid={submitted && !fullName.trim()}
        />

        <InputField
          id="registerEmail"
          label="Email address"
          icon={Mail}
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="you@example.com"
          autoComplete="email"
          maxLength={255}
          invalid={
            submitted &&
            !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())
          }
        />

        <div className="space-y-2">
          <InputField
            id="registerPassword"
            label="Password"
            icon={LockKeyhole}
            type={showPassword ? "text" : "password"}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Create a password"
            autoComplete="new-password"
            maxLength={72}
            invalid={submitted && rules.some((rule) => !rule.valid)}
            trailing={
              <button
                type="button"
                onClick={() => setShowPassword((value) => !value)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                className="shrink-0 text-slate-400 transition hover:text-white"
              >
                {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
              </button>
            }
          />

          <PasswordStrength password={password} />
          <PasswordRules password={password} />
        </div>

        <div className="space-y-2">
          <InputField
            id="confirmPassword"
            label="Confirm password"
            icon={LockKeyhole}
            type={showConfirmPassword ? "text" : "password"}
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            placeholder="Re-enter your password"
            autoComplete="new-password"
            maxLength={72}
            invalid={submitted && password !== confirmPassword}
            trailing={
              <button
                type="button"
                onClick={() =>
                  setShowConfirmPassword((value) => !value)
                }
                aria-label={
                  showConfirmPassword
                    ? "Hide confirm password"
                    : "Show confirm password"
                }
                className="shrink-0 text-slate-400 transition hover:text-white"
              >
                {showConfirmPassword ? (
                  <EyeOff size={19} />
                ) : (
                  <Eye size={19} />
                )}
              </button>
            }
          />

          {confirmPassword && (
            <p
              className={`text-xs ${
                passwordsMatch ? "text-emerald-400" : "text-red-400"
              }`}
            >
              {passwordsMatch
                ? "Passwords match"
                : "Passwords do not match"}
            </p>
          )}
        </div>

        {error && (
          <div
            role="alert"
            className="rounded-xl border border-red-400/20 bg-red-500/10 p-3 text-sm text-red-300"
          >
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="group flex w-full items-center justify-center gap-3 rounded-2xl bg-gradient-to-r from-violet-600 via-purple-600 to-fuchsia-600 px-5 py-4 text-sm font-bold text-white shadow-lg shadow-purple-900/30 transition hover:brightness-110 focus:outline-none focus:ring-2 focus:ring-violet-400 focus:ring-offset-2 focus:ring-offset-slate-900 disabled:cursor-not-allowed disabled:opacity-60"
        >
          <span>{loading ? "Creating account..." : "Create Account"}</span>
          {!loading && (
            <ArrowRight
              size={19}
              className="transition-transform group-hover:translate-x-1"
            />
          )}
        </button>
      </form>

      <div className="my-6 flex items-center gap-4">
        <div className="h-px flex-1 bg-white/10" />
        <span className="text-xs text-slate-400">OR</span>
        <div className="h-px flex-1 bg-white/10" />
      </div>

      <p className="text-center text-sm text-slate-300">
        Already have an account?{" "}
        <Link
          to="/login"
          className="inline-flex items-center gap-1 font-semibold text-violet-300 transition hover:text-white"
        >
          Sign in
          <ArrowRight size={15} />
        </Link>
      </p>
    </AuthLayout>
  );
}

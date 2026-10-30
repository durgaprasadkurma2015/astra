import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowLeft, Mail } from "lucide-react";

import AuthLayout from "../../layouts/AuthLayout";
import Button from "../../components/common/Button";
import authService from "../../services/authService";

export default function ForgotPasswordPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    setLoading(true);

    try {
      const result = await authService.forgotPassword(email.trim());

      navigate("/verify-otp", {
        state: {
          email: email.trim(),
          message: result.message,
        },
      });
    } catch (err) {
      setError(
        err.response?.data?.detail ||
        "We couldn't process your request. Please try again later."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <Link
        to="/login"
        className="mb-7 inline-flex items-center gap-2 text-sm text-slate-400 hover:text-white"
      >
        <ArrowLeft size={16} /> Back to login
      </Link>

      <div className="mb-7">
        <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-2xl bg-violet-500/15 text-violet-300">
          <Mail size={24} />
        </div>

        <h1 className="text-3xl font-bold">Forgot Password?</h1>

        <p className="mt-3 text-sm leading-6 text-slate-400">
          Enter your registered email address. If your account is eligible,
          we'll send you a verification code.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="space-y-2">
          <label
            htmlFor="resetEmail"
            className="block text-sm font-medium text-slate-200"
          >
            Email address
          </label>

          <input
            id="resetEmail"
            type="email"
            required
            maxLength={255}
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="you@example.com"
            className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3.5 text-sm text-white outline-none placeholder:text-slate-500 focus:border-violet-400 focus:ring-2 focus:ring-violet-500/20"
          />
        </div>

        {error && (
          <p role="alert" className="text-sm text-red-300">
            {error}
          </p>
        )}

        {message && (
          <p role="status" className="text-sm text-emerald-300">
            {message}
          </p>
        )}

        <Button type="submit" disabled={loading} className="w-full">
          {loading ? "Sending request..." : "Continue"}
        </Button>
      </form>
    </AuthLayout>
  );
}
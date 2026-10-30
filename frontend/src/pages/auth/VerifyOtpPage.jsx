import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { ArrowLeft, ShieldCheck } from "lucide-react";

import AuthLayout from "../../layouts/AuthLayout";
import Button from "../../components/common/Button";
import authService from "../../services/authService";

export default function VerifyOtpPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const email = location.state?.email || "";

  const [otp, setOtp] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!email) {
      setError("Start from Forgot Password to request a new code.");
      return;
    }

    if (!/^\d{6}$/.test(otp)) {
      setError("Enter the six-digit verification code.");
      return;
    }

    setLoading(true);

    try {
      const result = await authService.verifyOtp(email, otp);

      // Keep the short-lived reset token only for this tab.
      sessionStorage.setItem(
        "astra_password_reset_token",
        result.resetToken
      );

      navigate("/reset-password", {
        replace: true,
      });
    } catch (err) {
      setError(
        err.response?.data?.detail ||
        "The code is invalid or expired. Request a new code and try again."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <Link
        to="/forgot-password"
        className="mb-7 inline-flex items-center gap-2 text-sm text-slate-400 hover:text-white"
      >
        <ArrowLeft size={16} /> Back
      </Link>

      <div className="mb-7">
        <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-2xl bg-violet-500/15 text-violet-300">
          <ShieldCheck size={24} />
        </div>

        <h1 className="text-3xl font-bold">Verify Your Email</h1>

        <p className="mt-3 text-sm leading-6 text-slate-400">
          Enter the six-digit code sent to{" "}
          <span className="break-all text-slate-200">
            {email || "your registered email"}
          </span>.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="space-y-2">
          <label
            htmlFor="otp"
            className="block text-sm font-medium text-slate-200"
          >
            Verification code
          </label>

          <input
            id="otp"
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={6}
            pattern="[0-9]{6}"
            required
            value={otp}
            onChange={(event) =>
              setOtp(event.target.value.replace(/\D/g, "").slice(0, 6))
            }
            placeholder="000000"
            className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-4 text-center text-2xl tracking-[0.5em] text-white outline-none placeholder:text-slate-600 focus:border-violet-400 focus:ring-2 focus:ring-violet-500/20"
          />
        </div>

        {error && (
          <p role="alert" className="text-sm text-red-300">
            {error}
          </p>
        )}

        <Button type="submit" disabled={loading} className="w-full">
          {loading ? "Verifying..." : "Verify Code"}
        </Button>
      </form>
    </AuthLayout>
  );
}
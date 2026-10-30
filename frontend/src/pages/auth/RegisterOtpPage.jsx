
import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { MailCheck } from "lucide-react";
import authService from "../../services/authService";
import AuthLayout from "../../layouts/AuthLayout";
import Button from "../../components/common/Button";

export default function RegisterOtpPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const email = location.state?.email || "";

  const [otp, setOtp] = useState("");
  const [message, setMessage] = useState(
    location.state?.message || ""
  );
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);

  async function handleVerify(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!email) {
      setError("Registration email is missing. Please register again.");
      return;
    }

    if (!/^\d{6}$/.test(otp)) {
      setError("Enter the 6-digit OTP sent to your email.");
      return;
    }

    try {
      setLoading(true);
      const response = await authService.verifyRegistrationOtp(
        email,
        otp
      );

      navigate("/login", {
        replace: true,
        state: {
          email,
          emailVerified: true,
          message: response.message,
        },
      });
    } catch (err) {
      setError(
        err.response?.data?.detail ||
        err.response?.data?.message ||
        "OTP verification failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleResend() {
    setError("");
    setMessage("");

    try {
      setResending(true);
      const response =
        await authService.resendRegistrationOtp(email);
      setMessage(response.message);
    } catch (err) {
      setError(
        err.response?.data?.detail ||
        err.response?.data?.message ||
        "Unable to resend OTP. Please try again later."
      );
    } finally {
      setResending(false);
    }
  }

  return (
    <AuthLayout>
      <div className="mx-auto w-full max-w-md rounded-2xl border border-white/10 bg-white/5 p-6 text-white shadow-xl sm:p-8">
        <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-violet-500/20 text-violet-300">
          <MailCheck size={28} />
        </div>

        <h1 className="text-2xl font-bold">Verify your email</h1>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Enter the 6-digit code sent to{" "}
          <span className="font-semibold text-white">{email || "your email"}</span>.
          The code expires in 5 minutes.
        </p>

        {message && (
          <p role="status" className="mt-4 rounded-lg bg-emerald-500/10 p-3 text-sm text-emerald-300">
            {message}
          </p>
        )}

        {error && (
          <p role="alert" className="mt-4 rounded-lg bg-red-500/10 p-3 text-sm text-red-300">
            {error}
          </p>
        )}

        <form onSubmit={handleVerify} className="mt-6 space-y-4">
          <label htmlFor="registration-otp" className="block text-sm font-medium text-slate-200">
            Email verification code
          </label>

          <input
            id="registration-otp"
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={6}
            value={otp}
            onChange={(event) =>
              setOtp(event.target.value.replace(/\D/g, "").slice(0, 6))
            }
            placeholder="000000"
            required
            className="w-full rounded-xl border border-white/15 bg-slate-950/60 px-4 py-3 text-center text-xl tracking-[0.5em] text-white outline-none focus:border-violet-400 focus:ring-2 focus:ring-violet-400/20"
          />

          <Button type="submit" disabled={loading || !email}>
            {loading ? "Verifying..." : "Verify email"}
          </Button>
        </form>

        <button
          type="button"
          onClick={handleResend}
          disabled={resending || !email}
          className="mt-4 w-full rounded-xl px-4 py-3 text-sm font-semibold text-violet-300 hover:bg-white/5 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {resending ? "Sending..." : "Resend OTP"}
        </button>

        <p className="mt-4 text-center text-sm text-slate-400">
          Wrong email?{" "}
          <Link to="/register" className="font-semibold text-violet-300 hover:text-violet-200">
            Register again
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}

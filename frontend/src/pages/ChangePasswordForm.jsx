
import { useState } from "react";
import authService from "../services/authService";

export default function ChangePasswordForm() {
  const [form, setForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setError("");
    setSuccess("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (form.newPassword.length < 8) {
      setError("New password must contain at least 8 characters.");
      return;
    }

    if (form.newPassword.length > 72) {
      setError("New password cannot exceed 72 characters.");
      return;
    }

    if (form.newPassword !== form.confirmPassword) {
      setError("New password and confirmation do not match.");
      return;
    }

    if (form.currentPassword === form.newPassword) {
      setError("Choose a password different from your current password.");
      return;
    }

    try {
      setLoading(true);

      const response = await authService.changePassword(
        form.currentPassword,
        form.newPassword
      );

      setSuccess(response.message || "Password changed successfully.");

      setForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch (err) {
      setError(
        err.response?.data?.detail ||
        err.response?.data?.message ||
        (typeof err.response?.data === "string"
          ? err.response.data
          : "") ||
        "Unable to change password. Please check your current password and try again."
      );
    } finally {
      setLoading(false);
    }
  };

  const inputClass =
    "mt-2 w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-white outline-none transition placeholder:text-gray-500 focus:border-violet-400 focus:ring-2 focus:ring-violet-500/20";

  return (
    <section className="rounded-2xl border border-white/10 bg-[#171326] p-5 text-white shadow-xl sm:p-7">
      <div className="mb-6">
        <p className="text-sm font-medium text-violet-300">
          SECURITY SETTINGS
        </p>
        <h2 className="mt-2 text-2xl font-bold">
          Change password
        </h2>
        <p className="mt-2 text-sm text-gray-400">
          Enter your current password and choose a new, secure password.
        </p>
      </div>

      {error && (
        <div
          role="alert"
          className="mb-4 rounded-xl border border-red-500/30 bg-red-500/10 p-3 text-sm text-red-300"
        >
          {error}
        </div>
      )}

      {success && (
        <div
          role="status"
          className="mb-4 rounded-xl border border-emerald-500/30 bg-emerald-500/10 p-3 text-sm text-emerald-300"
        >
          {success}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label
            htmlFor="currentPassword"
            className="text-sm font-medium text-gray-200"
          >
            Current password
          </label>
          <input
            id="currentPassword"
            name="currentPassword"
            type="password"
            autoComplete="current-password"
            value={form.currentPassword}
            onChange={handleChange}
            className={inputClass}
            placeholder="Enter current password"
            required
          />
        </div>

        <div>
          <label
            htmlFor="newPassword"
            className="text-sm font-medium text-gray-200"
          >
            New password
          </label>
          <input
            id="newPassword"
            name="newPassword"
            type="password"
            autoComplete="new-password"
            minLength={8}
            maxLength={72}
            value={form.newPassword}
            onChange={handleChange}
            className={inputClass}
            placeholder="At least 8 characters"
            required
          />
        </div>

        <div>
          <label
            htmlFor="confirmPassword"
            className="text-sm font-medium text-gray-200"
          >
            Confirm new password
          </label>
          <input
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            autoComplete="new-password"
            minLength={8}
            maxLength={72}
            value={form.confirmPassword}
            onChange={handleChange}
            className={inputClass}
            placeholder="Re-enter new password"
            required
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-xl bg-violet-600 px-5 py-3 font-semibold text-white transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto"
        >
          {loading ? "Updating password..." : "Update password"}
        </button>
      </form>
    </section>
  );
}


import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  UserPlus,
  User,
  Mail,
  Lock,
  ShieldCheck,
  ArrowLeft,
} from "lucide-react";
import adminService from "../services/adminService";

export default function AdminRegisterPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
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
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    if (form.password.length < 8) {
      setError("Password must contain at least 8 characters.");
      return;
    }

    setLoading(true);

    try {
      await adminService.registerAdmin({
        name: form.name.trim(),
        email: form.email.trim(),
        password: form.password,
      });

      setSuccess("Admin account created successfully.");

      setForm({
        name: "",
        email: "",
        password: "",
        confirmPassword: "",
      });

      setTimeout(() => {
        navigate("/admin");
      }, 1000);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Admin registration failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto w-full max-w-4xl p-4 sm:p-6 lg:p-8">
      <div className="mb-6 flex items-center gap-3">
        <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-indigo-100 text-indigo-600 dark:bg-indigo-500/15 dark:text-indigo-400">
          <UserPlus size={24} />
        </div>

        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
            Register Admin
          </h1>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Create an administrator account for ASTRA.
          </p>
        </div>
      </div>

      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="border-b border-slate-200 px-6 py-5 dark:border-slate-800">
          <div className="flex items-center gap-2">
            <ShieldCheck
              size={20}
              className="text-indigo-600 dark:text-indigo-400"
            />
            <h2 className="font-semibold text-slate-900 dark:text-white">
              Administrator Details
            </h2>
          </div>
          <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
            Enter the details below to create an admin account.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 p-6">
          {error && (
            <div
              role="alert"
              className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/40 dark:text-red-300"
            >
              {error}
            </div>
          )}

          {success && (
            <div
              role="status"
              className="rounded-lg border border-green-200 bg-green-50 p-3 text-sm text-green-700 dark:border-green-900 dark:bg-green-950/40 dark:text-green-300"
            >
              {success}
            </div>
          )}

          <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
            <div>
              <label
                htmlFor="name"
                className="mb-2 block text-sm font-medium text-slate-700 dark:text-slate-300"
              >
                Full Name
              </label>

              <div className="relative">
                <User
                  size={18}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />
                <input
                  id="name"
                  name="name"
                  type="text"
                  value={form.name}
                  onChange={handleChange}
                  placeholder="Enter full name"
                  autoComplete="name"
                  required
                  maxLength={100}
                  className="w-full rounded-lg border border-slate-300 bg-white py-3 pl-10 pr-4 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="email"
                className="mb-2 block text-sm font-medium text-slate-700 dark:text-slate-300"
              >
                Email Address
              </label>

              <div className="relative">
                <Mail
                  size={18}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />
                <input
                  id="email"
                  name="email"
                  type="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="admin@example.com"
                  autoComplete="email"
                  required
                  className="w-full rounded-lg border border-slate-300 bg-white py-3 pl-10 pr-4 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="password"
                className="mb-2 block text-sm font-medium text-slate-700 dark:text-slate-300"
              >
                Password
              </label>

              <div className="relative">
                <Lock
                  size={18}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />
                <input
                  id="password"
                  name="password"
                  type="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="Minimum 8 characters"
                  autoComplete="new-password"
                  minLength={8}
                  required
                  className="w-full rounded-lg border border-slate-300 bg-white py-3 pl-10 pr-4 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="confirmPassword"
                className="mb-2 block text-sm font-medium text-slate-700 dark:text-slate-300"
              >
                Confirm Password
              </label>

              <div className="relative">
                <Lock
                  size={18}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type="password"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="Re-enter password"
                  autoComplete="new-password"
                  required
                  className="w-full rounded-lg border border-slate-300 bg-white py-3 pl-10 pr-4 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
                />
              </div>
            </div>
          </div>

          <div className="flex flex-col-reverse gap-3 border-t border-slate-200 pt-5 sm:flex-row sm:justify-between dark:border-slate-800">
            <button
              type="button"
              onClick={() => navigate("/admin")}
              className="inline-flex items-center justify-center gap-2 rounded-lg border border-slate-300 px-4 py-3 text-sm font-medium text-slate-700 transition hover:bg-slate-100 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
            >
              <ArrowLeft size={17} />
              Back to Dashboard
            </button>

            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <UserPlus size={17} />
              {loading ? "Creating Account..." : "Register Admin"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

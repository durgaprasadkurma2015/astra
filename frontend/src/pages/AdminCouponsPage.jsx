
import { useCallback, useEffect, useState } from "react";
import {
  TicketPercent,
  PlusCircle,
  RefreshCw,
  CheckCircle,
  XCircle,
  AlertCircle,
  CalendarDays,
  Tag,
} from "lucide-react";
import couponService from "../services/couponService";

function getLocalDateTimeValue(date) {
  const pad = (value) => String(value).padStart(2, "0");

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate()
  )}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function getDefaultForm() {
  const now = new Date();
  now.setSeconds(0, 0);

  const expiry = new Date(now);
  expiry.setDate(expiry.getDate() + 7);

  return {
    code: "",
    description: "",
    discountType: "PERCENTAGE",
    discountValue: "10",
    minimumOrderAmount: "0",
    maximumDiscountAmount: "200",
    usageLimit: "",
    perCustomerUsageLimit: "",
    startsAt: getLocalDateTimeValue(now),
    expiresAt: getLocalDateTimeValue(expiry),
  };
}

function getErrorMessage(error) {
  return (
    error?.response?.data?.detail ||
    error?.response?.data?.message ||
    error?.response?.data?.title ||
    error?.message ||
    "Something went wrong. Please try again."
  );
}

function formatPrice(amount) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(Number(amount ?? 0));
}

function formatDate(value) {
  if (!value) return "Not available";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Not available";
  }

  return date.toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function AdminCouponsPage() {
  const [form, setForm] = useState(getDefaultForm);
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [updatingId, setUpdatingId] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const loadCoupons = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const data = await couponService.getAllCoupons();
      setCoupons(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function fetchCoupons() {
      setLoading(true);

      try {
        const data = await couponService.getAllCoupons();

        if (!cancelled) {
          setCoupons(Array.isArray(data) ? data : []);
          setError("");
        }
      } catch (err) {
        if (!cancelled) {
          setError(getErrorMessage(err));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void fetchCoupons();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleCreateCoupon = async (event) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    const code = form.code.trim().toUpperCase();

    if (!/^[A-Z0-9_-]+$/.test(code)) {
      setError(
        "Coupon code can contain letters, numbers, hyphens and underscores only."
      );
      return;
    }

    const discountValue = Number(form.discountValue);
    const minimumOrderAmount = Number(form.minimumOrderAmount);

    const maximumDiscountAmount =
      form.maximumDiscountAmount.trim() === ""
        ? null
        : Number(form.maximumDiscountAmount);
    const usageLimit = form.usageLimit.trim() === "" ? null : Number(form.usageLimit);
    const perCustomerUsageLimit = form.perCustomerUsageLimit.trim() === ""
      ? null
      : Number(form.perCustomerUsageLimit);

    if (!Number.isFinite(discountValue) || discountValue <= 0) {
      setError("Discount value must be greater than zero.");
      return;
    }

    if (
      form.discountType === "PERCENTAGE" &&
      discountValue > 100
    ) {
      setError("Percentage discount cannot exceed 100.");
      return;
    }

    if (
      !Number.isFinite(minimumOrderAmount) ||
      minimumOrderAmount < 0
    ) {
      setError("Minimum order amount cannot be negative.");
      return;
    }

    if (
      maximumDiscountAmount !== null &&
      (!Number.isFinite(maximumDiscountAmount) ||
        maximumDiscountAmount <= 0)
    ) {
      setError(
        "Maximum discount must be greater than zero, or leave it empty."
      );
      return;
    }

    if ([usageLimit, perCustomerUsageLimit].some((value) =>
      value !== null && (!Number.isSafeInteger(value) || value < 1)
    )) {
      setError("Usage limits must be positive whole numbers or left empty.");
      return;
    }

    if (
      !form.startsAt ||
      !form.expiresAt ||
      new Date(form.expiresAt) <= new Date(form.startsAt)
    ) {
      setError("Expiry date must be later than the start date.");
      return;
    }

    const payload = {
      code,
      description: form.description.trim() || null,
      discountType: form.discountType,
      discountValue,
      minimumOrderAmount,
      maximumDiscountAmount,
      usageLimit,
      perCustomerUsageLimit,
      startsAt: form.startsAt,
      expiresAt: form.expiresAt,
    };

    setSaving(true);

    try {
      const createdCoupon = await couponService.createCoupon(payload);

      setCoupons((previous) => [
        createdCoupon,
        ...previous.filter((coupon) => coupon.id !== createdCoupon.id),
      ]);

      setForm(getDefaultForm());
      setSuccess(`Coupon ${createdCoupon.code} created successfully.`);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleToggleActive = async (coupon) => {
    setError("");
    setSuccess("");
    setUpdatingId(coupon.id);

    try {
      const updatedCoupon = await couponService.updateActiveStatus(
        coupon.id,
        !coupon.active
      );

      setCoupons((previous) =>
        previous.map((item) =>
          item.id === updatedCoupon.id ? updatedCoupon : item
        )
      );

      setSuccess(
        `${updatedCoupon.code} is now ${
          updatedCoupon.active ? "active" : "inactive"
        }.`
      );
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div className="min-h-screen bg-[#09090f] px-4 py-6 text-white sm:px-8 lg:px-10">
      <div className="mb-8 flex items-center gap-4">
        <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-violet-400/20 bg-violet-500/10">
          <TicketPercent size={28} className="text-violet-300" />
        </div>

        <div>
          <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
            Coupon Management
          </h1>
          <p className="mt-2 text-sm text-gray-400">
            Create discount coupons and manage their availability.
          </p>
        </div>
      </div>

      {error && (
        <div
          role="alert"
          className="mb-5 flex items-start gap-3 rounded-xl border border-red-400/20 bg-red-500/10 p-4 text-sm text-red-300"
        >
          <AlertCircle size={19} className="mt-0.5 shrink-0" />
          <p>{error}</p>
          <button
            type="button"
            onClick={() => setError("")}
            className="ml-auto"
            aria-label="Dismiss error"
          >
            <XCircle size={18} />
          </button>
        </div>
      )}

      {success && (
        <div
          role="status"
          className="mb-5 flex items-start gap-3 rounded-xl border border-emerald-400/20 bg-emerald-500/10 p-4 text-sm text-emerald-300"
        >
          <CheckCircle size={19} className="mt-0.5 shrink-0" />
          <p>{success}</p>
          <button
            type="button"
            onClick={() => setSuccess("")}
            className="ml-auto"
            aria-label="Dismiss success message"
          >
            <XCircle size={18} />
          </button>
        </div>
      )}

      <section className="mb-8 rounded-2xl border border-white/10 bg-white/[0.03] p-5 sm:p-8">
        <div className="mb-6 flex items-center gap-3">
          <PlusCircle size={23} className="text-violet-300" />
          <div>
            <h2 className="text-xl font-bold">Create a coupon</h2>
            <p className="mt-1 text-sm text-gray-400">
              Newly created coupons are active by default.
            </p>
          </div>
        </div>

        <form onSubmit={handleCreateCoupon}>
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            <div>
              <label
                htmlFor="code"
                className="mb-2 block text-sm font-medium text-gray-300"
              >
                Coupon code *
              </label>
              <input
                id="code"
                name="code"
                value={form.code}
                onChange={handleChange}
                placeholder="e.g. SAVE10"
                maxLength={50}
                pattern="[A-Za-z0-9_-]+"
                required
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-4 py-3 text-white outline-none transition placeholder:text-gray-600 focus:border-violet-400"
              />
            </div>

            <div>
              <label
                htmlFor="discountType"
                className="mb-2 block text-sm font-medium text-gray-300"
              >
                Discount type *
              </label>
              <select
                id="discountType"
                name="discountType"
                value={form.discountType}
                onChange={handleChange}
                required
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-4 py-3 text-white outline-none focus:border-violet-400"
              >
                <option value="PERCENTAGE">Percentage (%)</option>
                <option value="FIXED">Fixed amount (₹)</option>
              </select>
            </div>

            <div>
              <label
                htmlFor="discountValue"
                className="mb-2 block text-sm font-medium text-gray-300"
              >
                Discount value *
              </label>
              <input
                id="discountValue"
                name="discountValue"
                type="number"
                min="0.01"
                max={form.discountType === "PERCENTAGE" ? "100" : undefined}
                step="0.01"
                value={form.discountValue}
                onChange={handleChange}
                required
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-4 py-3 text-white outline-none focus:border-violet-400"
              />
            </div>

            <div>
              <label
                htmlFor="minimumOrderAmount"
                className="mb-2 block text-sm font-medium text-gray-300"
              >
                Minimum order amount (₹) *
              </label>
              <input
                id="minimumOrderAmount"
                name="minimumOrderAmount"
                type="number"
                min="0"
                step="0.01"
                value={form.minimumOrderAmount}
                onChange={handleChange}
                required
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-4 py-3 text-white outline-none focus:border-violet-400"
              />
            </div>

            <div>
              <label
                htmlFor="maximumDiscountAmount"
                className="mb-2 block text-sm font-medium text-gray-300"
              >
                Maximum discount (₹)
              </label>
              <input
                id="maximumDiscountAmount"
                name="maximumDiscountAmount"
                type="number"
                min="0.01"
                step="0.01"
                value={form.maximumDiscountAmount}
                onChange={handleChange}
                placeholder="Leave empty for no cap"
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-4 py-3 text-white outline-none focus:border-violet-400"
              />
              <p className="mt-1 text-xs text-gray-500">
                Optional. Applies to percentage and fixed discounts.
              </p>
            </div>

            <div>
              <label
                htmlFor="description"
                className="mb-2 block text-sm font-medium text-gray-300"
              >
                Description
              </label>
              <input
                id="description"
                name="description"
                value={form.description}
                onChange={handleChange}
                maxLength={255}
                placeholder="e.g. Festival discount"
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-4 py-3 text-white outline-none placeholder:text-gray-600 focus:border-violet-400"
              />
            </div>

            <div>
              <label htmlFor="usageLimit" className="mb-2 block text-sm font-medium text-gray-300">
                Total redemptions
              </label>
              <input
                id="usageLimit"
                name="usageLimit"
                type="number"
                min="1"
                step="1"
                value={form.usageLimit}
                onChange={handleChange}
                placeholder="Unlimited"
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-4 py-3 text-white outline-none focus:border-violet-400"
              />
            </div>

            <div>
              <label htmlFor="perCustomerUsageLimit" className="mb-2 block text-sm font-medium text-gray-300">
                Uses per customer
              </label>
              <input
                id="perCustomerUsageLimit"
                name="perCustomerUsageLimit"
                type="number"
                min="1"
                step="1"
                value={form.perCustomerUsageLimit}
                onChange={handleChange}
                placeholder="Unlimited"
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-4 py-3 text-white outline-none focus:border-violet-400"
              />
            </div>

            <div>
              <label
                htmlFor="startsAt"
                className="mb-2 flex items-center gap-2 text-sm font-medium text-gray-300"
              >
                <CalendarDays size={16} />
                Starts at *
              </label>
              <input
                id="startsAt"
                name="startsAt"
                type="datetime-local"
                value={form.startsAt}
                onChange={handleChange}
                required
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-3 py-3 text-white outline-none focus:border-violet-400"
              />
            </div>

            <div>
              <label
                htmlFor="expiresAt"
                className="mb-2 flex items-center gap-2 text-sm font-medium text-gray-300"
              >
                <CalendarDays size={16} />
                Expires at *
              </label>
              <input
                id="expiresAt"
                name="expiresAt"
                type="datetime-local"
                min={form.startsAt}
                value={form.expiresAt}
                onChange={handleChange}
                required
                className="w-full rounded-xl border border-white/10 bg-[#11101a] px-3 py-3 text-white outline-none focus:border-violet-400"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={saving}
            className="mt-7 inline-flex items-center justify-center gap-2 rounded-xl bg-violet-600 px-6 py-3 font-semibold text-white transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {saving ? (
              <RefreshCw size={18} className="animate-spin" />
            ) : (
              <PlusCircle size={18} />
            )}
            {saving ? "Creating coupon..." : "Create coupon"}
          </button>
        </form>
      </section>

      <section className="rounded-2xl border border-white/10 bg-white/[0.03] p-5 sm:p-8">
        <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <Tag size={23} className="text-violet-300" />
            <div>
              <h2 className="text-xl font-bold">Existing coupons</h2>
              <p className="mt-1 text-sm text-gray-400">
                {coupons.length} {coupons.length === 1 ? "coupon" : "coupons"}
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={loadCoupons}
            disabled={loading}
            className="inline-flex items-center gap-2 rounded-xl border border-white/10 px-4 py-2.5 text-sm font-semibold transition hover:bg-white/5 disabled:opacity-50"
          >
            <RefreshCw
              size={16}
              className={loading ? "animate-spin" : ""}
            />
            Refresh
          </button>
        </div>

        {loading ? (
          <div className="flex items-center justify-center gap-3 py-12 text-gray-400">
            <RefreshCw size={22} className="animate-spin" />
            Loading coupons...
          </div>
        ) : coupons.length === 0 ? (
          <div className="rounded-xl border border-dashed border-white/10 py-12 text-center">
            <TicketPercent
              size={38}
              className="mx-auto mb-3 text-gray-500"
            />
            <p className="font-semibold">No coupons created yet</p>
            <p className="mt-2 text-sm text-gray-400">
              Use the form above to create your first coupon.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[900px] text-left text-sm">
              <thead>
                <tr className="border-b border-white/10 text-xs uppercase tracking-wider text-gray-500">
                  <th className="px-4 py-4">Coupon</th>
                  <th className="px-4 py-4">Discount</th>
                  <th className="px-4 py-4">Minimum order</th>
                  <th className="px-4 py-4">Validity</th>
                  <th className="px-4 py-4">Status</th>
                  <th className="px-4 py-4">Action</th>
                </tr>
              </thead>

              <tbody>
                {coupons.map((coupon) => (
                  <tr
                    key={coupon.id}
                    className="border-b border-white/5 last:border-0"
                  >
                    <td className="px-4 py-5">
                      <p className="font-bold text-violet-300">
                        {coupon.code}
                      </p>
                      <p className="mt-1 max-w-56 text-xs text-gray-500">
                        {coupon.description || "No description"}
                      </p>
                      <p className="mt-1 text-xs text-gray-500">
                        Redeemed {coupon.usageCount || 0}{coupon.usageLimit ? ` / ${coupon.usageLimit}` : ""}
                        {coupon.perCustomerUsageLimit ? ` · ${coupon.perCustomerUsageLimit} per customer` : ""}
                      </p>
                    </td>

                    <td className="px-4 py-5">
                      <p className="font-semibold">
                        {coupon.discountType === "PERCENTAGE"
                          ? `${coupon.discountValue}%`
                          : formatPrice(coupon.discountValue)}
                      </p>
                      {coupon.maximumDiscountAmount != null && (
                        <p className="mt-1 text-xs text-gray-500">
                          Max: {formatPrice(coupon.maximumDiscountAmount)}
                        </p>
                      )}
                    </td>

                    <td className="px-4 py-5">
                      {formatPrice(coupon.minimumOrderAmount)}
                    </td>

                    <td className="px-4 py-5 text-xs text-gray-400">
                      <p>From: {formatDate(coupon.startsAt)}</p>
                      <p className="mt-2">
                        Until: {formatDate(coupon.expiresAt)}
                      </p>
                    </td>

                    <td className="px-4 py-5">
                      <span
                        className={`inline-flex rounded-full border px-3 py-1 text-xs font-semibold ${
                          coupon.active
                            ? "border-emerald-400/20 bg-emerald-500/10 text-emerald-300"
                            : "border-red-400/20 bg-red-500/10 text-red-300"
                        }`}
                      >
                        {coupon.active ? "Active" : "Inactive"}
                      </span>
                    </td>

                    <td className="px-4 py-5">
                      <button
                        type="button"
                        onClick={() => handleToggleActive(coupon)}
                        disabled={updatingId === coupon.id}
                        className={`inline-flex items-center gap-2 rounded-lg border px-3 py-2 text-xs font-semibold transition disabled:opacity-50 ${
                          coupon.active
                            ? "border-red-400/20 text-red-300 hover:bg-red-500/10"
                            : "border-emerald-400/20 text-emerald-300 hover:bg-emerald-500/10"
                        }`}
                      >
                        {updatingId === coupon.id ? (
                          <RefreshCw size={14} className="animate-spin" />
                        ) : coupon.active ? (
                          <XCircle size={14} />
                        ) : (
                          <CheckCircle size={14} />
                        )}

                        {coupon.active ? "Deactivate" : "Activate"}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

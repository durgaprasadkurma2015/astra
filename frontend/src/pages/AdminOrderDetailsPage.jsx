
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  Package,
  UserRound,
  MapPin,
  CreditCard,
  CalendarDays,
  RefreshCw,
  ReceiptText,
} from "lucide-react";

import orderService from "../services/orderService";

const statusStyles = {
  PLACED: "border-amber-400/20 bg-amber-400/10 text-amber-300",
  CONFIRMED: "border-blue-400/20 bg-blue-400/10 text-blue-300",
  SHIPPED: "border-violet-400/20 bg-violet-400/10 text-violet-300",
  OUT_FOR_DELIVERY: "border-cyan-400/20 bg-cyan-400/10 text-cyan-300",
  DELIVERED: "border-emerald-400/20 bg-emerald-400/10 text-emerald-300",
  CANCELLED: "border-red-400/20 bg-red-400/10 text-red-300",
};

function formatCurrency(amount) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    minimumFractionDigits: 2,
  }).format(Number(amount || 0));
}

function formatDate(value) {
  if (!value) return "Not available";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Not available";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function displayStatus(value) {
  return String(value || "UNKNOWN").replaceAll("_", " ");
}

function StatusBadge({ status }) {
  const classes =
    statusStyles[status] ||
    "border-white/10 bg-white/5 text-gray-300";

  return (
    <span
      className={`inline-flex rounded-full border px-3 py-1.5 text-xs font-semibold ${classes}`}
    >
      {displayStatus(status)}
    </span>
  );
}

function InfoCard({ title, icon: Icon, children }) {
  return (
    <section className="rounded-2xl border border-white/10 bg-[#11111b] p-5 sm:p-6">
      <div className="mb-5 flex items-center gap-3">
        <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-violet-500/10 text-violet-300">
          <Icon size={19} />
        </span>
        <h2 className="text-lg font-bold text-white">{title}</h2>
      </div>
      {children}
    </section>
  );
}

function DetailRow({ label, value, emphasis = false }) {
  return (
    <div className="flex items-start justify-between gap-4 py-2.5">
      <span className="text-sm text-gray-400">{label}</span>
      <span
        className={`max-w-[65%] break-words text-right text-sm ${
          emphasis
            ? "font-semibold text-white"
            : "text-gray-200"
        }`}
      >
        {value || "Not available"}
      </span>
    </div>
  );
}

export default function AdminOrderDetailsPage() {
  const { orderNumber } = useParams();
  const navigate = useNavigate();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  async function loadOrder(isRefresh = false) {
    if (!orderNumber) {
      setError("Order number is missing.");
      setLoading(false);
      return;
    }

    if (isRefresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }

    setError("");

    try {
      const data = await orderService.getAdminOrderDetails(orderNumber);
      setOrder(data);
    } catch (err) {
      setError(
        err.response?.status === 401
          ? "Your session has expired. Please log in again."
          : err.response?.status === 403
            ? "Access denied. An administrator account is required."
            : err.response?.status === 404
              ? "Order not found."
              : err.response?.data?.message ||
                "Unable to load order details. Please try again."
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }

  useEffect(() => {
    let cancelled = false;

    async function fetchOrder() {
      if (!orderNumber) {
        setError("Order number is missing.");
        setLoading(false);
        return;
      }

      setLoading(true);
      setError("");

      try {
        const data = await orderService.getAdminOrderDetails(orderNumber);

        if (!cancelled) {
          setOrder(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.status === 401
              ? "Your session has expired. Please log in again."
              : err.response?.status === 403
                ? "Access denied. An administrator account is required."
                : err.response?.status === 404
                  ? "Order not found."
                  : err.response?.data?.message ||
                    "Unable to load order details. Please try again."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void fetchOrder();

    return () => {
      cancelled = true;
    };
  }, [orderNumber]);

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#09090f] text-white">
        <div className="text-center">
          <div className="mx-auto h-9 w-9 animate-spin rounded-full border-2 border-violet-500/30 border-t-violet-400" />
          <p className="mt-4 text-sm text-gray-400">
            Loading order details...
          </p>
        </div>
      </main>
    );
  }

  if (error || !order) {
    return (
      <main className="min-h-screen bg-[#09090f] px-4 py-10 text-white sm:px-8">
        <div className="mx-auto max-w-3xl rounded-2xl border border-white/10 bg-[#11111b] p-6">
          <p role="alert" className="text-sm text-red-300">
            {error || "Order details are unavailable."}
          </p>

          {error.includes("session has expired") && (
            <button
              type="button"
              onClick={() => navigate("/login")}
              className="mt-4 rounded-xl bg-violet-600 px-4 py-2 text-sm font-semibold"
            >
              Log in
            </button>
          )}

          <button
            type="button"
            onClick={() => navigate("/admin/orders")}
            className="mt-5 flex items-center gap-2 text-sm text-violet-300 hover:text-violet-200"
          >
            <ArrowLeft size={16} />
            Back to Orders
          </button>

          <button
            type="button"
            onClick={() => void loadOrder(true)}
            disabled={refreshing}
            className="ml-5 inline-flex items-center gap-2 text-sm text-gray-300 hover:text-white disabled:opacity-50"
          >
            <RefreshCw size={15} />
            Retry
          </button>
        </div>
      </main>
    );
  }

  const items = Array.isArray(order.items) ? order.items : [];

  return (
    <div className="min-h-screen bg-[#09090f] text-white">
      <main className="mx-auto max-w-7xl px-4 py-6 sm:px-7 sm:py-9">
        <div className="flex flex-col justify-between gap-5 border-b border-white/10 pb-7 sm:flex-row sm:items-center">
          <div>
            <button
              type="button"
              onClick={() => navigate("/admin/orders")}
              className="mb-5 inline-flex items-center gap-2 text-sm text-gray-400 transition hover:text-violet-300"
            >
              <ArrowLeft size={16} />
              Back to Orders
            </button>

            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-violet-400">
              ASTRA Administration
            </p>

            <h1 className="mt-2 break-all text-2xl font-bold sm:text-3xl">
              Order Details
            </h1>

            <p className="mt-2 text-sm text-gray-400">
              Order #{order.orderNumber}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <StatusBadge status={order.orderStatus} />

            <button
              type="button"
              onClick={() => void loadOrder(true)}
              disabled={refreshing}
              className="inline-flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.04] px-4 py-3 text-sm font-semibold transition hover:bg-white/[0.08] disabled:opacity-50"
            >
              <RefreshCw
                size={16}
                className={refreshing ? "animate-spin" : ""}
              />
              Refresh
            </button>
          </div>
        </div>

        <section className="mt-7 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <div className="rounded-2xl border border-white/10 bg-[#11111b] p-5">
            <p className="text-sm text-gray-400">Order Total</p>
            <p className="mt-3 text-2xl font-bold text-white">
              {formatCurrency(order.totalAmount)}
            </p>
          </div>

          <div className="rounded-2xl border border-white/10 bg-[#11111b] p-5">
            <p className="text-sm text-gray-400">Items Purchased</p>
            <p className="mt-3 text-2xl font-bold text-white">
              {items.reduce(
                (total, item) => total + Number(item.quantity || 0),
                0
              )}
            </p>
          </div>

          <div className="rounded-2xl border border-white/10 bg-[#11111b] p-5">
            <p className="text-sm text-gray-400">Payment Method</p>
            <p className="mt-3 text-2xl font-bold text-white">
              {order.paymentMethod || "—"}
            </p>
          </div>

          <div className="rounded-2xl border border-white/10 bg-[#11111b] p-5">
            <p className="text-sm text-gray-400">Payment Status</p>
            <p className="mt-3 text-2xl font-bold text-white">
              {order.paymentStatus || "—"}
            </p>
          </div>
        </section>

        <div className="mt-7 grid grid-cols-1 gap-6 lg:grid-cols-2">
          <InfoCard title="Customer Information" icon={UserRound}>
            <DetailRow
              label="Customer Name"
              value={order.customerName}
              emphasis
            />
            <DetailRow
              label="Phone Number"
              value={order.phoneNumber}
            />
            <DetailRow
              label="Order Number"
              value={order.orderNumber}
            />
            <DetailRow
              label="Order Date"
              value={formatDate(order.createdAt)}
            />
          </InfoCard>

          <InfoCard title="Shipping Address" icon={MapPin}>
            <p className="whitespace-pre-wrap break-words text-sm leading-7 text-gray-200">
              {order.shippingAddress || "No shipping address available"}
            </p>
          </InfoCard>

          <InfoCard title="Payment Information" icon={CreditCard}>
            <DetailRow
              label="Payment Method"
              value={order.paymentMethod}
            />
            <DetailRow
              label="Payment Status"
              value={order.paymentStatus}
            />
            <DetailRow
              label="Coupon Code"
              value={order.couponCode || "Not applied"}
            />
          </InfoCard>

          <InfoCard title="Order Timeline" icon={CalendarDays}>
            <DetailRow
              label="Placed At"
              value={formatDate(order.createdAt)}
            />
            <DetailRow
              label="Current Status"
              value={displayStatus(order.orderStatus)}
              emphasis
            />
            <p className="mt-2 text-xs leading-5 text-gray-500">
              Order status is managed from the Admin Orders page.
            </p>
          </InfoCard>
        </div>

        <section className="mt-7 overflow-hidden rounded-2xl border border-white/10 bg-[#11111b]">
          <div className="flex items-center gap-3 border-b border-white/10 p-5 sm:p-6">
            <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-violet-500/10 text-violet-300">
              <Package size={20} />
            </span>

            <div>
              <h2 className="text-lg font-bold">Purchased Items</h2>
              <p className="mt-1 text-sm text-gray-500">
                {items.length} product line{items.length === 1 ? "" : "s"}
              </p>
            </div>
          </div>

          {items.length === 0 ? (
            <div className="p-10 text-center text-sm text-gray-400">
              No item details were returned for this order.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[650px] text-left">
                <thead>
                  <tr className="bg-white/[0.025] text-xs uppercase tracking-wider text-gray-500">
                    <th className="px-5 py-4 font-semibold">Product</th>
                    <th className="px-5 py-4 font-semibold">Unit Price</th>
                    <th className="px-5 py-4 font-semibold">Quantity</th>
                    <th className="px-5 py-4 text-right font-semibold">
                      Line Total
                    </th>
                  </tr>
                </thead>

                <tbody className="divide-y divide-white/[0.06]">
                  {items.map((item, index) => (
                    <tr
                      key={`${item.productId ?? item.productName}-${index}`}
                      className="hover:bg-white/[0.02]"
                    >
                      <td className="px-5 py-5">
                        <p className="font-semibold text-white">
                          {item.productName || "Product"}
                        </p>
                        {item.productId != null && (
                          <p className="mt-1 text-xs text-gray-500">
                            Product ID: {item.productId}
                          </p>
                        )}
                      </td>

                      <td className="px-5 py-5 text-sm text-gray-300">
                        {formatCurrency(item.unitPrice)}
                      </td>

                      <td className="px-5 py-5 text-sm text-gray-300">
                        {item.quantity ?? 0}
                      </td>

                      <td className="px-5 py-5 text-right text-sm font-semibold text-white">
                        {formatCurrency(item.lineTotal)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="border-t border-white/10 p-5 sm:p-6">
            <div className="ml-auto max-w-md">
              <DetailRow
                label="Subtotal"
                value={formatCurrency(order.subtotal)}
              />
              <DetailRow
                label="Discount"
                value={`− ${formatCurrency(order.discountAmount)}`}
              />

              <div className="my-2 border-t border-white/10" />

              <div className="flex items-center justify-between gap-4 py-3">
                <span className="font-semibold text-gray-200">
                  Total Amount
                </span>
                <span className="text-xl font-bold text-violet-300">
                  {formatCurrency(order.totalAmount)}
                </span>
              </div>
            </div>
          </div>
        </section>

        <footer className="mt-8 flex items-center gap-2 border-t border-white/10 pt-5 text-xs text-gray-600">
          <ReceiptText size={15} />
          ASTRA E-Commerce · Admin Order Details
        </footer>
      </main>
    </div>
  );
}

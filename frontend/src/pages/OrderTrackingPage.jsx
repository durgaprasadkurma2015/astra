
import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  ArrowLeft,
  CheckCircle2,
  Circle,
  Clock,
  Package,
  PackageCheck,
  RefreshCw,
  Truck,
  XCircle,
  MapPin,
  CreditCard,
} from "lucide-react";

import orderService from "../services/orderService";

const STEPS = [
  {
    status: "PLACED",
    title: "Order Placed",
    description: "Your order has been placed.",
    icon: Package,
  },
  {
    status: "CONFIRMED",
    title: "Confirmed",
    description: "Your order has been confirmed.",
    icon: CheckCircle2,
  },
  {
    status: "SHIPPED",
    title: "Shipped",
    description: "Your order has been dispatched.",
    icon: Truck,
  },
  {
    status: "OUT_FOR_DELIVERY",
    title: "Out for Delivery",
    description: "Your order is on its way.",
    icon: MapPin,
  },
  {
    status: "DELIVERED",
    title: "Delivered",
    description: "Your order has been delivered.",
    icon: PackageCheck,
  },
];

function normalizeStatus(value) {
  const status = String(value || "PLACED")
    .trim()
    .toUpperCase()
    .replace(/[\s-]+/g, "_");

  if (status === "ORDER_PLACED") return "PLACED";
  if (status === "PROCESSING") return "CONFIRMED";

  return status;
}

function formatPrice(value) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(Number(value || 0));
}

function formatDate(value) {
  if (!value) return "Not available";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return "Not available";

  return date.toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getErrorMessage(err) {
  const responseStatus = err.response?.status;

  if (responseStatus === 401) {
    return "Session expired. Please log in again.";
  }

  if (responseStatus === 403) {
    return "You do not have permission to view this order.";
  }

  if (responseStatus === 404) {
    return "Order not found or you do not have access to it.";
  }

  return (
    err.response?.data?.detail ||
    err.response?.data?.message ||
    "Unable to load order tracking. Please try again."
  );
}

export default function OrderTrackingPage() {
  const { orderNumber } = useParams();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    async function fetchOrder() {
      try {
        if (!orderNumber) {
          if (!ignore) {
            setError("Order number is missing.");
          }
          return;
        }

        const data = await orderService.getOrder(orderNumber);

        if (!ignore) {
          setOrder(data);
          setError("");
        }
      } catch (err) {
        if (!ignore) {
          setError(getErrorMessage(err));
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    fetchOrder();

    return () => {
      ignore = true;
    };
  }, [orderNumber]);

  const loadOrder = useCallback(
    async (isRefresh = false) => {
      if (!orderNumber) {
        setError("Order number is missing.");
        return;
      }

      if (isRefresh) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }

      setError("");

      try {
        const data = await orderService.getOrder(orderNumber);
        setOrder(data);
      } catch (err) {
        setError(getErrorMessage(err));
      } finally {
        setLoading(false);
        setRefreshing(false);
      }
    },
    [orderNumber]
  );

  const status = normalizeStatus(order?.orderStatus);
  const cancelled = status === "CANCELLED";
  const currentIndex = STEPS.findIndex(
    (step) => step.status === status
  );

  return (
    <section className="bg-slate-950 px-4 py-8 text-white sm:px-6 sm:py-10">
      <main className="mx-auto max-w-4xl space-y-6">
        <div>
          <Link
            to="/my-orders"
            className="mb-6 inline-flex items-center gap-2 text-sm text-slate-300 transition hover:text-white"
          >
            <ArrowLeft size={17} />
            My Orders
          </Link>

          <p className="text-sm font-semibold uppercase tracking-widest text-cyan-400">
            ASTRA E-Commerce
          </p>

          <h1 className="mt-2 text-3xl font-bold sm:text-4xl">
            Track Your Order
          </h1>

          <p className="mt-2 text-sm text-slate-400">
            Check your order status and delivery progress.
          </p>
        </div>

        {loading && (
          <div className="flex items-center justify-center gap-3 rounded-2xl border border-white/10 bg-slate-900 p-12">
            <RefreshCw className="animate-spin" size={20} />
            Loading order...
          </div>
        )}

        {!loading && error && (
          <div className="rounded-2xl border border-red-500/20 bg-red-500/10 p-6">
            <XCircle className="mb-3 text-red-400" size={28} />

            <p className="text-red-200">{error}</p>

            <button
              type="button"
              onClick={() => loadOrder(true)}
              disabled={refreshing}
              className="mt-5 flex items-center gap-2 rounded-xl bg-white px-4 py-3 text-sm font-semibold text-slate-950 disabled:opacity-50"
            >
              <RefreshCw
                size={16}
                className={refreshing ? "animate-spin" : ""}
              />
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && order && (
          <>
            <section className="rounded-2xl border border-white/10 bg-slate-900 p-5 sm:p-7">
              <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
                <div>
                  <p className="text-sm text-slate-400">Order Number</p>

                  <h2 className="mt-1 break-all text-xl font-bold">
                    {order.orderNumber || orderNumber}
                  </h2>

                  <p className="mt-2 text-sm text-slate-400">
                    Placed on {formatDate(order.createdAt)}
                  </p>
                </div>

                <button
                  type="button"
                  onClick={() => loadOrder(true)}
                  disabled={refreshing}
                  className="flex items-center gap-2 self-start rounded-xl border border-white/10 px-3 py-2 text-sm hover:bg-white/5 disabled:opacity-50"
                >
                  <RefreshCw
                    size={15}
                    className={refreshing ? "animate-spin" : ""}
                  />
                  Refresh Status
                </button>
              </div>

              <div className="mt-6 border-t border-white/10 pt-5">
                <p className="text-sm text-slate-400">Current Status</p>

                <span
                  className={`mt-2 inline-flex items-center gap-2 rounded-full border px-4 py-2 text-sm font-semibold ${
                    cancelled
                      ? "border-red-400/20 bg-red-500/10 text-red-300"
                      : status === "DELIVERED"
                        ? "border-emerald-400/20 bg-emerald-500/10 text-emerald-300"
                        : "border-cyan-400/20 bg-cyan-500/10 text-cyan-300"
                  }`}
                >
                  {cancelled ? (
                    <XCircle size={17} />
                  ) : status === "DELIVERED" ? (
                    <CheckCircle2 size={17} />
                  ) : (
                    <Clock size={17} />
                  )}

                  {status.replace(/_/g, " ")}
                </span>
              </div>
            </section>

            <section className="rounded-2xl border border-white/10 bg-slate-900 p-5 sm:p-7">
              <h2 className="text-xl font-bold">
                {cancelled ? "Order Cancelled" : "Delivery Progress"}
              </h2>

              {cancelled ? (
                <div className="mt-5 flex items-start gap-3 rounded-xl border border-red-500/20 bg-red-500/5 p-4">
                  <XCircle
                    className="shrink-0 text-red-400"
                    size={23}
                  />

                  <div>
                    <p className="font-semibold text-red-300">
                      This order has been cancelled.
                    </p>

                    <p className="mt-1 text-sm text-slate-400">
                      Delivery will not proceed for this order.
                    </p>
                  </div>
                </div>
              ) : (
                <div className="mt-6">
                  {STEPS.map((step, index) => {
                    const Icon = step.icon;
                    const completed = currentIndex >= index;
                    const current = currentIndex === index;

                    return (
                      <div key={step.status} className="flex gap-4">
                        <div className="flex w-8 shrink-0 flex-col items-center">
                          <div
                            className={`z-10 flex h-8 w-8 items-center justify-center rounded-full border ${
                              completed
                                ? "border-cyan-400 bg-cyan-400 text-slate-950"
                                : "border-slate-700 bg-slate-800 text-slate-500"
                            }`}
                          >
                            {completed ? (
                              <Icon size={17} />
                            ) : (
                              <Circle size={15} />
                            )}
                          </div>

                          {index < STEPS.length - 1 && (
                            <div
                              className={`min-h-14 w-0.5 ${
                                currentIndex > index
                                  ? "bg-cyan-400"
                                  : "bg-slate-700"
                              }`}
                            />
                          )}
                        </div>

                        <div className="pb-7">
                          <p
                            className={`font-semibold ${
                              current
                                ? "text-cyan-300"
                                : completed
                                  ? "text-white"
                                  : "text-slate-500"
                            }`}
                          >
                            {step.title}

                            {current && (
                              <span className="ml-2 text-xs text-cyan-300">
                                Current
                              </span>
                            )}
                          </p>

                          <p className="mt-1 text-sm text-slate-400">
                            {step.description}
                          </p>

                          {step.status === "PLACED" && order.createdAt && (
                            <p className="mt-1 text-xs text-slate-500">
                              {formatDate(order.createdAt)}
                            </p>
                          )}
                        </div>
                      </div>
                    );
                  })}

                  {currentIndex === -1 && (
                    <p className="text-sm text-amber-300">
                      This order has an unrecognized status: {status}.
                    </p>
                  )}
                </div>
              )}

              <p className="mt-2 text-xs leading-5 text-slate-500">
                Status is retrieved from the backend. Refresh to check for
                updates.
              </p>
            </section>

            <section className="rounded-2xl border border-white/10 bg-slate-900 p-5 sm:p-7">
              <h2 className="text-xl font-bold">Order Summary</h2>

              <div className="mt-5 divide-y divide-white/10">
                {(Array.isArray(order.items) ? order.items : []).map(
                  (item, index) => (
                    <div
                      key={`${item.productId ?? item.id ?? "item"}-${index}`}
                      className="flex items-start justify-between gap-4 py-4 first:pt-0 last:pb-0"
                    >
                      <div>
                        <p className="font-medium">
                          {item.productName || item.name || "Product"}
                        </p>

                        <p className="mt-1 text-sm text-slate-400">
                          Qty: {item.quantity ?? 0} ×{" "}
                          {formatPrice(item.unitPrice)}
                        </p>
                      </div>

                      <p className="shrink-0 font-semibold">
                        {formatPrice(item.lineTotal)}
                      </p>
                    </div>
                  )
                )}
              </div>

              <div className="mt-5 flex items-center justify-between border-t border-white/10 pt-5">
                <span className="text-slate-400">Subtotal</span>
                <span className="text-xl font-bold">
                  {formatPrice(order.subtotal)}
                </span>
              </div>

              <div className="mt-4 flex items-center justify-between gap-3 text-sm">
                <span className="flex items-center gap-2 text-slate-400">
                  <CreditCard size={16} />
                  Payment Method
                </span>

                <span>{order.paymentMethod || "Not available"}</span>
              </div>

              <div className="mt-3 flex items-center justify-between gap-3 text-sm">
                <span className="text-slate-400">Payment Status</span>
                <span>{order.paymentStatus || "Not available"}</span>
              </div>
            </section>

            <div className="flex flex-wrap gap-3">
              <Link
                to="/my-orders"
                className="flex items-center gap-2 rounded-xl border border-white/10 px-4 py-3 text-sm font-semibold hover:bg-white/5"
              >
                <ArrowLeft size={16} />
                My Orders
              </Link>

              <Link
                to="/products"
                className="rounded-xl bg-cyan-400 px-4 py-3 text-sm font-semibold text-slate-950 hover:bg-cyan-300"
              >
                Continue Shopping
              </Link>
            </div>
          </>
        )}
      </main>
    </section>
  );
}

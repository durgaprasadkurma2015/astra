
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  CheckCircle,
  Package,
  ShoppingBag,
  LoaderCircle,
  MapPin,
  Phone,
  CalendarDays,
  UserRound,
  CreditCard,
  ClipboardCheck,
  Clock,
} from "lucide-react";
import orderService from "../services/orderService";
import { useAuth } from "../context/useAuth.js";

export default function OrderConfirmationPage() {
  const { orderNumber } = useParams();
  const { user } = useAuth();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function fetchOrder() {
      if (!orderNumber) {
        setError("Order number is missing.");
        setLoading(false);
        return;
      }

      try {
        const data = await orderService.getOrder(orderNumber);

        if (!cancelled) {
          setOrder(data);
          setError("");
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.data?.message ||
              err.response?.data?.detail ||
              "Unable to load your order. Please try again."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    fetchOrder();

    return () => {
      cancelled = true;
    };
  }, [orderNumber]);

  const formatPrice = (amount) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(Number(amount || 0));

  const formatDate = (dateValue) => {
    if (!dateValue) return "Date unavailable";

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
      return "Date unavailable";
    }

    return new Intl.DateTimeFormat("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date);
  };

  const displayStatus = (status) =>
    String(status || "PLACED")
      .replaceAll("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (character) => character.toUpperCase());

  const subtotal = Number(order?.subtotal ?? 0);
  const discountAmount = Number(order?.discountAmount ?? 0);

  const totalAmount =
    order?.totalAmount != null
      ? Number(order.totalAmount)
      : Math.max(subtotal - discountAmount, 0);

  return (
    <section className="min-h-[60vh] bg-[#09090f] text-white">
      <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 sm:py-12">
        <div className="mb-8 flex justify-end">
          <Link
            to="/products"
            className="inline-flex items-center gap-2 text-sm text-gray-300 transition hover:text-violet-300"
          >
            <ShoppingBag size={18} />
            <span>Continue shopping</span>
          </Link>
        </div>

        {loading ? (
          <div className="flex flex-col items-center py-20 text-gray-300">
            <LoaderCircle
              className="mb-4 animate-spin text-violet-400"
              size={36}
            />
            <p>Loading your order...</p>
          </div>
        ) : error ? (
          <div className="rounded-2xl border border-red-500/20 bg-red-500/10 p-8 text-center">
            <p className="mb-4 text-red-300">{error}</p>

            <button
              type="button"
              onClick={() => window.location.reload()}
              className="rounded-xl bg-violet-600 px-5 py-3 font-semibold transition hover:bg-violet-500"
            >
              Try again
            </button>
          </div>
        ) : order ? (
          <>
            <section className="mb-8 text-center">
              <CheckCircle
                className="mx-auto mb-4 text-emerald-400"
                size={64}
              />

              <p className="mb-2 text-sm font-semibold uppercase tracking-[0.2em] text-emerald-400">
                Order confirmed
              </p>

              <h1 className="mb-3 text-3xl font-bold sm:text-4xl">
                Order placed successfully!
              </h1>

              <p className="text-gray-400">
                Thank you
                {order.customerName
                  ? `, ${order.customerName}`
                  : user?.fullName
                    ? `, ${user.fullName}`
                    : ""}
                . Your order has been received.
              </p>

              <div className="mt-5 inline-block rounded-xl border border-violet-400/20 bg-violet-500/10 px-5 py-3">
                <p className="text-xs text-gray-400">Order number</p>
                <p className="mt-1 font-bold tracking-wide text-violet-300">
                  {order.orderNumber}
                </p>
              </div>
            </section>

            <section className="mb-6 rounded-2xl border border-white/10 bg-white/[0.03] p-5 sm:p-6">
              <div className="flex items-start gap-4">
                <div className="rounded-xl bg-emerald-500/10 p-3">
                  <ClipboardCheck
                    className="text-emerald-400"
                    size={25}
                  />
                </div>

                <div className="flex-1">
                  <p className="text-sm text-gray-400">
                    Current order status
                  </p>

                  <p className="mt-1 text-xl font-bold text-emerald-300">
                    {displayStatus(order.orderStatus)}
                  </p>

                  <p className="mt-2 text-sm text-gray-400">
                    We have received your order. Further status updates
                    will appear here when supported by the order system.
                  </p>
                </div>
              </div>
            </section>

            <section className="mb-6 rounded-2xl border border-white/10 bg-white/[0.03] p-6 sm:p-8">
              <div className="mb-6 flex items-center gap-3">
                <UserRound className="text-violet-400" size={24} />
                <h2 className="text-xl font-bold">Customer details</h2>
              </div>

              <div className="grid gap-6 sm:grid-cols-2">
                <div className="flex items-start gap-3">
                  <UserRound
                    className="mt-1 shrink-0 text-gray-400"
                    size={19}
                  />
                  <div>
                    <p className="text-sm text-gray-400">Full name</p>
                    <p className="mt-1 font-medium">
                      {order.customerName || "Not available"}
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <Phone
                    className="mt-1 shrink-0 text-gray-400"
                    size={19}
                  />
                  <div>
                    <p className="text-sm text-gray-400">Phone number</p>
                    <p className="mt-1 font-medium">
                      {order.phoneNumber || "Not available"}
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3 sm:col-span-2">
                  <MapPin
                    className="mt-1 shrink-0 text-gray-400"
                    size={19}
                  />
                  <div>
                    <p className="text-sm text-gray-400">
                      Shipping address
                    </p>
                    <p className="mt-1 whitespace-pre-line font-medium leading-6">
                      {order.shippingAddress || "Address not available"}
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3 sm:col-span-2">
                  <CalendarDays
                    className="mt-1 shrink-0 text-gray-400"
                    size={19}
                  />
                  <div>
                    <p className="text-sm text-gray-400">Order date</p>
                    <p className="mt-1 font-medium">
                      {formatDate(order.createdAt)}
                    </p>
                  </div>
                </div>
              </div>
            </section>

            <section className="mb-6 rounded-2xl border border-white/10 bg-white/[0.03] p-6 sm:p-8">
              <div className="mb-6 flex items-center gap-3">
                <Package className="text-violet-400" size={24} />
                <h2 className="text-xl font-bold">Order items</h2>
              </div>

              {(order.items || []).length > 0 ? (
                <div className="space-y-5">
                  {order.items.map((item, index) => (
                    <div
                      key={`${item.productId ?? item.productName}-${index}`}
                      className="flex items-start justify-between gap-4 border-b border-white/10 pb-5 last:border-b-0 last:pb-0"
                    >
                      <div className="min-w-0">
                        <p className="break-words font-medium">
                          {item.productName || "Product"}
                        </p>

                        <p className="mt-2 text-sm text-gray-400">
                          Quantity: {item.quantity}
                        </p>

                        <p className="mt-1 text-sm text-gray-400">
                          {formatPrice(item.unitPrice)} each
                        </p>
                      </div>

                      <p className="shrink-0 font-semibold text-white">
                        {formatPrice(item.lineTotal)}
                      </p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-gray-400">
                  No order items are available.
                </p>
              )}

              <div className="mt-6 space-y-4 border-t border-white/10 pt-6">
                <div className="flex items-center justify-between gap-4 text-sm">
                  <span className="flex items-center gap-2 text-gray-400">
                    <CreditCard size={17} />
                    Payment method
                  </span>
                  <span className="font-medium">
                    {displayStatus(order.paymentMethod || "COD")}
                  </span>
                </div>

                <div className="flex items-center justify-between gap-4 text-sm">
                  <span className="flex items-center gap-2 text-gray-400">
                    <Clock size={17} />
                    Payment status
                  </span>
                  <span className="font-medium text-amber-300">
                    {displayStatus(order.paymentStatus || "PENDING")}
                  </span>
                </div>

                <div className="flex items-center justify-between gap-4 text-sm">
                  <span className="text-gray-400">Subtotal</span>
                  <span>{formatPrice(subtotal)}</span>
                </div>

                {order.couponCode && (
                  <div className="flex items-center justify-between gap-4 text-sm">
                    <span className="text-gray-400">Coupon applied</span>
                    <span className="rounded-lg border border-emerald-400/20 bg-emerald-500/10 px-3 py-1 font-semibold text-emerald-300">
                      {order.couponCode}
                    </span>
                  </div>
                )}

                {discountAmount > 0 && (
                  <div className="flex items-center justify-between gap-4 text-sm">
                    <span className="text-gray-400">Coupon discount</span>
                    <span className="font-semibold text-emerald-400">
                      -{formatPrice(discountAmount)}
                    </span>
                  </div>
                )}

                <div className="flex items-center justify-between gap-4 border-t border-white/10 pt-5 text-lg font-bold">
                  <span>Final total</span>
                  <span className="text-violet-300">
                    {formatPrice(totalAmount)}
                  </span>
                </div>

                {discountAmount > 0 && (
                  <p className="text-right text-xs text-emerald-400">
                    You saved {formatPrice(discountAmount)} on this order!
                  </p>
                )}
              </div>
            </section>

            <div className="flex flex-col justify-center gap-3 sm:flex-row">
              <Link
                to="/my-orders"
                className="inline-flex items-center justify-center gap-2 rounded-xl border border-white/15 px-6 py-3 font-semibold transition hover:bg-white/5"
              >
                <Package size={18} />
                View my orders
              </Link>

              <Link
                to="/products"
                className="inline-flex items-center justify-center gap-2 rounded-xl bg-violet-600 px-7 py-3 font-semibold transition hover:bg-violet-500"
              >
                <ShoppingBag size={18} />
                Continue shopping
              </Link>
            </div>
          </>
        ) : null}
      </div>
    </section>
  );
}

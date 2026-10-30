
import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  Package,
  ShoppingBag,
  RefreshCw,
  ClipboardList,
  XCircle,
  CheckCircle,
  AlertCircle,
} from "lucide-react";
import orderService from "../services/orderService";

export default function MyOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({ totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [cancellingOrder, setCancellingOrder] = useState("");
  const [cancelMessage, setCancelMessage] = useState("");
  const [cancelError, setCancelError] = useState("");
  const [confirmOrder, setConfirmOrder] = useState(null);
  const [payingOrder, setPayingOrder] = useState("");

  const formatPrice = (amount) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(Number(amount || 0));

  const formatDate = (value) => {
    if (!value) return "Date unavailable";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return "Date unavailable";
    }

    return date.toLocaleString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const loadOrders = useCallback(async (pageNumber = page) => {
    setLoading(true);
    setError("");

    try {
      const data = await orderService.getMyOrders(pageNumber, 10);
      setOrders(Array.isArray(data?.content) ? data.content : []);
      setPageInfo({
        totalPages: Number(data?.totalPages || 0),
        totalElements: Number(data?.totalElements || 0),
      });
    } catch (err) {
      const status = err.response?.status;

      if (status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (status === 403) {
        setError(
          "Access denied. Please log in again or check backend authorization."
        );
      } else {
        setError(
          err.response?.data?.detail ||
            err.response?.data?.message ||
            "Unable to load your orders. Please try again."
        );
      }
    } finally {
      setLoading(false);
    }
  }, [page]);

  const handleRetryPayment = async (order) => {
    if (payingOrder || !order.razorpayOrderId || !order.razorpayKeyId) return;
    setPayingOrder(order.orderNumber);
    setError("");
    try {
      if (!window.Razorpay) {
        await new Promise((resolve, reject) => {
          const script = document.createElement("script");
          script.src = "https://checkout.razorpay.com/v1/checkout.js";
          script.async = true;
          script.onload = () => window.Razorpay ? resolve() : reject(new Error("Payment checkout could not be loaded."));
          script.onerror = () => reject(new Error("Payment checkout could not be loaded."));
          document.body.appendChild(script);
        });
      }
      const checkout = new window.Razorpay({
        key: order.razorpayKeyId,
        amount: Math.round(Number(order.totalAmount) * 100),
        currency: "INR",
        name: "ASTRA",
        description: `Order ${order.orderNumber}`,
        order_id: order.razorpayOrderId,
        handler: async (payment) => {
          try {
            const paidOrder = await orderService.verifyRazorpayPayment(order.orderNumber, payment);
            setOrders((current) => current.map((item) => item.orderNumber === order.orderNumber ? paidOrder : item));
            setCancelMessage(`Payment received for order ${order.orderNumber}.`);
          } catch (err) {
            setError(err.response?.data?.detail || err.response?.data?.message || "Payment verification failed. Contact support before retrying.");
          } finally {
            setPayingOrder("");
          }
        },
        modal: { ondismiss: () => setPayingOrder("") },
      });
      checkout.on("payment.failed", (event) => {
        setError(event.error?.description || "Payment failed. You can try again.");
        setPayingOrder("");
      });
      checkout.open();
    } catch (err) {
      setError(err.message || "Unable to start payment. Please try again.");
      setPayingOrder("");
    }
  };

  useEffect(() => {
    void Promise.resolve().then(loadOrders);
  }, [loadOrders]);

  const statusStyle = (value) => {
    switch ((value || "").toUpperCase()) {
      case "DELIVERED":
        return "border-emerald-400/20 bg-emerald-500/10 text-emerald-300";
      case "CANCELLED":
        return "border-red-400/20 bg-red-500/10 text-red-300";
      case "SHIPPED":
        return "border-sky-400/20 bg-sky-500/10 text-sky-300";
      case "OUT_FOR_DELIVERY":
        return "border-cyan-400/20 bg-cyan-500/10 text-cyan-300";
      case "CONFIRMED":
        return "border-blue-400/20 bg-blue-500/10 text-blue-300";
      case "PLACED":
      default:
        return "border-violet-400/20 bg-violet-500/10 text-violet-300";
    }
  };

  const handleCancelOrder = async () => {
    if (!confirmOrder?.orderNumber || cancellingOrder) return;

    const orderNumber = confirmOrder.orderNumber;

    setCancellingOrder(orderNumber);
    setCancelMessage("");
    setCancelError("");
    setConfirmOrder(null);

    try {
      const updatedOrder = await orderService.cancelOrder(orderNumber);

      setOrders((previousOrders) =>
        previousOrders.map((order) =>
          order.orderNumber === orderNumber
            ? { ...order, ...updatedOrder, orderStatus: "CANCELLED" }
            : order
        )
      );

      setCancelMessage(
        `Order ${orderNumber} cancelled successfully.`
      );
    } catch (err) {
      const status = err.response?.status;

      if (status === 401) {
        setCancelError("Your session has expired. Please log in again.");
      } else if (status === 404) {
        setCancelError(
          "Order not found or you do not have access to it."
        );
      } else if (status === 409) {
        setCancelError(
          err.response?.data?.detail ||
            err.response?.data?.message ||
            "This order cannot be cancelled because its status has changed."
        );
      } else {
        setCancelError(
          err.response?.data?.detail ||
            err.response?.data?.message ||
            "Unable to cancel this order. Please try again."
        );
      }

      await loadOrders();
    } finally {
      setCancellingOrder("");
    }
  };

  return (
    <section className="min-h-[60vh] bg-[#09090f] text-white">
      <div className="mx-auto max-w-5xl px-4 py-8 sm:px-6 sm:py-12">
        <div className="mb-7 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-violet-400/20 bg-violet-500/10">
              <ClipboardList
                size={28}
                className="text-violet-300"
              />
            </div>

            <div>
              <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
                My Orders
              </h1>
              <p className="mt-2 text-sm text-gray-400">
                View your orders and track their status.
              </p>
            </div>
          </div>

          <Link
            to="/products"
            className="inline-flex items-center gap-2 text-sm text-gray-300 transition hover:text-violet-300"
          >
            <ShoppingBag size={18} />
            Continue shopping
          </Link>
        </div>

        {cancelMessage && (
          <div
            role="status"
            className="mb-5 flex items-start gap-3 rounded-xl border border-emerald-400/20 bg-emerald-500/10 p-4 text-sm text-emerald-300"
          >
            <CheckCircle size={20} className="shrink-0" />
            <p>{cancelMessage}</p>
            <button
              type="button"
              onClick={() => setCancelMessage("")}
              className="ml-auto"
              aria-label="Dismiss success message"
            >
              <XCircle size={18} />
            </button>
          </div>
        )}

        {cancelError && (
          <div
            role="alert"
            className="mb-5 flex items-start gap-3 rounded-xl border border-red-400/20 bg-red-500/10 p-4 text-sm text-red-300"
          >
            <AlertCircle size={20} className="shrink-0" />
            <p>{cancelError}</p>
            <button
              type="button"
              onClick={() => setCancelError("")}
              className="ml-auto"
              aria-label="Dismiss error message"
            >
              <XCircle size={18} />
            </button>
          </div>
        )}

        {loading ? (
          <div className="flex flex-col items-center justify-center rounded-2xl border border-white/10 bg-white/[0.03] px-5 py-20 text-center">
            <RefreshCw
              size={30}
              className="mb-4 animate-spin text-violet-300"
            />
            <p className="text-gray-300">Loading your orders...</p>
          </div>
        ) : error ? (
          <div className="rounded-2xl border border-red-400/20 bg-red-500/5 px-6 py-10 text-center">
            <p role="alert" className="mb-5 text-sm text-red-300">
              {error}
            </p>

            <button
              type="button"
              onClick={loadOrders}
              className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold transition hover:bg-violet-500"
            >
              <RefreshCw size={16} />
              Try again
            </button>
          </div>
        ) : orders.length === 0 ? (
          <div className="rounded-2xl border border-white/10 bg-white/[0.03] px-6 py-16 text-center">
            <Package
              size={48}
              className="mx-auto mb-5 text-gray-500"
            />

            <h2 className="text-xl font-semibold">No orders yet</h2>

            <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-gray-400">
              Your orders will appear here after you complete checkout.
            </p>

            <Link
              to="/products"
              className="mt-6 inline-flex items-center gap-2 rounded-xl bg-violet-600 px-6 py-3 font-semibold transition hover:bg-violet-500"
            >
              <ShoppingBag size={18} />
              Explore products
            </Link>
          </div>
        ) : (
          <div className="space-y-5">
            <div className="flex items-center justify-between text-sm text-gray-400">
              <span>
                {pageInfo.totalElements} {pageInfo.totalElements === 1 ? "order" : "orders"}
              </span>

              <button
                type="button"
                onClick={loadOrders}
                disabled={loading || Boolean(cancellingOrder)}
                className="inline-flex items-center gap-2 transition hover:text-white disabled:opacity-50"
              >
                <RefreshCw size={15} />
                Refresh
              </button>
            </div>

            {orders.map((order) => {
              const subtotal = Number(order.subtotal ?? 0);
              const discountAmount = Number(order.discountAmount ?? 0);

              const totalAmount =
                order.totalAmount != null
                  ? Number(order.totalAmount)
                  : Math.max(subtotal - discountAmount, 0);

              const isPlaced =
                (order.orderStatus || "").toUpperCase() === "PLACED";

              const isCancelling =
                cancellingOrder === order.orderNumber;

              return (
                <article
                  key={order.id ?? order.orderNumber}
                  className="overflow-hidden rounded-2xl border border-white/10 bg-white/[0.03] transition hover:border-violet-400/30"
                >
                  <div className="flex flex-col gap-4 border-b border-white/10 p-5 sm:flex-row sm:items-center sm:justify-between sm:p-6">
                    <div>
                      <p className="text-xs uppercase tracking-wider text-gray-500">
                        Order number
                      </p>

                      <h2 className="mt-1 break-all font-semibold text-violet-300">
                        {order.orderNumber || "Unavailable"}
                      </h2>

                      <p className="mt-2 text-sm text-gray-400">
                        Placed on {formatDate(order.createdAt)}
                      </p>
                    </div>

                    <span
                      className={`w-fit rounded-full border px-3 py-1.5 text-xs font-semibold ${statusStyle(order.orderStatus)}`}
                    >
                      {(order.orderStatus || "PLACED").replace(/_/g, " ")}
                    </span>
                  </div>

                  <div className="p-5 sm:p-6">
                    <div className="space-y-4">
                      {(Array.isArray(order.items) ? order.items : []).map(
                        (item, index) => (
                          <div
                            key={`${item.productId ?? item.productName ?? "item"}-${index}`}
                            className="flex items-start justify-between gap-4"
                          >
                            <div className="min-w-0">
                              <p className="break-words font-medium text-gray-200">
                                {item.productName || "Product"}
                              </p>

                              <p className="mt-1 text-sm text-gray-400">
                                Qty: {item.quantity ?? 1}
                              </p>
                            </div>

                            <p className="shrink-0 text-sm font-medium">
                              {formatPrice(item.lineTotal)}
                            </p>
                          </div>
                        )
                      )}
                    </div>

                    <div className="mt-5 flex flex-col gap-4 border-t border-white/10 pt-5 sm:flex-row sm:items-center sm:justify-between">
                      <div>
                        <p className="text-xs text-gray-500">Payment</p>

                        <p className="mt-1 text-sm text-gray-300">
                          {order.paymentMethod || "COD"} ·{" "}
                          {order.paymentStatus || "PENDING"}
                        </p>

                        <div className="mt-3 space-y-2">
                          <div className="flex items-center justify-between gap-4 text-sm">
                            <span className="text-gray-400">Subtotal</span>
                            <span>{formatPrice(subtotal)}</span>
                          </div>

                          {order.couponCode && (
                            <div className="flex items-center justify-between gap-4 text-sm">
                              <span className="text-gray-400">Coupon</span>
                              <span className="font-semibold text-emerald-300">
                                {order.couponCode}
                              </span>
                            </div>
                          )}

                          {discountAmount > 0 && (
                            <div className="flex items-center justify-between gap-4 text-sm">
                              <span className="text-gray-400">Discount</span>
                              <span className="font-semibold text-emerald-400">
                                -{formatPrice(discountAmount)}
                              </span>
                            </div>
                          )}

                          <p className="pt-1 text-xs text-gray-500">
                            Final total
                          </p>

                          <p className="text-xl font-bold text-violet-300">
                            {formatPrice(totalAmount)}
                          </p>
                        </div>
                      </div>

                      <div className="flex flex-col gap-3 sm:items-end">
                        {order.paymentMethod === "RAZORPAY" && order.paymentStatus !== "PAID" && order.razorpayOrderId && order.razorpayKeyId && (
                          <button type="button" onClick={() => handleRetryPayment(order)} disabled={Boolean(payingOrder)} className="inline-flex items-center justify-center gap-2 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold transition hover:bg-violet-500 disabled:opacity-50">
                            {payingOrder === order.orderNumber ? "Opening payment…" : "Pay now"}
                          </button>
                        )}
                        <Link
                          to={`/order-confirmation/${encodeURIComponent(order.orderNumber || "")}`}
                          className="inline-flex items-center justify-center gap-2 rounded-xl border border-white/10 px-4 py-3 text-sm font-semibold transition hover:border-violet-400/40 hover:bg-violet-500/10"
                        >
                          View order
                          <ArrowRight size={16} />
                        </Link>

                        {order.orderNumber && (
                          <Link
                            to={`/track-order/${encodeURIComponent(order.orderNumber)}`}
                            className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-400 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-300"
                          >
                            <Package size={16} />
                            Track Order
                          </Link>
                        )}

                        {isPlaced && !(order.paymentMethod === "RAZORPAY" && order.paymentStatus === "PAID") && (
                          <button
                            type="button"
                            onClick={() => {
                              setCancelError("");
                              setCancelMessage("");
                              setConfirmOrder(order);
                            }}
                            disabled={Boolean(cancellingOrder)}
                            className="inline-flex items-center justify-center gap-2 rounded-xl border border-red-400/30 px-4 py-3 text-sm font-semibold text-red-300 transition hover:bg-red-500/10 disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            {isCancelling ? (
                              <RefreshCw
                                size={16}
                                className="animate-spin"
                              />
                            ) : (
                              <XCircle size={16} />
                            )}
                            {isCancelling ? "Cancelling..." : "Cancel order"}
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </article>
              );
            })}
            {pageInfo.totalPages > 1 && (
              <nav aria-label="Order pages" className="flex items-center justify-between gap-4 pt-2">
                <button type="button" disabled={page <= 0 || loading} onClick={() => setPage((value) => Math.max(0, value - 1))} className="rounded-lg border border-white/10 px-4 py-2 text-sm disabled:opacity-40">Previous</button>
                <span className="text-sm text-gray-400">Page {page + 1} of {pageInfo.totalPages}</span>
                <button type="button" disabled={page + 1 >= pageInfo.totalPages || loading} onClick={() => setPage((value) => Math.min(pageInfo.totalPages - 1, value + 1))} className="rounded-lg border border-white/10 px-4 py-2 text-sm disabled:opacity-40">Next</button>
              </nav>
            )}
          </div>
        )}
      </div>

      {confirmOrder && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-5 backdrop-blur-sm"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              setConfirmOrder(null);
            }
          }}
        >
          <section
            role="dialog"
            aria-modal="true"
            aria-labelledby="cancel-order-title"
            className="w-full max-w-md rounded-2xl border border-white/10 bg-[#13111d] p-6 shadow-2xl"
          >
            <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-red-500/10">
              <XCircle size={25} className="text-red-400" />
            </div>

            <h2
              id="cancel-order-title"
              className="text-xl font-bold text-white"
            >
              Cancel this order?
            </h2>

            <p className="mt-3 text-sm leading-6 text-gray-400">
              Are you sure you want to cancel order{" "}
              <span className="font-semibold text-violet-300">
                {confirmOrder.orderNumber}
              </span>
              ? This action cannot be undone.
            </p>

            <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
              <button
                type="button"
                onClick={() => setConfirmOrder(null)}
                className="rounded-xl border border-white/10 px-5 py-3 text-sm font-semibold text-gray-300 transition hover:bg-white/5"
              >
                Keep order
              </button>

              <button
                type="button"
                onClick={handleCancelOrder}
                disabled={Boolean(cancellingOrder)}
                className="inline-flex items-center justify-center gap-2 rounded-xl bg-red-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-red-500 disabled:opacity-50"
              >
                <XCircle size={16} />
                Yes, cancel order
              </button>
            </div>
          </section>
        </div>
      )}
    </section>
  );
}

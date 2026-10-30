
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

const nextStatuses = {
  PLACED: ["CONFIRMED"],
  CONFIRMED: ["SHIPPED"],
  SHIPPED: ["OUT_FOR_DELIVERY"],
  OUT_FOR_DELIVERY: ["DELIVERED"],
  DELIVERED: [],
  CANCELLED: [],
};

const statusStyles = {
  PLACED: "border-amber-400/20 bg-amber-400/10 text-amber-300",
  CONFIRMED: "border-blue-400/20 bg-blue-400/10 text-blue-300",
  SHIPPED: "border-violet-400/20 bg-violet-400/10 text-violet-300",
  OUT_FOR_DELIVERY: "border-cyan-400/20 bg-cyan-400/10 text-cyan-300",
  DELIVERED: "border-emerald-400/20 bg-emerald-400/10 text-emerald-300",
  CANCELLED: "border-red-400/20 bg-red-400/10 text-red-300",
};

const formatCurrency = (amount) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    minimumFractionDigits: 2,
  }).format(Number(amount || 0));

function StatusBadge({ status }) {
  const classes =
    statusStyles[status] || "border-white/10 bg-white/5 text-gray-300";

  return (
    <span
      className={`inline-flex whitespace-nowrap rounded-full border px-3 py-1.5 text-xs font-semibold ${classes}`}
    >
      <span className="mr-2">●</span>
      {(status || "UNKNOWN").replaceAll("_", " ")}
    </span>
  );
}

function SummaryCard({ label, value, description, icon }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-[#11111b] p-5">
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-400">{label}</p>
        <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-violet-500/10 text-xl">
          {icon}
        </span>
      </div>
      <p className="mt-4 text-3xl font-bold tracking-tight text-white">
        {value}
      </p>
      <p className="mt-2 text-xs text-gray-500">{description}</p>
    </div>
  );
}

export default function AdminOrdersPage() {
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({ totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [updatingOrder, setUpdatingOrder] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function fetchOrders() {
      try {
        const response = await api.get("/admin/orders/page", {
          params: { page, size: 20, query: search.trim() || undefined, status: statusFilter },
        });

        if (!cancelled) {
          setOrders(Array.isArray(response.data?.content) ? response.data.content : []);
          setPageInfo({ totalPages: Number(response.data?.totalPages || 0), totalElements: Number(response.data?.totalElements || 0) });
          setError("");
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.status === 403
              ? "Access denied. An administrator account is required."
              : err.response?.status === 401
                ? "Your session may have expired. Please log in again."
                : err.response?.data?.message ||
                  "Unable to load orders. Check your backend connection."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void fetchOrders();

    return () => {
      cancelled = true;
    };
  }, [reloadKey, page, search, statusFilter]);

  function loadOrders() {
    setLoading(true);
    setError("");
    setMessage("");
    setReloadKey((key) => key + 1);
  }

  async function updateStatus(orderNumber, orderStatus) {
    setUpdatingOrder(orderNumber);
    setError("");
    setMessage("");

    try {
      const response = await api.put(
        `/admin/orders/${encodeURIComponent(orderNumber)}/status`,
        { orderStatus }
      );

      setOrders((currentOrders) =>
        currentOrders.map((order) =>
          order.orderNumber === orderNumber
            ? response.data && typeof response.data === "object"
              ? { ...order, ...response.data, orderStatus }
              : { ...order, orderStatus }
            : order
        )
      );

      setMessage(`Order ${orderNumber} updated to ${orderStatus}.`);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          `Could not update order ${orderNumber}.`
      );
    } finally {
      setUpdatingOrder("");
    }
  }

  const filteredOrders = orders;

  const totalValue = orders.reduce(
    (sum, order) => sum + Number(order.subtotal || 0),
    0
  );

  const pendingOrders = orders.filter((order) =>
    ["PLACED", "CONFIRMED"].includes(order.orderStatus)
  ).length;

  const deliveredOrders = orders.filter(
    (order) => order.orderStatus === "DELIVERED"
  ).length;

  const allStatuses = [
    ...new Set([
      ...Object.keys(nextStatuses),
      ...orders.map((order) => order.orderStatus).filter(Boolean),
    ]),
  ];

  const selectClass =
    "rounded-xl border border-white/10 bg-[#151522] px-3 py-2.5 text-sm text-gray-200 outline-none transition focus:border-violet-500";

  return (
    <div className="min-h-screen bg-[#09090f] px-4 py-6 text-white sm:px-7 sm:py-8 lg:px-10">
      <header className="flex flex-col justify-between gap-5 border-b border-white/10 pb-7 sm:flex-row sm:items-center">
        <div>
          <button
            type="button"
            onClick={() => navigate("/admin")}
            className="mb-3 text-sm text-gray-500 transition hover:text-violet-300"
          >
            ← Back to dashboard
          </button>

          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-violet-400">
            Store operations
          </p>

          <h1 className="mt-2 text-3xl font-bold sm:text-4xl">
            Order Management
          </h1>

          <p className="mt-2 text-sm text-gray-400">
            Track customer orders and manage delivery progress.
          </p>
        </div>

        <button
          type="button"
          onClick={loadOrders}
          disabled={loading}
          className="inline-flex items-center justify-center gap-2 self-start rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:opacity-60 sm:self-auto"
        >
          <span>{loading ? "⟳" : "↻"}</span>
          {loading ? "Refreshing..." : "Refresh Orders"}
        </button>
      </header>

      {error && (
        <div
          role="alert"
          className="mt-6 rounded-xl border border-red-400/20 bg-red-500/10 p-4 text-sm text-red-300"
        >
          <strong className="mr-2">Error:</strong>
          {error}
          {error.includes("log in again") && (
            <button
              type="button"
              onClick={() => navigate("/login")}
              className="ml-3 font-semibold underline"
            >
              Log in
            </button>
          )}
        </div>
      )}

      {message && (
        <div
          role="status"
          className="mt-6 rounded-xl border border-emerald-400/20 bg-emerald-500/10 p-4 text-sm text-emerald-300"
        >
          {message}
        </div>
      )}

      <section className="mt-7 grid grid-cols-1 gap-4 sm:grid-cols-2 2xl:grid-cols-4">
        <SummaryCard
          label="Total Orders"
          value={pageInfo.totalElements}
          description="Orders matching the current filters"
          icon="📦"
        />
        <SummaryCard
          label="Order Value"
          value={formatCurrency(totalValue)}
          description="Sum of order subtotals"
          icon="₹"
        />
        <SummaryCard
          label="Pending Processing"
          value={pendingOrders}
          description="Placed or confirmed orders"
          icon="◷"
        />
        <SummaryCard
          label="Delivered"
          value={deliveredOrders}
          description="Successfully delivered orders"
          icon="✓"
        />
      </section>

      <section className="mt-8 overflow-hidden rounded-2xl border border-white/10 bg-[#11111b]">
        <div className="flex flex-col gap-4 border-b border-white/10 p-5 sm:p-6 xl:flex-row xl:items-center xl:justify-between">
          <div>
            <h2 className="text-lg font-bold">All Orders</h2>
            <p className="mt-1 text-sm text-gray-500">
              {loading
                ? "Loading orders..."
                : `${filteredOrders.length} shown of ${pageInfo.totalElements} matching orders`}
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <label className="sr-only" htmlFor="order-search">
              Search orders
            </label>
            <input
              id="order-search"
              type="search"
              value={search}
              onChange={(event) => { setPage(0); setSearch(event.target.value); }}
              placeholder="Search order or customer..."
              className="w-full rounded-xl border border-white/10 bg-[#09090f] px-4 py-3 text-sm text-white outline-none placeholder:text-gray-600 focus:border-violet-500 sm:w-64"
            />

            <label className="sr-only" htmlFor="order-status-filter">
              Filter by status
            </label>
            <select
              id="order-status-filter"
              value={statusFilter}
              onChange={(event) => { setPage(0); setStatusFilter(event.target.value); }}
              className={selectClass}
            >
              <option value="ALL">All statuses</option>
              {allStatuses.map((status) => (
                <option key={status} value={status}>
                  {status.replaceAll("_", " ")}
                </option>
              ))}
            </select>
          </div>
        </div>

        {loading ? (
          <div className="p-12 text-center">
            <div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-violet-500/30 border-t-violet-400" />
            <p className="mt-4 text-sm text-gray-400">
              Loading your orders...
            </p>
          </div>
        ) : orders.length === 0 && !error ? (
          <div className="p-12 text-center">
            <div className="text-4xl">📦</div>
            <h3 className="mt-4 font-semibold">No orders yet</h3>
            <p className="mt-2 text-sm text-gray-500">
              Customer orders will appear here when they are placed.
            </p>
          </div>
        ) : filteredOrders.length === 0 && !error ? (
          <div className="p-10 text-center">
            <p className="font-semibold">No matching orders</p>
            <p className="mt-2 text-sm text-gray-500">
              Try a different search term or status filter.
            </p>
            <button
              type="button"
              onClick={() => {
                setSearch("");
                setStatusFilter("ALL");
              }}
              className="mt-4 text-sm font-semibold text-violet-300 hover:text-violet-200"
            >
              Clear filters
            </button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[1050px] border-collapse text-left">
              <thead>
                <tr className="bg-white/[0.025] text-xs uppercase tracking-wider text-gray-500">
                  {[
                    "Order",
                    "Customer",
                    "Total",
                    "Payment",
                    "Status",
                    "Update Status",
                  ].map((heading) => (
                    <th
                      key={heading}
                      className="border-b border-white/10 px-5 py-4 font-semibold"
                    >
                      {heading}
                    </th>
                  ))}
                </tr>
              </thead>

              <tbody className="divide-y divide-white/[0.06]">
                {filteredOrders.map((order) => {
                  const availableStatuses =
                    nextStatuses[order.orderStatus] || [];

                  return (
                    <tr
                      key={order.orderNumber}
                      className="transition hover:bg-white/[0.025]"
                    >
                      <td className="px-5 py-5">
                        <button
                          type="button"
                          onClick={() =>
                            navigate(
                              `/admin/orders/${encodeURIComponent(order.orderNumber)}`
                            )
                          }
                          className="font-semibold text-violet-300 transition hover:text-violet-200 hover:underline"
                        >
                          {order.orderNumber}
                        </button>
                      </td>

                      <td className="px-5 py-5">
                        <p className="font-medium text-gray-200">
                          {order.customerName || "Customer"}
                        </p>
                      </td>

                      <td className="px-5 py-5">
                        <span className="font-semibold text-white">
                          {formatCurrency(order.subtotal)}
                        </span>
                      </td>

                      <td className="px-5 py-5">
                        <p className="text-sm text-gray-200">
                          {order.paymentMethod || "—"}
                        </p>
                        <p className="mt-1 text-xs text-gray-500">
                          {order.paymentStatus || "—"}
                        </p>
                      </td>

                      <td className="px-5 py-5">
                        <StatusBadge status={order.orderStatus} />
                      </td>

                      <td className="px-5 py-5">
                        {availableStatuses.length === 0 ? (
                          <span className="text-xs text-gray-500">
                            No further updates
                          </span>
                        ) : (
                          <div className="flex items-center gap-2">
                            <select
                              aria-label={`Update order ${order.orderNumber}`}
                              value=""
                              disabled={updatingOrder === order.orderNumber}
                              onChange={(event) => {
                                const newStatus = event.target.value;
                                if (newStatus) {
                                  void updateStatus(
                                    order.orderNumber,
                                    newStatus
                                  );
                                }
                              }}
                              className={`${selectClass} max-w-48`}
                            >
                              <option value="">Change status...</option>
                              {availableStatuses.map((status) => (
                                <option key={status} value={status}>
                                  {status.replaceAll("_", " ")}
                                </option>
                              ))}
                            </select>

                            {updatingOrder === order.orderNumber && (
                              <span
                                className="text-xs text-violet-300"
                                role="status"
                              >
                                Saving...
                              </span>
                            )}
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {pageInfo.totalPages > 1 && (
        <nav aria-label="Admin order pages" className="mt-5 flex items-center justify-between gap-4">
          <button type="button" disabled={page <= 0 || loading} onClick={() => setPage((value) => Math.max(0, value - 1))} className="rounded-lg border border-white/10 px-4 py-2 text-sm disabled:opacity-40">Previous</button>
          <span className="text-sm text-gray-400">Page {page + 1} of {pageInfo.totalPages}</span>
          <button type="button" disabled={page + 1 >= pageInfo.totalPages || loading} onClick={() => setPage((value) => Math.min(pageInfo.totalPages - 1, value + 1))} className="rounded-lg border border-white/10 px-4 py-2 text-sm disabled:opacity-40">Next</button>
        </nav>
      )}

      <footer className="mt-8 border-t border-white/10 pt-5 text-xs text-gray-600">
        ASTRA E-Commerce · Order Management
      </footer>
    </div>
  );
}

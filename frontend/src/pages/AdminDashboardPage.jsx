
import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  Activity,
  ArrowDownToLine,

  BarChart3,
  CalendarDays,

  Package,
  RefreshCw,
  ShoppingBag,
  Users,
  Warehouse,

  IndianRupee,
  TrendingUp,

} from "lucide-react";

import api from "../services/api";

const today = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};

const daysAgo = (days) => {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};

const initialRange = {
  startDate: daysAgo(29),
  endDate: today(),
};

const money = (value) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(Number(value || 0));

const number = (value) =>
  new Intl.NumberFormat("en-IN").format(Number(value || 0));

const dateLabel = (value) =>
  new Date(`${value}T00:00:00`).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
  });

function Card({ children, className = "" }) {
  return (
    <section
      className={`rounded-2xl border border-white/10 bg-[#11111c] p-5 ${className}`}
    >
      {children}
    </section>
  );
}

function MetricCard({ title, value, description, icon: Icon, accent }) {
  return (
    <Card>
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-400">{title}</p>
        <div className={`rounded-xl p-3 ${accent}`}>
          <Icon size={20} />
        </div>
      </div>

      <p className="mt-5 text-2xl font-bold text-white sm:text-3xl">
        {value}
      </p>

      <p className="mt-2 text-xs text-gray-500">
        {description}
      </p>
    </Card>
  );
}

function SalesChart({ data = [] }) {
  const width = 760;
  const height = 230;
  const left = 58;
  const right = 18;
  const top = 18;
  const bottom = 38;

  const values = data.flatMap((item) => [
    Number(item.grossSales || 0),
    Number(item.collectedRevenue || 0),
  ]);

  const max = Math.max(...values, 1);
  const chartWidth = width - left - right;
  const chartHeight = height - top - bottom;

  const points = (key) =>
    data.map((item, index) => {
      const x =
        left +
        (data.length <= 1
          ? chartWidth / 2
          : (index / (data.length - 1)) * chartWidth);

      const y =
        top +
        chartHeight -
        (Number(item[key] || 0) / max) * chartHeight;

      return `${x},${y}`;
    }).join(" ");

  const labels = data.filter((_, index) =>
    index === 0 ||
    index === data.length - 1 ||
    index === Math.floor(data.length / 2)
  );

  return (
    <Card>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="font-semibold text-white">
            Sales Performance
          </h2>
          <p className="mt-1 text-xs text-gray-500">
            Daily order value and collected revenue
          </p>
        </div>

        <div className="flex gap-4 text-xs">
          <span className="flex items-center gap-2 text-violet-300">
            <span className="h-2 w-2 rounded-full bg-violet-400" />
            Gross sales
          </span>
          <span className="flex items-center gap-2 text-emerald-300">
            <span className="h-2 w-2 rounded-full bg-emerald-400" />
            Collected
          </span>
        </div>
      </div>

      {data.length === 0 ? (
        <p className="py-16 text-center text-sm text-gray-500">
          No sales data available.
        </p>
      ) : (
        <div className="mt-5 overflow-x-auto">
          <svg
            viewBox={`0 0 ${width} ${height}`}
            className="min-w-[540px] w-full"
            role="img"
            aria-label="Daily gross sales and collected revenue chart"
          >
            {[0, 1, 2, 3].map((line) => {
              const y = top + (chartHeight / 3) * line;
              return (
                <g key={line}>
                  <line
                    x1={left}
                    x2={width - right}
                    y1={y}
                    y2={y}
                    stroke="#343444"
                    strokeDasharray="4 5"
                  />
                  <text
                    x={left - 8}
                    y={y + 4}
                    fill="#88889a"
                    fontSize="10"
                    textAnchor="end"
                  >
                    {money(max * (1 - line / 3))}
                  </text>
                </g>
              );
            })}

            <polyline
              points={points("grossSales")}
              fill="none"
              stroke="#a78bfa"
              strokeWidth="3"
              strokeLinejoin="round"
              strokeLinecap="round"
            />

            <polyline
              points={points("collectedRevenue")}
              fill="none"
              stroke="#34d399"
              strokeWidth="3"
              strokeLinejoin="round"
              strokeLinecap="round"
            />

            {labels.map((item) => {
              const index = data.indexOf(item);
              const x = left +
                (data.length <= 1
                  ? chartWidth / 2
                  : (index / (data.length - 1)) * chartWidth);

              return (
                <text
                  key={item.day}
                  x={x}
                  y={height - 10}
                  fill="#9999a9"
                  fontSize="10"
                  textAnchor="middle"
                >
                  {dateLabel(item.day)}
                </text>
              );
            })}
          </svg>
        </div>
      )}
    </Card>
  );
}

function HorizontalBars({ title, subtitle, items, labelKey, valueKey, formatter }) {
  const max = Math.max(
    1,
    ...items.map((item) => Number(item[valueKey] || 0))
  );

  return (
    <Card>
      <h2 className="font-semibold text-white">{title}</h2>
      <p className="mt-1 text-xs text-gray-500">{subtitle}</p>

      <div className="mt-6 space-y-5">
        {items.length === 0 ? (
          <p className="py-8 text-center text-sm text-gray-500">
            No data available.
          </p>
        ) : (
          items.map((item, index) => (
            <div key={`${item[labelKey]}-${index}`}>
              <div className="mb-2 flex items-center justify-between gap-3">
                <span className="truncate text-sm text-gray-300">
                  {item[labelKey] || "Other"}
                </span>
                <span className="shrink-0 text-sm font-semibold text-white">
                  {formatter(item[valueKey])}
                </span>
              </div>

              <div className="h-2 overflow-hidden rounded-full bg-white/5">
                <div
                  className="h-full rounded-full bg-violet-500"
                  style={{
                    width: `${Math.max(
                      0,
                      (Number(item[valueKey] || 0) / max) * 100
                    )}%`,
                  }}
                />
              </div>
            </div>
          ))
        )}
      </div>
    </Card>
  );
}

const shortcuts = [
  { label: "7 Days", days: 7 },
  { label: "30 Days", days: 30 },
  { label: "90 Days", days: 90 },
];

export default function AdminDashboardPage() {
  const navigate = useNavigate();

  const [range, setRange] = useState(initialRange);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  const loadAnalytics = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await api.get("/admin/analytics", {
        params: range,
      });

      setAnalytics(response.data);
    } catch (err) {
      console.error("Analytics loading failed:", err);

      setError(
        err.response?.data?.detail ||
        err.response?.data?.message ||
        "Unable to load analytics. Check your admin access and backend logs."
      );
    } finally {
      setLoading(false);
    }
  }, [range]);

useEffect(() => {
  const timer = setTimeout(() => {
    loadAnalytics();
  }, 0);

  return () => clearTimeout(timer);
}, [loadAnalytics, refreshKey]);

const summary = useMemo(
  () => analytics?.summary ?? {},
  [analytics?.summary]
);

  const metricCards = useMemo(() => [
    {
      title: "Total Orders",
      value: number(summary.totalOrders),
      description: "Orders in selected period",
      icon: ShoppingBag,
      accent: "bg-violet-500/15 text-violet-300",
    },
    {
      title: "Gross Sales",
      value: money(summary.grossSales),
      description: "Excluding cancelled orders",
      icon: IndianRupee,
      accent: "bg-emerald-500/15 text-emerald-300",
    },
    {
      title: "Collected Revenue",
      value: money(summary.collectedRevenue),
      description: "Successfully paid orders",
      icon: TrendingUp,
      accent: "bg-blue-500/15 text-blue-300",
    },
    {
      title: "Average Order Value",
      value: money(summary.averageOrderValue),
      description: "Per non-cancelled order",
      icon: BarChart3,
      accent: "bg-amber-500/15 text-amber-300",
    },
    {
      title: "Customers",
      value: number(summary.totalCustomers),
      description: "All registered customers",
      icon: Users,
      accent: "bg-pink-500/15 text-pink-300",
    },
    {
      title: "New Customers",
      value: number(summary.newCustomers),
      description: "Registered in selected period",
      icon: UserIcon,
      accent: "bg-cyan-500/15 text-cyan-300",
    },
    {
      title: "Products",
      value: number(summary.totalProducts),
      description: "Active catalog products",
      icon: Package,
      accent: "bg-indigo-500/15 text-indigo-300",
    },
    {
      title: "Low Stock",
      value: number(summary.lowStockCount),
      description: "Active products with 5 or fewer units",
      icon: Warehouse,
      accent: "bg-red-500/15 text-red-300",
    },
  ], [summary]);

  async function exportCsv() {
    setExporting(true);

    try {
      const response = await api.get("/admin/analytics/export", {
        params: range,
        responseType: "blob",
      });

      const url = window.URL.createObjectURL(
        new Blob([response.data], { type: "text/csv" })
      );

      const link = document.createElement("a");
      link.href = url;
      link.download = "astra-orders-analytics.csv";
      document.body.appendChild(link);
      link.click();
      link.remove();

      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error("CSV export failed:", err);
      setError("Unable to export analytics CSV.");
    } finally {
      setExporting(false);
    }
  }

  function selectShortcut(days) {
    setRange({
      startDate: daysAgo(days - 1),
      endDate: today(),
    });
  }

  function refresh() {
    setRefreshKey((value) => value + 1);
  }

  const statusItems = (analytics?.orderStatuses || []).map((item) => ({
    ...item,
    label: item.status,
  }));

  const paymentItems = (analytics?.paymentMethods || []).map((item) => ({
    ...item,
    label: item.method,
  }));

  return (
    <div className="min-h-screen bg-[#09090f] px-4 py-7 text-white sm:px-7 lg:px-10">

      <header className="flex flex-col justify-between gap-5 border-b border-white/10 pb-7 lg:flex-row lg:items-center">
        <div>
          <p className="text-sm font-semibold tracking-widest text-violet-400">
            ASTRA BUSINESS INTELLIGENCE
          </p>
          <h1 className="mt-2 text-3xl font-bold sm:text-4xl">
            Analytics Dashboard
          </h1>
          <p className="mt-2 text-sm text-gray-400">
            Monitor sales, customers, orders and inventory performance.
          </p>
        </div>

        <div className="flex flex-wrap gap-3">
          <button
            onClick={refresh}
            disabled={loading}
            className="flex items-center gap-2 rounded-xl border border-white/10 px-4 py-3 text-sm hover:bg-white/5 disabled:opacity-50"
          >
            <RefreshCw size={16} />
            Refresh
          </button>

          <button
            onClick={exportCsv}
            disabled={exporting}
            className="flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold hover:bg-violet-500 disabled:opacity-50"
          >
            <ArrowDownToLine size={16} />
            {exporting ? "Exporting..." : "Export CSV"}
          </button>
        </div>
      </header>

      <Card className="mt-7">
        <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
          <div>
            <div className="flex items-center gap-2 text-sm font-semibold">
              <CalendarDays size={17} className="text-violet-400" />
              Analytics date range
            </div>

            <div className="mt-3 flex flex-wrap gap-2">
              {shortcuts.map((shortcut) => (
                <button
                  key={shortcut.days}
                  onClick={() => selectShortcut(shortcut.days)}
                  className="rounded-lg border border-white/10 px-3 py-2 text-xs text-gray-300 hover:border-violet-400/50 hover:text-white"
                >
                  {shortcut.label}
                </button>
              ))}
            </div>
          </div>

          <div className="flex flex-wrap items-end gap-3">
            <label className="text-xs text-gray-400">
              Start date
              <input
                type="date"
                value={range.startDate}
                max={range.endDate}
                onChange={(e) =>
                  setRange((old) => ({
                    ...old,
                    startDate: e.target.value,
                  }))
                }
                className="mt-2 block rounded-xl border border-white/10 bg-[#09090f] px-3 py-2 text-sm text-white"
              />
            </label>

            <label className="text-xs text-gray-400">
              End date
              <input
                type="date"
                value={range.endDate}
                min={range.startDate}
                max={today()}
                onChange={(e) =>
                  setRange((old) => ({
                    ...old,
                    endDate: e.target.value,
                  }))
                }
                className="mt-2 block rounded-xl border border-white/10 bg-[#09090f] px-3 py-2 text-sm text-white"
              />
            </label>
          </div>
        </div>
      </Card>

      {error && (
        <div
          role="alert"
          className="mt-5 rounded-xl border border-red-400/20 bg-red-500/10 p-4 text-sm text-red-300"
        >
          {error}
        </div>
      )}

      {loading && !analytics ? (
        <div className="py-24 text-center text-gray-400">
          Loading Astra analytics...
        </div>
      ) : analytics ? (
        <>
          <section className="mt-7">
            <div className="mb-4 flex items-center justify-between">
              <div>
                <h2 className="text-lg font-bold">Business Overview</h2>
                <p className="mt-1 text-xs text-gray-500">
                  {range.startDate} to {range.endDate}
                </p>
              </div>

              <Activity className="text-violet-400" />
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
              {metricCards.map((card) => (
                <MetricCard key={card.title} {...card} />
              ))}
            </div>
          </section>

          <section className="mt-7 grid grid-cols-1 gap-5 xl:grid-cols-3">
            <div className="xl:col-span-2">
              <SalesChart data={analytics.salesTrend || []} />
            </div>

            <div className="space-y-5">
              <Card>
                <h2 className="font-semibold">Order Summary</h2>

                <div className="mt-5 space-y-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-400">Total orders</span>
                    <strong>{number(summary.totalOrders)}</strong>
                  </div>

                  <div className="flex justify-between text-sm">
                    <span className="text-gray-400">Cancelled</span>
                    <strong className="text-red-300">
                      {number(summary.cancelledOrders)}
                    </strong>
                  </div>

                  <div className="flex justify-between text-sm">
                    <span className="text-gray-400">Purchasing customers</span>
                    <strong>{number(summary.purchasingCustomers)}</strong>
                  </div>

                  <div className="flex justify-between text-sm">
                    <span className="text-gray-400">New customers</span>
                    <strong className="text-emerald-300">
                      {number(summary.newCustomers)}
                    </strong>
                  </div>
                </div>
              </Card>

              <Card>
                <h2 className="font-semibold">Inventory Alert</h2>
                <p className="mt-1 text-xs text-gray-500">
                  Products that need restocking
                </p>

                <div className="mt-4 space-y-3">
                  {(analytics.lowStockProducts || []).length === 0 ? (
                    <p className="py-4 text-sm text-emerald-300">
                      All products have sufficient stock.
                    </p>
                  ) : (
                    analytics.lowStockProducts.map((product) => (
                      <div
                        key={product.id}
                        className="flex items-center justify-between gap-3"
                      >
                        <div className="min-w-0">
                          <p className="truncate text-sm text-gray-200">
                            {product.name}
                          </p>
                          <p className="text-xs text-gray-500">
                            {product.sku || "No SKU"}
                          </p>
                        </div>

                        <span className="shrink-0 rounded-lg bg-red-500/10 px-3 py-1 text-xs font-bold text-red-300">
                          {product.stockQuantity} left
                        </span>
                      </div>
                    ))
                  )}
                </div>

                <button
                  onClick={() => navigate("/admin/inventory")}
                  className="mt-5 w-full rounded-xl border border-white/10 py-2.5 text-sm hover:bg-white/5"
                >
                  Manage inventory
                </button>
              </Card>
            </div>
          </section>

          <section className="mt-5 grid grid-cols-1 gap-5 lg:grid-cols-2">
            <HorizontalBars
              title="Payment Methods"
              subtitle="Successfully collected revenue by payment method"
              items={paymentItems}
              labelKey="label"
              valueKey="collectedRevenue"
              formatter={money}
            />

            <HorizontalBars
              title="Order Status"
              subtitle="Orders grouped by current status"
              items={statusItems}
              labelKey="label"
              valueKey="orderCount"
              formatter={number}
            />
          </section>

          <section className="mt-5 grid grid-cols-1 gap-5 lg:grid-cols-2">
            <HorizontalBars
              title="Top Selling Products"
              subtitle="Products ranked by units sold"
              items={analytics.topProducts || []}
              labelKey="productName"
              valueKey="unitsSold"
              formatter={number}
            />

            <HorizontalBars
              title="Top Categories"
              subtitle="Category performance by sales amount"
              items={analytics.topCategories || []}
              labelKey="category"
              valueKey="salesAmount"
              formatter={money}
            />
          </section>

          <section className="mt-7">
            <Card>
              <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 className="font-semibold">Recent Orders</h2>
                  <p className="mt-1 text-xs text-gray-500">
                    Latest orders within the selected period
                  </p>
                </div>

                <button
                  onClick={() => navigate("/admin/orders")}
                  className="text-sm font-semibold text-violet-300 hover:text-violet-200"
                >
                  View all orders →
                </button>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full min-w-[720px] text-left text-sm">
                  <thead className="border-b border-white/10 text-xs text-gray-500">
                    <tr>
                      <th className="px-3 py-3">Order</th>
                      <th className="px-3 py-3">Customer</th>
                      <th className="px-3 py-3">Amount</th>
                      <th className="px-3 py-3">Payment</th>
                      <th className="px-3 py-3">Status</th>
                      <th className="px-3 py-3">Date</th>
                    </tr>
                  </thead>

                  <tbody>
                    {(analytics.recentOrders || []).map((order) => (
                      <tr
                        key={order.orderNumber}
                        onClick={() =>
                          navigate(`/admin/orders/${order.orderNumber}`)
                        }
                        className="cursor-pointer border-b border-white/5 hover:bg-white/[0.03]"
                      >
                        <td className="px-3 py-4 font-semibold text-violet-300">
                          {order.orderNumber}
                        </td>
                        <td className="px-3 py-4 text-gray-300">
                          {order.customerName}
                        </td>
                        <td className="px-3 py-4 font-semibold">
                          {money(order.totalAmount)}
                        </td>
                        <td className="px-3 py-4 text-gray-400">
                          {order.paymentStatus}
                        </td>
                        <td className="px-3 py-4 text-gray-300">
                          {order.orderStatus}
                        </td>
                        <td className="px-3 py-4 text-gray-400">
                          {order.createdAt
                            ? new Date(order.createdAt).toLocaleDateString("en-IN")
                            : "—"}
                        </td>
                      </tr>
                    ))}

                    {(analytics.recentOrders || []).length === 0 && (
                      <tr>
                        <td
                          colSpan="6"
                          className="py-10 text-center text-gray-500"
                        >
                          No orders found for this period.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </Card>
          </section>

        </>
      ) : (
        <div className="py-20 text-center text-gray-400">
          Analytics are not available.
        </div>
      )}

      <footer className="mt-10 border-t border-white/10 pt-5 text-xs text-gray-500">
        ASTRA Business Intelligence · Admin Dashboard
      </footer>
    </div>
  );
}

function UserIcon({ size = 20 }) {
  return <Users size={size} />;
}

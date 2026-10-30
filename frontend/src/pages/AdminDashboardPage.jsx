
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

const adminSections = [
  {
    title: "Order Management",
    description:
      "View customer orders, inspect order details, and update order status.",
    icon: "📦",
    path: "/admin/orders",
    action: "Manage Orders",
  },
  {
    title: "Product Management",
    description:
      "Manage the product catalog, product information, prices, and availability.",
    icon: "🛍️",
    path: "/admin/products",
    action: "Manage Products",
  },
  {
    title: "Inventory Management",
    description:
      "Monitor stock levels and identify products that need replenishment.",
    icon: "📊",
    path: "/admin/inventory",
    action: "Manage Inventory",
  },
  {
    title: "Coupon Management",
    description:
      "Create discount coupons and manage their active status.",
    icon: "🎟️",
    path: "/admin/coupons",
    action: "Manage Coupons",
  },
  {
    title: "Customer Management",
    description: "Search customer accounts and manage account access.",
    icon: "👤",
    path: "/admin/customers",
    action: "Manage Customers",
  },
  {
    title: "Admin Registration",
    description:
      "Register additional administrators through the authorized admin workflow.",
    icon: "👥",
    path: "/admin/register",
    action: "Register Admin",
  },
];

const summaryCards = [
  {
    label: "Total Orders",
    description: "Order statistics",
    icon: "📦",
    statKey: "orders",
  },
  {
    label: "Total Products",
    description: "Catalog statistics",
    icon: "🛍️",
    statKey: "products",
  },
  {
    label: "Active Coupons",
    description: "Discount statistics",
    icon: "🎟️",
    statKey: "coupons",
  },
  {
    label: "Low Stock",
    description: "Inventory statistics",
    icon: "⚠️",
    statKey: "lowStock",
  },
  {
    label: "Customers",
    description: "Customer accounts",
    icon: "👤",
    statKey: "customers",
  },
];

const initialStats = {
  orders: null,
  products: null,
  coupons: null,
  lowStock: null,
  customers: null,
};

export default function AdminDashboardPage() {
  const navigate = useNavigate();

  const [stats, setStats] = useState(initialStats);
  const [statsLoading, setStatsLoading] = useState(true);
  const [statsError, setStatsError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function loadDashboardStats() {
      try {
        const results = await Promise.allSettled([
          api.get("/admin/orders/summary"),
          api.get("/products"),
          api.get("/admin/coupons"),
          api.get("/admin/customers", { params: { page: 0, size: 1 } }),
        ]);

        if (cancelled) return;

        const nextStats = { ...initialStats };
        const errors = [];

        // Count orders without loading the full order list and its items.
        if (results[0].status === "fulfilled") {
          const data = results[0].value.data;
          if (Number.isFinite(Number(data?.totalOrders))) {
            nextStats.orders = Number(data.totalOrders);
          } else {
            errors.push("Orders response format");
          }
        } else {
          errors.push("Orders API");
        }

        // Products and low-stock count.
        if (results[1].status === "fulfilled") {
          const products = results[1].value.data;

          if (Array.isArray(products)) {
            nextStats.products = products.length;
            nextStats.lowStock = products.filter(
              (product) => Number(product.stockQuantity ?? 0) <= 5
            ).length;
          } else {
            errors.push("Products response format");
          }
        } else {
          errors.push("Products API");
        }

        // Active coupons.
        if (results[2].status === "fulfilled") {
          const data = results[2].value.data;
          const coupons = Array.isArray(data) ? data : data?.content;

          if (Array.isArray(coupons)) {
            nextStats.coupons = coupons.filter(
              (coupon) =>
                coupon.active === true || coupon.active === "true"
            ).length;
          } else {
            errors.push("Coupons response format");
          }
        } else {
          errors.push("Coupons API");
        }

        if (results[3].status === "fulfilled") {
          nextStats.customers = results[3].value.data?.totalElements ?? null;
        } else {
          errors.push("Customers API");
        }

        setStats(nextStats);
        setStatsError(
          errors.length
            ? `Unable to load: ${errors.join(", ")}. Check the browser Network tab.`
            : ""
        );
      } catch {
        if (!cancelled) {
          setStatsError("Unable to load dashboard statistics.");
        }
      } finally {
        if (!cancelled) {
          setStatsLoading(false);
        }
      }
    }

    void loadDashboardStats();

    return () => {
      cancelled = true;
    };
  }, [refreshKey]);

  function refreshStats() {
    setStatsLoading(true);
    setStatsError("");
    setRefreshKey((key) => key + 1);
  }

  return (
    <div className="min-h-screen bg-[#09090f] px-5 py-8 text-white sm:px-8 lg:px-10">
      <header className="flex flex-col justify-between gap-4 border-b border-white/10 pb-7 sm:flex-row sm:items-center">
        <div>
          <p className="text-sm font-medium text-violet-400">
            STORE ADMINISTRATION
          </p>
          <h1 className="mt-2 text-3xl font-bold sm:text-4xl">
            Dashboard
          </h1>
          <p className="mt-2 text-sm text-gray-400">
            Manage ASTRA E-Commerce operations.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={refreshStats}
            disabled={statsLoading}
            className="rounded-xl border border-white/10 px-4 py-3 text-sm font-semibold transition hover:bg-white/5 disabled:opacity-60"
          >
            {statsLoading ? "Loading..." : "↻ Refresh"}
          </button>

          <button
            type="button"
            onClick={() => navigate("/")}
            className="rounded-xl border border-white/10 px-4 py-3 text-sm font-semibold transition hover:bg-white/5"
          >
            View Store →
          </button>
        </div>
      </header>

      <section className="mt-8">
        <div className="mb-4">
          <h2 className="text-lg font-semibold">Store Overview</h2>
          <p className="mt-1 text-sm text-gray-500">
            Live statistics from your backend APIs.
          </p>
        </div>

        {statsError && (
          <p
            role="alert"
            className="mb-4 rounded-xl border border-amber-400/20 bg-amber-400/10 p-3 text-sm text-amber-200"
          >
            {statsError}
          </p>
        )}

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {summaryCards.map((card) => (
            <div
              key={card.label}
              className="rounded-2xl border border-white/10 bg-white/[0.03] p-5"
            >
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-400">{card.label}</span>
                <span className="text-xl">{card.icon}</span>
              </div>

              <p className="mt-5 text-2xl font-bold text-gray-300">
                {statsLoading
                  ? "..."
                  : stats[card.statKey] === null
                    ? "—"
                    : stats[card.statKey]}
              </p>

              <p className="mt-1 text-xs text-gray-500">
                {card.description}
              </p>
            </div>
          ))}
        </div>
      </section>

      <section className="mt-10">
        <div className="mb-5">
          <h2 className="text-xl font-bold">Manage Your Store</h2>
          <p className="mt-1 text-sm text-gray-400">
            Choose a module to continue.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-3">
          {adminSections.map((section) => (
            <article
              key={section.path}
              className="flex flex-col rounded-2xl border border-white/10 bg-white/[0.03] p-6 transition hover:border-violet-400/40 hover:bg-white/[0.05]"
            >
              <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-violet-400/20 bg-violet-500/10 text-2xl">
                {section.icon}
              </div>

              <h3 className="mt-5 text-lg font-bold">{section.title}</h3>

              <p className="mt-3 flex-1 text-sm leading-6 text-gray-400">
                {section.description}
              </p>

              <button
                type="button"
                onClick={() => navigate(section.path)}
                className="mt-6 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold transition hover:bg-violet-500"
              >
                {section.action} →
              </button>
            </article>
          ))}
        </div>
      </section>

      <footer className="mt-12 border-t border-white/10 pt-5 text-xs text-gray-500">
        ASTRA E-Commerce · Administration
      </footer>
    </div>
  );
}

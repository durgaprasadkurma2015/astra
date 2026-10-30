
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Package, RefreshCw, Save, Pencil, X } from "lucide-react";
import productService from "../services/productService";

const inputClass =
  "w-full rounded-xl border border-white/10 bg-[#09090f] px-4 py-3 text-sm text-white outline-none focus:border-violet-500";

function getErrorMessage(error) {
  if (error?.response?.status === 401) {
    return "Session expired. Please log in again.";
  }

  if (error?.response?.status === 403) {
    return "Admin access is required to update inventory.";
  }

  return (
    error?.response?.data?.message ||
    error?.response?.data?.detail ||
    "Unable to complete the inventory operation."
  );
}

function isActive(product) {
  return product.active === true || product.active === "true";
}

export default function AdminInventoryPage() {
  const navigate = useNavigate();

  const [products, setProducts] = useState([]);
  const [search, setSearch] = useState("");
  const [stockFilter, setStockFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  const [editingProduct, setEditingProduct] = useState(null);
  const [stockInput, setStockInput] = useState("");
  const [stockReason, setStockReason] = useState("");
  const [movements, setMovements] = useState([]);
  const [movementsLoading, setMovementsLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function loadInventory() {
      setLoading(true);
      setError("");

      try {
        // Includes active and inactive products.
        const data = await productService.getAdminProducts();

        if (!cancelled) {
          setProducts(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        if (!cancelled) setError(getErrorMessage(err));
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    void loadInventory();

    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  const inventory = useMemo(() => {
    const query = search.trim().toLowerCase();

    return products.filter((product) => {
      const stock = Number(product.stockQuantity || 0);

      const matchesSearch =
        !query ||
        String(product.name || "").toLowerCase().includes(query) ||
        String(product.sku || "").toLowerCase().includes(query);

      const matchesFilter =
        stockFilter === "ALL" ||
        (stockFilter === "OUT" && stock === 0) ||
        (stockFilter === "LOW" && stock > 0 && stock <= 5) ||
        (stockFilter === "AVAILABLE" && stock > 5);

      return matchesSearch && matchesFilter;
    });
  }, [products, search, stockFilter]);

  const outOfStock = products.filter(
    (product) => Number(product.stockQuantity || 0) === 0
  ).length;

  const lowStock = products.filter((product) => {
    const stock = Number(product.stockQuantity || 0);
    return stock > 0 && stock <= 5;
  }).length;

  const totalUnits = products.reduce(
    (sum, product) => sum + Number(product.stockQuantity || 0),
    0
  );

  async function openStockEditor(product) {
    setEditingProduct(product);
    setStockInput(String(product.stockQuantity ?? 0));
    setStockReason("");
    setMovements([]);
    setError("");
    setMessage("");
    setMovementsLoading(true);
    try {
      const page = await productService.getStockMovements(product.id);
      setMovements(page.content || []);
    } catch {
      setMovements([]);
    } finally {
      setMovementsLoading(false);
    }
  }

  async function saveStock(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (
      stockInput.trim() === "" ||
      !Number.isSafeInteger(Number(stockInput)) ||
      Number(stockInput) < 0
    ) {
      setError("Enter a whole number greater than or equal to zero.");
      return;
    }

    if (!editingProduct) return;
    if (!stockReason.trim()) {
      setError("Enter a reason for this stock adjustment.");
      return;
    }

    setSaving(true);

    try {
      const newStock = Number(stockInput);

      const updatedProduct = await productService.updateStock(
        editingProduct.id,
        newStock,
        stockReason.trim()
      );

      setProducts((current) =>
        current.map((product) =>
          product.id === editingProduct.id
            ? {
                ...product,
                ...(updatedProduct && typeof updatedProduct === "object"
                  ? updatedProduct
                  : {}),
                stockQuantity: newStock,
              }
            : product
        )
      );

      setMessage(
        `Stock for "${editingProduct.name}" updated to ${newStock}.`
      );
      setEditingProduct(null);
      setStockInput("");
      setStockReason("");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  function refreshInventory() {
    setError("");
    setMessage("");
    setLoading(true);
    setReloadKey((key) => key + 1);
  }

  return (
    <div className="min-h-screen bg-[#09090f] px-4 py-6 text-white sm:px-8 lg:px-10">
      <header className="flex flex-col justify-between gap-4 border-b border-white/10 pb-7 sm:flex-row sm:items-center">
        <div>
          <button
            type="button"
            onClick={() => navigate("/admin")}
            className="mb-3 text-sm text-gray-500 hover:text-violet-300"
          >
            ← Back to dashboard
          </button>

          <p className="text-xs uppercase tracking-[0.2em] text-violet-400">
            Stock control
          </p>

          <h1 className="mt-2 text-3xl font-bold">Inventory</h1>

          <p className="mt-2 text-sm text-gray-400">
            Monitor stock and update quantities without changing product details.
          </p>
        </div>

        <button
          type="button"
          onClick={refreshInventory}
          disabled={loading}
          className="inline-flex items-center gap-2 self-start rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold hover:bg-violet-500 disabled:opacity-60"
        >
          <RefreshCw
            size={16}
            className={loading ? "animate-spin" : ""}
          />
          {loading ? "Loading..." : "Refresh Inventory"}
        </button>
      </header>

      {message && (
        <div
          role="status"
          className="mt-6 rounded-xl border border-emerald-400/20 bg-emerald-500/10 p-4 text-sm text-emerald-300"
        >
          {message}
        </div>
      )}

      {error && (
        <div
          role="alert"
          className="mt-6 rounded-xl border border-red-400/20 bg-red-500/10 p-4 text-sm text-red-300"
        >
          {error}
        </div>
      )}

      <section className="mt-7 grid grid-cols-1 gap-4 sm:grid-cols-3">
        {[
          ["Total Products", products.length, "Active and inactive products"],
          ["Low Stock", lowStock, "Between 1 and 5 units"],
          ["Out of Stock", outOfStock, "Products with zero units"],
        ].map(([label, value, description]) => (
          <div
            key={label}
            className="rounded-2xl border border-white/10 bg-[#11111b] p-5"
          >
            <p className="text-sm text-gray-400">{label}</p>
            <p className="mt-4 text-3xl font-bold">{value}</p>
            <p className="mt-2 text-xs text-gray-500">{description}</p>
          </div>
        ))}
      </section>

      <section className="mt-4 rounded-2xl border border-white/10 bg-[#11111b] p-5">
        <p className="text-sm text-gray-400">Total units in stock</p>
        <p className="mt-2 text-3xl font-bold">{totalUnits}</p>
        <p className="mt-2 text-xs text-gray-500">
          Calculated from product stock quantities.
        </p>
      </section>

      {editingProduct && (
        <section className="mt-6 rounded-2xl border border-violet-400/20 bg-[#11111b] p-5 sm:p-6">
          <div className="mb-5 flex items-start justify-between gap-3">
            <div>
              <h2 className="text-lg font-bold">Update Stock</h2>
              <p className="mt-1 text-sm text-gray-400">
                {editingProduct.name} · SKU: {editingProduct.sku || "—"}
              </p>
              <p className="mt-1 text-xs text-gray-500">
                Current stock: {editingProduct.stockQuantity}
              </p>
            </div>

            <button
              type="button"
              disabled={saving}
              onClick={() => setEditingProduct(null)}
              className="rounded-lg p-2 text-gray-400 hover:bg-white/5 hover:text-white"
              aria-label="Close stock editor"
            >
              <X size={20} />
            </button>
          </div>

          <form
            onSubmit={saveStock}
            className="flex flex-col gap-3 sm:flex-row sm:items-end"
          >
            <label className="block flex-1 text-sm text-gray-300">
              New stock quantity
              <input
                className={`${inputClass} mt-2`}
                type="number"
                min="0"
                step="1"
                value={stockInput}
                onChange={(event) => setStockInput(event.target.value)}
                required
              />
            </label>

            <label className="block flex-1 text-sm text-gray-300">
              Adjustment reason
              <input
                className={`${inputClass} mt-2`}
                type="text"
                maxLength={255}
                value={stockReason}
                onChange={(event) => setStockReason(event.target.value)}
                placeholder="e.g. Received supplier shipment"
                required
              />
            </label>

            <button
              type="submit"
              disabled={saving}
              className="inline-flex items-center justify-center gap-2 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold hover:bg-violet-500 disabled:opacity-60"
            >
              <Save size={16} />
              {saving ? "Saving..." : "Save Stock"}
            </button>

            <button
              type="button"
              disabled={saving}
              onClick={() => setEditingProduct(null)}
              className="rounded-xl border border-white/10 px-5 py-3 text-sm text-gray-300 hover:bg-white/5"
            >
              Cancel
            </button>
          </form>
          <div className="mt-6 border-t border-white/10 pt-5">
            <h3 className="text-sm font-semibold text-gray-200">Recent stock movements</h3>
            {movementsLoading ? (
              <p className="mt-3 text-sm text-gray-500">Loading history...</p>
            ) : movements.length === 0 ? (
              <p className="mt-3 text-sm text-gray-500">No recorded adjustments yet.</p>
            ) : (
              <ul className="mt-3 divide-y divide-white/5">
                {movements.map((movement) => (
                  <li key={movement.id} className="flex flex-wrap justify-between gap-2 py-3 text-sm">
                    <span className="font-semibold">{movement.quantityDelta > 0 ? "+" : ""}{movement.quantityDelta} units</span>
                    <span className="text-gray-400">{movement.reason}</span>
                    <span className="text-xs text-gray-500">{movement.actorEmail} · {movement.createdAt ? new Date(movement.createdAt).toLocaleString() : ""}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      )}

      <section className="mt-8 overflow-hidden rounded-2xl border border-white/10 bg-[#11111b]">
        <div className="flex flex-col gap-4 border-b border-white/10 p-5 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-lg font-bold">Stock Overview</h2>
            <p className="mt-1 text-sm text-gray-500">
              {inventory.length} products shown
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search product or SKU..."
              className="w-full rounded-xl border border-white/10 bg-[#09090f] px-4 py-3 text-sm outline-none focus:border-violet-500 sm:w-64"
            />

            <select
              value={stockFilter}
              onChange={(event) => setStockFilter(event.target.value)}
              className="rounded-xl border border-white/10 bg-[#151522] px-3 py-2.5 text-sm text-gray-200 outline-none focus:border-violet-500"
            >
              <option value="ALL">All stock levels</option>
              <option value="AVAILABLE">Available (&gt; 5)</option>
              <option value="LOW">Low stock (1–5)</option>
              <option value="OUT">Out of stock</option>
            </select>
          </div>
        </div>

        {loading ? (
          <p className="p-12 text-center text-gray-400">
            Loading inventory...
          </p>
        ) : error && products.length === 0 ? (
          <p className="p-12 text-center text-gray-400">
            Inventory could not be loaded.
          </p>
        ) : inventory.length === 0 ? (
          <div className="p-12 text-center">
            <Package className="mx-auto mb-3 text-gray-500" size={30} />
            <p className="font-semibold">No matching inventory</p>
            <p className="mt-2 text-sm text-gray-500">
              Try changing your search or stock filter.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[900px] text-left">
              <thead className="bg-white/[0.025] text-xs uppercase tracking-wider text-gray-500">
                <tr>
                  {[
                    "Product",
                    "SKU",
                    "Category",
                    "Quantity",
                    "Stock Status",
                    "Action",
                  ].map((heading) => (
                    <th
                      key={heading}
                      className="border-b border-white/10 px-5 py-4"
                    >
                      {heading}
                    </th>
                  ))}
                </tr>
              </thead>

              <tbody className="divide-y divide-white/[0.06]">
                {inventory.map((product) => {
                  const stock = Number(product.stockQuantity || 0);

                  const label =
                    stock === 0
                      ? "Out of stock"
                      : stock <= 5
                        ? "Low stock"
                        : "In stock";

                  const badgeClass =
                    stock === 0
                      ? "border-red-400/20 bg-red-400/10 text-red-300"
                      : stock <= 5
                        ? "border-amber-400/20 bg-amber-400/10 text-amber-300"
                        : "border-emerald-400/20 bg-emerald-400/10 text-emerald-300";

                  return (
                    <tr
                      key={product.id}
                      className="hover:bg-white/[0.025]"
                    >
                      <td className="px-5 py-5">
                        <p className="font-semibold">{product.name}</p>
                        <p className="mt-1 text-xs text-gray-500">
                          ID: {product.id}
                          {!isActive(product) ? " · Inactive product" : ""}
                        </p>
                      </td>

                      <td className="px-5 py-5 text-sm text-gray-400">
                        {product.sku || "—"}
                      </td>

                      <td className="px-5 py-5 text-sm text-gray-300">
                        {product.category || "Uncategorized"}
                      </td>

                      <td className="px-5 py-5">
                        <span className="text-lg font-semibold">
                          {stock}
                        </span>
                        <div className="mt-2 h-1.5 w-28 overflow-hidden rounded-full bg-white/10">
                          <div
                            className={`h-full rounded-full ${
                              stock === 0
                                ? "bg-red-400"
                                : stock <= 5
                                  ? "bg-amber-400"
                                  : "bg-emerald-400"
                            }`}
                            style={{
                              width: `${Math.min(stock, 100)}%`,
                            }}
                          />
                        </div>
                      </td>

                      <td className="px-5 py-5">
                        <span
                          className={`whitespace-nowrap rounded-full border px-3 py-1.5 text-xs font-semibold ${badgeClass}`}
                        >
                          {label}
                        </span>
                      </td>

                      <td className="px-5 py-5">
                        <button
                          type="button"
                          onClick={() => openStockEditor(product)}
                          disabled={saving}
                          className="inline-flex items-center gap-2 rounded-lg border border-violet-400/20 px-3 py-2 text-xs font-semibold text-violet-300 hover:bg-violet-500/10 disabled:opacity-50"
                        >
                          <Pencil size={14} />
                          Update Stock
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

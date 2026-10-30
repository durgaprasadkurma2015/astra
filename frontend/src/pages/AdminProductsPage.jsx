
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Plus,
  Pencil,
  RefreshCw,
  CheckCircle,
  XCircle,
  X,
  Package,
} from "lucide-react";
import productService from "../services/productService";

const currency = (value) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
  }).format(Number(value || 0));

const emptyForm = {
  name: "",
  sku: "",
  description: "",
  category: "",
  subcategory: "",
  imageUrl: "",
  price: "",
  discountPercentage: "0",
  stockQuantity: "0",
  variants: [],
};

const inputClass =
  "w-full rounded-xl border border-white/10 bg-[#09090f] px-4 py-3 text-sm text-white outline-none focus:border-violet-500";

const selectClass =
  "rounded-xl border border-white/10 bg-[#151522] px-3 py-2.5 text-sm text-gray-200 outline-none focus:border-violet-500";

function getErrorMessage(error, fallback) {
  const data = error?.response?.data;

  if (typeof data?.detail === "string") return data.detail;
  if (typeof data?.message === "string") return data.message;
  if (typeof data === "string") return data;

  if (error?.response?.status === 401) {
    return "Your session has expired. Please log in again.";
  }

  if (error?.response?.status === 403) {
    return "Admin access is required for this operation.";
  }

  if (error?.response?.status === 409) {
    return "This SKU already exists. Please use a unique SKU.";
  }

  if (error?.response?.status === 400) {
    return "Invalid product data. Check all required fields and values.";
  }

  return fallback;
}

function isActive(product) {
  return product.active === true || product.active === "true";
}

export default function AdminProductsPage() {
  const navigate = useNavigate();

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  const [showForm, setShowForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [updatingId, setUpdatingId] = useState(null);
  const [formError, setFormError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadProducts() {
      setLoading(true);
      setError("");

      try {
        // Admin endpoint includes active and inactive products.
        const [productData, categoryResult] = await Promise.all([
          productService.getAdminProducts(),
          productService.getCategories().catch(() => []),
        ]);

        if (cancelled) return;

        const allProducts = Array.isArray(productData) ? productData : [];

        setProducts(allProducts);

        const apiCategories = Array.isArray(categoryResult)
          ? categoryResult
          : [];

        const productCategories = allProducts
          .map((product) => product.category)
          .filter(Boolean);

        setCategories(
          [...new Set([...apiCategories, ...productCategories])].sort()
        );
      } catch (err) {
        if (!cancelled) {
          setError(
            getErrorMessage(
              err,
              "Unable to load products. Check the backend connection."
            )
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    void loadProducts();

    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  const filteredProducts = useMemo(() => {
    const query = search.trim().toLowerCase();

    return products.filter((product) => {
      const matchesSearch = [
        product.name,
        product.sku,
        product.category,
        product.subcategory,
      ].some((value) =>
        String(value || "").toLowerCase().includes(query)
      );

      const matchesCategory =
        category === "ALL" || product.category === category;

      const matchesStatus =
        statusFilter === "ALL" ||
        (statusFilter === "ACTIVE" && isActive(product)) ||
        (statusFilter === "INACTIVE" && !isActive(product));

      return matchesSearch && matchesCategory && matchesStatus;
    });
  }, [products, search, category, statusFilter]);

  const totalStock = filteredProducts.reduce(
    (sum, product) => sum + Number(product.stockQuantity || 0),
    0
  );

  const activeCount = products.filter(isActive).length;
  const inactiveCount = products.length - activeCount;

  function refreshProducts() {
    setError("");
    setMessage("");
    setLoading(true);
    setReloadKey((key) => key + 1);
  }

  function openCreateForm() {
    setEditingProduct(null);
    setForm({
      ...emptyForm,
      variants: [],
    });
    setFormError("");
    setError("");
    setMessage("");
    setShowForm(true);
  }

  function openEditForm(product) {
    setEditingProduct(product);

    setForm({
      name: product.name || "",
      sku: product.sku || "",
      description: product.description || "",
      category: product.category || "",
      subcategory: product.subcategory || "",
      imageUrl: product.imageUrl || "",
      price: String(product.price ?? ""),
      discountPercentage: String(product.discountPercentage ?? 0),
      stockQuantity: String(product.stockQuantity ?? 0),
      variants: Array.isArray(product.variants)
        ? product.variants.map((variant) => ({
            sku: variant.sku || "",
            size: variant.size || "",
            color: variant.color || "",
            stockQuantity: String(variant.stockQuantity ?? 0),
          }))
        : [],
    });

    setFormError("");
    setError("");
    setMessage("");
    setShowForm(true);
  }

  function updateField(event) {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  }

  function updateVariant(index, field, value) {
    setForm((current) => ({
      ...current,
      variants: current.variants.map((variant, variantIndex) =>
        variantIndex === index
          ? { ...variant, [field]: value }
          : variant
      ),
    }));
  }

  function addVariant() {
    setForm((current) => ({
      ...current,
      variants: [
        ...current.variants,
        {
          sku: "",
          size: "",
          color: "",
          stockQuantity: "0",
        },
      ],
    }));
  }

  function removeVariant(index) {
    setForm((current) => ({
      ...current,
      variants: current.variants.filter(
        (_, variantIndex) => variantIndex !== index
      ),
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setFormError("");
    setError("");
    setMessage("");

    const price = Number(form.price);
    const discount = Number(form.discountPercentage);
    const stock = Number(form.stockQuantity);

    if (!form.name.trim()) {
      setFormError("Product name is required.");
      return;
    }

    if (form.price.trim() === "" || !Number.isFinite(price) || price < 0) {
      setFormError("Enter a valid price greater than or equal to zero.");
      return;
    }

    if (
      form.discountPercentage.trim() === "" ||
      !Number.isFinite(discount) ||
      discount < 0 ||
      discount > 100
    ) {
      setFormError("Discount must be between 0 and 100.");
      return;
    }

    if (
      form.stockQuantity.trim() === "" ||
      !Number.isInteger(stock) ||
      stock < 0
    ) {
      setFormError("Stock quantity must be a whole number of zero or more.");
      return;
    }

    for (const variant of form.variants) {
      const variantStock = Number(variant.stockQuantity);

      if (
        variant.stockQuantity === "" ||
        !Number.isInteger(variantStock) ||
        variantStock < 0
      ) {
        setFormError(
          "Each variant must have a valid non-negative stock quantity."
        );
        return;
      }
    }

    const payload = {
      name: form.name.trim(),
      sku: form.sku.trim() || null,
      description: form.description.trim() || null,
      category: form.category.trim() || null,
      subcategory: form.subcategory.trim() || null,
      imageUrl: form.imageUrl.trim() || null,
      price,
      discountPercentage: discount,
      stockQuantity: stock,
      variants: form.variants.map((variant) => ({
        sku: variant.sku.trim() || null,
        size: variant.size.trim() || null,
        color: variant.color.trim() || null,
        stockQuantity: Number(variant.stockQuantity),
      })),
    };

    setSaving(true);

    try {
      if (editingProduct) {
        await productService.updateProduct(editingProduct.id, payload);
        setMessage(`Product "${payload.name}" updated successfully.`);
      } else {
        await productService.createProduct(payload);
        setMessage(`Product "${payload.name}" created successfully.`);
      }

      setShowForm(false);
      setEditingProduct(null);
      setForm(emptyForm);
      setReloadKey((key) => key + 1);
    } catch (err) {
      setFormError(
        getErrorMessage(
          err,
          editingProduct
            ? "Unable to update the product."
            : "Unable to create the product."
        )
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleToggleActive(product) {
    const nextActive = !isActive(product);

    const confirmed = window.confirm(
      `${nextActive ? "Activate" : "Deactivate"} "${product.name}"?`
    );

    if (!confirmed) return;

    setUpdatingId(product.id);
    setError("");
    setMessage("");

    try {
      const updatedProduct = await productService.updateProductStatus(
        product.id,
        nextActive
      );

      setProducts((current) =>
        current.map((item) =>
          item.id === product.id
            ? { ...item, ...updatedProduct }
            : item
        )
      );

      setMessage(
        `"${product.name}" ${nextActive ? "activated" : "deactivated"} successfully.`
      );
    } catch (err) {
      setError(
        getErrorMessage(
          err,
          `Unable to ${nextActive ? "activate" : "deactivate"} this product.`
        )
      );
    } finally {
      setUpdatingId(null);
    }
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
            Catalog administration
          </p>

          <h1 className="mt-2 text-3xl font-bold">Products</h1>

          <p className="mt-2 text-sm text-gray-400">
            Manage product information, pricing, stock and availability.
          </p>
        </div>

        <div className="flex flex-wrap gap-3 self-start">
          <button
            type="button"
            onClick={openCreateForm}
            className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold hover:bg-violet-500"
          >
            <Plus size={17} />
            Add Product
          </button>

          <button
            type="button"
            onClick={refreshProducts}
            disabled={loading}
            className="inline-flex items-center gap-2 rounded-xl border border-white/10 px-5 py-3 text-sm font-semibold hover:bg-white/5 disabled:opacity-60"
          >
            <RefreshCw
              size={16}
              className={loading ? "animate-spin" : ""}
            />
            {loading ? "Loading..." : "Refresh Products"}
          </button>
        </div>
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
          ["Active Products", activeCount, "Available in the store"],
          ["Units in Stock", totalStock, "Stock across filtered products"],
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

      {showForm && (
        <section className="mt-8 rounded-2xl border border-violet-400/20 bg-[#11111b] p-5 sm:p-7">
          <div className="mb-6 flex items-center justify-between gap-3">
            <div>
              <h2 className="text-xl font-bold">
                {editingProduct ? "Edit Product" : "Add Product"}
              </h2>
              <p className="mt-1 text-sm text-gray-500">
                Fields marked * are required.
              </p>
            </div>

            <button
              type="button"
              onClick={() => {
                setShowForm(false);
                setFormError("");
              }}
              className="rounded-lg p-2 text-gray-400 hover:bg-white/5 hover:text-white"
              aria-label="Close product form"
            >
              <X size={20} />
            </button>
          </div>

          {formError && (
            <div
              role="alert"
              className="mb-5 rounded-xl border border-red-400/20 bg-red-500/10 p-3 text-sm text-red-300"
            >
              {formError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <label className="space-y-2 text-sm text-gray-300">
                <span>Product Name *</span>
                <input
                  className={inputClass}
                  name="name"
                  value={form.name}
                  onChange={updateField}
                  maxLength={150}
                  required
                />
              </label>

              <label className="space-y-2 text-sm text-gray-300">
                <span>SKU</span>
                <input
                  className={inputClass}
                  name="sku"
                  value={form.sku}
                  onChange={updateField}
                  maxLength={100}
                />
              </label>

              <label className="space-y-2 text-sm text-gray-300">
                <span>Category</span>
                <input
                  className={inputClass}
                  name="category"
                  value={form.category}
                  onChange={updateField}
                  list="product-category-options"
                  maxLength={100}
                />
                <datalist id="product-category-options">
                  {categories.map((item) => (
                    <option key={item} value={item} />
                  ))}
                </datalist>
              </label>

              <label className="space-y-2 text-sm text-gray-300">
                <span>Subcategory</span>
                <input
                  className={inputClass}
                  name="subcategory"
                  value={form.subcategory}
                  onChange={updateField}
                  maxLength={100}
                />
              </label>

              <label className="space-y-2 text-sm text-gray-300">
                <span>Price (₹) *</span>
                <input
                  className={inputClass}
                  type="number"
                  name="price"
                  value={form.price}
                  onChange={updateField}
                  min="0"
                  step="0.01"
                  required
                />
              </label>

              <label className="space-y-2 text-sm text-gray-300">
                <span>Discount (%)</span>
                <input
                  className={inputClass}
                  type="number"
                  name="discountPercentage"
                  value={form.discountPercentage}
                  onChange={updateField}
                  min="0"
                  max="100"
                  step="0.01"
                  required
                />
              </label>

              <label className="space-y-2 text-sm text-gray-300">
                <span>Stock Quantity *</span>
                <input
                  className={inputClass}
                  type="number"
                  name="stockQuantity"
                  value={form.stockQuantity}
                  onChange={updateField}
                  disabled={Boolean(editingProduct)}
                  min="0"
                  step="1"
                  required
                />
                {editingProduct && (
                  <span className="text-xs text-gray-500">For an auditable stock change, use Inventory Management.</span>
                )}
              </label>

              <label className="space-y-2 text-sm text-gray-300 sm:col-span-2">
                <span>Image URL</span>
                <input
                  className={inputClass}
                  type="url"
                  name="imageUrl"
                  value={form.imageUrl}
                  onChange={updateField}
                  maxLength={1000}
                  placeholder="https://example.com/product.jpg"
                />
              </label>

              <label className="space-y-2 text-sm text-gray-300 sm:col-span-2 lg:col-span-3">
                <span>Description</span>
                <textarea
                  className={inputClass}
                  name="description"
                  value={form.description}
                  onChange={updateField}
                  maxLength={1000}
                  rows={3}
                />
              </label>
            </div>

            <div className="border-t border-white/10 pt-5">
              <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h3 className="font-semibold">Product Variants</h3>
                  <p className="mt-1 text-xs text-gray-500">
                    Optional sizes, colors, SKUs and variant stock.
                  </p>
                </div>

                <button
                  type="button"
                  onClick={addVariant}
                  className="rounded-lg border border-violet-400/30 px-3 py-2 text-sm text-violet-300 hover:bg-violet-500/10"
                >
                  + Add Variant
                </button>
              </div>

              {form.variants.length === 0 ? (
                <p className="text-sm text-gray-500">No variants added.</p>
              ) : (
                <div className="space-y-3">
                  {form.variants.map((variant, index) => (
                    <div
                      key={index}
                      className="grid grid-cols-1 gap-3 rounded-xl border border-white/10 p-3 sm:grid-cols-2 lg:grid-cols-5"
                    >
                      <input
                        className={inputClass}
                        placeholder="Variant SKU"
                        value={variant.sku}
                        onChange={(event) =>
                          updateVariant(index, "sku", event.target.value)
                        }
                      />

                      <input
                        className={inputClass}
                        placeholder="Size"
                        value={variant.size}
                        onChange={(event) =>
                          updateVariant(index, "size", event.target.value)
                        }
                      />

                      <input
                        className={inputClass}
                        placeholder="Color"
                        value={variant.color}
                        onChange={(event) =>
                          updateVariant(index, "color", event.target.value)
                        }
                      />

                      <input
                        className={inputClass}
                        type="number"
                        min="0"
                        step="1"
                        placeholder="Variant stock"
                        value={variant.stockQuantity}
                        onChange={(event) =>
                          updateVariant(
                            index,
                            "stockQuantity",
                            event.target.value
                          )
                        }
                        required
                      />

                      <button
                        type="button"
                        onClick={() => removeVariant(index)}
                        className="rounded-xl border border-red-400/20 px-3 py-2 text-sm text-red-300 hover:bg-red-500/10"
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="flex flex-wrap gap-3 border-t border-white/10 pt-5">
              <button
                type="submit"
                disabled={saving}
                className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold hover:bg-violet-500 disabled:opacity-60"
              >
                {saving && (
                  <RefreshCw size={16} className="animate-spin" />
                )}
                {saving
                  ? "Saving..."
                  : editingProduct
                    ? "Save Changes"
                    : "Create Product"}
              </button>

              <button
                type="button"
                disabled={saving}
                onClick={() => {
                  setShowForm(false);
                  setFormError("");
                }}
                className="rounded-xl border border-white/10 px-5 py-3 text-sm text-gray-300 hover:bg-white/5"
              >
                Cancel
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="mt-8 overflow-hidden rounded-2xl border border-white/10 bg-[#11111b]">
        <div className="flex flex-col gap-4 border-b border-white/10 p-5 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-lg font-bold">Product Catalog</h2>
            <p className="mt-1 text-sm text-gray-500">
              {filteredProducts.length} products shown
              {" · "}
              {inactiveCount} inactive overall
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row sm:flex-wrap">
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search name, SKU, category..."
              className="w-full rounded-xl border border-white/10 bg-[#09090f] px-4 py-3 text-sm outline-none focus:border-violet-500 sm:w-64"
            />

            <select
              value={category}
              onChange={(event) => setCategory(event.target.value)}
              className={selectClass}
            >
              <option value="ALL">All categories</option>
              {categories.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>

            <select
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              className={selectClass}
              aria-label="Filter by product status"
            >
              <option value="ALL">All statuses</option>
              <option value="ACTIVE">Active only</option>
              <option value="INACTIVE">Inactive only</option>
            </select>
          </div>
        </div>

        {loading ? (
          <p className="p-12 text-center text-gray-400">
            Loading products...
          </p>
        ) : error ? null : filteredProducts.length === 0 ? (
          <div className="p-12 text-center">
            <Package className="mx-auto mb-3 text-gray-500" size={30} />
            <p className="font-semibold">No products found</p>
            <p className="mt-2 text-sm text-gray-500">
              Try another search or category.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[1120px] text-left">
              <thead className="bg-white/[0.025] text-xs uppercase tracking-wider text-gray-500">
                <tr>
                  {[
                    "Product",
                    "SKU",
                    "Category",
                    "Price",
                    "Stock",
                    "Availability",
                    "Actions",
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
                {filteredProducts.map((product) => {
                  const stock = Number(product.stockQuantity || 0);
                  const active = isActive(product);

                  return (
                    <tr
                      key={product.id}
                      className="hover:bg-white/[0.025]"
                    >
                      <td className="px-5 py-4">
                        <div className="flex items-center gap-3">
                          {product.imageUrl ? (
                            <img
                              src={product.imageUrl}
                              alt={product.name}
                              className="h-12 w-12 rounded-xl border border-white/10 object-cover"
                            />
                          ) : (
                            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-violet-500/10 text-xl">
                              📦
                            </div>
                          )}

                          <div>
                            <p className="font-semibold">{product.name}</p>
                            <p className="mt-1 text-xs text-gray-500">
                              ID: {product.id}
                            </p>
                          </div>
                        </div>
                      </td>

                      <td className="px-5 py-4 text-sm text-gray-400">
                        {product.sku || "—"}
                      </td>

                      <td className="px-5 py-4 text-sm text-gray-300">
                        {product.category || "Uncategorized"}
                        {product.subcategory && (
                          <p className="mt-1 text-xs text-gray-500">
                            {product.subcategory}
                          </p>
                        )}
                      </td>

                      <td className="px-5 py-4">
                        <p className="font-semibold">
                          {currency(product.price)}
                        </p>
                        {Number(product.discountPercentage || 0) > 0 && (
                          <p className="mt-1 text-xs text-emerald-400">
                            {product.discountPercentage}% discount
                          </p>
                        )}
                      </td>

                      <td className="px-5 py-4">
                        <span
                          className={
                            stock === 0
                              ? "text-red-400"
                              : stock <= 5
                                ? "text-amber-300"
                                : "text-emerald-300"
                          }
                        >
                          {stock}
                        </span>
                      </td>

                      <td className="px-5 py-4">
                        <span
                          className={`rounded-full border px-3 py-1.5 text-xs ${
                            active
                              ? "border-emerald-400/20 bg-emerald-400/10 text-emerald-300"
                              : "border-gray-400/20 bg-gray-400/10 text-gray-300"
                          }`}
                        >
                          {active ? "Active" : "Inactive"}
                        </span>
                      </td>

                      <td className="px-5 py-4">
                        <div className="flex items-center gap-2">
                          <button
                            type="button"
                            onClick={() => openEditForm(product)}
                            className="inline-flex items-center gap-1.5 rounded-lg border border-violet-400/20 px-3 py-2 text-xs font-semibold text-violet-300 hover:bg-violet-500/10"
                          >
                            <Pencil size={14} />
                            Edit
                          </button>

                          <button
                            type="button"
                            onClick={() => handleToggleActive(product)}
                            disabled={updatingId === product.id}
                            className={`inline-flex items-center gap-1.5 rounded-lg border px-3 py-2 text-xs font-semibold disabled:opacity-50 ${
                              active
                                ? "border-red-400/20 text-red-300 hover:bg-red-500/10"
                                : "border-emerald-400/20 text-emerald-300 hover:bg-emerald-500/10"
                            }`}
                          >
                            {updatingId === product.id ? (
                              <RefreshCw
                                size={14}
                                className="animate-spin"
                              />
                            ) : active ? (
                              <XCircle size={14} />
                            ) : (
                              <CheckCircle size={14} />
                            )}
                            {active ? "Deactivate" : "Activate"}
                          </button>
                        </div>
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

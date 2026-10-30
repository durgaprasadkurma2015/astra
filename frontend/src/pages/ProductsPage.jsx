
import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowUpDown,
  Search,
  ShoppingCart,
  Package,
  SlidersHorizontal,
  X,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
  Tag,
  Heart,
  GitCompareArrows,
} from "lucide-react";

import productService from "../services/productService";
import cartService from "../services/cartService";
import api from "../services/api";
import { useAuth } from "../context/useAuth.js";
import { useCompare } from "../context/CompareContext.jsx";

const PAGE_SIZE = 12;

const inputClass =
  "w-full rounded-xl border border-white/10 bg-slate-900 px-3 py-3 text-sm text-white outline-none placeholder:text-slate-500 focus:border-violet-400";

const buttonClass =
  "inline-flex items-center justify-center gap-2 rounded-xl border border-white/10 bg-white/[0.04] px-4 py-3 text-sm font-semibold text-slate-200 transition hover:bg-white/10 disabled:cursor-not-allowed disabled:opacity-40";

export default function ProductsPage() {
  const [initialParams] = useState(
    () => new URLSearchParams(window.location.search)
  );
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [subcategories, setSubcategories] = useState([]);

  const [loading, setLoading] = useState(true);
  const [categoryLoading, setCategoryLoading] = useState(false);
  const [addingId, setAddingId] = useState(null);

  const [wishlistIds, setWishlistIds] = useState(() => new Set());
  const [wishlistLoading, setWishlistLoading] = useState(false);
  const [wishlistActionId, setWishlistActionId] = useState(null);
  const [wishlistError, setWishlistError] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [compareMessage, setCompareMessage] = useState("");

  const [search, setSearch] = useState(() => initialParams.get("q") || "");
  const [selectedCategory, setSelectedCategory] = useState(() => initialParams.get("category") || "");
  const [selectedSubcategory, setSelectedSubcategory] = useState(() => initialParams.get("subcategory") || "");
  const [minPrice, setMinPrice] = useState(() => initialParams.get("minPrice") || "");
  const [maxPrice, setMaxPrice] = useState(() => initialParams.get("maxPrice") || "");
  const [stockOnly, setStockOnly] = useState(() => initialParams.get("inStock") === "true");

  const [sortBy, setSortBy] = useState(() => initialParams.get("sortBy") || "id");
  const [direction, setDirection] = useState(() => initialParams.get("direction") || "asc");
  const [page, setPage] = useState(() => {
    const value = Number(initialParams.get("page") || 0);
    return Number.isSafeInteger(value) && value >= 0 ? value : 0;
  });
  const [retryKey, setRetryKey] = useState(0);

  const [pageData, setPageData] = useState({
    totalElements: 0,
    totalPages: 0,
    number: 0,
  });

  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  // Keep catalog state in the URL so filtered results can be bookmarked/shared.
  useEffect(() => {
    const params = new URLSearchParams();
    if (search.trim()) params.set("q", search.trim());
    if (selectedCategory) params.set("category", selectedCategory);
    if (selectedSubcategory) params.set("subcategory", selectedSubcategory);
    if (minPrice !== "") params.set("minPrice", minPrice);
    if (maxPrice !== "") params.set("maxPrice", maxPrice);
    if (stockOnly) params.set("inStock", "true");
    if (sortBy !== "id") params.set("sortBy", sortBy);
    if (direction !== "asc") params.set("direction", direction);
    if (page > 0) params.set("page", String(page));
    const query = params.toString();
    const nextUrl = `${window.location.pathname}${query ? `?${query}` : ""}`;
    window.history.replaceState(window.history.state, "", nextUrl);
  }, [search, selectedCategory, selectedSubcategory, minPrice, maxPrice, stockOnly, sortBy, direction, page]);

  const {
    compareCount,
    maxCompareProducts,
    addProduct,
    removeProduct,
    clearComparison,
    isCompared,
  } = useCompare();

  const visibleCategories = useMemo(
    () => ["", ...categories],
    [categories]
  );

  // Load categories.
  useEffect(() => {
    let cancelled = false;

    async function loadCategories() {
      try {
        const data = await productService.getCategories();

        if (!cancelled) {
          setCategories(Array.isArray(data) ? data : []);
        }
      } catch {
        if (!cancelled) {
          setCategories([]);
        }
      }
    }

    loadCategories();

    return () => {
      cancelled = true;
    };
  }, []);

  // Load subcategories when the selected category changes.
  useEffect(() => {
    let cancelled = false;

    async function loadSubcategories() {
      setSubcategories([]);

      if (!selectedCategory) {
        setCategoryLoading(false);
        return;
      }

      setCategoryLoading(true);

      try {
        const data =
          await productService.getSubcategories(selectedCategory);

        if (!cancelled) {
          setSubcategories(Array.isArray(data) ? data : []);
        }
      } catch {
        if (!cancelled) {
          setSubcategories([]);
        }
      } finally {
        if (!cancelled) {
          setCategoryLoading(false);
        }
      }
    }

    loadSubcategories();

    return () => {
      cancelled = true;
    };
  }, [selectedCategory]);

  // Load products using backend search, filters, sorting and pagination.
  useEffect(() => {
    let cancelled = false;

    const timer = setTimeout(async () => {
      setLoading(true);
      setError("");

      try {
        const result = await productService.searchProducts({
          keyword: search,
          category: selectedCategory,
          subcategory: selectedSubcategory,
          minPrice,
          maxPrice,
          inStock: stockOnly,
          page,
          size: PAGE_SIZE,
          sortBy,
          direction,
        });

        if (cancelled) return;

        setProducts(
          Array.isArray(result?.content) ? result.content : []
        );

        setPageData({
          totalElements: result?.totalElements ?? 0,
          totalPages: result?.totalPages ?? 0,
          number: result?.number ?? page,
        });
      } catch (err) {
        if (cancelled) return;

        setProducts([]);
        setPageData({
          totalElements: 0,
          totalPages: 0,
          number: 0,
        });

        setError(
          err.response?.data?.detail ||
            err.response?.data?.message ||
            "Unable to load products. Please check your connection and try again."
        );
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }, 300);

    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [
    search,
    selectedCategory,
    selectedSubcategory,
    minPrice,
    maxPrice,
    stockOnly,
    sortBy,
    direction,
    page,
    retryKey,
  ]);

  // Load the current user's wishlist.
  useEffect(() => {
    let cancelled = false;

    async function loadWishlist() {
      if (authLoading) return;

      if (!user) {
        setWishlistIds(new Set());
        setWishlistError("");
        setWishlistLoading(false);
        return;
      }

      setWishlistLoading(true);
      setWishlistError("");

      try {
        const response = await api.get("/wishlist");

        if (cancelled) return;

        const data = response.data;
        const items = Array.isArray(data)
          ? data
          : Array.isArray(data?.items)
            ? data.items
            : Array.isArray(data?.content)
              ? data.content
              : Array.isArray(data?.products)
                ? data.products
                : [];

        const ids = items
          .map((item) => {
            const id =
              item?.productId ??
              item?.product?.id ??
              item?.id;

            return id == null ? null : String(id);
          })
          .filter((id) => id !== null);

        setWishlistIds(new Set(ids));
      } catch (err) {
        if (cancelled) return;

        setWishlistError(
          err.response?.data?.detail ||
            err.response?.data?.message ||
            "Could not load your wishlist. Please retry."
        );
      } finally {
        if (!cancelled) {
          setWishlistLoading(false);
        }
      }
    }

    loadWishlist();

    return () => {
      cancelled = true;
    };
  }, [user, authLoading, retryKey]);

  function money(amount) {
    return `₹${Number(amount || 0).toLocaleString("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  }

  function getDiscount(product) {
    const value = Number(product.discountPercentage ?? 0);

    if (!Number.isFinite(value)) return 0;

    return Math.min(100, Math.max(0, value));
  }

  function getFinalPrice(product) {
    const price = Number(product.price || 0);
    const discount = getDiscount(product);

    return price * (1 - discount / 100);
  }

  function getErrorMessage(err, fallback) {
    return (
      err.response?.data?.detail ||
      err.response?.data?.message ||
      fallback
    );
  }

  function changeSearch(value) {
    setSearch(value);
    setPage(0);
    setMessage("");
  }

  function changeCategory(value) {
    setSelectedCategory(value);
    setSelectedSubcategory("");
    setPage(0);
    setMessage("");
  }

  function changeSubcategory(value) {
    setSelectedSubcategory(value);
    setPage(0);
  }

  function changeMinPrice(value) {
    setMinPrice(value);
    setPage(0);
  }

  function changeMaxPrice(value) {
    setMaxPrice(value);
    setPage(0);
  }

  function changeSorting(value) {
    const [field, order] = value.split(":");

    setSortBy(field);
    setDirection(order);
    setPage(0);
  }

  function clearFilters() {
    setSearch("");
    setSelectedCategory("");
    setSelectedSubcategory("");
    setMinPrice("");
    setMaxPrice("");
    setStockOnly(false);
    setSortBy("id");
    setDirection("asc");
    setPage(0);
    setMessage("");
    setError("");
  }

  function retryLoading() {
    setError("");
    setWishlistError("");
    setRetryKey((key) => key + 1);
  }

  // Add a product to the cart.
  async function handleAddToCart(product) {
    setMessage("");
    setError("");

    if (!user) {
      navigate("/login", {
        state: { from: { pathname: "/products" } },
      });
      return;
    }

    setAddingId(product.id);

    try {
      await cartService.addItem(product.id, 1);
      setMessage(`${product.name} added to your cart.`);
    } catch (err) {
      setError(
        getErrorMessage(err, "Could not add this product to your cart.")
      );
    } finally {
      setAddingId(null);
    }
  }

  // Add or remove a product from the wishlist.
  async function handleWishlistToggle(product) {
    setMessage("");
    setError("");
    setWishlistError("");

    if (!user) {
      navigate("/login", {
        state: { from: { pathname: "/products" } },
      });
      return;
    }

    if (wishlistActionId !== null) return;

    const productId = String(product.id);
    const alreadyInWishlist = wishlistIds.has(productId);

    setWishlistActionId(product.id);

    try {
      if (alreadyInWishlist) {
        await api.delete(`/wishlist/items/${product.id}`);

        setWishlistIds((currentIds) => {
          const updated = new Set(currentIds);
          updated.delete(productId);
          return updated;
        });

        setMessage(`${product.name} removed from your wishlist.`);
      } else {
        await api.post(`/wishlist/items/${product.id}`);

        setWishlistIds((currentIds) => {
          const updated = new Set(currentIds);
          updated.add(productId);
          return updated;
        });

        setMessage(`${product.name} added to your wishlist.`);
      }
    } catch (err) {
      setWishlistError(
        getErrorMessage(
          err,
          "Could not update your wishlist. Please try again."
        )
      );
    } finally {
      setWishlistActionId(null);
    }
  }

  // Add or remove a product from comparison.
  function handleCompareToggle(product) {
    setCompareMessage("");

    if (isCompared(product.id)) {
      removeProduct(product.id);
      setCompareMessage(`${product.name} removed from comparison.`);
      return;
    }

    const result = addProduct(product);

    if (!result?.success) {
      setCompareMessage(
        result?.message || "Could not update the comparison list."
      );
      return;
    }

    setCompareMessage(`${product.name} added to comparison.`);
  }

  const totalPages = pageData.totalPages;

  const firstItem =
    pageData.totalElements === 0
      ? 0
      : pageData.number * PAGE_SIZE + 1;

  const lastItem = Math.min(
    (pageData.number + 1) * PAGE_SIZE,
    pageData.totalElements
  );

  return (
    <div className="min-h-screen bg-slate-950 text-white">
      {/* Shared StorefrontLayout renders the main header and footer. */}

      <section className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        {/* Hero */}
        <div className="relative mb-10 overflow-hidden rounded-3xl border border-violet-400/20 bg-gradient-to-br from-violet-500/15 via-slate-900 to-indigo-500/10 p-6 sm:p-10 lg:p-12">
          <div className="pointer-events-none absolute -right-16 -top-24 h-72 w-72 rounded-full bg-violet-600/15 blur-3xl" />

          <div className="relative max-w-2xl">
            <p className="text-xs font-bold uppercase tracking-[0.25em] text-violet-300">
              Discover ASTRA
            </p>

            <h1 className="mt-3 text-4xl font-extrabold tracking-tight sm:text-5xl">
              Find your next{" "}
              <span className="text-violet-400">favorite.</span>
            </h1>

            <p className="mt-4 max-w-xl leading-7 text-slate-300">
              Search by product name or SKU, filter by category and
              price, and discover products that fit your needs.
              Save your favorite products to your wishlist.
            </p>

            <div className="mt-7 flex items-center gap-2 text-sm text-slate-400">
              <Package size={17} className="text-violet-300" />
              {loading
                ? "Loading catalogue..."
                : `${pageData.totalElements} products found`}
            </div>
          </div>
        </div>

        {/* Success message */}
        {message && (
          <div
            role="status"
            className="mb-5 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-emerald-400/20 bg-emerald-400/10 p-4 text-sm text-emerald-200"
          >
            <span>{message}</span>

            {message.toLowerCase().includes("cart") && (
              <Link to="/cart" className="font-semibold underline">
                View cart
              </Link>
            )}

            {message.toLowerCase().includes("wishlist") && (
              <Link to="/wishlist" className="font-semibold underline">
                View wishlist
              </Link>
            )}
          </div>
        )}

        {/* Wishlist error */}
        {wishlistError && (
          <div
            role="alert"
            className="mb-5 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-pink-400/20 bg-pink-400/10 p-4 text-sm text-pink-200"
          >
            <span>{wishlistError}</span>

            <button
              type="button"
              onClick={retryLoading}
              className="inline-flex items-center gap-2 font-semibold underline"
            >
              <RefreshCw size={15} />
              Retry
            </button>
          </div>
        )}

        {/* Product loading error */}
        {error && (
          <div
            role="alert"
            className="mb-5 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200"
          >
            <span>{error}</span>

            <button
              type="button"
              onClick={retryLoading}
              className="inline-flex items-center gap-2 font-semibold underline"
            >
              <RefreshCw size={15} />
              Retry
            </button>
          </div>
        )}

        {/* Search and filters */}
        <section className="mb-8 rounded-2xl border border-white/10 bg-white/[0.03] p-4 sm:p-5">
          <div className="mb-4 flex items-center gap-2">
            <SlidersHorizontal
              size={19}
              className="text-violet-300"
            />
            <h2 className="font-semibold">Search and discovery</h2>
          </div>

          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            <div>
              <label
                htmlFor="product-search"
                className="mb-2 block text-xs font-medium text-slate-400"
              >
                Search products or SKU
              </label>

              <div className="relative">
                <Search
                  size={18}
                  className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                />

                <input
                  id="product-search"
                  type="search"
                  value={search}
                  onChange={(event) => changeSearch(event.target.value)}
                  placeholder="Name, description, SKU..."
                  className={`${inputClass} py-3 pl-11 pr-10`}
                />

                {search && (
                  <button
                    type="button"
                    onClick={() => changeSearch("")}
                    aria-label="Clear search"
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
                  >
                    <X size={17} />
                  </button>
                )}
              </div>
            </div>

            <div>
              <label
                htmlFor="product-category"
                className="mb-2 block text-xs font-medium text-slate-400"
              >
                Category
              </label>

              <select
                id="product-category"
                value={selectedCategory}
                onChange={(event) => changeCategory(event.target.value)}
                className={inputClass}
              >
                <option value="">All categories</option>

                {visibleCategories.filter(Boolean).map((category) => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label
                htmlFor="product-subcategory"
                className="mb-2 block text-xs font-medium text-slate-400"
              >
                Subcategory
              </label>

              <select
                id="product-subcategory"
                value={selectedSubcategory}
                onChange={(event) =>
                  changeSubcategory(event.target.value)
                }
                disabled={
                  !selectedCategory ||
                  categoryLoading ||
                  subcategories.length === 0
                }
                className={`${inputClass} disabled:cursor-not-allowed disabled:opacity-50`}
              >
                <option value="">
                  {categoryLoading
                    ? "Loading subcategories..."
                    : "All subcategories"}
                </option>

                {subcategories.map((subcategory) => (
                  <option key={subcategory} value={subcategory}>
                    {subcategory}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label
                htmlFor="min-price"
                className="mb-2 block text-xs font-medium text-slate-400"
              >
                Minimum price (₹)
              </label>

              <input
                id="min-price"
                type="number"
                min="0"
                step="0.01"
                value={minPrice}
                onChange={(event) => changeMinPrice(event.target.value)}
                placeholder="e.g. 500"
                className={inputClass}
              />
            </div>

            <div>
              <label
                htmlFor="max-price"
                className="mb-2 block text-xs font-medium text-slate-400"
              >
                Maximum price (₹)
              </label>

              <input
                id="max-price"
                type="number"
                min="0"
                step="0.01"
                value={maxPrice}
                onChange={(event) => changeMaxPrice(event.target.value)}
                placeholder="e.g. 50000"
                className={inputClass}
              />
            </div>

            <div>
              <label
                htmlFor="product-sort"
                className="mb-2 block text-xs font-medium text-slate-400"
              >
                Sort products
              </label>

              <select
                id="product-sort"
                value={`${sortBy}:${direction}`}
                onChange={(event) => changeSorting(event.target.value)}
                className={inputClass}
              >
                <option value="id:asc">Default order</option>
                <option value="price:asc">Price: Low to High</option>
                <option value="price:desc">Price: High to Low</option>
                <option value="name:asc">Name: A to Z</option>
                <option value="name:desc">Name: Z to A</option>
                <option value="createdAt:desc">Newest first</option>
                <option value="createdAt:asc">Oldest first</option>
              </select>
            </div>
          </div>

          <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-white/10 pt-4">
            <label className="inline-flex cursor-pointer items-center gap-2 text-sm text-slate-300">
              <input
                type="checkbox"
                checked={stockOnly}
                onChange={(event) => {
                  setStockOnly(event.target.checked);
                  setPage(0);
                }}
                className="h-4 w-4 accent-violet-500"
              />
              In-stock products only
            </label>

            <button
              type="button"
              onClick={clearFilters}
              className="inline-flex items-center gap-2 text-sm font-medium text-violet-300 transition hover:text-violet-200"
            >
              <X size={15} />
              Clear filters
            </button>
          </div>
        </section>

        {/* Product comparison summary */}
        <div className="mb-6 flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-violet-400/20 bg-violet-500/[0.06] p-4">
          <div className="flex items-center gap-3">
            <GitCompareArrows
              size={22}
              className="text-violet-300"
            />

            <div>
              <p className="font-semibold text-white">
                Compare products
              </p>

              <p className="mt-1 text-sm text-slate-400">
                {compareCount} of {maxCompareProducts} products selected
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {compareCount > 0 && (
              <button
                type="button"
                onClick={() => {
                  clearComparison();
                  setCompareMessage("");
                }}
                className="text-sm text-slate-400 hover:text-white"
              >
                Clear selection
              </button>
            )}

            <button
              type="button"
              disabled={compareCount < 2}
              onClick={() => navigate("/compare")}
              className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:opacity-40"
            >
              <GitCompareArrows size={17} />
              Compare Now ({compareCount})
            </button>
          </div>
        </div>

        {compareMessage && (
          <div
            role="status"
            className="mb-5 rounded-xl border border-violet-400/20 bg-violet-500/10 p-3 text-sm text-violet-200"
          >
            {compareMessage}
          </div>
        )}

        {/* Results summary */}
        <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
          <p className="text-sm text-slate-400">
            {loading
              ? "Finding products..."
              : pageData.totalElements === 0
                ? "No products to display"
                : `Showing ${firstItem}–${lastItem} of ${pageData.totalElements} products`}
          </p>

          <div className="inline-flex items-center gap-2 text-xs text-slate-500">
            <ArrowUpDown size={15} />

            {sortBy === "price" && direction === "asc"
              ? "Lowest price first"
              : sortBy === "price" && direction === "desc"
                ? "Highest price first"
                : sortBy === "name"
                  ? "Sorted by name"
                  : sortBy === "createdAt"
                    ? "Sorted by date added"
                    : "Default order"}
          </div>
        </div>

        {/* Loading skeleton */}
        {loading ? (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {Array.from({ length: 8 }).map((_, index) => (
              <div
                key={index}
                className="h-96 animate-pulse rounded-2xl border border-white/5 bg-white/[0.04]"
              />
            ))}
          </div>
        ) : error ? (
          <div className="rounded-3xl border border-dashed border-red-400/20 bg-red-400/[0.04] px-6 py-16 text-center">
            <Package
              size={45}
              className="mx-auto text-red-300"
            />

            <h2 className="mt-5 text-xl font-bold">
              Products could not be loaded
            </h2>

            <p className="mt-2 text-sm text-slate-400">
              Check that the backend is running, then try again.
            </p>

            <button
              type="button"
              onClick={retryLoading}
              className="mt-6 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold text-white hover:bg-violet-500"
            >
              Retry
            </button>
          </div>
        ) : products.length === 0 ? (
          <div className="rounded-3xl border border-dashed border-white/15 bg-white/[0.03] px-6 py-16 text-center">
            <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-2xl bg-violet-500/10">
              <Search size={35} className="text-violet-300" />
            </div>

            <h2 className="mt-5 text-xl font-bold">
              No products found
            </h2>

            <p className="mt-2 text-sm text-slate-400">
              Try another keyword or adjust your price range and filters.
            </p>

            <button
              type="button"
              onClick={clearFilters}
              className="mt-6 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-violet-500"
            >
              Clear all filters
            </button>
          </div>
        ) : (
          <>
            {/* Product cards */}
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {products.map((product) => {
                const discount = getDiscount(product);
                const finalPrice = getFinalPrice(product);

                const stockQuantity = Number(
                  product.stockQuantity ?? 0
                );

                const isWishlisted = wishlistIds.has(
                  String(product.id)
                );

                const isWishlistActionRunning =
                  wishlistActionId === product.id;

                const productIsCompared = isCompared(product.id);

                return (
                  <article
                    key={product.id}
                    className="group relative flex flex-col overflow-hidden rounded-2xl border border-white/10 bg-white/[0.035] transition duration-300 hover:-translate-y-1 hover:border-violet-400/40 hover:shadow-xl hover:shadow-violet-950/20"
                  >
                    {/* Product image and category */}
                    <Link
                      to={`/products/${product.id}`}
                      aria-label={`View ${product.name} details`}
                      className="relative flex h-56 items-center justify-center overflow-hidden bg-gradient-to-br from-violet-500/10 via-slate-900 to-indigo-500/10"
                    >
                      {product.imageUrl ? (
                        <img
                          src={product.imageUrl}
                          alt={product.name}
                          loading="lazy"
                          onError={(event) => {
                            event.currentTarget.style.display = "none";
                          }}
                          className="h-full w-full object-cover transition duration-500 group-hover:scale-105"
                        />
                      ) : (
                        <Package
                          size={62}
                          className="text-violet-300/50 transition duration-300 group-hover:scale-110"
                        />
                      )}

                      <span className="absolute left-3 top-3 rounded-full border border-white/10 bg-slate-950/80 px-3 py-1 text-xs font-medium text-violet-200 backdrop-blur">
                        {product.category || "ASTRA Collection"}
                      </span>

                      {discount > 0 && (
                        <span className="absolute right-14 top-3 inline-flex items-center gap-1 rounded-full bg-emerald-500 px-3 py-1 text-xs font-bold text-white">
                          <Tag size={12} />
                          {discount}% OFF
                        </span>
                      )}

                      {stockQuantity < 1 && (
                        <span className="absolute bottom-3 left-3 rounded-full bg-red-500/90 px-3 py-1 text-xs font-semibold text-white">
                          Out of stock
                        </span>
                      )}
                    </Link>

                    {/* Wishlist button */}
                    <button
                      type="button"
                      onClick={() => handleWishlistToggle(product)}
                      disabled={
                        authLoading ||
                        wishlistLoading ||
                        isWishlistActionRunning ||
                        wishlistActionId !== null
                      }
                      aria-label={
                        isWishlisted
                          ? `Remove ${product.name} from wishlist`
                          : `Add ${product.name} to wishlist`
                      }
                      aria-pressed={isWishlisted}
                      title={
                        isWishlisted
                          ? "Remove from wishlist"
                          : "Add to wishlist"
                      }
                      className={`absolute right-3 top-3 z-10 flex h-10 w-10 items-center justify-center rounded-full border shadow-lg backdrop-blur transition ${
                        isWishlisted
                          ? "border-pink-400/40 bg-pink-500 text-white"
                          : "border-white/15 bg-slate-950/85 text-slate-200 hover:border-pink-400/40 hover:bg-pink-500/20 hover:text-pink-300"
                      } disabled:cursor-not-allowed disabled:opacity-60`}
                    >
                      {isWishlistActionRunning ? (
                        <RefreshCw
                          size={18}
                          className="animate-spin"
                        />
                      ) : (
                        <Heart
                          size={19}
                          fill={isWishlisted ? "currentColor" : "none"}
                        />
                      )}
                    </button>

                    {/* Product information */}
                    <div className="flex flex-1 flex-col p-5">
                      <Link
                        to={`/products/${product.id}`}
                        className="line-clamp-2 text-lg font-bold transition hover:text-violet-300"
                      >
                        {product.name}
                      </Link>

                      {product.sku && (
                        <p className="mt-1 text-xs text-slate-500">
                          SKU: {product.sku}
                        </p>
                      )}

                      {product.subcategory && (
                        <p className="mt-1 text-xs text-slate-500">
                          {product.subcategory}
                        </p>
                      )}

                      <p className="mt-2 line-clamp-2 min-h-10 text-sm leading-5 text-slate-400">
                        {product.description ||
                          "Discover this product from ASTRA."}
                      </p>

                      {/* Price and discount */}
                      <div className="mt-5 flex items-end justify-between gap-3">
                        <div>
                          <p className="text-xs text-slate-500">
                            Price
                          </p>

                          <p className="mt-1 text-xl font-extrabold text-white">
                            {money(finalPrice)}
                          </p>

                          {discount > 0 && (
                            <p className="mt-1 text-sm text-slate-500 line-through">
                              {money(product.price)}
                            </p>
                          )}
                        </div>

                        {stockQuantity > 0 && (
                          <span className="text-xs font-medium text-emerald-300">
                            In stock
                          </span>
                        )}
                      </div>

                      {/* Product variants */}
                      {Array.isArray(product.variants) &&
                        product.variants.length > 0 && (
                          <p className="mt-3 line-clamp-1 text-xs text-slate-400">
                            Options:{" "}
                            {[
                              ...new Set(
                                product.variants
                                  .flatMap((variant) => [
                                    variant.size,
                                    variant.color,
                                  ])
                                  .filter(Boolean)
                              ),
                            ].join(", ")}
                          </p>
                        )}

                      {/* Add to cart */}
                      <button
                        type="button"
                        disabled={
                          stockQuantity < 1 ||
                          addingId !== null ||
                          authLoading
                        }
                        onClick={() => handleAddToCart(product)}
                        className="mt-5 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:bg-white/10 disabled:text-slate-500"
                      >
                        <ShoppingCart size={17} />

                        {addingId === product.id
                          ? "Adding to cart..."
                          : stockQuantity < 1
                            ? "Out of stock"
                            : "Add to cart"}
                      </button>

                      {/* Add or remove from comparison */}
                      <button
                        type="button"
                        onClick={() => handleCompareToggle(product)}
                        aria-pressed={productIsCompared}
                        className={`mt-3 inline-flex w-full items-center justify-center gap-2 rounded-xl border px-4 py-3 text-sm font-semibold transition ${
                          productIsCompared
                            ? "border-violet-400/40 bg-violet-500/15 text-violet-200 hover:bg-violet-500/25"
                            : "border-white/10 bg-white/[0.04] text-slate-200 hover:border-violet-400/40 hover:bg-white/10"
                        }`}
                      >
                        <GitCompareArrows size={17} />

                        {productIsCompared
                          ? "Remove from Compare"
                          : "Add to Compare"}
                      </button>
                    </div>
                  </article>
                );
              })}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <nav
                aria-label="Product pagination"
                className="mt-10 flex flex-wrap items-center justify-center gap-3"
              >
                <button
                  type="button"
                  disabled={page <= 0 || loading}
                  onClick={() =>
                    setPage((current) => Math.max(0, current - 1))
                  }
                  className={buttonClass}
                >
                  <ChevronLeft size={17} />
                  Previous
                </button>

                <span className="rounded-xl border border-violet-400/20 bg-violet-500/10 px-4 py-3 text-sm font-medium text-violet-200">
                  Page {pageData.number + 1} of {totalPages}
                </span>

                <button
                  type="button"
                  disabled={page >= totalPages - 1 || loading}
                  onClick={() =>
                    setPage((current) =>
                      Math.min(totalPages - 1, current + 1)
                    )
                  }
                  className={buttonClass}
                >
                  Next
                  <ChevronRight size={17} />
                </button>
              </nav>
            )}
          </>
        )}
      </section>
    </div>
  );
}

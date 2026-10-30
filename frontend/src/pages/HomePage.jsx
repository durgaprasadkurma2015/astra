
import { useEffect, useMemo, useState } from "react";
import {
  ArrowRight,
  Search,
  Heart,
  Package,
  X,
  Trash2,
  LoaderCircle,
} from "lucide-react";
import { Link, useNavigate } from "react-router-dom";

import Button from "../components/common/Button";
import { useAuth } from "../context/useAuth.js";
import productService from "../services/productService";
import api from "../services/api";

const categoryStyles = [
  {
    name: "Electronics",
    icon: "🎧",
    description: "Latest gadgets and smart devices",
  },
  {
    name: "Fashion",
    icon: "👜",
    description: "Styles for every occasion",
  },
  {
    name: "Home & Living",
    icon: "🛋️",
    description: "Make your space feel special",
  },
  {
    name: "Accessories",
    icon: "✨",
    description: "Everyday essentials and more",
  },
];

function formatPrice(value) {
  const price = Number(value);

  return Number.isFinite(price)
    ? `₹${price.toLocaleString("en-IN", {
        maximumFractionDigits: 2,
      })}`
    : "Price unavailable";
}

function extractItems(responseData) {
  if (Array.isArray(responseData)) return responseData;
  if (Array.isArray(responseData?.items)) return responseData.items;
  if (Array.isArray(responseData?.content)) return responseData.content;
  return [];
}

function getWishlistProductId(item) {
  return item?.productId ?? item?.product?.id ?? item?.id;
}

export default function HomePage() {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  const [products, setProducts] = useState([]);
  const [productsLoading, setProductsLoading] = useState(true);
  const [productsError, setProductsError] = useState("");

  const [categoriesFromApi, setCategoriesFromApi] = useState([]);
  const [search, setSearch] = useState("");
  const [activeCategory, setActiveCategory] = useState("All");

  const [wishlist, setWishlist] = useState([]);
  const [wishlistLoading, setWishlistLoading] = useState(false);
  const [wishlistError, setWishlistError] = useState("");
  const [wishlistActionId, setWishlistActionId] = useState(null);
  const [showWishlist, setShowWishlist] = useState(false);

  // Load products.
  useEffect(() => {
    let cancelled = false;

    async function loadProducts() {
      setProductsLoading(true);
      setProductsError("");

      try {
        const data = await productService.getProducts();

        if (!cancelled) {
          setProducts(extractItems(data));
        }
      } catch (error) {
        console.error("Failed to load products:", error);

        if (!cancelled) {
          setProductsError(
            "We couldn't load products right now. Please try again."
          );
        }
      } finally {
        if (!cancelled) {
          setProductsLoading(false);
        }
      }
    }

    loadProducts();

    return () => {
      cancelled = true;
    };
  }, []);

  // Load categories.
  useEffect(() => {
    let cancelled = false;

    async function loadCategories() {
      try {
        const data = await productService.getCategories();

        if (cancelled) return;

        if (Array.isArray(data)) {
          setCategoriesFromApi(
            data
              .map((category) =>
                typeof category === "string"
                  ? category
                  : category?.name
              )
              .filter(Boolean)
          );
        }
      } catch (error) {
        console.error("Failed to load categories:", error);
      }
    }

    loadCategories();

    return () => {
      cancelled = true;
    };
  }, []);

  // Load the authenticated user's wishlist.
  useEffect(() => {
    let cancelled = false;

    async function loadWishlist() {
      if (authLoading) return;

      if (!user) {
        setWishlist([]);
        setWishlistError("");
        setWishlistLoading(false);
        return;
      }

      setWishlistLoading(true);
      setWishlistError("");

      try {
        const response = await api.get("/wishlist");

        if (!cancelled) {
          setWishlist(extractItems(response.data));
        }
      } catch (error) {
        console.error("Failed to load Wishlist:", error);

        if (!cancelled) {
          setWishlistError(
            "Unable to load your Wishlist. Please try again."
          );
        }
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
  }, [user, authLoading]);

  const categories = useMemo(() => {
    if (categoriesFromApi.length > 0) {
      return categoriesFromApi;
    }

    return [
      ...new Set(
        products.map((product) => product.category).filter(Boolean)
      ),
    ];
  }, [categoriesFromApi, products]);

  const visibleProducts = useMemo(() => {
    const query = search.trim().toLowerCase();

    return products.filter((product) => {
      const matchesCategory =
        activeCategory === "All" ||
        product.category === activeCategory;

      const searchable = [
        product.name,
        product.description,
        product.category,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return matchesCategory && (!query || searchable.includes(query));
    });
  }, [products, search, activeCategory]);

  const featuredProducts = visibleProducts.slice(0, 8);

  function isInWishlist(productId) {
    return wishlist.some(
      (item) =>
        String(getWishlistProductId(item)) === String(productId)
    );
  }

  async function toggleWishlist(product) {
    if (!user) {
      navigate("/login", {
        state: { from: { pathname: "/" } },
      });
      return;
    }

    const productId = product?.id;

    if (productId == null) {
      setWishlistError("This product does not have a valid ID.");
      return;
    }

    if (wishlistActionId !== null) return;

    const alreadyWished = isInWishlist(productId);

    setWishlistActionId(String(productId));
    setWishlistError("");

    try {
      if (alreadyWished) {
        await api.delete(`/wishlist/items/${productId}`);

        setWishlist((current) =>
          current.filter(
            (item) =>
              String(getWishlistProductId(item)) !==
              String(productId)
          )
        );
      } else {
        const response = await api.post(
          `/wishlist/items/${productId}`
        );

        const returnedItem = response?.data;

        const newItem =
          returnedItem &&
          typeof returnedItem === "object" &&
          (
            returnedItem.productId != null ||
            returnedItem.product?.id != null
          )
            ? returnedItem
            : {
                productId: product.id,
                name: product.name,
                description: product.description,
                category: product.category,
                imageUrl: product.imageUrl,
                price: product.price,
                discountedPrice: product.discountedPrice,
                stockQuantity: product.stockQuantity,
              };

        setWishlist((current) => {
          const exists = current.some(
            (item) =>
              String(getWishlistProductId(item)) ===
              String(productId)
          );

          return exists ? current : [...current, newItem];
        });
      }
    } catch (error) {
      console.error("Wishlist update failed:", error);

      setWishlistError(
        error?.response?.data?.message ||
          "Could not update your Wishlist. Please try again."
      );
    } finally {
      setWishlistActionId(null);
    }
  }

  function chooseCategory(category) {
    setActiveCategory(category);

    document
      .getElementById("products")
      ?.scrollIntoView({ behavior: "smooth" });
  }

  function getWishlistDisplayProduct(item) {
    const productId = getWishlistProductId(item);

    const catalogueProduct = products.find(
      (product) => String(product.id) === String(productId)
    );

    return {
      ...catalogueProduct,
      ...item,
      id: productId,
      name:
        item.name ??
        item.product?.name ??
        catalogueProduct?.name ??
        "ASTRA product",
      imageUrl:
        item.imageUrl ??
        item.product?.imageUrl ??
        catalogueProduct?.imageUrl,
      price:
        item.price ??
        item.product?.price ??
        catalogueProduct?.price,
      description:
        item.description ??
        item.product?.description ??
        catalogueProduct?.description,
    };
  }

  return (
    <main className="min-h-screen overflow-hidden bg-[#050714] text-white">
      {/* HERO */}
      <section
        id="home"
        className="relative overflow-hidden border-b border-white/10"
      >
        <div className="pointer-events-none absolute -left-32 top-0 h-80 w-80 rounded-full bg-violet-600/20 blur-[100px]" />
        <div className="pointer-events-none absolute -right-20 bottom-0 h-80 w-80 rounded-full bg-indigo-500/20 blur-[100px]" />

        <div className="relative mx-auto grid max-w-[1440px] items-center gap-10 px-5 py-16 sm:py-20 lg:grid-cols-2 lg:px-10 lg:py-24">
          <div>
            <p className="inline-flex items-center gap-2 rounded-full border border-violet-400/30 bg-violet-500/10 px-4 py-2 text-xs font-bold uppercase tracking-[0.18em] text-violet-200">
              <span className="h-2 w-2 rounded-full bg-violet-300" />
              Welcome to ASTRA
            </p>

            <h1 className="mt-6 max-w-2xl text-4xl font-black leading-tight sm:text-5xl lg:text-6xl">
              Discover your next{" "}
              <span className="bg-gradient-to-r from-violet-300 to-indigo-300 bg-clip-text text-transparent">
                favourite.
              </span>
            </h1>

            <p className="mt-5 max-w-xl text-base leading-7 text-slate-400">
              Explore products for your everyday life, find what you love,
              and save your favourites to your personal Wishlist.
            </p>

            <div className="mt-8 flex flex-wrap gap-3">
              <Button onClick={() => navigate("/products")}>
                Shop products
                <ArrowRight size={16} />
              </Button>

              <button
                type="button"
                onClick={() => {
                  setShowWishlist(true);
                  setWishlistError("");
                }}
                className="inline-flex items-center gap-2 rounded-xl border border-white/15 px-5 py-3 text-sm font-bold transition hover:border-pink-400/50 hover:bg-pink-500/10"
              >
                <Heart size={17} />
                My Wishlist
              </button>
            </div>

            <div className="mt-10 flex flex-wrap gap-6 text-sm text-slate-400">
              <div>
                <p className="text-2xl font-extrabold text-white">
                  {products.length}+
                </p>
                <p className="mt-1">Products</p>
              </div>

              <div>
                <p className="text-2xl font-extrabold text-white">
                  {categories.length}
                </p>
                <p className="mt-1">Categories</p>
              </div>

              <div>
                <p className="text-2xl font-extrabold text-white">
                  {wishlist.length}
                </p>
                <p className="mt-1">Saved favourites</p>
              </div>
            </div>
          </div>

          <div className="relative mx-auto w-full max-w-lg">
            <div className="rounded-[2rem] border border-white/10 bg-gradient-to-br from-violet-500/15 via-[#10152c] to-indigo-500/15 p-6 shadow-2xl shadow-violet-950/30 sm:p-10">
              <div className="flex items-center justify-between">
                <span className="text-sm font-bold tracking-[0.2em] text-violet-200">
                  ASTRA EDIT
                </span>
                <Heart className="text-pink-300" size={24} />
              </div>

              <div className="my-10 grid grid-cols-2 gap-4">
                <div className="flex h-36 items-center justify-center rounded-2xl border border-white/10 bg-white/5 text-5xl">
                  🎧
                </div>
                <div className="mt-8 flex h-36 items-center justify-center rounded-2xl border border-white/10 bg-white/5 text-5xl">
                  👟
                </div>
                <div className="flex h-36 items-center justify-center rounded-2xl border border-white/10 bg-white/5 text-5xl">
                  👜
                </div>
                <div className="mt-8 flex h-36 items-center justify-center rounded-2xl border border-white/10 bg-white/5 text-5xl">
                  ⌚
                </div>
              </div>

              <p className="text-center text-lg font-bold">
                Find something made for you.
              </p>
              <p className="mt-2 text-center text-sm text-slate-400">
                Your next favourite is waiting.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CATEGORIES */}
      <section
        id="categories"
        className="mx-auto max-w-[1440px] px-5 py-10 lg:px-10 lg:py-12"
      >
        <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-violet-300">
              Find your style
            </p>
            <h2 className="mt-2 text-2xl font-bold sm:text-3xl">
              Shop by category
            </h2>
          </div>

          <Link
            to="/products"
            className="inline-flex items-center gap-2 text-sm font-semibold text-violet-300 hover:text-violet-200"
          >
            View all products
            <ArrowRight size={16} />
          </Link>
        </div>

        {categories.length === 0 ? (
          <p className="rounded-xl border border-white/10 bg-white/[0.03] p-5 text-sm text-slate-400">
            {productsLoading
              ? "Loading categories..."
              : "No categories available yet."}
          </p>
        ) : (
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            {categories.map((categoryName, index) => {
              const categoryStyle =
                categoryStyles.find(
                  (item) =>
                    item.name.toLowerCase() ===
                    String(categoryName).toLowerCase()
                ) || {
                  name: categoryName,
                  icon: "🛍️",
                  description: `Explore ${categoryName} products`,
                };

              return (
                <button
                  key={categoryName}
                  type="button"
                  onClick={() => chooseCategory(categoryName)}
                  className={`group flex min-h-28 items-center gap-4 rounded-2xl border p-4 text-left transition hover:-translate-y-1 hover:border-violet-400/60 hover:bg-violet-500/10 ${
                    index === 0
                      ? "border-violet-500/50 bg-violet-900/20"
                      : "border-white/10 bg-white/[0.035]"
                  }`}
                >
                  <span className="flex h-16 w-16 shrink-0 items-center justify-center rounded-xl border border-white/10 bg-gradient-to-br from-violet-500/20 to-indigo-500/10 text-3xl">
                    {categoryStyle.icon}
                  </span>

                  <span className="min-w-0 flex-1">
                    <span className="block font-bold">
                      {categoryName}
                    </span>
                    <span className="mt-1 block text-xs leading-5 text-slate-400">
                      {categoryStyle.description}
                    </span>
                  </span>

                  <ArrowRight
                    size={17}
                    className="shrink-0 text-violet-300 transition group-hover:translate-x-1"
                  />
                </button>
              );
            })}
          </div>
        )}
      </section>

      {/* PRODUCTS */}
      <section
        id="products"
        className="mx-auto max-w-[1440px] px-5 pb-16 lg:px-10"
      >
        <label className="mb-6 flex max-w-xl items-center gap-3 rounded-xl border border-white/10 bg-white/[0.04] px-4 py-3 focus-within:border-violet-400/60">
          <Search size={18} className="shrink-0 text-slate-400" />
          <input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Search products..."
            aria-label="Search products"
            className="w-full bg-transparent text-sm text-white outline-none placeholder:text-slate-500"
          />
        </label>

        <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-violet-300">
              Handpicked for you
            </p>
            <h2 className="mt-2 text-2xl font-bold sm:text-3xl">
              Trending right now
            </h2>
            <p className="mt-2 text-sm text-slate-400">
              Save the products you love to your Wishlist.
            </p>
          </div>

          <Link
            to="/products"
            className="inline-flex items-center gap-2 text-sm font-semibold text-violet-300 hover:text-violet-200"
          >
            View all products
            <ArrowRight size={16} />
          </Link>
        </div>

        {/* CATEGORY FILTERS */}
        <div className="mb-5 flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => setActiveCategory("All")}
            className={`rounded-full px-4 py-2 text-xs font-semibold transition ${
              activeCategory === "All"
                ? "bg-violet-600 text-white"
                : "border border-white/10 bg-white/[0.03] text-slate-300"
            }`}
          >
            All products
          </button>

          {categories.map((category) => (
            <button
              key={category}
              type="button"
              onClick={() => setActiveCategory(category)}
              className={`rounded-full px-4 py-2 text-xs font-semibold transition ${
                activeCategory === category
                  ? "bg-violet-600 text-white"
                  : "border border-white/10 bg-white/[0.03] text-slate-300"
              }`}
            >
              {category}
            </button>
          ))}
        </div>

        {productsLoading ? (
          <div className="flex items-center justify-center gap-3 rounded-2xl border border-white/10 bg-white/[0.03] py-16 text-slate-300">
            <LoaderCircle className="animate-spin" size={22} />
            Loading products...
          </div>
        ) : productsError ? (
          <div
            role="alert"
            className="rounded-2xl border border-red-400/20 bg-red-400/10 p-6 text-red-200"
          >
            {productsError}
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="ml-3 underline"
            >
              Retry
            </button>
          </div>
        ) : featuredProducts.length === 0 ? (
          <div className="rounded-2xl border border-white/10 bg-white/[0.03] px-5 py-12 text-center">
            <Package size={38} className="mx-auto text-violet-300" />
            <h3 className="mt-4 text-lg font-semibold">
              No matching products
            </h3>
            <p className="mt-2 text-sm text-slate-400">
              Try another search or category.
            </p>
            <button
              type="button"
              onClick={() => {
                setSearch("");
                setActiveCategory("All");
              }}
              className="mt-4 text-sm font-semibold text-violet-300 hover:text-white"
            >
              Clear filters
            </button>
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {featuredProducts.map((product) => {
              const wished = isInWishlist(product.id);
              const actionPending =
                wishlistActionId === String(product.id);

              return (
                <article
                  key={product.id}
                  className="group overflow-hidden rounded-2xl border border-white/10 bg-[#0b1022] transition duration-300 hover:-translate-y-1 hover:border-violet-400/40 hover:shadow-xl hover:shadow-violet-950/25"
                >
                  <div className="relative flex h-52 items-center justify-center overflow-hidden bg-gradient-to-br from-violet-500/10 via-slate-900 to-indigo-500/10">
                    {product.imageUrl ? (
                      <img
                        src={product.imageUrl}
                        alt={product.name || "ASTRA product"}
                        loading="lazy"
                        className="h-full w-full object-cover transition duration-500 group-hover:scale-105"
                      />
                    ) : (
                      <Package
                        size={52}
                        className="text-violet-300/60"
                      />
                    )}

                    <span className="absolute left-3 top-3 rounded-full border border-white/10 bg-[#07091a]/80 px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider text-violet-200">
                      {product.category || "ASTRA collection"}
                    </span>

                    <button
                      type="button"
                      onClick={() => toggleWishlist(product)}
                      disabled={actionPending}
                      aria-label={
                        wished
                          ? `Remove ${product.name} from Wishlist`
                          : `Add ${product.name} to Wishlist`
                      }
                      title={
                        wished
                          ? "Remove from Wishlist"
                          : "Add to Wishlist"
                      }
                      className={`absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-full border backdrop-blur transition disabled:cursor-wait disabled:opacity-60 ${
                        wished
                          ? "border-pink-400/50 bg-pink-500/20 text-pink-300"
                          : "border-white/20 bg-black/40 text-white hover:border-pink-400/50 hover:text-pink-300"
                      }`}
                    >
                      {actionPending ? (
                        <LoaderCircle
                          size={17}
                          className="animate-spin"
                        />
                      ) : (
                        <Heart
                          size={17}
                          fill={wished ? "currentColor" : "none"}
                        />
                      )}
                    </button>
                  </div>

                  <div className="p-4">
                    <h3 className="truncate font-semibold">
                      {product.name || "ASTRA product"}
                    </h3>

                    <p className="mt-1.5 line-clamp-2 min-h-10 text-xs leading-5 text-slate-400">
                      {product.description ||
                        "Discover this product from the ASTRA collection."}
                    </p>

                    <div className="mt-4 flex items-center justify-between gap-2">
                      <span className="text-lg font-extrabold">
                        {formatPrice(
                          product.discountedPrice ?? product.price
                        )}
                      </span>

                      <span
                        className={`inline-flex items-center gap-1.5 text-[11px] ${
                          Number(product.stockQuantity) > 0
                            ? "text-emerald-300"
                            : "text-slate-500"
                        }`}
                      >
                        <span
                          className={`h-1.5 w-1.5 rounded-full ${
                            Number(product.stockQuantity) > 0
                              ? "bg-emerald-400"
                              : "bg-slate-500"
                          }`}
                        />
                        {Number(product.stockQuantity) > 0
                          ? "In stock"
                          : "Out of stock"}
                      </span>
                    </div>

                    <div className="mt-4">
                      <Button onClick={() => navigate("/products")}>
                        <span>View product</span>
                        <ArrowRight size={15} />
                      </Button>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        )}

        {!productsLoading &&
          !productsError &&
          visibleProducts.length > 8 && (
            <p className="mt-5 text-center text-xs text-slate-500">
              Showing 8 of {visibleProducts.length} matching products.
            </p>
          )}
      </section>

      {/* WISHLIST POPUP */}
      {showWishlist && (
        <div
          className="fixed inset-0 z-[100] flex justify-end bg-black/70 backdrop-blur-sm"
          onClick={(event) => {
            if (event.target === event.currentTarget) {
              setShowWishlist(false);
            }
          }}
        >
          <aside
            role="dialog"
            aria-modal="true"
            aria-labelledby="wishlist-title"
            className="flex h-full w-full max-w-lg flex-col border-l border-white/10 bg-[#080b1c] shadow-2xl"
          >
            <div className="flex items-center justify-between border-b border-white/10 px-5 py-5">
              <div>
                <h2
                  id="wishlist-title"
                  className="flex items-center gap-2 text-xl font-bold"
                >
                  <Heart className="text-pink-300" size={22} />
                  My Wishlist
                </h2>
                <p className="mt-1 text-xs text-slate-400">
                  {wishlist.length} saved{" "}
                  {wishlist.length === 1 ? "product" : "products"}
                </p>
              </div>

              <button
                type="button"
                onClick={() => setShowWishlist(false)}
                aria-label="Close Wishlist"
                className="rounded-xl p-2 text-slate-400 transition hover:bg-white/10 hover:text-white"
              >
                <X size={22} />
              </button>
            </div>

            {wishlistError && (
              <div
                role="alert"
                className="mx-5 mt-4 rounded-xl border border-red-400/20 bg-red-400/10 p-3 text-sm text-red-200"
              >
                {wishlistError}
                <button
                  type="button"
                  onClick={() => setWishlistError("")}
                  className="ml-2 underline"
                >
                  Dismiss
                </button>
              </div>
            )}

            <div className="flex-1 overflow-y-auto p-5">
              {!user ? (
                <div className="py-16 text-center">
                  <Heart
                    size={42}
                    className="mx-auto text-pink-300/70"
                  />
                  <h3 className="mt-4 text-lg font-bold">
                    Login to view your Wishlist
                  </h3>
                  <p className="mt-2 text-sm text-slate-400">
                    Sign in to save your favourite products to your account.
                  </p>
                  <div className="mt-6">
                    <Button
                      onClick={() => {
                        setShowWishlist(false);
                        navigate("/login");
                      }}
                    >
                      Login
                    </Button>
                  </div>
                </div>
              ) : wishlistLoading ? (
                <div className="flex items-center justify-center gap-3 py-16 text-slate-300">
                  <LoaderCircle
                    size={22}
                    className="animate-spin"
                  />
                  Loading your Wishlist...
                </div>
              ) : wishlist.length === 0 ? (
                <div className="py-16 text-center">
                  <Heart
                    size={44}
                    className="mx-auto text-pink-300/70"
                  />
                  <h3 className="mt-4 text-lg font-bold">
                    Your Wishlist is empty
                  </h3>
                  <p className="mt-2 text-sm text-slate-400">
                    Tap the heart on a product to save it here.
                  </p>
                  <button
                    type="button"
                    onClick={() => setShowWishlist(false)}
                    className="mt-6 rounded-xl bg-violet-600 px-5 py-3 text-sm font-bold transition hover:bg-violet-500"
                  >
                    Explore products
                  </button>
                </div>
              ) : (
                <div className="space-y-3">
                  {wishlist.map((item) => {
                    const product = getWishlistDisplayProduct(item);
                    const productId = getWishlistProductId(item);
                    const pending =
                      wishlistActionId === String(productId);

                    return (
                      <article
                        key={String(productId)}
                        className="flex gap-4 rounded-2xl border border-white/10 bg-white/[0.035] p-3"
                      >
                        <div className="flex h-24 w-24 shrink-0 items-center justify-center overflow-hidden rounded-xl bg-white/5">
                          {product.imageUrl ? (
                            <img
                              src={product.imageUrl}
                              alt={product.name}
                              className="h-full w-full object-cover"
                            />
                          ) : (
                            <Package
                              size={30}
                              className="text-violet-300/70"
                            />
                          )}
                        </div>

                        <div className="min-w-0 flex-1">
                          <h3 className="truncate text-sm font-bold">
                            {product.name}
                          </h3>

                          {product.category && (
                            <p className="mt-1 text-xs text-slate-400">
                              {product.category}
                            </p>
                          )}

                          <p className="mt-2 font-extrabold">
                            {formatPrice(
                              product.discountedPrice ?? product.price
                            )}
                          </p>

                          <div className="mt-3 flex flex-wrap items-center gap-3">
                            <button
                              type="button"
                              disabled={pending}
                              onClick={() =>
                                toggleWishlist({ id: productId })
                              }
                              className="inline-flex items-center gap-1.5 text-xs font-semibold text-pink-300 transition hover:text-pink-200 disabled:opacity-50"
                            >
                              {pending ? (
                                <LoaderCircle
                                  size={14}
                                  className="animate-spin"
                                />
                              ) : (
                                <Trash2 size={14} />
                              )}
                              Remove
                            </button>

                            <button
                              type="button"
                              onClick={() => {
                                setShowWishlist(false);
                                navigate("/products");
                              }}
                              className="inline-flex items-center gap-1 text-xs font-semibold text-violet-300 hover:text-violet-200"
                            >
                              View products
                              <ArrowRight size={13} />
                            </button>
                          </div>
                        </div>
                      </article>
                    );
                  })}
                </div>
              )}
            </div>

            <div className="border-t border-white/10 p-5">
              <button
                type="button"
                onClick={() => setShowWishlist(false)}
                className="w-full rounded-xl border border-white/10 px-4 py-3 text-sm font-bold transition hover:bg-white/5"
              >
                Continue shopping
              </button>
            </div>
          </aside>
        </div>
      )}
    </main>
  );
}

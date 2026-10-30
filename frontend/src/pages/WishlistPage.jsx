import { useState } from "react";
import { Link } from "react-router-dom";
import {
  ArrowLeft,
  Heart,
  Package,
  RefreshCw,
  ShoppingCart,
  Trash2,
} from "lucide-react";

import { useWishlist } from "../context/useWishlist.js";

function money(amount) {
  return `₹${Number(amount || 0).toLocaleString("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function getPrice(item) {
  if (item.discountedPrice != null) {
    return Number(item.discountedPrice);
  }

  const price = Number(item.price || 0);
  const discount = Number(item.discountPercentage || 0);

  return price * (1 - discount / 100);
}

export default function WishlistPage() {
  const {
    items,
    loading,
    loadError,
    refresh,
    removeItem,
  } = useWishlist();

  const [removingId, setRemovingId] = useState(null);
  const [actionError, setActionError] = useState("");

  async function handleRemove(productId) {
    setRemovingId(productId);
    setActionError("");

    try {
      await removeItem(productId);
    } catch (error) {
      setActionError(
        error.response?.data?.detail ||
          error.response?.data?.message ||
          error.message ||
          "Unable to remove this product. Please try again."
      );
    } finally {
      setRemovingId(null);
    }
  }

  return (
    <div className="min-h-[65vh] bg-slate-950 text-white">
      <section className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <div className="mb-8 flex items-start gap-4">
          <div className="rounded-2xl border border-pink-400/20 bg-pink-500/10 p-3 text-pink-300">
            <Heart size={25} fill="currentColor" />
          </div>

          <div>
            <p className="text-xs font-bold uppercase tracking-[0.25em] text-pink-300">
              Saved for later
            </p>

            <h1 className="mt-2 text-3xl font-extrabold tracking-tight sm:text-4xl">
              My Wishlist
            </h1>

            <p className="mt-2 text-sm text-slate-400">
              {items.length}{" "}
              {items.length === 1 ? "product" : "products"}{" "}
              saved to your account.
            </p>
          </div>
        </div>

        <div className="mb-6 flex flex-wrap items-center gap-3">
          <Link
            to="/products"
            className="inline-flex items-center gap-2 rounded-xl border border-white/10 px-4 py-2.5 text-sm font-semibold text-slate-300 transition hover:bg-white/5 hover:text-white"
          >
            <ArrowLeft size={17} />
            Continue shopping
          </Link>

          <Link
            to="/cart"
            aria-label="My cart"
            className="inline-flex items-center gap-2 rounded-xl border border-violet-400/30 bg-violet-500/10 px-4 py-2.5 text-sm font-semibold text-violet-200 transition hover:bg-violet-500/20"
          >
            <ShoppingCart size={18} />
            My Cart
          </Link>
        </div>

        {actionError && (
          <div
            role="alert"
            className="mb-6 flex items-start justify-between gap-4 rounded-xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200"
          >
            <span>{actionError}</span>

            <button
              type="button"
              onClick={() => setActionError("")}
              aria-label="Dismiss error"
              className="font-bold hover:text-white"
            >
              ×
            </button>
          </div>
        )}

        {loading ? (
          <div className="flex min-h-64 flex-col items-center justify-center text-center">
            <div className="h-10 w-10 animate-spin rounded-full border-4 border-violet-400/25 border-t-violet-400" />

            <p className="mt-4 text-sm text-slate-400">
              Loading your wishlist…
            </p>
          </div>
        ) : loadError ? (
          <div className="rounded-2xl border border-red-400/20 bg-red-400/5 p-8 text-center">
            <p role="alert" className="text-red-200">
              {loadError}
            </p>

            <button
              type="button"
              onClick={refresh}
              className="mt-5 inline-flex items-center gap-2 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold transition hover:bg-violet-500"
            >
              <RefreshCw size={16} />
              Try again
            </button>
          </div>
        ) : items.length === 0 ? (
          <div className="flex min-h-80 flex-col items-center justify-center rounded-3xl border border-dashed border-white/15 bg-white/[0.025] px-6 py-12 text-center">
            <div className="rounded-full bg-white/5 p-5 text-slate-400">
              <Heart size={34} />
            </div>

            <h2 className="mt-5 text-xl font-bold">
              Your wishlist is empty
            </h2>

            <p className="mt-2 max-w-md text-sm leading-6 text-slate-400">
              Tap the heart on any product to save it here.
              Your saved products stay with your account.
            </p>

            <Link
              to="/products"
              className="mt-6 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold transition hover:bg-violet-500"
            >
              Explore products
            </Link>
          </div>
        ) : (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {items.map((item) => {
              const price = getPrice(item);
              const productId = item.productId;

              return (
                <article
                  key={productId}
                  className="flex flex-col overflow-hidden rounded-2xl border border-white/10 bg-white/[0.035] transition hover:border-violet-400/30"
                >
                  <Link
                    to={`/products/${productId}`}
                    aria-label={`View ${item.name} details`}
                    className="flex h-56 items-center justify-center overflow-hidden bg-gradient-to-br from-violet-500/10 via-slate-900 to-indigo-500/10"
                  >
                    {item.imageUrl ? (
                      <img
                        src={item.imageUrl}
                        alt={item.name || "ASTRA product"}
                        loading="lazy"
                        onError={(event) => {
                          event.currentTarget.style.display = "none";
                        }}
                        className="h-full w-full object-cover transition duration-300 hover:scale-105"
                      />
                    ) : (
                      <Package
                        size={58}
                        className="text-violet-300/50"
                      />
                    )}
                  </Link>

                  <div className="flex flex-1 flex-col p-5">
                    <p className="text-xs font-medium uppercase tracking-wide text-violet-300">
                      {item.category || "ASTRA Collection"}
                    </p>

                    <Link
                      to={`/products/${productId}`}
                      className="mt-2 line-clamp-2 font-bold transition hover:text-violet-300"
                    >
                      {item.name || `Product ${productId}`}
                    </Link>

                    <p className="mt-3 text-lg font-extrabold text-violet-200">
                      {money(price)}
                    </p>

                    {item.active === false && (
                      <p className="mt-2 text-xs text-amber-300">
                        This product is currently unavailable.
                      </p>
                    )}

                    <div className="mt-auto pt-5">
                      <button
                        type="button"
                        onClick={() => handleRemove(productId)}
                        disabled={removingId === productId}
                        className="inline-flex w-full items-center justify-center gap-2 rounded-xl border border-red-400/25 bg-red-500/10 px-4 py-3 text-sm font-semibold text-red-200 transition hover:bg-red-500/20 disabled:cursor-wait disabled:opacity-60"
                      >
                        <Trash2 size={16} />
                        {removingId === productId
                          ? "Removing…"
                          : "Remove from wishlist"}
                      </button>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
}
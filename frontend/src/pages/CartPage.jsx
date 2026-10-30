
import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Minus,
  Plus,
  ShoppingBag,
  ShoppingCart,
  Trash2,
} from "lucide-react";
import cartService from "../services/cartService";
import { useAuth } from "../context/useAuth.js";

export default function CartPage() {
  const [cart, setCart] = useState({
    items: [],
    totalItemsPrice: 0,
    totalQuantity: 0,
  });

  const [loading, setLoading] = useState(true);
  const [busyProductId, setBusyProductId] = useState(null);
  const [error, setError] = useState("");

  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  const loadCart = useCallback(async () => {
    setLoading(true);

    try {
      const data = await cartService.getCart();
      setCart(data);
      setError("");
    } catch (err) {
      setError(
        err.response?.data?.detail ||
          err.response?.data?.message ||
          "Unable to load your cart. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (authLoading) return;

    if (!user) {
      navigate("/login", {
        replace: true,
        state: { from: { pathname: "/cart" } },
      });
      return;
    }

    let cancelled = false;

    async function fetchCart() {
      try {
        const data = await cartService.getCart();

        if (!cancelled) {
          setCart(data);
          setError("");
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.data?.detail ||
              err.response?.data?.message ||
              "Unable to load your cart. Please try again."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }


    fetchCart();

    return () => {
      cancelled = true;
    };
  }, [authLoading, user, navigate]);

  async function changeQuantity(item, nextQuantity) {
    if (
      nextQuantity < 1 ||
      nextQuantity > item.stockQuantity ||
      busyProductId !== null
    ) {
      return;
    }

    setBusyProductId(item.productId);
    setError("");

    try {
      const updatedCart = await cartService.updateQuantity(
        item.productId,
        nextQuantity
      );
      setCart(updatedCart);
    } catch (err) {
      setError(
        err.response?.data?.detail ||
          err.response?.data?.message ||
          "Unable to update quantity."
      );
    } finally {
      setBusyProductId(null);
    }
  }

  async function removeItem(productId) {
    if (busyProductId !== null) return;

    setBusyProductId(productId);
    setError("");

    try {
      const updatedCart = await cartService.removeItem(productId);
      setCart(updatedCart);
    } catch (err) {
      setError(
        err.response?.data?.detail ||
          err.response?.data?.message ||
          "Unable to remove this product."
      );
    } finally {
      setBusyProductId(null);
    }
  }

  const money = (amount) =>
    `₹${Number(amount || 0).toLocaleString("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;

  if (authLoading || (loading && user)) {
    return (
      <section className="flex min-h-[60vh] items-center justify-center bg-slate-950 text-white">
        <div className="text-center">
          <ShoppingCart
            size={38}
            className="mx-auto animate-pulse text-violet-400"
          />
          <p className="mt-4 font-medium text-slate-300">
            Loading your cart...
          </p>
        </div>
      </section>
    );
  }

  return (
    <section className="min-h-[60vh] bg-slate-950 text-white">
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10 lg:px-8">
        <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-widest text-violet-300">
              Your ASTRA basket
            </p>
            <h1 className="mt-2 text-3xl font-extrabold tracking-tight sm:text-4xl">
              Shopping Cart
            </h1>
            <p className="mt-3 text-slate-400">
              {cart.totalQuantity} item(s) in your cart
            </p>
          </div>

          <Link
            to="/products"
            className="inline-flex items-center gap-2 rounded-xl border border-violet-400/30 bg-violet-500/10 px-4 py-2.5 text-sm font-semibold text-violet-200 transition hover:border-violet-400/60 hover:bg-violet-500/20"
          >
            <ArrowLeft size={17} />
            Continue shopping
          </Link>
        </div>

        {error && (
          <div
            role="alert"
            className="mb-6 rounded-xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200"
          >
            {error}
            <button
              type="button"
              onClick={loadCart}
              className="ml-3 font-semibold underline hover:text-white"
            >
              Retry
            </button>
          </div>
        )}

        {!loading && cart.items.length === 0 ? (
          <div className="rounded-3xl border border-white/10 bg-white/[0.04] px-6 py-16 text-center shadow-xl shadow-black/10">
            <div className="mx-auto flex h-24 w-24 items-center justify-center rounded-3xl border border-violet-400/20 bg-violet-500/10">
              <ShoppingBag size={48} className="text-violet-300" />
            </div>

            <h2 className="mt-6 text-2xl font-bold">
              Your cart is empty
            </h2>

            <p className="mx-auto mt-3 max-w-md text-slate-400">
              Discover something you love and add your favorite products
              to your ASTRA shopping cart.
            </p>

            <Link
              to="/products"
              className="mt-7 inline-flex items-center gap-2 rounded-xl bg-violet-600 px-5 py-3 font-semibold text-white transition hover:bg-violet-500"
            >
              <ShoppingCart size={18} />
              Browse products
            </Link>
          </div>
        ) : (
          <div className="grid items-start gap-8 lg:grid-cols-[1fr_350px]">
            <div className="space-y-4">
              {cart.items.map((item) => (
                <article
                  key={item.productId}
                  className="flex flex-col gap-4 rounded-2xl border border-white/10 bg-white/[0.04] p-4 shadow-sm transition hover:border-violet-400/30 sm:flex-row sm:items-center sm:p-5"
                >
                  <div className="flex h-32 w-full shrink-0 items-center justify-center overflow-hidden rounded-xl border border-white/5 bg-gradient-to-br from-violet-500/10 via-slate-900 to-indigo-500/10 sm:h-28 sm:w-28">
                    {item.imageUrl ? (
                      <img
                        src={item.imageUrl}
                        alt={item.name || item.productName}
                        loading="lazy"
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <ShoppingBag
                        size={36}
                        className="text-violet-300/60"
                      />
                    )}
                  </div>

                  <div className="min-w-0 flex-1">
                    <p className="text-xs font-semibold uppercase tracking-wide text-violet-300">
                      {item.category || "ASTRA Collection"}
                    </p>

                    <h2 className="mt-2 text-lg font-bold text-white">
                      {item.name || item.productName}
                    </h2>

                    <p className="mt-2 text-sm text-slate-400">
                      Unit price: {money(item.price)}
                    </p>

                    <p className="mt-1 font-semibold text-violet-200">
                      Item total: {money(item.lineTotal)}
                    </p>
                  </div>

                  <div className="flex flex-wrap items-center justify-between gap-4 sm:flex-col sm:items-end">
                    <div className="inline-flex items-center rounded-xl border border-white/10 bg-slate-900/70">
                      <button
                        type="button"
                        aria-label={`Decrease quantity of ${item.name || item.productName}`}
                        disabled={
                          busyProductId !== null || item.quantity <= 1
                        }
                        onClick={() =>
                          changeQuantity(item, item.quantity - 1)
                        }
                        className="rounded-l-xl p-2.5 transition hover:bg-violet-500/10 disabled:cursor-not-allowed disabled:opacity-40"
                      >
                        <Minus size={16} />
                      </button>

                      <span className="min-w-10 text-center text-sm font-semibold">
                        {item.quantity}
                      </span>

                      <button
                        type="button"
                        aria-label={`Increase quantity of ${item.name || item.productName}`}
                        disabled={
                          busyProductId !== null ||
                          item.quantity >= item.stockQuantity
                        }
                        onClick={() =>
                          changeQuantity(item, item.quantity + 1)
                        }
                        className="rounded-r-xl p-2.5 transition hover:bg-violet-500/10 disabled:cursor-not-allowed disabled:opacity-40"
                      >
                        <Plus size={16} />
                      </button>
                    </div>

                    <button
                      type="button"
                      disabled={busyProductId !== null}
                      onClick={() => removeItem(item.productId)}
                      className="inline-flex items-center gap-2 rounded-lg px-2 py-1 text-sm font-medium text-red-400 transition hover:bg-red-400/10 hover:text-red-300 disabled:opacity-50"
                    >
                      <Trash2 size={16} />
                      Remove
                    </button>
                  </div>
                </article>
              ))}
            </div>

            <aside className="rounded-2xl border border-violet-400/20 bg-gradient-to-br from-violet-500/[0.08] to-white/[0.03] p-6 shadow-xl shadow-black/10 lg:sticky lg:top-6">
              <p className="text-xs font-semibold uppercase tracking-widest text-violet-300">
                ASTRA Checkout
              </p>

              <h2 className="mt-2 text-xl font-bold">Order Summary</h2>

              <div className="mt-6 space-y-4 text-sm">
                <div className="flex justify-between gap-4 text-slate-300">
                  <span>Items ({cart.totalQuantity})</span>
                  <span>{money(cart.totalItemsPrice)}</span>
                </div>

                <div className="flex justify-between gap-4 text-slate-300">
                  <span>Shipping</span>
                  <span className="text-slate-400">At checkout</span>
                </div>

                <div className="border-t border-white/10 pt-5">
                  <div className="flex items-center justify-between gap-4">
                    <span className="font-semibold text-white">Subtotal</span>
                    <span className="text-xl font-extrabold text-violet-200">
                      {money(cart.totalItemsPrice)}
                    </span>
                  </div>

                  <p className="mt-3 text-xs leading-5 text-slate-400">
                    Shipping charges and applicable taxes are not included.
                  </p>
                </div>
              </div>

              <Link
                to="/checkout"
                className="mt-6 inline-flex w-full items-center justify-center rounded-xl bg-violet-600 px-5 py-3.5 font-bold text-white transition hover:bg-violet-500"
              >
                Proceed to checkout
              </Link>

              <Link
                to="/products"
                className="mt-4 block text-center text-sm font-semibold text-violet-300 transition hover:text-violet-200"
              >
                Continue shopping
              </Link>

              <div className="mt-6 flex items-center justify-center gap-2 border-t border-white/10 pt-5 text-xs text-slate-400">
                <ShoppingBag size={15} className="text-violet-300" />
                Secure shopping with ASTRA
              </div>
            </aside>
          </div>
        )}
      </div>
    </section>
  );
}

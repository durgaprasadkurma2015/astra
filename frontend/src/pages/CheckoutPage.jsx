
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CreditCard,
  MapPin,
  ShoppingBag,
  Truck,
  Tag,
  X,
} from "lucide-react";

import cartService from "../services/cartService";
import couponService from "../services/couponService";
import orderService from "../services/orderService";
import addressService from "../services/addressService";
import { useAuth } from "../context/useAuth.js";

function loadRazorpayCheckout() {
  if (window.Razorpay) return Promise.resolve(true);
  return new Promise((resolve) => {
    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.async = true;
    script.onload = () => resolve(Boolean(window.Razorpay));
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

function getErrorMessage(err, fallback) {
  return (
    err.response?.data?.detail ||
    err.response?.data?.message ||
    err.response?.data?.title ||
    err.message ||
    fallback
  );
}

export default function CheckoutPage() {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  const [cart, setCart] = useState({
    items: [],
    totalItemsPrice: 0,
    totalQuantity: 0,
  });

  const [customerName, setCustomerName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [shippingAddress, setShippingAddress] = useState("");
  const [paymentMethod, setPaymentMethod] = useState("COD");
  const [savedAddresses, setSavedAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState("");

  const [couponCode, setCouponCode] = useState("");
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [couponLoading, setCouponLoading] = useState(false);
  const [couponError, setCouponError] = useState("");
  const [couponMessage, setCouponMessage] = useState("");

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (authLoading) return;

    if (!user) {
      navigate("/login", {
        replace: true,
        state: { from: { pathname: "/checkout" } },
      });
      return;
    }

    let cancelled = false;

    async function loadCart() {
      try {
        const data = await cartService.getCart();

        if (cancelled) return;

        setCart(data);

        try {
          const saved = await addressService.list();
          if (!cancelled) {
            setSavedAddresses(saved);
            const preferred = saved.find((address) => address.defaultAddress);
            if (preferred) {
              setSelectedAddressId(String(preferred.id));
              setCustomerName(preferred.fullName || "");
              setPhoneNumber(preferred.phoneNumber || "");
              setShippingAddress([
                preferred.addressLine1,
                preferred.addressLine2,
                preferred.landmark,
                preferred.city,
                preferred.district,
                preferred.state,
                preferred.postalCode,
                preferred.country,
              ].filter(Boolean).join(", "));
            }
          }
        } catch {
          // Saved addresses are optional; customers can still enter delivery details.
        }

        if (!data.items?.length) {
          navigate("/cart", { replace: true });
        }
      } catch (err) {
        if (!cancelled) {
          setError(getErrorMessage(err, "Unable to load your cart."));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadCart();

    return () => {
      cancelled = true;
    };
  }, [user, authLoading, navigate]);

  function money(amount) {
    return `₹${Number(amount || 0).toLocaleString("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  }

  async function handleApplyCoupon() {
    const code = couponCode.trim();

    if (!code) {
      setCouponError("Please enter a coupon code.");
      setCouponMessage("");
      return;
    }

    setCouponLoading(true);
    setCouponError("");
    setCouponMessage("");
    setError("");

    try {
      const result = await couponService.applyCoupon(
        code,
        Number(cart.totalItemsPrice || 0)
      );

      setAppliedCoupon(result);
      setCouponCode(result.couponCode || code.toUpperCase());
      setCouponMessage(
        result.message || "Coupon applied successfully."
      );
    } catch (err) {
      setAppliedCoupon(null);
      setCouponError(
        getErrorMessage(
          err,
          "Unable to apply this coupon. Please try again."
        )
      );
    } finally {
      setCouponLoading(false);
    }
  }

  function handleRemoveCoupon() {
    setAppliedCoupon(null);
    setCouponCode("");
    setCouponError("");
    setCouponMessage("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (
      !customerName.trim() ||
      !phoneNumber.trim() ||
      !shippingAddress.trim()
    ) {
      setError("Please complete all delivery details.");
      return;
    }

    if (!/^[0-9+\-\s()]{7,20}$/.test(phoneNumber.trim())) {
      setError("Please enter a valid phone number.");
      return;
    }

    if (submitting) return;

    setSubmitting(true);
    let paymentModalOpened = false;

    try {
      const order = await orderService.checkout(
        customerName.trim(),
        phoneNumber.trim(),
        shippingAddress.trim(),
        appliedCoupon?.couponCode || "",
        paymentMethod
      );

      if (!order?.orderNumber) {
        throw new Error(
          "The server did not return an order number. Please check My Orders before retrying."
        );
      }

      if (paymentMethod === "RAZORPAY") {
        if (!order.razorpayOrderId || !order.razorpayKeyId) {
          throw new Error("Online payment is not configured. Please choose Cash on Delivery.");
        }
        if (!(await loadRazorpayCheckout())) {
          throw new Error("Unable to load secure payment checkout. Please try again.");
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
              await orderService.verifyRazorpayPayment(order.orderNumber, payment);
              navigate(`/order-confirmation/${encodeURIComponent(order.orderNumber)}`, { replace: true });
            } catch (verifyError) {
              setError(getErrorMessage(verifyError, "Payment could not be verified. Check My Orders before retrying."));
            } finally {
              setSubmitting(false);
            }
          },
          modal: {
            ondismiss: () => {
              setError("Payment was not completed. Your order is saved with payment pending; retry is available from My Orders.");
              setSubmitting(false);
            },
          },
          theme: { color: "#7c3aed" },
        });
        checkout.on("payment.failed", (event) => {
          setError(event.error?.description || "Payment failed. Your order remains pending.");
          setSubmitting(false);
        });
        paymentModalOpened = true;
        checkout.open();
      } else {
        navigate(`/order-confirmation/${encodeURIComponent(order.orderNumber)}`, { replace: true });
      }
    } catch (err) {
      paymentModalOpened = false;
      setError(
        getErrorMessage(
          err,
          "Order could not be placed. Please try again."
        )
      );
    } finally {
      if (!paymentModalOpened) setSubmitting(false);
    }
  }

  const subtotal = Number(cart.totalItemsPrice || 0);

  // Preview only. The server calculates the final total during checkout.
  const discount = appliedCoupon
    ? Number(appliedCoupon.discountAmount || 0)
    : 0;

  const total = appliedCoupon
    ? Number(appliedCoupon.totalAmount ?? subtotal - discount)
    : subtotal;

  if (authLoading || (loading && user)) {
    return (
      <section className="flex min-h-[60vh] items-center justify-center bg-slate-950 text-white">
        <p className="font-medium text-slate-300">
          Loading checkout...
        </p>
      </section>
    );
  }

  return (
    <section className="min-h-[60vh] bg-slate-950 text-white">
      <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 sm:py-10">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-widest text-violet-300">
              Almost there
            </p>

            <h1 className="mt-2 text-3xl font-extrabold sm:text-4xl">
              Checkout
            </h1>

            <p className="mt-2 text-slate-400">
              Enter your delivery details to place your order.
            </p>
          </div>

          <Link
            to="/cart"
            className="inline-flex items-center gap-2 rounded-xl border border-white/10 px-4 py-2.5 text-sm text-slate-300 transition hover:border-violet-400/40 hover:text-violet-300"
          >
            <ArrowLeft size={17} />
            Back to cart
          </Link>
        </div>

        {error && (
          <div
            role="alert"
            className="mt-6 rounded-xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200"
          >
            {error}
          </div>
        )}

        <form
          onSubmit={handleSubmit}
          className="mt-8 grid gap-8 lg:grid-cols-[1.3fr_0.7fr]"
        >
          <div className="space-y-6">
            <section className="rounded-2xl border border-white/10 bg-white/[0.035] p-6">
              <div className="mb-6 flex items-center gap-3">
                <MapPin className="text-violet-300" size={22} />

                <div>
                  <h2 className="text-lg font-bold">
                    Delivery details
                  </h2>
                  <p className="text-sm text-slate-400">
                    Where should we deliver your order?
                  </p>
                </div>
              </div>

              <div className="space-y-5">
                {savedAddresses.length > 0 && (
                  <div>
                    <label htmlFor="savedAddress" className="mb-2 block text-sm text-slate-300">
                      Use a saved address
                    </label>
                    <select
                      id="savedAddress"
                      value={selectedAddressId}
                      onChange={(event) => {
                        const id = event.target.value;
                        setSelectedAddressId(id);
                        const selected = savedAddresses.find((address) => String(address.id) === id);
                        if (selected) {
                          setCustomerName(selected.fullName || "");
                          setPhoneNumber(selected.phoneNumber || "");
                          setShippingAddress([
                            selected.addressLine1, selected.addressLine2,
                            selected.landmark, selected.city, selected.district,
                            selected.state, selected.postalCode, selected.country,
                          ].filter(Boolean).join(", "));
                        }
                      }}
                      className="w-full rounded-xl border border-white/10 bg-slate-900 px-4 py-3 text-white outline-none focus:border-violet-400"
                    >
                      <option value="">Enter a different address</option>
                      {savedAddresses.map((address) => (
                        <option key={address.id} value={address.id}>
                          {address.label} — {address.addressLine1}, {address.city}
                        </option>
                      ))}
                    </select>
                  </div>
                )}
                <div>
                  <label
                    htmlFor="customerName"
                    className="mb-2 block text-sm text-slate-300"
                  >
                    Full name
                  </label>

                  <input
                    id="customerName"
                    required
                    maxLength={150}
                    value={customerName}
                    onChange={(event) => { setSelectedAddressId(""); setCustomerName(event.target.value); }}
                    autoComplete="name"
                    placeholder="Enter your full name"
                    className="w-full rounded-xl border border-white/10 bg-slate-900 px-4 py-3 outline-none focus:border-violet-400"
                  />
                </div>

                <div>
                  <label
                    htmlFor="phoneNumber"
                    className="mb-2 block text-sm text-slate-300"
                  >
                    Phone number
                  </label>

                  <input
                    id="phoneNumber"
                    required
                    maxLength={20}
                    value={phoneNumber}
                    onChange={(event) => { setSelectedAddressId(""); setPhoneNumber(event.target.value); }}
                    autoComplete="tel"
                    inputMode="tel"
                    placeholder="Enter contact number"
                    className="w-full rounded-xl border border-white/10 bg-slate-900 px-4 py-3 outline-none focus:border-violet-400"
                  />
                </div>

                <div>
                  <label
                    htmlFor="shippingAddress"
                    className="mb-2 block text-sm text-slate-300"
                  >
                    Complete shipping address
                  </label>

                  <textarea
                    id="shippingAddress"
                    required
                    maxLength={2000}
                    rows={4}
                    value={shippingAddress}
                    onChange={(event) => { setSelectedAddressId(""); setShippingAddress(event.target.value); }}
                    autoComplete="street-address"
                    placeholder="House number, street, city, state, PIN code"
                    className="w-full resize-y rounded-xl border border-white/10 bg-slate-900 px-4 py-3 outline-none focus:border-violet-400"
                  />
                </div>
              </div>
            </section>

            <section className="rounded-2xl border border-white/10 bg-white/[0.035] p-6">
              <div className="flex items-center gap-3">
                <Truck className="text-violet-300" size={22} />

                <div>
                  <h2 className="font-bold">Payment method</h2>
                  <p className="mt-1 text-sm text-slate-400">Choose how you want to pay.</p>
                </div>
              </div>

              <div className="mt-5 grid gap-3 sm:grid-cols-2">
                {[
                  { value: "COD", label: "Cash on Delivery", note: "Pay when your order arrives", icon: Truck },
                  { value: "RAZORPAY", label: "Pay Online", note: "Cards, UPI and supported methods", icon: CreditCard },
                ].map(({ value, label, note, icon: Icon }) => (
                  <label key={value} className={`flex cursor-pointer gap-3 rounded-xl border p-4 transition ${paymentMethod === value ? "border-violet-400 bg-violet-500/10" : "border-white/10 hover:border-white/25"}`}>
                    <input type="radio" name="paymentMethod" value={value} checked={paymentMethod === value} onChange={() => setPaymentMethod(value)} className="mt-1 accent-violet-500" />
                    <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/5"><Icon size={18} className="text-violet-300" /></span>
                    <span><span className="block text-sm font-semibold">{label}</span><span className="mt-1 block text-xs text-slate-400">{note}</span></span>
                  </label>
                ))}
              </div>
            </section>
          </div>

          <aside className="h-fit rounded-2xl border border-white/10 bg-white/[0.035] p-6">
            <div className="flex items-center gap-3">
              <ShoppingBag className="text-violet-300" size={22} />
              <h2 className="text-lg font-bold">Order summary</h2>
            </div>

            <div className="mt-6 space-y-4">
              {cart.items?.map((item, index) => (
                <div
                  key={item.productId ?? item.id ?? index}
                  className="flex justify-between gap-4 text-sm"
                >
                  <div>
                    <p className="font-medium text-slate-200">
                      {item.name || item.productName || "Product"}
                    </p>
                    <p className="mt-1 text-slate-400">
                      Qty: {item.quantity}
                    </p>
                  </div>

                  <p className="shrink-0 font-semibold">
                    {money(item.lineTotal)}
                  </p>
                </div>
              ))}
            </div>

            <div className="mt-6 border-t border-white/10 pt-5">
              <div className="mb-3 flex items-center gap-2">
                <Tag size={18} className="text-violet-300" />
                <h3 className="font-semibold">Apply coupon</h3>
              </div>

              {appliedCoupon ? (
                <div className="rounded-xl border border-emerald-400/30 bg-emerald-400/10 p-4">
                  <div className="flex items-center justify-between gap-3">
                    <div>
                      <p className="font-bold text-emerald-300">
                        {appliedCoupon.couponCode}
                      </p>
                      <p className="mt-1 text-sm text-emerald-200">
                        You save {money(discount)}
                      </p>
                    </div>

                    <button
                      type="button"
                      onClick={handleRemoveCoupon}
                      disabled={submitting}
                      aria-label="Remove coupon"
                      className="rounded-lg p-2 text-slate-300 transition hover:bg-white/10 hover:text-white disabled:opacity-50"
                    >
                      <X size={18} />
                    </button>
                  </div>

                  {couponMessage && (
                    <p className="mt-2 text-xs text-emerald-200">
                      {couponMessage}
                    </p>
                  )}
                </div>
              ) : (
                <div className="flex gap-2">
                  <input
                    type="text"
                    aria-label="Coupon code"
                    maxLength={50}
                    value={couponCode}
                    onChange={(event) => {
                      setCouponCode(event.target.value);
                      setCouponError("");
                      setCouponMessage("");
                      setAppliedCoupon(null);
                    }}
                    placeholder="Enter coupon code"
                    className="min-w-0 flex-1 rounded-xl border border-white/10 bg-slate-900 px-3 py-3 text-sm uppercase outline-none focus:border-violet-400"
                  />

                  <button
                    type="button"
                    onClick={handleApplyCoupon}
                    disabled={couponLoading || submitting}
                    className="rounded-xl bg-violet-600 px-4 py-3 text-sm font-bold transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    {couponLoading ? "Applying..." : "Apply"}
                  </button>
                </div>
              )}

              {couponError && (
                <p role="alert" className="mt-3 text-sm text-red-300">
                  {couponError}
                </p>
              )}
            </div>

            <div className="mt-6 space-y-3 border-t border-white/10 pt-5">
              <div className="flex justify-between gap-4 text-sm text-slate-400">
                <span>Items ({cart.totalQuantity || 0})</span>
                <span>{money(subtotal)}</span>
              </div>

              <div className="flex justify-between gap-4 text-sm text-slate-400">
                <span>Shipping</span>
                <span>Free</span>
              </div>

              {appliedCoupon && (
                <div className="flex justify-between gap-4 text-sm text-emerald-300">
                  <span>Coupon discount</span>
                  <span>-{money(discount)}</span>
                </div>
              )}

              <div className="flex justify-between gap-4 border-t border-white/10 pt-4 text-lg font-extrabold">
                <span>Total</span>
                <span className="text-violet-200">{money(total)}</span>
              </div>
            </div>

            <button
              type="submit"
              disabled={
                submitting ||
                couponLoading ||
                !cart.items?.length
              }
              className="mt-6 w-full rounded-xl bg-violet-600 px-5 py-3.5 font-bold transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {submitting ? "Processing order..." : paymentMethod === "RAZORPAY" ? "Continue to payment" : "Place order"}
            </button>

            <p className="mt-3 text-center text-xs leading-5 text-slate-500">
              The server verifies product prices, stock, and coupon
              validity when you place your order.
            </p>
          </aside>
        </form>
      </div>
    </section>
  );
}

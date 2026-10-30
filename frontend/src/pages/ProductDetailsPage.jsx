
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  Minus,
  Plus,
  Package,
  ShoppingCart,
  CheckCircle2,
  Star,
} from "lucide-react";

import WishlistButton from "../components/common/WishlistButton";
import productService from "../services/productService";
import productReviewService from "../services/productReviewService";
import cartService from "../services/cartService";
import { useAuth } from "../context/useAuth.js";

export default function ProductDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, loading: authLoading } = useAuth();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [reviews, setReviews] = useState([]);
  const [reviewSummary, setReviewSummary] = useState(null);
  const [reviewsLoading, setReviewsLoading] = useState(true);
  const [reviewError, setReviewError] = useState("");
  const [reviewMessage, setReviewMessage] = useState("");
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [submittingReview, setSubmittingReview] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function loadProduct() {
      setLoading(true);
      setError("");
      setProduct(null);
      setMessage("");

      try {
        const data = await productService.getProduct(id);

        if (!cancelled) {
          setProduct(data);
          setQuantity(1);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.status === 404
              ? "This product was not found or is no longer available."
              : "Unable to load this product. Please try again."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadProduct();

    return () => {
      cancelled = true;
    };
  }, [id]);

  useEffect(() => {
    let cancelled = false;

    async function loadReviews() {
      setReviewsLoading(true);
      setReviewError("");
      setReviews([]);
      setReviewSummary(null);

      try {
        const [reviewData, summaryData] = await Promise.all([
          productReviewService.getReviews(id),
          productReviewService.getSummary(id),
        ]);

        if (!cancelled) {
          setReviews(Array.isArray(reviewData) ? reviewData : []);
          setReviewSummary(summaryData);
        }
      } catch (err) {
        if (!cancelled) {
          setReviewError(
            err.response?.data?.detail ||
              err.response?.data?.message ||
              "Unable to load product reviews. Please try again."
          );
        }
      } finally {
        if (!cancelled) {
          setReviewsLoading(false);
        }
      }
    }

    if (id) {
      loadReviews();
    }

    return () => {
      cancelled = true;
    };
  }, [id]);

  const money = (amount) =>
    `₹${Number(amount || 0).toLocaleString("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;

  function handleAddToCart() {
    if (!product || adding) return;

    setMessage("");
    setError("");

    if (!user) {
      navigate("/login", {
        state: {
          from: {
            pathname: `/products/${id}`,
          },
        },
      });
      return;
    }

    addProductToCart();
  }

  async function addProductToCart() {
    setAdding(true);

    try {
      await cartService.addItem(product.id, quantity);
      setMessage(`${product.name} added to your cart.`);
    } catch (err) {
      setError(
        err.response?.data?.detail ||
          err.response?.data?.message ||
          "Unable to add this product to your cart."
      );
    } finally {
      setAdding(false);
    }
  }

  async function handleSubmitReview(event) {
    event.preventDefault();

    setReviewError("");
    setReviewMessage("");

    if (!user) {
      navigate("/login", {
        state: {
          from: {
            pathname: `/products/${id}`,
          },
        },
      });
      return;
    }

    if (!Number.isInteger(rating) || rating < 1 || rating > 5) {
      setReviewError("Please select a rating between 1 and 5 stars.");
      return;
    }

    if (!comment.trim()) {
      setReviewError("Please enter your review.");
      return;
    }

    if (comment.trim().length > 2000) {
      setReviewError("Review cannot exceed 2000 characters.");
      return;
    }

    setSubmittingReview(true);

    try {
      await productReviewService.createReview(id, {
        rating,
        comment: comment.trim(),
      });

      const [reviewData, summaryData] = await Promise.all([
        productReviewService.getReviews(id),
        productReviewService.getSummary(id),
      ]);

      setReviews(Array.isArray(reviewData) ? reviewData : []);
      setReviewSummary(summaryData);
      setComment("");
      setRating(5);
      setReviewMessage("Your review was submitted successfully.");
    } catch (err) {
      setReviewError(
        err.response?.data?.detail ||
          err.response?.data?.message ||
          (err.response?.status === 401
            ? "Please log in to submit a review."
            : err.response?.status === 409
              ? "You have already reviewed this product."
              : "Unable to submit your review. Please try again.")
      );
    } finally {
      setSubmittingReview(false);
    }
  }

  function formatReviewDate(value) {
    if (!value) return "";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) return "";

    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  }

  function renderStars(value, size = 18) {
    const stars = Math.round(Number(value) || 0);

    return (
      <span
        className="inline-flex items-center gap-1"
        aria-label={`${stars} out of 5 stars`}
      >
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            size={size}
            fill={star <= stars ? "currentColor" : "none"}
            className={
              star <= stars
                ? "text-amber-400"
                : "text-slate-600"
            }
          />
        ))}
      </span>
    );
  }

  const stockQuantity = Number(product?.stockQuantity ?? 0);

  const distribution = [
    { label: 5, count: reviewSummary?.fiveStarCount ?? 0 },
    { label: 4, count: reviewSummary?.fourStarCount ?? 0 },
    { label: 3, count: reviewSummary?.threeStarCount ?? 0 },
    { label: 2, count: reviewSummary?.twoStarCount ?? 0 },
    { label: 1, count: reviewSummary?.oneStarCount ?? 0 },
  ];

  const totalReviews = Number(reviewSummary?.totalReviews ?? 0);

  return (
    <div className="min-h-screen bg-slate-950 text-white">
      {/* Shared StorefrontLayout renders the common header and footer. */}

      <section className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Link
          to="/products"
          className="inline-flex items-center gap-2 text-sm font-medium text-slate-400 transition hover:text-violet-300"
        >
          <ArrowLeft size={17} />
          Back to products
        </Link>

        {loading ? (
          <div className="mt-8 grid gap-8 lg:grid-cols-2">
            <div className="h-96 animate-pulse rounded-3xl bg-white/5" />
            <div className="h-96 animate-pulse rounded-3xl bg-white/5" />
          </div>
        ) : error && !product ? (
          <div
            role="alert"
            className="mt-8 rounded-2xl border border-red-400/20 bg-red-400/10 p-6 text-red-200"
          >
            {error}

            <div className="mt-5">
              <Link
                to="/products"
                className="font-semibold text-violet-300 underline"
              >
                Return to products
              </Link>
            </div>
          </div>
        ) : product ? (
          <>
            <div className="mt-8 grid items-start gap-8 lg:grid-cols-2 lg:gap-14">
              {/* Product image and wishlist button */}
              <div className="relative">
                <div className="flex min-h-80 items-center justify-center overflow-hidden rounded-3xl border border-white/10 bg-gradient-to-br from-violet-500/10 via-slate-900 to-indigo-500/10 sm:min-h-[480px]">
                  {product.imageUrl ? (
                    <img
                      src={product.imageUrl}
                      alt={product.name}
                      onError={(event) => {
                        event.currentTarget.style.display = "none";
                      }}
                      className="h-full max-h-[560px] w-full object-contain"
                    />
                  ) : (
                    <Package
                      size={100}
                      className="text-violet-300/50"
                    />
                  )}
                </div>

                <WishlistButton
                  product={product}
                  className="absolute right-4 top-4 z-10"
                />
              </div>

              {/* Product details */}
              <div className="py-2 sm:py-5">
                <p className="text-xs font-bold uppercase tracking-[0.2em] text-violet-300">
                  {product.category || "ASTRA Collection"}
                </p>

                <h1 className="mt-4 text-3xl font-extrabold tracking-tight sm:text-4xl">
                  {product.name}
                </h1>

                {/* Rating summary near product name */}
                <button
                  type="button"
                  onClick={() =>
                    document
                      .getElementById("product-reviews")
                      ?.scrollIntoView({ behavior: "smooth" })
                  }
                  className="mt-3 inline-flex flex-wrap items-center gap-2 text-sm text-slate-300 hover:text-violet-300"
                >
                  {renderStars(reviewSummary?.averageRating)}
                  <span>
                    {reviewSummary
                      ? `${Number(
                          reviewSummary.averageRating ?? 0
                        ).toFixed(1)} (${totalReviews} ${
                          totalReviews === 1 ? "review" : "reviews"
                        })`
                      : "Reviews"}
                  </span>
                </button>

                <p className="mt-5 text-3xl font-bold text-violet-200">
                  {money(product.price)}
                </p>

                {product.sku && (
                  <p className="mt-2 text-sm text-slate-400">
                    SKU: {product.sku}
                  </p>
                )}

                {product.subcategory && (
                  <p className="mt-2 text-sm text-slate-400">
                    Subcategory: {product.subcategory}
                  </p>
                )}

                <div className="mt-6 border-y border-white/10 py-5">
                  <h2 className="font-semibold">Product description</h2>

                  <p className="mt-3 whitespace-pre-line text-sm leading-7 text-slate-300">
                    {product.description ||
                      "Discover quality and convenience with this ASTRA product."}
                  </p>
                </div>

                {/* Product variants */}
                {Array.isArray(product.variants) &&
                  product.variants.length > 0 && (
                    <div className="mt-5">
                      <h2 className="font-semibold">Available options</h2>

                      <div className="mt-3 flex flex-wrap gap-2">
                        {[
                          ...new Set(
                            product.variants
                              .flatMap((variant) => [
                                variant.size,
                                variant.color,
                              ])
                              .filter(Boolean)
                          ),
                        ].map((option) => (
                          <span
                            key={option}
                            className="rounded-lg border border-white/10 bg-white/[0.04] px-3 py-2 text-sm text-slate-300"
                          >
                            {option}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                {/* Stock availability */}
                <div className="mt-5 flex items-center gap-2 text-sm">
                  <span
                    className={
                      stockQuantity > 0
                        ? "text-emerald-300"
                        : "text-red-300"
                    }
                  >
                    <CheckCircle2 size={17} className="inline" />{" "}
                    {stockQuantity > 0
                      ? `${stockQuantity} available in stock`
                      : "Currently out of stock"}
                  </span>
                </div>

                {message && (
                  <div
                    role="status"
                    className="mt-5 rounded-xl border border-emerald-400/20 bg-emerald-400/10 p-4 text-sm text-emerald-200"
                  >
                    {message}{" "}
                    <Link
                      to="/cart"
                      className="font-semibold underline"
                    >
                      View cart
                    </Link>
                  </div>
                )}

                {error && product && (
                  <div
                    role="alert"
                    className="mt-5 rounded-xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200"
                  >
                    {error}
                  </div>
                )}

                {/* Quantity selector */}
                <div className="mt-7">
                  <label
                    htmlFor="product-quantity"
                    className="mb-3 block text-sm font-semibold"
                  >
                    Quantity
                  </label>

                  <div className="inline-flex items-center rounded-xl border border-white/10 bg-white/[0.04]">
                    <button
                      type="button"
                      aria-label="Decrease quantity"
                      disabled={quantity <= 1 || stockQuantity < 1}
                      onClick={() =>
                        setQuantity((value) => Math.max(1, value - 1))
                      }
                      className="rounded-l-xl p-3 transition hover:bg-violet-500/10 disabled:opacity-40"
                    >
                      <Minus size={17} />
                    </button>

                    <span
                      id="product-quantity"
                      className="min-w-12 text-center font-semibold"
                    >
                      {quantity}
                    </span>

                    <button
                      type="button"
                      aria-label="Increase quantity"
                      disabled={
                        quantity >= stockQuantity || stockQuantity < 1
                      }
                      onClick={() =>
                        setQuantity((value) =>
                          Math.min(stockQuantity, value + 1)
                        )
                      }
                      className="rounded-r-xl p-3 transition hover:bg-violet-500/10 disabled:opacity-40"
                    >
                      <Plus size={17} />
                    </button>
                  </div>

                  <p className="mt-3 text-sm text-slate-400">
                    Total:{" "}
                    <span className="font-bold text-white">
                      {money(Number(product.price) * quantity)}
                    </span>
                  </p>
                </div>

                {/* Add to cart */}
                <button
                  type="button"
                  disabled={stockQuantity < 1 || adding || authLoading}
                  onClick={handleAddToCart}
                  className="mt-7 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-violet-600 px-6 py-4 font-semibold text-white transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:bg-white/10 disabled:text-slate-500 sm:w-auto sm:min-w-64"
                >
                  <ShoppingCart size={19} />
                  {adding
                    ? "Adding to cart..."
                    : stockQuantity < 1
                      ? "Out of stock"
                      : "Add to cart"}
                </button>
              </div>
            </div>

            {/* Product reviews and ratings */}
            <section
              id="product-reviews"
              className="mt-16 scroll-mt-8 border-t border-white/10 pt-10"
            >
              <div className="flex flex-wrap items-center gap-3">
                <Star className="text-amber-400" size={25} />
                <h2 className="text-2xl font-bold">
                  Customer Reviews
                </h2>
              </div>

              <p className="mt-2 text-sm text-slate-400">
                Ratings and feedback from customers.
              </p>

              {reviewsLoading ? (
                <div className="mt-6 h-36 animate-pulse rounded-2xl bg-white/5" />
              ) : reviewError && !reviewSummary ? (
                <div
                  role="alert"
                  className="mt-6 rounded-xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200"
                >
                  {reviewError}
                </div>
              ) : (
                <div className="mt-6 grid gap-8 lg:grid-cols-[280px_1fr]">
                  {/* Average rating and distribution */}
                  <div className="h-fit rounded-2xl border border-white/10 bg-white/[0.03] p-6">
                    <p className="text-sm text-slate-400">
                      Overall rating
                    </p>

                    <p className="mt-2 text-4xl font-extrabold">
                      {Number(
                        reviewSummary?.averageRating ?? 0
                      ).toFixed(1)}
                      <span className="ml-1 text-lg font-medium text-slate-400">
                        / 5
                      </span>
                    </p>

                    <div className="mt-3">
                      {renderStars(reviewSummary?.averageRating, 20)}
                    </div>

                    <p className="mt-2 text-sm text-slate-400">
                      {totalReviews}{" "}
                      {totalReviews === 1 ? "review" : "reviews"}
                    </p>

                    <div className="mt-6 space-y-3">
                      {distribution.map((item) => {
                        const percentage =
                          totalReviews > 0
                            ? (item.count / totalReviews) * 100
                            : 0;

                        return (
                          <div
                            key={item.label}
                            className="flex items-center gap-3 text-sm"
                          >
                            <span className="w-8 text-slate-300">
                              {item.label} ★
                            </span>

                            <div className="h-2 flex-1 overflow-hidden rounded-full bg-white/10">
                              <div
                                className="h-full rounded-full bg-amber-400"
                                style={{ width: `${percentage}%` }}
                              />
                            </div>

                            <span className="w-7 text-right text-slate-400">
                              {item.count}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  </div>

                  {/* Submit a review */}
                  <div className="rounded-2xl border border-white/10 bg-white/[0.03] p-6">
                    <h3 className="text-lg font-bold">
                      Write a review
                    </h3>

                    <p className="mt-1 text-sm text-slate-400">
                      Share your experience with this product.
                    </p>

                    {!user ? (
                      <div className="mt-5 rounded-xl border border-violet-400/20 bg-violet-400/10 p-4 text-sm text-slate-200">
                        Please{" "}
                        <Link
                          to="/login"
                          state={{
                            from: { pathname: `/products/${id}` },
                          }}
                          className="font-semibold text-violet-300 underline"
                        >
                          log in
                        </Link>{" "}
                        to submit a review.
                      </div>
                    ) : (
                      <form
                        onSubmit={handleSubmitReview}
                        className="mt-5 space-y-5"
                      >
                        <fieldset>
                          <legend className="mb-3 text-sm font-semibold">
                            Your rating
                          </legend>

                          <div className="flex items-center gap-2">
                            {[1, 2, 3, 4, 5].map((value) => (
                              <button
                                key={value}
                                type="button"
                                aria-label={`${value} star${
                                  value === 1 ? "" : "s"
                                }`}
                                aria-pressed={rating === value}
                                onClick={() => setRating(value)}
                                className="rounded-lg p-1 transition hover:scale-110 focus:outline-none focus:ring-2 focus:ring-violet-400"
                              >
                                <Star
                                  size={30}
                                  fill={
                                    value <= rating
                                      ? "currentColor"
                                      : "none"
                                  }
                                  className={
                                    value <= rating
                                      ? "text-amber-400"
                                      : "text-slate-600"
                                  }
                                />
                              </button>
                            ))}

                            <span className="ml-2 text-sm text-slate-400">
                              {rating} / 5
                            </span>
                          </div>
                        </fieldset>

                        <div>
                          <label
                            htmlFor="review-comment"
                            className="mb-2 block text-sm font-semibold"
                          >
                            Your review
                          </label>

                          <textarea
                            id="review-comment"
                            value={comment}
                            onChange={(event) =>
                              setComment(event.target.value.slice(0, 2000))
                            }
                            rows={5}
                            maxLength={2000}
                            required
                            placeholder="What did you like or dislike about this product?"
                            className="w-full resize-y rounded-xl border border-white/10 bg-slate-900 px-4 py-3 text-sm text-white outline-none placeholder:text-slate-500 focus:border-violet-400"
                          />

                          <p className="mt-1 text-right text-xs text-slate-500">
                            {comment.length}/2000
                          </p>
                        </div>

                        {reviewError && (
                          <div
                            role="alert"
                            className="rounded-xl border border-red-400/20 bg-red-400/10 p-3 text-sm text-red-200"
                          >
                            {reviewError}
                          </div>
                        )}

                        {reviewMessage && (
                          <div
                            role="status"
                            className="rounded-xl border border-emerald-400/20 bg-emerald-400/10 p-3 text-sm text-emerald-200"
                          >
                            {reviewMessage}
                          </div>
                        )}

                        <button
                          type="submit"
                          disabled={submittingReview || reviewsLoading}
                          className="inline-flex items-center justify-center rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:opacity-50"
                        >
                          {submittingReview
                            ? "Submitting review..."
                            : "Submit review"}
                        </button>
                      </form>
                    )}
                  </div>
                </div>
              )}

              {/* Review list */}
              {!reviewsLoading && reviewSummary && (
                <div className="mt-8">
                  <h3 className="text-lg font-bold">
                    Customer feedback
                  </h3>

                  {reviews.length === 0 ? (
                    <div className="mt-4 rounded-2xl border border-dashed border-white/15 p-8 text-center">
                      <Star
                        size={32}
                        className="mx-auto text-slate-500"
                      />
                      <p className="mt-3 font-semibold">
                        No reviews yet
                      </p>
                      <p className="mt-1 text-sm text-slate-400">
                        Be the first to share your experience.
                      </p>
                    </div>
                  ) : (
                    <div className="mt-4 space-y-4">
                      {reviews.map((review) => (
                        <article
                          key={review.id}
                          className="rounded-2xl border border-white/10 bg-white/[0.03] p-5 sm:p-6"
                        >
                          <div className="flex flex-wrap items-start justify-between gap-3">
                            <div>
                              <p className="font-semibold">
                                {review.customerName || "Customer"}
                              </p>

                              <div className="mt-2">
                                {renderStars(review.rating, 16)}
                              </div>
                            </div>

                            <div className="text-right">
                              <time className="text-xs text-slate-500">
                                {formatReviewDate(review.createdAt)}
                              </time>

                              {review.verifiedPurchase && (
                                <p className="mt-2 text-xs font-medium text-emerald-300">
                                  <CheckCircle2
                                    size={13}
                                    className="mr-1 inline"
                                  />
                                  Verified purchase
                                </p>
                              )}
                            </div>
                          </div>

                          <p className="mt-4 whitespace-pre-line break-words text-sm leading-7 text-slate-300">
                            {review.comment}
                          </p>
                        </article>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </section>
          </>
        ) : null}
      </section>
    </div>
  );
}

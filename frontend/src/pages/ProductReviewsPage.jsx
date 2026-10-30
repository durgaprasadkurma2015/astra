import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, CheckCircle2, Star } from "lucide-react";

import productService from "../services/productService";
import productReviewService from "../services/productReviewService";
import { useAuth } from "../context/useAuth.js";

export default function ProductReviewsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, loading: authLoading } = useAuth();

  const [product, setProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [summary, setSummary] = useState(null);

  const [pageLoading, setPageLoading] = useState(true);
  const [reviewsLoading, setReviewsLoading] = useState(true);

  const [pageError, setPageError] = useState("");
  const [reviewError, setReviewError] = useState("");
  const [reviewMessage, setReviewMessage] = useState("");

  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // Load product details.
  useEffect(() => {
    let cancelled = false;

    async function loadProduct() {
      setPageLoading(true);
      setPageError("");

      try {
        const productData = await productService.getProduct(id);

        if (!cancelled) {
          setProduct(productData);
        }
      } catch (error) {
        if (!cancelled) {
          setPageError(
            error.response?.status === 404
              ? "Product not found."
              : error.response?.data?.message ||
                  "Unable to load product details. Please try again."
          );
        }
      } finally {
        if (!cancelled) {
          setPageLoading(false);
        }
      }
    }

 if (id) {
  loadProduct();
}

    return () => {
      cancelled = true;
    };
  }, [id]);

  // Load reviews and summary in one place.
  const loadReviews = useCallback(
    async ({ showLoading = true } = {}) => {
      if (!id) {
        setReviewsLoading(false);
        return false;
      }

      if (showLoading) {
        setReviewsLoading(true);
      }

      setReviewError("");

      try {
        const [reviewData, summaryData] = await Promise.all([
          productReviewService.getReviews(id),
          productReviewService.getSummary(id),
        ]);

        setReviews(Array.isArray(reviewData) ? reviewData : []);
        setSummary(summaryData);

        return true;
      } catch (error) {
        setReviewError(
          error.response?.data?.detail ||
            error.response?.data?.message ||
            "Unable to load reviews. Please try again."
        );

        return false;
      } finally {
        if (showLoading) {
          setReviewsLoading(false);
        }
      }
    },
    [id]
  );

  // Only one initial reviews-loading effect.
// Load reviews after the component mounts or the product ID changes.
useEffect(() => {
  let cancelled = false;

  async function fetchReviews() {
    if (!id) return;

    try {
      const [reviewData, summaryData] = await Promise.all([
        productReviewService.getReviews(id),
        productReviewService.getSummary(id),
      ]);

      if (!cancelled) {
        setReviews(Array.isArray(reviewData) ? reviewData : []);
        setSummary(summaryData);
        setReviewError("");
      }
    } catch (error) {
      if (!cancelled) {
        setReviewError(
          error.response?.data?.detail ||
            error.response?.data?.message ||
            "Unable to load reviews. Please try again."
        );
      }
    } finally {
      if (!cancelled) {
        setReviewsLoading(false);
      }
    }
  }

  fetchReviews();

  return () => {
    cancelled = true;
  };
}, [id]);

  function renderStars(value, size = 18) {
    const filledStars = Math.round(Number(value) || 0);

    return (
      <span
        className="inline-flex items-center gap-1"
        aria-label={`${filledStars} out of 5 stars`}
      >
        {[1, 2, 3, 4, 5].map((number) => (
          <Star
            key={number}
            size={size}
            fill={number <= filledStars ? "currentColor" : "none"}
            className={
              number <= filledStars
                ? "text-amber-400"
                : "text-slate-600"
            }
          />
        ))}
      </span>
    );
  }

  function formatDate(value) {
    if (!value) return "";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return "";
    }

    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  }

  async function handleSubmitReview(event) {
    event.preventDefault();

    setReviewError("");
    setReviewMessage("");

    if (authLoading) {
      setReviewError("Please wait while your login status is checked.");
      return;
    }

    if (!user) {
      navigate("/login", {
        state: {
          from: {
            pathname: `/products/${id}/reviews`,
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

    if (submitting) {
      return;
    }

    setSubmitting(true);

    try {
      await productReviewService.createReview(id, {
        rating,
        comment: comment.trim(),
      });

      setComment("");
      setRating(5);
      setReviewMessage("Your review was submitted and is awaiting approval before it appears publicly.");

      // Refresh data without blocking the form on the loading flag.
      await loadReviews({ showLoading: false });
    } catch (error) {
      setReviewError(
        error.response?.data?.detail ||
          error.response?.data?.message ||
          (error.response?.status === 401
            ? "Your session may have expired. Please log in again."
            : error.response?.status === 409
              ? "You have already reviewed this product."
              : "Unable to submit your review. Please try again.")
      );
    } finally {
      setSubmitting(false);
    }
  }

  const totalReviews = Number(summary?.totalReviews ?? 0);

  const distribution = [
    { stars: 5, count: Number(summary?.fiveStarCount ?? 0) },
    { stars: 4, count: Number(summary?.fourStarCount ?? 0) },
    { stars: 3, count: Number(summary?.threeStarCount ?? 0) },
    { stars: 2, count: Number(summary?.twoStarCount ?? 0) },
    { stars: 1, count: Number(summary?.oneStarCount ?? 0) },
  ];

  if (pageLoading) {
    return (
      <main className="min-h-screen bg-slate-950 p-8 text-white">
        Loading product details...
      </main>
    );
  }

  if (pageError || !product) {
    return (
      <main className="min-h-screen bg-slate-950 p-8 text-white">
        <p role="alert" className="text-red-300">
          {pageError || "Product not found."}
        </p>

        <Link
          to="/products"
          className="mt-4 inline-block text-violet-300 underline"
        >
          Back to products
        </Link>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-950 text-white">
      <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
        <Link
          to={`/products/${id}`}
          className="inline-flex items-center gap-2 text-sm text-slate-300 hover:text-violet-300"
        >
          <ArrowLeft size={17} />
          Back to product
        </Link>

        {/* Product review heading */}
        <header className="mt-8 border-b border-white/10 pb-6">
          <p className="text-sm font-semibold uppercase tracking-widest text-violet-300">
            Product Reviews
          </p>

          <h1 className="mt-3 text-3xl font-extrabold sm:text-4xl">
            {product.name}
          </h1>

          <div className="mt-4 flex flex-wrap items-center gap-3">
            {renderStars(summary?.averageRating, 20)}

            <span className="text-lg font-bold">
              {Number(summary?.averageRating ?? 0).toFixed(1)} / 5
            </span>

            <span className="text-sm text-slate-400">
              ({totalReviews}{" "}
              {totalReviews === 1 ? "review" : "reviews"})
            </span>
          </div>
        </header>

        {/* Review loading/error status */}
        {reviewsLoading && (
          <p className="mt-5 text-sm text-slate-400" role="status">
            Loading customer reviews...
          </p>
        )}

        {reviewError && (
          <div
            role="alert"
            className="mt-5 rounded-xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200"
          >
            {reviewError}

            {!reviewsLoading && (
              <button
                type="button"
                onClick={() => loadReviews()}
                className="ml-3 font-semibold underline"
              >
                Retry
              </button>
            )}
          </div>
        )}

        {/* Rating breakdown */}
        <section className="mt-8 grid gap-8 lg:grid-cols-[280px_1fr]">
          <div className="h-fit rounded-2xl border border-white/10 bg-white/[0.03] p-6">
            <h2 className="text-lg font-bold">Rating breakdown</h2>

            <div className="mt-5 space-y-4">
              {distribution.map((item) => {
                const percentage =
                  totalReviews > 0
                    ? (item.count / totalReviews) * 100
                    : 0;

                return (
                  <div
                    key={item.stars}
                    className="flex items-center gap-3 text-sm"
                  >
                    <span className="w-8 text-slate-300">
                      {item.stars} ★
                    </span>

                    <div className="h-2 flex-1 overflow-hidden rounded-full bg-white/10">
                      <div
                        className="h-full rounded-full bg-amber-400"
                        style={{ width: `${percentage}%` }}
                      />
                    </div>

                    <span className="w-6 text-right text-slate-400">
                      {item.count}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Write review form remains visible while reviews load */}
          <div className="rounded-2xl border border-white/10 bg-white/[0.03] p-6">
            <h2 className="text-xl font-bold">Write a review</h2>

            <p className="mt-2 text-sm text-slate-400">
              Share your experience with this product.
            </p>

            {authLoading ? (
              <p className="mt-5 text-sm text-slate-400" role="status">
                Checking your login status...
              </p>
            ) : !user ? (
              <p className="mt-5 text-sm text-slate-300">
                Please{" "}
                <Link
                  to="/login"
                  state={{
                    from: {
                      pathname: `/products/${id}/reviews`,
                    },
                  }}
                  className="font-semibold text-violet-300 underline"
                >
                  log in
                </Link>{" "}
                to submit a review.
              </p>
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
                        aria-label={`${value} star rating`}
                        aria-pressed={rating === value}
                        onClick={() => setRating(value)}
                        className="rounded p-1 focus:outline-none focus:ring-2 focus:ring-violet-400"
                      >
                        <Star
                          size={30}
                          fill={
                            value <= rating ? "currentColor" : "none"
                          }
                          className={
                            value <= rating
                              ? "text-amber-400"
                              : "text-slate-600"
                          }
                        />
                      </button>
                    ))}
                  </div>

                  <p className="mt-2 text-xs text-slate-400">
                    Selected rating: {rating} out of 5
                  </p>
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
                    onChange={(event) => {
                      setComment(event.target.value);

                      if (reviewError) {
                        setReviewError("");
                      }
                    }}
                    placeholder="What did you like or dislike about this product?"
                    rows={5}
                    maxLength={2000}
                    required
                    className="w-full resize-y rounded-xl border border-white/10 bg-slate-900 px-4 py-3 text-sm text-white outline-none placeholder:text-slate-500 focus:border-violet-400"
                  />

                  <p className="mt-1 text-right text-xs text-slate-500">
                    {comment.length}/2000
                  </p>
                </div>

                {reviewMessage && (
                  <p
                    role="status"
                    className="rounded-xl bg-emerald-400/10 p-3 text-sm text-emerald-200"
                  >
                    {reviewMessage}
                  </p>
                )}

                <button
                  type="submit"
                  disabled={submitting || authLoading}
                  className="inline-flex items-center justify-center rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-violet-500 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {submitting ? "Submitting review..." : "Submit review"}
                </button>
              </form>
            )}
          </div>
        </section>

        {/* Customer reviews */}
        <section className="mt-12">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-2xl font-bold">Customer feedback</h2>

            {!reviewsLoading && (
              <span className="text-sm text-slate-400">
                {totalReviews}{" "}
                {totalReviews === 1 ? "review" : "reviews"}
              </span>
            )}
          </div>

          {reviewsLoading ? (
            <p className="mt-5 text-slate-400">
              Fetching customer feedback...
            </p>
          ) : reviews.length === 0 ? (
            <div className="mt-5 rounded-2xl border border-dashed border-white/15 p-8 text-center">
              <Star size={32} className="mx-auto text-slate-500" />

              <p className="mt-3 font-semibold">No reviews yet</p>

              <p className="mt-1 text-sm text-slate-400">
                Be the first to share your experience.
              </p>
            </div>
          ) : (
            <div className="mt-5 space-y-4">
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
                        {formatDate(review.createdAt)}
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
        </section>
      </div>
    </main>
  );
}

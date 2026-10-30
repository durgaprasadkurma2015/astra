import { useCallback, useEffect, useState } from "react";
import { Star } from "lucide-react";
import api from "../services/api";

export default function AdminReviewsPage() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState("");

  const loadReviews = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const response = await api.get("/admin/reviews", { params: { page, size: 20 } });
      setData(response.data);
    } catch (err) {
      setError(err.response?.data?.detail || err.response?.data?.message || "Unable to load reviews.");
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { void Promise.resolve().then(loadReviews); }, [loadReviews]);

  async function updateVisibility(review) {
    setBusyId(review.id);
    setError("");
    try {
      await api.put(`/admin/reviews/${review.id}/visibility`, { published: !review.published });
      await loadReviews();
    } catch (err) {
      setError(err.response?.data?.detail || err.response?.data?.message || "Unable to update review visibility.");
    } finally {
      setBusyId(null);
    }
  }

  return (
    <section className="min-h-screen bg-[#09090f] px-4 py-7 text-white sm:px-8">
      <header className="mb-7">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-violet-400">Customer feedback</p>
        <h1 className="mt-2 text-3xl font-bold">Review moderation</h1>
        <p className="mt-2 text-sm text-gray-400">Approve verified purchase reviews before they appear on product pages.</p>
      </header>
      {error && <p role="alert" className="mb-5 rounded-xl border border-red-400/20 bg-red-500/10 p-4 text-sm text-red-300">{error}</p>}
      <div className="mb-4 text-sm text-gray-400">{data.totalElements || 0} total reviews</div>
      {loading ? <p className="py-16 text-center text-gray-400">Loading reviews…</p> : data.content?.length ? (
        <div className="space-y-4">
          {data.content.map((review) => (
            <article key={review.id} className="rounded-2xl border border-white/10 bg-white/[0.03] p-5">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                  <h2 className="font-semibold text-white">{review.productName}</h2>
                  <p className="mt-1 text-sm text-gray-400">{review.customerName} · {review.customerEmail}</p>
                  <div className="mt-2 flex items-center gap-1 text-amber-300" aria-label={`${review.rating} out of 5 stars`}>
                    {Array.from({ length: 5 }, (_, index) => <Star key={index} size={15} fill={index < review.rating ? "currentColor" : "none"} />)}
                    <span className="ml-1 text-xs text-gray-400">{review.verifiedPurchase ? "Verified purchase" : "Unverified"}</span>
                  </div>
                </div>
                <button type="button" disabled={busyId === review.id} onClick={() => updateVisibility(review)} className={`rounded-lg px-4 py-2 text-sm font-semibold disabled:opacity-50 ${review.published ? "border border-red-400/30 text-red-300 hover:bg-red-500/10" : "bg-emerald-600 text-white hover:bg-emerald-500"}`}>
                  {busyId === review.id ? "Saving…" : review.published ? "Hide review" : "Approve review"}
                </button>
              </div>
              <p className="mt-4 whitespace-pre-wrap text-sm leading-6 text-gray-300">{review.comment}</p>
              <p className="mt-3 text-xs text-gray-500">{review.createdAt ? new Date(review.createdAt).toLocaleString() : "Date unavailable"}</p>
            </article>
          ))}
          {data.totalPages > 1 && <nav aria-label="Review pages" className="flex items-center justify-between py-3">
            <button type="button" disabled={page <= 0} onClick={() => setPage((value) => Math.max(0, value - 1))} className="rounded-lg border border-white/10 px-4 py-2 text-sm disabled:opacity-40">Previous</button>
            <span className="text-sm text-gray-400">Page {page + 1} of {data.totalPages}</span>
            <button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage((value) => Math.min(data.totalPages - 1, value + 1))} className="rounded-lg border border-white/10 px-4 py-2 text-sm disabled:opacity-40">Next</button>
          </nav>}
        </div>
      ) : <p className="rounded-xl border border-white/10 p-10 text-center text-gray-400">No reviews have been submitted yet.</p>}
    </section>
  );
}

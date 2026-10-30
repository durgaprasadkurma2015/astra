import { useEffect, useState } from "react";
import { Search, RefreshCw, UserRound, UserRoundCheck, UserRoundX } from "lucide-react";
import api from "../services/api";

const PAGE_SIZE = 20;

export default function AdminCustomersPage() {
  const [customers, setCustomers] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState({ totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      try {
        const response = await api.get("/admin/customers", {
          params: { keyword: search || undefined, page, size: PAGE_SIZE },
        });
        if (cancelled) return;
        setCustomers(response.data?.content || []);
        setPageData({ totalElements: response.data?.totalElements || 0, totalPages: response.data?.totalPages || 0 });
        setError("");
      } catch (loadError) {
        if (!cancelled) {
          setCustomers([]);
          setError(loadError.response?.data?.detail || loadError.response?.data?.message || "Unable to load customers.");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    void load();
    return () => { cancelled = true; };
  }, [search, page, reloadKey]);

  function submitSearch(event) {
    event.preventDefault();
    setPage(0);
    setSearch(keyword.trim());
  }

  async function toggleStatus(customer) {
    setSavingId(customer.id);
    setError(""); setMessage("");
    try {
      const response = await api.put(`/admin/customers/${customer.id}/active`, { active: !customer.active });
      setCustomers((current) => current.map((item) => item.id === customer.id ? response.data : item));
      setMessage(`${customer.fullName} is now ${response.data.active ? "active" : "inactive"}.`);
    } catch (statusError) {
      setError(statusError.response?.data?.detail || statusError.response?.data?.message || "Unable to update customer status.");
    } finally { setSavingId(null); }
  }

  return (
    <main className="min-h-screen bg-[#09090f] px-4 py-7 text-white sm:px-8 lg:px-10">
      <div className="mb-7 flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="flex items-center gap-2 text-sm font-semibold text-violet-300"><UserRound size={17} /> CUSTOMER ACCOUNTS</p>
          <h1 className="mt-2 text-3xl font-bold">Customers</h1>
          <p className="mt-2 text-sm text-gray-400">Search customer accounts and manage access.</p>
        </div>
        <button type="button" onClick={() => setReloadKey((value) => value + 1)} disabled={loading} className="inline-flex items-center gap-2 rounded-xl border border-white/10 px-4 py-2.5 text-sm hover:bg-white/5 disabled:opacity-50"><RefreshCw size={16} className={loading ? "animate-spin" : ""} /> Refresh</button>
      </div>

      {error && <p role="alert" className="mb-4 rounded-xl border border-red-400/20 bg-red-500/10 p-4 text-sm text-red-200">{error}</p>}
      {message && <p role="status" className="mb-4 rounded-xl border border-emerald-400/20 bg-emerald-500/10 p-4 text-sm text-emerald-200">{message}</p>}

      <form onSubmit={submitSearch} className="mb-5 flex flex-col gap-3 sm:flex-row">
        <label className="relative flex-1">
          <span className="sr-only">Search by customer name or email</span>
          <Search size={17} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500" />
          <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="Search name or email" className="w-full rounded-xl border border-white/10 bg-[#11111b] py-3 pl-11 pr-4 text-sm outline-none focus:border-violet-400" />
        </label>
        <button className="rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold hover:bg-violet-500">Search</button>
      </form>

      <section className="overflow-hidden rounded-2xl border border-white/10 bg-[#11111b]">
        {loading ? <div className="py-16 text-center text-gray-400">Loading customers...</div> : customers.length === 0 ? (
          <div className="py-16 text-center text-gray-400">No customers found.</div>
        ) : <div className="overflow-x-auto">
          <table className="w-full min-w-[760px] text-left text-sm">
            <thead className="border-b border-white/10 text-xs uppercase tracking-wider text-gray-500"><tr><th className="px-5 py-4">Customer</th><th className="px-5 py-4">Phone</th><th className="px-5 py-4">Joined</th><th className="px-5 py-4">Verification</th><th className="px-5 py-4">Status</th><th className="px-5 py-4">Action</th></tr></thead>
            <tbody>{customers.map((customer) => (
              <tr key={customer.id} className="border-b border-white/5 last:border-0">
                <td className="px-5 py-4"><p className="font-semibold">{customer.fullName}</p><p className="mt-1 text-xs text-gray-500">{customer.email}</p></td>
                <td className="px-5 py-4 text-gray-300">{customer.phoneNumber || "—"}</td>
                <td className="px-5 py-4 text-gray-400">{customer.createdAt ? new Date(customer.createdAt).toLocaleDateString() : "—"}</td>
                <td className="px-5 py-4">{customer.emailVerified ? <span className="text-emerald-300">Verified</span> : <span className="text-amber-300">Pending</span>}</td>
                <td className="px-5 py-4">{customer.active ? <span className="rounded-full bg-emerald-500/10 px-3 py-1 text-xs text-emerald-300">Active</span> : <span className="rounded-full bg-red-500/10 px-3 py-1 text-xs text-red-300">Inactive</span>}</td>
                <td className="px-5 py-4"><button type="button" disabled={savingId === customer.id} onClick={() => toggleStatus(customer)} className="inline-flex items-center gap-2 rounded-lg border border-white/10 px-3 py-2 text-xs font-semibold hover:bg-white/5 disabled:opacity-50">{customer.active ? <UserRoundX size={15} /> : <UserRoundCheck size={15} />}{savingId === customer.id ? "Saving..." : customer.active ? "Deactivate" : "Activate"}</button></td>
              </tr>
            ))}</tbody>
          </table>
        </div>}
      </section>

      <div className="mt-4 flex flex-wrap items-center justify-between gap-3 text-sm text-gray-400">
        <span>{pageData.totalElements} customer{pageData.totalElements === 1 ? "" : "s"}</span>
        <div className="flex gap-2">
          <button type="button" disabled={page === 0 || loading} onClick={() => setPage((value) => value - 1)} className="rounded-lg border border-white/10 px-3 py-2 disabled:opacity-40">Previous</button>
          <span className="px-2 py-2">{pageData.totalPages ? page + 1 : 0} / {pageData.totalPages}</span>
          <button type="button" disabled={page + 1 >= pageData.totalPages || loading} onClick={() => setPage((value) => value + 1)} className="rounded-lg border border-white/10 px-3 py-2 disabled:opacity-40">Next</button>
        </div>
      </div>
    </main>
  );
}

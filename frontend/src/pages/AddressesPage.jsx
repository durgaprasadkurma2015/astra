import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, Check, MapPin, Pencil, Plus, Star, Trash2 } from "lucide-react";
import api from "../services/api";

const EMPTY_ADDRESS = {
  label: "Home",
  fullName: "",
  phoneNumber: "",
  addressLine1: "",
  addressLine2: "",
  landmark: "",
  city: "",
  district: "",
  state: "",
  postalCode: "",
  country: "India",
  defaultAddress: false,
};

const FIELDS = [
  ["label", "Label", true],
  ["fullName", "Full name", true],
  ["phoneNumber", "Phone number", true],
  ["addressLine1", "Address line 1", true],
  ["addressLine2", "Address line 2", false],
  ["landmark", "Landmark", false],
  ["city", "City", true],
  ["district", "District", false],
  ["state", "State", true],
  ["postalCode", "Postal code", true],
  ["country", "Country", true],
];

function errorText(error) {
  return error.response?.data?.detail || error.response?.data?.message || error.message || "Please try again.";
}

export default function AddressesPage() {
  const [addresses, setAddresses] = useState([]);
  const [form, setForm] = useState(EMPTY_ADDRESS);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadAddresses = useCallback(async () => {
    try {
      const response = await api.get("/addresses");
      setAddresses(response.data);
      setError("");
    } catch (loadError) {
      setError(errorText(loadError));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;
    api.get("/addresses").then((response) => {
      if (!cancelled) setAddresses(response.data);
    }).catch((loadError) => {
      if (!cancelled) setError(errorText(loadError));
    }).finally(() => {
      if (!cancelled) setLoading(false);
    });
    return () => { cancelled = true; };
  }, []);

  function startEdit(address) {
    const { id, createdAt, updatedAt, ...editable } = address;
    void createdAt; void updatedAt;
    setEditingId(id);
    setForm({ ...EMPTY_ADDRESS, ...editable });
    setError("");
    setMessage("");
  }

  function resetForm() {
    setEditingId(null);
    setForm(EMPTY_ADDRESS);
  }

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    const payload = Object.fromEntries(
      Object.entries(form).map(([key, value]) => [
        key,
        typeof value === "string" ? value.trim() : value,
      ])
    );
    try {
      if (editingId) await api.put(`/addresses/${editingId}`, payload);
      else await api.post("/addresses", payload);
      resetForm();
      setMessage(editingId ? "Address updated." : "Address saved.");
      await loadAddresses();
    } catch (saveError) {
      setError(errorText(saveError));
    } finally {
      setSaving(false);
    }
  }

  async function makeDefault(id) {
    setError(""); setMessage("");
    try {
      await api.put(`/addresses/${id}/default`);
      setMessage("Default address updated.");
      await loadAddresses();
    } catch (actionError) { setError(errorText(actionError)); }
  }

  async function removeAddress(id) {
    setError(""); setMessage("");
    try {
      await api.delete(`/addresses/${id}`);
      if (editingId === id) resetForm();
      setMessage("Address removed.");
      await loadAddresses();
    } catch (actionError) { setError(errorText(actionError)); }
  }

  return (
    <main className="mx-auto min-h-[70vh] max-w-6xl px-4 py-10 text-white sm:px-6 lg:px-8">
      <Link to="/profile" className="inline-flex items-center gap-2 text-sm text-violet-300 hover:text-white">
        <ArrowLeft size={16} /> Back to profile
      </Link>
      <div className="mb-8 mt-5">
        <p className="flex items-center gap-2 text-sm font-semibold text-violet-300"><MapPin size={17} /> ACCOUNT</p>
        <h1 className="mt-2 text-3xl font-bold">Your addresses</h1>
        <p className="mt-2 text-slate-400">Save delivery addresses and choose a default for future orders.</p>
      </div>

      {(error || message) && <p role={error ? "alert" : "status"} className={`mb-5 rounded-xl border p-4 text-sm ${error ? "border-red-400/30 bg-red-500/10 text-red-200" : "border-emerald-400/30 bg-emerald-500/10 text-emerald-200"}`}>{error || message}</p>}

      <div className="grid gap-8 lg:grid-cols-[1fr_1.1fr]">
        <section className="rounded-2xl border border-white/10 bg-white/[0.03] p-5 sm:p-6">
          <h2 className="mb-5 text-xl font-semibold">{editingId ? "Edit address" : "Add an address"}</h2>
          <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
            {FIELDS.map(([key, label, required]) => (
              <label key={key} className={`grid gap-1.5 text-sm text-slate-300 ${["addressLine1", "addressLine2", "landmark"].includes(key) ? "sm:col-span-2" : ""}`}>
                {label}
                <input
                  required={required}
                  maxLength={key === "addressLine1" || key === "addressLine2" ? 255 : key === "landmark" ? 150 : key === "label" ? 40 : key === "fullName" ? 150 : key === "phoneNumber" || key === "postalCode" ? 20 : 100}
                  value={form[key] || ""}
                  onChange={(event) => setForm((value) => ({ ...value, [key]: event.target.value }))}
                  className="rounded-lg border border-white/10 bg-slate-900 px-3 py-2.5 text-white outline-none focus:border-violet-400"
                />
              </label>
            ))}
            <label className="flex items-center gap-2 text-sm text-slate-300 sm:col-span-2">
              <input type="checkbox" checked={form.defaultAddress} onChange={(event) => setForm((value) => ({ ...value, defaultAddress: event.target.checked }))} className="accent-violet-500" />
              Make this my default address
            </label>
            <div className="flex gap-3 sm:col-span-2">
              <button disabled={saving} className="inline-flex items-center gap-2 rounded-lg bg-violet-600 px-4 py-2.5 font-semibold disabled:opacity-60"><Check size={16} />{saving ? "Saving..." : editingId ? "Save changes" : "Save address"}</button>
              {editingId && <button type="button" onClick={resetForm} className="rounded-lg border border-white/15 px-4 py-2.5">Cancel</button>}
            </div>
          </form>
        </section>

        <section>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl font-semibold">Saved addresses</h2>
            {!editingId && <button onClick={() => { resetForm(); document.querySelector("input")?.focus(); }} className="inline-flex items-center gap-1 text-sm text-violet-300 hover:text-white"><Plus size={16} /> Add new</button>}
          </div>
          {loading ? <p className="py-10 text-center text-slate-400">Loading addresses...</p> : addresses.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-white/15 p-8 text-center text-slate-400">No saved addresses yet.</div>
          ) : <div className="grid gap-4">{addresses.map((address) => (
            <article key={address.id} className="rounded-2xl border border-white/10 bg-white/[0.03] p-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <div className="flex flex-wrap items-center gap-2"><h3 className="font-semibold">{address.label}</h3>{address.defaultAddress && <span className="rounded-full bg-violet-500/15 px-2.5 py-1 text-xs text-violet-200">Default</span>}</div>
                  <p className="mt-3 font-medium">{address.fullName}</p>
                  <p className="text-sm text-slate-400">{address.phoneNumber}</p>
                  <p className="mt-2 text-sm leading-6 text-slate-300">{[address.addressLine1, address.addressLine2, address.landmark, address.city, address.district, address.state, address.postalCode, address.country].filter(Boolean).join(", ")}</p>
                </div>
                <MapPin className="shrink-0 text-violet-300" size={19} />
              </div>
              <div className="mt-4 flex flex-wrap gap-3 border-t border-white/10 pt-4 text-sm">
                <button onClick={() => startEdit(address)} className="inline-flex items-center gap-1.5 text-slate-300 hover:text-white"><Pencil size={15} /> Edit</button>
                {!address.defaultAddress && <button onClick={() => makeDefault(address.id)} className="inline-flex items-center gap-1.5 text-slate-300 hover:text-white"><Star size={15} /> Set default</button>}
                <button onClick={() => removeAddress(address.id)} className="inline-flex items-center gap-1.5 text-red-300 hover:text-red-200"><Trash2 size={15} /> Remove</button>
              </div>
            </article>
          ))}</div>}
        </section>
      </div>
    </main>
  );
}

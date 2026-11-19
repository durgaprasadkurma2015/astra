
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Activity,
  AlertCircle,
  ArrowDown,
  ArrowDownUp,
  CalendarDays,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  CircleDot,
  Clock3,
  Database,
  Eye,
  Layers3,
  ListFilter,
  LoaderCircle,
  Pencil,
  PlusCircle,
  RefreshCw,
  Search,
  Server,
  ShieldCheck,
  Table2,
  Trash2,
  UserRound,
  X,
  Zap,
} from "lucide-react";
import apiClient from "../api/apiClient";

const BASE_PATH = "/admin/database-activity";
const PAGE_SIZE = 20;
const REFRESH_INTERVAL = 10000;

const initialStats = {
  totalChanges: 0,
  selects: 0,
  inserts: 0,
  updates: 0,
  deletes: 0,
};

function formatDate(value) {
  if (!value) return "—";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);

  return date.toLocaleString();
}

function formatJson(value) {
  if (value === null || value === undefined) return "null";

  try {
    const parsed = typeof value === "string" ? JSON.parse(value) : value;
    return JSON.stringify(parsed, null, 2);
  } catch {
    return String(value);
  }
}

function parseJson(value, fallback = {}) {
  if (value === null || value === undefined) return fallback;
  if (typeof value !== "string") return value;

  try {
    return JSON.parse(value);
  } catch {
    return fallback;
  }
}

function getChangedColumns(log) {
  const columns = parseJson(log?.changedColumns, []);

  if (Array.isArray(columns)) {
    return columns.filter(
      (column) =>
        typeof column === "string" &&
        column.trim().length > 0
    );
  }

  if (columns && typeof columns === "object") {
    return Object.keys(columns);
  }

  return [];
}

function getRecordId(value) {
  const parsed = parseJson(value, value);

  if (parsed === null || parsed === undefined) return "—";

  return typeof parsed === "object"
    ? JSON.stringify(parsed)
    : String(parsed);
}

function OperationBadge({ operation }) {
  const config = {
    SELECT: {
  icon: Eye,
  style: "border-amber-400/20 bg-amber-400/10 text-amber-300",
},
    INSERT: {
      icon: PlusCircle,
      style:
        "border-emerald-400/20 bg-emerald-400/10 text-emerald-300",
    },
    UPDATE: {
      icon: Pencil,
      style: "border-sky-400/20 bg-sky-400/10 text-sky-300",
    },
    DELETE: {
      icon: Trash2,
      style: "border-rose-400/20 bg-rose-400/10 text-rose-300",
    },
  };

  const item = config[operation] || config.UPDATE;
  const Icon = item.icon;

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-lg border px-2.5 py-1 text-[11px] font-bold tracking-wide ${item.style}`}
    >
      <Icon size={12} />
      {operation || "UNKNOWN"}
    </span>
  );
}

function MetricCard({ title, value, icon: Icon, color, subtitle }) {
  const colors = {
    violet:
      "border-violet-400/25 from-violet-500/20 to-violet-950/20 text-violet-300",
    green:
      "border-emerald-400/25 from-emerald-500/15 to-emerald-950/20 text-emerald-300",
    blue:
      "border-sky-400/25 from-sky-500/15 to-sky-950/20 text-sky-300",
    red:
      "border-rose-400/25 from-rose-500/15 to-rose-950/20 text-rose-300",
  };

  return (
    <div
      className={`group relative overflow-hidden rounded-2xl border bg-gradient-to-br p-4 transition duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-violet-950/20 ${colors[color]}`}
    >
      <div className="absolute -right-5 -top-7 h-24 w-24 rounded-full bg-white/[0.04] blur-2xl" />

      <div className="relative flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-medium text-slate-400">{title}</p>
          <p className="mt-3 text-3xl font-black tracking-tight text-white">
            {Number(value || 0).toLocaleString()}
          </p>
          <p className="mt-1 text-[11px] text-slate-500">{subtitle}</p>
        </div>

        <div className="rounded-xl border border-white/10 bg-white/[0.06] p-3">
          <Icon size={23} />
        </div>
      </div>

      <div className="absolute bottom-0 left-0 h-0.5 w-full bg-gradient-to-r from-transparent via-current to-transparent opacity-40" />
    </div>
  );
}

function JsonPanel({ title, value, variant }) {
  const styles =
    variant === "old"
      ? "border-rose-400/25 bg-rose-400/[0.05]"
      : "border-emerald-400/25 bg-emerald-400/[0.05]";

  return (
    <div className={`min-w-0 overflow-hidden rounded-xl border ${styles}`}>
      <div className="flex items-center gap-2 border-b border-white/10 px-3 py-2.5">
        {variant === "old" ? (
          <ArrowDown size={14} className="text-rose-300" />
        ) : (
          <CheckCircle2 size={14} className="text-emerald-300" />
        )}
        <span className="text-xs font-bold text-slate-200">{title}</span>
      </div>

      <pre className="max-h-72 overflow-auto p-3 text-[11px] leading-5 text-slate-300">
        {formatJson(value)}
      </pre>
    </div>
  );
}

export default function AdminDatabaseActivityPage() {
  const [stats, setStats] = useState(initialStats);
  const [logs, setLogs] = useState([]);
  const [tables, setTables] = useState([]);
  const [selected, setSelected] = useState(null);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [tableFilter, setTableFilter] = useState("ALL");
  const [operationFilter, setOperationFilter] = useState("ALL");
  const [rangeFilter, setRangeFilter] = useState("24H");

  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");

  const [autoRefresh, setAutoRefresh] = useState(true);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  // Load summary cards.
  const loadStats = useCallback(async () => {
    const response = await apiClient.get(`${BASE_PATH}/stats`);
    const data = response.data || {};

    setStats({
      totalChanges: Number(
        data.totalChanges ?? data.totalchanges ?? 0
      ),
      selects: Number(data.selects ?? 0),
      inserts: Number(data.inserts ?? 0),
      updates: Number(data.updates ?? 0),
      deletes: Number(data.deletes ?? 0),
    });
  }, []);

  // Load available database table names.
  const loadTables = useCallback(async () => {
    const response = await apiClient.get(`${BASE_PATH}/tables`);

    const data = response.data;
    const tableNames = Array.isArray(data)
      ? data
      : data?.tables || [];

    setTables(
      [...new Set(tableNames.filter((name) => typeof name === "string"))]
        .sort((a, b) => a.localeCompare(b))
    );
  }, []);

  // Load paginated activity logs.
  const loadLogs = useCallback(
    async (showLoader = false) => {
      if (showLoader) {
        setLoading(true);
      } else {
        setRefreshing(true);
      }

      try {
        const params = {
          page,
          size: PAGE_SIZE,
          table: tableFilter === "ALL" ? undefined : tableFilter,
          operation:
            operationFilter === "ALL"
              ? undefined
              : operationFilter,
          range: rangeFilter,
          search: search || undefined,
        };

        const response = await apiClient.get(`${BASE_PATH}/logs`, {
          params,
        });

        const data = response.data || {};
        const content = data.content || data.logs || [];

        setLogs(Array.isArray(content) ? content : []);
        setTotalPages(Number(data.totalPages ?? 0));
        setTotalElements(Number(data.totalElements ?? 0));
        setError("");
      } catch (err) {
        setError(
          err.response?.data?.message ||
            "Unable to load database activity. Check the backend API."
        );
      } finally {
        setLoading(false);
        setRefreshing(false);
      }
    },
    [page, tableFilter, operationFilter, rangeFilter, search]
  );

  // Load all dashboard data.
  const loadAll = useCallback(
    async (showLoader = false) => {
      const results = await Promise.allSettled([
        loadStats(),
        loadTables(),
        loadLogs(showLoader),
      ]);

      const failed = results.some(
        (result) => result.status === "rejected"
      );

      if (failed) {
        setError((current) =>
          current ||
          "Some database monitoring information could not be loaded."
        );
      }
    },
    [loadStats, loadTables, loadLogs]
  );

  // Initial load and reload when filters/page change.
useEffect(() => {
  const timer = window.setTimeout(() => {
    void loadAll(true);
  }, 0);

  return () => window.clearTimeout(timer);
}, [loadAll]);

  // Auto-refresh dashboard.
  useEffect(() => {
    if (!autoRefresh) return undefined;

    const timer = window.setInterval(() => {
      void loadAll(false);
    }, REFRESH_INTERVAL);

    return () => window.clearInterval(timer);
  }, [autoRefresh, loadAll]);

  const tableOptions = useMemo(
    () => ["ALL", ...tables],
    [tables]
  );

  const selectedColumns = selected
    ? getChangedColumns(selected)
    : [];

  const applySearch = (event) => {
    event.preventDefault();
    setPage(0);
    setSearch(searchInput.trim());
  };

  const changeFilter = (setter) => (event) => {
    setter(event.target.value);
    setPage(0);
  };

  const selectLog = (log) => {
    setSelected(log);
  };

  return (
    <div className="min-h-screen overflow-hidden bg-[#080b1b] text-white">
      <div className="relative isolate">
        <div className="pointer-events-none absolute -right-40 -top-40 -z-10 h-96 w-96 rounded-full bg-violet-700/15 blur-[120px]" />
        <div className="pointer-events-none absolute left-1/3 top-72 -z-10 h-72 w-72 rounded-full bg-cyan-500/10 blur-[110px]" />

        <div className="space-y-5 p-4 sm:p-6 xl:p-7">
          {/* Breadcrumb */}
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-white/[0.07] pb-4">
            <div className="flex items-center gap-2 text-xs text-slate-500">
              <span>Monitoring</span>
              <ChevronRight size={13} />
              <span className="font-semibold text-slate-200">
                Database Activity
              </span>
            </div>

            <div className="flex items-center gap-2 text-xs text-slate-400">
              <span className="relative flex h-2 w-2">
                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-60" />
                <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-400" />
              </span>
              Astra database monitor
            </div>
          </div>

          {/* Hero */}
          <section className="relative overflow-hidden rounded-3xl border border-violet-400/20 bg-gradient-to-br from-[#191a43] via-[#10152e] to-[#0b1025] p-5 shadow-2xl shadow-violet-950/20 sm:p-7">
            <div className="pointer-events-none absolute -right-12 -top-24 h-72 w-72 rounded-full border border-violet-400/10" />
            <div className="pointer-events-none absolute -right-4 -top-16 h-56 w-56 rounded-full border border-cyan-400/10" />
            <div className="pointer-events-none absolute bottom-0 right-0 h-40 w-2/3 bg-gradient-to-l from-violet-500/[0.08] to-transparent blur-2xl" />

            <div className="relative flex flex-col justify-between gap-6 xl:flex-row xl:items-center">
              <div className="max-w-2xl">
                <div className="flex items-center gap-3">
                  <div className="rounded-2xl border border-violet-300/20 bg-violet-400/10 p-3 text-violet-200 shadow-lg shadow-violet-900/30">
                    <Database size={28} />
                  </div>

                  <div>
                    <div className="flex flex-wrap items-center gap-2">
                      <h1 className="text-2xl font-black tracking-tight sm:text-3xl">
                        Database Activity
                      </h1>

                      <span className="rounded-full border border-emerald-400/25 bg-emerald-400/10 px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider text-emerald-300">
                        Live monitor
                      </span>
                    </div>

                    <p className="mt-1 text-sm text-slate-400">
                      Every row change. Every table. One activity stream.
                    </p>
                  </div>
                </div>

                <div className="mt-5 flex flex-wrap gap-2">
                  <span className="inline-flex items-center gap-1.5 rounded-full border border-violet-300/15 bg-violet-400/10 px-3 py-1.5 text-xs text-violet-200">
                    <Zap size={13} />
                    Change tracking
                  </span>

                  <span className="inline-flex items-center gap-1.5 rounded-full border border-cyan-300/15 bg-cyan-400/10 px-3 py-1.5 text-xs text-cyan-200">
                    <Layers3 size={13} />
                    All tables
                  </span>

                  <span className="inline-flex items-center gap-1.5 rounded-full border border-white/10 bg-white/[0.05] px-3 py-1.5 text-xs text-slate-300">
                    <ShieldCheck size={13} />
                    Audit history
                  </span>
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-2">
                <div className="flex items-center gap-2 rounded-xl border border-emerald-400/25 bg-emerald-400/10 px-3 py-2.5 text-xs font-semibold text-emerald-300">
                  <CircleDot size={13} className="animate-pulse" />
                  Monitoring active
                </div>

                <button
                  type="button"
                  onClick={() => setAutoRefresh((value) => !value)}
                  className={`rounded-xl border px-3 py-2.5 text-xs font-semibold transition ${
                    autoRefresh
                      ? "border-violet-400/30 bg-violet-500/15 text-violet-200"
                      : "border-white/10 bg-white/[0.04] text-slate-400"
                  }`}
                >
                  Auto refresh: {autoRefresh ? "ON" : "OFF"}
                </button>

                <button
                  type="button"
                  onClick={() => void loadAll(false)}
                  disabled={refreshing}
                  className="rounded-xl border border-white/10 bg-white/[0.06] p-2.5 text-slate-200 transition hover:border-violet-400/40 hover:bg-violet-500/15 disabled:opacity-50"
                  title="Refresh data"
                >
                  <RefreshCw
                    size={17}
                    className={refreshing ? "animate-spin" : ""}
                  />
                </button>
              </div>
            </div>
          </section>

          {/* Error */}
          {error && (
            <div className="flex items-start gap-3 rounded-xl border border-rose-400/25 bg-rose-400/[0.07] p-4 text-sm text-rose-200">
              <AlertCircle size={18} className="mt-0.5 shrink-0" />
              <div>
                <p className="font-semibold">
                  Monitoring API unavailable
                </p>
                <p className="mt-1 text-xs text-rose-200/70">
                  {error}
                </p>
              </div>
            </div>
          )}

          {/* Metrics */}
          <section className="grid grid-cols-1 gap-3 sm:grid-cols-2 2xl:grid-cols-4">
            <MetricCard
              title="Total database changes"
              value={stats.totalChanges}
              icon={Database}
              color="violet"
              subtitle="Captured row operations"
            />
<MetricCard
  title="SELECT operations"
  value={stats.selects}
  icon={Eye}
  color="blue"
  subtitle="Database queries captured"
/>
            <MetricCard
              title="INSERT operations"
              value={stats.inserts}
              icon={PlusCircle}
              color="green"
              subtitle="New records created"
            />

            <MetricCard
              title="UPDATE operations"
              value={stats.updates}
              icon={Pencil}
              color="blue"
              subtitle="Existing records changed"
            />

            <MetricCard
              title="DELETE operations"
              value={stats.deletes}
              icon={Trash2}
              color="red"
              subtitle="Records removed"
            />
          </section>

          {/* Filters */}
          <section className="rounded-2xl border border-white/10 bg-[#111831]/85 p-4 shadow-xl shadow-black/10">
            <div className="mb-3 flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-400">
              <ListFilter size={15} className="text-violet-300" />
              Activity filters
            </div>

            <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
              <label className="block">
                <span className="mb-1.5 block text-[11px] font-semibold text-slate-400">
                  Time range
                </span>

                <div className="relative">
                  <CalendarDays
                    size={15}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-violet-300"
                  />

                  <select
                    value={rangeFilter}
                    onChange={changeFilter(setRangeFilter)}
                    className="w-full appearance-none rounded-xl border border-white/10 bg-[#0b1228] py-3 pl-9 pr-3 text-xs text-slate-200 outline-none focus:border-violet-400/60"
                  >
                    <option value="1H">Last hour</option>
                    <option value="24H">Last 24 hours</option>
                    <option value="7D">Last 7 days</option>
                    <option value="30D">Last 30 days</option>
                    <option value="90D">Last 90 days</option>
                    <option value="ALL">All time</option>
                  </select>
                </div>
              </label>

              <label className="block">
                <span className="mb-1.5 block text-[11px] font-semibold text-slate-400">
                  Database table
                </span>

                <div className="relative">
                  <Table2
                    size={15}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-violet-300"
                  />

                  <select
                    value={tableFilter}
                    onChange={changeFilter(setTableFilter)}
                    className="w-full appearance-none rounded-xl border border-white/10 bg-[#0b1228] py-3 pl-9 pr-3 text-xs text-slate-200 outline-none focus:border-violet-400/60"
                  >
                    <option value="ALL">All tables</option>

                    {tableOptions
                      .filter((item) => item !== "ALL")
                      .map((table) => (
                        <option key={table} value={table}>
                          {table}
                        </option>
                      ))}
                  </select>
                </div>
              </label>

              <label className="block">
                <span className="mb-1.5 block text-[11px] font-semibold text-slate-400">
                  Operation
                </span>

                <div className="relative">
                  <ArrowDownUp
                    size={15}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-violet-300"
                  />

               <select
  value={operationFilter}
  onChange={changeFilter(setOperationFilter)}
  className="w-full appearance-none rounded-xl border border-white/10 bg-[#0b1228] py-3 pl-9 pr-3 text-xs text-slate-200 outline-none focus:border-violet-400/60"
>
  <option value="ALL">All operations</option>
  <option value="INSERT">INSERT</option>
  <option value="UPDATE">UPDATE</option>
  <option value="DELETE">DELETE</option>
  <option value="SELECT">SELECT</option>
</select>
                </div>
              </label>

              <form onSubmit={applySearch}>
                <span className="mb-1.5 block text-[11px] font-semibold text-slate-400">
                  Search activity
                </span>

                <div className="flex rounded-xl border border-white/10 bg-[#0b1228] focus-within:border-violet-400/60">
                  <Search
                    size={16}
                    className="ml-3 mt-3 shrink-0 text-violet-300"
                  />

                  <input
                    value={searchInput}
                    onChange={(event) =>
                      setSearchInput(event.target.value)
                    }
                    placeholder="Table, record ID, column..."
                    className="min-w-0 flex-1 bg-transparent px-2 py-3 text-xs text-white outline-none placeholder:text-slate-600"
                  />

                  <button
                    type="submit"
                    className="m-1 rounded-lg bg-violet-600 px-3 text-xs font-bold text-white hover:bg-violet-500"
                  >
                    Go
                  </button>
                </div>
              </form>
            </div>
          </section>

          {/* Activity and detail */}
          <section className="grid min-w-0 grid-cols-1 gap-4 2xl:grid-cols-[minmax(0,1.7fr)_minmax(350px,0.8fr)]">
            {/* Activity table */}
            <div className="min-w-0 overflow-hidden rounded-2xl border border-violet-400/15 bg-[#111831]/90 shadow-xl shadow-black/10">
              <div className="flex flex-wrap items-center justify-between gap-3 border-b border-white/[0.07] p-4">
                <div>
                  <h2 className="flex items-center gap-2 text-sm font-bold text-white">
                    <Activity size={17} className="text-violet-300" />
                    Change activity stream
                  </h2>

                  <p className="mt-1 text-[11px] text-slate-500">
                    {totalElements.toLocaleString()} total database changes
                  </p>
                </div>

                <div className="flex items-center gap-2 text-[11px] text-slate-400">
                  {refreshing && (
                    <LoaderCircle
                      size={13}
                      className="animate-spin"
                    />
                  )}
                  <span>Newest first</span>
                </div>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full min-w-[850px] text-left">
                  <thead>
                    <tr className="border-b border-white/[0.07] bg-white/[0.025] text-[10px] uppercase tracking-wider text-slate-500">
                      <th className="px-4 py-3">Changed at</th>
                      <th className="px-3 py-3">Table</th>
                      <th className="px-3 py-3">Operation</th>
                      <th className="px-3 py-3">Record ID</th>
                      <th className="px-3 py-3">Changed columns</th>
                      <th className="px-3 py-3">DB user</th>
                      <th className="px-3 py-3 text-right">View</th>
                    </tr>
                  </thead>

                  <tbody>
                    {loading ? (
                      <tr>
                        <td colSpan={7} className="py-16 text-center">
                          <LoaderCircle
                            size={24}
                            className="mx-auto animate-spin text-violet-300"
                          />
                          <p className="mt-3 text-xs text-slate-500">
                            Loading database activity...
                          </p>
                        </td>
                      </tr>
                    ) : logs.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="py-16 text-center">
                          <Database
                            size={28}
                            className="mx-auto text-slate-600"
                          />

                          <p className="mt-3 text-sm font-semibold text-slate-300">
                            No database changes found
                          </p>

                          <p className="mt-1 text-xs text-slate-500">
                            Try changing your filters or check audit triggers.
                          </p>
                        </td>
                      </tr>
                    ) : (
                      logs.map((log) => {
                        const active = selected?.id === log.id;
                        const columns = getChangedColumns(log);

                        return (
                          <tr
                            key={log.id}
                            onClick={() => selectLog(log)}
                            className={`cursor-pointer border-b border-white/[0.045] transition ${
                              active
                                ? "bg-violet-500/15"
                                : "hover:bg-white/[0.035]"
                            }`}
                          >
                            <td className="whitespace-nowrap px-4 py-3 text-[11px] text-slate-400">
                              {formatDate(log.changedAt)}
                            </td>

                            <td className="px-3 py-3">
                              <span className="inline-flex items-center gap-2 text-xs font-semibold text-slate-200">
                                <Table2
                                  size={13}
                                  className="text-violet-300"
                                />
                                {log.tableName}
                              </span>
                            </td>

                            <td className="px-3 py-3">
                              <OperationBadge
                                operation={log.operation}
                              />
                            </td>

                            <td className="max-w-36 truncate px-3 py-3 font-mono text-[10px] text-slate-400">
                              {getRecordId(log.recordId)}
                            </td>

                            <td className="px-3 py-3">
                              <div className="flex max-w-44 flex-wrap gap-1">
                                {columns.length ? (
                                  columns.slice(0, 3).map((column) => (
                                    <span
                                      key={column}
                                      className="rounded-md border border-violet-300/10 bg-violet-400/10 px-1.5 py-1 text-[10px] text-violet-200"
                                    >
                                      {column}
                                    </span>
                                  ))
                                ) : (
                                  <span className="text-xs text-slate-600">
                                    —
                                  </span>
                                )}

                                {columns.length > 3 && (
                                  <span className="text-[10px] text-slate-500">
                                    +{columns.length - 3}
                                  </span>
                                )}
                              </div>
                            </td>

                            <td className="px-3 py-3 text-[11px] text-slate-400">
                              <span className="inline-flex items-center gap-1.5">
                                <UserRound size={12} />
                                {log.databaseUser || "—"}
                              </span>
                            </td>

                            <td className="px-3 py-3 text-right">
                              <button
                                type="button"
                                onClick={(event) => {
                                  event.stopPropagation();
                                  selectLog(log);
                                }}
                                className="rounded-lg border border-violet-400/20 bg-violet-500/10 p-2 text-violet-200 transition hover:bg-violet-500/25"
                                aria-label="View change details"
                              >
                                <Eye size={14} />
                              </button>
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              <div className="flex flex-wrap items-center justify-between gap-3 border-t border-white/[0.07] p-4">
                <p className="text-[11px] text-slate-500">
                  Page {totalPages ? page + 1 : 0} of {totalPages}
                  {" · "}
                  {totalElements.toLocaleString()} records
                </p>

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    disabled={page <= 0 || loading}
                    onClick={() =>
                      setPage((value) => Math.max(0, value - 1))
                    }
                    className="rounded-lg border border-white/10 p-2 text-slate-300 hover:bg-white/5 disabled:opacity-30"
                    aria-label="Previous page"
                  >
                    <ChevronLeft size={16} />
                  </button>

                  <span className="min-w-8 rounded-lg bg-violet-600 px-3 py-2 text-center text-xs font-bold">
                    {totalPages ? page + 1 : 0}
                  </span>

                  <button
                    type="button"
                    disabled={page + 1 >= totalPages || loading}
                    onClick={() => setPage((value) => value + 1)}
                    className="rounded-lg border border-white/10 p-2 text-slate-300 hover:bg-white/5 disabled:opacity-30"
                    aria-label="Next page"
                  >
                    <ChevronRight size={16} />
                  </button>
                </div>
              </div>
            </div>

            {/* Detail inspector */}
            <aside className="min-w-0 overflow-hidden rounded-2xl border border-violet-400/20 bg-gradient-to-b from-[#191a43]/90 to-[#10162d]/95 shadow-xl shadow-violet-950/20">
              <div className="flex items-center justify-between border-b border-white/[0.08] p-4">
                <h2 className="flex items-center gap-2 text-sm font-bold">
                  <Server size={17} className="text-violet-300" />
                  Change inspector
                </h2>

                {selected && (
                  <button
                    type="button"
                    onClick={() => setSelected(null)}
                    className="rounded-lg p-1.5 text-slate-400 hover:bg-white/10 hover:text-white"
                    aria-label="Close details"
                  >
                    <X size={17} />
                  </button>
                )}
              </div>

              {!selected ? (
                <div className="flex min-h-[430px] flex-col items-center justify-center p-6 text-center">
                  <div className="rounded-2xl border border-violet-400/20 bg-violet-400/10 p-5 text-violet-300">
                    <Eye size={30} />
                  </div>

                  <h3 className="mt-4 text-sm font-bold text-slate-200">
                    Inspect a database change
                  </h3>

                  <p className="mt-2 max-w-xs text-xs leading-5 text-slate-500">
                    Select any activity from the table to compare previous and
                    current database values.
                  </p>
                </div>
              ) : (
                <div className="space-y-4 p-4">
                  <div className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-white/10 bg-white/[0.04] p-3">
                    <OperationBadge operation={selected.operation} />

                    <span className="text-[11px] text-slate-400">
                      {selected.tableName}
                    </span>
                  </div>

                  <div className="space-y-3">
                    <div className="flex items-start gap-3">
                      <Clock3
                        size={15}
                        className="mt-0.5 text-violet-300"
                      />

                      <div className="min-w-0">
                        <p className="text-[10px] text-slate-500">
                          Changed at
                        </p>

                        <p className="break-all text-xs text-slate-200">
                          {formatDate(selected.changedAt)}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <UserRound
                        size={15}
                        className="mt-0.5 text-violet-300"
                      />

                      <div>
                        <p className="text-[10px] text-slate-500">
                          Database user
                        </p>

                        <p className="text-xs text-slate-200">
                          {selected.databaseUser || "Unknown"}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <Database
                        size={15}
                        className="mt-0.5 text-violet-300"
                      />

                      <div className="min-w-0">
                        <p className="text-[10px] text-slate-500">
                          Record ID
                        </p>

                        <p className="break-all font-mono text-xs text-slate-200">
                          {getRecordId(selected.recordId)}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <Activity
                        size={15}
                        className="mt-0.5 text-violet-300"
                      />

                      <div>
                        <p className="text-[10px] text-slate-500">
                          Connection ID
                        </p>

                        <p className="text-xs text-slate-200">
                          {selected.connectionId ?? "—"}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Changed columns */}
                  <div className="border-t border-white/10 pt-4">
                    <p className="mb-2 text-xs font-bold text-slate-200">
                      Changed columns
                    </p>

                    <div className="flex flex-wrap gap-1.5">
                      {selectedColumns.length ? (
                        selectedColumns.map((column) => (
                          <span
                            key={column}
                            className="rounded-lg border border-violet-300/20 bg-violet-500/15 px-2.5 py-1 text-[10px] font-semibold text-violet-200"
                          >
                            {column}
                          </span>
                        ))
                      ) : (
                        <span className="text-xs text-slate-500">
                          No column comparison available
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Old and new data */}
                  <div className="grid grid-cols-1 gap-3">
                    {selected.operation !== "INSERT" && (
                      <JsonPanel
                        title="Old data · Before"
                        value={selected.oldData}
                        variant="old"
                      />
                    )}

                    {selected.operation !== "DELETE" && (
                      <JsonPanel
                        title="New data · After"
                        value={selected.newData}
                        variant="new"
                      />
                    )}
                  </div>

                  <div className="rounded-xl border border-white/10 bg-black/10 p-3">
                    <p className="mb-1 text-[10px] font-semibold uppercase tracking-wider text-slate-500">
                      Audit record
                    </p>

                    <p className="font-mono text-[11px] text-slate-400">
                      #{selected.id}
                    </p>
                  </div>
                </div>
              )}
            </aside>
          </section>

          {/* Footer */}
          <footer className="flex flex-wrap items-center justify-between gap-2 pb-2 text-[10px] text-slate-600">
            <span className="flex items-center gap-1.5">
              <ShieldCheck size={12} />
              Astra database audit history
            </span>

            <span>Auto refresh interval: 10 seconds</span>
          </footer>
        </div>
      </div>
    </div>
  );
}

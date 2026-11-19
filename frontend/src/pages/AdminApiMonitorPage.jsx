
import { useCallback, useEffect, useState } from "react";
import {
  Activity,
  AlertCircle,
  ArrowUpRight,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Globe,
  Layers3,
  RefreshCw,
  Search,
  Server,
  ShieldCheck,
  X,
  Zap,
} from "lucide-react";

import apiMonitorService from "../services/apiMonitorService";

const PAGE_SIZE = 20;

const EMPTY_STATS = {
  totalRequests: 0,
  successfulRequests: 0,
  failedRequests: 0,
  averageResponseTime: 0,
  frontendRequests: 0,
  backendRequests: 0,
};

function formatDate(value) {
  if (!value) return "—";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return value;

  return date.toLocaleString();
}

function formatDuration(value) {
  const duration = Number(value || 0);

  if (duration < 1000) return `${duration.toFixed(0)} ms`;

  return `${(duration / 1000).toFixed(2)} s`;
}

function formatJson(value) {
  if (value === null || value === undefined || value === "") {
    return "No data captured";
  }

  if (typeof value === "object") {
    return JSON.stringify(value, null, 2);
  }

  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return String(value);
  }
}

function StatCard({ title, value, subtitle, icon: Icon, color }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-[#11111b] p-5">
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-400">{title}</p>
        <div className={`rounded-xl p-2 ${color}`}>
          <Icon size={19} />
        </div>
      </div>

      <p className="mt-4 text-3xl font-bold text-white">
        {value}
      </p>

      <p className="mt-2 text-xs text-gray-500">{subtitle}</p>
    </div>
  );
}

function StatusBadge({ status }) {
  const code = Number(status);

  let classes = "border-gray-500/20 bg-gray-500/10 text-gray-400";

  if (code >= 200 && code < 300) {
    classes = "border-green-500/20 bg-green-500/10 text-green-400";
  } else if (code >= 300 && code < 400) {
    classes = "border-blue-500/20 bg-blue-500/10 text-blue-400";
  } else if (code >= 400 && code < 500) {
    classes = "border-yellow-500/20 bg-yellow-500/10 text-yellow-400";
  } else if (code >= 500) {
    classes = "border-red-500/20 bg-red-500/10 text-red-400";
  }

  return (
    <span
      className={`inline-flex rounded-lg border px-2.5 py-1 text-xs font-semibold ${classes}`}
    >
      {code || "ERR"}
    </span>
  );
}

function SourceBadge({ source }) {
  const isFrontend = source === "FRONTEND";

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1 text-xs ${
        isFrontend
          ? "bg-violet-500/10 text-violet-400"
          : "bg-cyan-500/10 text-cyan-400"
      }`}
    >
      {isFrontend ? <Globe size={13} /> : <Server size={13} />}
      {source || "UNKNOWN"}
    </span>
  );
}

function DetailSection({ title, value }) {
  return (
    <div className="mt-5">
      <h4 className="mb-2 text-sm font-semibold text-gray-300">
        {title}
      </h4>

      <pre className="max-h-72 overflow-auto whitespace-pre-wrap break-all rounded-xl border border-white/10 bg-[#09090f] p-4 text-xs leading-6 text-gray-300">
        {formatJson(value)}
      </pre>
    </div>
  );
}

function RequestDetails({ log, onClose }) {
  if (!log) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/70">
      <button
        type="button"
        aria-label="Close request details"
        onClick={onClose}
        className="absolute inset-0 cursor-default"
      />

      <section className="relative z-10 flex h-full w-full max-w-2xl flex-col border-l border-white/10 bg-[#10101a] shadow-2xl">
        <header className="flex items-center justify-between border-b border-white/10 p-5">
          <div>
            <p className="text-lg font-semibold text-white">
              Request details
            </p>
            <p className="mt-1 text-xs text-gray-500">
              Request ID: {log.requestId || log.id}
            </p>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-lg border border-white/10 p-2 text-gray-400 hover:bg-white/5 hover:text-white"
          >
            <X size={19} />
          </button>
        </header>

        <div className="flex-1 overflow-y-auto p-5">
          <div className="flex flex-wrap items-center gap-2">
            <span className="rounded-lg bg-violet-500/10 px-3 py-1.5 text-sm font-bold text-violet-400">
              {log.method}
            </span>

            <StatusBadge status={log.status} />
            <SourceBadge source={log.source} />
          </div>

          <div className="mt-4 break-all rounded-xl border border-white/10 bg-[#09090f] p-4 text-sm text-gray-200">
            {log.endpoint}
          </div>

          <div className="mt-5 grid grid-cols-2 gap-3">
            <div className="rounded-xl border border-white/10 bg-[#171722] p-4">
              <p className="text-xs text-gray-500">Duration</p>
              <p className="mt-2 text-lg font-semibold text-white">
                {formatDuration(log.durationMs)}
              </p>
            </div>

            <div className="rounded-xl border border-white/10 bg-[#171722] p-4">
              <p className="text-xs text-gray-500">Timestamp</p>
              <p className="mt-2 text-sm font-medium text-white">
                {formatDate(log.timestamp)}
              </p>
            </div>
          </div>

          <div className="mt-5 space-y-2 text-sm">
            <p className="text-gray-400">
              Client IP:{" "}
              <span className="text-gray-200">
                {log.clientIp || "Not available"}
              </span>
            </p>

            <p className="break-all text-gray-400">
              User Agent:{" "}
              <span className="text-gray-200">
                {log.userAgent || "Not available"}
              </span>
            </p>

            {log.error && (
              <div className="rounded-xl border border-red-500/20 bg-red-500/10 p-3 text-sm text-red-300">
                {log.error}
              </div>
            )}
          </div>

          <DetailSection
            title="Request headers"
            value={log.requestHeaders}
          />

          <DetailSection
            title="Request body"
            value={log.requestBody}
          />

          <DetailSection
            title="Response headers"
            value={log.responseHeaders}
          />

          <DetailSection
            title="Response body"
            value={log.responseBody}
          />
        </div>
      </section>
    </div>
  );
}

export default function AdminApiMonitorPage() {
  const [stats, setStats] = useState(EMPTY_STATS);
  const [logs, setLogs] = useState([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [source, setSource] = useState("ALL");
  const [status, setStatus] = useState("ALL");
  const [search, setSearch] = useState("");
  const [appliedSearch, setAppliedSearch] = useState("");

  const [selectedLog, setSelectedLog] = useState(null);

  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState("");

  const loadStats = useCallback(async () => {
    setStatsLoading(true);

    try {
      const result = await apiMonitorService.getStats();
      setStats({ ...EMPTY_STATS, ...result });
    } catch (err) {
      console.error("API monitor stats error:", err);
      setError(
        err.response?.data?.message ||
          "Unable to load API monitoring statistics."
      );
    } finally {
      setStatsLoading(false);
    }
  }, []);

  const loadLogs = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const result = await apiMonitorService.getLogs({
        page,
        size: PAGE_SIZE,
        source,
        status,
        search: appliedSearch,
      });

      setLogs(result.content || []);
      setTotalPages(result.totalPages || 0);
      setTotalElements(result.totalElements || 0);
    } catch (err) {
      console.error("API monitor logs error:", err);
      setLogs([]);
      setError(
        err.response?.data?.message ||
          "Unable to load API request logs. Check your admin access and backend."
      );
    } finally {
      setLoading(false);
    }
  }, [page, source, status, appliedSearch]);

  const refreshDashboard = useCallback(() => {
    loadStats();
    loadLogs();
  }, [loadStats, loadLogs]);

useEffect(() => {
  const timer = setTimeout(() => {
    loadStats();
  }, 0);

  return () => clearTimeout(timer);
}, [loadStats]);

useEffect(() => {
  const timer = setTimeout(() => {
    loadLogs();
  }, 0);

  return () => clearTimeout(timer);
}, [loadLogs]);

  async function openDetails(id) {
    setDetailLoading(true);
    setSelectedLog(null);

    try {
      const result = await apiMonitorService.getLogDetails(id);
      setSelectedLog(result);
    } catch (err) {
      console.error("API request details error:", err);
      setError(
        err.response?.data?.message ||
          "Unable to load request details."
      );
    } finally {
      setDetailLoading(false);
    }
  }

  function applySearch(event) {
    event.preventDefault();
    setPage(0);
    setAppliedSearch(search.trim());
  }

  function changeSource(value) {
    setSource(value);
    setPage(0);
  }

  function changeStatus(value) {
    setStatus(value);
    setPage(0);
  }

  const statCards = [
    {
      title: "Total requests",
      value: stats.totalRequests,
      subtitle: "Last 24 hours",
      icon: Activity,
      color: "bg-violet-500/10 text-violet-400",
    },
    {
      title: "Successful requests",
      value: stats.successfulRequests,
      subtitle: "Last 24 hours",
      icon: CheckCircle2,
      color: "bg-green-500/10 text-green-400",
    },
    {
      title: "Failed requests",
      value: stats.failedRequests,
      subtitle: "Last 24 hours",
      icon: AlertCircle,
      color: "bg-red-500/10 text-red-400",
    },
    {
      title: "Average response",
      value: formatDuration(stats.averageResponseTime),
      subtitle: "Last 24 hours",
      icon: Zap,
      color: "bg-yellow-500/10 text-yellow-400",
    },
  ];

  return (
    <div className="min-h-screen bg-[#09090f] p-4 text-white sm:p-6 lg:p-8">
      <div className="mx-auto max-w-[1600px]">
        {/* Header */}
        <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <div className="flex items-center gap-2">
              <Activity className="text-violet-400" size={24} />
              <h1 className="text-2xl font-bold tracking-tight">
                HTTP Request Monitor
              </h1>
            </div>

            <p className="mt-2 text-sm text-gray-400">
              Monitor frontend and backend API traffic in real time.
            </p>
          </div>

          <button
            type="button"
            onClick={refreshDashboard}
            disabled={loading || statsLoading}
            className="inline-flex items-center justify-center gap-2 rounded-xl border border-white/10 bg-[#171722] px-4 py-2.5 text-sm text-gray-200 transition hover:bg-white/10 disabled:opacity-50"
          >
            <RefreshCw
              size={16}
              className={
                loading || statsLoading ? "animate-spin" : ""
              }
            />
            Refresh
          </button>
        </div>

        {/* Security notice */}
        <div className="mt-6 flex items-start gap-3 rounded-xl border border-green-500/20 bg-green-500/[0.06] p-4">
          <ShieldCheck
            size={20}
            className="mt-0.5 shrink-0 text-green-400"
          />

          <div>
            <p className="text-sm font-semibold text-green-300">
              Sensitive data protection enabled
            </p>
            <p className="mt-1 text-xs leading-5 text-gray-400">
              Authorization tokens, passwords, cookies and other
              sensitive fields are redacted before log storage.
            </p>
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="mt-5 flex items-start gap-3 rounded-xl border border-red-500/20 bg-red-500/10 p-4">
            <AlertCircle
              size={19}
              className="mt-0.5 shrink-0 text-red-400"
            />

            <div className="flex-1 text-sm text-red-300">
              {error}
            </div>

            <button
              type="button"
              onClick={() => setError("")}
              aria-label="Dismiss error"
              className="text-red-300 hover:text-white"
            >
              <X size={17} />
            </button>
          </div>
        )}

        {/* Stats */}
        <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {statCards.map((item) => (
            <StatCard
              key={item.title}
              {...item}
              value={
                statsLoading
                  ? "..."
                  : item.value
              }
            />
          ))}
        </div>

        {/* Traffic source summary */}
        <div className="mt-5 grid grid-cols-1 gap-4 md:grid-cols-2">
          <div className="rounded-2xl border border-white/10 bg-[#11111b] p-5">
            <div className="flex items-center gap-3">
              <div className="rounded-xl bg-violet-500/10 p-3 text-violet-400">
                <Globe size={21} />
              </div>

              <div>
                <p className="text-sm text-gray-400">
                  Frontend requests
                </p>
                <p className="mt-1 text-2xl font-bold">
                  {statsLoading ? "..." : stats.frontendRequests}
                </p>
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-white/10 bg-[#11111b] p-5">
            <div className="flex items-center gap-3">
              <div className="rounded-xl bg-cyan-500/10 p-3 text-cyan-400">
                <Server size={21} />
              </div>

              <div>
                <p className="text-sm text-gray-400">
                  Backend requests
                </p>
                <p className="mt-1 text-2xl font-bold">
                  {statsLoading ? "..." : stats.backendRequests}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Filters */}
        <div className="mt-7 rounded-2xl border border-white/10 bg-[#11111b] p-4 sm:p-5">
          <div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
            <div>
              <h2 className="text-lg font-semibold">
                Request logs
              </h2>
              <p className="mt-1 text-xs text-gray-500">
                {totalElements} records found
              </p>
            </div>

            <div className="flex flex-wrap gap-2">
              {["ALL", "FRONTEND", "BACKEND"].map((item) => (
                <button
                  type="button"
                  key={item}
                  onClick={() => changeSource(item)}
                  className={`rounded-xl px-4 py-2 text-xs font-semibold transition ${
                    source === item
                      ? "bg-violet-600 text-white"
                      : "border border-white/10 bg-[#09090f] text-gray-400 hover:text-white"
                  }`}
                >
                  {item === "ALL" ? "All requests" : item}
                </button>
              ))}
            </div>
          </div>

          <div className="mt-5 flex flex-col gap-3 lg:flex-row">
            <form
              onSubmit={applySearch}
              className="flex min-w-0 flex-1 gap-2"
            >
              <div className="relative min-w-0 flex-1">
                <Search
                  size={17}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500"
                />

                <input
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Search endpoint, method, request ID..."
                  className="w-full rounded-xl border border-white/10 bg-[#09090f] py-3 pl-10 pr-4 text-sm text-white outline-none placeholder:text-gray-600 focus:border-violet-500"
                />
              </div>

              <button
                type="submit"
                className="rounded-xl bg-violet-600 px-4 text-sm font-semibold text-white hover:bg-violet-500"
              >
                Search
              </button>
            </form>

            <select
              value={status}
              onChange={(event) => changeStatus(event.target.value)}
              className="rounded-xl border border-white/10 bg-[#09090f] px-4 py-3 text-sm text-gray-300 outline-none focus:border-violet-500"
            >
              <option value="ALL">All statuses</option>
              <option value="200">200 OK</option>
              <option value="201">201 Created</option>
              <option value="204">204 No Content</option>
              <option value="400">400 Bad Request</option>
              <option value="401">401 Unauthorized</option>
              <option value="403">403 Forbidden</option>
              <option value="404">404 Not Found</option>
              <option value="500">500 Server Error</option>
            </select>
          </div>
        </div>

        {/* Request table */}
        <div className="mt-4 overflow-hidden rounded-2xl border border-white/10 bg-[#11111b]">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[950px] text-left">
              <thead className="border-b border-white/10 bg-[#171722]">
                <tr className="text-xs uppercase tracking-wider text-gray-500">
                  <th className="px-5 py-4 font-medium">Time</th>
                  <th className="px-5 py-4 font-medium">Source</th>
                  <th className="px-5 py-4 font-medium">Method</th>
                  <th className="px-5 py-4 font-medium">Endpoint</th>
                  <th className="px-5 py-4 font-medium">Status</th>
                  <th className="px-5 py-4 font-medium">Duration</th>
                  <th className="px-5 py-4 text-right font-medium">
                    Details
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-white/5">
                {loading ? (
                  <tr>
                    <td colSpan={7} className="px-5 py-16 text-center">
                      <RefreshCw
                        size={22}
                        className="mx-auto animate-spin text-violet-400"
                      />
                      <p className="mt-3 text-sm text-gray-500">
                        Loading request logs...
                      </p>
                    </td>
                  </tr>
                ) : logs.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-5 py-16 text-center">
                      <Layers3
                        size={30}
                        className="mx-auto text-gray-600"
                      />
                      <p className="mt-3 text-sm font-medium text-gray-300">
                        No request logs found
                      </p>
                      <p className="mt-1 text-xs text-gray-500">
                        API requests will appear here when monitoring
                        records are available.
                      </p>
                    </td>
                  </tr>
                ) : (
                  logs.map((log) => (
                    <tr
                      key={log.id}
                      className="cursor-pointer transition hover:bg-white/[0.03]"
                      onClick={() => openDetails(log.id)}
                    >
                      <td className="whitespace-nowrap px-5 py-4 text-xs text-gray-400">
                        {formatDate(log.timestamp)}
                      </td>

                      <td className="px-5 py-4">
                        <SourceBadge source={log.source} />
                      </td>

                      <td className="px-5 py-4">
                        <span className="rounded-lg bg-violet-500/10 px-2.5 py-1 text-xs font-bold text-violet-400">
                          {log.method}
                        </span>
                      </td>

                      <td className="max-w-[350px] px-5 py-4">
                        <p className="truncate text-sm text-gray-200">
                          {log.endpoint}
                        </p>
                        <p className="mt-1 text-xs text-gray-600">
                          ID: {log.requestId || log.id}
                        </p>
                      </td>

                      <td className="px-5 py-4">
                        <StatusBadge status={log.status} />
                      </td>

                      <td className="whitespace-nowrap px-5 py-4 text-sm text-gray-300">
                        {formatDuration(log.durationMs)}
                      </td>

                      <td className="px-5 py-4 text-right">
                        <button
                          type="button"
                          onClick={(event) => {
                            event.stopPropagation();
                            openDetails(log.id);
                          }}
                          className="rounded-lg border border-white/10 p-2 text-gray-400 hover:bg-violet-500/10 hover:text-violet-400"
                          aria-label="View request details"
                        >
                          <ArrowUpRight size={16} />
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          <div className="flex flex-col gap-3 border-t border-white/10 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-xs text-gray-500">
              Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
              {" · "}
              {totalElements} total requests
            </p>

            <div className="flex items-center gap-2">
              <button
                type="button"
                disabled={page <= 0 || loading}
                onClick={() => setPage((current) => current - 1)}
                className="inline-flex items-center gap-1 rounded-lg border border-white/10 px-3 py-2 text-xs text-gray-300 hover:bg-white/5 disabled:cursor-not-allowed disabled:opacity-30"
              >
                <ChevronLeft size={15} />
                Previous
              </button>

              <button
                type="button"
                disabled={page + 1 >= totalPages || loading}
                onClick={() => setPage((current) => current + 1)}
                className="inline-flex items-center gap-1 rounded-lg border border-white/10 px-3 py-2 text-xs text-gray-300 hover:bg-white/5 disabled:cursor-not-allowed disabled:opacity-30"
              >
                Next
                <ChevronRight size={15} />
              </button>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="mt-5 flex items-center gap-2 text-xs text-gray-600">
          <ShieldCheck size={15} />
          <span>
            Request details are available only to authorized administrators.
          </span>
        </div>
      </div>

      {detailLoading && (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/60">
          <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-[#171722] px-5 py-4 text-sm text-gray-300">
            <RefreshCw size={18} className="animate-spin text-violet-400" />
            Loading request details...
          </div>
        </div>
      )}

      {selectedLog && (
        <RequestDetails
          log={selectedLog}
          onClose={() => setSelectedLog(null)}
        />
      )}
    </div>
  );
}

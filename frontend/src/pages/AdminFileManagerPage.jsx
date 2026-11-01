import { useCallback, useEffect, useState } from "react";
import { Download, FileUp, RefreshCw, Trash2 } from "lucide-react";
import fileService from "../services/fileService";

const inputClass =
  "w-full rounded-xl border border-white/10 bg-[#09090f] px-4 py-3 text-sm text-white";

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();

  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export default function AdminFileManagerPage() {
  const [files, setFiles] = useState([]);
  const [file, setFile] = useState(null);
  const [purpose, setPurpose] = useState("DOCUMENT");
  const [productId, setProductId] = useState("");
  const [loading, setLoading] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadFiles = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      setFiles(await fileService.getAdminFiles());
    } catch (err) {
      setError(
        err.response?.data?.detail ||
        err.response?.data?.message ||
        "Unable to load files."
      );
    } finally {
      setLoading(false);
    }
  }, []);

useEffect(() => {
  let cancelled = false;

  async function fetchFiles() {
    try {
      const response = await fileService.getMyFiles();
      if (!cancelled) {
        setFiles(response);
      }
    } catch (error) {
      if (!cancelled) {
        setError(error.message || "Failed to load files");
      }
    } finally {
      if (!cancelled) {
        setLoading(false);
      }
    }
  }

  void fetchFiles();

  return () => {
    cancelled = true;
  };
}, []);

  async function handleUpload(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!file) {
      setError("Select a file first.");
      return;
    }

    if (purpose === "PRODUCT_IMAGE" &&
        (!productId || !Number.isInteger(Number(productId)) ||
         Number(productId) <= 0)) {
      setError("Enter a valid existing product ID.");
      return;
    }

    setBusyId("upload");

    try {
      const formData = new FormData();
      formData.append("file", file);

      const api = (await import("../services/api")).default;

      await api.post("/files/admin/upload", formData, {
        params: {
          purpose,
          ...(purpose === "PRODUCT_IMAGE"
            ? { entityId: Number(productId) }
            : {}),
        },
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 60000,
      });

      setFile(null);
      setProductId("");
      event.currentTarget.reset();
      setMessage("File uploaded successfully.");
      await loadFiles();
    } catch (err) {
      setError(
        err.response?.data?.detail ||
        err.response?.data?.message ||
        "Upload failed."
      );
    } finally {
      setBusyId(null);
    }
  }

  async function handleDownload(item) {
    setError("");
    setBusyId(item.id);

    try {
      const blob = await fileService.downloadFile(item.id);
      downloadBlob(blob, item.originalFilename);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to download this file."
      );
    } finally {
      setBusyId(null);
    }
  }

  async function handleDelete(item) {
    if (!window.confirm(`Delete "${item.originalFilename}"?`)) {
      return;
    }

    setError("");
    setMessage("");
    setBusyId(item.id);

    try {
      await fileService.deleteAdminFile(item.id);
      setFiles((current) =>
        current.filter((entry) => entry.id !== item.id)
      );
      setMessage("File deleted.");
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to delete this file."
      );
    } finally {
      setBusyId(null);
    }
  }

  return (
    <section className="space-y-6 p-5 text-white md:p-8">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">File Manager</h1>
          <p className="mt-1 text-sm text-gray-400">
            Upload, download and manage Astra files.
          </p>
        </div>

        <button
          type="button"
          onClick={loadFiles}
          className="flex items-center gap-2 rounded-xl border border-white/10 px-4 py-2"
        >
          <RefreshCw size={16} /> Refresh
        </button>
      </div>

      {error && <p role="alert" className="text-sm text-red-400">{error}</p>}
      {message && <p role="status" className="text-sm text-green-400">{message}</p>}

      <form
        onSubmit={handleUpload}
        className="space-y-4 rounded-2xl border border-white/10 bg-white/[0.02] p-5"
      >
        <h2 className="font-semibold">Upload file</h2>

        <label className="block space-y-2 text-sm text-gray-300">
          File purpose
          <select
            className={inputClass}
            value={purpose}
            onChange={(event) => setPurpose(event.target.value)}
          >
            <option value="DOCUMENT">Document</option>
            <option value="PRODUCT_IMAGE">Product image</option>
          </select>
        </label>

        {purpose === "PRODUCT_IMAGE" && (
          <label className="block space-y-2 text-sm text-gray-300">
            Existing product ID
            <input
              className={inputClass}
              value={productId}
              onChange={(event) => setProductId(event.target.value)}
              inputMode="numeric"
              required
            />
          </label>
        )}

        <label className="block space-y-2 text-sm text-gray-300">
          Choose file
          <input
            className={inputClass}
            type="file"
            accept={
              purpose === "PRODUCT_IMAGE"
                ? "image/jpeg,image/png,image/webp"
                : ".pdf,.doc,.docx,.xls,.xlsx,.csv,.txt,.rtf,.ppt,.pptx"
            }
            onChange={(event) =>
              setFile(event.target.files?.[0] || null)
            }
            required
          />
        </label>

        <button
          type="submit"
          disabled={busyId === "upload"}
          className="flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold disabled:opacity-50"
        >
          <FileUp size={17} />
          {busyId === "upload" ? "Uploading…" : "Upload"}
        </button>
      </form>

      <div className="overflow-x-auto rounded-2xl border border-white/10">
        <table className="w-full text-left text-sm">
          <thead className="bg-white/5 text-gray-400">
            <tr>
              <th className="p-4">File</th>
              <th className="p-4">Purpose</th>
              <th className="p-4">Size</th>
              <th className="p-4">Owner</th>
              <th className="p-4">Actions</th>
            </tr>
          </thead>

          <tbody>
            {files.map((item) => (
              <tr key={item.id} className="border-t border-white/10">
                <td className="max-w-xs break-words p-4">
                  {item.originalFilename}
                </td>
                <td className="p-4">{item.purpose}</td>
                <td className="p-4">{formatSize(item.fileSize)}</td>
                <td className="p-4">{item.ownerUsername || "—"}</td>
                <td className="p-4">
                  <div className="flex gap-2">
                    <button
                      type="button"
                      aria-label={`Download ${item.originalFilename}`}
                      disabled={busyId === item.id}
                      onClick={() => handleDownload(item)}
                      className="rounded-lg border border-white/10 p-2"
                    >
                      <Download size={16} />
                    </button>

                    <button
                      type="button"
                      aria-label={`Delete ${item.originalFilename}`}
                      disabled={busyId === item.id}
                      onClick={() => handleDelete(item)}
                      className="rounded-lg border border-red-400/20 p-2 text-red-400"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}

            {!loading && files.length === 0 && (
              <tr>
                <td colSpan={5} className="p-8 text-center text-gray-400">
                  No files uploaded yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {loading && (
          <p className="p-4 text-sm text-gray-400">Loading files…</p>
        )}
      </div>
    </section>
  );
}
import { useCallback, useEffect, useState } from "react";
import { Download, FileUp, Trash2 } from "lucide-react";
import fileService from "../services/fileService";

function saveBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");

  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();

  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export default function MyFilesPage() {
  const [files, setFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  

  const loadFiles = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      setFiles(await fileService.getMyFiles());
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to load your files."
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

  const formElement = event.currentTarget;

  setError("");
  setMessage("");

  if (!selectedFile) {
    setError("Choose a document first.");
    return;
  }

  const maxFileSize = 10 * 1024 * 1024;

  if (selectedFile.size > maxFileSize) {
    setError("File size must not exceed 10 MiB.");
    return;
  }

  setBusy(true);

  try {
    await fileService.uploadDocument(selectedFile);

    setSelectedFile(null);
    formElement.reset();

    setMessage("Document uploaded successfully.");

    await loadFiles();
  } catch (err) {
    setError(
      err.response?.data?.detail ||
        err.response?.data?.message ||
        "Document upload failed."
    );
  } finally {
    setBusy(false);
  }
}

  async function handleDownload(file) {
    setError("");
    setBusy(true);

    try {
      const blob = await fileService.downloadFile(file.id);
      saveBlob(blob, file.originalFilename);
    } catch (err) {
      setError(
        err.response?.data?.message || "Download failed."
      );
    } finally {
      setBusy(false);
    }
  }

  async function handleDelete(file) {
    if (!window.confirm(`Delete ${file.originalFilename}?`)) return;

    setError("");
    setMessage("");
    setBusy(true);

    try {
      await fileService.deleteMyFile(file.id);
      setFiles((current) =>
        current.filter((item) => item.id !== file.id)
      );
      setMessage("File deleted successfully.");
    } catch (err) {
      setError(
        err.response?.data?.message || "File deletion failed."
      );
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="min-h-[70vh] bg-[#09090f] p-5 text-white md:p-10">
      <div className="mx-auto max-w-5xl space-y-6">
        <header>
          <h1 className="text-2xl font-bold">My Files</h1>
          <p className="mt-2 text-sm text-gray-400">
            Upload and manage your personal documents.
          </p>
        </header>

        {error && <p role="alert" className="text-sm text-red-400">{error}</p>}
        {message && <p role="status" className="text-sm text-green-400">{message}</p>}

        <form
          onSubmit={handleUpload}
          className="space-y-4 rounded-2xl border border-white/10 p-5"
        >
          <label className="block space-y-2 text-sm text-gray-300">
            Select document
            <input
              type="file"
              required
              accept=".pdf,.doc,.docx,.xls,.xlsx,.csv,.txt,.rtf,.ppt,.pptx"
              onChange={(event) =>
                setSelectedFile(event.target.files?.[0] || null)
              }
              className="block w-full text-sm"
            />
          </label>

          <button
            type="submit"
            disabled={busy}
            className="flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold disabled:opacity-50"
          >
            <FileUp size={17} />
            {busy ? "Please wait…" : "Upload document"}
          </button>
        </form>

        <section className="overflow-x-auto rounded-2xl border border-white/10">
          <table className="w-full text-left text-sm">
            <thead className="bg-white/5 text-gray-400">
              <tr>
                <th className="p-4">Filename</th>
                <th className="p-4">Type</th>
                <th className="p-4">Actions</th>
              </tr>
            </thead>
            <tbody>
              {files.map((file) => (
                <tr key={file.id} className="border-t border-white/10">
                  <td className="break-words p-4">
                    {file.originalFilename}
                  </td>
                  <td className="p-4">{file.purpose}</td>
                  <td className="p-4">
                    <div className="flex gap-2">
                      <button
                        type="button"
                        disabled={busy}
                        onClick={() => handleDownload(file)}
                        aria-label={`Download ${file.originalFilename}`}
                        className="rounded-lg border border-white/10 p-2"
                      >
                        <Download size={16} />
                      </button>
                      <button
                        type="button"
                        disabled={busy}
                        onClick={() => handleDelete(file)}
                        aria-label={`Delete ${file.originalFilename}`}
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
                  <td colSpan={3} className="p-8 text-center text-gray-400">
                    You have no uploaded files.
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          {loading && (
            <p className="p-4 text-sm text-gray-400">Loading files…</p>
          )}
        </section>
      </div>
    </main>
  );
}
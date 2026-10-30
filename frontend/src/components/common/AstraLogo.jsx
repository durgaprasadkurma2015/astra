import { Link } from "react-router-dom";
import { Sparkles } from "lucide-react";

export default function AstraLogo() {
  return (
    <Link
      to="/"
      className="inline-flex items-center gap-2"
      aria-label="ASTRA home"
    >
      <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-violet-600 text-white shadow-lg shadow-violet-500/30">
        <Sparkles size={21} />
      </span>

      <span className="text-xl font-bold tracking-wide text-white">
        ASTRA
      </span>
    </Link>
  );
}
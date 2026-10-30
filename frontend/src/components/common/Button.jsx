export default function Button({
  children,
  type = "button",
  variant = "primary",
  disabled = false,
  onClick,
  className = "",
}) {
  const base =
    "inline-flex items-center justify-center gap-2 rounded-xl px-5 py-3 text-sm font-semibold transition duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-400 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50";

  const variants = {
    primary:
      "bg-violet-600 text-white shadow-lg shadow-violet-500/20 hover:bg-violet-500",

    secondary:
      "border border-white/20 bg-white/5 text-white hover:bg-white/10",

    light:
      "bg-white text-slate-900 hover:bg-violet-50",
  };

  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`${base} ${variants[variant]} ${className}`}
    >
      {children}
    </button>
  );
}
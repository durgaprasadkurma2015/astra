import { useId } from "react";

export default function InputField({
  label,
  type = "text",
  value,
  onChange,
  placeholder,
  error,
  autoComplete,
  required = true,
}) {
  const id = useId();

  return (
    <div className="w-full space-y-2">
      <label
        htmlFor={id}
        className="block text-sm font-medium text-slate-200"
      >
        {label}
      </label>

      <input
        id={id}
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        autoComplete={autoComplete}
        required={required}
        aria-invalid={Boolean(error)}
        className="w-full rounded-xl border border-slate-300/20 bg-white/5 px-4 py-3 text-sm text-white outline-none placeholder:text-slate-400 focus:border-violet-400 focus:ring-2 focus:ring-violet-500/20"
      />

      {error && (
        <p role="alert" className="text-sm text-red-400">
          {error}
        </p>
      )}
    </div>
  );
}
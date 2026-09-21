import { Star } from "lucide-react";

export default function Rating({ value = 4.5, count }) {
  return (
    <div className="rating">
      <span>{value.toFixed(1)}</span>
      <Star size={14} fill="currentColor" />
      {count !== undefined && <small>({count.toLocaleString()})</small>}
    </div>
  );
}

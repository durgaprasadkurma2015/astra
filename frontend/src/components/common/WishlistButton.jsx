
import { useState } from "react";
import { Heart } from "lucide-react";
import { useLocation, useNavigate } from "react-router-dom";

import { useAuth } from "../../context/useAuth.js";
import { useWishlist } from "../../context/useWishlist.js";

export default function WishlistButton({
  product,
  className = "",
}) {
  const { user } = useAuth();
  const { addItem, removeItem, isInWishlist } = useWishlist();

  const navigate = useNavigate();
  const location = useLocation();

  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");

  const productId = product?.id;
  const wished = productId != null && isInWishlist(productId);

  async function handleClick(event) {
    event.preventDefault();
    event.stopPropagation();

    if (productId == null || pending) return;

    setError("");

    if (!user) {
      navigate("/login", {
        state: {
          from: {
            pathname: location.pathname,
            search: location.search,
            hash: location.hash,
          },
        },
      });
      return;
    }

    setPending(true);

    try {
      if (wished) {
        await removeItem(productId);
      } else {
        // WishlistContext and wishlistService expect the product ID.
        await addItem(productId);
      }
    } catch (requestError) {
      setError(
        requestError.response?.data?.detail ||
          requestError.response?.data?.message ||
          requestError.message ||
          "Wishlist update failed. Please try again."
      );
    } finally {
      setPending(false);
    }
  }

  return (
    <div className={`inline-flex flex-col items-end ${className}`}>
      <button
        type="button"
        onClick={handleClick}
        disabled={pending || productId == null}
        aria-pressed={Boolean(wished)}
        aria-label={
          wished
            ? `Remove ${product?.name || "product"} from wishlist`
            : `Add ${product?.name || "product"} to wishlist`
        }
        title={
          error ||
          (wished ? "Remove from wishlist" : "Save to wishlist")
        }
        className={`flex h-10 w-10 items-center justify-center rounded-full border shadow-lg backdrop-blur transition-all duration-200 disabled:cursor-wait disabled:opacity-60 ${
          wished
            ? "border-pink-400/50 bg-pink-500/20 text-pink-300"
            : "border-white/20 bg-black/50 text-white hover:border-pink-400/50 hover:bg-pink-500/10 hover:text-pink-300"
        }`}
      >
        <Heart
          size={18}
          fill={wished ? "currentColor" : "none"}
          className={`transition-transform duration-200 ${
            pending ? "animate-pulse" : "hover:scale-110"
          }`}
        />
      </button>

      {error && (
        <span
          role="alert"
          className="mt-2 max-w-48 rounded-md border border-red-400/30 bg-red-950/95 px-2 py-1 text-left text-xs text-red-200 shadow-lg"
        >
          {error}
        </span>
      )}
    </div>
  );
}

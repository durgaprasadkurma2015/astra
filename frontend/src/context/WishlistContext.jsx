
import { useCallback, useEffect, useState } from "react";
import { WishlistContext } from "./WishlistContextObject.js";
import { useAuth } from "./useAuth.js";
import wishlistService from "../services/wishlistService.js";

export function WishlistProvider({ children }) {
  const { user } = useAuth();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState("");

  const refresh = useCallback(async () => {
    if (!user) {
      setItems([]);
      setLoadError("");
      setLoading(false);
      return;
    }

    setLoading(true);
    setLoadError("");

    try {
      const data = await wishlistService.getWishlist();

      const wishlistItems = Array.isArray(data)
        ? data
        : Array.isArray(data?.items)
          ? data.items
          : [];

      setItems(wishlistItems);
    } catch (error) {
      console.error("Failed to load wishlist:", error);

      setLoadError(
        error?.response?.data?.detail ||
          error?.response?.data?.message ||
          error?.message ||
          "Unable to load your wishlist. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      void refresh();
    }, 0);

    return () => clearTimeout(timeoutId);
  }, [refresh]);

 const addItem = async (productId) => {
  setLoadError("");
  await wishlistService.addItem(productId);
  await refresh();
};


 const removeItem = async (productId) => {
  setLoadError("");
  await wishlistService.removeItem(productId);
  await refresh();
};

  const isInWishlist = (productId) =>
    items.some(
      (item) =>
        String(item.productId ?? item.product?.id ?? item.id) ===
        String(productId)
    );

  const value = {
    items,
    loading,
    loadError,
    refresh,
    addItem,
    removeItem,
    isInWishlist,
  };

  return (
    <WishlistContext.Provider value={value}>
      {children}
    </WishlistContext.Provider>
  );
}

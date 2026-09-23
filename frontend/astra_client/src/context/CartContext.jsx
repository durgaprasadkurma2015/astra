import {
  useCallback,
  useEffect,
  useState
} from "react";

import { cartApi } from "../api/api";
import { useAuth } from "./AuthContext";
import { CartContext } from "./CartContextValue";

export function CartProvider({ children }) {
  const { user } = useAuth();

  const [cart, setCart] = useState(null);

  const refresh = useCallback(async () => {
    if (!user) {
      setCart(null);
      return;
    }

    try {
      const response = await cartApi.get();
      setCart(response.data);
    } catch (err) {
      console.error(
        "Failed to refresh cart:",
        err
      );

      setCart(null);
    }
  }, [user]);

  useEffect(() => {
    let cancelled = false;

    const loadCart = async () => {
      if (!user) {
        setCart(null);
        return;
      }

      try {
        const response = await cartApi.get();

        if (!cancelled) {
          setCart(response.data);
        }
      } catch (err) {
        if (!cancelled) {
          console.error(
            "Failed to load cart:",
            err
          );

          setCart(null);
        }
      }
    };

    loadCart();

    return () => {
      cancelled = true;
    };
  }, [user]);

  const addToCart = async (
    productId,
    quantity = 1
  ) => {
    const result = await cartApi.add({
      productId,
      quantity
    });

    setCart(result.data);

    return result.data;
  };

  const updateItem = async (
    itemId,
    quantity
  ) => {
    const result = await cartApi.update(
      itemId,
      { quantity }
    );

    setCart(result.data);
  };

  const removeItem = async (itemId) => {
    const result =
      await cartApi.remove(itemId);

    setCart(result.data);
  };

  const clearCart = async () => {
    await cartApi.clear();
    await refresh();
  };

  return (
    <CartContext.Provider
      value={{
        cart,
        refresh,
        addToCart,
        updateItem,
        removeItem,
        clearCart,
        count: cart?.totalItems || 0
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

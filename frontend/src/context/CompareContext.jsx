
/* eslint-disable react-refresh/only-export-components */

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

const CompareContext = createContext(null);

const STORAGE_KEY = "astra_compare_products";
const MAX_COMPARE_PRODUCTS = 4;

function readSavedProducts() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);

    if (!saved) return [];

    const parsed = JSON.parse(saved);

    if (!Array.isArray(parsed)) return [];

    const uniqueProducts = [];
    const seenIds = new Set();

    for (const product of parsed) {
      if (product?.id == null) continue;

      const id = String(product.id);

      if (!seenIds.has(id)) {
        seenIds.add(id);
        uniqueProducts.push(product);
      }

      if (uniqueProducts.length === MAX_COMPARE_PRODUCTS) {
        break;
      }
    }

    return uniqueProducts;
  } catch {
    return [];
  }
}

export function CompareProvider({ children }) {
  const [compareProducts, setCompareProducts] = useState(
    readSavedProducts
  );

  useEffect(() => {
    try {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify(compareProducts)
      );
    } catch (error) {
      console.error("Could not save comparison list:", error);
    }
  }, [compareProducts]);

  const addProduct = useCallback(
    (product) => {
      if (!product || product.id == null) {
        return {
          success: false,
          message: "This product has an invalid ID.",
        };
      }

      const alreadyAdded = compareProducts.some(
        (item) => String(item.id) === String(product.id)
      );

      if (alreadyAdded) {
        return {
          success: false,
          message: "This product is already in your comparison list.",
        };
      }

      if (compareProducts.length >= MAX_COMPARE_PRODUCTS) {
        return {
          success: false,
          message: `You can compare up to ${MAX_COMPARE_PRODUCTS} products.`,
        };
      }

      setCompareProducts((current) => [...current, product]);

      return { success: true, message: "" };
    },
    [compareProducts]
  );

  const removeProduct = useCallback((productId) => {
    setCompareProducts((current) =>
      current.filter(
        (product) => String(product.id) !== String(productId)
      )
    );
  }, []);

  const clearComparison = useCallback(() => {
    setCompareProducts([]);
  }, []);

  const isCompared = useCallback(
    (productId) =>
      compareProducts.some(
        (product) => String(product.id) === String(productId)
      ),
    [compareProducts]
  );

  const value = useMemo(
    () => ({
      compareProducts,
      compareCount: compareProducts.length,
      maxCompareProducts: MAX_COMPARE_PRODUCTS,
      addProduct,
      removeProduct,
      clearComparison,
      isCompared,
    }),
    [
      compareProducts,
      addProduct,
      removeProduct,
      clearComparison,
      isCompared,
    ]
  );

  return (
    <CompareContext.Provider value={value}>
      {children}
    </CompareContext.Provider>
  );
}

export function useCompare() {
  const context = useContext(CompareContext);

  if (!context) {
    throw new Error(
      "useCompare must be used within CompareProvider"
    );
  }

  return context;
}

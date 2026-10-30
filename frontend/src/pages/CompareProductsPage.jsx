import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  GitCompareArrows,
  ShoppingBag,
  Trash2,
  X,
} from "lucide-react";
import { useCompare } from "../context/CompareContext.jsx";

function getProductImage(product) {
  return (
    product?.imageUrl ||
    product?.image ||
    product?.thumbnail ||
    product?.images?.[0]?.url ||
    product?.images?.[0] ||
    ""
  );
}

function formatPrice(price) {
  if (price == null || price === "") return "Not available";

  const numericPrice = Number(price);

  if (!Number.isFinite(numericPrice)) return String(price);

  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(numericPrice);
}

function getValue(value) {
  if (value == null || value === "") return "Not available";

  if (typeof value === "boolean") {
    return value ? "Yes" : "No";
  }

  return String(value);
}

function getStockStatus(product) {
  if (typeof product?.stockQuantity === "number") {
    return product.stockQuantity > 0
      ? `In stock (${product.stockQuantity})`
      : "Out of stock";
  }

  if (typeof product?.quantity === "number") {
    return product.quantity > 0
      ? `In stock (${product.quantity})`
      : "Out of stock";
  }

  if (typeof product?.stock === "boolean") {
    return product.stock ? "In stock" : "Out of stock";
  }

  if (typeof product?.inStock === "boolean") {
    return product.inStock ? "In stock" : "Out of stock";
  }

  return "Not available";
}

function getRating(product) {
  const rating = product?.averageRating ?? product?.rating;

  if (rating == null || rating === "") {
    return "Not available";
  }

  const numericRating = Number(rating);

  return Number.isFinite(numericRating)
    ? `${numericRating.toFixed(1)} / 5`
    : String(rating);
}

function getDescription(product) {
  return (
    product?.description ||
    product?.shortDescription ||
    "No description available."
  );
}

export default function CompareProductsPage() {
  const navigate = useNavigate();

  const {
    compareProducts,
    compareCount,
    maxCompareProducts,
    removeProduct,
    clearComparison,
  } = useCompare();

  const comparisonRows = [
    {
      label: "Product",
      render: (product) => (
        <div className="flex min-w-40 flex-col items-center gap-3">
          {getProductImage(product) ? (
            <img
              src={getProductImage(product)}
              alt={product?.name || product?.title || "Product"}
              className="h-36 w-36 rounded-xl border border-gray-200 bg-white object-contain p-2"
              onError={(event) => {
                event.currentTarget.style.display = "none";
              }}
            />
          ) : (
            <div className="flex h-36 w-36 items-center justify-center rounded-xl bg-gray-100 text-gray-400">
              <ShoppingBag size={36} />
            </div>
          )}

          <p className="max-w-48 text-center font-semibold text-gray-900">
            {product?.name || product?.title || "Unnamed product"}
          </p>

          <button
            type="button"
            onClick={() => navigate(`/products/${product.id}`)}
            className="inline-flex items-center gap-1 text-sm font-medium text-blue-600 hover:text-blue-800"
          >
            View details <ArrowRight size={15} />
          </button>
        </div>
      ),
    },
    {
      label: "Price",
      render: (product) => (
        <span className="font-semibold text-gray-900">
          {formatPrice(product?.price ?? product?.sellingPrice)}
        </span>
      ),
    },
    {
      label: "Brand",
      render: (product) =>
        getValue(product?.brand?.name ?? product?.brand),
    },
    {
      label: "Category",
      render: (product) =>
        getValue(product?.category?.name ?? product?.category),
    },
    {
      label: "Rating",
      render: (product) => getRating(product),
    },
    {
      label: "Availability",
      render: (product) => getStockStatus(product),
    },
    {
      label: "Description",
      render: (product) => (
        <p className="max-w-64 whitespace-normal text-sm leading-6 text-gray-600">
          {getDescription(product)}
        </p>
      ),
    },
  ];

  return (
    <main className="min-h-screen bg-gray-50 px-4 py-8 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <button
          type="button"
          onClick={() => navigate("/products")}
          className="mb-6 inline-flex items-center gap-2 text-sm font-medium text-gray-600 transition hover:text-gray-900"
        >
          <ArrowLeft size={18} />
          Back to products
        </button>

        <div className="mb-8 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <div className="mb-2 flex items-center gap-2">
              <GitCompareArrows size={25} className="text-blue-600" />
              <h1 className="text-2xl font-bold text-gray-900 sm:text-3xl">
                Compare Products
              </h1>
            </div>

            <p className="text-sm text-gray-500">
              Compare product details side by side before making a decision.
            </p>

            <p className="mt-2 text-sm font-medium text-gray-700">
              {compareCount} of {maxCompareProducts} products selected
            </p>
          </div>

          {compareCount > 0 && (
            <button
              type="button"
              onClick={clearComparison}
              className="inline-flex items-center justify-center gap-2 self-start rounded-lg border border-red-200 px-4 py-2.5 text-sm font-semibold text-red-600 transition hover:bg-red-50 sm:self-auto"
            >
              <Trash2 size={17} />
              Clear all
            </button>
          )}
        </div>

        {compareProducts.length === 0 ? (
          <section className="rounded-2xl border border-dashed border-gray-300 bg-white px-5 py-16 text-center">
            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-blue-50 text-blue-600">
              <GitCompareArrows size={30} />
            </div>

            <h2 className="text-xl font-semibold text-gray-900">
              No products selected
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-gray-500">
              Add at least two products from the products page to compare
              their details here.
            </p>

            <button
              type="button"
              onClick={() => navigate("/products")}
              className="mt-6 inline-flex items-center gap-2 rounded-lg bg-blue-600 px-5 py-3 font-semibold text-white transition hover:bg-blue-700"
            >
              Browse products
              <ArrowRight size={18} />
            </button>
          </section>
        ) : compareProducts.length === 1 ? (
          <div className="mb-6 rounded-xl border border-blue-100 bg-blue-50 p-4 text-sm text-blue-800">
            Select at least one more product for a side-by-side comparison.
          </div>
        ) : null}

        {compareProducts.length > 0 && (
          <section className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
            <div className="overflow-x-auto">
              <table className="w-full min-w-max border-collapse text-left">
                <thead>
                  <tr>
                    <th className="sticky left-0 z-10 min-w-36 border-b border-r border-gray-200 bg-gray-50 p-4 text-sm font-semibold text-gray-700">
                      Features
                    </th>

                    {compareProducts.map((product) => (
                      <th
                        key={product.id}
                        className="min-w-60 border-b border-gray-200 p-4 align-top"
                      >
                        <div className="flex justify-end">
                          <button
                            type="button"
                            onClick={() => removeProduct(product.id)}
                            aria-label={`Remove ${product.name || product.title || "product"} from comparison`}
                            title="Remove product"
                            className="rounded-lg p-2 text-gray-400 transition hover:bg-red-50 hover:text-red-600"
                          >
                            <X size={18} />
                          </button>
                        </div>

                        <div className="pb-2">
                          <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
                            Product {compareProducts.indexOf(product) + 1}
                          </p>
                        </div>
                      </th>
                    ))}
                  </tr>
                </thead>

                <tbody>
                  {comparisonRows.map((row, rowIndex) => (
                    <tr
                      key={row.label}
                      className={
                        rowIndex % 2 === 0 ? "bg-white" : "bg-gray-50/70"
                      }
                    >
                      <th className="sticky left-0 z-10 min-w-36 border-r border-gray-200 bg-inherit p-4 align-top text-sm font-semibold text-gray-700">
                        {row.label}
                      </th>

                      {compareProducts.map((product) => (
                        <td
                          key={product.id}
                          className="min-w-60 border-b border-gray-100 p-4 align-top text-sm text-gray-700"
                        >
                          {row.render(product)}
                        </td>
                      ))}
                    </tr>
                  ))}

                  <tr>
                    <th className="sticky left-0 z-10 border-r border-gray-200 bg-gray-50 p-4 text-sm font-semibold text-gray-700">
                      Action
                    </th>

                    {compareProducts.map((product) => (
                      <td
                        key={product.id}
                        className="min-w-60 border-t border-gray-100 p-4"
                      >
                        <button
                          type="button"
                          onClick={() => navigate(`/products/${product.id}`)}
                          className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700"
                        >
                          View product
                        </button>
                      </td>
                    ))}
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        )}

        <div className="mt-6 flex flex-wrap items-center justify-between gap-3">
          <p className="text-xs text-gray-500">
            You can compare up to {maxCompareProducts} products at a time.
          </p>

          <button
            type="button"
            onClick={() => navigate("/products")}
            className="inline-flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm font-semibold text-gray-700 transition hover:bg-gray-50"
          >
            <ArrowLeft size={16} />
            Continue shopping
          </button>
        </div>
      </div>
    </main>
  );
}
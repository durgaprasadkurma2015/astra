
import { useEffect, useState } from "react";
import {
  Warehouse,
  RefreshCw,
  ArrowLeftRight,
  TrendingDown,
  AlertTriangle,
  Package,
} from "lucide-react";

import inventoryService from "../services/inventoryService";

const inputClass =
  "w-full rounded-xl border border-white/10 bg-[#09090f] px-4 py-3 text-sm text-white";

export default function AdminWarehousePage() {
  const [warehouses, setWarehouses] = useState([]);
  const [stock, setStock] = useState([]);
  const [forecast, setForecast] = useState([]);

  const [warehouseId, setWarehouseId] = useState("");
  const [leadTime, setLeadTime] = useState(14);

  const [transfer, setTransfer] = useState({
    productId: "",
    sourceWarehouseId: "",
    destinationWarehouseId: "",
    quantity: 1,
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  // Load warehouses
  useEffect(() => {
    let cancelled = false;

    async function fetchWarehouses() {
      try {
        setLoading(true);
        setError("");

        const data = await inventoryService.getWarehouses();

        if (cancelled) return;

        setWarehouses(data);
        setWarehouseId(current =>
          current || (data.length ? String(data[0].id) : "")
        );
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.data?.message ||
              "Unable to load warehouses."
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    fetchWarehouses();

    return () => {
      cancelled = true;
    };
  }, []);

  // Load warehouse stock
// Load warehouse stock
useEffect(() => {
  if (!warehouseId) return;

  let cancelled = false;

  async function fetchStock() {
    try {
      const data =
        await inventoryService.getWarehouseStock(warehouseId);

      if (!cancelled) {
        setStock(data);
      }
    } catch (err) {
      if (!cancelled) {
        setError(
          err.response?.data?.message ||
            "Unable to load warehouse stock."
        );
      }
    }
  }

  fetchStock();

  return () => {
    cancelled = true;
  };
}, [warehouseId]);

  // Load demand forecast
  useEffect(() => {
    let cancelled = false;

    async function fetchForecast() {
      try {
        const data = await inventoryService.getForecast(leadTime);

        if (!cancelled) setForecast(data);
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.data?.message ||
              "Unable to load inventory forecast."
          );
        }
      }
    }

    fetchForecast();

    return () => {
      cancelled = true;
    };
  }, [leadTime]);

  // Refresh all data
  async function loadWarehouses() {
    setLoading(true);
    setError("");

    try {
      const data = await inventoryService.getWarehouses();
      setWarehouses(data);

      setWarehouseId(current =>
        current || (data.length ? String(data[0].id) : "")
      );

      if (warehouseId) {
        const stockData =
          await inventoryService.getWarehouseStock(warehouseId);
        setStock(stockData);
      }

      const forecastData =
        await inventoryService.getForecast(leadTime);
      setForecast(forecastData);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to refresh inventory data."
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleTransfer(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (
      !transfer.productId ||
      !transfer.sourceWarehouseId ||
      !transfer.destinationWarehouseId ||
      transfer.sourceWarehouseId ===
        transfer.destinationWarehouseId ||
      !Number.isSafeInteger(Number(transfer.quantity)) ||
      Number(transfer.quantity) < 1
    ) {
      setError("Enter valid product, warehouses and quantity.");
      return;
    }

    try {
      await inventoryService.transferStock({
        ...transfer,
        productId: Number(transfer.productId),
        sourceWarehouseId: Number(transfer.sourceWarehouseId),
        destinationWarehouseId: Number(transfer.destinationWarehouseId),
        quantity: Number(transfer.quantity),
      });

      setMessage("Stock transfer completed successfully.");

      await loadWarehouses();

      setTransfer({
        productId: "",
        sourceWarehouseId: "",
        destinationWarehouseId: "",
        quantity: 1,
      });
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to complete stock transfer."
      );
    }
  }

  const totals = warehouses.reduce(
    (result, item) => ({
      onHand: result.onHand + Number(item.onHandUnits || 0),
      reserved: result.reserved + Number(item.reservedUnits || 0),
      available: result.available + Number(item.availableUnits || 0),
    }),
    { onHand: 0, reserved: 0, available: 0 }
  );

  const lowStock = forecast.filter(
    item =>
      item.estimatedDaysUntilStockout != null &&
      Number(item.estimatedDaysUntilStockout) <= leadTime
  );

  return (
    <div className="min-h-screen bg-[#09090f] px-4 py-7 text-white sm:px-8">
      <header className="flex flex-wrap items-center justify-between gap-4 border-b border-white/10 pb-6">
        <div>
          <p className="text-xs font-semibold tracking-widest text-violet-400">
            ASTRA INVENTORY CONTROL
          </p>
          <h1 className="mt-2 text-3xl font-bold">
            Warehouse Management
          </h1>
          <p className="mt-2 text-sm text-gray-400">
            Manage stock distribution, reservations and demand forecasts.
          </p>
        </div>

        <button
          onClick={loadWarehouses}
          disabled={loading}
          className="flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold"
        >
          <RefreshCw size={16} />
          Refresh
        </button>
      </header>

      {error && (
        <div className="mt-5 rounded-xl border border-red-400/20 bg-red-500/10 p-4 text-sm text-red-300">
          {error}
        </div>
      )}

      {message && (
        <div className="mt-5 rounded-xl border border-emerald-400/20 bg-emerald-500/10 p-4 text-sm text-emerald-300">
          {message}
        </div>
      )}

      <section className="mt-7 grid grid-cols-1 gap-4 sm:grid-cols-3">
        {[
          ["Physical Stock", totals.onHand, Package],
          ["Reserved Stock", totals.reserved, Warehouse],
          ["Available Stock", totals.available, TrendingDown],
        ].map(([title, value, Icon]) => (
          <div
            key={title}
            className="rounded-2xl border border-white/10 bg-[#11111c] p-5"
          >
            <div className="flex justify-between">
              <p className="text-sm text-gray-400">{title}</p>
              <Icon size={20} className="text-violet-300" />
            </div>
            <p className="mt-5 text-3xl font-bold">
              {Number(value).toLocaleString("en-IN")}
            </p>
          </div>
        ))}
      </section>

      <section className="mt-7 rounded-2xl border border-white/10 bg-[#11111c] p-5">
        <h2 className="font-semibold">Warehouse Stock</h2>

        <select
          value={warehouseId}
          onChange={event => setWarehouseId(event.target.value)}
          className={`${inputClass} mt-4`}
        >
          <option value="">Select warehouse</option>
          {warehouses.map(item => (
            <option key={item.id} value={item.id}>
              {item.warehouseName} ({item.warehouseCode})
            </option>
          ))}
        </select>

        <div className="mt-5 overflow-x-auto">
          <table className="w-full min-w-[650px] text-left text-sm">
            <thead className="border-b border-white/10 text-gray-500">
              <tr>
                <th className="p-3">Product</th>
                <th className="p-3">SKU</th>
                <th className="p-3">On Hand</th>
                <th className="p-3">Reserved</th>
                <th className="p-3">Available</th>
                <th className="p-3">Reorder Level</th>
              </tr>
            </thead>

            <tbody>
              {stock.map(item => (
                <tr
                  key={item.productId}
                  className="border-b border-white/5"
                >
                  <td className="p-3">{item.productName}</td>
                  <td className="p-3 text-gray-400">{item.sku}</td>
                  <td className="p-3">{item.onHandQuantity}</td>
                  <td className="p-3 text-amber-300">
                    {item.reservedQuantity}
                  </td>
                  <td className="p-3 font-semibold">
                    {item.availableQuantity}
                  </td>
                  <td className="p-3">{item.reorderLevel}</td>
                </tr>
              ))}

              {stock.length === 0 && (
                <tr>
                  <td
                    colSpan="6"
                    className="p-8 text-center text-gray-500"
                  >
                    No warehouse stock found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="mt-7 grid grid-cols-1 gap-5 xl:grid-cols-2">
        <div className="rounded-2xl border border-white/10 bg-[#11111c] p-5">
          <div className="flex items-center gap-2">
            <ArrowLeftRight className="text-violet-400" />
            <h2 className="font-semibold">Stock Transfer</h2>
          </div>

          <form onSubmit={handleTransfer} className="mt-5 space-y-4">
            <input
              type="number"
              min="1"
              placeholder="Product ID"
              value={transfer.productId}
              onChange={e =>
                setTransfer({ ...transfer, productId: e.target.value })
              }
              className={inputClass}
              required
            />

            <select
              value={transfer.sourceWarehouseId}
              onChange={e =>
                setTransfer({
                  ...transfer,
                  sourceWarehouseId: e.target.value,
                })
              }
              className={inputClass}
              required
            >
              <option value="">Source warehouse</option>
              {warehouses.map(item => (
                <option key={item.id} value={item.id}>
                  {item.warehouseName}
                </option>
              ))}
            </select>

            <select
              value={transfer.destinationWarehouseId}
              onChange={e =>
                setTransfer({
                  ...transfer,
                  destinationWarehouseId: e.target.value,
                })
              }
              className={inputClass}
              required
            >
              <option value="">Destination warehouse</option>
              {warehouses.map(item => (
                <option key={item.id} value={item.id}>
                  {item.warehouseName}
                </option>
              ))}
            </select>

            <input
              type="number"
              min="1"
              placeholder="Quantity"
              value={transfer.quantity}
              onChange={e =>
                setTransfer({ ...transfer, quantity: e.target.value })
              }
              className={inputClass}
              required
            />

            <button className="w-full rounded-xl bg-violet-600 p-3 text-sm font-semibold hover:bg-violet-500">
              Transfer Stock
            </button>
          </form>
        </div>

        <div className="rounded-2xl border border-white/10 bg-[#11111c] p-5">
          <div className="flex items-center gap-2">
            <TrendingDown className="text-amber-300" />
            <h2 className="font-semibold">Demand Forecast</h2>
          </div>

          <label className="mt-5 block text-sm text-gray-400">
            Supplier lead time (days)
            <input
              type="number"
              min="1"
              max="180"
              value={leadTime}
              onChange={e =>
                setLeadTime(Math.max(1, Math.min(180, Number(e.target.value))))
              }
              className={`${inputClass} mt-2`}
            />
          </label>

          <div className="mt-5 flex items-center gap-2 text-sm text-amber-300">
            <AlertTriangle size={17} />
            {lowStock.length} products projected to run low within {leadTime} days
          </div>

          <div className="mt-4 max-h-64 space-y-3 overflow-y-auto">
            {forecast.slice(0, 10).map(item => (
              <div
                key={item.productId}
                className="flex items-center justify-between gap-3 border-b border-white/5 pb-3"
              >
                <div className="min-w-0">
                  <p className="truncate text-sm">
                    {item.productName}
                  </p>
                  <p className="text-xs text-gray-500">
                    {item.averageDailyDemand} units/day
                  </p>
                </div>

                <div className="shrink-0 text-right">
                  <p className="text-sm font-semibold">
                    {item.suggestedReorderQuantity} units
                  </p>
                  <p className="text-xs text-gray-500">
                    Suggested reorder
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

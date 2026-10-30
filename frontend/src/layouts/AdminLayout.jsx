
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  ShoppingBag,
  Package,
  Warehouse,
  TicketPercent,
  UserPlus,
  Users,
  MessageSquareText,
  Menu,
  X,
} from "lucide-react";
import { useState } from "react";

const navItems = [
  { label: "Dashboard", path: "/admin", icon: LayoutDashboard, end: true },
  { label: "Orders", path: "/admin/orders", icon: ShoppingBag },
  { label: "Reviews", path: "/admin/reviews", icon: MessageSquareText },
  { label: "Customers", path: "/admin/customers", icon: Users },
  { label: "Products", path: "/admin/products", icon: Package },
  { label: "Inventory", path: "/admin/inventory", icon: Warehouse },
  { label: "Coupons", path: "/admin/coupons", icon: TicketPercent },
  { label: "Register Admin", path: "/admin/register", icon: UserPlus },
];

export default function AdminLayout() {
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const linkClass = ({ isActive }) =>
    `flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm transition ${
      isActive
        ? "bg-violet-600 font-semibold text-white"
        : "text-gray-400 hover:bg-white/5 hover:text-white"
    }`;

  function renderNavigation() {
    return navItems.map(({ label, path, icon: Icon, end }) => (
      <NavLink
        key={path}
        to={path}
        end={end}
        onClick={() => setMobileOpen(false)}
        className={linkClass}
      >
        <Icon size={18} />
        <span>{label}</span>
      </NavLink>
    ));
  }

  return (
    <div className="min-h-screen bg-[#09090f] text-white">
      <div className="flex min-h-screen">
        {/* Desktop sidebar */}
        <aside className="hidden w-64 shrink-0 border-r border-white/10 bg-[#0d0d15] p-6 lg:flex lg:flex-col">
          <button
            type="button"
            onClick={() => navigate("/admin")}
            className="text-left"
          >
            <span className="text-2xl font-black tracking-[0.2em] text-violet-400">
              ASTRA
            </span>
            <p className="mt-1 text-xs tracking-wider text-gray-500">
              ADMINISTRATION
            </p>
          </button>

          <p className="mb-3 mt-10 px-3 text-xs font-semibold uppercase tracking-widest text-gray-600">
            Workspace
          </p>

          <nav className="space-y-2">{renderNavigation()}</nav>

          <div className="mt-auto rounded-2xl border border-violet-400/20 bg-violet-500/[0.06] p-4">
            <p className="text-sm font-semibold">Store operations</p>
            <p className="mt-2 text-xs leading-5 text-gray-400">
              Manage products, orders, inventory and store settings.
            </p>
          </div>
        </aside>

        {/* Main page area */}
        <div className="min-w-0 flex-1">
          {/* Mobile top bar */}
          <header className="flex items-center justify-between border-b border-white/10 bg-[#0d0d15] px-4 py-4 lg:hidden">
            <button
              type="button"
              onClick={() => navigate("/admin")}
              className="text-lg font-black tracking-[0.2em] text-violet-400"
            >
              ASTRA
            </button>

            <button
              type="button"
              onClick={() => setMobileOpen((open) => !open)}
              className="rounded-lg border border-white/10 p-2 text-gray-300"
              aria-label={mobileOpen ? "Close navigation" : "Open navigation"}
              aria-expanded={mobileOpen}
            >
              {mobileOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </header>

          {mobileOpen && (
            <nav className="space-y-2 border-b border-white/10 bg-[#0d0d15] p-4 lg:hidden">
              {renderNavigation()}
            </nav>
          )}

          <main className="min-w-0">{<Outlet />}</main>
        </div>
      </div>
    </div>
  );
}

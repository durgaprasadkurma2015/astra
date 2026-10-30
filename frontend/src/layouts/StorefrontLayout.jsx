
import { useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  Menu,
  X,
  ShoppingCart,
  Heart,
  UserRound,
  LogOut,
  Search,
} from "lucide-react";

import AstraLogo from "../components/common/AstraLogo";
import { useAuth } from "../context/useAuth";
import { useWishlist } from "../context/useWishlist";

const navLinkClass = ({ isActive }) =>
  `rounded-xl px-3 py-2 text-sm font-medium transition duration-200 ${
    isActive
      ? "bg-violet-500/15 text-violet-300 ring-1 ring-violet-400/20"
      : "text-slate-300 hover:bg-white/[0.06] hover:text-white"
  }`;

export default function StorefrontLayout() {
  const [menuOpen, setMenuOpen] = useState(false);
  const { user, loading, logout } = useAuth();
  const navigate = useNavigate();

  const wishlist = useWishlist();
  const wishlistCount =
    wishlist?.items?.length ??
    wishlist?.wishlistItems?.length ??
    0;

  const closeMenu = () => setMenuOpen(false);

  const handleLogout = () => {
    logout();
    closeMenu();
    navigate("/login", { replace: true });
  };

  const navItems = [
    { label: "Home", to: "/" },
    { label: "Products", to: "/products" },
    { label: "About", to: "/about" },
    ...(user ? [{ label: "Addresses", to: "/addresses" }] : []),
  ];

  return (
    <div className="min-h-screen bg-[#050714] text-white">
      <header className="sticky top-0 z-40 border-b border-white/[0.08] bg-[#080b1b]/95 shadow-lg shadow-black/10 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl items-center gap-4 px-4 py-4 sm:px-6 lg:px-8">
          <div className="shrink-0 [&_span:last-child]:text-white">
            <AstraLogo />
          </div>

          <nav className="hidden items-center gap-1 md:flex">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === "/"}
                className={navLinkClass}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="ml-auto flex items-center gap-1 sm:gap-2">
            <Link
              to="/products"
              aria-label="Search products"
              className="hidden rounded-xl p-2.5 text-slate-300 transition hover:bg-white/[0.07] hover:text-violet-300 sm:inline-flex"
            >
              <Search size={20} />
            </Link>

            {user && (
              <>
                <Link
                  to="/wishlist"
                  aria-label="Wishlist"
                  className="relative rounded-xl p-2.5 text-slate-300 transition hover:bg-violet-500/10 hover:text-violet-300"
                >
                  <Heart size={21} />

                  {wishlistCount > 0 && (
                    <span className="absolute right-0 top-0 flex h-4 min-w-4 items-center justify-center rounded-full bg-violet-500 px-1 text-[10px] font-bold text-white ring-2 ring-[#080b1b]">
                      {wishlistCount}
                    </span>
                  )}
                </Link>

                <Link
                  to="/cart"
                  aria-label="Shopping cart"
                  className="rounded-xl p-2.5 text-slate-300 transition hover:bg-violet-500/10 hover:text-violet-300"
                >
                  <ShoppingCart size={21} />
                </Link>
              </>
            )}

            {!loading && !user && (
              <div className="hidden items-center gap-2 sm:flex">
                <Link
                  to="/login"
                  className="rounded-xl px-4 py-2 text-sm font-semibold text-slate-300 transition hover:bg-white/[0.06] hover:text-white"
                >
                  Login
                </Link>

                <Link
                  to="/register"
                  className="rounded-xl bg-violet-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-violet-900/20 transition hover:bg-violet-500"
                >
                  Sign up
                </Link>
              </div>
            )}

            {!loading && user && (
              <div className="hidden items-center gap-1 lg:flex">
                <Link
                  to="/profile"
                  className="inline-flex items-center gap-2 rounded-xl px-3 py-2 text-sm font-medium text-slate-300 transition hover:bg-white/[0.06] hover:text-white"
                >
                  <UserRound size={18} />
                  Profile
                </Link>

                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-xl p-2.5 text-slate-400 transition hover:bg-red-500/10 hover:text-red-300"
                  aria-label="Logout"
                  title="Logout"
                >
                  <LogOut size={19} />
                </button>
              </div>
            )}

            <button
              type="button"
              onClick={() => setMenuOpen((open) => !open)}
              className="rounded-xl p-2.5 text-slate-300 transition hover:bg-white/[0.07] hover:text-white md:hidden"
              aria-label={menuOpen ? "Close menu" : "Open menu"}
              aria-expanded={menuOpen}
            >
              {menuOpen ? <X size={22} /> : <Menu size={22} />}
            </button>
          </div>
        </div>

        {menuOpen && (
          <div className="border-t border-white/[0.08] bg-[#080b1b] px-4 py-4 md:hidden">
            <nav className="mx-auto flex max-w-7xl flex-col gap-1">
              {navItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === "/"}
                  onClick={closeMenu}
                  className={navLinkClass}
                >
                  {item.label}
                </NavLink>
              ))}

              {user ? (
                <>
                  <NavLink
                    to="/wishlist"
                    onClick={closeMenu}
                    className={navLinkClass}
                  >
                    Wishlist
                  </NavLink>

                  <NavLink
                    to="/cart"
                    onClick={closeMenu}
                    className={navLinkClass}
                  >
                    Cart
                  </NavLink>

                  <NavLink
                    to="/profile"
                    onClick={closeMenu}
                    className={navLinkClass}
                  >
                    My Profile
                  </NavLink>

                  <NavLink
                    to="/my-orders"
                    onClick={closeMenu}
                    className={navLinkClass}
                  >
                    My Orders
                  </NavLink>

                  {user.role === "ADMIN" && (
                    <NavLink
                      to="/admin"
                      onClick={closeMenu}
                      className={navLinkClass}
                    >
                      Admin Dashboard
                    </NavLink>
                  )}

                  <button
                    type="button"
                    onClick={handleLogout}
                    className="mt-2 flex items-center gap-2 rounded-xl px-3 py-2 text-left text-sm font-semibold text-red-300 transition hover:bg-red-500/10"
                  >
                    <LogOut size={17} />
                    Logout
                  </button>
                </>
              ) : (
                <div className="mt-3 grid grid-cols-2 gap-3">
                  <Link
                    to="/login"
                    onClick={closeMenu}
                    className="rounded-xl border border-white/10 px-4 py-3 text-center text-sm font-semibold text-slate-200 transition hover:bg-white/[0.06]"
                  >
                    Login
                  </Link>

                  <Link
                    to="/register"
                    onClick={closeMenu}
                    className="rounded-xl bg-violet-600 px-4 py-3 text-center text-sm font-semibold text-white transition hover:bg-violet-500"
                  >
                    Sign up
                  </Link>
                </div>
              )}
            </nav>
          </div>
        )}
      </header>

      <main className="min-h-[65vh]">
        <Outlet />
      </main>

      <footer className="mt-12 border-t border-white/[0.08] bg-[#080b1b] text-slate-300">
        <div className="mx-auto grid max-w-7xl gap-8 px-4 py-10 sm:px-6 md:grid-cols-3 lg:px-8">
          <div>
            <div className="inline-flex rounded-lg bg-white px-3 py-2 [&_span:last-child]:text-slate-950">
              <AstraLogo />
            </div>

            <p className="mt-4 max-w-sm text-sm leading-6 text-slate-400">
              Discover quality products with a smooth and reliable shopping
              experience.
            </p>
          </div>

          <div>
            <h2 className="font-semibold text-white">Explore</h2>

            <div className="mt-3 flex flex-col items-start gap-3 text-sm">
              <Link
                to="/products"
                className="text-slate-400 transition hover:text-violet-300"
              >
                All Products
              </Link>

              <Link
                to="/about"
                className="text-slate-400 transition hover:text-violet-300"
              >
                About ASTRA
              </Link>
            </div>
          </div>

          <div>
            <h2 className="font-semibold text-white">Your Account</h2>

            <div className="mt-3 flex flex-col items-start gap-3 text-sm">
              {user ? (
                <>
                  <Link
                    to="/profile"
                    className="text-slate-400 transition hover:text-violet-300"
                  >
                    My Profile
                  </Link>

                  <Link
                    to="/my-orders"
                    className="text-slate-400 transition hover:text-violet-300"
                  >
                    My Orders
                  </Link>

                  <Link
                    to="/wishlist"
                    className="text-slate-400 transition hover:text-violet-300"
                  >
                    Wishlist
                  </Link>
                </>
              ) : (
                <>
                  <Link
                    to="/login"
                    className="text-slate-400 transition hover:text-violet-300"
                  >
                    Login
                  </Link>

                  <Link
                    to="/register"
                    className="text-slate-400 transition hover:text-violet-300"
                  >
                    Create Account
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>

        <div className="border-t border-white/[0.08] px-4 py-4 text-center text-xs text-slate-500">
          © {new Date().getFullYear()} ASTRA E-Commerce. All rights reserved.
        </div>
      </footer>
    </div>
  );
}


import { Link } from "react-router-dom";
import {
  ShoppingBag,
  Truck,
  ShieldCheck,
  Package,
  ShoppingCart,
  Sparkles,
} from "lucide-react";

import AstraLogo from "../components/common/AstraLogo";

export default function AuthLayout({ children }) {
  return (
    <main className="relative min-h-screen overflow-hidden bg-[#05091c] text-white">
      {/* Background glow */}
      <div className="pointer-events-none fixed -left-40 top-20 h-96 w-96 rounded-full bg-violet-700/25 blur-[120px]" />
      <div className="pointer-events-none fixed -right-32 bottom-0 h-96 w-96 rounded-full bg-indigo-600/25 blur-[120px]" />
      <div className="pointer-events-none fixed left-1/2 top-1/3 h-80 w-80 -translate-x-1/2 rounded-full bg-purple-600/10 blur-[100px]" />

      <div className="relative mx-auto min-h-screen w-full max-w-[1600px] px-5 py-5 sm:px-8 lg:px-12 lg:py-7">
        {/* Header */}
        <header className="flex items-center justify-between gap-4">
          <Link to="/" aria-label="ASTRA home" className="shrink-0">
            <AstraLogo />
          </Link>

    
        </header>

        {/* Main split layout */}
        <div className="grid min-h-[calc(100vh-120px)] items-center gap-10 py-8 lg:grid-cols-[0.95fr_1.05fr] lg:gap-12 lg:py-5">
          {/* Left promotional section */}
          <section className="relative hidden min-h-[620px] items-center lg:flex">
            <div className="relative z-10 w-[55%] max-w-[400px]">
              <span className="inline-flex items-center gap-2 rounded-full border border-violet-400/20 bg-violet-500/10 px-4 py-2 text-sm text-violet-200">
                <Sparkles size={15} />
                Discover
              </span>

              <h1 className="mt-6 text-5xl font-extrabold leading-[1.12] tracking-tight xl:text-6xl">
                Shop
                <br />
                Smarter,
                <br />
                <span className="bg-gradient-to-r from-violet-400 via-purple-400 to-fuchsia-400 bg-clip-text text-transparent">
                  Live Better
                </span>
              </h1>

              <p className="mt-6 max-w-sm text-base leading-7 text-slate-300 xl:text-lg">
                Top quality products, great prices and a seamless shopping
                experience.
              </p>

              <div className="mt-8 space-y-5">
                <Feature
                  icon={<Truck size={23} />}
                  title="Wide Range"
                  subtitle="of Products"
                />
                <Feature
                  icon={<ShieldCheck size={23} />}
                  title="Secure"
                  subtitle="Payments"
                />
                <Feature
                  icon={<Package size={23} />}
                  title="Fast"
                  subtitle="Delivery"
                />
              </div>
            </div>

            {/* Decorative shopping illustration */}
            <div
              aria-hidden="true"
              className="pointer-events-none absolute inset-y-0 right-[-4%] flex w-[58%] items-center justify-center"
            >
              <div className="absolute h-80 w-80 rounded-full bg-violet-600/25 blur-[80px]" />

              <div className="relative flex h-[410px] w-full items-center justify-center">
                <div className="absolute right-8 top-12 rotate-12 rounded-3xl border border-violet-300/30 bg-gradient-to-br from-violet-500/30 to-indigo-500/10 p-8 shadow-2xl shadow-violet-900/40 backdrop-blur-sm">
                  <ShoppingBag
                    size={100}
                    strokeWidth={1.1}
                    className="text-violet-200"
                  />
                </div>

                <div className="absolute bottom-8 left-1/2 -translate-x-1/2 rounded-full border border-violet-400/30 bg-gradient-to-r from-violet-600/50 to-indigo-600/40 px-12 py-5 shadow-[0_0_65px_rgba(124,58,237,0.35)]">
                  <ShoppingCart
                    size={112}
                    strokeWidth={1.2}
                    className="text-white"
                  />
                </div>

                <div className="absolute left-5 top-24 rounded-2xl border border-white/10 bg-slate-900/80 p-4 shadow-xl">
                  <Package size={32} className="text-fuchsia-300" />
                </div>

                <div className="absolute right-1 top-60 rounded-xl border border-violet-400/20 bg-indigo-950/80 p-3">
                  <Sparkles size={25} className="text-violet-300" />
                </div>

                <div className="absolute bottom-24 left-5 h-3 w-3 rounded-full bg-fuchsia-400 shadow-[0_0_25px_8px_rgba(217,70,239,0.3)]" />
                <div className="absolute left-1/2 top-10 h-2 w-2 rounded-full bg-violet-300 shadow-[0_0_20px_5px_rgba(167,139,250,0.4)]" />
              </div>
            </div>

            {/* Decorative slide indicators */}
            <div className="absolute bottom-1 left-0 flex gap-3">
              <span className="h-3 w-3 rounded-full bg-violet-500 shadow-[0_0_15px_rgba(139,92,246,0.8)]" />
              <span className="h-3 w-3 rounded-full bg-slate-600" />
              <span className="h-3 w-3 rounded-full bg-slate-600" />
            </div>
          </section>

          {/* Right login card */}
          <section className="mx-auto w-full max-w-[540px] lg:max-w-none">
            <div className="relative overflow-hidden rounded-[28px] border border-violet-400/30 bg-slate-900/70 p-10 shadow-[0_0_80px_rgba(76,29,149,0.16)] backdrop-blur-2xl sm:p-12 lg:p-10">
              <div className="pointer-events-none absolute -right-20 -top-24 h-64 w-64 rounded-full bg-violet-600/10 blur-3xl" />
              <div className="relative z-10">{children}</div>
            </div>

            <p className="mt-5 text-center text-xs text-slate-500">
              © {new Date().getFullYear()} ASTRA E-Commerce
            </p>
          </section>
        </div>
      </div>
    </main>
  );
}

function Feature({ icon, title, subtitle }) {
  return (
    <div className="flex items-center gap-4">
      <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full border border-violet-300/20 bg-gradient-to-br from-violet-600/30 to-indigo-600/20 text-violet-200">
        {icon}
      </div>

      <div>
        <p className="text-sm font-semibold text-white">{title}</p>
        <p className="mt-0.5 text-sm text-slate-400">{subtitle}</p>
      </div>
    </div>
  );
}

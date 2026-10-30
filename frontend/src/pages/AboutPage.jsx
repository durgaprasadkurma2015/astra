
import {
  ArrowLeft,
  ArrowRight,
  ShieldCheck,
  Heart,
  Target,
  Eye,
  ShoppingBag,
  Headphones,
  Truck,
  Sparkles,
} from "lucide-react";

import { Link } from "react-router-dom";


const features = [
  {
    icon: ShoppingBag,
    title: "Curated Products",
    description:
      "Discover products selected to deliver quality, style, and value for your everyday needs.",
  },
  {
    icon: ShieldCheck,
    title: "Secure Shopping",
    description:
      "We prioritize a secure shopping experience and help protect your account information.",
  },
  {
    icon: Truck,
    title: "Convenient Delivery",
    description:
      "Enjoy a simple shopping journey from browsing products to placing your order.",
  },
  {
    icon: Headphones,
    title: "Customer First",
    description:
      "We aim to make every step of your shopping experience smooth and convenient.",
  },
];

export default function AboutPage() {
  return (
    <div className="min-h-screen overflow-hidden bg-[#080711] text-white">
      {/* Background effects */}
      <div className="pointer-events-none fixed inset-0 overflow-hidden">
        <div className="absolute -left-40 top-20 h-96 w-96 rounded-full bg-purple-700/15 blur-[120px]" />
        <div className="absolute -right-40 top-80 h-96 w-96 rounded-full bg-indigo-600/15 blur-[120px]" />
      </div>

      {/* Navbar */}
     
      <main className="relative z-10">
        {/* Hero */}
        <section className="mx-auto max-w-7xl px-6 pb-20 pt-24 text-center md:pt-32">
          <div className="mx-auto mb-7 inline-flex items-center gap-2 rounded-full border border-purple-400/20 bg-purple-500/10 px-4 py-2 text-sm text-purple-300">
            <Sparkles size={16} />
            Discover the ASTRA experience
          </div>

          <h1 className="mx-auto max-w-4xl text-4xl font-extrabold leading-tight tracking-tight sm:text-6xl lg:text-7xl">
            Shopping made
            <span className="block bg-gradient-to-r from-purple-400 via-fuchsia-400 to-indigo-400 bg-clip-text text-transparent">
              simple. Smart. ASTRA.
            </span>
          </h1>

          <p className="mx-auto mt-7 max-w-2xl text-base leading-8 text-gray-400 sm:text-lg">
            Welcome to ASTRA, your online shopping destination. Explore
            products, enjoy a smooth browsing experience, and discover a
            smarter way to shop.
          </p>

          <div className="mt-10 flex flex-wrap justify-center gap-4">
            <Link
              to="/products"
              className="inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 px-7 py-3.5 font-semibold shadow-lg shadow-purple-600/20 transition hover:-translate-y-0.5 hover:shadow-purple-500/30"
            >
              Explore Products <ArrowRight size={18} />
            </Link>

            <Link
              to="/"
              className="inline-flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.04] px-7 py-3.5 font-semibold text-gray-200 transition hover:border-purple-400/40 hover:bg-white/[0.08]"
            >
              <ArrowLeft size={18} /> Back to Home
            </Link>
          </div>

          {/* Stats */}
          <div className="mx-auto mt-20 grid max-w-3xl grid-cols-1 gap-4 sm:grid-cols-3">
            {[
              { value: "01", label: "Customer-focused" },
              { value: "24/7", label: "Shop online anytime" },
              { value: "100%", label: "Convenience-focused" },
            ].map((item) => (
              <div
                key={item.value}
                className="rounded-2xl border border-white/10 bg-white/[0.035] p-6 backdrop-blur-xl"
              >
                <p className="text-3xl font-bold text-purple-300">
                  {item.value}
                </p>
                <p className="mt-2 text-sm text-gray-400">
                  {item.label}
                </p>
              </div>
            ))}
          </div>
        </section>

        {/* About */}
        <section className="border-y border-white/[0.07] bg-white/[0.02]">
          <div className="mx-auto grid max-w-7xl items-center gap-12 px-6 py-20 md:grid-cols-2 md:py-28">
            <div>
              <p className="mb-4 text-sm font-semibold uppercase tracking-[0.25em] text-purple-400">
                Who We Are
              </p>

              <h2 className="text-3xl font-bold leading-tight sm:text-4xl">
                A better way to
                <span className="text-purple-400"> shop online.</span>
              </h2>

              <p className="mt-6 leading-8 text-gray-400">
                ASTRA is an e-commerce platform designed to make online
                shopping straightforward and enjoyable. Our goal is to bring
                product discovery and shopping convenience together in one
                place.
              </p>

              <p className="mt-4 leading-8 text-gray-400">
                From exploring products to managing your account, ASTRA aims
                to provide a clean, intuitive experience that keeps your
                shopping journey simple.
              </p>
            </div>

            <div className="relative">
              <div className="absolute -inset-4 rounded-[2rem] bg-gradient-to-br from-purple-600/20 to-indigo-600/20 blur-2xl" />

              <div className="relative rounded-3xl border border-white/10 bg-gradient-to-br from-[#19132d] to-[#0d0b19] p-8 sm:p-10">
                <div className="mb-8 flex h-16 w-16 items-center justify-center rounded-2xl bg-purple-500/15 text-purple-300">
                  <ShoppingBag size={32} />
                </div>

                <h3 className="text-2xl font-bold">
                  Your journey, your ASTRA.
                </h3>

                <p className="mt-4 leading-7 text-gray-400">
                  Browse with ease, discover what you need, and enjoy a
                  shopping experience built around convenience.
                </p>

                <div className="mt-8 h-1.5 overflow-hidden rounded-full bg-white/10">
                  <div className="h-full w-3/4 rounded-full bg-gradient-to-r from-purple-500 to-indigo-500" />
                </div>

                <p className="mt-3 text-xs text-gray-500">
                  Designed around a simpler shopping experience
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Mission and Vision */}
        <section className="mx-auto max-w-7xl px-6 py-20 md:py-28">
          <div className="mb-12 text-center">
            <p className="mb-3 text-sm font-semibold uppercase tracking-[0.25em] text-purple-400">
              What Drives Us
            </p>

            <h2 className="text-3xl font-bold sm:text-4xl">
              Our mission and vision
            </h2>
          </div>

          <div className="grid gap-6 md:grid-cols-2">
            <div className="rounded-3xl border border-purple-400/20 bg-gradient-to-br from-purple-500/10 to-transparent p-8 sm:p-10">
              <div className="mb-6 flex h-14 w-14 items-center justify-center rounded-2xl bg-purple-500/15 text-purple-300">
                <Target size={28} />
              </div>

              <h3 className="text-2xl font-bold">Our Mission</h3>

              <p className="mt-4 leading-8 text-gray-400">
                To make online shopping convenient and accessible through an
                intuitive platform that helps customers explore products and
                manage their shopping journey with ease.
              </p>
            </div>

            <div className="rounded-3xl border border-indigo-400/20 bg-gradient-to-br from-indigo-500/10 to-transparent p-8 sm:p-10">
              <div className="mb-6 flex h-14 w-14 items-center justify-center rounded-2xl bg-indigo-500/15 text-indigo-300">
                <Eye size={28} />
              </div>

              <h3 className="text-2xl font-bold">Our Vision</h3>

              <p className="mt-4 leading-8 text-gray-400">
                To build a trusted shopping destination where thoughtful
                design, useful technology, and customer-focused experiences
                come together.
              </p>
            </div>
          </div>
        </section>

        {/* Features */}
        <section className="border-y border-white/[0.07] bg-white/[0.02]">
          <div className="mx-auto max-w-7xl px-6 py-20 md:py-28">
            <div className="mx-auto mb-14 max-w-2xl text-center">
              <p className="mb-3 text-sm font-semibold uppercase tracking-[0.25em] text-purple-400">
                The ASTRA Difference
              </p>

              <h2 className="text-3xl font-bold sm:text-4xl">
                Designed for your convenience
              </h2>

              <p className="mt-5 leading-7 text-gray-400">
                Every detail aims to make your online shopping experience
                easier and more enjoyable.
              </p>
            </div>

            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
              {features.map((feature) => {
                const Icon = feature.icon;

                return (
                  <div
                    key={feature.title}
                    className="group rounded-2xl border border-white/10 bg-[#100d1d] p-6 transition duration-300 hover:-translate-y-1 hover:border-purple-400/30 hover:bg-[#151027]"
                  >
                    <div className="mb-6 flex h-12 w-12 items-center justify-center rounded-xl bg-purple-500/10 text-purple-300 transition group-hover:bg-purple-500/20">
                      <Icon size={24} />
                    </div>

                    <h3 className="text-lg font-bold">
                      {feature.title}
                    </h3>

                    <p className="mt-3 text-sm leading-7 text-gray-400">
                      {feature.description}
                    </p>
                  </div>
                );
              })}
            </div>
          </div>
        </section>

        {/* CTA */}
        <section className="mx-auto max-w-5xl px-6 py-24 text-center md:py-32">
          <div className="rounded-3xl border border-purple-400/20 bg-gradient-to-br from-purple-600/15 via-[#151027] to-indigo-600/10 px-6 py-12 sm:px-12 sm:py-16">
            <div className="mx-auto mb-6 flex h-14 w-14 items-center justify-center rounded-2xl bg-purple-500/20 text-purple-300">
              <Heart size={28} />
            </div>

            <h2 className="text-3xl font-bold sm:text-4xl">
              Ready to explore ASTRA?
            </h2>

            <p className="mx-auto mt-5 max-w-xl leading-7 text-gray-400">
              Start discovering products and enjoy a simple, convenient
              online shopping experience.
            </p>

            <Link
              to="/products"
              className="mt-8 inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 px-7 py-3.5 font-semibold transition hover:shadow-lg hover:shadow-purple-500/20"
            >
              Start Shopping <ArrowRight size={18} />
            </Link>
          </div>
        </section>
      </main>

      {/* Footer */}
     
    </div>
  );
}

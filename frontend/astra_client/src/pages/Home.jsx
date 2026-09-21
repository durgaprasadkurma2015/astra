import { useMemo, useState } from "react";
import Header from "../components/header/Header";
import HeroBanner from "../components/home/HeroBanner";
import SidebarCategories from "../components/home/SidebarCategories";
import CategoryGrid from "../components/home/CategoryGrid";
import Deals from "../components/home/Deals";
import BestSellers from "../components/home/BestSellers";
import Footer from "../components/footer/Footer";
import AuthModal from "../components/auth/AuthModal";
import { useSelector } from "react-redux";

const PRODUCTS = [
  { id: 1, brand: "AstraTech", name: "NovaBook 15 Laptop", price: 599.99, oldPrice: 799.99, rating: 4.7, reviews: 1234, discount: 25, image: "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=700&q=80" },
  { id: 2, brand: "SoundAir", name: "AirBeat Pro Wireless Earbuds", price: 79.99, oldPrice: 129.99, rating: 4.5, reviews: 2180, discount: 38, image: "https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?auto=format&fit=crop&w=700&q=80" },
  { id: 3, brand: "AstraFit", name: "Pulse X Smart Watch", price: 129.99, oldPrice: 199.99, rating: 4.6, reviews: 1656, discount: 35, image: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=700&q=80" },
  { id: 4, brand: "Stride", name: "Velocity Running Shoes", price: 69.99, oldPrice: 99.99, rating: 4.4, reviews: 892, discount: 30, image: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=700&q=80" },
  { id: 5, brand: "KitchenPro", name: "CrispAir 5.8QT Air Fryer", price: 89.99, oldPrice: 129.99, rating: 4.7, reviews: 3421, discount: 31, image: "https://images.unsplash.com/photo-1585515320310-259814833e62?auto=format&fit=crop&w=700&q=80" },
  { id: 6, brand: "UrbanPack", name: "Pro Laptop Backpack", price: 49.99, oldPrice: 79.99, rating: 4.5, reviews: 1023, discount: 38, image: "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=700&q=80" },
];

export default function Home() {
  const [authOpen, setAuthOpen] = useState(false);
  const [cart, setCart] = useState([]);
  const [query, setQuery] = useState("");
  const user = useSelector((state) => state.auth.user);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return PRODUCTS;
    return PRODUCTS.filter((p) => `${p.name} ${p.brand}`.toLowerCase().includes(q));
  }, [query]);

  const addToCart = (product) => setCart((items) => [...items, product]);

  return (
    <>
      <Header cartCount={cart.length} onAuth={() => setAuthOpen(true)} onSearch={setQuery} />

      <main>
        {user && (
          <div className="welcome-bar">
            Welcome back, <strong>{user.name}</strong>. Explore today's offers.
          </div>
        )}

        <div className="home-layout">
          <SidebarCategories />
          <div className="home-main">
            <HeroBanner />
            <CategoryGrid />
          </div>
        </div>

        {query && (
          <div className="search-result-note">
            Search results for <strong>"{query}"</strong> — {filtered.length} products
          </div>
        )}

        <Deals products={filtered} onAdd={addToCart} />
        <BestSellers products={filtered.length >= 5 ? filtered : PRODUCTS} onAdd={addToCart} />
      </main>

      <Footer />
      <AuthModal open={authOpen} onClose={() => setAuthOpen(false)} />
    </>
  );
}

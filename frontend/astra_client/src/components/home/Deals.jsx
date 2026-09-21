import ProductCard from "../product/ProductCard";

export default function Deals({ products, onAdd }) {
  return (
    <section id="deals" className="section">
      <div className="section-title">
        <div>
          <h2>Today's Deals</h2>
          <span>Up to 70% Off</span>
        </div>
        <button>View All →</button>
      </div>

      <div className="product-grid">
        {products.slice(0, 5).map((p) => (
          <ProductCard key={p.id} product={p} onAdd={onAdd} />
        ))}
      </div>
    </section>
  );
}

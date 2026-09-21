import ProductCard from "../product/ProductCard";

export default function BestSellers({ products, onAdd }) {
  return (
    <section id="best-sellers" className="section">
      <div className="section-title">
        <div>
          <h2>Best Sellers</h2>
          <div className="tabs">
            <button className="active">Electronics</button>
            <button>Fashion</button>
            <button>Home & Kitchen</button>
            <button>Beauty</button>
            <button>Sports</button>
          </div>
        </div>
        <button>View All →</button>
      </div>

      <div className="product-grid">
        {products.slice(1, 6).map((p) => (
          <ProductCard key={p.id} product={p} onAdd={onAdd} />
        ))}
      </div>
    </section>
  );
}

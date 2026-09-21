import { memo } from "react";
import { Heart, ShoppingCart } from "lucide-react";
import Rating from "../common/Rating";

function ProductCard({ product, onAdd }) {
  return (
    <article className="product-card">
      <button className="heart-btn" aria-label="Add to wishlist">
        <Heart size={18} />
      </button>

      <div className="product-image-wrap">
        <img src={product.image} alt={product.name} className="product-image" />
        {product.discount && <span className="discount-badge">{product.discount}% OFF</span>}
      </div>

      <div className="product-content">
        <p className="product-brand">{product.brand}</p>
        <h3>{product.name}</h3>
        <Rating value={product.rating} count={product.reviews} />

        <div className="price-row">
          <strong>${product.price.toFixed(2)}</strong>
          <del>${product.oldPrice.toFixed(2)}</del>
        </div>

        <button className="add-btn" onClick={() => onAdd(product)}>
          <ShoppingCart size={16} />
          Add to Cart
        </button>
      </div>
    </article>
  );
}

export default memo(ProductCard);

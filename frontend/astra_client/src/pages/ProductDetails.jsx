import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { orderApi } from '../api/orderApi';
import { setCart } from '../store/cartSlice';
import { useParams } from 'react-router-dom';
import { ShoppingCart, Heart, Truck, ShieldCheck, ArrowLeft } from 'lucide-react';
import { catalogApi } from '../api/catalogApi';
import Rating from '../components/common/Rating';
import ReviewsSection from '../components/reviews/ReviewsSection';
import RecommendationStrip from '../components/product/RecommendationStrip';

export default function ProductDetails() {
  const { slug } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  const [product, setProduct] = useState(null);
  const [qty, setQty] = useState(1);

  useEffect(() => { catalogApi.product(slug).then(r => setProduct(r.data)); }, [slug]);

  if (!product) return <main className="catalog-empty">Loading product…</main>;

  const discount = product.mrp ? Math.round((1 - Number(product.price) / Number(product.mrp)) * 100) : 0;

  return (
    <main className="product-details">
      <button className="back-link" onClick={() => navigate(-1)}><ArrowLeft size={16}/> Back</button>
      <div className="detail-layout">
        <div className="detail-image"><img src={product.imageUrl} alt={product.name}/></div>
        <div className="detail-info">
          <span className="product-category">{product.categoryName}</span>
          <h1>{product.name}</h1>
          <div className="detail-rating"><Rating value={product.rating}/> <span>{product.reviewCount.toLocaleString()} ratings</span></div>
          <hr/>
          <p className="detail-description">{product.description}</p>
          <div className="detail-price">${product.price} {product.mrp && <del>${product.mrp}</del>} {discount > 0 && <b>{discount}% off</b>}</div>
          <p className={product.stock > 0 ? 'stock-ok' : 'stock-out'}>{product.stock > 0 ? `${product.stock} units available` : 'Currently unavailable'}</p>
          <div className="qty"><label>Quantity</label><select value={qty} onChange={e => setQty(Number(e.target.value))}>{[1,2,3,4,5].map(n => <option key={n}>{n}</option>)}</select></div>
          <div className="detail-actions">
            <button className="primary-btn" onClick={()=>{if(!user){navigate("/");return;} orderApi.addToCart(product.id,qty).then(r=>dispatch(setCart(r.data)));}}><ShoppingCart size={18}/> Add to Cart</button>
            <button className="secondary-btn" onClick={()=>{if(!user){navigate("/");return;} orderApi.toggleWishlist(product.id).then(()=>{});}}><Heart size={18}/> Wishlist</button>
          </div>
          <div className="trust-row">
            <span><Truck size={18}/> Fast delivery</span>
            <span><ShieldCheck size={18}/> Secure checkout</span>
          </div>
        </div>
      </div>
    <RecommendationStrip slug={slug}/>
      <ReviewsSection slug={slug} productName={product.name}/>
    </main>
  );
}

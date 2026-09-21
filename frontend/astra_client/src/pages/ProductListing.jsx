import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { searchApi } from '../api/searchApi';
import { catalogApi } from '../api/catalogApi';
import Rating from '../components/common/Rating';

export default function ProductListing() {
  const [params, setParams] = useSearchParams();
  const [data, setData] = useState({content: [], totalElements: 0, totalPages: 0, suggestions: []});
  const [categories, setCategories] = useState([]);
  const [trending, setTrending] = useState([]);
  const [loading, setLoading] = useState(true);
  const [min, setMin] = useState(params.get('minPrice') || '');
  const [max, setMax] = useState(params.get('maxPrice') || '');

  const q = params.get('q') || '';
  const category = params.get('category') || '';
  const sort = params.get('sort') || 'relevance';
  const page = Number(params.get('page') || 0);

  useEffect(() => { catalogApi.categories().then(r => setCategories(r.data)).catch(() => {}); searchApi.trending().then(r => setTrending(r.data)).catch(() => {}); }, []);
  useEffect(() => {
    setLoading(true);
    searchApi.search({q, category, minPrice: params.get('minPrice') || undefined, maxPrice: params.get('maxPrice') || undefined, sort, page, size: 12})
      .then(r => setData(r.data)).catch(() => setData({content: [], totalElements: 0, totalPages: 0, suggestions: []}))
      .finally(() => setLoading(false));
  }, [q, category, sort, page, params.get('minPrice'), params.get('maxPrice')]);

  const update = (key, value) => { const next = new URLSearchParams(params); if (value) next.set(key, value); else next.delete(key); next.set('page', '0'); setParams(next); };
  const applyPrice = e => { e.preventDefault(); const next = new URLSearchParams(params); min ? next.set('minPrice', min) : next.delete('minPrice'); max ? next.set('maxPrice', max) : next.delete('maxPrice'); next.set('page','0'); setParams(next); };

  return <main className="catalog-page">
    <div className="catalog-head"><div><p className="eyebrow">ASTRA SMART SEARCH</p><h1>{q ? `Results for "${q}"` : category ? categories.find(c => c.slug === category)?.name || 'Products' : 'All Products'}</h1><p className="muted">{data.totalElements} products available {data.elasticsearch ? '· AI-ready search index' : ''}</p></div>
      <select value={sort} onChange={e => update('sort', e.target.value)}><option value="relevance">Featured</option><option value="rating">Top Rated</option><option value="price_asc">Price: Low to High</option><option value="price_desc">Price: High to Low</option><option value="newest">Newest</option></select></div>
    {q && data.suggestions?.length > 0 && <div className="search-suggestions"><span>Related:</span>{data.suggestions.map(x => <button key={x} onClick={() => update('q', x)}>{x}</button>)}</div>}
    {!q && trending.length > 0 && <div className="search-suggestions"><span>Trending:</span>{trending.slice(0,8).map(x => <button key={x} onClick={() => update('q', x)}>{x}</button>)}</div>}
    <div className="catalog-layout"><aside className="filter-panel"><h3>Categories</h3><button className={!category ? 'active' : ''} onClick={() => update('category','')}>All</button>{categories.map(c => <button key={c.id} className={category === c.slug ? 'active' : ''} onClick={() => update('category', c.slug)}>{c.name}</button>)}<form className="price-filter" onSubmit={applyPrice}><h3>Price</h3><div><input type="number" min="0" placeholder="Min" value={min} onChange={e=>setMin(e.target.value)}/><input type="number" min="0" placeholder="Max" value={max} onChange={e=>setMax(e.target.value)}/></div><button className="primary-btn" type="submit">Apply</button></form></aside>
      <section className="catalog-grid">{loading ? <div className="catalog-empty">Searching ASTRA…</div> : data.content.length === 0 ? <div className="catalog-empty">No products found.</div> : data.content.map(p => <Link to={`/products/${p.slug}`} className="catalog-card" key={p.id}><div className="catalog-image"><img src={p.imageUrl} alt={p.name}/></div><div className="catalog-card-body"><div className="product-category">{p.categoryName}</div><h3>{p.name}</h3><Rating value={p.rating}/><div className="price-row"><strong>₹{p.price}</strong>{p.mrp && <del>₹{p.mrp}</del>}</div><small>{p.stock > 0 ? 'In stock' : 'Out of stock'} · {p.reviewCount.toLocaleString()} reviews</small></div></Link>)}</section></div>
    {data.totalPages > 1 && <div className="pagination"><button disabled={page === 0} onClick={() => update('page', String(page - 1))}>Previous</button><span>Page {page + 1} of {data.totalPages}</span><button disabled={page + 1 >= data.totalPages} onClick={() => update('page', String(page + 1))}>Next</button></div>}
  </main>;
}

import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { reviewApi } from '../../api/reviewApi';
import Rating from '../common/Rating';

export default function ReviewsSection({ slug, productName }) {
 const user=useSelector(s=>s.auth.user); const [data,setData]=useState(null); const [form,setForm]=useState({rating:5,title:'',body:''}); const [msg,setMsg]=useState(''); const [busy,setBusy]=useState(false);
 const load=()=>reviewApi.list(slug).then(r=>setData(r.data)).catch(()=>setData({content:[]})); useEffect(()=>{load()},[slug]);
 const submit=async e=>{e.preventDefault();setBusy(true);setMsg('');try{await reviewApi.create(slug,form);setMsg('Review submitted. Verified purchases are published automatically; other reviews go to moderation.');setForm({rating:5,title:'',body:''});load()}catch(err){setMsg(err.response?.data?.message||'Unable to submit review.')}finally{setBusy(false)}};
 return <section className="reviews-section"><div className="section-title"><div><h2>Customer reviews</h2><span>Real feedback from the ASTRA community</span></div></div>
 {user&&<form className="review-form" onSubmit={submit}><h3>Write a review for {productName}</h3><label>Rating<select value={form.rating} onChange={e=>setForm({...form,rating:Number(e.target.value)})}>{[5,4,3,2,1].map(n=><option key={n} value={n}>{n} stars</option>)}</select></label><label>Title<input value={form.title} maxLength={100} onChange={e=>setForm({...form,title:e.target.value})} required/></label><label>Your review<textarea value={form.body} maxLength={2000} onChange={e=>setForm({...form,body:e.target.value})} required/></label><button className="primary-btn" disabled={busy}>{busy?'Submitting…':'Submit review'}</button></form>}
 {msg&&<div className="success-banner">{msg}</div>}
 <div className="review-list">{data?.content?.length?data.content.map(r=><article className="review-card" key={r.id}><div className="review-head"><strong>{r.reviewerName}</strong>{r.verifiedPurchase&&<span className="verified-badge">✓ Verified purchase</span>}<span className="review-date">{new Date(r.createdAt).toLocaleDateString()}</span></div><Rating value={r.rating}/><h3>{r.title}</h3><p>{r.body}</p></article>):<div className="empty-card">No published reviews yet. Be the first to share your experience.</div>}</div></section>;
}

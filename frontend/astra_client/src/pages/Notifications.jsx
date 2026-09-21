import {useEffect,useState} from "react";
import {Bell,CheckCheck,Package,ShieldCheck} from "lucide-react";
import Header from "../components/header/Header";
import Footer from "../components/footer/Footer";
import {getNotifications,markAllNotificationsRead,markNotificationRead} from "../api/notificationApi";
import {useSelector} from "react-redux";
import {useNavigate} from "react-router-dom";

export default function Notifications(){
 const user=useSelector(s=>s.auth.user), nav=useNavigate(), [data,setData]=useState({items:[],unread:0}),[loading,setLoading]=useState(true);
 useEffect(()=>{if(!user){nav('/');return;} getNotifications().then(r=>setData(r.data)).finally(()=>setLoading(false));},[user,nav]);
 const read=async n=>{if(!n.readAt) await markNotificationRead(n.id); setData(d=>({...d,unread:Math.max(0,d.unread-(n.readAt?0:1)),items:d.items.map(x=>x.id===n.id?{...x,readAt:new Date().toISOString()}:x)})); if(n.type==='ORDER'&&n.referenceId)nav(`/orders/${n.referenceId}`);};
 const all=async()=>{await markAllNotificationsRead();setData(d=>({...d,unread:0,items:d.items.map(x=>({...x,readAt:new Date().toISOString()}))}));};
 return <><Header/><main className="notification-page"><div className="page-title"><div><small>ASTRA UPDATES</small><h1>Notifications</h1></div>{data.unread>0&&<button className="secondary-btn" onClick={all}><CheckCheck size={17}/> Mark all read</button>}</div>{loading?<div className="empty-card">Loading notifications...</div>:data.items.length===0?<div className="empty-card"><Bell size={38}/><h3>You're all caught up</h3><p>No new notifications.</p></div>:<div className="notification-list">{data.items.map(n=><button key={n.id} className={`notification-card ${n.readAt?'read':''}`} onClick={()=>read(n)}><span className="notification-icon">{n.type==='ORDER'?<Package size={19}/>:<ShieldCheck size={19}/>}</span><span><strong>{n.title}</strong><small>{n.message}</small><time>{new Date(n.createdAt).toLocaleString()}</time></span>{!n.readAt&&<i/>}</button>)}</div>}</main><Footer/></>;
}

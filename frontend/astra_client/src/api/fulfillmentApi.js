import api from './axiosClient';
export const fulfillmentApi={
 shipment:(orderNumber)=>api.get(`/orders/${orderNumber}/shipment`).then(r=>r.data),
 track:(trackingNumber)=>api.get(`/tracking/${trackingNumber}`).then(r=>r.data),
 returns:()=>api.get('/returns').then(r=>r.data),
 requestReturn:(body)=>api.post('/returns',body).then(r=>r.data),
 adminReturns:()=>api.get('/admin/returns').then(r=>r.data),
 updateReturn:(id,status,note)=>api.patch(`/admin/returns/${id}`,null,{params:{status,note}}).then(r=>r.data),
 updateShipment:(orderNumber,body)=>api.put(`/admin/orders/${orderNumber}/shipment`,body).then(r=>r.data)
};

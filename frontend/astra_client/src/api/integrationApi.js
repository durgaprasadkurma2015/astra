import axiosClient from './axiosClient';
export const integrationApi = {
  locationSearch: q => axiosClient.get('/location/search', { params: { q } }),
  createShipment: orderNumber => axiosClient.post(`/admin/shipping/orders/${encodeURIComponent(orderNumber)}/create`),
  syncTracking: trackingNumber => axiosClient.post(`/admin/shipping/tracking/${encodeURIComponent(trackingNumber)}/sync`),
  cloudUpload: file => { const form = new FormData(); form.append('file', file); return axiosClient.post('/media/cloud/images', form, { headers: { 'Content-Type': 'multipart/form-data' } }); }
};

import axiosClient from './axiosClient';
export const adminApi={
 dashboard:()=>axiosClient.get('/admin/dashboard'),
 products:()=>axiosClient.get('/admin/products'),
 createProduct:(data)=>axiosClient.post('/admin/products',data),
 updateProduct:(id,data)=>axiosClient.put(`/admin/products/${id}`,data),
 inventory:(id,stock)=>axiosClient.patch(`/admin/products/${id}/inventory`,{stock}),
 deleteProduct:(id)=>axiosClient.delete(`/admin/products/${id}`),
 categories:()=>axiosClient.get('/admin/categories'),
 createCategory:(data)=>axiosClient.post('/admin/categories',data),
 orders:()=>axiosClient.get('/admin/orders'),
 updateOrderStatus:(no,status)=>axiosClient.patch(`/admin/orders/${no}/status?status=${status}`),
 users:()=>axiosClient.get('/admin/users'),
 updateUser:(id,params)=>axiosClient.patch(`/admin/users/${id}`,null,{params}),
};

adminApi.pendingReviews = () => axiosClient.get('/admin/reviews/pending');
adminApi.publishReview = (id) => axiosClient.post(`/admin/reviews/${id}/publish`);
adminApi.rejectReview = (id) => axiosClient.post(`/admin/reviews/${id}/reject`);

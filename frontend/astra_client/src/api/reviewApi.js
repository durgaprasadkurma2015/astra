import axiosClient from './axiosClient';
export const reviewApi={ list:(slug,page=0,size=10)=>axiosClient.get(`/reviews/product/${encodeURIComponent(slug)}`,{params:{page,size}}), create:(slug,data)=>axiosClient.post(`/reviews/product/${encodeURIComponent(slug)}`,data), remove:(id)=>axiosClient.delete(`/reviews/${id}`) };

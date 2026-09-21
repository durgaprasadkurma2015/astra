import axiosClient from './axiosClient';
export const orderApi={
 cart:()=>axiosClient.get('/cart'), addToCart:(id,quantity=1)=>axiosClient.post(`/cart/items/${id}`,null,{params:{quantity}}), updateCart:(id,quantity)=>axiosClient.patch(`/cart/items/${id}`,{quantity}), removeFromCart:(id)=>axiosClient.delete(`/cart/items/${id}`),
 wishlist:()=>axiosClient.get('/wishlist'), toggleWishlist:(id)=>axiosClient.post(`/wishlist/${id}/toggle`),
 addresses:()=>axiosClient.get('/addresses'), addAddress:(data)=>axiosClient.post('/addresses',data), removeAddress:(id)=>axiosClient.delete(`/addresses/${id}`),
 checkout:(data)=>axiosClient.post('/orders/checkout',data), onlineCheckout:(data)=>axiosClient.post('/orders/checkout/online',data), verifyPayment:(data)=>axiosClient.post('/payments/verify',data), paymentStatus:(number)=>axiosClient.get(`/payments/orders/${number}`), orders:()=>axiosClient.get('/orders'), order:(number)=>axiosClient.get(`/orders/${number}`), cancel:(number)=>axiosClient.post(`/orders/${number}/cancel`)
};

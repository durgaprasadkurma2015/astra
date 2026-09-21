import axiosClient from './axiosClient';

export const catalogApi = {
  products: (params) => axiosClient.get('/catalog/products', { params }),
  product: (slug) => axiosClient.get(`/catalog/products/${slug}`),
  categories: () => axiosClient.get('/catalog/categories'),
  bestSellers: () => axiosClient.get('/catalog/products/best-sellers'),
  topRated: () => axiosClient.get('/catalog/products/top-rated'),
};

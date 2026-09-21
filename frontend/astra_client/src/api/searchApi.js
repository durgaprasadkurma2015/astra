import axiosClient from './axiosClient';
export const searchApi = {
  search: (params) => axiosClient.get('/search', { params }),
  suggestions: (q) => axiosClient.get('/search/suggestions', { params: { q } }),
  trending: (days=7) => axiosClient.get('/search/trending', { params: { days } }),
};

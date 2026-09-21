import axiosClient from './axiosClient';
export const recommendationApi={ related:(slug)=>axiosClient.get(`/recommendations/related/${encodeURIComponent(slug)}`), mine:()=>axiosClient.get('/recommendations/for-me') };

import { createSlice } from '@reduxjs/toolkit';
const initialState={items:[],subtotal:0,shippingFee:0,total:0,itemCount:0};
const slice=createSlice({name:'cart',initialState,reducers:{setCart:(s,a)=>Object.assign(s,a.payload),clearCart:(s)=>Object.assign(s,initialState)}});
export const {setCart,clearCart}=slice.actions; export default slice.reducer;

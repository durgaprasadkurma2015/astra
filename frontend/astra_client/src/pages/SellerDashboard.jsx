import { useEffect, useState } from "react";
import { Container, Paper, Typography } from "@mui/material";
import { sellerApi } from "../api/api";

export default function SellerDashboard(){
 const [data,setData]=useState(null);
 useEffect(()=>{sellerApi.me().then(r=>setData(r.data));},[]);
 return <Container maxWidth="lg" sx={{py:5}}><Typography variant="h3" mb={3}>Seller Dashboard</Typography><Paper sx={{p:4}}><pre style={{whiteSpace:"pre-wrap"}}>{JSON.stringify(data,null,2)}</pre></Paper></Container>;
}
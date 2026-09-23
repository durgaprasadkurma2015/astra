import { useEffect, useState } from "react";
import { Container, Grid, Paper, Typography, Button, Stack } from "@mui/material";
import { adminApi } from "../api/api";

export default function AdminDashboard(){
 const [data,setData]=useState({});
 useEffect(()=>{adminApi.dashboard().then(r=>setData(r.data));},[]);
 const cards=Object.entries(data).filter(([,v])=>["string","number"].includes(typeof v));
 return <Container maxWidth="xl" sx={{py:5}}><Typography variant="h3" mb={3}>Admin Dashboard</Typography>
 <Grid container spacing={3}>{cards.map(([k,v])=><Grid size={{xs:12,sm:6,md:3}} key={k}><Paper sx={{p:3}}><Typography color="text.secondary">{k}</Typography><Typography variant="h4">{String(v)}</Typography></Paper></Grid>)}</Grid>
 <Stack direction="row" gap={2} mt={4}><Button variant="outlined" onClick={()=>adminApi.reindex?.()}>Reindex products</Button><Button variant="outlined" onClick={()=>adminApi.orders()}>Load orders</Button></Stack>
 </Container>;
}
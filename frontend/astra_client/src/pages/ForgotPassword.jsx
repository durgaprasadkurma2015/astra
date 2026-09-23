import { useState } from "react";
import { Container, Paper, TextField, Button, Typography, Alert } from "@mui/material";
import { authApi } from "../api/api";

export default function ForgotPassword(){
 const [email,setEmail]=useState(""); const [msg,setMsg]=useState("");
 const submit=async e=>{e.preventDefault(); const r=await authApi.forgotPassword({email}); setMsg(r.data.message||"Reset instructions sent");};
 return <Container maxWidth="sm" sx={{py:8}}><Paper sx={{p:4}}><Typography variant="h4" mb={3}>Forgot password</Typography>{msg&&<Alert>{msg}</Alert>}<form onSubmit={submit}><TextField fullWidth required type="email" label="Email" value={email} onChange={e=>setEmail(e.target.value)} margin="normal"/><Button type="submit" variant="contained">Send reset request</Button></form></Paper></Container>;
}
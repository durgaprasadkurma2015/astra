import { useState } from "react";
import { Alert, Box, Button, Container, Paper, TextField, Typography } from "@mui/material";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name:"", email:"", password:"", phone:"" });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const submit = async e => {
    e.preventDefault(); setError(""); setMessage("");
    try { const r = await register(form); setMessage(r.data.message || "Registration successful. Verify your email."); }
    catch (err) { setError(err.response?.data?.message || "Registration failed"); }
  };

  return <Container maxWidth="sm" sx={{ py: 8 }}>
    <Paper sx={{ p: 4 }}>
      <Typography variant="h4" mb={3}>Create ASTRA account</Typography>
      {message && <Alert severity="success">{message}</Alert>}
      {error && <Alert severity="error">{error}</Alert>}
      <Box component="form" onSubmit={submit}>
        {["name","email","phone","password"].map(name =>
          <TextField key={name} fullWidth required={name !== "phone"} label={name[0].toUpperCase()+name.slice(1)}
            type={name === "password" ? "password" : name === "email" ? "email" : "text"} margin="normal"
            value={form[name]} onChange={e => setForm({...form, [name]:e.target.value})} />
        )}
        <Button type="submit" fullWidth variant="contained" sx={{ mt: 2 }}>Register</Button>
      </Box>
      <Typography sx={{ mt: 2 }}>Already registered? <Link to="/login">Login</Link></Typography>
    </Paper>
  </Container>;
}
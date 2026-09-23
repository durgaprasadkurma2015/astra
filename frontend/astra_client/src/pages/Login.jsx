import { useState } from "react";
import { Alert, Box, Button, Container, Paper, TextField, Typography } from "@mui/material";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");

  const submit = async e => {
    e.preventDefault();
    setError("");
    try { await login(form); navigate("/"); }
    catch (err) { setError(err.response?.data?.message || "Login failed"); }
  };

  return <Container maxWidth="sm" sx={{ py: 8 }}>
    <Paper sx={{ p: 4 }}>
      <Typography variant="h4" mb={3}>Login</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Box component="form" onSubmit={submit}>
        <TextField fullWidth label="Email" type="email" margin="normal" required
          value={form.email} onChange={e => setForm({...form, email:e.target.value})} />
        <TextField fullWidth label="Password" type="password" margin="normal" required
          value={form.password} onChange={e => setForm({...form, password:e.target.value})} />
        <Button type="submit" fullWidth variant="contained" size="large" sx={{ mt: 2 }}>Login</Button>
      </Box>
      <Button component={Link} to="/forgot-password" sx={{ mt: 1 }}>Forgot password?</Button>
      <Typography sx={{ mt: 2 }}>New to ASTRA? <Link to="/register">Create account</Link></Typography>
    </Paper>
  </Container>;
}
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  AppBar, Toolbar, Typography, IconButton, Badge, Button, Box,
  Drawer, List, ListItemButton, ListItemText, TextField, InputAdornment
} from "@mui/material";
import { ShoppingCart, Menu, Search, AccountCircle } from "@mui/icons-material";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const { count } = useCart();
  const navigate = useNavigate();
  const [drawer, setDrawer] = useState(false);
  const [q, setQ] = useState("");

  const search = (e) => {
    e.preventDefault();
    if (q.trim()) navigate(`/search?q=${encodeURIComponent(q.trim())}`);
  };

  return <>
    <AppBar position="sticky">
      <Toolbar sx={{ gap: 1 }}>
        <IconButton color="inherit" onClick={() => setDrawer(true)}><Menu /></IconButton>
        <Typography component={Link} to="/" variant="h6" sx={{ color: "inherit", textDecoration: "none", fontWeight: 800 }}>
          ASTRA
        </Typography>
        <Box component="form" onSubmit={search} sx={{ flex: 1, maxWidth: 700, mx: 2 }}>
          <TextField fullWidth size="small" value={q} onChange={e => setQ(e.target.value)}
            placeholder="Search products..." sx={{ bgcolor: "white", borderRadius: 1 }}
            InputProps={{ endAdornment: <InputAdornment position="end"><Search /></InputAdornment> }} />
        </Box>
        {user ? (
          <Button color="inherit" onClick={() => navigate("/profile")} startIcon={<AccountCircle />}>{user.name}</Button>
        ) : <Button color="inherit" onClick={() => navigate("/login")}>Login</Button>}
        <IconButton color="inherit" onClick={() => navigate("/cart")}>
          <Badge badgeContent={count} color="secondary"><ShoppingCart /></Badge>
        </IconButton>
      </Toolbar>
    </AppBar>

    <Drawer open={drawer} onClose={() => setDrawer(false)}>
      <Box sx={{ width: 270 }} role="presentation" onClick={() => setDrawer(false)}>
        <List>
          {[
            ["/", "Home"], ["/products", "Products"], ["/wishlist", "Wishlist"],
            ["/orders", "My Orders"], ["/shipments", "Shipments"], ["/profile", "Profile"],
            ...(user?.role === "ADMIN" ? [["/admin", "Admin Dashboard"]] : []),
            ...(user?.role === "SELLER" ? [["/seller", "Seller Dashboard"]] : [])
          ].map(([path, label]) => (
            <ListItemButton key={path} component={Link} to={path}><ListItemText primary={label} /></ListItemButton>
          ))}
          {user && <ListItemButton onClick={logout}><ListItemText primary="Logout" /></ListItemButton>}
        </List>
      </Box>
    </Drawer>
    <Box component="main">{children}</Box>
  </>;
}
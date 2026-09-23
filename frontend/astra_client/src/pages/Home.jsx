import { useEffect, useState } from "react";
import { Box, Container, Grid, Typography, Button, Stack } from "@mui/material";
import { Link } from "react-router-dom";
import { productApi, categoryApi, recommendationApi } from "../api/api";
import ProductCard from "../components/ProductCard";

export default function Home() {
  const [featured, setFeatured] = useState([]);
  const [categories, setCategories] = useState([]);
  const [trending, setTrending] = useState([]);

  useEffect(() => {
    Promise.allSettled([productApi.featured(), categoryApi.list(), recommendationApi.trending()])
      .then(([p, c, t]) => {
        setFeatured(p.status === "fulfilled" ? (p.value.data || []) : []);
        setCategories(c.status === "fulfilled" ? (c.value.data || []) : []);
        setTrending(t.status === "fulfilled" ? (t.value.data || []) : []);
      });
  }, []);

  return <Container maxWidth="xl" sx={{ py: 4 }}>
    <Box sx={{ p: { xs: 3, md: 7 }, borderRadius: 3, bgcolor: "primary.main", color: "white", mb: 4 }}>
      <Typography variant="h2" sx={{ fontWeight: 800 }}>Shop smarter with ASTRA</Typography>
      <Typography variant="h6" sx={{ mt: 2 }}>Products, orders, payments, shipping and recommendations connected to your Spring Boot backend.</Typography>
      <Button component={Link} to="/products" variant="contained" color="secondary" sx={{ mt: 3 }}>Explore products</Button>
    </Box>

    <Typography variant="h4" mb={2}>Categories</Typography>
    <Stack direction="row" gap={2} flexWrap="wrap" mb={5}>
      {categories.map(c => <Button key={c.id} component={Link} to={`/products?categoryId=${c.id}`} variant="outlined">{c.name}</Button>)}
    </Stack>

    <Typography variant="h4" mb={2}>Featured</Typography>
    <Grid container spacing={3} mb={5}>
      {featured.slice(0, 8).map(p => <Grid key={p.id} size={{ xs: 12, sm: 6, md: 3 }}><ProductCard product={p} /></Grid>)}
    </Grid>

    <Typography variant="h4" mb={2}>Trending</Typography>
    <Grid container spacing={3}>
      {trending.slice(0, 8).map(p => <Grid key={p.id} size={{ xs: 12, sm: 6, md: 3 }}><ProductCard product={p} /></Grid>)}
    </Grid>
  </Container>;
}
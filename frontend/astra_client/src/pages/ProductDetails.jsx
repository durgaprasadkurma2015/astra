import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Alert, Button, Container, Grid, Rating, Stack, Typography, TextField, Divider } from "@mui/material";
import { productApi, reviewApi, recommendationApi } from "../api/api";
import { useCart } from "../context/CartContext";
import ProductCard from "../components/ProductCard";

export default function ProductDetails() {
  const { id } = useParams();
  const { addToCart } = useCart();
  const [product, setProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [similar, setSimilar] = useState([]);
  const [qty, setQty] = useState(1);
  const [message, setMessage] = useState("");

  useEffect(() => {
    Promise.allSettled([productApi.get(id), reviewApi.list(id), recommendationApi.similar(id)])
      .then(([p,r,s]) => {
        if (p.status === "fulfilled") setProduct(p.value.data);
        if (r.status === "fulfilled") setReviews(r.value.data?.content || r.value.data || []);
        if (s.status === "fulfilled") setSimilar(s.value.data || []);
      });
  }, [id]);

  if (!product) return <Container sx={{ py: 5 }}>Loading...</Container>;
  const image = product.thumbnailUrl || product.images?.[0]?.imageUrl;

  return <Container maxWidth="xl" sx={{ py: 5 }}>
    {message && <Alert sx={{ mb: 2 }}>{message}</Alert>}
    <Grid container spacing={5}>
      <Grid size={{ xs: 12, md: 6 }}><img src={image} alt={product.name} style={{ width:"100%", maxHeight:520, objectFit:"contain", background:"#fff", borderRadius:12 }} /></Grid>
      <Grid size={{ xs: 12, md: 6 }}>
        <Typography variant="h3">{product.name}</Typography>
        <Stack direction="row" gap={1} alignItems="center" my={2}><Rating value={product.rating || 0} readOnly precision={0.5}/><span>{product.reviewCount || 0} reviews</span></Stack>
        <Typography variant="h4">₹{Number(product.discountPrice ?? product.price).toLocaleString("en-IN")}</Typography>
        <Typography color="text.secondary" sx={{ mt: 2 }}>{product.description || product.shortDescription}</Typography>
        <Stack direction="row" gap={2} sx={{ mt: 4 }}>
          <TextField label="Quantity" type="number" value={qty} onChange={e => setQty(Math.max(1, Number(e.target.value)))} sx={{ width:120 }} />
          <Button variant="contained" size="large" disabled={!product.stockQuantity}
            onClick={async()=>{ await addToCart(product.id, qty); setMessage("Added to cart"); }}>Add to cart</Button>
        </Stack>
      </Grid>
    </Grid>
    <Divider sx={{ my: 5 }} />
    <Typography variant="h4" mb={2}>Reviews</Typography>
    <Stack gap={2}>{reviews.map(r => <div key={r.id}><Rating value={r.rating} readOnly/><Typography variant="subtitle1">{r.title}</Typography><Typography>{r.comment}</Typography></div>)}</Stack>
    <Divider sx={{ my: 5 }} />
    <Typography variant="h4" mb={2}>Similar products</Typography>
    <Grid container spacing={3}>{similar.map(p => <Grid key={p.id} size={{xs:12,sm:6,md:3}}><ProductCard product={p}/></Grid>)}</Grid>
  </Container>;
}
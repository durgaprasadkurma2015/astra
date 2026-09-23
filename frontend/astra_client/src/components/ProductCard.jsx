import { Link } from "react-router-dom";
import {
  Card,
  CardContent,
  CardMedia,
  Typography,
  Button,
  Stack,
  Rating
} from "@mui/material";
import { useCart } from "../context/CartContext";


export default function ProductCard({ product }) {
  const { addToCart } = useCart();

  const image =
    product?.thumbnailUrl ||
    product?.images?.[0]?.imageUrl ||
    "https://via.placeholder.com/600x400?text=ASTRA";

  const price = Number(
    product?.discountPrice ??
    product?.price ??
    0
  );

  const originalPrice = Number(
    product?.price ?? 0
  );

  const hasDiscount =
    product?.discountPrice != null &&
    Number(product.discountPrice) <
      originalPrice;

  const rating = Number(
    product?.rating ?? 0
  );

  const reviewCount = Number(
    product?.reviewCount ?? 0
  );

  const stockQuantity = Number(
    product?.stockQuantity ?? 0
  );


  const handleAddToCart = async () => {
    try {
      await addToCart(product.id);
    } catch (error) {
      console.error(
        "Unable to add product to cart:",
        error
      );
    }
  };


  return (
    <Card
      sx={{
        height: "100%",
        display: "flex",
        flexDirection: "column",
        transition: "transform .2s ease, box-shadow .2s ease",
        "&:hover": {
          transform: "translateY(-4px)",
          boxShadow: 5
        }
      }}
    >

      <CardMedia
        component="img"
        height="210"
        image={image}
        alt={product?.name || "Product"}
        sx={{
          objectFit: "cover"
        }}
      />


      <CardContent
        sx={{
          display: "flex",
          flexDirection: "column",
          flex: 1
        }}
      >

        {/* Product name */}
        <Typography
          component={Link}
          to={`/products/${product.id}`}
          variant="h6"
          sx={{
            textDecoration: "none",
            color: "inherit",
            display: "block",
            fontWeight: 700,
            lineHeight: 1.3,
            minHeight: 58,
            "&:hover": {
              color: "primary.main"
            }
          }}
        >
          {product?.name || "Unnamed Product"}
        </Typography>


        {/* Description */}
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            minHeight: 42,
            mt: 1,
            display: "-webkit-box",
            WebkitLineClamp: 2,
            WebkitBoxOrient: "vertical",
            overflow: "hidden"
          }}
        >
          {product?.shortDescription ||
            product?.description ||
            "No description available."}
        </Typography>


        {/* Rating */}
        <Stack
          direction="row"
          alignItems="center"
          gap={1}
          mt={1.5}
        >
          <Rating
            value={rating}
            precision={0.5}
            size="small"
            readOnly
          />

          <Typography
            variant="caption"
            color="text.secondary"
          >
            ({reviewCount})
          </Typography>
        </Stack>


        {/* Price */}
        <Stack
          direction="row"
          alignItems="baseline"
          gap={1}
          mt={1.5}
        >
          <Typography
            variant="h6"
            fontWeight={800}
          >
            ₹{price.toLocaleString("en-IN")}
          </Typography>

          {hasDiscount && (
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{
                textDecoration: "line-through"
              }}
            >
              ₹{originalPrice.toLocaleString("en-IN")}
            </Typography>
          )}
        </Stack>


        {/* Stock */}
        {stockQuantity <= 0 ? (
          <Typography
            variant="caption"
            color="error"
            fontWeight={700}
            sx={{ mt: 1 }}
          >
            Out of stock
          </Typography>
        ) : stockQuantity <= 5 ? (
          <Typography
            variant="caption"
            color="warning.main"
            fontWeight={700}
            sx={{ mt: 1 }}
          >
            Only {stockQuantity} left
          </Typography>
        ) : (
          <Typography
            variant="caption"
            color="success.main"
            sx={{ mt: 1 }}
          >
            In stock
          </Typography>
        )}


        {/* Buttons */}
        <Stack
          direction={{
            xs: "column",
            sm: "row"
          }}
          spacing={1}
          sx={{
            mt: "auto",
            pt: 2
          }}
        >

          <Button
            fullWidth
            variant="outlined"
            component={Link}
            to={`/products/${product.id}`}
          >
            View
          </Button>

          <Button
            fullWidth
            variant="contained"
            disabled={stockQuantity <= 0}
            onClick={handleAddToCart}
          >
            Add to Cart
          </Button>

        </Stack>

      </CardContent>
    </Card>
  );
}

import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CardMedia,
  Container,
  Divider,
  Grid,
  IconButton,
  Rating,
  Stack,
  Typography
} from "@mui/material";

import {
  DeleteOutline,
  FavoriteBorder,
  ShoppingCartOutlined,
  ArrowBack
} from "@mui/icons-material";

import { wishlistApi } from "../api/api";
import { useCart } from "../context/CartContext";


export default function Wishlist() {
  const { addToCart } = useCart();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [movingId, setMovingId] = useState(null);
  const [deletingId, setDeletingId] = useState(null);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");


  const loadWishlist = async () => {
    setLoading(true);
    setError("");

    try {
      const response = await wishlistApi.get();

      const data = response.data;

      const wishlistItems =
        data?.items ||
        data?.content ||
        data ||
        [];

      setItems(
        Array.isArray(wishlistItems)
          ? wishlistItems
          : []
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to load your wishlist."
      );
    } finally {
      setLoading(false);
    }
  };


  useEffect(() => {
    loadWishlist();
  }, []);


  const showSuccess = (message) => {
    setSuccess(message);

    setTimeout(() => {
      setSuccess("");
    }, 3000);
  };


  const getProduct = (item) => {
    return item.product || item;
  };


  const getProductId = (item) => {
    const product = getProduct(item);

    return (
      product.id ||
      item.productId
    );
  };


  const getImage = (product) => {
    return (
      product.thumbnailUrl ||
      product.images?.find(
        image =>
          image.isPrimary ||
          image.primary
      )?.imageUrl ||
      product.images?.[0]?.imageUrl ||
      "https://via.placeholder.com/600x400?text=ASTRA"
    );
  };


  const getPrice = (product) => {
    return Number(
      product.discountPrice ??
      product.price ??
      0
    );
  };


  const getOriginalPrice = (product) => {
    return Number(
      product.price ?? 0
    );
  };


  const removeItem = async (item) => {
    const itemId = item.id;

    if (!itemId) {
      return;
    }

    setDeletingId(itemId);
    setError("");

    try {
      await wishlistApi.removeItem(
        itemId
      );

      setItems(previous =>
        previous.filter(
          wishlistItem =>
            wishlistItem.id !== itemId
        )
      );

      showSuccess(
        "Product removed from wishlist."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to remove product."
      );
    } finally {
      setDeletingId(null);
    }
  };


  const moveToCart = async (item) => {
    const itemId = item.id;
    const product = getProduct(item);
    const productId =
      getProductId(item);

    setMovingId(itemId);
    setError("");

    try {
      /*
       * Prefer the dedicated backend
       * wishlist -> cart endpoint.
       */
      if (itemId) {
        try {
          await wishlistApi.moveToCart(
            itemId
          );

          setItems(previous =>
            previous.filter(
              wishlistItem =>
                wishlistItem.id !==
                itemId
            )
          );

          showSuccess(
            "Product moved to cart."
          );

          return;
        } catch {
          /*
           * Fallback to cart API if the
           * dedicated endpoint isn't
           * available.
           */
        }
      }

      if (!productId) {
        throw new Error(
          "Product ID is missing."
        );
      }

      await addToCart(
        productId,
        1
      );

      await wishlistApi.removeProduct(
        productId
      );

      setItems(previous =>
        previous.filter(
          wishlistItem =>
            getProductId(
              wishlistItem
            ) !== productId
        )
      );

      showSuccess(
        "Product added to cart."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        err?.message ||
        "Unable to move product to cart."
      );
    } finally {
      setMovingId(null);
    }
  };


  if (loading) {
    return (
      <Container
        maxWidth="xl"
        sx={{
          py: 10
        }}
      >
        <Stack
          alignItems="center"
          spacing={2}
        >
          <FavoriteBorder
            sx={{
              fontSize: 48,
              color: "text.disabled"
            }}
          />

          <Typography
            color="text.secondary"
          >
            Loading your wishlist...
          </Typography>
        </Stack>
      </Container>
    );
  }


  return (
    <Container
      maxWidth="xl"
      sx={{
        py: {
          xs: 3,
          md: 5
        }
      }}
    >

      {/* Header */}
      <Stack
        direction={{
          xs: "column",
          sm: "row"
        }}
        justifyContent="space-between"
        alignItems={{
          xs: "flex-start",
          sm: "center"
        }}
        spacing={2}
        mb={4}
      >
        <Box>
          <Stack
            direction="row"
            spacing={1}
            alignItems="center"
          >
            <FavoriteBorder
              color="primary"
            />

            <Typography
              variant="h4"
              fontWeight={900}
            >
              My Wishlist
            </Typography>
          </Stack>

          <Typography
            color="text.secondary"
            sx={{
              mt: 0.75
            }}
          >
            {items.length === 0
              ? "You haven't saved any products yet."
              : `${items.length} ${
                  items.length === 1
                    ? "product"
                    : "products"
                } saved`}
          </Typography>
        </Box>

        {items.length > 0 && (
          <Button
            component={Link}
            to="/products"
            variant="outlined"
            startIcon={
              <ArrowBack />
            }
          >
            Continue Shopping
          </Button>
        )}
      </Stack>


      {/* Alerts */}
      {error && (
        <Alert
          severity="error"
          sx={{
            mb: 3
          }}
          onClose={() =>
            setError("")
          }
        >
          {error}
        </Alert>
      )}

      {success && (
        <Alert
          severity="success"
          sx={{
            mb: 3
          }}
          onClose={() =>
            setSuccess("")
          }
        >
          {success}
        </Alert>
      )}


      {/* Empty state */}
      {items.length === 0 ? (
        <Card>
          <CardContent
            sx={{
              py: {
                xs: 7,
                md: 10
              },
              textAlign: "center"
            }}
          >
            <FavoriteBorder
              sx={{
                fontSize: 80,
                color: "text.disabled",
                mb: 2
              }}
            />

            <Typography
              variant="h5"
              fontWeight={800}
            >
              Your wishlist is empty
            </Typography>

            <Typography
              color="text.secondary"
              sx={{
                mt: 1,
                mb: 3
              }}
            >
              Save products you love
              and come back to them later.
            </Typography>

            <Button
              component={Link}
              to="/products"
              variant="contained"
              size="large"
            >
              Explore Products
            </Button>
          </CardContent>
        </Card>
      ) : (

        <Grid
          container
          spacing={3}
        >
          {items.map(item => {
            const product =
              getProduct(item);

            const productId =
              getProductId(item);

            const image =
              getImage(product);

            const price =
              getPrice(product);

            const originalPrice =
              getOriginalPrice(product);

            const hasDiscount =
              product.discountPrice !=
                null &&
              originalPrice >
                price;

            const isDeleting =
              deletingId === item.id;

            const isMoving =
              movingId === item.id;

            return (
              <Grid
                key={
                  item.id ||
                  productId
                }
                size={{
                  xs: 12,
                  sm: 6,
                  md: 4,
                  lg: 3
                }}
              >
                <Card
                  sx={{
                    height: "100%",
                    display: "flex",
                    flexDirection:
                      "column",
                    overflow: "hidden",
                    transition:
                      "transform .2s ease, box-shadow .2s ease",
                    "&:hover": {
                      transform:
                        "translateY(-3px)",
                      boxShadow: 5
                    }
                  }}
                >

                  {/* Product image */}
                  <Box
                    sx={{
                      position:
                        "relative"
                    }}
                  >
                    <CardMedia
                      component="img"
                      image={image}
                      alt={
                        product.name ||
                        "Product"
                      }
                      sx={{
                        height: {
                          xs: 220,
                          md: 240
                        },
                        objectFit:
                          "cover"
                      }}
                    />

                    {hasDiscount && (
                      <Box
                        sx={{
                          position:
                            "absolute",
                          top: 12,
                          left: 12,
                          bgcolor:
                            "error.main",
                          color:
                            "error.contrastText",
                          px: 1,
                          py: 0.5,
                          borderRadius: 1,
                          fontWeight: 800,
                          fontSize:
                            "0.75rem"
                        }}
                      >
                        SALE
                      </Box>
                    )}

                    <IconButton
                      onClick={() =>
                        removeItem(
                          item
                        )
                      }
                      disabled={
                        isDeleting ||
                        isMoving
                      }
                      sx={{
                        position:
                          "absolute",
                        top: 8,
                        right: 8,
                        bgcolor:
                          "background.paper",
                        boxShadow: 2,
                        "&:hover": {
                          bgcolor:
                            "background.paper"
                        }
                      }}
                      color="error"
                      aria-label="Remove from wishlist"
                    >
                      <DeleteOutline />
                    </IconButton>
                  </Box>


                  {/* Product details */}
                  <CardContent
                    sx={{
                      display:
                        "flex",
                      flexDirection:
                        "column",
                      flex: 1
                    }}
                  >

                    <Typography
                      component={Link}
                      to={
                        productId
                          ? `/products/${productId}`
                          : "/products"
                      }
                      variant="h6"
                      fontWeight={800}
                      sx={{
                        color:
                          "text.primary",
                        textDecoration:
                          "none",
                        display:
                          "block",
                        "&:hover": {
                          color:
                            "primary.main"
                        }
                      }}
                    >
                      {product.name ||
                        "Product"}
                    </Typography>


                    {product.shortDescription && (
                      <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                          mt: 0.75,
                          minHeight: 40,
                          display:
                            "-webkit-box",
                          WebkitLineClamp: 2,
                          WebkitBoxOrient:
                            "vertical",
                          overflow:
                            "hidden"
                        }}
                      >
                        {
                          product.shortDescription
                        }
                      </Typography>
                    )}


                    {/* Rating */}
                    <Stack
                      direction="row"
                      spacing={1}
                      alignItems="center"
                      sx={{
                        mt: 1.5
                      }}
                    >
                      <Rating
                        value={
                          Number(
                            product.rating ||
                              0
                          )
                        }
                        precision={0.5}
                        size="small"
                        readOnly
                      />

                      <Typography
                        variant="caption"
                        color="text.secondary"
                      >
                        (
                        {
                          product.reviewCount ||
                          0
                        }
                        )
                      </Typography>
                    </Stack>


                    {/* Price */}
                    <Stack
                      direction="row"
                      spacing={1}
                      alignItems="baseline"
                      sx={{
                        mt: 1.5
                      }}
                    >
                      <Typography
                        variant="h6"
                        fontWeight={900}
                      >
                        ₹
                        {price.toLocaleString(
                          "en-IN"
                        )}
                      </Typography>

                      {hasDiscount && (
                        <Typography
                          variant="body2"
                          color="text.secondary"
                          sx={{
                            textDecoration:
                              "line-through"
                          }}
                        >
                          ₹
                          {originalPrice.toLocaleString(
                            "en-IN"
                          )}
                        </Typography>
                      )}
                    </Stack>


                    {/* Stock */}
                    <Typography
                      variant="caption"
                      sx={{
                        mt: 0.5,
                        color:
                          product.stockQuantity >
                          0
                            ? "success.main"
                            : "error.main",
                        fontWeight: 700
                      }}
                    >
                      {product.stockQuantity >
                      0
                        ? "In stock"
                        : "Out of stock"}
                    </Typography>


                    <Box
                      sx={{
                        flex: 1
                      }}
                    />


                    <Divider
                      sx={{
                        my: 2
                      }}
                    />


                    {/* Actions */}
                    <Stack
                      spacing={1}
                    >
                      <Button
                        fullWidth
                        variant="contained"
                        startIcon={
                          <ShoppingCartOutlined />
                        }
                        disabled={
                          !product.stockQuantity ||
                          isMoving ||
                          isDeleting
                        }
                        onClick={() =>
                          moveToCart(
                            item
                          )
                        }
                      >
                        {isMoving
                          ? "Moving..."
                          : product.stockQuantity
                            ? "Move to Cart"
                            : "Out of Stock"}
                      </Button>

                      <Button
                        fullWidth
                        color="error"
                        variant="text"
                        startIcon={
                          <DeleteOutline />
                        }
                        disabled={
                          isDeleting ||
                          isMoving
                        }
                        onClick={() =>
                          removeItem(
                            item
                          )
                        }
                      >
                        {isDeleting
                          ? "Removing..."
                          : "Remove"}
                      </Button>
                    </Stack>

                  </CardContent>
                </Card>
              </Grid>
            );
          })}
        </Grid>
      )}

    </Container>
  );
}

import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
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
  Stack,
  TextField,
  Typography
} from "@mui/material";

import {
  Add,
  ArrowBack,
  DeleteOutline,
  Remove,
  ShoppingCartOutlined,
  ArrowForward
} from "@mui/icons-material";

import { useCart } from "../context/CartContext";


export default function Cart() {
  const {
    cart,
    refresh,
    updateItem,
    removeItem,
    clearCart
  } = useCart();

  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);
  const [removingId, setRemovingId] = useState(null);
  const [clearing, setClearing] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");


  /*
   * Load cart when this page opens.
   */
  useEffect(() => {
    const load = async () => {
      setLoading(true);

      try {
        await refresh();
      } catch (err) {
        setError(
          err?.response?.data?.message ||
          "Unable to load your cart."
        );
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);


  const showSuccess = (message) => {
    setSuccess(message);

    setTimeout(() => {
      setSuccess("");
    }, 2500);
  };


  const getItems = () => {
    if (!cart) {
      return [];
    }

    return (
      cart.items ||
      cart.cartItems ||
      cart.content ||
      []
    );
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


  const getUnitPrice = (item) => {
    const product = getProduct(item);

    return Number(
      item.unitPrice ??
      item.price ??
      product.discountPrice ??
      product.price ??
      0
    );
  };


  const getOriginalPrice = (item) => {
    const product = getProduct(item);

    return Number(
      product.price ??
      getUnitPrice(item)
    );
  };


  const getQuantity = (item) => {
    return Number(
      item.quantity || 1
    );
  };


  const getLineTotal = (item) => {
    if (
      item.subtotal != null
    ) {
      return Number(
        item.subtotal
      );
    }

    if (
      item.totalPrice != null
    ) {
      return Number(
        item.totalPrice
      );
    }

    return (
      getUnitPrice(item) *
      getQuantity(item)
    );
  };


  const formatPrice = (value) => {
    return Number(
      value || 0
    ).toLocaleString(
      "en-IN"
    );
  };


  /*
   * Change item quantity.
   */
  const changeQuantity = async (
    item,
    quantity
  ) => {
    const itemId = item.id;

    if (!itemId) {
      return;
    }

    if (quantity < 1) {
      return;
    }

    const product =
      getProduct(item);

    const stock =
      Number(
        product.stockQuantity ??
        item.stockQuantity ??
        999999
      );

    if (
      stock > 0 &&
      quantity > stock
    ) {
      setError(
        `Only ${stock} item${
          stock === 1
            ? ""
            : "s"
        } available in stock.`
      );

      return;
    }

    setError("");
    setUpdatingId(itemId);

    try {
      await updateItem(
        itemId,
        quantity
      );

      showSuccess(
        "Cart updated."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to update cart."
      );
    } finally {
      setUpdatingId(null);
    }
  };


  /*
   * Remove item.
   */
  const handleRemove = async (
    item
  ) => {
    if (!item.id) {
      return;
    }

    setRemovingId(item.id);
    setError("");

    try {
      await removeItem(
        item.id
      );

      showSuccess(
        "Item removed from cart."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to remove item."
      );
    } finally {
      setRemovingId(null);
    }
  };


  /*
   * Clear entire cart.
   */
  const handleClear = async () => {
    if (!getItems().length) {
      return;
    }

    const confirmed =
      window.confirm(
        "Are you sure you want to remove all items from your cart?"
      );

    if (!confirmed) {
      return;
    }

    setClearing(true);
    setError("");

    try {
      await clearCart();

      showSuccess(
        "Cart cleared successfully."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to clear cart."
      );
    } finally {
      setClearing(false);
    }
  };


  const items = getItems();


  /*
   * Prefer backend totals when available.
   * Otherwise calculate them locally.
   */
  const calculatedSubtotal =
    items.reduce(
      (sum, item) =>
        sum +
        getLineTotal(item),
      0
    );


  const subtotal = Number(
    cart?.subtotal ??
    cart?.subTotal ??
    calculatedSubtotal
  );


  const discount = Number(
    cart?.discount ??
    cart?.discountAmount ??
    0
  );


  const shipping = Number(
    cart?.shipping ??
    cart?.shippingFee ??
    cart?.deliveryFee ??
    0
  );


  const tax = Number(
    cart?.tax ??
    cart?.taxAmount ??
    0
  );


  const total = Number(
    cart?.total ??
    cart?.totalAmount ??
    subtotal -
      discount +
      shipping +
      tax
  );


  /*
   * Loading.
   */
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
          <ShoppingCartOutlined
            sx={{
              fontSize: 50,
              color:
                "text.disabled"
            }}
          />

          <Typography
            color="text.secondary"
          >
            Loading your cart...
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
            <ShoppingCartOutlined
              color="primary"
            />

            <Typography
              variant="h4"
              fontWeight={900}
            >
              Shopping Cart
            </Typography>
          </Stack>

          <Typography
            color="text.secondary"
            sx={{
              mt: 0.75
            }}
          >
            {items.length === 0
              ? "Your cart is empty."
              : `${cart?.totalItems ??
                  items.reduce(
                    (sum, item) =>
                      sum +
                      getQuantity(
                        item
                      ),
                    0
                  )} items in your cart`}
          </Typography>
        </Box>


        {items.length > 0 && (
          <Button
            color="error"
            variant="outlined"
            startIcon={
              <DeleteOutline />
            }
            onClick={
              handleClear
            }
            disabled={clearing}
          >
            {clearing
              ? "Clearing..."
              : "Clear Cart"}
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


      {/* Empty cart */}
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

            <ShoppingCartOutlined
              sx={{
                fontSize: 90,
                color:
                  "text.disabled",
                mb: 2
              }}
            />

            <Typography
              variant="h5"
              fontWeight={800}
            >
              Your cart is empty
            </Typography>

            <Typography
              color="text.secondary"
              sx={{
                mt: 1,
                mb: 3
              }}
            >
              Looks like you haven't
              added anything yet.
            </Typography>

            <Button
              component={Link}
              to="/products"
              variant="contained"
              size="large"
              startIcon={
                <ArrowBack />
              }
            >
              Continue Shopping
            </Button>

          </CardContent>
        </Card>
      ) : (

        <Grid
          container
          spacing={3}
          alignItems="flex-start"
        >

          {/* Cart items */}
          <Grid
            size={{
              xs: 12,
              lg: 8
            }}
          >

            <Stack spacing={2}>

              {items.map(item => {
                const product =
                  getProduct(item);

                const itemId =
                  item.id;

                const productId =
                  getProductId(item);

                const quantity =
                  getQuantity(item);

                const unitPrice =
                  getUnitPrice(item);

                const originalPrice =
                  getOriginalPrice(item);

                const lineTotal =
                  getLineTotal(item);

                const image =
                  getImage(product);

                const stock =
                  Number(
                    product.stockQuantity ??
                    item.stockQuantity ??
                    999999
                  );

                const updating =
                  updatingId ===
                  itemId;

                const removing =
                  removingId ===
                  itemId;

                return (
                  <Card
                    key={
                      itemId ||
                      productId
                    }
                  >
                    <CardContent
                      sx={{
                        p: {
                          xs: 2,
                          md: 2.5
                        }
                      }}
                    >

                      <Grid
                        container
                        spacing={2}
                        alignItems="center"
                      >

                        {/* Image */}
                        <Grid
                          size={{
                            xs: 4,
                            sm: 3,
                            md: 2.5
                          }}
                        >
                          <CardMedia
                            component="img"
                            src={image}
                            alt={
                              product.name ||
                              "Product"
                            }
                            sx={{
                              width: "100%",
                              aspectRatio:
                                "1 / 1",
                              objectFit:
                                "cover",
                              borderRadius: 2
                            }}
                          />
                        </Grid>


                        {/* Product info */}
                        <Grid
                          size={{
                            xs: 8,
                            sm: 9,
                            md: 4.5
                          }}
                        >

                          <Typography
                            component={
                              Link
                            }
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
                                mt: 0.5,
                                display:
                                  {
                                    xs:
                                      "none",
                                    sm:
                                      "-webkit-box"
                                  },
                                WebkitLineClamp:
                                  2,
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


                          <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                              mt: 1
                            }}
                          >
                            Unit price: ₹
                            {formatPrice(
                              unitPrice
                            )}
                          </Typography>


                          {stock !==
                            999999 && (
                            <Typography
                              variant="caption"
                              sx={{
                                display:
                                  "block",
                                mt: 0.5,
                                color:
                                  stock >
                                  0
                                    ? "success.main"
                                    : "error.main",
                                fontWeight: 700
                              }}
                            >
                              {stock >
                              0
                                ? `${stock} available`
                                : "Out of stock"}
                            </Typography>
                          )}

                        </Grid>


                        {/* Quantity */}
                        <Grid
                          size={{
                            xs: 7,
                            sm: 5,
                            md: 2.5
                          }}
                        >

                          <Typography
                            variant="caption"
                            color="text.secondary"
                            sx={{
                              display:
                                "block",
                              mb: 0.75
                            }}
                          >
                            Quantity
                          </Typography>

                          <Stack
                            direction="row"
                            alignItems="center"
                            sx={{
                              width:
                                "fit-content",
                              border:
                                "1px solid",
                              borderColor:
                                "divider",
                              borderRadius: 1.5
                            }}
                          >

                            <IconButton
                              size="small"
                              disabled={
                                updating ||
                                quantity <=
                                  1
                              }
                              onClick={() =>
                                changeQuantity(
                                  item,
                                  quantity -
                                    1
                                )
                              }
                            >
                              <Remove />
                            </IconButton>


                            <TextField
                              value={
                                quantity
                              }
                              onChange={event => {
                                const value =
                                  Number(
                                    event
                                      .target
                                      .value
                                  );

                                if (
                                  Number.isInteger(
                                    value
                                  ) &&
                                  value >=
                                    1
                                ) {
                                  changeQuantity(
                                    item,
                                    value
                                  );
                                }
                              }}
                              size="small"
                              inputProps={{
                                min: 1,
                                max:
                                  stock !==
                                  999999
                                    ? stock
                                    : undefined,
                                style: {
                                  textAlign:
                                    "center",
                                  width: 45
                                }
                              }}
                              sx={{
                                "& fieldset": {
                                  border: "none"
                                }
                              }}
                            />


                            <IconButton
                              size="small"
                              disabled={
                                updating ||
                                (
                                  stock !==
                                    999999 &&
                                  quantity >=
                                    stock
                                )
                              }
                              onClick={() =>
                                changeQuantity(
                                  item,
                                  quantity +
                                    1
                                )
                              }
                            >
                              <Add />
                            </IconButton>

                          </Stack>

                        </Grid>


                        {/* Price / delete */}
                        <Grid
                          size={{
                            xs: 5,
                            sm: 4,
                            md: 2.5
                          }}
                        >

                          <Stack
                            alignItems={{
                              xs:
                                "flex-end",
                              md:
                                "flex-end"
                            }}
                            spacing={1}
                          >

                            <Typography
                              variant="h6"
                              fontWeight={900}
                            >
                              ₹
                              {formatPrice(
                                lineTotal
                              )}
                            </Typography>


                            {originalPrice >
                              unitPrice && (
                              <Typography
                                variant="caption"
                                color="text.secondary"
                                sx={{
                                  textDecoration:
                                    "line-through"
                                }}
                              >
                                ₹
                                {formatPrice(
                                  originalPrice *
                                    quantity
                                )}
                              </Typography>
                            )}


                            <Button
                              size="small"
                              color="error"
                              startIcon={
                                <DeleteOutline />
                              }
                              disabled={
                                removing ||
                                updating
                              }
                              onClick={() =>
                                handleRemove(
                                  item
                                )
                              }
                            >
                              {removing
                                ? "Removing..."
                                : "Remove"}
                            </Button>

                          </Stack>

                        </Grid>

                      </Grid>

                    </CardContent>
                  </Card>
                );
              })}

            </Stack>


            {/* Continue shopping */}
            <Button
              component={Link}
              to="/products"
              variant="text"
              startIcon={
                <ArrowBack />
              }
              sx={{
                mt: 2
              }}
            >
              Continue Shopping
            </Button>

          </Grid>


          {/* Summary */}
          <Grid
            size={{
              xs: 12,
              lg: 4
            }}
          >

            <Card
              sx={{
                position: {
                  lg: "sticky"
                },
                top: {
                  lg: 90
                }
              }}
            >
              <CardContent
                sx={{
                  p: 3
                }}
              >

                <Typography
                  variant="h5"
                  fontWeight={900}
                  mb={3}
                >
                  Order Summary
                </Typography>


                <Stack
                  spacing={1.75}
                >

                  <Stack
                    direction="row"
                    justifyContent="space-between"
                  >
                    <Typography
                      color="text.secondary"
                    >
                      Subtotal
                    </Typography>

                    <Typography
                      fontWeight={700}
                    >
                      ₹
                      {formatPrice(
                        subtotal
                      )}
                    </Typography>
                  </Stack>


                  {discount > 0 && (
                    <Stack
                      direction="row"
                      justifyContent="space-between"
                    >
                      <Typography
                        color="text.secondary"
                      >
                        Discount
                      </Typography>

                      <Typography
                        color="success.main"
                        fontWeight={700}
                      >
                        -₹
                        {formatPrice(
                          discount
                        )}
                      </Typography>
                    </Stack>
                  )}


                  <Stack
                    direction="row"
                    justifyContent="space-between"
                  >
                    <Typography
                      color="text.secondary"
                    >
                      Shipping
                    </Typography>

                    <Typography
                      fontWeight={700}
                    >
                      {shipping ===
                      0
                        ? "FREE"
                        : `₹${formatPrice(
                            shipping
                          )}`}
                    </Typography>
                  </Stack>


                  {tax > 0 && (
                    <Stack
                      direction="row"
                      justifyContent="space-between"
                    >
                      <Typography
                        color="text.secondary"
                      >
                        Tax
                      </Typography>

                      <Typography
                        fontWeight={700}
                      >
                        ₹
                        {formatPrice(
                          tax
                        )}
                      </Typography>
                    </Stack>
                  )}

                </Stack>


                <Divider
                  sx={{
                    my: 2.5
                  }}
                />


                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                >
                  <Typography
                    variant="h6"
                    fontWeight={900}
                  >
                    Total
                  </Typography>

                  <Typography
                    variant="h5"
                    fontWeight={900}
                    color="primary"
                  >
                    ₹
                    {formatPrice(
                      total
                    )}
                  </Typography>
                </Stack>


                <Button
                  fullWidth
                  variant="contained"
                  size="large"
                  endIcon={
                    <ArrowForward />
                  }
                  sx={{
                    mt: 3,
                    py: 1.5
                  }}
                  onClick={() =>
                    navigate(
                      "/checkout"
                    )
                  }
                  disabled={
                    !items.length
                  }
                >
                  Proceed to Checkout
                </Button>


                <Typography
                  variant="caption"
                  color="text.secondary"
                  textAlign="center"
                  sx={{
                    display:
                      "block",
                    mt: 2
                  }}
                >
                  Secure checkout
                  powered by ASTRA
                </Typography>

              </CardContent>
            </Card>

          </Grid>

        </Grid>
      )}

    </Container>
  );
}

import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Divider,
  FormControl,
  FormControlLabel,
  Grid,
  Radio,
  RadioGroup,
  Stack,
  TextField,
  Typography
} from "@mui/material";

import {
  Add,
  ArrowBack,
  ArrowForward,
  LocationOn,
  Payment,
  ShoppingCartOutlined
} from "@mui/icons-material";

import { useCart } from "../context/CartContext";
import { addressApi, orderApi, paymentApi } from "../api/api";


export default function Checkout() {
  const navigate = useNavigate();

  const {
    cart,
    refresh
  } = useCart();

  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState("");

  const [loading, setLoading] = useState(true);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [paymentLoading, setPaymentLoading] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [showAddressForm, setShowAddressForm] =
    useState(false);

  const [addressForm, setAddressForm] = useState({
    name: "",
    phone: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    postalCode: "",
    country: "India"
  });


  const items = useMemo(() => {
    if (!cart) {
      return [];
    }

    return (
      cart.items ||
      cart.cartItems ||
      cart.content ||
      []
    );
  }, [cart]);


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


  const getQuantity = (item) => {
    return Number(
      item.quantity || 1
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


  const getLineTotal = (item) => {
    if (item.subtotal != null) {
      return Number(item.subtotal);
    }

    if (item.totalPrice != null) {
      return Number(item.totalPrice);
    }

    return (
      getUnitPrice(item) *
      getQuantity(item)
    );
  };


  const formatPrice = (value) =>
    Number(
      value || 0
    ).toLocaleString(
      "en-IN"
    );


  /*
   * Load addresses and cart.
   */
  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError("");

      try {
        await refresh();

        const response =
          await addressApi.list();

        const data =
          response.data;

        const list =
          data?.addresses ||
          data?.items ||
          data?.content ||
          data ||
          [];

        const normalized =
          Array.isArray(list)
            ? list
            : [];

        setAddresses(
          normalized
        );

        /*
         * Automatically select
         * the default address.
         */
        const defaultAddress =
          normalized.find(
            address =>
              address.isDefault ||
              address.defaultAddress ||
              address.default
          );

        if (defaultAddress) {
          setSelectedAddress(
            String(
              defaultAddress.id
            )
          );
        } else if (
          normalized.length > 0
        ) {
          setSelectedAddress(
            String(
              normalized[0].id
            )
          );
        }

      } catch (err) {
        setError(
          err?.response?.data?.message ||
          "Unable to load checkout information."
        );
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);


  /*
   * Handle address form fields.
   */
  const updateAddressField = (
    event
  ) => {
    const {
      name,
      value
    } = event.target;

    setAddressForm(
      previous => ({
        ...previous,
        [name]: value
      })
    );
  };


  /*
   * Add a new address.
   */
  const handleAddAddress = async (
    event
  ) => {
    event.preventDefault();

    setError("");

    if (
      !addressForm.name ||
      !addressForm.phone ||
      !addressForm.addressLine1 ||
      !addressForm.city ||
      !addressForm.state ||
      !addressForm.postalCode
    ) {
      setError(
        "Please fill in all required address fields."
      );

      return;
    }

    try {
      const response =
        await addressApi.create(
          addressForm
        );

      const created =
        response.data?.address ||
        response.data;

      /*
       * Refresh from backend to
       * ensure we have the actual
       * address ID.
       */
      const addressesResponse =
        await addressApi.list();

      const data =
        addressesResponse.data;

      const list =
        data?.addresses ||
        data?.items ||
        data?.content ||
        data ||
        [];

      const normalized =
        Array.isArray(list)
          ? list
          : [];

      setAddresses(
        normalized
      );

      if (created?.id) {
        setSelectedAddress(
          String(created.id)
        );
      } else if (
        normalized.length
      ) {
        const newest =
          normalized[
            normalized.length - 1
          ];

        setSelectedAddress(
          String(newest.id)
        );
      }

      setAddressForm({
        name: "",
        phone: "",
        addressLine1: "",
        addressLine2: "",
        city: "",
        state: "",
        postalCode: "",
        country: "India"
      });

      setShowAddressForm(
        false
      );

      setSuccess(
        "Address added successfully."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to add address."
      );
    }
  };


  /*
   * Calculate totals.
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
   * Create order.
   *
   * The exact backend DTO can differ,
   * so we send common field names
   * supported by the API structure.
   */
  const createOrder = async () => {
    if (!selectedAddress) {
      setError(
        "Please select a delivery address."
      );

      return null;
    }

    if (!items.length) {
      setError(
        "Your cart is empty."
      );

      return null;
    }

    const orderData = {
      addressId:
        Number(selectedAddress),

      shippingAddressId:
        Number(selectedAddress),

      items: items.map(
        item => ({
          productId:
            getProductId(item),
          quantity:
            getQuantity(item)
        })
      ),

      subtotal,
      discount,
      shipping,
      tax,
      total
    };

    const response =
      await orderApi.create(
        orderData
      );

    return (
      response.data?.order ||
      response.data
    );
  };


  /*
   * Place order.
   */
  const handlePlaceOrder = async () => {
    setPlacingOrder(true);
    setError("");
    setSuccess("");

    try {
      const order =
        await createOrder();

      if (!order) {
        throw new Error(
          "Order creation failed."
        );
      }

      const orderId =
        order.id ||
        order.orderId;

      if (!orderId) {
        /*
         * If backend already returns
         * a completed order without
         * payment, go to orders.
         */
        navigate("/orders");
        return;
      }

      /*
       * Start payment.
       */
      setPaymentLoading(true);

      try {
        const paymentResponse =
          await paymentApi.initiate(
            orderId
          );

        const payment =
          paymentResponse.data
            ?.payment ||
          paymentResponse.data;

        /*
         * Some backends return a
         * payment URL.
         */
        const paymentUrl =
          payment?.paymentUrl ||
          payment?.checkoutUrl ||
          payment?.redirectUrl;

        if (paymentUrl) {
          window.location.href =
            paymentUrl;

          return;
        }

        /*
         * If no external payment
         * URL is returned, open
         * the order details page.
         */
        navigate(
          `/orders/${orderId}`
        );

      } catch (paymentError) {
        /*
         * Order exists even if
         * payment initiation fails.
         */
        setError(
          paymentError?.response
            ?.data?.message ||
          "Order was created, but payment could not be started."
        );

        navigate(
          `/orders/${orderId}`
        );
      }

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        err?.message ||
        "Unable to place your order."
      );
    } finally {
      setPlacingOrder(false);
      setPaymentLoading(false);
    }
  };


  /*
   * Loading state.
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
          <CircularProgress />

          <Typography
            color="text.secondary"
          >
            Preparing checkout...
          </Typography>
        </Stack>
      </Container>
    );
  }


  /*
   * Empty cart.
   */
  if (!items.length) {
    return (
      <Container
        maxWidth="md"
        sx={{
          py: 10
        }}
      >
        <Card>
          <CardContent
            sx={{
              py: 8,
              textAlign: "center"
            }}
          >
            <ShoppingCartOutlined
              sx={{
                fontSize: 80,
                color:
                  "text.disabled",
                mb: 2
              }}
            />

            <Typography
              variant="h5"
              fontWeight={900}
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
              Add some products before
              continuing to checkout.
            </Typography>

            <Button
              component={Link}
              to="/products"
              variant="contained"
            >
              Continue Shopping
            </Button>
          </CardContent>
        </Card>
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
        direction="row"
        alignItems="center"
        spacing={1}
        mb={4}
      >
        <Payment color="primary" />

        <Box>
          <Typography
            variant="h4"
            fontWeight={900}
          >
            Checkout
          </Typography>

          <Typography
            color="text.secondary"
          >
            Complete your order
            securely.
          </Typography>
        </Box>
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


      <Grid
        container
        spacing={3}
        alignItems="flex-start"
      >

        {/* Left */}
        <Grid
          size={{
            xs: 12,
            lg: 8
          }}
        >

          {/* Address */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                mb={2}
              >
                <Stack
                  direction="row"
                  spacing={1}
                  alignItems="center"
                >
                  <LocationOn
                    color="primary"
                  />

                  <Typography
                    variant="h6"
                    fontWeight={900}
                  >
                    Delivery Address
                  </Typography>
                </Stack>

                <Button
                  size="small"
                  startIcon={<Add />}
                  onClick={() =>
                    setShowAddressForm(
                      value => !value
                    )
                  }
                >
                  Add Address
                </Button>
              </Stack>


              {/* Existing addresses */}
              {addresses.length > 0 ? (
                <FormControl
                  fullWidth
                >
                  <RadioGroup
                    value={
                      selectedAddress
                    }
                    onChange={event =>
                      setSelectedAddress(
                        event.target
                          .value
                      )
                    }
                  >

                    <Stack
                      spacing={1.5}
                    >
                      {addresses.map(
                        address => (
                          <Card
                            key={
                              address.id
                            }
                            variant="outlined"
                            sx={{
                              border:
                                selectedAddress ===
                                String(
                                  address.id
                                )
                                  ? "2px solid"
                                  : "1px solid",
                              borderColor:
                                selectedAddress ===
                                String(
                                  address.id
                                )
                                  ? "primary.main"
                                  : "divider",
                              bgcolor:
                                selectedAddress ===
                                String(
                                  address.id
                                )
                                  ? "action.selected"
                                  : "transparent"
                            }}
                          >
                            <FormControlLabel
                              value={String(
                                address.id
                              )}
                              control={
                                <Radio />
                              }
                              sx={{
                                m: 0,
                                p: 1.5,
                                width:
                                  "100%",
                                alignItems:
                                  "flex-start"
                              }}
                              label={
                                <Box
                                  sx={{
                                    ml: 1
                                  }}
                                >
                                  <Typography
                                    fontWeight={
                                      800
                                    }
                                  >
                                    {address.name ||
                                      address.fullName ||
                                      "Delivery Address"}
                                  </Typography>

                                  <Typography
                                    variant="body2"
                                    color="text.secondary"
                                  >
                                    {address.addressLine1 ||
                                      address.line1 ||
                                      address.address ||
                                      ""}
                                  </Typography>

                                  {address.addressLine2 && (
                                    <Typography
                                      variant="body2"
                                      color="text.secondary"
                                    >
                                      {
                                        address.addressLine2
                                      }
                                    </Typography>
                                  )}

                                  <Typography
                                    variant="body2"
                                    color="text.secondary"
                                  >
                                    {
                                      address.city
                                    }
                                    ,{" "}
                                    {
                                      address.state
                                    }{" "}
                                    {
                                      address.postalCode ||
                                      address.zipCode
                                    }
                                  </Typography>

                                  {address.phone && (
                                    <Typography
                                      variant="body2"
                                      color="text.secondary"
                                      sx={{
                                        mt: 0.5
                                      }}
                                    >
                                      Phone:{" "}
                                      {
                                        address.phone
                                      }
                                    </Typography>
                                  )}

                                  {(address.isDefault ||
                                    address.defaultAddress ||
                                    address.default) && (
                                    <Typography
                                      variant="caption"
                                      color="primary"
                                      fontWeight={
                                        800
                                      }
                                    >
                                      Default address
                                    </Typography>
                                  )}
                                </Box>
                              }
                            />
                          </Card>
                        )
                      )}
                    </Stack>

                  </RadioGroup>
                </FormControl>
              ) : (
                <Alert severity="info">
                  You don't have a saved
                  address. Add one below.
                </Alert>
              )}


              {/* Add address form */}
              {showAddressForm && (
                <Box
                  component="form"
                  onSubmit={
                    handleAddAddress
                  }
                  sx={{
                    mt: 3,
                    p: 2,
                    borderRadius: 2,
                    bgcolor:
                      "action.hover"
                  }}
                >
                  <Typography
                    variant="subtitle1"
                    fontWeight={800}
                    mb={2}
                  >
                    New Address
                  </Typography>

                  <Grid
                    container
                    spacing={2}
                  >

                    <Grid
                      size={{
                        xs: 12,
                        sm: 6
                      }}
                    >
                      <TextField
                        fullWidth
                        required
                        label="Full Name"
                        name="name"
                        value={
                          addressForm.name
                        }
                        onChange={
                          updateAddressField
                        }
                      />
                    </Grid>

                    <Grid
                      size={{
                        xs: 12,
                        sm: 6
                      }}
                    >
                      <TextField
                        fullWidth
                        required
                        label="Phone"
                        name="phone"
                        value={
                          addressForm.phone
                        }
                        onChange={
                          updateAddressField
                        }
                      />
                    </Grid>

                    <Grid
                      size={12}
                    >
                      <TextField
                        fullWidth
                        required
                        label="Address Line 1"
                        name="addressLine1"
                        value={
                          addressForm.addressLine1
                        }
                        onChange={
                          updateAddressField
                        }
                      />
                    </Grid>

                    <Grid
                      size={12}
                    >
                      <TextField
                        fullWidth
                        label="Address Line 2"
                        name="addressLine2"
                        value={
                          addressForm.addressLine2
                        }
                        onChange={
                          updateAddressField
                        }
                      />
                    </Grid>

                    <Grid
                      size={{
                        xs: 12,
                        sm: 4
                      }}
                    >
                      <TextField
                        fullWidth
                        required
                        label="City"
                        name="city"
                        value={
                          addressForm.city
                        }
                        onChange={
                          updateAddressField
                        }
                      />
                    </Grid>

                    <Grid
                      size={{
                        xs: 12,
                        sm: 4
                      }}
                    >
                      <TextField
                        fullWidth
                        required
                        label="State"
                        name="state"
                        value={
                          addressForm.state
                        }
                        onChange={
                          updateAddressField
                        }
                      />
                    </Grid>

                    <Grid
                      size={{
                        xs: 12,
                        sm: 4
                      }}
                    >
                      <TextField
                        fullWidth
                        required
                        label="Postal Code"
                        name="postalCode"
                        value={
                          addressForm.postalCode
                        }
                        onChange={
                          updateAddressField
                        }
                      />
                    </Grid>

                    <Grid
                      size={12}
                    >
                      <TextField
                        fullWidth
                        label="Country"
                        name="country"
                        value={
                          addressForm.country
                        }
                        onChange={
                          updateAddressField
                        }
                      />
                    </Grid>

                    <Grid
                      size={12}
                    >
                      <Stack
                        direction="row"
                        spacing={1}
                      >
                        <Button
                          type="submit"
                          variant="contained"
                        >
                          Save Address
                        </Button>

                        <Button
                          type="button"
                          onClick={() =>
                            setShowAddressForm(
                              false
                            )
                          }
                        >
                          Cancel
                        </Button>
                      </Stack>
                    </Grid>

                  </Grid>
                </Box>
              )}

            </CardContent>
          </Card>


          {/* Items */}
          <Card>
            <CardContent>
              <Typography
                variant="h6"
                fontWeight={900}
                mb={2}
              >
                Order Items
              </Typography>

              <Stack
                spacing={2}
              >
                {items.map(
                  item => {
                    const product =
                      getProduct(
                        item
                      );

                    const image =
                      product.thumbnailUrl ||
                      product.images?.[0]
                        ?.imageUrl ||
                      "https://via.placeholder.com/200x200?text=ASTRA";

                    return (
                      <Box
                        key={
                          item.id ||
                          getProductId(
                            item
                          )
                        }
                      >
                        <Stack
                          direction="row"
                          spacing={2}
                          alignItems="center"
                        >

                          <Box
                            component="img"
                            src={image}
                            alt={
                              product.name ||
                              "Product"
                            }
                            sx={{
                              width: 75,
                              height: 75,
                              borderRadius: 2,
                              objectFit:
                                "cover"
                            }}
                          />

                          <Box
                            sx={{
                              flex: 1,
                              minWidth: 0
                            }}
                          >
                            <Typography
                              fontWeight={800}
                              noWrap
                            >
                              {product.name ||
                                "Product"}
                            </Typography>

                            <Typography
                              variant="body2"
                              color="text.secondary"
                            >
                              Qty:{" "}
                              {
                                getQuantity(
                                  item
                                )
                              }
                            </Typography>
                          </Box>

                          <Typography
                            fontWeight={800}
                          >
                            ₹
                            {formatPrice(
                              getLineTotal(
                                item
                              )
                            )}
                          </Typography>

                        </Stack>

                        <Divider
                          sx={{
                            mt: 2
                          }}
                        />
                      </Box>
                    );
                  }
                )}
              </Stack>

            </CardContent>
          </Card>

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
                    {shipping === 0
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
                sx={{
                  mt: 3,
                  py: 1.5
                }}
                startIcon={
                  placingOrder ||
                  paymentLoading ? (
                    <CircularProgress
                      size={20}
                      color="inherit"
                    />
                  ) : (
                    <Payment />
                  )
                }
                endIcon={
                  !placingOrder &&
                  !paymentLoading && (
                    <ArrowForward />
                  )
                }
                onClick={
                  handlePlaceOrder
                }
                disabled={
                  placingOrder ||
                  paymentLoading ||
                  !selectedAddress ||
                  !items.length
                }
              >
                {paymentLoading
                  ? "Starting Payment..."
                  : placingOrder
                    ? "Placing Order..."
                    : "Place Order"}
              </Button>


              <Button
                fullWidth
                component={Link}
                to="/cart"
                variant="text"
                startIcon={
                  <ArrowBack />
                }
                sx={{
                  mt: 1
                }}
                disabled={
                  placingOrder ||
                  paymentLoading
                }
              >
                Back to Cart
              </Button>


              <Alert
                severity="info"
                sx={{
                  mt: 2
                }}
              >
                Your payment will be
                processed securely after
                the order is created.
              </Alert>

            </CardContent>
          </Card>
        </Grid>

      </Grid>
    </Container>
  );
}

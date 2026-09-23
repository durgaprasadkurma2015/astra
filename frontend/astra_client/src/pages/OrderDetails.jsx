import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";


import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  Divider,
  Grid,
  Stack,
  Step,
  StepLabel,
  Stepper,
  Typography
} from "@mui/material";

import {
  ArrowBack,
  CancelOutlined,
  LocalShippingOutlined,
  LocationOnOutlined,
  PaymentOutlined,
  ShoppingBagOutlined
} from "@mui/icons-material";

import {
  orderApi,
  paymentApi,
  shipmentApi
} from "../api/api";


export default function OrderDetails() {
  const { id } = useParams();

  const [order, setOrder] = useState(null);
  const [shipment, setShipment] = useState(null);
  const [payment, setPayment] = useState(null);

  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");


  /*
   * Load order + shipment + payment
   *
   * useCallback keeps the function stable until id changes.
   */
  const loadOrder = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const orderResponse = await orderApi.get(id);

      const orderData =
        orderResponse.data?.order ||
        orderResponse.data;

      setOrder(orderData);


      /*
       * Load shipment separately.
       * Shipment may not exist yet.
       */
      try {
        const shipmentResponse =
          await shipmentApi.byOrder(id);

        setShipment(
          shipmentResponse.data?.shipment ||
          shipmentResponse.data
        );
      } catch {
        setShipment(null);
      }


      /*
       * Load payment separately.
       * Payment may not exist yet.
       */
      try {
        const paymentResponse =
          await paymentApi.byOrder(id);

        setPayment(
          paymentResponse.data?.payment ||
          paymentResponse.data
        );
      } catch {
        setPayment(null);
      }

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to load the order."
      );
    } finally {
      setLoading(false);
    }
  }, [id]);


  /*
   * Load order whenever the URL order id changes.
   */
  useEffect(() => {
    loadOrder();
  }, [loadOrder]);


  const formatPrice = (value) =>
    Number(value || 0).toLocaleString(
      "en-IN"
    );


  const formatDate = (value) => {
    if (!value) {
      return "—";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return date.toLocaleDateString(
      "en-IN",
      {
        day: "2-digit",
        month: "short",
        year: "numeric"
      }
    );
  };


  const formatDateTime = (value) => {
    if (!value) {
      return "—";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return date.toLocaleString(
      "en-IN",
      {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      }
    );
  };


  const formatStatus = (value) => {
    return String(
      value || "PENDING"
    )
      .toLowerCase()
      .split("_")
      .map(
        (word) =>
          word.charAt(0).toUpperCase() +
          word.slice(1)
      )
      .join(" ");
  };


  const getStatusColor = (value) => {
    const status = String(
      value || ""
    ).toUpperCase();

    if (
      [
        "DELIVERED",
        "COMPLETED",
        "SUCCESS"
      ].includes(status)
    ) {
      return "success";
    }

    if (
      [
        "CANCELLED",
        "CANCELED",
        "FAILED",
        "REJECTED"
      ].includes(status)
    ) {
      return "error";
    }

    if (
      [
        "SHIPPED",
        "OUT_FOR_DELIVERY",
        "IN_TRANSIT"
      ].includes(status)
    ) {
      return "info";
    }

    if (
      [
        "CONFIRMED",
        "PROCESSING",
        "PACKED"
      ].includes(status)
    ) {
      return "primary";
    }

    return "warning";
  };


  const getItems = () => {
    return (
      order?.items ||
      order?.orderItems ||
      order?.products ||
      []
    );
  };


  const getAddress = () => {
    return (
      order?.shippingAddress ||
      order?.deliveryAddress ||
      order?.address ||
      {}
    );
  };


  const getProduct = (item) => {
    return item.product || item;
  };


  const getQuantity = (item) => {
    return Number(
      item.quantity || 1
    );
  };


  const getItemPrice = (item) => {
    const product = getProduct(item);

    return Number(
      item.unitPrice ??
      item.price ??
      product.discountPrice ??
      product.price ??
      0
    );
  };


  const getItemTotal = (item) => {
    return Number(
      item.subtotal ??
      item.totalPrice ??
      item.lineTotal ??
      getItemPrice(item) *
        getQuantity(item)
    );
  };


  const getOrderTotal = () => {
    return Number(
      order?.total ??
      order?.totalAmount ??
      order?.grandTotal ??
      order?.amount ??
      0
    );
  };


  const handleCancel = async () => {
    const confirmed =
      window.confirm(
        "Are you sure you want to cancel this order?"
      );

    if (!confirmed) {
      return;
    }

    setCancelling(true);
    setError("");
    setSuccess("");

    try {
      const response =
        await orderApi.cancel(id);

      const updatedOrder =
        response.data?.order ||
        response.data;

      if (
        updatedOrder &&
        typeof updatedOrder === "object"
      ) {
        setOrder(updatedOrder);
      } else {
        await loadOrder();
      }

      setSuccess(
        "Your order has been cancelled."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to cancel this order."
      );
    } finally {
      setCancelling(false);
    }
  };


  if (loading) {
    return (
      <Container
        maxWidth="lg"
        sx={{ py: 10 }}
      >
        <Stack
          alignItems="center"
          spacing={2}
        >
          <CircularProgress />

          <Typography color="text.secondary">
            Loading order details...
          </Typography>
        </Stack>
      </Container>
    );
  }


  if (!order) {
    return (
      <Container
        maxWidth="md"
        sx={{ py: 8 }}
      >
        <Alert severity="error">
          {error || "Order not found."}
        </Alert>

        <Button
          component={Link}
          to="/orders"
          startIcon={<ArrowBack />}
          sx={{ mt: 2 }}
        >
          Back to Orders
        </Button>
      </Container>
    );
  }


  const items = getItems();
  const address = getAddress();

  const orderNumber =
    order.orderNumber ||
    order.orderNo ||
    order.number ||
    `#${order.id}`;

  const status =
    order.status ||
    order.orderStatus ||
    "PENDING";

  const paymentStatus =
    payment?.status ||
    order.paymentStatus ||
    "PENDING";

  const shipmentStatus =
    shipment?.status ||
    order.shipmentStatus ||
    null;

  const subtotal = Number(
    order.subtotal ??
    order.subTotal ??
    items.reduce(
      (sum, item) =>
        sum + getItemTotal(item),
      0
    )
  );

  const discount = Number(
    order.discount ??
    order.discountAmount ??
    0
  );

  const shipping = Number(
    order.shipping ??
    order.shippingFee ??
    order.deliveryFee ??
    0
  );

  const tax = Number(
    order.tax ??
    order.taxAmount ??
    0
  );

  const total = getOrderTotal();

  const normalizedStatus =
    String(status).toUpperCase();

  const canCancel =
    ![
      "CANCELLED",
      "CANCELED",
      "DELIVERED",
      "COMPLETED",
      "SHIPPED",
      "OUT_FOR_DELIVERY"
    ].includes(normalizedStatus);


  const statusSteps = [
    "PLACED",
    "CONFIRMED",
    "PROCESSING",
    "SHIPPED",
    "DELIVERED"
  ];


  const statusIndexMap = {
    PENDING: 0,
    PLACED: 0,
    CONFIRMED: 1,
    PROCESSING: 2,
    PACKED: 2,
    SHIPPED: 3,
    IN_TRANSIT: 3,
    OUT_FOR_DELIVERY: 3,
    DELIVERED: 4,
    COMPLETED: 4
  };


  const activeStep =
    statusIndexMap[
      normalizedStatus
    ] ?? 0;


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
        mb={3}
      >
        <Box>
          <Button
            component={Link}
            to="/orders"
            startIcon={<ArrowBack />}
            sx={{ mb: 1 }}
          >
            Back to Orders
          </Button>

          <Typography
            variant="h4"
            fontWeight={900}
          >
            Order {orderNumber}
          </Typography>

          <Typography
            color="text.secondary"
            sx={{ mt: 0.5 }}
          >
            Placed on{" "}
            {formatDateTime(
              order.createdAt ||
              order.createdDate ||
              order.orderDate
            )}
          </Typography>
        </Box>

        <Chip
          label={formatStatus(status)}
          color={getStatusColor(status)}
          sx={{
            fontWeight: 800,
            px: 1
          }}
        />
      </Stack>


      {error && (
        <Alert
          severity="error"
          sx={{ mb: 2 }}
          onClose={() => setError("")}
        >
          {error}
        </Alert>
      )}


      {success && (
        <Alert
          severity="success"
          sx={{ mb: 2 }}
          onClose={() => setSuccess("")}
        >
          {success}
        </Alert>
      )}


      {/* Status */}
      {![
        "CANCELLED",
        "CANCELED",
        "FAILED"
      ].includes(normalizedStatus) && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography
              variant="h6"
              fontWeight={900}
              mb={3}
            >
              Order Status
            </Typography>

            <Stepper
              activeStep={activeStep}
              alternativeLabel
            >
              {statusSteps.map((step) => (
                <Step key={step}>
                  <StepLabel>
                    {formatStatus(step)}
                  </StepLabel>
                </Step>
              ))}
            </Stepper>
          </CardContent>
        </Card>
      )}


      <Grid
        container
        spacing={3}
        alignItems="flex-start"
      >

        {/* Main */}
        <Grid
          size={{
            xs: 12,
            lg: 8
          }}
        >

          {/* Products */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Stack
                direction="row"
                spacing={1}
                alignItems="center"
                mb={2}
              >
                <ShoppingBagOutlined
                  color="primary"
                />

                <Typography
                  variant="h6"
                  fontWeight={900}
                >
                  Products
                </Typography>
              </Stack>

              <Stack spacing={2}>
                {items.map(
                  (item, index) => {
                    const product =
                      getProduct(item);

                    const image =
                      product.thumbnailUrl ||
                      product.images?.[0]
                        ?.imageUrl ||
                      "https://via.placeholder.com/150x150?text=ASTRA";

                    return (
                      <Box
                        key={
                          item.id ||
                          product.id ||
                          index
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
                              width: 85,
                              height: 85,
                              borderRadius: 2,
                              objectFit:
                                "cover",
                              flexShrink: 0
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
                            >
                              {product.name ||
                                item.productName ||
                                "Product"}
                            </Typography>

                            <Typography
                              variant="body2"
                              color="text.secondary"
                              sx={{ mt: 0.5 }}
                            >
                              Quantity:{" "}
                              {getQuantity(
                                item
                              )}
                            </Typography>

                            <Typography
                              variant="body2"
                              color="text.secondary"
                            >
                              ₹
                              {formatPrice(
                                getItemPrice(
                                  item
                                )
                              )}{" "}
                              each
                            </Typography>
                          </Box>

                          <Typography
                            fontWeight={900}
                          >
                            ₹
                            {formatPrice(
                              getItemTotal(
                                item
                              )
                            )}
                          </Typography>
                        </Stack>

                        {index <
                          items.length -
                            1 && (
                          <Divider
                            sx={{ mt: 2 }}
                          />
                        )}
                      </Box>
                    );
                  }
                )}
              </Stack>
            </CardContent>
          </Card>


          {/* Shipping */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Stack
                direction="row"
                spacing={1}
                alignItems="center"
                mb={2}
              >
                <LocationOnOutlined
                  color="primary"
                />

                <Typography
                  variant="h6"
                  fontWeight={900}
                >
                  Delivery Address
                </Typography>
              </Stack>

              <Typography fontWeight={800}>
                {address.name ||
                  address.fullName ||
                  "Delivery Address"}
              </Typography>

              <Typography
                color="text.secondary"
                sx={{ mt: 0.5 }}
              >
                {address.addressLine1 ||
                  address.line1 ||
                  address.address ||
                  ""}
              </Typography>

              {address.addressLine2 && (
                <Typography color="text.secondary">
                  {address.addressLine2}
                </Typography>
              )}

              <Typography color="text.secondary">
                {address.city}
                {address.city &&
                  address.state &&
                  ", "}
                {address.state}{" "}
                {address.postalCode ||
                  address.zipCode ||
                  ""}
              </Typography>

              {address.country && (
                <Typography color="text.secondary">
                  {address.country}
                </Typography>
              )}

              {address.phone && (
                <Typography
                  color="text.secondary"
                  sx={{ mt: 1 }}
                >
                  Phone: {address.phone}
                </Typography>
              )}
            </CardContent>
          </Card>


          {/* Shipment */}
          {(shipment ||
            shipmentStatus ||
            order.trackingNumber) && (
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Stack
                  direction="row"
                  spacing={1}
                  alignItems="center"
                  mb={2}
                >
                  <LocalShippingOutlined
                    color="primary"
                  />

                  <Typography
                    variant="h6"
                    fontWeight={900}
                  >
                    Shipment
                  </Typography>
                </Stack>

                <Stack spacing={1.5}>

                  {shipmentStatus && (
                    <Stack
                      direction="row"
                      justifyContent="space-between"
                    >
                      <Typography color="text.secondary">
                        Status
                      </Typography>

                      <Chip
                        size="small"
                        label={formatStatus(
                          shipmentStatus
                        )}
                        color={getStatusColor(
                          shipmentStatus
                        )}
                      />
                    </Stack>
                  )}


                  {(shipment?.trackingNumber ||
                    order.trackingNumber) && (
                    <Stack
                      direction={{
                        xs: "column",
                        sm: "row"
                      }}
                      justifyContent="space-between"
                    >
                      <Typography color="text.secondary">
                        Tracking Number
                      </Typography>

                      <Typography
                        fontWeight={800}
                      >
                        {shipment?.trackingNumber ||
                          order.trackingNumber}
                      </Typography>
                    </Stack>
                  )}


                  {(shipment?.carrier ||
                    order.carrier) && (
                    <Stack
                      direction={{
                        xs: "column",
                        sm: "row"
                      }}
                      justifyContent="space-between"
                    >
                      <Typography color="text.secondary">
                        Carrier
                      </Typography>

                      <Typography fontWeight={800}>
                        {shipment?.carrier ||
                          order.carrier}
                      </Typography>
                    </Stack>
                  )}


                  {(shipment?.estimatedDelivery ||
                    order.estimatedDelivery) && (
                    <Stack
                      direction={{
                        xs: "column",
                        sm: "row"
                      }}
                      justifyContent="space-between"
                    >
                      <Typography color="text.secondary">
                        Estimated Delivery
                      </Typography>

                      <Typography fontWeight={800}>
                        {formatDate(
                          shipment?.estimatedDelivery ||
                            order.estimatedDelivery
                        )}
                      </Typography>
                    </Stack>
                  )}


                  {(shipment?.trackingNumber ||
                    order.trackingNumber) && (
                    <Button
                      variant="outlined"
                      component={Link}
                      to={`/shipments/${
                        shipment?.id || ""
                      }`}
                      startIcon={
                        <LocalShippingOutlined />
                      }
                      sx={{
                        alignSelf: {
                          xs: "stretch",
                          sm: "flex-start"
                        }
                      }}
                    >
                      Track Shipment
                    </Button>
                  )}

                </Stack>
              </CardContent>
            </Card>
          )}


          {/* Payment */}
          <Card>
            <CardContent>
              <Stack
                direction="row"
                spacing={1}
                alignItems="center"
                mb={2}
              >
                <PaymentOutlined
                  color="primary"
                />

                <Typography
                  variant="h6"
                  fontWeight={900}
                >
                  Payment
                </Typography>
              </Stack>

              <Stack spacing={1.5}>

                <Stack
                  direction="row"
                  justifyContent="space-between"
                >
                  <Typography color="text.secondary">
                    Payment Status
                  </Typography>

                  <Chip
                    size="small"
                    label={formatStatus(
                      paymentStatus
                    )}
                    color={getStatusColor(
                      paymentStatus
                    )}
                  />
                </Stack>


                {(payment?.method ||
                  order.paymentMethod) && (
                  <Stack
                    direction="row"
                    justifyContent="space-between"
                  >
                    <Typography color="text.secondary">
                      Payment Method
                    </Typography>

                    <Typography fontWeight={800}>
                      {formatStatus(
                        payment?.method ||
                          order.paymentMethod
                      )}
                    </Typography>
                  </Stack>
                )}


                {(payment?.transactionId ||
                  payment?.paymentId) && (
                  <Stack
                    direction="row"
                    justifyContent="space-between"
                  >
                    <Typography color="text.secondary">
                      Transaction ID
                    </Typography>

                    <Typography
                      fontWeight={700}
                      sx={{
                        wordBreak:
                          "break-all"
                      }}
                    >
                      {payment.transactionId ||
                        payment.paymentId}
                    </Typography>
                  </Stack>
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
            <CardContent sx={{ p: 3 }}>

              <Typography
                variant="h5"
                fontWeight={900}
                mb={3}
              >
                Order Summary
              </Typography>


              <Stack spacing={1.5}>

                <Stack
                  direction="row"
                  justifyContent="space-between"
                >
                  <Typography color="text.secondary">
                    Subtotal
                  </Typography>

                  <Typography fontWeight={700}>
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
                    <Typography color="text.secondary">
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
                  <Typography color="text.secondary">
                    Shipping
                  </Typography>

                  <Typography fontWeight={700}>
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
                    <Typography color="text.secondary">
                      Tax
                    </Typography>

                    <Typography fontWeight={700}>
                      ₹
                      {formatPrice(
                        tax
                      )}
                    </Typography>
                  </Stack>
                )}

              </Stack>


              <Divider sx={{ my: 2.5 }} />


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
                  color="primary"
                  fontWeight={900}
                >
                  ₹
                  {formatPrice(
                    total
                  )}
                </Typography>
              </Stack>


              {canCancel && (
                <Button
                  fullWidth
                  variant="outlined"
                  color="error"
                  startIcon={
                    cancelling ? (
                      <CircularProgress
                        size={18}
                        color="inherit"
                      />
                    ) : (
                      <CancelOutlined />
                    )
                  }
                  onClick={
                    handleCancel
                  }
                  disabled={cancelling}
                  sx={{ mt: 3 }}
                >
                  {cancelling
                    ? "Cancelling..."
                    : "Cancel Order"}
                </Button>
              )}


              <Button
                fullWidth
                component={Link}
                to="/orders"
                variant="text"
                startIcon={
                  <ArrowBack />
                }
                sx={{ mt: 1 }}
              >
                Back to Orders
              </Button>

            </CardContent>
          </Card>
        </Grid>

      </Grid>
    </Container>
  );
}

import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
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
  Pagination,
  Stack,
  Typography
} from "@mui/material";

import {
  ArrowForward,
  Inventory2Outlined,
  ShoppingBagOutlined
} from "@mui/icons-material";

import { orderApi } from "../api/api";

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  /*
   * Load orders.
   *
   * This function is used by pagination.
   * Initial loading is handled directly inside useEffect
   * to avoid the React Hooks set-state-in-effect warning.
   */
  const loadOrders = async (requestedPage = 0) => {
    setLoading(true);
    setError("");

    try {
      const response = await orderApi.list({
        page: requestedPage,
        size: 10
      });

      const data = response.data;

      const list =
        data?.orders ||
        data?.content ||
        data?.items ||
        data ||
        [];

      setOrders(
        Array.isArray(list)
          ? list
          : []
      );

      setPage(
        Number(
          data?.page ??
          requestedPage
        )
      );

      setTotalPages(
        Number(
          data?.totalPages ||
          1
        )
      );
    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to load your orders."
      );
    } finally {
      setLoading(false);
    }
  };

  /*
   * Initial order loading.
   *
   * The API call is defined and executed inside the effect
   * instead of calling loadOrders(), because loadOrders()
   * performs synchronous setState calls.
   */
  useEffect(() => {
    let cancelled = false;

    const loadInitialOrders = async () => {
      setLoading(true);
      setError("");

      try {
        const response = await orderApi.list({
          page: 0,
          size: 10
        });

        if (cancelled) {
          return;
        }

        const data = response.data;

        const list =
          data?.orders ||
          data?.content ||
          data?.items ||
          data ||
          [];

        setOrders(
          Array.isArray(list)
            ? list
            : []
        );

        setPage(
          Number(
            data?.page ?? 0
          )
        );

        setTotalPages(
          Number(
            data?.totalPages || 1
          )
        );
      } catch (err) {
        if (cancelled) {
          return;
        }

        setError(
          err?.response?.data?.message ||
          "Unable to load your orders."
        );
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadInitialOrders();

    return () => {
      cancelled = true;
    };
  }, []);

  const formatPrice = (value) => {
    return Number(
      value || 0
    ).toLocaleString(
      "en-IN"
    );
  };

  const formatDate = (value) => {
    if (!value) {
      return "—";
    }

    const date = new Date(value);

    if (
      Number.isNaN(
        date.getTime()
      )
    ) {
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

  const getOrderId = (order) => {
    return (
      order.id ||
      order.orderId
    );
  };

  const getOrderNumber = (order) => {
    return (
      order.orderNumber ||
      order.orderNo ||
      order.number ||
      `#${getOrderId(order) || ""}`
    );
  };

  const getStatus = (order) => {
    return (
      order.status ||
      order.orderStatus ||
      "PENDING"
    );
  };

  const getItems = (order) => {
    return (
      order.items ||
      order.orderItems ||
      order.products ||
      []
    );
  };

  const getTotal = (order) => {
    return Number(
      order.total ??
      order.totalAmount ??
      order.grandTotal ??
      order.amount ??
      0
    );
  };

  const getDate = (order) => {
    return (
      order.createdAt ||
      order.createdDate ||
      order.orderDate ||
      order.date
    );
  };

  const getStatusColor = (status) => {
    const normalized =
      String(status)
        .toUpperCase();

    if (
      normalized === "DELIVERED" ||
      normalized === "COMPLETED" ||
      normalized === "SUCCESS"
    ) {
      return "success";
    }

    if (
      normalized === "CANCELLED" ||
      normalized === "CANCELED" ||
      normalized === "FAILED" ||
      normalized === "REJECTED"
    ) {
      return "error";
    }

    if (
      normalized === "SHIPPED" ||
      normalized === "OUT_FOR_DELIVERY"
    ) {
      return "info";
    }

    if (
      normalized === "PROCESSING" ||
      normalized === "CONFIRMED" ||
      normalized === "PACKED"
    ) {
      return "primary";
    }

    return "warning";
  };

  const formatStatus = (status) => {
    return String(
      status || "PENDING"
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

  if (loading) {
    return (
      <Container
        maxWidth="lg"
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
            Loading your orders...
          </Typography>
        </Stack>
      </Container>
    );
  }

  return (
    <Container
      maxWidth="lg"
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
            <ShoppingBagOutlined
              color="primary"
            />

            <Typography
              variant="h4"
              fontWeight={900}
            >
              My Orders
            </Typography>
          </Stack>

          <Typography
            color="text.secondary"
            sx={{
              mt: 0.75
            }}
          >
            View and track your
            previous orders.
          </Typography>
        </Box>

        <Button
          component={Link}
          to="/products"
          variant="contained"
        >
          Continue Shopping
        </Button>
      </Stack>

      {/* Error */}
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

      {/* Empty state */}
      {orders.length === 0 ? (
        <Card>
          <CardContent
            sx={{
              py: 9,
              textAlign: "center"
            }}
          >
            <Inventory2Outlined
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
              No orders yet
            </Typography>

            <Typography
              color="text.secondary"
              sx={{
                mt: 1,
                mb: 3
              }}
            >
              Your completed purchases
              will appear here.
            </Typography>

            <Button
              component={Link}
              to="/products"
              variant="contained"
            >
              Start Shopping
            </Button>
          </CardContent>
        </Card>
      ) : (
        <Stack spacing={2}>
          {orders.map((order) => {
            const id =
              getOrderId(order);

            const orderNumber =
              getOrderNumber(order);

            const status =
              getStatus(order);

            const items =
              getItems(order);

            const total =
              getTotal(order);

            const date =
              getDate(order);

            const itemCount =
              order.totalItems ??
              items.reduce(
                (
                  sum,
                  item
                ) =>
                  sum +
                  Number(
                    item.quantity ||
                      1
                  ),
                0
              );

            return (
              <Card
                key={
                  id ||
                  orderNumber
                }
                sx={{
                  transition:
                    "box-shadow .2s ease",
                  "&:hover": {
                    boxShadow: 4
                  }
                }}
              >
                <CardContent
                  sx={{
                    p: {
                      xs: 2,
                      md: 3
                    }
                  }}
                >
                  {/* Top row */}
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
                  >
                    <Box>
                      <Typography
                        variant="h6"
                        fontWeight={900}
                      >
                        Order{" "}
                        {orderNumber}
                      </Typography>

                      <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                          mt: 0.5
                        }}
                      >
                        Placed on{" "}
                        {formatDate(
                          date
                        )}
                      </Typography>
                    </Box>

                    <Chip
                      label={formatStatus(
                        status
                      )}
                      color={getStatusColor(
                        status
                      )}
                      sx={{
                        fontWeight: 700
                      }}
                    />
                  </Stack>

                  <Divider
                    sx={{
                      my: 2
                    }}
                  />

                  {/* Order information */}
                  <Stack
                    direction={{
                      xs: "column",
                      sm: "row"
                    }}
                    spacing={{
                      xs: 1.5,
                      sm: 4
                    }}
                  >
                    <Box>
                      <Typography
                        variant="caption"
                        color="text.secondary"
                      >
                        Items
                      </Typography>

                      <Typography
                        fontWeight={800}
                      >
                        {itemCount}{" "}
                        {itemCount === 1
                          ? "item"
                          : "items"}
                      </Typography>
                    </Box>

                    <Box>
                      <Typography
                        variant="caption"
                        color="text.secondary"
                      >
                        Order Total
                      </Typography>

                      <Typography
                        fontWeight={900}
                        color="primary"
                      >
                        ₹
                        {formatPrice(
                          total
                        )}
                      </Typography>
                    </Box>

                    {order.paymentStatus && (
                      <Box>
                        <Typography
                          variant="caption"
                          color="text.secondary"
                        >
                          Payment
                        </Typography>

                        <Typography
                          fontWeight={700}
                        >
                          {formatStatus(
                            order.paymentStatus
                          )}
                        </Typography>
                      </Box>
                    )}
                  </Stack>

                  {/* Items preview */}
                  {items.length > 0 && (
                    <Box
                      sx={{
                        mt: 2
                      }}
                    >
                      <Typography
                        variant="caption"
                        color="text.secondary"
                      >
                        Products
                      </Typography>

                      <Stack
                        spacing={0.5}
                        sx={{
                          mt: 0.5
                        }}
                      >
                        {items
                          .slice(0, 3)
                          .map(
                            (
                              item,
                              index
                            ) => {
                              const product =
                                item.product ||
                                item;

                              return (
                                <Typography
                                  key={
                                    item.id ||
                                    index
                                  }
                                  variant="body2"
                                  noWrap
                                >
                                  •{" "}
                                  {product.name ||
                                    item.productName ||
                                    "Product"}
                                  {" × "}
                                  {item.quantity ||
                                    1}
                                </Typography>
                              );
                            }
                          )}

                        {items.length >
                          3 && (
                          <Typography
                            variant="caption"
                            color="text.secondary"
                          >
                            +{" "}
                            {items.length -
                              3}{" "}
                            more
                          </Typography>
                        )}
                      </Stack>
                    </Box>
                  )}

                  {/* Actions */}
                  <Stack
                    direction={{
                      xs: "column",
                      sm: "row"
                    }}
                    justifyContent="flex-end"
                    spacing={1}
                    sx={{
                      mt: 2
                    }}
                  >
                    {id && (
                      <Button
                        component={Link}
                        to={`/orders/${id}`}
                        variant="contained"
                        endIcon={
                          <ArrowForward />
                        }
                      >
                        View Order
                      </Button>
                    )}
                  </Stack>
                </CardContent>
              </Card>
            );
          })}

          {/* Pagination */}
          {totalPages > 1 && (
            <Stack
              alignItems="center"
              sx={{
                pt: 2
              }}
            >
              <Pagination
                count={totalPages}
                page={page + 1}
                color="primary"
                onChange={(
                  _event,
                  value
                ) => {
                  loadOrders(
                    value - 1
                  );

                  window.scrollTo({
                    top: 0,
                    behavior:
                      "smooth"
                  });
                }}
              />
            </Stack>
          )}
        </Stack>
      )}
    </Container>
  );
}

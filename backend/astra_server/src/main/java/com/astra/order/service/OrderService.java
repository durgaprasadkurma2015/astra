package com.astra.order.service;

import com.astra.dto.OrderItemResponse;
import com.astra.entity.Cart;
import com.astra.entity.CartItem;
import com.astra.entity.Order;
import com.astra.entity.OrderItem;
import com.astra.entity.Product;
import com.astra.entity.User;
import com.astra.enums.OrderStatus;
import com.astra.enums.PaymentStatus;
import com.astra.order.dto.CreateOrderRequest;
import com.astra.order.dto.OrderResponse;
import com.astra.repository.CartRepository;
import com.astra.repository.OrderRepository;
import com.astra.repository.ProductRepository;
import com.astra.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create order from current user's cart.
     */
    public OrderResponse createOrder(
            String email,
            CreateOrderRequest request
    ) {

        User user = getUser(email);

        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Cart not found"
                        )
                );

        if (cart.getItems() == null ||
                cart.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Cannot create order. Cart is empty"
            );
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        List<OrderItem> orderItems =
                new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            validateProduct(product);

            int quantity = cartItem.getQuantity();

            if (quantity <= 0) {

                throw new IllegalArgumentException(
                        "Invalid cart quantity for product: "
                                + product.getId()
                );
            }

            if (product.getStockQuantity() == null ||
                    product.getStockQuantity() < quantity) {

                throw new IllegalArgumentException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            BigDecimal currentPrice =
                    getCurrentPrice(product);

            BigDecimal originalPrice =
                    product.getPrice();

            BigDecimal lineTotal =
                    currentPrice.multiply(
                            BigDecimal.valueOf(quantity)
                    );

            BigDecimal originalLineTotal =
                    originalPrice.multiply(
                            BigDecimal.valueOf(quantity)
                    );

            subtotal =
                    subtotal.add(lineTotal);

            if (originalLineTotal.compareTo(lineTotal) > 0) {

                discount =
                        discount.add(
                                originalLineTotal.subtract(
                                        lineTotal
                                )
                        );
            }

            OrderItem orderItem =
                    OrderItem.builder()
                            .product(product)
                            .productName(product.getName())
                            .productSlug(product.getSlug())
                            .thumbnailUrl(
                                    product.getThumbnailUrl()
                            )
                            .quantity(quantity)
                            .unitPrice(currentPrice)
                            .lineTotal(lineTotal)
                            .build();

            orderItems.add(orderItem);
        }

        BigDecimal shippingCharge =
                calculateShipping(subtotal);

        BigDecimal tax =
                calculateTax(subtotal);

        BigDecimal total =
                subtotal
                        .add(shippingCharge)
                        .add(tax);

        Order order =
                Order.builder()
                        .orderNumber(generateOrderNumber())
                        .user(user)
                        .status(OrderStatus.PENDING)
                        .paymentStatus(PaymentStatus.PENDING)
                        .subtotal(subtotal)
                        .discount(discount)
                        .shippingCharge(shippingCharge)
                        .tax(tax)
                        .total(total)
                        .shippingFullName(
                                request.shippingFullName()
                        )
                        .shippingPhone(
                                request.shippingPhone()
                        )
                        .shippingAddressLine1(
                                request.shippingAddressLine1()
                        )
                        .shippingAddressLine2(
                                request.shippingAddressLine2()
                        )
                        .shippingCity(
                                request.shippingCity()
                        )
                        .shippingState(
                                request.shippingState()
                        )
                        .shippingPostalCode(
                                request.shippingPostalCode()
                        )
                        .shippingCountry(
                                request.shippingCountry()
                        )
                        .items(new ArrayList<>())
                        .build();

        for (OrderItem orderItem : orderItems) {

            orderItem.setOrder(order);

            order.getItems().add(orderItem);
        }

        /*
         * Reduce inventory and increase sales count.
         */
        for (CartItem cartItem : cart.getItems()) {

            Product product =
                    cartItem.getProduct();

            int currentStock =
                    safeInt(product.getStockQuantity());

            int newStock =
                    currentStock - cartItem.getQuantity();

            product.setStockQuantity(newStock);

            long currentSales =
                    safeLong(product.getSalesCount());

            long newSales =
                    currentSales + cartItem.getQuantity();

            product.setSalesCount(newSales);

            productRepository.save(product);
        }

        Order savedOrder =
                orderRepository.save(order);

        /*
         * Clear cart after successful order creation.
         */
        cart.getItems().clear();

        cart.setSubtotal(BigDecimal.ZERO);
        cart.setDiscount(BigDecimal.ZERO);
        cart.setTotal(BigDecimal.ZERO);

        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    /**
     * Get current user's orders.
     */
    @Transactional
    public Page<OrderResponse> getOrders(
            String email,
            Pageable pageable
    ) {

        User user = getUser(email);

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                )
                .map(this::toResponse);
    }

    /**
     * Get order details.
     */
    @Transactional
    public OrderResponse getOrder(
            String email,
            Long orderId
    ) {

        User user = getUser(email);

        Order order =
                orderRepository
                        .findByIdAndUserId(
                                orderId,
                                user.getId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        return toResponse(order);
    }

    /**
     * Cancel order.
     */
    public OrderResponse cancelOrder(
            String email,
            Long orderId
    ) {

        User user = getUser(email);

        Order order =
                orderRepository
                        .findByIdAndUserId(
                                orderId,
                                user.getId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        if (!canCancel(order.getStatus())) {

            throw new IllegalArgumentException(
                    "Order cannot be cancelled in status: "
                            + order.getStatus()
            );
        }

        /*
         * Restore stock and sales count.
         */
        for (OrderItem item : order.getItems()) {

            Product product =
                    item.getProduct();

            int currentStock =
                    safeInt(product.getStockQuantity());

            product.setStockQuantity(
                    currentStock + item.getQuantity()
            );

            long currentSales =
                    safeLong(product.getSalesCount());

            long newSales =
                    Math.max(
                            0L,
                            currentSales - item.getQuantity()
                    );

            product.setSalesCount(newSales);

            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);

        /*
         * Payment is still PENDING in Phase 9.
         * Payment/refund handling can be added separately.
         */
        Order savedOrder =
                orderRepository.save(order);

        return toResponse(savedOrder);
    }

    /**
     * Get order status.
     */
    @Transactional
    public OrderStatus getStatus(
            String email,
            Long orderId
    ) {

        User user = getUser(email);

        Order order =
                orderRepository
                        .findByIdAndUserId(
                                orderId,
                                user.getId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        return order.getStatus();
    }

    private BigDecimal calculateShipping(
            BigDecimal subtotal
    ) {

        /*
         * Free shipping above ₹1,000.
         * ₹50 otherwise.
         */
        if (subtotal.compareTo(
                BigDecimal.valueOf(1000)
        ) >= 0) {

            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(50);
    }

    private BigDecimal calculateTax(
            BigDecimal subtotal
    ) {

        /*
         * 18% placeholder tax.
         */
        return subtotal
                .multiply(
                        BigDecimal.valueOf(0.18)
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private boolean canCancel(
            OrderStatus status
    ) {

        return status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PROCESSING;
    }

    private void validateProduct(
            Product product
    ) {

        if (!product.isActive()) {

            throw new IllegalArgumentException(
                    "Product is not active: "
                            + product.getId()
            );
        }
    }

    private BigDecimal getCurrentPrice(
            Product product
    ) {

        if (product.getDiscountPrice() != null
                && product.getDiscountPrice()
                .compareTo(product.getPrice()) < 0) {

            return product.getDiscountPrice();
        }

        return product.getPrice();
    }

    private User getUser(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated user email is missing"
            );
        }

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );
    }

    private String generateOrderNumber() {

        String orderNumber;

        do {

            orderNumber =
                    "ASTRA-"
                            + UUID.randomUUID()
                            .toString()
                            .substring(0, 12)
                            .toUpperCase();

        } while (
                orderRepository.existsByOrderNumber(
                        orderNumber
                )
        );

        return orderNumber;
    }

    private int safeInt(
            Integer value
    ) {

        return value == null ? 0 : value;
    }

    private long safeLong(
            Long value
    ) {

        return value == null ? 0L : value;
    }

    private OrderResponse toResponse(
            Order order
    ) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(
                                item ->
                                        new OrderItemResponse(
                                                item.getId(),
                                                item.getProduct().getId(),
                                                item.getProductName(),
                                                item.getProductSlug(),
                                                item.getThumbnailUrl(),
                                                item.getQuantity(),
                                                item.getUnitPrice(),
                                                item.getLineTotal()
                                        )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getId(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getSubtotal(),
                order.getDiscount(),
                order.getShippingCharge(),
                order.getTax(),
                order.getTotal(),
                order.getShippingFullName(),
                order.getShippingPhone(),
                order.getShippingAddressLine1(),
                order.getShippingAddressLine2(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPostalCode(),
                order.getShippingCountry(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}

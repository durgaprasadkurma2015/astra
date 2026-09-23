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
import com.astra.inventory.service.InventoryService;
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
private final InventoryService inventoryService;

public OrderService(
        OrderRepository orderRepository,
        CartRepository cartRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        InventoryService inventoryService
) {
    this.orderRepository = orderRepository;
    this.cartRepository = cartRepository;
    this.productRepository = productRepository;
    this.userRepository = userRepository;
    this.inventoryService = inventoryService;
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

    /*
     * Build order items and calculate totals.
     */
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

        /*
         * Check Product.stockQuantity first.
         *
         * InventoryService.reserve() performs the
         * final locked inventory check.
         */
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

    /*
     * Create order.
     */
    Order order =
            Order.builder()
                    .orderNumber(
                            generateOrderNumber()
                    )
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

    /*
     * Attach order items to order.
     */
    for (OrderItem orderItem : orderItems) {

        orderItem.setOrder(order);

        order.getItems().add(orderItem);
    }

    /*
     * Save the order BEFORE reserving inventory.
     *
     * InventoryMovement has an Order relationship,
     * so using the persisted order is safer.
     */
    Order savedOrder =
            orderRepository.save(order);

    /*
     * Reserve inventory exactly ONCE.
     *
     * Do NOT call reserve() twice.
     */
    for (CartItem cartItem : cart.getItems()) {

        inventoryService.reserve(
                cartItem.getProduct().getId(),
                cartItem.getQuantity(),
                savedOrder
        );
    }

    /*
     * Clear cart after successful order creation
     * and successful inventory reservation.
     */
    cart.getItems().clear();

    cart.setSubtotal(
            BigDecimal.ZERO
    );

    cart.setDiscount(
            BigDecimal.ZERO
    );

    cart.setTotal(
            BigDecimal.ZERO
    );

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
     * Release reserved inventory.
     *
     * Do NOT directly modify Product.stockQuantity
     * here because InventoryService owns the
     * available/reserved inventory state.
     */
    for (OrderItem item : order.getItems()) {

        inventoryService.release(
                item.getProduct().getId(),
                item.getQuantity(),
                order,
                "Reserved stock released after order cancellation"
        );
    }

    order.setStatus(
            OrderStatus.CANCELLED
    );

    /*
     * Payment is still PENDING unless it has already
     * been processed separately.
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

    if (product == null) {

        throw new IllegalArgumentException(
                "Product not found"
        );
    }

    if (!product.isActive()) {

        throw new IllegalArgumentException(
                "Product is not active: "
                        + product.getId()
        );
    }

    if (product.getPrice() == null) {

        throw new IllegalArgumentException(
                "Product price is missing: "
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
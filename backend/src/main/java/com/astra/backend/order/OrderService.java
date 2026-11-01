
package com.astra.backend.order;

import com.astra.backend.cart.CartItem;
import com.astra.backend.cart.CartItemRepository;
import com.astra.backend.coupon.CouponService;
import com.astra.backend.order.dto.CheckoutRequest;
import com.astra.backend.order.dto.OrderItemResponse;
import com.astra.backend.order.dto.OrderResponse;
import com.astra.backend.order.dto.UpdateOrderStatusRequest;
import com.astra.backend.product.Product;
import com.astra.backend.product.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.astra.backend.event.publisher.AstraEventPublisher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class OrderService {

    private final CartItemRepository cartRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderItemRepository orderItemRepository;
    private final CouponService couponService;
    private final RazorpayPaymentService paymentService;
    private final AstraEventPublisher eventPublisher;

    public OrderService(
            CartItemRepository cartRepository,
            ProductRepository productRepository,
            CustomerOrderRepository orderRepository,
            CustomerOrderItemRepository orderItemRepository,
            CouponService couponService,
            RazorpayPaymentService paymentService,
            AstraEventPublisher eventPublisher) {

        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.couponService = couponService;
        this.paymentService = paymentService;
        this.eventPublisher = eventPublisher;
    }

    private String currentEmail() {
        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to access your orders");
        }

        return authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    // CUSTOMER: Checkout with idempotency protection.
    @Transactional
    public OrderResponse checkout(
            CheckoutRequest request,
            String idempotencyKey) {

        String email = currentEmail();

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Checkout request is required");
        }

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.trim().length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid Idempotency-Key header is required");
        }

        idempotencyKey = idempotencyKey.trim();

        // Calculate the fingerprint before checking for an existing order.
        String requestHash = createCheckoutRequestHash(request);

        Optional<CustomerOrder> existingOrder =
                orderRepository.findByUserEmailAndCheckoutIdempotencyKey(
                        email,
                        idempotencyKey);

        if (existingOrder.isPresent()) {
            CustomerOrder order = existingOrder.get();

            validateCheckoutRequestHash(order, requestHash);

            return buildOrderResponse(order);
        }

        List<CartItem> cartItems =
                cartRepository.findByUserEmailOrderByIdAsc(email);

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Your cart is empty");
        }

        // Validate cart items before acquiring product locks.
        for (CartItem cartItem : cartItems) {
            if (cartItem == null
                    || cartItem.getProduct() == null
                    || cartItem.getProduct().getId() == null
                    || cartItem.getQuantity() == null
                    || cartItem.getQuantity() < 1) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cart contains an invalid product or quantity");
            }
        }

        // Lock products in ascending ID order to reduce deadlock risk.
        List<Long> productIds = cartItems.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .sorted()
                .toList();

        Map<Long, Product> lockedProducts = new LinkedHashMap<>();

        for (Long productId : productIds) {
            Product product = productRepository
                    .findByIdForUpdate(productId)
                    .filter(p -> Boolean.TRUE.equals(p.getActive()))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A product in your cart is unavailable"));

            lockedProducts.put(productId, product);
        }

        CustomerOrder order = new CustomerOrder();

        order.setOrderNumber(
                "AST-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase(Locale.ROOT));

        order.setUserEmail(email);
        order.setCheckoutIdempotencyKey(idempotencyKey);
        order.setCheckoutRequestHash(requestHash);
        order.setCustomerName(request.customerName());
        order.setPhoneNumber(request.phoneNumber());
        order.setShippingAddress(request.shippingAddress());
        order.setOrderStatus("PLACED");
        String paymentMethod = request.paymentMethod() == null
                || request.paymentMethod().isBlank()
                ? "COD" : request.paymentMethod().trim().toUpperCase(Locale.ROOT);
        if (!List.of("COD", "RAZORPAY").contains(paymentMethod)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Choose a supported payment method");
        }
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING");

        BigDecimal subtotal = BigDecimal.ZERO;

        List<CustomerOrderItem> orderItems = new ArrayList<>();
        List<OrderItemResponse> responseItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Long productId = cartItem.getProduct().getId();
            int quantity = cartItem.getQuantity();

            Product product = lockedProducts.get(productId);

            if (product == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A product in your cart is unavailable");
            }

            Integer availableStock = product.getStockQuantity();

            if (availableStock == null || availableStock < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for product: "
                                + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();

            if (unitPrice == null || unitPrice.signum() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Invalid price for product: " + product.getName());
            }

            BigDecimal lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity));

            subtotal = subtotal.add(lineTotal);

            CustomerOrderItem orderItem = new CustomerOrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(quantity);
            orderItem.setLineTotal(lineTotal);

            orderItems.add(orderItem);

            responseItems.add(new OrderItemResponse(
                    product.getId(),
                    product.getName(),
                    unitPrice,
                    quantity,
                    lineTotal));

            // Product is locked; JPA persists this stock change
            // within the checkout transaction.
            product.setStockQuantity(availableStock - quantity);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        String couponCode = request.couponCode() == null
                ? null
                : request.couponCode()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        CouponService.CheckoutCoupon checkoutCoupon =
                couponCode == null || couponCode.isBlank()
                        ? null
                        : couponService.prepareCheckout(
                                couponCode, subtotal, email);
        BigDecimal discount = checkoutCoupon == null
                ? BigDecimal.ZERO
                : checkoutCoupon.discount();

        if (discount == null
                || discount.signum() < 0
                || discount.compareTo(subtotal) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The coupon discount is invalid");
        }

        discount = discount.setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setCouponCode(
                couponCode == null || couponCode.isBlank()
                        ? null
                        : couponCode);
        order.setDiscountAmount(discount);
        order.setTotalAmount(total);

        if ("RAZORPAY".equals(paymentMethod)) {
            order.setRazorpayOrderId(
                    paymentService.createOrder(total, order.getOrderNumber()));
        }

        // Flush here so a unique-key collision is detected before
        // saving order items and deleting the cart.
        CustomerOrder savedOrder = orderRepository.saveAndFlush(order);

        for (CustomerOrderItem orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
        }

        orderItemRepository.saveAll(orderItems);
        couponService.recordRedemption(checkoutCoupon, email, savedOrder);

        for (CartItem cartItem : cartItems) {
            cartRepository.deleteByUserEmailAndProductId(
                    email,
                    cartItem.getProduct().getId());
        }

        // Publish only after all checkout operations have succeeded.
        // Listener executes AFTER the transaction commits.
        eventPublisher.orderPlaced(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                email,
                savedOrder.getTotalAmount(),
                savedOrder.getPaymentMethod()
        );

        return toOrderResponse(savedOrder, responseItems);
    }

    private String createCheckoutRequestHash(CheckoutRequest request) {
        String customerName = normalizeHashValue(request.customerName());
        String phoneNumber = normalizeHashValue(request.phoneNumber());
        String shippingAddress =
                normalizeHashValue(request.shippingAddress());

        String couponCode = request.couponCode() == null
                ? ""
                : request.couponCode().trim().toUpperCase(Locale.ROOT);
        String paymentMethod = request.paymentMethod() == null
                || request.paymentMethod().isBlank()
                ? "COD" : request.paymentMethod().trim().toUpperCase(Locale.ROOT);

        String canonicalRequest =
                hashPart(customerName)
                        + hashPart(phoneNumber)
                        + hashPart(shippingAddress)
                        + hashPart(couponCode)
                        + hashPart(paymentMethod);

        try {
            byte[] hashBytes = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hashBytes.length * 2);

            for (byte value : hashBytes) {
                hex.append(String.format("%02x", value & 0xff));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception);
        }
    }

    private String normalizeHashValue(String value) {
        return value == null ? "" : value.trim();
    }

    private String hashPart(String value) {
        return value.length() + ":" + value;
    }

    private void validateCheckoutRequestHash(
            CustomerOrder order,
            String requestedHash) {

        String storedHash = order.getCheckoutRequestHash();

        if (storedHash == null || !storedHash.equals(requestedHash)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This checkout key was already used with a different "
                            + "request. Please start a new checkout.");
        }
    }

    private OrderResponse buildOrderResponse(CustomerOrder order) {
        List<OrderItemResponse> items =
                orderItemRepository.findByOrderId(order.getId())
                        .stream()
                        .map(item -> new OrderItemResponse(
                                item.getProduct().getId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getLineTotal()))
                        .toList();

        return toOrderResponse(order, items);
    }

    // CUSTOMER: Cancel an order.
    @Transactional
    public OrderResponse cancelOrder(String orderNumber) {
        String email = currentEmail();

        CustomerOrder order = orderRepository
                .findByOrderNumberAndUserEmailForUpdate(orderNumber, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"));

        if (!"PLACED".equalsIgnoreCase(order.getOrderStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only orders with PLACED status can be cancelled");
        }

        if ("RAZORPAY".equals(order.getPaymentMethod())
                && "PAID".equals(order.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A paid online order must be refunded before it can be cancelled. Please contact support.");
        }

        List<CustomerOrderItem> items =
                orderItemRepository.findByOrderId(order.getId());

        List<Long> productIds = items.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .sorted()
                .toList();

        Map<Long, Product> lockedProducts = new LinkedHashMap<>();

        for (Long productId : productIds) {
            Product product = productRepository
                    .findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A product associated with this order no longer exists"));

            lockedProducts.put(productId, product);
        }

        for (CustomerOrderItem item : items) {
            Long productId = item.getProduct().getId();
            Product product = lockedProducts.get(productId);

            Integer currentStock = product.getStockQuantity();
            Integer quantity = item.getQuantity();

            if (currentStock == null || quantity == null || quantity < 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Unable to restore stock for " + item.getProductName());
            }

            try {
                product.setStockQuantity(Math.addExact(currentStock, quantity));
            } catch (ArithmeticException ex) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock quantity overflow for " + item.getProductName());
            }
        }

        order.setOrderStatus("CANCELLED");

        CustomerOrder savedOrder = orderRepository.saveAndFlush(order);
        eventPublisher.orderCancelled(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getUserEmail()
        );
        List<OrderItemResponse> responseItems = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineTotal()))
                .toList();

        return toOrderResponse(savedOrder, responseItems);
    }

    @Transactional
    public OrderResponse verifyRazorpayPayment(
            String orderNumber,
            String razorpayOrderId,
            String razorpayPaymentId,
            String signature) {
        String email = currentEmail();
        CustomerOrder order = orderRepository
                .findByOrderNumberAndUserEmailForUpdate(orderNumber, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));
        if (!"RAZORPAY".equals(order.getPaymentMethod())
                || order.getRazorpayOrderId() == null
                || !order.getRazorpayOrderId().equals(razorpayOrderId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Payment does not match this order");
        }
        if ("PAID".equals(order.getPaymentStatus())) {
            if (!razorpayPaymentId.equals(order.getRazorpayPaymentId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Order was paid with another payment");
            }
            return buildOrderResponse(order);
        }
        if (!paymentService.isCapturedPayment(
                razorpayOrderId, razorpayPaymentId, signature)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment verification failed");
        }
        order.setRazorpayPaymentId(razorpayPaymentId);
        order.setPaymentStatus("PAID");

        CustomerOrder savedOrder = orderRepository.save(order);

        eventPublisher.paymentCompleted(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getUserEmail(),
                razorpayPaymentId,
                savedOrder.getTotalAmount()
        );

        return buildOrderResponse(savedOrder);  }

    @Transactional
    public void recordCapturedPayment(String razorpayOrderId, String paymentId) {
        CustomerOrder order = orderRepository
                .findByRazorpayOrderIdForUpdate(razorpayOrderId)
                .orElse(null);
        if (order == null) return;
        if ("PAID".equals(order.getPaymentStatus())) {
            if (paymentId.equals(order.getRazorpayPaymentId())) return;
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A different payment is already recorded for this order");
        }
        order.setRazorpayPaymentId(paymentId);
        order.setPaymentStatus("PAID");

        CustomerOrder savedOrder = orderRepository.save(order);

        eventPublisher.paymentCompleted(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getUserEmail(),
                paymentId,
                savedOrder.getTotalAmount()
        );
    }

    // CUSTOMER: Retrieve the logged-in customer's orders.
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        String email = currentEmail();

        return orderRepository
                .findByUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::buildOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        String email = currentEmail();
        return orderRepository
                .findByUserEmailOrderByCreatedAtDesc(email, pageable)
                .map(this::buildOrderResponse);
    }

    // CUSTOMER: Retrieve one of the logged-in customer's orders.
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNumber) {
        String email = currentEmail();

        CustomerOrder order = orderRepository
                .findByOrderNumberAndUserEmail(orderNumber, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"));

        return buildOrderResponse(order);
    }

    // ADMIN: Retrieve all orders.
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersForAdmin() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::buildOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countAllOrdersForAdmin() {
        return orderRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> searchAdminOrders(
            String query,
            String status,
            Pageable pageable) {
        String normalizedQuery = query == null || query.isBlank()
                ? null : query.trim();
        String normalizedStatus = status == null || status.isBlank()
                || "ALL".equalsIgnoreCase(status)
                ? null : status.trim().toUpperCase(Locale.ROOT);
        if (normalizedStatus != null && !List.of(
                "PLACED", "CONFIRMED", "SHIPPED", "OUT_FOR_DELIVERY",
                "DELIVERED", "CANCELLED").contains(normalizedStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid order status filter");
        }
        return orderRepository.searchAdminOrders(
                        normalizedQuery, normalizedStatus, pageable)
                .map(this::buildOrderResponse);
    }

    // ADMIN: Retrieve one order with its purchased items.
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailsForAdmin(String orderNumber) {
        CustomerOrder order = orderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"));

        return buildOrderResponse(order);
    }

    // ADMIN: Update order status using validated transitions.
    @Transactional
    public OrderResponse updateOrderStatus(
            String orderNumber,
            UpdateOrderStatusRequest request) {

        if (request == null
                || request.orderStatus() == null
                || request.orderStatus().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order status is required");
        }

        CustomerOrder order = orderRepository
                .findByOrderNumberForUpdate(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"));

        String currentStatus = order.getOrderStatus();
        String newStatus = request.orderStatus()
                .trim()
                .toUpperCase(Locale.ROOT);

        boolean validTransition = switch (currentStatus) {
            case "PLACED" -> newStatus.equals("CONFIRMED");
            case "CONFIRMED" -> newStatus.equals("SHIPPED");
            case "SHIPPED" -> newStatus.equals("OUT_FOR_DELIVERY");
            case "OUT_FOR_DELIVERY" -> newStatus.equals("DELIVERED");
            default -> false;
        };

        if (!validTransition) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid order status transition: "
                            + currentStatus + " -> " + newStatus);
        }

        if ("RAZORPAY".equals(order.getPaymentMethod())
                && !"PAID".equals(order.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An online order cannot be shipped until its payment is captured");
        }

        order.setOrderStatus(newStatus);
        CustomerOrder savedOrder = orderRepository.saveAndFlush(order);

        eventPublisher.orderStatusChanged(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                currentStatus,
                newStatus
        );

        return buildOrderResponse(savedOrder);
    }

    // Recovery lookup used after a checkout database conflict.
    // The request must be supplied so the fingerprint can be validated.
    @Transactional(readOnly = true)
    public Optional<OrderResponse> findExistingCheckout(
            String idempotencyKey,
            CheckoutRequest request) {

        String email = currentEmail();

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || request == null) {
            return Optional.empty();
        }

        Optional<CustomerOrder> existingOrder =
                orderRepository.findByUserEmailAndCheckoutIdempotencyKey(
                        email,
                        idempotencyKey.trim());

        if (existingOrder.isEmpty()) {
            return Optional.empty();
        }

        CustomerOrder order = existingOrder.get();

        validateCheckoutRequestHash(
                order,
                createCheckoutRequestHash(request));

        return Optional.of(buildOrderResponse(order));
    }

    // Retained for source compatibility with the current controller.
    // It deliberately does not return an order without validating the
    // original request payload.
    @Deprecated
    @Transactional(readOnly = true)
    public Optional<OrderResponse> findExistingCheckout(
            String idempotencyKey) {
        return Optional.empty();
    }

    // Convert an order and its items to the API response.
    private OrderResponse toOrderResponse(
            CustomerOrder order,
            List<OrderItemResponse> items) {

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getPhoneNumber(),
                order.getShippingAddress(),
                order.getOrderStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getSubtotal(),
                order.getCouponCode(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getCreatedAt() != null
                        ? order.getCreatedAt()
                        : LocalDateTime.now(java.time.ZoneOffset.UTC),
                items,
                order.getRazorpayOrderId(),
                "RAZORPAY".equals(order.getPaymentMethod())
                        ? paymentService.getPublicKeyOrEmpty() : null);
    }
}


package com.astra.order.service;

import com.astra.order.dto.*;
import com.astra.order.entity.*;
import com.astra.order.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class OrderWriter {

    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderItemRepository itemRepository;
    private final OrderOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OrderWriter(
            CustomerOrderRepository orderRepository,
            CustomerOrderItemRepository itemRepository,
            OrderOutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    
    @Transactional
    public OrderResponse create(
            String email,
            String idempotencyKey,
            String requestHash,
            CheckoutRequest request,
            CartResponse cart) {

        Optional<CustomerOrder> existing =
                orderRepository.findByUserEmailAndCheckoutIdempotencyKey(
                        email, idempotencyKey);

        if (existing.isPresent()) {
            return existingResponse(existing.get(), requestHash);
        }

        if (cart.items().isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty");
        }

        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setCheckoutIdempotencyKey(idempotencyKey);
        order.setCheckoutRequestHash(requestHash);
        order.setUserEmail(email);
        order.setCustomerName(request.customerName().trim());
        order.setPhoneNumber(request.phoneNumber().trim());
        order.setShippingAddress(request.shippingAddress().trim());
        order.setOrderStatus("PENDING_STOCK");
        order.setPaymentMethod("COD");
        order.setPaymentStatus("PENDING");

        BigDecimal subtotal = BigDecimal.ZERO;
        List<CustomerOrderItem> items = new ArrayList<>();
        List<OrderCreatedEvent.Item> eventItems = new ArrayList<>();

        Set<Long> seenProducts = new HashSet<>();

        for (CartItemResponse cartItem : cart.items()) {
            if (cartItem.productId() == null
                    || cartItem.quantity() == null
                    || cartItem.quantity() < 1
                    || cartItem.price() == null
                    || cartItem.price().signum() < 0
                    || cartItem.name() == null
                    || !seenProducts.add(cartItem.productId())) {
                throw new IllegalArgumentException(
                        "Cart contains an invalid or duplicate item");
            }

            BigDecimal unitPrice = cartItem.price()
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(cartItem.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);

            CustomerOrderItem item = new CustomerOrderItem();
            item.setOrder(order);
            item.setProductId(cartItem.productId());
            item.setProductName(cartItem.name());
            item.setUnitPrice(unitPrice);
            item.setQuantity(cartItem.quantity());
            item.setLineTotal(lineTotal);
            items.add(item);

            eventItems.add(new OrderCreatedEvent.Item(
                    cartItem.productId(), cartItem.quantity()));
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        // Coupon discounts are intentionally not applied until
        // Promotion Service is integrated and validates the coupon.
        if (request.couponCode() != null
                && !request.couponCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Coupon checkout will be available after Promotion Service integration");
        }

        order.setSubtotal(subtotal);
        order.setCouponCode(null);
        order.setDiscountAmount(BigDecimal.ZERO.setScale(2));
        order.setTotalAmount(subtotal);

        try {
            CustomerOrder saved = orderRepository.saveAndFlush(order);

            for (CustomerOrderItem item : items) {
                item.setOrder(saved);
            }
            itemRepository.saveAll(items);

            String eventId = UUID.randomUUID().toString();

            OrderCreatedEvent event = new OrderCreatedEvent(
                    saved.getOrderNumber(),
                    eventItems);

            OrderOutboxEvent outbox = new OrderOutboxEvent();
            outbox.setEventId(eventId);
            outbox.setAggregateType("ORDER");
            outbox.setAggregateId(saved.getOrderNumber());
            outbox.setEventType("ORDER_CREATED");
            outbox.setTopic("order.created");
            outbox.setPayload(serialize(event));
            outbox.setEventStatus("PENDING");
            outbox.setRetryCount(0);
            outboxRepository.save(outbox);

            return toResponse(saved, items);

        } catch (DataIntegrityViolationException ex) {
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public java.util.Optional<OrderResponse> findExisting(
            String email, String key, String requestHash) {

        return orderRepository
                .findByUserEmailAndCheckoutIdempotencyKey(email, key)
                .map(order -> existingResponse(order, requestHash));
    }
    private OrderResponse existingResponse(
            CustomerOrder order, String requestHash) {

        if (!Objects.equals(
                order.getCheckoutRequestHash(), requestHash)) {
            throw new IllegalStateException(
                    "This Idempotency-Key was already used with a different checkout request");
        }

        List<CustomerOrderItem> items =
                itemRepository.findByOrderId(order.getId());

        return toResponse(order, items);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Unable to serialize order event", ex);
        }
    }

    private String generateOrderNumber() {
        return "AST-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
    }

    private OrderResponse toResponse(
            CustomerOrder order, List<CustomerOrderItem> items) {

        List<OrderItemResponse> responseItems = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineTotal()))
                .toList();

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
                order.getCreatedAt(),
                responseItems);
    }
}

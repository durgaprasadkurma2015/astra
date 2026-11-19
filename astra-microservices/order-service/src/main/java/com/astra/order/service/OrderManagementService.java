
package com.astra.order.service;

import com.astra.order.client.InventoryClient;
import com.astra.order.dto.*;
import com.astra.order.entity.*;
import com.astra.order.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Locale;

@Service
public class OrderManagementService {

    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderItemRepository itemRepository;
    private final InventoryClient inventoryClient;
    private final OrderCancellationWriter cancellationWriter;

    public OrderManagementService(
            CustomerOrderRepository orderRepository,
            CustomerOrderItemRepository itemRepository,
            InventoryClient inventoryClient,
            OrderCancellationWriter cancellationWriter
            ) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.inventoryClient = inventoryClient;
        this.cancellationWriter = cancellationWriter;
    }

    private String currentEmail() {
        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to access your orders");
        }

        return authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        return orderRepository
                .findByUserEmailOrderByCreatedAtDesc(currentEmail())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(String orderNumber) {
        CustomerOrder order = orderRepository
                .findByOrderNumberAndUserEmail(
                        orderNumber, currentEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getAdminOrder(String orderNumber) {
        CustomerOrder order = orderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        return toResponse(order);
    }

    public OrderResponse cancelMyOrder(String orderNumber) {
        String email = currentEmail();

        CustomerOrder order = orderRepository
                .findByOrderNumberAndUserEmail(orderNumber, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        // Cancellation of a pending reservation is deliberately rejected.
        // It needs a coordinated cancellation event in Inventory Service.
        if (!"CONFIRMED".equals(order.getOrderStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only CONFIRMED orders can currently be cancelled");
        }

        // Release inventory before marking the order as cancelled.
        // If Inventory Service fails, the order remains CONFIRMED.
        inventoryClient.releaseReservation(orderNumber);

        return cancellationWriter.cancel(orderNumber, email);
    }

    @Transactional
    public OrderResponse updateCancelled(
            String orderNumber, String email) {

        CustomerOrder order = orderRepository
                .findByOrderNumberAndUserEmailForUpdate(
                        orderNumber, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if (!"CONFIRMED".equals(order.getOrderStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order status changed; cancellation is not allowed");
        }

        order.setOrderStatus("CANCELLED");
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(
            String orderNumber,
            UpdateOrderStatusRequest request) {

        String newStatus = request.orderStatus()
                .trim()
                .toUpperCase(Locale.ROOT);

        CustomerOrder order = orderRepository
                .findByOrderNumberForUpdate(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        String current = order.getOrderStatus();

        boolean allowed = switch (current) {
            case "CONFIRMED" -> newStatus.equals("SHIPPED");
            case "SHIPPED" -> newStatus.equals("OUT_FOR_DELIVERY");
            case "OUT_FOR_DELIVERY" -> newStatus.equals("DELIVERED");
            default -> false;
        };

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid order status transition: "
                            + current + " -> " + newStatus);
        }

        order.setOrderStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(CustomerOrder order) {
        List<OrderItemResponse> items =
                itemRepository.findByOrderId(order.getId())
                        .stream()
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
                items);
    }
}

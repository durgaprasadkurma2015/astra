
package com.astra.order.service;

import com.astra.order.dto.*;
import com.astra.order.entity.CustomerOrder;
import com.astra.order.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderCancellationWriter {

    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderItemRepository itemRepository;

    public OrderCancellationWriter(
            CustomerOrderRepository orderRepository,
            CustomerOrderItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public OrderResponse cancel(String orderNumber, String email) {
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
        CustomerOrder saved = orderRepository.save(order);

        List<OrderItemResponse> items =
                itemRepository.findByOrderId(saved.getId())
                        .stream()
                        .map(item -> new OrderItemResponse(
                                item.getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getLineTotal()))
                        .toList();

        return new OrderResponse(
                saved.getId(),
                saved.getOrderNumber(),
                saved.getCustomerName(),
                saved.getPhoneNumber(),
                saved.getShippingAddress(),
                saved.getOrderStatus(),
                saved.getPaymentMethod(),
                saved.getPaymentStatus(),
                saved.getSubtotal(),
                saved.getCouponCode(),
                saved.getDiscountAmount(),
                saved.getTotalAmount(),
                saved.getCreatedAt(),
                items);
    }
}

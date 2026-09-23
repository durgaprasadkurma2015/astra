package com.astra.admin.service;

import com.astra.dto.OrderItemResponse;
import com.astra.dto.AdminOrderSummaryResponse;
import com.astra.entity.Order;
import com.astra.enums.OrderStatus;
import com.astra.repository.OrderRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AdminOrderService {

    private final OrderRepository orderRepository;

    public AdminOrderService(
            OrderRepository orderRepository
    ) {
        this.orderRepository = orderRepository;
    }

    public Page<AdminOrderSummaryResponse> getOrders(
            OrderStatus status,
            Pageable pageable
    ) {

        Page<Order> orders;

        if (status != null) {

            orders =
                    orderRepository
                            .findByStatusOrderByCreatedAtDesc(
                                    status,
                                    pageable
                            );

        } else {

            orders =
                    orderRepository
                            .findAllByOrderByCreatedAtDesc(
                                    pageable
                            );
        }

        return orders.map(this::toResponse);
    }

    public AdminOrderSummaryResponse getOrder(
            Long orderId
    ) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        return toResponse(order);
    }

    public AdminOrderSummaryResponse updateStatus(
            Long orderId,
            OrderStatus status
    ) {

        if (status == null) {

            throw new IllegalArgumentException(
                    "Order status is required"
            );
        }

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        validateStatusTransition(
                order.getStatus(),
                status
        );

        order.setStatus(status);

        return toResponse(
                orderRepository.save(order)
        );
    }

    private void validateStatusTransition(
            OrderStatus current,
            OrderStatus next
    ) {

        if (current == next) {
            return;
        }

        if (current == OrderStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cancelled order cannot change status"
            );
        }

        if (current == OrderStatus.DELIVERED) {

            throw new IllegalArgumentException(
                    "Delivered order cannot change status"
            );
        }

        if (current == OrderStatus.RETURNED) {

            throw new IllegalArgumentException(
                    "Returned order cannot change status"
            );
        }

        if (current == OrderStatus.PENDING
                && next != OrderStatus.CONFIRMED
                && next != OrderStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Invalid order status transition"
            );
        }

        if (current == OrderStatus.CONFIRMED
                && next != OrderStatus.PROCESSING
                && next != OrderStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Invalid order status transition"
            );
        }

        if (current == OrderStatus.PROCESSING
                && next != OrderStatus.SHIPPED
                && next != OrderStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Invalid order status transition"
            );
        }

        if (current == OrderStatus.SHIPPED
                && next != OrderStatus.OUT_FOR_DELIVERY) {

            throw new IllegalArgumentException(
                    "Invalid order status transition"
            );
        }

        if (current == OrderStatus.OUT_FOR_DELIVERY
                && next != OrderStatus.DELIVERED) {

            throw new IllegalArgumentException(
                    "Invalid order status transition"
            );
        }

        if (current == OrderStatus.RETURN_REQUESTED
                && next != OrderStatus.RETURNED) {

            throw new IllegalArgumentException(
                    "Invalid order status transition"
            );
        }
    }

    private AdminOrderSummaryResponse toResponse(
            Order order
    ) {

        String customerEmail =
                order.getUser() != null
                        ? order.getUser().getEmail()
                        : null;

        return new AdminOrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                customerEmail,
                order.getTotal(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                order.getCreatedAt()
        );
    }
}

package com.astra.order.controller;

import com.astra.enums.OrderStatus;
import com.astra.order.dto.CreateOrderRequest;
import com.astra.order.dto.OrderResponse;
import com.astra.order.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService
    ) {
        this.orderService = orderService;
    }

    /**
     * Create a new order from the current user's cart.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request
    ) {

        OrderResponse response =
                orderService.createOrder(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get current user's orders.
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            Authentication authentication,
            Pageable pageable
    ) {

        Page<OrderResponse> response =
                orderService.getOrders(
                        authentication.getName(),
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Get one order by ID.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        OrderResponse response =
                orderService.getOrder(
                        authentication.getName(),
                        orderId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an order.
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        OrderResponse response =
                orderService.cancelOrder(
                        authentication.getName(),
                        orderId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Get order status.
     */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderStatus> getStatus(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        OrderStatus status =
                orderService.getStatus(
                        authentication.getName(),
                        orderId
                );

        return ResponseEntity.ok(status);
    }
}

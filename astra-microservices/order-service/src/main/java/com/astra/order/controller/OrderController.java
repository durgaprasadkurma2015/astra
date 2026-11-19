
package com.astra.order.controller;

import com.astra.order.dto.CheckoutRequest;
import com.astra.order.dto.OrderResponse;
import com.astra.order.service.CheckoutService;
import com.astra.order.service.OrderManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderManagementService orderService;

    public OrderController(
            CheckoutService checkoutService,
            OrderManagementService orderService) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Principal principal) {

        return checkoutService.checkout(
                principal.getName(), idempotencyKey, request);
    }

    @GetMapping("/my-orders")
    public List<OrderResponse> getMyOrders() {
        return orderService.getMyOrders();
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse getOrder(
            @PathVariable String orderNumber) {
        return orderService.getMyOrder(orderNumber);
    }

    @PutMapping("/{orderNumber}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable String orderNumber) {
        return orderService.cancelMyOrder(orderNumber);
    }
}


package com.astra.order.controller;

import com.astra.order.dto.OrderResponse;
import com.astra.order.dto.UpdateOrderStatusRequest;
import com.astra.order.service.OrderManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderManagementService orderService;

    public AdminOrderController(
            OrderManagementService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse getOrder(
            @PathVariable String orderNumber) {
        return orderService.getAdminOrder(orderNumber);
    }

    @PutMapping("/{orderNumber}/status")
    public OrderResponse updateStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(orderNumber, request);
    }
}

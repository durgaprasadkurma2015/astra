
package com.astra.backend.order;

import com.astra.backend.order.dto.OrderResponse;
import com.astra.backend.order.dto.UpdateOrderStatusRequest;
import com.astra.backend.order.dto.AdminOrderSummaryResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // GET all orders for the admin.
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrdersForAdmin();
    }

    @GetMapping("/summary")
    public AdminOrderSummaryResponse getOrderSummary() {
        return new AdminOrderSummaryResponse(
                orderService.countAllOrdersForAdmin());
    }

    @GetMapping("/page")
    public Page<OrderResponse> searchOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        return orderService.searchAdminOrders(
                query, status,
                PageRequest.of(Math.max(0, page),
                        Math.min(50, Math.max(1, size)),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // GET details of one order, including purchased items.
    @GetMapping("/{orderNumber}")
    public OrderResponse getOrderDetails(
            @PathVariable String orderNumber) {
        return orderService.getOrderDetailsForAdmin(orderNumber);
    }

    // Update an order's delivery status.
    @PutMapping("/{orderNumber}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        return orderService.updateOrderStatus(orderNumber, request);
    }
}

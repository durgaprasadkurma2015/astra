package com.astra.admin.controller;

import com.astra.admin.service.AdminOrderService;
import com.astra.dto.AdminOrderSummaryResponse;
import com.astra.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService orderService;

    public AdminOrderController(
            AdminOrderService orderService
    ) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminOrderSummaryResponse>> getOrders(
            @RequestParam(required = false)
            OrderStatus status,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                orderService.getOrders(
                        status,
                        pageable
                )
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderSummaryResponse> getOrder(
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(
                orderService.getOrder(orderId)
        );
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<AdminOrderSummaryResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    ) {

        return ResponseEntity.ok(
                orderService.updateStatus(
                        orderId,
                        status
                )
        );
    }
}

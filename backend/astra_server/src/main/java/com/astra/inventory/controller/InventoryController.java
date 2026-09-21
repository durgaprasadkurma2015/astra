package com.astra.inventory.controller;

import com.astra.dto.InventoryMovementResponse;
import com.astra.inventory.dto.InventoryAdjustmentRequest;
import com.astra.inventory.dto.InventoryResponse;
import com.astra.inventory.service.InventoryService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService) {

        this.inventoryService = inventoryService;
    }

    @GetMapping("/products/{productId}")
    public InventoryResponse getInventory(
            @PathVariable Long productId) {

        return inventoryService.getInventory(productId);
    }

    @GetMapping("/products/{productId}/movements")
    public Page<InventoryMovementResponse> getProductMovements(
            @PathVariable Long productId,
            Pageable pageable) {

        return inventoryService.getProductMovements(
                productId,
                pageable
        );
    }

    @GetMapping("/orders/{orderId}/movements")
    public Page<InventoryMovementResponse> getOrderMovements(
            @PathVariable Long orderId,
            Pageable pageable) {

        return inventoryService.getOrderMovements(
                orderId,
                pageable
        );
    }

    @PostMapping("/products/{productId}/restock")
    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse restock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryAdjustmentRequest request) {

        return inventoryService.restock(
                productId,
                request.quantity(),
                request.reason()
        );
    }

    @PutMapping("/products/{productId}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse adjust(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryAdjustmentRequest request) {

        return inventoryService.adjust(
                productId,
                request
        );
    }
}
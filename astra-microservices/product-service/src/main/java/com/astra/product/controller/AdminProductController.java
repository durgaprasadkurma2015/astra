package com.astra.product.controller;

import com.astra.product.dto.ProductRequest;
import com.astra.product.dto.ProductResponse;
import com.astra.product.service.ProductManagementService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final ProductManagementService service;

    public AdminProductController(ProductManagementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(service.getAllProducts());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ProductResponse> updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusRequest request) {

        return ResponseEntity.ok(
            service.updateStatus(id, request.active())
        );
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {

        return ResponseEntity.ok(
            service.updateStock(id, request.stockQuantity())
        );
    }

    public record ProductStatusRequest(
        @NotNull Boolean active
    ) {}

    public record StockUpdateRequest(
        @NotNull @PositiveOrZero Integer stockQuantity
    ) {}
}
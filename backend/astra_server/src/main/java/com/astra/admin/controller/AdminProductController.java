package com.astra.admin.controller;

import com.astra.admin.dto.AdminProductResponse;
import com.astra.admin.service.AdminProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService productService;

    public AdminProductController(
            AdminProductService productService
    ) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminProductResponse>> getProducts(
            @RequestParam(required = false)
            Boolean active,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                productService.getProducts(
                        active,
                        pageable
                )
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<AdminProductResponse> getProduct(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                productService.getProduct(productId)
        );
    }

    @PutMapping("/{productId}/activate")
    public ResponseEntity<AdminProductResponse> activate(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                productService.activate(productId)
        );
    }

    @PutMapping("/{productId}/deactivate")
    public ResponseEntity<AdminProductResponse> deactivate(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                productService.deactivate(productId)
        );
    }

    @PutMapping("/{productId}/featured")
    public ResponseEntity<AdminProductResponse> setFeatured(
            @PathVariable Long productId,
            @RequestParam boolean featured
    ) {

        return ResponseEntity.ok(
                productService.setFeatured(
                        productId,
                        featured
                )
        );
    }

    @PostMapping("/{productId}/inventory/restock")
    public ResponseEntity<AdminProductResponse> restock(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String reason
    ) {

        return ResponseEntity.ok(
                productService.restock(
                        productId,
                        quantity,
                        reason
                )
        );
    }

    @PutMapping("/{productId}/inventory/adjust")
    public ResponseEntity<AdminProductResponse> adjustInventory(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String reason
    ) {

        return ResponseEntity.ok(
                productService.adjustInventory(
                        productId,
                        quantity,
                        reason
                )
        );
    }
}

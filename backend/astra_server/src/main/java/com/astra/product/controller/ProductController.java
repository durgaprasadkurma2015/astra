package com.astra.product.controller;

import com.astra.product.dto.ProductRequest;
import com.astra.product.dto.ProductResponse;
import com.astra.product.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            @RequestParam(required = false)
            Long categoryId) {

        return ResponseEntity.ok(
                service.getProducts(
                        page,
                        size,
                        categoryId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(
                service.create(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(
                service.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> search(

            @RequestParam String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        return ResponseEntity.ok(
                service.search(
                        keyword,
                        page,
                        size
                )
        );
    }

    @GetMapping("/featured")
    public ResponseEntity<Page<ProductResponse>> featured(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        return ResponseEntity.ok(
                service.featured(page, size)
        );
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<Page<ProductResponse>> bestSellers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        return ResponseEntity.ok(
                service.bestSellers(page, size)
        );
    }

    @GetMapping("/top-rated")
    public ResponseEntity<Page<ProductResponse>> topRated(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        return ResponseEntity.ok(
                service.topRated(page, size)
        );
    }
}
package com.astra.backend.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductRepository productRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS =
        Set.of("id", "name", "price", "createdAt");

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<ProductResponse> getProducts(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String subcategory
    ) {
        if (category != null && !category.isBlank()
                && subcategory != null && !subcategory.isBlank()) {
            return productRepository
                .findByActiveTrueAndCategoryIgnoreCaseAndSubcategoryIgnoreCaseOrderByIdAsc(
                    category.trim(),
                    subcategory.trim()
                ).stream().map(ProductResponse::from).toList();
        }

        if (category != null && !category.isBlank()) {
            return productRepository
                .findByActiveTrueAndCategoryIgnoreCaseOrderByIdAsc(
                    category.trim()
                ).stream().map(ProductResponse::from).toList();
        }

        return productRepository.findByActiveTrueOrderByIdAsc()
            .stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String subcategory,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(defaultValue = "false") boolean inStock,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String direction
    ) {
        if (page < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 50) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page size must be between 1 and 50"
            );
        }

        if (minPrice != null && minPrice.signum() < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Minimum price cannot be negative"
            );
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Maximum price cannot be negative"
            );
        }

        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Minimum price cannot exceed maximum price"
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported sort field"
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Direction must be asc or desc"
            );
        }

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(sortDirection, sortBy)
        );

        return productRepository.searchActiveProducts(
            clean(keyword),
            clean(category),
            clean(subcategory),
            minPrice,
            maxPrice,
            inStock,
            pageable
        ).map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found"
            ));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found"
            );
        }

        return ProductResponse.from(product);
    }

    @GetMapping("/new-arrivals")
    public List<ProductResponse> getNewArrivals() {
        return productRepository.findByActiveTrueOrderByCreatedAtDesc()
            .stream()
            .limit(8)
            .map(ProductResponse::from)
            .toList();
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return productRepository.findActiveCategories();
    }

    @GetMapping("/subcategories")
    public List<String> getSubcategories(
        @RequestParam String category
    ) {
        if (category == null || category.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Category is required"
            );
        }

        return productRepository.findActiveSubcategories(category.trim());
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

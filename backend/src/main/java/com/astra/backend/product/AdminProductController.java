package com.astra.backend.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.astra.backend.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final InventoryStockMovementRepository movementRepository;
    private final AuditService auditService;

    public AdminProductController(ProductRepository productRepository,
            InventoryStockMovementRepository movementRepository,
            AuditService auditService) {
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
        this.auditService = auditService;
    }

    // GET: Return all products, including inactive products.
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAllByOrderByIdAsc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    // POST: Create a product.
    @PostMapping
    @Transactional
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        validateSku(request.sku(), null);

        Product product = new Product();
        applyRequest(product, request);

        Product savedProduct = productRepository.save(product);
        if (savedProduct.getStockQuantity() > 0) {
            recordStockMovement(savedProduct, authentication.getName(),
                    savedProduct.getStockQuantity(), "Initial product stock", httpRequest);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponse.from(savedProduct));
    }

    // PUT: Update an existing product.
    @PutMapping("/{id}")
    @Transactional
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        validateSku(request.sku(), id);

        int previousStock = product.getStockQuantity();
        if (request.stockQuantity() != previousStock
                && (request.stockAdjustmentReason() == null
                    || request.stockAdjustmentReason().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide a reason for changing stock");
        }

        applyRequest(product, request);
        int stockDelta = product.getStockQuantity() - previousStock;
        if (stockDelta != 0) {
            recordStockMovement(product, authentication.getName(), stockDelta,
                    request.stockAdjustmentReason(), httpRequest);
        }

        return ProductResponse.from(productRepository.save(product));
    }

    // PATCH: Activate or deactivate a product.
    @PatchMapping("/{id}/active")
    @Transactional
    public ProductResponse updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        product.setActive(request.active());

        return ProductResponse.from(productRepository.save(product));
    }

    private void applyRequest(
            Product product,
            ProductRequest request) {

        product.setName(request.name().trim());
        product.setSku(clean(request.sku()));
        product.setDescription(clean(request.description()));
        product.setCategory(clean(request.category()));
        product.setSubcategory(clean(request.subcategory()));
        product.setImageUrl(clean(request.imageUrl()));
        product.setPrice(request.price());

        product.setDiscountPercentage(
                request.discountPercentage() == null
                        ? BigDecimal.ZERO
                        : request.discountPercentage());

        product.setStockQuantity(request.stockQuantity());

        // Keep a predictable initial state for new products.
        if (product.getId() == null) {
            product.setActive(true);
        }

        List<ProductVariant> variants = new ArrayList<>();

        if (request.variants() != null) {
            for (VariantRequest variant : request.variants()) {
                variants.add(new ProductVariant(
                        clean(variant.sku()),
                        clean(variant.size()),
                        clean(variant.color()),
                        variant.stockQuantity()));
            }
        }

        product.setVariants(variants);
    }

    private void validateSku(String sku, Long currentProductId) {
        String normalizedSku = clean(sku);

        if (normalizedSku == null) {
            return;
        }

        boolean duplicate = productRepository.findAll()
                .stream()
                .filter(product -> product.getSku() != null)
                .filter(product -> normalizedSku.equalsIgnoreCase(
                        product.getSku().trim()))
                .anyMatch(product ->
                        !Objects.equals(
                                product.getId(),
                                currentProductId));

        if (duplicate) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product with this SKU already exists");
        }
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public record ProductRequest(
            @NotBlank(message = "Product name is required")
            String name,

            String sku,

            String description,

            String category,

            String subcategory,

            String imageUrl,

            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.00", message = "Price cannot be negative")
            BigDecimal price,

            @DecimalMin(value = "0.00")
            @DecimalMax(value = "100.00")
            BigDecimal discountPercentage,

            @NotNull(message = "Stock quantity is required")
            @PositiveOrZero(message = "Stock cannot be negative")
            Integer stockQuantity,

            @Valid
            List<VariantRequest> variants,

            @Size(max = 255)
            String stockAdjustmentReason
    ) {
    }

    public record VariantRequest(
            String sku,
            String size,
            String color,

            @NotNull(message = "Variant stock quantity is required")
            @PositiveOrZero(message = "Variant stock cannot be negative")
            Integer stockQuantity
    ) {
    }

    public record ProductStatusRequest(
            @NotNull(message = "Active status is required")
            Boolean active
    ) {
    }

    public record StockUpdateRequest(
            @jakarta.validation.constraints.NotNull(
                    message = "Stock quantity is required")
            @jakarta.validation.constraints.PositiveOrZero(
                    message = "Stock cannot be negative")
            Integer stockQuantity,

            @NotBlank(message = "An adjustment reason is required")
            @Size(max = 255)
            String reason
    ) {}

    @PatchMapping("/{id}/stock")
    @Transactional
    public ProductResponse updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        int delta = request.stockQuantity() - product.getStockQuantity();
        product.setStockQuantity(request.stockQuantity());
        Product saved = productRepository.save(product);
        if (delta != 0) {
            recordStockMovement(saved, authentication.getName(), delta,
                    request.reason(), httpRequest);
        }
        return ProductResponse.from(saved);
    }

    @GetMapping("/{id}/stock-movements")
    @Transactional(readOnly = true)
    public Page<InventoryStockMovementResponse> getStockMovements(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100");
        }
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return movementRepository
                .findByProductIdOrderByCreatedAtDesc(id, PageRequest.of(page, size))
                .map(InventoryStockMovementResponse::from);
    }

    private void recordStockMovement(Product product, String actorEmail,
            int delta, String reason, HttpServletRequest request) {
        String actor = actorEmail.trim().toLowerCase(java.util.Locale.ROOT);
        movementRepository.save(new InventoryStockMovement(product, actor,
                delta, product.getStockQuantity(), reason.trim()));
        auditService.log(actorEmail, "INVENTORY_ADJUSTMENT", "SUCCESS",
                "productId=" + product.getId() + ", delta=" + delta
                        + ", reason=" + reason.trim(),
                request.getRemoteAddr());
    }

}

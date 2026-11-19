package com.astra.product.service;

import com.astra.product.dto.ProductRequest;
import com.astra.product.dto.ProductResponse;
import com.astra.product.entity.Product;
import com.astra.product.entity.ProductVariant;
import com.astra.product.repository.ProductRepository;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ProductManagementService {

    private final ProductRepository repository;

    public ProductManagementService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts(
            String category,
            String subcategory) {

        List<Product> products;

        if (hasText(category) && hasText(subcategory)) {
            products = repository
                .findByActiveTrueAndCategoryIgnoreCaseAndSubcategoryIgnoreCaseOrderByIdAsc(
                    category.trim(), subcategory.trim()
                );
        } else if (hasText(category)) {
            products = repository
                .findByActiveTrueAndCategoryIgnoreCaseOrderByIdAsc(
                    category.trim()
                );
        } else {
            products = repository.findByActiveTrueOrderByIdAsc();
        }

        return products.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getActiveProduct(Long id) {
        Product product = repository.findWithVariantsById(id)
            .orElseThrow(() -> notFound(id));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw notFound(id);
        }

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getNewArrivals() {
        return repository.findByActiveTrueOrderByCreatedAtDesc()
            .stream()
            .limit(8)
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return repository.findActiveCategories();
    }

    @Transactional(readOnly = true)
    public List<String> getSubcategories(String category) {
        if (!hasText(category)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Category is required"
            );
        }

        return repository.findActiveSubcategories(category.trim());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(
            String keyword,
            String category,
            String subcategory,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            boolean inStock,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page must be >= 0 and size must be between 1 and 50"
            );
        }

        if (minPrice != null && minPrice.signum() < 0
                || maxPrice != null && maxPrice.signum() < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Price cannot be negative"
            );
        }

        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Minimum price cannot exceed maximum price"
            );
        }

        if (!List.of("id", "name", "price", "createdAt").contains(sortBy)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported sort field"
            );
        }

        Sort sort;
        try {
            sort = Sort.by(
                Sort.Direction.fromString(direction),
                sortBy
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Direction must be asc or desc"
            );
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        return repository.searchActiveProducts(
            clean(keyword),
            clean(category),
            clean(subcategory),
            minPrice,
            maxPrice,
            inStock,
            pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return repository.findAllByOrderByIdAsc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public ProductResponse create(ProductRequest request) {
        validateSku(request.sku(), null);

        Product product = new Product();
        applyRequest(product, request);
        product.setActive(true);

        return toResponse(repository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = repository.findWithVariantsById(id)
            .orElseThrow(() -> notFound(id));

        validateSku(request.sku(), id);
        applyRequest(product, request);

        return toResponse(repository.save(product));
    }

    public ProductResponse updateStatus(Long id, Boolean active) {
        Product product = repository.findWithVariantsById(id)
            .orElseThrow(() -> notFound(id));

        product.setActive(active);

        return toResponse(repository.save(product));
    }

    public ProductResponse updateStock(Long id, Integer stockQuantity) {
        Product product = repository.findWithVariantsById(id)
            .orElseThrow(() -> notFound(id));

        product.setStockQuantity(stockQuantity);

        return toResponse(repository.save(product));
    }

    private void applyRequest(Product product, ProductRequest request) {
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
                : request.discountPercentage()
        );
        product.setStockQuantity(request.stockQuantity());

        List<ProductVariant> variants = new ArrayList<>();

        if (request.variants() != null) {
            for (ProductRequest.VariantRequest variant : request.variants()) {
                variants.add(new ProductVariant(
                    clean(variant.sku()),
                    clean(variant.size()),
                    clean(variant.color()),
                    variant.stockQuantity()
                ));
            }
        }

        product.setVariants(variants);
    }

    private void validateSku(String sku, Long currentId) {
        if (!hasText(sku)) {
            return;
        }

        String normalized = sku.trim();

        boolean duplicate = currentId == null
            ? repository.existsBySkuIgnoreCase(normalized)
            : repository.existsBySkuIgnoreCaseAndIdNot(normalized, currentId);

        if (duplicate) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A product with this SKU already exists"
            );
        }
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
            p.getId(),
            p.getName(),
            p.getSku(),
            p.getDescription(),
            p.getCategory(),
            p.getSubcategory(),
            p.getImageUrl(),
            p.getPrice(),
            p.getDiscountPercentage(),
            p.getDiscountedPrice(),
            p.getStockQuantity(),
            p.getActive(),
            List.copyOf(p.getVariants()),
            p.getCreatedAt()
        );
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Product not found: " + id
        );
    }

    private String clean(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
package com.astra.product.service;

import com.astra.dto.ProductImageResponse;
import com.astra.entity.Category;
import com.astra.entity.Product;
import com.astra.exception.ApiException;
import com.astra.product.dto.ProductRequest;
import com.astra.product.dto.ProductResponse;
import com.astra.repository.CategoryRepository;
import com.astra.repository.ProductImageRepository;
import com.astra.repository.ProductInventoryRepository;
import com.astra.repository.ProductRepository;
import com.astra.event.ProductEventPublisher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final ProductEventPublisher eventPublisher;


    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductImageRepository productImageRepository,
            ProductInventoryRepository productInventoryRepository,
            ProductEventPublisher eventPublisher) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.productInventoryRepository = productInventoryRepository;
        this.eventPublisher = eventPublisher;
    }


    public Page<ProductResponse> getProducts(
            int page,
            int size,
            Long categoryId) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Product> products;

        if (categoryId != null) {

            products =
                    productRepository.findByCategoryIdAndActiveTrue(
                            categoryId,
                            pageable
                    );

        } else {

            products =
                    productRepository.findByActiveTrue(pageable);
        }

        return products.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames="products", key="#id")
    public ProductResponse getById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Product not found."
                        )
                );

        return toResponse(product);
    }

    @Transactional
    @CacheEvict(cacheNames={"products","productLists"}, allEntries=true)
    public ProductResponse create(ProductRequest request) {

        Category category =
                categoryRepository.findById(request.categoryId())
                        .orElseThrow(() ->
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Category not found."
                                )
                        );

        String slug = createSlug(request.name());

        Product product = Product.builder()
                .name(request.name().trim())
                .slug(slug)
                .sku(request.sku())
                .shortDescription(request.shortDescription())
                .description(request.description())
                .price(request.price())
                .discountPrice(request.discountPrice())
                .stockQuantity(request.stockQuantity())
                .active(request.active())
                .featured(request.featured())
                .category(category)
                .thumbnailUrl(request.thumbnailUrl())
                .rating(0.0)
                .reviewCount(0L)
                .salesCount(0L)
                .build();

        Product saved = productRepository.save(product);
        eventPublisher.publish("CREATED", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames={"products","productLists"}, allEntries=true)
    public ProductResponse update(
            Long id,
            ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Product not found."
                        )
                );

        Category category =
                categoryRepository.findById(request.categoryId())
                        .orElseThrow(() ->
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Category not found."
                                )
                        );

        product.setName(request.name().trim());
        product.setSlug(createSlug(request.name()));
        product.setSku(request.sku());
        product.setShortDescription(request.shortDescription());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setDiscountPrice(request.discountPrice());
        product.setStockQuantity(request.stockQuantity());
        product.setActive(request.active());
        product.setFeatured(request.featured());
        product.setCategory(category);
        product.setThumbnailUrl(request.thumbnailUrl());

        Product saved = productRepository.save(product);
        eventPublisher.publish("UPDATED", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames={"products","productLists"}, allEntries=true)
    public void delete(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Product not found."
                        )
                );

        product.setActive(false);

        productRepository.save(product);
        eventPublisher.publish("DELETED", product.getId());
    }

    public Page<ProductResponse> search(
            String keyword,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size
        );

        return productRepository
                .search(keyword, pageable)
                .map(this::toResponse);
    }

    public Page<ProductResponse> featured(
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        return productRepository
                .findByFeaturedTrueAndActiveTrue(pageable)
                .map(this::toResponse);
    }

    public Page<ProductResponse> bestSellers(
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        return productRepository
                .findByActiveTrueOrderBySalesCountDesc(pageable)
                .map(this::toResponse);
    }

    public Page<ProductResponse> topRated(
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        return productRepository
                .findByActiveTrueOrderByRatingDesc(pageable)
                .map(this::toResponse);
    }

    private String createSlug(String value) {

        return value
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private ProductResponse toResponse(Product product) {

        List<ProductImageResponse> images =
                productImageRepository
                        .findByProductIdOrderByDisplayOrderAsc(
                                product.getId()
                        )
                        .stream()
                        .map(image -> new ProductImageResponse(
                                image.getId(),
                                image.getProduct().getId(),
                                image.getImageUrl(),
                                image.getOriginalFileName(),
                                image.getStoredFileName(),
                                image.getContentType(),
                                image.getFileSize(),
                                image.isPrimaryImage(),
                                image.getDisplayOrder()
                        ))
                        .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getShortDescription(),
                product.getDescription(),
                product.getPrice(),
                product.getDiscountPrice(),
                product.getStockQuantity(),
                product.isActive(),
                product.isFeatured(),
                product.getRating(),
                product.getReviewCount(),
                product.getSalesCount(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getThumbnailUrl(),
                images
        );
    }
}

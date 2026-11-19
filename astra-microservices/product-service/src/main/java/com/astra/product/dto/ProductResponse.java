package com.astra.product.dto;

import com.astra.product.entity.ProductVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
    Long id,
    String name,
    String sku,
    String description,
    String category,
    String subcategory,
    String imageUrl,
    BigDecimal price,
    BigDecimal discountPercentage,
    BigDecimal discountedPrice,
    Integer stockQuantity,
    Boolean active,
    List<ProductVariant> variants,
    LocalDateTime createdAt
) {}
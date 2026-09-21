package com.astra.product.dto;

import java.math.BigDecimal;
import java.util.List;

import com.astra.dto.ProductImageResponse;

public record ProductResponse(

        Long id,

        String name,

        String slug,

        String sku,

        String shortDescription,

        String description,

        BigDecimal price,

        BigDecimal discountPrice,

        Integer stockQuantity,

        boolean active,

        boolean featured,

        Double rating,

        Long reviewCount,

        Long salesCount,

        Long categoryId,

        String categoryName,

        String thumbnailUrl,

        List<ProductImageResponse> images

) {
}
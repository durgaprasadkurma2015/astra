package com.astra.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WishlistItemResponse(

        Long id,

        Long productId,

        String productName,

        String productSlug,

        String thumbnailUrl,

        BigDecimal price,

        BigDecimal discountPrice,

        boolean active,

        boolean inStock,

        Integer stockQuantity,

        Double rating,

        Long reviewCount,

        LocalDateTime addedAt

) {
}
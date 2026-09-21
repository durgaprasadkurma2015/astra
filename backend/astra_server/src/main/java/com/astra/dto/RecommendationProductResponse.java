package com.astra.dto;

import java.math.BigDecimal;

public record RecommendationProductResponse(
        Long id,
        String name,
        String slug,
        BigDecimal price,
        BigDecimal discountPrice,
        String thumbnailUrl,
        Double rating
) {
}
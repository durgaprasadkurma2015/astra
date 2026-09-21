package com.astra.dto;

import java.math.BigDecimal;

public record OrderItemResponse(

        Long id,

        Long productId,

        String productName,

        String productSlug,

        String thumbnailUrl,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal lineTotal

) {
}
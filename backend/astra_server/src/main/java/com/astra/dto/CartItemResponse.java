package com.astra.dto;

import java.math.BigDecimal;

public record CartItemResponse(

        Long id,

        Long productId,

        String productName,

        String productSlug,

        String thumbnailUrl,

        Integer quantity,

        Integer availableStock,

        BigDecimal unitPrice,

        BigDecimal lineTotal

) {
}
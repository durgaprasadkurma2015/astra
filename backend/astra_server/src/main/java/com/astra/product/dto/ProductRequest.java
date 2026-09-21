package com.astra.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank
        String name,

        String sku,

        String shortDescription,

        String description,

        @NotNull
        @Positive
        BigDecimal price,

        @PositiveOrZero
        BigDecimal discountPrice,

        @NotNull
        @PositiveOrZero
        Integer stockQuantity,

        @NotNull
        Long categoryId,

        String thumbnailUrl,

        boolean active,

        boolean featured
) {
}
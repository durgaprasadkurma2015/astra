package com.astra.admin.dto;

import java.math.BigDecimal;

public record AdminProductResponse(

        Long id,

        String name,

        String slug,

        BigDecimal price,

        BigDecimal discountPrice,

        Integer stockQuantity,

        boolean active,

        Double rating

) {
}
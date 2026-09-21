package com.astra.search.dto;

import java.math.BigDecimal;

public record ProductSearchResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        Boolean active
) {
}
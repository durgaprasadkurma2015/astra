
package com.astra.cart.dto;

import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String name,
    String description,
    String category,
    String imageUrl,
    BigDecimal price,
    Integer stockQuantity,
    Boolean active
) {
}

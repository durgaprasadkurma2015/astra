
package com.astra.backend.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
    Long productId,
    String name,
    String description,
    String category,
    String imageUrl,
    BigDecimal price,
    Integer quantity,
    Integer stockQuantity,
    BigDecimal lineTotal
) {
}

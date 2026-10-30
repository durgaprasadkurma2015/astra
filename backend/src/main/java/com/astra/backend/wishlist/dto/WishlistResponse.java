package com.astra.backend.wishlist.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WishlistResponse(
    Long productId,
    String name,
    String description,
    String category,
    String imageUrl,
    BigDecimal price,
    BigDecimal discountedPrice,
    Integer stockQuantity,
    Boolean active,
    LocalDateTime addedAt
) {
}
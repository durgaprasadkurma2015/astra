
package com.astra.wishlist.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WishlistItemResponse(
        Long productId,
        String name,
        String description,
        String category,
        String imageUrl,
        BigDecimal price,
        Integer stockQuantity,
        LocalDateTime addedAt
) {}

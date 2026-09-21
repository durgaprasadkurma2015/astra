package com.astra.review.dto;

import java.time.LocalDateTime;

public record ReviewResponse(

        Long id,

        Long productId,

        Long userId,

        String userName,

        Integer rating,

        String title,

        String comment,

        boolean verifiedPurchase,

        boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
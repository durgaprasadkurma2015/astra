package com.astra.backend.product;

import java.time.LocalDateTime;

public record AdminProductReviewResponse(
        Long id,
        Long productId,
        String productName,
        String customerName,
        String customerEmail,
        Integer rating,
        String comment,
        boolean verifiedPurchase,
        boolean published,
        LocalDateTime createdAt) {

    public static AdminProductReviewResponse from(ProductReview review) {
        return new AdminProductReviewResponse(
                review.getId(), review.getProduct().getId(),
                review.getProduct().getName(), review.getCustomerName(),
                review.getUserEmail(), review.getRating(), review.getComment(),
                review.isVerifiedPurchase(), review.isPublished(),
                review.getCreatedAt());
    }
}

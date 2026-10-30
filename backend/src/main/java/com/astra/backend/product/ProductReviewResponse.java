
package com.astra.backend.product;

import java.time.LocalDateTime;

public record ProductReviewResponse(
    Long id,
    Long productId,
    String customerName,
    Integer rating,
    String comment,
    boolean verifiedPurchase,
    LocalDateTime createdAt
) {
    public static ProductReviewResponse from(ProductReview review) {
        return new ProductReviewResponse(
            review.getId(),
            review.getProduct().getId(),
            review.getCustomerName(),
            review.getRating(),
            review.getComment(),
            review.isVerifiedPurchase(),
            review.getCreatedAt()
        );
    }
}

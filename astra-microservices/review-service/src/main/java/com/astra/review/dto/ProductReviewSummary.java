
package com.astra.review.dto;

public record ProductReviewSummary(
    Long productId,
    Double averageRating,
    long totalReviews,
    long fiveStarCount,
    long fourStarCount,
    long threeStarCount,
    long twoStarCount,
    long oneStarCount
) {}

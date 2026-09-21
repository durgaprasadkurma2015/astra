package com.astra.controller;

import com.astra.dto.ReviewSummaryResponse;
import com.astra.review.dto.ReviewRequest;
import com.astra.review.dto.ReviewResponse;
import com.astra.service.ProductReviewService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ProductReviewController {

    private final ProductReviewService reviewService;

    public ProductReviewController(
            ProductReviewService reviewService
    ) {

        this.reviewService = reviewService;
    }

    /**
     * Create review.
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            Authentication authentication,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request
    ) {

        ReviewResponse response =
                reviewService.createReview(
                        authentication.getName(),
                        productId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get product reviews.
     */
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable Long productId,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                reviewService.getReviews(
                        productId,
                        pageable
                )
        );
    }

    /**
     * Get one review.
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId
    ) {

        return ResponseEntity.ok(
                reviewService.getReview(
                        productId,
                        reviewId
                )
        );
    }

    /**
     * Get current user's review.
     */
    @GetMapping("/me")
    public ResponseEntity<ReviewResponse> getMyReview(
            Authentication authentication,
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                reviewService.getMyReview(
                        authentication.getName(),
                        productId
                )
        );
    }

    /**
     * Get rating summary.
     */
    @GetMapping("/summary")
    public ResponseEntity<ReviewSummaryResponse> getSummary(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                reviewService.getSummary(productId)
        );
    }

    /**
     * Update own review.
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            Authentication authentication,
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request
    ) {

        return ResponseEntity.ok(
                reviewService.updateReview(
                        authentication.getName(),
                        productId,
                        reviewId,
                        request
                )
        );
    }

    /**
     * Delete own review.
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            Authentication authentication,
            @PathVariable Long productId,
            @PathVariable Long reviewId
    ) {

        reviewService.deleteReview(
                authentication.getName(),
                productId,
                reviewId
        );

        return ResponseEntity.noContent().build();
    }
}
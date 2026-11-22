
package com.astra.review.controller;

import com.astra.review.dto.ProductReviewRequest;
import com.astra.review.dto.ProductReviewResponse;
import com.astra.review.dto.ProductReviewSummary;
import com.astra.review.service.ProductReviewService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ProductReviewController {

    private final ProductReviewService reviewService;

    public ProductReviewController(
        ProductReviewService reviewService
    ) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ProductReviewResponse> getReviews(
        @PathVariable Long productId
    ) {
        return reviewService.getReviews(productId);
    }

    @GetMapping("/summary")
    public ProductReviewSummary getSummary(
        @PathVariable Long productId
    ) {
        return reviewService.getSummary(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductReviewResponse createReview(
        @PathVariable Long productId,
        @Valid @RequestBody ProductReviewRequest request
    ) {
        return reviewService.createReview(productId, request);
    }
}

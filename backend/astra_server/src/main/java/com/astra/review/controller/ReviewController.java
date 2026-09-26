package com.astra.review.controller;

import com.astra.dto.ReviewSummaryResponse;
import com.astra.review.dto.ReviewRequest;
import com.astra.review.dto.ReviewResponse;
import com.astra.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public Page<ReviewResponse> list(
            @PathVariable Long productId,
            Pageable pageable) {

        return reviewService.list(productId, pageable);
    }

    @GetMapping("/summary")
    public ReviewSummaryResponse summary(
            @PathVariable Long productId) {

        return reviewService.summary(productId);
    }

    @GetMapping("/me")
    public ReviewResponse mine(
            @PathVariable Long productId,
            Principal principal) {

        return reviewService.mine(
                principal.getName(),
                productId
        );
    }

    @GetMapping("/{reviewId}")
    public ReviewResponse get(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {

        return reviewService.get(
                productId,
                reviewId
        );
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(
            @PathVariable Long productId,
            Principal principal,
            @Valid @RequestBody ReviewRequest request) {

        return ResponseEntity.ok(
                reviewService.create(
                        principal.getName(),
                        productId,
                        request
                )
        );
    }

    @PutMapping("/{reviewId}")
    public ReviewResponse update(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            Principal principal,
            @Valid @RequestBody ReviewRequest request) {

        return reviewService.update(
                principal.getName(),
                productId,
                reviewId,
                request
        );
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            Principal principal) {

        reviewService.delete(
                principal.getName(),
                productId,
                reviewId
        );

        return ResponseEntity.noContent().build();
    }
}

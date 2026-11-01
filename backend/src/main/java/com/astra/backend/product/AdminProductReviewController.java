package com.astra.backend.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductReviewController {
    private final ProductReviewService reviewService;

    public AdminProductReviewController(ProductReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public Page<AdminProductReviewResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return reviewService.getAdminReviews(PageRequest.of(
                Math.max(0, page), Math.min(50, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PutMapping("/{reviewId}/visibility")
    public AdminProductReviewResponse setVisibility(
            @PathVariable Long reviewId,
            @RequestBody ReviewVisibilityRequest request) {
        return reviewService.setPublished(reviewId, request.published());
    }
}

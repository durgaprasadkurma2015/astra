package com.astra.review.service;

import com.astra.dto.ReviewSummaryResponse;
import com.astra.review.dto.ReviewRequest;
import com.astra.review.dto.ReviewResponse;
import com.astra.service.ProductReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final ProductReviewService delegate;

    public ReviewService(ProductReviewService delegate) {
        this.delegate = delegate;
    }

    public ReviewResponse create(String email, Long productId, ReviewRequest request) {
        return delegate.createReview(email, productId, request);
    }

    public Page<ReviewResponse> list(Long productId, Pageable pageable) {
        return delegate.getReviews(productId, pageable);
    }

    public ReviewResponse get(Long productId, Long reviewId) {
        return delegate.getReview(productId, reviewId);
    }

    public ReviewResponse mine(String email, Long productId) {
        return delegate.getMyReview(email, productId);
    }

    public ReviewResponse update(String email, Long productId, Long reviewId, ReviewRequest request) {
        return delegate.updateReview(email, productId, reviewId, request);
    }

    public void delete(String email, Long productId, Long reviewId) {
        delegate.deleteReview(email, productId, reviewId);
    }

    public ReviewSummaryResponse summary(Long productId) {
        return delegate.getSummary(productId);
    }
}


package com.astra.review.service;

import com.astra.review.client.OrderClient;
import com.astra.review.client.ProductClient;
import com.astra.review.dto.ProductReviewRequest;
import com.astra.review.dto.ProductReviewResponse;
import com.astra.review.dto.ProductReviewSummary;
import com.astra.review.entity.ProductReview;
import com.astra.review.repository.ProductReviewRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductClient productClient;
    private final OrderClient orderClient;

    public ProductReviewService(
        ProductReviewRepository reviewRepository,
        ProductClient productClient,
        OrderClient orderClient
    ) {
        this.reviewRepository = reviewRepository;
        this.productClient = productClient;
        this.orderClient = orderClient;
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponse> getReviews(Long productId) {
        String token = getCurrentToken();

        productClient.getActiveProduct(productId, token);

        return reviewRepository
            .findByProductIdOrderByCreatedAtDesc(productId)
            .stream()
            .map(ProductReviewResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProductReviewSummary getSummary(Long productId) {
        String token = getCurrentToken();

        productClient.getActiveProduct(productId, token);

        Double average =
            reviewRepository.findAverageRatingByProductId(productId);

        long total = reviewRepository.countByProductId(productId);

        return new ProductReviewSummary(
            productId,
            average == null ? 0.0 : Math.round(average * 100.0) / 100.0,
            total,
            reviewRepository.countByProductIdAndRating(productId, 5),
            reviewRepository.countByProductIdAndRating(productId, 4),
            reviewRepository.countByProductIdAndRating(productId, 3),
            reviewRepository.countByProductIdAndRating(productId, 2),
            reviewRepository.countByProductIdAndRating(productId, 1)
        );
    }

    @Transactional
    public ProductReviewResponse createReview(
        Long productId,
        ProductReviewRequest request
    ) {
        String email = getAuthenticatedEmail();
        String token = getCurrentToken();

        ProductClient.ProductInfo product =
            productClient.getActiveProduct(productId, token);

        if (request == null
            || request.rating() == null
            || request.rating() < 1
            || request.rating() > 5) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Rating must be between 1 and 5"
            );
        }

        if (request.comment() == null
            || request.comment().isBlank()
            || request.comment().trim().length() > 2000) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Review comment must contain 1 to 2000 characters"
            );
        }

        if (reviewRepository.existsByProductIdAndUserEmail(
            productId, email
        )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "You have already reviewed this product"
            );
        }

        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        String customerName = email.substring(0, email.indexOf('@'));

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String fullName = jwtAuth.getToken().getClaimAsString("fullName");
            String name = jwtAuth.getToken().getClaimAsString("name");

            if (fullName != null && !fullName.isBlank()) {
                customerName = fullName.trim();
            } else if (name != null && !name.isBlank()) {
                customerName = name.trim();
            }
        }

        boolean verifiedPurchase =
            orderClient.hasDeliveredPurchase(productId, token);

        ProductReview review = new ProductReview();
        review.setProductId(product.id());
        review.setUserEmail(email);
        review.setCustomerName(customerName);
        review.setRating(request.rating());
        review.setComment(request.comment().trim());
        review.setVerifiedPurchase(verifiedPurchase);

        return ProductReviewResponse.from(
            reviewRepository.saveAndFlush(review)
        );
    }

    private String getAuthenticatedEmail() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication.getName() == null
            || authentication.getName().isBlank()
            || "anonymousUser".equalsIgnoreCase(authentication.getName())) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in to submit a review"
            );
        }

        return authentication.getName()
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    private String getCurrentToken() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }

        throw new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "A valid JWT token is required"
        );
    }
}

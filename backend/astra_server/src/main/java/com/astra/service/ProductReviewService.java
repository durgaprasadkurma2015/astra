package com.astra.service;

import com.astra.dto.ReviewSummaryResponse;
import com.astra.entity.Product;
import com.astra.entity.ProductReview;
import com.astra.entity.User;
import com.astra.repository.ProductRepository;
import com.astra.repository.ProductReviewRepository;
import com.astra.repository.UserRepository;
import com.astra.review.dto.ReviewRequest;
import com.astra.review.dto.ReviewResponse;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductReviewService(
            ProductReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a product review.
     */
    public ReviewResponse createReview(
            String email,
            Long productId,
            ReviewRequest request
    ) {

        User user = getUser(email);

        Product product = getProduct(productId);

        validateProduct(product);

        if (reviewRepository.existsByProductIdAndUserId(
                productId,
                user.getId()
        )) {

            throw new IllegalArgumentException(
                    "You have already reviewed this product"
            );
        }

        validateRating(request.rating());

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(request.rating())
                .title(request.title())
                .comment(request.comment())
                .verifiedPurchase(false)
                .active(true)
                .build();

        ProductReview savedReview =
                reviewRepository.save(review);

        updateProductRating(product);

        return toResponse(savedReview);
    }

    /**
     * Get paginated reviews.
     */
    @Transactional
    public Page<ReviewResponse> getReviews(
            Long productId,
            Pageable pageable
    ) {

        Product product = getProduct(productId);

        validateProduct(product);

        return reviewRepository
                .findByProductIdAndActiveTrue(
                        productId,
                        pageable
                )
                .map(this::toResponse);
    }

    /**
     * Get one review.
     */
    @Transactional
    public ReviewResponse getReview(
            Long productId,
            Long reviewId
    ) {

        ProductReview review =
                reviewRepository
                        .findByIdAndProductIdAndActiveTrue(
                                reviewId,
                                productId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Review not found: "
                                                + reviewId
                                )
                        );

        return toResponse(review);
    }

    /**
     * Get current user's review.
     */
    @Transactional
    public ReviewResponse getMyReview(
            String email,
            Long productId
    ) {

        User user = getUser(email);

        ProductReview review =
                reviewRepository
                        .findByProductIdAndUserIdAndActiveTrue(
                                productId,
                                user.getId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "You have not reviewed this product"
                                )
                        );

        return toResponse(review);
    }

    /**
     * Update own review.
     */
    public ReviewResponse updateReview(
            String email,
            Long productId,
            Long reviewId,
            ReviewRequest request
    ) {

        User user = getUser(email);

        ProductReview review =
                reviewRepository
                        .findByIdAndProductIdAndActiveTrue(
                                reviewId,
                                productId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Review not found: "
                                                + reviewId
                                )
                        );

        if (!review.getUser().getId().equals(user.getId())) {

            throw new IllegalArgumentException(
                    "You can update only your own review"
            );
        }

        validateRating(request.rating());

        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setComment(request.comment());

        ProductReview savedReview =
                reviewRepository.save(review);

        updateProductRating(review.getProduct());

        return toResponse(savedReview);
    }

    /**
     * Delete own review.
     */
    public void deleteReview(
            String email,
            Long productId,
            Long reviewId
    ) {

        User user = getUser(email);

        ProductReview review =
                reviewRepository
                        .findByIdAndProductIdAndActiveTrue(
                                reviewId,
                                productId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Review not found: "
                                                + reviewId
                                )
                        );

        if (!review.getUser().getId().equals(user.getId())) {

            throw new IllegalArgumentException(
                    "You can delete only your own review"
            );
        }

        Product product = review.getProduct();

        review.setActive(false);

        reviewRepository.save(review);

        updateProductRating(product);
    }

    /**
     * Get rating summary.
     */
    @Transactional
    public ReviewSummaryResponse getSummary(
            Long productId
    ) {

        Product product = getProduct(productId);

        validateProduct(product);

        long reviewCount =
                reviewRepository.countByProductIdAndActiveTrue(
                        productId
                );

        long five =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                productId,
                                5
                        );

        long four =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                productId,
                                4
                        );

        long three =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                productId,
                                3
                        );

        long two =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                productId,
                                2
                        );

        long one =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                productId,
                                1
                        );

        BigDecimal average =
                calculateAverage(
                        five,
                        four,
                        three,
                        two,
                        one,
                        reviewCount
                );

        return new ReviewSummaryResponse(
                productId,
                average,
                reviewCount,
                five,
                four,
                three,
                two,
                one
        );
    }

    /**
     * Recalculate Product.rating and Product.reviewCount.
     */
    private void updateProductRating(
            Product product
    ) {

        long reviewCount =
                reviewRepository.countByProductIdAndActiveTrue(
                        product.getId()
                );

        long five =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                product.getId(),
                                5
                        );

        long four =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                product.getId(),
                                4
                        );

        long three =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                product.getId(),
                                3
                        );

        long two =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                product.getId(),
                                2
                        );

        long one =
                reviewRepository
                        .countByProductIdAndRatingAndActiveTrue(
                                product.getId(),
                                1
                        );

        BigDecimal average =
                calculateAverage(
                        five,
                        four,
                        three,
                        two,
                        one,
                        reviewCount
                );

        /*
         * Product.rating is Double,
         * so convert BigDecimal to Double.
         */
        product.setRating(
                average.doubleValue()
        );

        /*
         * Product.reviewCount is Long,
         * so keep it as long.
         */
        product.setReviewCount(
                reviewCount
        );

        productRepository.save(product);
    }

    private BigDecimal calculateAverage(
            long five,
            long four,
            long three,
            long two,
            long one,
            long total
    ) {

        if (total == 0) {
            return BigDecimal.ZERO;
        }

        long totalPoints =
                (five * 5)
                        + (four * 4)
                        + (three * 3)
                        + (two * 2)
                        + one;

        return BigDecimal.valueOf(totalPoints)
                .divide(
                        BigDecimal.valueOf(total),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private void validateRating(
            Integer rating
    ) {

        if (rating == null) {

            throw new IllegalArgumentException(
                    "Rating is required"
            );
        }

        if (rating < 1 || rating > 5) {

            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5"
            );
        }
    }

    private void validateProduct(
            Product product
    ) {

        if (!product.isActive()) {

            throw new IllegalArgumentException(
                    "Product is not active"
            );
        }
    }

    private User getUser(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated user email is missing"
            );
        }

        /*
         * UserRepository provides:
         * findByEmailIgnoreCase(String)
         *
         * NOT findByEmail(String).
         */
        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );
    }

    private Product getProduct(
            Long productId
    ) {

        return productRepository
                .findById(productId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Product not found: "
                                        + productId
                        )
                );
    }

    private ReviewResponse toResponse(
            ProductReview review
    ) {

        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.isVerifiedPurchase(),
                review.isActive(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

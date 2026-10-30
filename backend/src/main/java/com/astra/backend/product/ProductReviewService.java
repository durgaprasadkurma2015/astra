
package com.astra.backend.product;

import com.astra.backend.order.CustomerOrder;
import com.astra.backend.order.CustomerOrderItem;
import com.astra.backend.order.CustomerOrderItemRepository;
import com.astra.backend.order.CustomerOrderRepository;
import com.astra.backend.user.AppUser;
import com.astra.backend.user.AppUserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;
    private final CustomerOrderItemRepository orderItemRepository;
    private final AppUserRepository userRepository;

    public ProductReviewService(
            ProductReviewRepository reviewRepository,
            ProductRepository productRepository,
            CustomerOrderRepository orderRepository,
            CustomerOrderItemRepository orderItemRepository,
            AppUserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponse> getReviews(Long productId) {
        ensureActiveProductExists(productId);

        return reviewRepository
                .findByProductIdAndPublishedTrueOrderByCreatedAtDesc(productId)
                .stream()
                .map(ProductReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductReviewSummary getSummary(Long productId) {
        ensureActiveProductExists(productId);

        List<ProductReview> reviews =
                reviewRepository.findByProductIdAndPublishedTrueOrderByCreatedAtDesc(
                        productId);

        long five = 0;
        long four = 0;
        long three = 0;
        long two = 0;
        long one = 0;

        for (ProductReview review : reviews) {
            switch (review.getRating()) {
                case 5 -> five++;
                case 4 -> four++;
                case 3 -> three++;
                case 2 -> two++;
                case 1 -> one++;
                default -> {
                    // Ratings are constrained to 1–5.
                }
            }
        }

        Double average = reviewRepository
                .findAverageRatingByProductId(productId);

        return new ProductReviewSummary(
                productId,
                average == null ? 0.0 : Math.round(average * 100.0) / 100.0,
                reviews.size(),
                five,
                four,
                three,
                two,
                one
        );
    }

    @Transactional
    public ProductReviewResponse createReview(
            Long productId,
            ProductReviewRequest request) {

        String email = getAuthenticatedEmail();

        Product product = ensureActiveProductExists(productId);

        if (request == null
                || request.rating() == null
                || request.rating() < 1
                || request.rating() > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rating must be between 1 and 5");
        }

        if (request.comment() == null
                || request.comment().isBlank()
                || request.comment().trim().length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Review comment must contain 1 to 2000 characters");
        }

        if (reviewRepository.existsByProductIdAndUserEmail(
                productId, email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already reviewed this product");
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"));

        boolean verifiedPurchase = hasEligiblePurchase(productId, email);
        if (!verifiedPurchase) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can review this product after an order containing it is delivered");
        }

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUserEmail(email);
        review.setCustomerName(user.getFullName());
        review.setRating(request.rating());
        review.setComment(request.comment().trim());
        review.setVerifiedPurchase(verifiedPurchase);
        review.setPublished(false);

        return ProductReviewResponse.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public Page<AdminProductReviewResponse> getAdminReviews(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AdminProductReviewResponse::from);
    }

    @Transactional
    public AdminProductReviewResponse setPublished(Long reviewId, boolean published) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Review not found"));
        review.setPublished(published);
        return AdminProductReviewResponse.from(reviewRepository.save(review));
    }

    private boolean hasEligiblePurchase(Long productId, String email) {
        List<CustomerOrder> orders =
                orderRepository.findByUserEmailOrderByCreatedAtDesc(email);

        for (CustomerOrder order : orders) {
            String status = order.getOrderStatus();

            if (status == null
                    || !status.trim().equalsIgnoreCase("DELIVERED")) {
                continue;
            }

            List<CustomerOrderItem> items =
                    orderItemRepository.findByOrderId(order.getId());

            for (CustomerOrderItem item : items) {
                if (item.getProduct() != null
                        && productId.equals(item.getProduct().getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    private Product ensureActiveProductExists(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found");
        }

        return product;
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
                    "Please log in to submit a review");
        }

        return authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}

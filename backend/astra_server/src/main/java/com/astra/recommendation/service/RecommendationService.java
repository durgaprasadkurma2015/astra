package com.astra.recommendation.service;

import com.astra.dto.RecommendationProductResponse;
import com.astra.entity.Product;
import com.astra.entity.RecommendationEvent;
import com.astra.entity.User;
import com.astra.enums.RecommendationEventType;
import com.astra.repository.ProductRepository;
import com.astra.repository.RecommendationEventRepository;
import com.astra.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class RecommendationService {

    private final RecommendationEventRepository eventRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public RecommendationService(
            RecommendationEventRepository eventRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public void recordProductView(
            String slug,
            String username
    ) {

        Product product =
                productRepository
                        .findBySlug(slug)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Product not found"
                                )
                        );

        User user = null;

        if (username != null && !username.isBlank()) {

            user = userRepository
                    .findByEmailIgnoreCase(username)
                    .orElse(null);
        }

        RecommendationEvent event =
                new RecommendationEvent();

        event.setProduct(product);
        event.setUser(user);

        event.setEventType(
                RecommendationEventType.PRODUCT_VIEW
        );

        event.setQuantity(1);

        eventRepository.save(event);
    }

    @Transactional
    public List<RecommendationProductResponse> getTrending(
            int limit
    ) {

        List<Product> products =
                eventRepository.findPopularProducts(
                        RecommendationEventType.PRODUCT_VIEW,
                        PageRequest.of(0, limit)
                );

        return convert(products);
    }

    @Transactional
    public List<RecommendationProductResponse> getRecentlyViewed(
            String username,
            int limit
    ) {

        User user =
                userRepository
                        .findByEmailIgnoreCase(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<Product> products =
                eventRepository.findRecentProducts(
                        user.getId(),
                        RecommendationEventType.PRODUCT_VIEW,
                        PageRequest.of(0, limit)
                );

        return convert(products);
    }

    @Transactional
    public List<RecommendationProductResponse> getForYou(
            String username,
            int limit
    ) {

        User user =
                userRepository
                        .findByEmailIgnoreCase(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        List<Product> products =
                eventRepository.findUserProducts(
                        user.getId(),
                        RecommendationEventType.PURCHASE,
                        PageRequest.of(0, limit)
                );

        if (products.isEmpty()) {

            products =
                    eventRepository.findUserProducts(
                            user.getId(),
                            RecommendationEventType.PRODUCT_VIEW,
                            PageRequest.of(0, limit)
                    );
        }

        if (products.isEmpty()) {
            return getTrending(limit);
        }

        return convert(products);
    }

    public List<RecommendationProductResponse> getSimilar(
            Long productId,
            int limit
    ) {

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Product not found"
                                )
                        );

        /*
         * First try products from the same category.
         *
         * Uses the existing repository method:
         *
         * findByCategoryIdAndActiveTrue(...)
         *
         * The current product is removed in Java.
         */
        if (product.getCategory() != null) {

            List<Product> products =
                    productRepository
                            .findByCategoryIdAndActiveTrue(
                                    product.getCategory().getId(),
                                    PageRequest.of(0, limit + 1)
                            )
                            .stream()
                            .filter(
                                    candidate ->
                                            !candidate.getId()
                                                    .equals(productId)
                            )
                            .limit(limit)
                            .toList();

            if (!products.isEmpty()) {
                return convert(products);
            }
        }

        /*
         * Fallback to popular products.
         */
        return getTrending(limit);
    }

    private List<RecommendationProductResponse> convert(
            List<Product> products
    ) {

        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    private RecommendationProductResponse toResponse(
            Product product
    ) {

        return new RecommendationProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getDiscountPrice(),
                getThumbnail(product),
                product.getRating()
        );
    }

    private String getThumbnail(
            Product product
    ) {

        /*
         * Product does not expose getImages().
         * Use the existing thumbnail URL.
         */
        return product.getThumbnailUrl();
    }
}

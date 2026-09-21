package com.astra.repository;

import com.astra.entity.ProductReview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Long> {

    Page<ProductReview> findByProductIdAndActiveTrue(
            Long productId,
            Pageable pageable
    );

    Optional<ProductReview> findByIdAndProductId(
            Long id,
            Long productId
    );

    Optional<ProductReview> findByIdAndProductIdAndActiveTrue(
            Long id,
            Long productId
    );

    Optional<ProductReview> findByProductIdAndUserId(
            Long productId,
            Long userId
    );

    Optional<ProductReview> findByProductIdAndUserIdAndActiveTrue(
            Long productId,
            Long userId
    );

    boolean existsByProductIdAndUserId(
            Long productId,
            Long userId
    );

    long countByProductIdAndActiveTrue(
            Long productId
    );

    long countByProductIdAndRatingAndActiveTrue(
            Long productId,
            Integer rating
    );
}
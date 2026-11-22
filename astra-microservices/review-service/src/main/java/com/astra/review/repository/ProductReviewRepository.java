
package com.astra.review.repository;

import com.astra.review.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductIdOrderByCreatedAtDesc(
        Long productId
    );

    boolean existsByProductIdAndUserEmail(
        Long productId,
        String userEmail
    );

    long countByProductId(Long productId);

    @Query("""
        SELECT COALESCE(AVG(r.rating), 0)
        FROM ProductReview r
        WHERE r.productId = :productId
        """)
    Double findAverageRatingByProductId(
        @Param("productId") Long productId
    );

    @Query("""
        SELECT COUNT(r)
        FROM ProductReview r
        WHERE r.productId = :productId
          AND r.rating = :rating
        """)
    long countByProductIdAndRating(
        @Param("productId") Long productId,
        @Param("rating") Integer rating
    );
}

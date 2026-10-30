
package com.astra.backend.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<ProductReview> findByProductIdAndPublishedTrueOrderByCreatedAtDesc(Long productId);

    Page<ProductReview> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByProductIdAndUserEmail(
            Long productId,
            String userEmail
    );

    Optional<ProductReview> findByProductIdAndUserEmail(
            Long productId,
            String userEmail
    );

    long countByProductId(Long productId);

    @Query("""
        SELECT COALESCE(AVG(r.rating), 0)
        FROM ProductReview r
        WHERE r.product.id = :productId AND r.published = true
        """)
    Double findAverageRatingByProductId(
            @Param("productId") Long productId
    );
}

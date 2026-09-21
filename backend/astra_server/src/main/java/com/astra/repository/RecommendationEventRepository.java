package com.astra.repository;

import com.astra.entity.Product;
import com.astra.entity.RecommendationEvent;
import com.astra.enums.RecommendationEventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationEventRepository
        extends JpaRepository<RecommendationEvent, Long> {

    @Query("""
            SELECT e.product
            FROM RecommendationEvent e
            WHERE e.user.id = :userId
              AND e.eventType = :eventType
            GROUP BY e.product.id
            ORDER BY MAX(e.createdAt) DESC
            """)
    List<Product> findRecentProducts(
            @Param("userId") Long userId,
            @Param("eventType") RecommendationEventType eventType,
            Pageable pageable
    );

    @Query("""
            SELECT e.product
            FROM RecommendationEvent e
            WHERE e.eventType = :eventType
            GROUP BY e.product.id
            ORDER BY COUNT(e.id) DESC
            """)
    List<Product> findPopularProducts(
            @Param("eventType") RecommendationEventType eventType,
            Pageable pageable
    );

    @Query("""
            SELECT e.product
            FROM RecommendationEvent e
            WHERE e.user.id = :userId
              AND e.eventType = :eventType
            GROUP BY e.product.id
            ORDER BY COUNT(e.id) DESC
            """)
    List<Product> findUserProducts(
            @Param("userId") Long userId,
            @Param("eventType") RecommendationEventType eventType,
            Pageable pageable
    );

    @Query("""
            SELECT e.product
            FROM RecommendationEvent e
            WHERE e.product.id = :productId
              AND e.eventType = :eventType
            GROUP BY e.product.id
            ORDER BY COUNT(e.id) DESC
            """)
    List<Product> findProductsByProductEvent(
            @Param("productId") Long productId,
            @Param("eventType") RecommendationEventType eventType,
            Pageable pageable
    );
}
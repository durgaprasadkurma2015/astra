package com.astra.repository;

import com.astra.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndActiveTrue(Long id);

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    List<Product> findByActiveTrue();

    Page<Product> findByActiveTrue(
            Pageable pageable
    );

    List<Product> findByCategoryId(
            Long categoryId
    );

    Page<Product> findByCategoryId(
            Long categoryId,
            Pageable pageable
    );

    Page<Product> findByCategoryIdAndActiveTrue(
            Long categoryId,
            Pageable pageable
    );

    List<Product> findByFeaturedTrueAndActiveTrue();

    Page<Product> findByFeaturedTrueAndActiveTrue(
            Pageable pageable
    );

    Page<Product> findByActiveTrueOrderByRatingDesc(
            Pageable pageable
    );

    Page<Product> findByActiveTrueOrderBySalesCountDesc(
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(p)
            FROM Product p
            WHERE p.stockQuantity <= 10
            """)
    long countLowStockProducts();

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.active = true
              AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<Product> search(
            String keyword,
            Pageable pageable
    );
}

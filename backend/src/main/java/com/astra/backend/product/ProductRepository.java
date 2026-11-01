package com.astra.backend.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public interface ProductRepository extends JpaRepository<Product, Long> {
List<Product> findAllByOrderByIdAsc();
    List<Product> findByActiveTrueOrderByIdAsc();

    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    @Query("""
        SELECT DISTINCT p.category
        FROM Product p
        WHERE p.active = true
          AND p.category IS NOT NULL
          AND TRIM(p.category) <> ''
        ORDER BY p.category
        """)
    List<String> findActiveCategories();

    @Query("""
        SELECT DISTINCT p.subcategory
        FROM Product p
        WHERE p.active = true
          AND LOWER(p.category) = LOWER(:category)
          AND p.subcategory IS NOT NULL
          AND TRIM(p.subcategory) <> ''
        ORDER BY p.subcategory
        """)
    List<String> findActiveSubcategories(
        @Param("category") String category
    );

    List<Product> findByActiveTrueAndCategoryIgnoreCaseOrderByIdAsc(
        String category
    );

    List<Product> findByActiveTrueAndCategoryIgnoreCaseAndSubcategoryIgnoreCaseOrderByIdAsc(
        String category,
        String subcategory
    );

    @Query(
        value = """
            SELECT p
            FROM Product p
            WHERE p.active = true
              AND (
                    :keyword IS NULL
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.subcategory) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
              AND (
                    :category IS NULL
                    OR LOWER(p.category) = LOWER(:category)
              )
              AND (
                    :subcategory IS NULL
                    OR LOWER(p.subcategory) = LOWER(:subcategory)
              )
              AND (
                    :minPrice IS NULL
                    OR p.price >= :minPrice
              )
              AND (
                    :maxPrice IS NULL
                    OR p.price <= :maxPrice
              )
              AND (
                    :inStock = false
                    OR p.stockQuantity > 0
              )
            """,
        countQuery = """
            SELECT COUNT(p)
            FROM Product p
            WHERE p.active = true
              AND (
                    :keyword IS NULL
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.subcategory) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
              AND (
                    :category IS NULL
                    OR LOWER(p.category) = LOWER(:category)
              )
              AND (
                    :subcategory IS NULL
                    OR LOWER(p.subcategory) = LOWER(:subcategory)
              )
              AND (
                    :minPrice IS NULL
                    OR p.price >= :minPrice
              )
              AND (
                    :maxPrice IS NULL
                    OR p.price <= :maxPrice
              )
              AND (
                    :inStock = false
                    OR p.stockQuantity > 0
              )
            """
    )
    Page<Product> searchActiveProducts(
        @Param("keyword") String keyword,
        @Param("category") String category,
        @Param("subcategory") String subcategory,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice,
        @Param("inStock") boolean inStock,
        Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(
        @Param("id") Long id
    );
    boolean existsBySkuIgnoreCase(String sku);
	boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);
}
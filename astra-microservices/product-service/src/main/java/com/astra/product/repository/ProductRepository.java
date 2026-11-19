package com.astra.product.repository;

import com.astra.product.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByIdAsc();

    List<Product> findByActiveTrueOrderByIdAsc();

    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    List<Product> findByActiveTrueAndCategoryIgnoreCaseOrderByIdAsc(
        String category
    );

    List<Product>
    findByActiveTrueAndCategoryIgnoreCaseAndSubcategoryIgnoreCaseOrderByIdAsc(
        String category,
        String subcategory
    );

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

    @Query(
        value = """
            SELECT p FROM Product p
            WHERE p.active = true
            AND (
                :keyword IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.subcategory) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
            AND (:subcategory IS NULL OR LOWER(p.subcategory) = LOWER(:subcategory))
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:inStock = false OR p.stockQuantity > 0)
        """,
        countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE p.active = true
            AND (
                :keyword IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.subcategory) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
            AND (:subcategory IS NULL OR LOWER(p.subcategory) = LOWER(:subcategory))
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:inStock = false OR p.stockQuantity > 0)
        """
    )
    Page<Product> searchActiveProducts(
        @Param("keyword") String keyword,
        @Param("category") String category,
        @Param("subcategory") String subcategory,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("inStock") boolean inStock,
        Pageable pageable
    );

    @EntityGraph(attributePaths = "variants")
    Optional<Product> findWithVariantsById(Long id);
}
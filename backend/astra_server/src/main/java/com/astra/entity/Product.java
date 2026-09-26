package com.astra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_product_slug", columnList = "slug"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_active", columnList = "active"),
        @Index(name = "idx_product_featured", columnList = "featured")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(length = 1000)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false)
    private Long reviewCount;

    @Column(nullable = false)
    private Long salesCount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 500)
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (rating == null) {
            rating = 0.0;
        }

        if (reviewCount == null) {
            reviewCount = 0L;
        }

        if (salesCount == null) {
            salesCount = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
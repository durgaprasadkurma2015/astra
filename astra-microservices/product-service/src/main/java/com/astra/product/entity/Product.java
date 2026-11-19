package com.astra.product.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "products",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_products_sku",
            columnNames = "sku"
        )
    }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String sku;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String subcategory;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(
        name = "discount_percentage",
        nullable = false,
        precision = 5,
        scale = 2
    )
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "product_variants",
        joinColumns = @JoinColumn(name = "product_id")
    )
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    public Integer getStockQuantity() { return stockQuantity; }
    public Boolean getActive() { return active; }
    public List<ProductVariant> getVariants() { return variants; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public BigDecimal getDiscountedPrice() {
        BigDecimal discount = discountPercentage == null
            ? BigDecimal.ZERO
            : discountPercentage;

        return price.multiply(
            BigDecimal.ONE.subtract(
                discount.divide(
                    new BigDecimal("100"),
                    4,
                    RoundingMode.HALF_UP
                )
            )
        ).setScale(2, RoundingMode.HALF_UP);
    }

    public void setName(String value) { name = value; }
    public void setSku(String value) { sku = value; }
    public void setDescription(String value) { description = value; }
    public void setCategory(String value) { category = value; }
    public void setSubcategory(String value) { subcategory = value; }
    public void setImageUrl(String value) { imageUrl = value; }
    public void setPrice(BigDecimal value) { price = value; }
    public void setDiscountPercentage(BigDecimal value) {
        discountPercentage = value;
    }
    public void setStockQuantity(Integer value) {
        stockQuantity = value;
    }
    public void setActive(Boolean value) { active = value; }
    public void setVariants(List<ProductVariant> value) {
        variants = value == null ? new ArrayList<>() : value;
    }
}
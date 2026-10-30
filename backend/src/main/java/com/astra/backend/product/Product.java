package com.astra.backend.product;

import jakarta.persistence.*;
import java.math.BigDecimal;
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

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String category;

    @Column(name = "subcategory", length = 100)
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "product_variants",
        joinColumns = @JoinColumn(name = "product_id")
    )
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public Boolean getActive() {
        return active;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getDiscountedPrice() {
        if (price == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = discountPercentage == null
            ? BigDecimal.ZERO
            : discountPercentage;

        return price.multiply(
            BigDecimal.ONE.subtract(
                discount.divide(new BigDecimal("100"), 4,
                    java.math.RoundingMode.HALF_UP)
            )
        ).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants == null
            ? new ArrayList<>()
            : variants;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
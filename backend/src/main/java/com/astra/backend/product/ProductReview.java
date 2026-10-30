
package com.astra.backend.product;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "product_reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_product_reviews_product_user",
            columnNames = {"product_id", "user_email"}
        )
    },
    indexes = {
        @Index(
            name = "idx_product_reviews_product_created",
            columnList = "product_id, created_at"
        )
    }
)
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 2000)
    private String comment;

    @Column(name = "verified_purchase", nullable = false)
    private boolean verifiedPurchase = false;

    @Column(nullable = false)
    private boolean published = true;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime updatedAt;

    public ProductReview() {
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isVerifiedPurchase() {
        return verifiedPurchase;
    }

    public void setVerifiedPurchase(boolean verifiedPurchase) {
        this.verifiedPurchase = verifiedPurchase;
    }

    public boolean isPublished() { return published; }

    public void setPublished(boolean published) { this.published = published; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}


package com.astra.review.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "product_reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_product_reviews_product_user",
        columnNames = {"product_id", "user_email"}
    ),
    indexes = @Index(
        name = "idx_product_reviews_product_created",
        columnList = "product_id, created_at"
    )
)
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

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

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public ProductReview() {}

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getUserEmail() { return userEmail; }
    public String getCustomerName() { return customerName; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public boolean isVerifiedPurchase() { return verifiedPurchase; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setProductId(Long productId) { this.productId = productId; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setRating(Integer rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setVerifiedPurchase(boolean verifiedPurchase) {
        this.verifiedPurchase = verifiedPurchase;
    }
}

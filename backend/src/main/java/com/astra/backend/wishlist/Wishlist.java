package com.astra.backend.wishlist;

import com.astra.backend.product.Product;
import com.astra.backend.user.AppUser;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "wishlist_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_wishlist_user_product",
            columnNames = {"user_id", "product_id"}
        )
    },
    indexes = {
        @Index(
            name = "idx_wishlist_user_created",
            columnList = "user_id, created_at"
        )
    }
)
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_wishlist_user")
    )
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_wishlist_product")
    )
    private Product product;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    public Wishlist() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Product getProduct() {
        return product;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
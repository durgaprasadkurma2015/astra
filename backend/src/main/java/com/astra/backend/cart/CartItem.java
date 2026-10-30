
package com.astra.backend.cart;

import com.astra.backend.product.Product;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_cart_user_product",
        columnNames = {"user_email", "product_id"}
    )
)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public CartItem() {
    }

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

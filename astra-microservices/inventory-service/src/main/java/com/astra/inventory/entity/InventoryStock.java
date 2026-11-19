
package com.astra.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "inventory_stock",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_inventory_product",
        columnNames = "product_id"
    )
)
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    public InventoryStock() {}

    public InventoryStock(Long productId, int quantity) {
        this.productId = productId;
        this.availableQuantity = quantity;
        this.reservedQuantity = 0;
    }

    public void addStock(int quantity) {
        this.availableQuantity += quantity;
    }

    public void reserve(int quantity) {
        if (quantity <= 0 || availableQuantity < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    public void confirm(int quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Reserved quantity is insufficient");
        }
        reservedQuantity -= quantity;
    }

    public void release(int quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Reserved quantity is insufficient");
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

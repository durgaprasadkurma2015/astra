package com.astra.backend.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_stock_movements", indexes = {
        @Index(name = "idx_inventory_movements_product_created", columnList = "product_id, created_at"),
        @Index(name = "idx_inventory_movements_actor_created", columnList = "actor_email, created_at")
})
public class InventoryStockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "actor_email", nullable = false, length = 255)
    private String actorEmail;

    @Column(name = "quantity_delta", nullable = false)
    private int quantityDelta;

    @Column(name = "stock_after", nullable = false)
    private int stockAfter;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected InventoryStockMovement() { }

    public InventoryStockMovement(Product product, String actorEmail,
            int quantityDelta, int stockAfter, String reason) {
        this.product = product;
        this.actorEmail = actorEmail;
        this.quantityDelta = quantityDelta;
        this.stockAfter = stockAfter;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getActorEmail() { return actorEmail; }
    public int getQuantityDelta() { return quantityDelta; }
    public int getStockAfter() { return stockAfter; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}


package com.astra.inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "stock_reservation_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_reservation_product",
        columnNames = {"reservation_id", "product_id"}
    )
)
public class StockReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private StockReservation reservation;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    protected StockReservationItem() {}

    public StockReservationItem(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public void setReservation(StockReservation reservation) {
        this.reservation = reservation;
    }

    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
}

package com.astra.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import com.astra.enums.ShipmentStatus;

@Entity
@Table(
    name = "shipment_tracking",
    indexes = {
        @Index(
            name = "idx_tracking_shipment",
            columnList = "shipment_id"
        ),
        @Index(
            name = "idx_tracking_created",
            columnList = "created_at"
        )
    }
)
public class ShipmentTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "shipment_id",
        nullable = false
    )
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ShipmentStatus status;

    @Column(length = 255)
    private String location;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
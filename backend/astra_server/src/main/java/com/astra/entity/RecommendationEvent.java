package com.astra.entity;

import com.astra.enums.RecommendationEventType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recommendation_events",
        indexes = {
                @Index(
                        name = "idx_rec_event_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_rec_event_product",
                        columnList = "product_id"
                ),
                @Index(
                        name = "idx_rec_event_type",
                        columnList = "event_type"
                ),
                @Index(
                        name = "idx_rec_event_created",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_rec_event_user_created",
                        columnList = "user_id, created_at"
                )
        }
)
public class RecommendationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 40
    )
    private RecommendationEventType eventType;

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity = 1;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public RecommendationEventType getEventType() {
        return eventType;
    }

    public void setEventType(RecommendationEventType eventType) {
        this.eventType = eventType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
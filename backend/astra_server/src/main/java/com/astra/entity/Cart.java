package com.astra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "carts",
        indexes = {
                @Index(
                        name = "idx_cart_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_cart_user"
            )
    )
    private User user;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }

        if (discount == null) {
            discount = BigDecimal.ZERO;
        }

        if (total == null) {
            total = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
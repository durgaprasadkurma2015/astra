package com.astra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.astra.enums.PaymentMethod;
import com.astra.enums.PaymentStatus;

@Entity
@Table(
    name = "payment_transactions",
    indexes = {
        @Index(
            name = "idx_payment_order",
            columnList = "order_id"
        ),
        @Index(
            name = "idx_payment_user",
            columnList = "user_id"
        ),
        @Index(
            name = "idx_payment_status",
            columnList = "status"
        ),
        @Index(
            name = "idx_payment_provider_id",
            columnList = "provider_payment_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        nullable = false,
        unique = true,
        length = 50
    )
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "order_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_payment_order"
        )
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_payment_user"
        )
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 30
    )
    @Builder.Default
    private PaymentStatus status =
            PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 30
    )
    private PaymentMethod paymentMethod;

    @Column(
        nullable = false,
        precision = 19,
        scale = 2
    )
    private BigDecimal amount;

    @Column(
        nullable = false,
        length = 10
    )
    @Builder.Default
    private String currency = "INR";

    @Column(
        unique = true,
        length = 150
    )
    private String providerPaymentId;

    @Column(length = 500)
    private String providerOrderId;

    @Column(length = 1000)
    private String failureReason;

    @Column(length = 1000)
    private String refundReference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
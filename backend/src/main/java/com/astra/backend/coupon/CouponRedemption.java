package com.astra.backend.coupon;

import com.astra.backend.order.CustomerOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_redemptions", indexes = {
        @Index(name = "idx_coupon_redemptions_owner", columnList = "coupon_id, user_email"),
        @Index(name = "idx_coupon_redemptions_coupon_date", columnList = "coupon_id, redeemed_at")
})
public class CouponRedemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private CustomerOrder order;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "redeemed_at", insertable = false, updatable = false)
    private LocalDateTime redeemedAt;

    protected CouponRedemption() { }

    public CouponRedemption(Coupon coupon, CustomerOrder order,
            String userEmail, BigDecimal discountAmount) {
        this.coupon = coupon;
        this.order = order;
        this.userEmail = userEmail;
        this.discountAmount = discountAmount;
    }
}

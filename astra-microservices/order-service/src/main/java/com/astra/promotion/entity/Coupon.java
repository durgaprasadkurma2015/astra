
package com.astra.promotion.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumOrderAmount;

    @Column(name = "maximum_discount_amount", precision = 12, scale = 2)
    private BigDecimal maximumDiscountAmount;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Coupon() {}

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getMinimumOrderAmount() { return minimumOrderAmount; }
    public BigDecimal getMaximumDiscountAmount() { return maximumDiscountAmount; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Boolean getActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCode(String code) { this.code = code; }
    public void setDescription(String description) { this.description = description; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) { this.minimumOrderAmount = minimumOrderAmount; }
    public void setMaximumDiscountAmount(BigDecimal maximumDiscountAmount) { this.maximumDiscountAmount = maximumDiscountAmount; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setActive(Boolean active) { this.active = active; }
}

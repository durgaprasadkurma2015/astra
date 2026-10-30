package com.astra.backend.coupon.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
    Long id,
    String code,
    String description,
    String discountType,
    BigDecimal discountValue,
    BigDecimal minimumOrderAmount,
    BigDecimal maximumDiscountAmount,
    LocalDateTime startsAt,
    LocalDateTime expiresAt,
    Boolean active,
    LocalDateTime createdAt,
    Integer usageLimit,
    Integer perCustomerUsageLimit,
    int usageCount
) {
}

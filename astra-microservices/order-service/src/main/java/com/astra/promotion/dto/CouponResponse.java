
package com.astra.promotion.dto;

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
    LocalDateTime createdAt
) {}

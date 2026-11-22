
package com.astra.promotion.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "[A-Za-z0-9_-]+",
        message = "Coupon code can contain letters, numbers, hyphens and underscores only")
    String code,

    @Size(max = 255)
    String description,

    @NotBlank
    @Pattern(regexp = "PERCENTAGE|FIXED",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Discount type must be PERCENTAGE or FIXED")
    String discountType,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal discountValue,

    @NotNull
    @DecimalMin("0.00")
    BigDecimal minimumOrderAmount,

    @DecimalMin("0.01")
    BigDecimal maximumDiscountAmount,

    @NotNull
    LocalDateTime startsAt,

    @NotNull
    LocalDateTime expiresAt
) {}

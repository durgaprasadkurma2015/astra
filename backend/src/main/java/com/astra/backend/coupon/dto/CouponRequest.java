package com.astra.backend.coupon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(

    @NotBlank
    @Size(max = 50)
    @Pattern(
        regexp = "[A-Za-z0-9_-]+",
        message = "Coupon code can contain letters, numbers, hyphens and underscores only"
    )
    String code,

    @Size(max = 255)
    String description,

    @NotBlank
    @Pattern(
        regexp = "PERCENTAGE|FIXED",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Discount type must be PERCENTAGE or FIXED"
    )
    String discountType,

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal discountValue,

    @NotNull
    @DecimalMin(value = "0.00")
    BigDecimal minimumOrderAmount,

    @DecimalMin(value = "0.01")
    BigDecimal maximumDiscountAmount,

    @NotNull
    LocalDateTime startsAt,

    @NotNull
    LocalDateTime expiresAt,

    @Positive
    Integer usageLimit,

    @Positive
    Integer perCustomerUsageLimit
) {
}

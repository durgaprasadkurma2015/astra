package com.astra.backend.coupon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CouponApplyRequest(
    @NotBlank
    @Size(max = 50)
    String couponCode,

    @NotNull
    @DecimalMin("0.00")
    BigDecimal subtotal
) {}
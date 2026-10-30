package com.astra.backend.coupon.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCouponActiveRequest(
    @NotNull Boolean active
) {
}
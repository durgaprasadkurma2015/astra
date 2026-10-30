package com.astra.backend.coupon.dto;

import java.math.BigDecimal;

public record ApplyCouponResponse(
        String couponCode,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String message
) {
}
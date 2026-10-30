package com.astra.backend.coupon.dto;

import java.math.BigDecimal;

public record CouponApplyResponse(
    String couponCode,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    String message
) {}
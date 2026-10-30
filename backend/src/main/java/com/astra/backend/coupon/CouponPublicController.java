package com.astra.backend.coupon;

import com.astra.backend.coupon.dto.CouponApplyRequest;
import com.astra.backend.coupon.dto.CouponApplyResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponPublicController {

    private final CouponService couponService;

    public CouponPublicController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/apply")
    public CouponApplyResponse applyCoupon(
            @Valid @RequestBody CouponApplyRequest request) {

        BigDecimal subtotal = request.subtotal()
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = couponService.calculateDiscount(
                request.couponCode(),
                subtotal
        );

        BigDecimal total = subtotal.subtract(discount)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        return new CouponApplyResponse(
                request.couponCode().trim().toUpperCase(Locale.ROOT),
                subtotal,
                discount,
                total,
                "Coupon applied successfully"
        );
    }
}
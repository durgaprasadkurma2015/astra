
package com.astra.promotion.controller;

import com.astra.promotion.dto.*;
import com.astra.promotion.service.CouponService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/coupons")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public CouponResponse createCoupon(
            @Valid @RequestBody CouponRequest request) {
        return couponService.createCoupon(request);
    }

    @GetMapping
    public List<CouponResponse> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @PatchMapping("/{id}/active")
    public CouponResponse updateActiveStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCouponActiveRequest request) {

        return couponService.updateActiveStatus(id, request.active());
    }
}

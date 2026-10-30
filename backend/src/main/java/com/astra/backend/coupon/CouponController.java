package com.astra.backend.coupon;

import com.astra.backend.coupon.dto.CouponRequest;
import com.astra.backend.coupon.dto.CouponResponse;
import com.astra.backend.coupon.dto.UpdateCouponActiveRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
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

        return couponService.updateActiveStatus(
                id,
                request.active()
        );
    }
}
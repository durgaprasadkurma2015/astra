package com.astra.backend.coupon;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {
    long countByCouponId(Long couponId);
    long countByCouponIdAndUserEmail(Long couponId, String userEmail);
}

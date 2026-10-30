package com.astra.backend.coupon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class CouponServiceTest {

    private final CouponRepository coupons =
            org.mockito.Mockito.mock(CouponRepository.class);
    private final CouponRedemptionRepository redemptions =
            org.mockito.Mockito.mock(CouponRedemptionRepository.class);
    private final CouponService service = new CouponService(coupons, redemptions);

    @Test
    void rejectsCouponWhenGlobalRedemptionLimitIsReached() {
        Coupon coupon = validCoupon();
        coupon.setUsageLimit(2);
        when(coupons.findByCodeIgnoreCaseForUpdate("SAVE10"))
                .thenReturn(Optional.of(coupon));
        when(coupons.findByCodeIgnoreCase("SAVE10"))
                .thenReturn(Optional.of(coupon));
        when(redemptions.countByCouponId(null)).thenReturn(2L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.prepareCheckout("SAVE10", new BigDecimal("100"), "user@example.test"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void rejectsCouponWhenCustomerLimitIsReached() {
        Coupon coupon = validCoupon();
        coupon.setPerCustomerUsageLimit(1);
        when(coupons.findByCodeIgnoreCaseForUpdate("SAVE10"))
                .thenReturn(Optional.of(coupon));
        when(coupons.findByCodeIgnoreCase("SAVE10"))
                .thenReturn(Optional.of(coupon));
        when(redemptions.countByCouponId(null)).thenReturn(0L);
        when(redemptions.countByCouponIdAndUserEmail(null, "user@example.test"))
                .thenReturn(1L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.prepareCheckout("SAVE10", new BigDecimal("100"), "user@example.test"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    private Coupon validCoupon() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        Coupon coupon = new Coupon();
        coupon.setCode("SAVE10");
        coupon.setDiscountType("PERCENTAGE");
        coupon.setDiscountValue(new BigDecimal("10.00"));
        coupon.setMinimumOrderAmount(BigDecimal.ZERO);
        coupon.setStartsAt(now.minusHours(1));
        coupon.setExpiresAt(now.plusHours(1));
        coupon.setActive(true);
        return coupon;
    }
}

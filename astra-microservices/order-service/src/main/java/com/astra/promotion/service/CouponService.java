
package com.astra.promotion.service;

import com.astra.promotion.dto.CouponRequest;
import com.astra.promotion.dto.CouponResponse;
import com.astra.promotion.entity.Coupon;
import com.astra.promotion.repository.CouponRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {

        String code = normalizeCode(request.code());
        String type = request.discountType().trim().toUpperCase(Locale.ROOT);

        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Coupon code already exists");
        }

        if (!request.expiresAt().isAfter(request.startsAt())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Coupon expiry must be after its start date");
        }

        if ("PERCENTAGE".equals(type)
                && request.discountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Percentage discount cannot exceed 100");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDescription(request.description());
        coupon.setDiscountType(type);
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinimumOrderAmount(request.minimumOrderAmount());
        coupon.setMaximumDiscountAmount(request.maximumDiscountAmount());
        coupon.setStartsAt(request.startsAt());
        coupon.setExpiresAt(request.expiresAt());
        coupon.setActive(true);

        return toResponse(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public CouponResponse updateActiveStatus(Long id, boolean active) {

        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Coupon not found"));

        coupon.setActive(active);

        return toResponse(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(
            String couponCode, BigDecimal subtotal) {

        if (couponCode == null || couponCode.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Coupon code is required");
        }

        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid order subtotal");
        }

        Coupon coupon = couponRepository
            .findByCodeIgnoreCase(normalizeCode(couponCode))
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Invalid coupon code"));

        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "This coupon is inactive");
        }

        if (now.isBefore(coupon.getStartsAt())
                || now.isAfter(coupon.getExpiresAt())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This coupon is not currently valid");
        }

        if (subtotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Minimum order amount for this coupon is "
                    + coupon.getMinimumOrderAmount());
        }

        BigDecimal discount;

        if ("PERCENTAGE".equals(coupon.getDiscountType())) {
            discount = subtotal.multiply(coupon.getDiscountValue())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }

        BigDecimal maximum = coupon.getMaximumDiscountAmount();

        if (maximum != null && discount.compareTo(maximum) > 0) {
            discount = maximum;
        }

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Coupon code is required");
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
            coupon.getId(),
            coupon.getCode(),
            coupon.getDescription(),
            coupon.getDiscountType(),
            coupon.getDiscountValue(),
            coupon.getMinimumOrderAmount(),
            coupon.getMaximumDiscountAmount(),
            coupon.getStartsAt(),
            coupon.getExpiresAt(),
            coupon.getActive(),
            coupon.getCreatedAt()
        );
    }
}

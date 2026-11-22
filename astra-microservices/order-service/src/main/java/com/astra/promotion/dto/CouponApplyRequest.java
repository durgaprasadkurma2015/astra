
package com.astra.promotion.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CouponApplyRequest(
    @NotBlank
    @Size(max = 50)
    String couponCode,

    @NotNull
    @DecimalMin("0.00")
    BigDecimal subtotal
) {}

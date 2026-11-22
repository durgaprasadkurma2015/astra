
package com.astra.promotion.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCouponActiveRequest(
    @NotNull Boolean active
) {}

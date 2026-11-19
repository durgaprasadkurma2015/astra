package com.astra.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartQuantityRequest(
    @NotNull @Min(1) Integer quantity
) {
}
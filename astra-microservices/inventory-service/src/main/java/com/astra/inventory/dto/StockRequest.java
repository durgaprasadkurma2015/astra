
package com.astra.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
    @NotNull Long productId,
    @Min(0) int quantity
) {}

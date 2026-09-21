package com.astra.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryAdjustmentRequest(

        @NotNull
        @Min(0)
        Integer quantity,

        String reason
) {
}
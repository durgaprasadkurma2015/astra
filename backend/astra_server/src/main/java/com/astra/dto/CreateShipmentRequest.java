package com.astra.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateShipmentRequest(

        @NotBlank
        String carrier,

        Integer estimatedDeliveryDays
) {
}
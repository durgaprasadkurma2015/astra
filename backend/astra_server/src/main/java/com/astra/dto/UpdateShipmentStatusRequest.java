package com.astra.dto;

import com.astra.enums.ShipmentStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateShipmentStatusRequest(

        @NotNull
        ShipmentStatus status,

        String location,

        String description
) {
}
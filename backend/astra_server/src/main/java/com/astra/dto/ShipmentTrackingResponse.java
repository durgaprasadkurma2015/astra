package com.astra.dto;

import java.time.LocalDateTime;

import com.astra.enums.ShipmentStatus;

public record ShipmentTrackingResponse(

        Long id,

        ShipmentStatus status,

        String location,

        String description,

        LocalDateTime createdAt
) {
}
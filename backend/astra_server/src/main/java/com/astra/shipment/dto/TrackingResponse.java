package com.astra.shipment.dto;

import java.time.LocalDateTime;

public record TrackingResponse(

        Long shipmentId,

        Long orderId,

        String orderNumber,

        String trackingNumber,

        String carrier,

        String status,

        String currentLocation,

        String estimatedDelivery,

        LocalDateTime shippedAt,

        LocalDateTime deliveredAt

) {
}

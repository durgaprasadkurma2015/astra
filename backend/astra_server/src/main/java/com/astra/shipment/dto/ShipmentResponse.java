package com.astra.shipment.dto;

import com.astra.dto.ShipmentTrackingResponse;
import com.astra.enums.ShipmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ShipmentResponse(

        Long id,

        Long orderId,

        String orderNumber,

        Long userId,

        String trackingNumber,

        String carrier,

        ShipmentStatus status,

        String shippingFullName,

        String shippingPhone,

        String shippingAddressLine1,

        String shippingAddressLine2,

        String shippingCity,

        String shippingState,

        String shippingPostalCode,

        String shippingCountry,

        LocalDate estimatedDeliveryDate,

        LocalDate actualDeliveryDate,

        LocalDateTime shippedAt,

        LocalDateTime deliveredAt,

        String currentLocation,

        String deliveryNote,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        List<ShipmentTrackingResponse> trackingHistory
) {
}
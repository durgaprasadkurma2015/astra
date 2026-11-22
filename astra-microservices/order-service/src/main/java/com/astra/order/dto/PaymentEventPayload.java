package com.astra.order.dto;

import java.math.BigDecimal;

public record PaymentEventPayload(
        String eventId,
        String eventType,
        String paymentReference,
        String orderNumber,
        String customerEmail,
        BigDecimal amount,
        String currency,
        String paymentStatus,
        String occurredAt
) {
}
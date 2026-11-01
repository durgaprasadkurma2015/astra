package com.astra.backend.event.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderPlacedEvent(
        Long orderId,
        String orderNumber,
        String customerEmail,
        BigDecimal totalAmount,
        String paymentMethod,
        Instant occurredAt
) {}
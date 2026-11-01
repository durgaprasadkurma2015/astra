package com.astra.backend.event.model;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        Long orderId,
        String orderNumber,
        String customerEmail,
        String paymentId,
        BigDecimal amount,
        Instant occurredAt
) {}
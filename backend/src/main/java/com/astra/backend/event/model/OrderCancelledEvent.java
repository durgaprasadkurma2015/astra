package com.astra.backend.event.model;

import java.time.Instant;

public record OrderCancelledEvent(
        Long orderId,
        String orderNumber,
        String customerEmail,
        Instant occurredAt
) {}
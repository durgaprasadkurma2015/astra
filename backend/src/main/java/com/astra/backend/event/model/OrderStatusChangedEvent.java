package com.astra.backend.event.model;

import java.time.Instant;

public record OrderStatusChangedEvent(
        Long orderId,
        String orderNumber,
        String previousStatus,
        String newStatus,
        Instant occurredAt
) {}
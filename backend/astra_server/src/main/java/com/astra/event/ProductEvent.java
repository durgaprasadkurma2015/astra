package com.astra.event;
import java.time.Instant;
public record ProductEvent(String type, Long productId, Instant occurredAt) {}

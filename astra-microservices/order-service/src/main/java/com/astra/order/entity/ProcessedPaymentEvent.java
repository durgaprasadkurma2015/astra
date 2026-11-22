package com.astra.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "processed_payment_events",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_processed_payment_event",
            columnNames = "event_id"
        )
    }
)
public class ProcessedPaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "order_number", nullable = false, length = 40)
    private String orderNumber;

    @Column(name = "processed_at", insertable = false, updatable = false)
    private LocalDateTime processedAt;

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String value) {
        eventId = value;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String value) {
        eventType = value;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String value) {
        orderNumber = value;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}

package com.astra.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_outbox_events")
public class OrderOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 150)
    private String topic;

    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    @Column(name = "event_status", nullable = false, length = 20)
    private String eventStatus = "PENDING";

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
    public String getEventStatus() { return eventStatus; }
    public Integer getRetryCount() { return retryCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }

    public void setEventId(String value) { eventId = value; }
    public void setAggregateType(String value) { aggregateType = value; }
    public void setAggregateId(String value) { aggregateId = value; }
    public void setEventType(String value) { eventType = value; }
    public void setTopic(String value) { topic = value; }
    public void setPayload(String value) { payload = value; }
    public void setEventStatus(String value) { eventStatus = value; }
    public void setRetryCount(Integer value) { retryCount = value; }
    public void setPublishedAt(LocalDateTime value) { publishedAt = value; }
}


package com.astra.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_events")
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 150)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Column(name = "order_number", length = 50)
    private String orderNumber;

    @Column(length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "delivery_status", nullable = false, length = 30)
    private String deliveryStatus = "PENDING";

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    

@Column(name = "attempt_count", nullable = false)
private Integer attemptCount = 0;

@Column(name = "max_attempts", nullable = false)
private Integer maxAttempts = 5;

@Column(name = "next_attempt_at")
private LocalDateTime nextAttemptAt;

@Column(name = "last_attempt_at")
private LocalDateTime lastAttemptAt;

@Column(name = "sent_at")
private LocalDateTime sentAt;

@Column(name = "locked_until")
private LocalDateTime lockedUntil;


    public NotificationEvent() {}

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getOrderNumber() { return orderNumber; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setMessage(String message) { this.message = message; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    

public Integer getAttemptCount() { return attemptCount; }
public Integer getMaxAttempts() { return maxAttempts; }
public LocalDateTime getNextAttemptAt() { return nextAttemptAt; }
public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }
public LocalDateTime getSentAt() { return sentAt; }
public LocalDateTime getLockedUntil() { return lockedUntil; }

public void setAttemptCount(Integer attemptCount) {
    this.attemptCount = attemptCount;
}

public void setMaxAttempts(Integer maxAttempts) {
    this.maxAttempts = maxAttempts;
}

public void setNextAttemptAt(LocalDateTime nextAttemptAt) {
    this.nextAttemptAt = nextAttemptAt;
}

public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
    this.lastAttemptAt = lastAttemptAt;
}

public void setSentAt(LocalDateTime sentAt) {
    this.sentAt = sentAt;
}

public void setLockedUntil(LocalDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
}

}


package com.astra.notification.dto;

import com.astra.notification.entity.NotificationEvent;
import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String eventId,
    String eventType,
    String recipientEmail,
    String orderNumber,
    String subject,
    String message,
    String deliveryStatus,
    String failureReason,
    Integer attemptCount,
    Integer maxAttempts,
    LocalDateTime nextAttemptAt,
    LocalDateTime sentAt,
    LocalDateTime createdAt
) {
    public static NotificationResponse from(NotificationEvent event) {
        return new NotificationResponse(
            event.getId(),
            event.getEventId(),
            event.getEventType(),
            event.getRecipientEmail(),
            event.getOrderNumber(),
            event.getSubject(),
            event.getMessage(),
            event.getDeliveryStatus(),
            event.getFailureReason(),
            event.getAttemptCount(),
            event.getMaxAttempts(),
            event.getNextAttemptAt(),
            event.getSentAt(),
            event.getCreatedAt()
        );
    }
}

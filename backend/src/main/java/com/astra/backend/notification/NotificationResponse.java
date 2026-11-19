package com.astra.backend.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String title,
    String message,
    String type,
    String referenceId,
    String targetUrl,
    boolean read,
    LocalDateTime createdAt
) {
    public static NotificationResponse from(
            UserNotification notification) {

        return new NotificationResponse(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getType(),
            notification.getReferenceId(),
            notification.getTargetUrl(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
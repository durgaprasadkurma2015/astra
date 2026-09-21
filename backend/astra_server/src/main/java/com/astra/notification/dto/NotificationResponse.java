package com.astra.notification.dto;

import java.time.LocalDateTime;

import com.astra.enums.NotificationChannel;
import com.astra.enums.NotificationStatus;
import com.astra.enums.NotificationType;

public record NotificationResponse(

        Long id,

        NotificationType type,

        NotificationChannel channel,

        NotificationStatus status,

        String subject,

        String message,

        String recipient,

        String referenceType,

        Long referenceId,

        Integer retryCount,

        LocalDateTime sentAt,

        LocalDateTime readAt,

        LocalDateTime createdAt
) {
}
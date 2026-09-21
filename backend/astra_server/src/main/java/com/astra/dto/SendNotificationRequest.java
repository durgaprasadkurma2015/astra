package com.astra.dto;

import com.astra.enums.NotificationChannel;
import com.astra.enums.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequest(

        @NotNull
        Long userId,

        @NotNull
        NotificationType type,

        @NotNull
        NotificationChannel channel,

        @NotBlank
        String subject,

        @NotBlank
        String message
) {
}
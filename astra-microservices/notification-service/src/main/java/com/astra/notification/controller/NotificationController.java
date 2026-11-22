
package com.astra.notification.controller;

import com.astra.notification.dto.NotificationResponse;
import com.astra.notification.service.NotificationService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping("/pending")
    public List<NotificationResponse> getPendingNotifications() {
        return notificationService.getPendingNotifications();
    }
}

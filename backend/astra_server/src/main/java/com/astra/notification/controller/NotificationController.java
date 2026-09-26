package com.astra.notification.controller;

import com.astra.dto.NotificationCountResponse;
import com.astra.dto.SendNotificationRequest;
import com.astra.notification.dto.NotificationResponse;
import com.astra.notification.service.NotificationService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final com.astra.notification.service.NotificationSseService notificationSseService;

    public NotificationController(
            NotificationService notificationService,
            com.astra.notification.service.NotificationSseService notificationSseService) {

        this.notificationService = notificationService;
        this.notificationSseService = notificationSseService;
    }

    @GetMapping
    public Page<NotificationResponse> getMyNotifications(
            java.security.Principal principal,
            Pageable pageable) {

        return notificationService.getMyNotifications(
                principal.getName(),
                pageable
        );
    }


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(java.security.Principal principal) {
        return notificationSseService.subscribe(principal.getName(), 30 * 60 * 1000L);
    }

    @GetMapping("/unread")
    public Page<NotificationResponse> getUnread(
            java.security.Principal principal,
            Pageable pageable) {

        return notificationService
                .getMyUnreadNotifications(
                        principal.getName(),
                        pageable
                );
    }

    @GetMapping("/unread/count")
    public NotificationCountResponse getUnreadCount(
            java.security.Principal principal) {

        return notificationService.getUnreadCount(
                principal.getName()
        );
    }

    @PutMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long notificationId,
            java.security.Principal principal) {

        return notificationService.markAsRead(
                notificationId,
                principal.getName()
        );
    }

    @PutMapping("/read-all")
    public void markAllAsRead(
            java.security.Principal principal) {

        notificationService.markAllAsRead(
                principal.getName()
        );
    }

    /*
     * Admin/system testing endpoint.
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificationResponse send(
            @Valid @RequestBody SendNotificationRequest request) {

        return notificationService.send(
                request.userId(),
                request.type(),
                request.channel(),
                request.subject(),
                request.message(),
                "SYSTEM",
                null
        );
    }
}
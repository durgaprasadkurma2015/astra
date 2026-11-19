package com.astra.backend.notification;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications(
            Principal principal) {

        return service.getNotifications(principal.getName());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Principal principal) {

        return Map.of(
            "count",
            service.getUnreadCount(principal.getName())
        );
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long id,
            Principal principal) {

        return service.markAsRead(id, principal.getName());
    }

    @PatchMapping("/read-all")
    public Map<String, Integer> markAllAsRead(
            Principal principal) {

        return Map.of(
            "updated",
            service.markAllAsRead(principal.getName())
        );
    }

    @GetMapping(value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(Principal principal) {

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(service.subscribe(principal.getName()));
    }
}
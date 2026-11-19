package com.astra.backend.notification;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    private final ConcurrentHashMap<String,
        CopyOnWriteArrayList<SseEmitter>> connections =
            new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NotificationResponse create(
            String email,
            String title,
            String message,
            String type,
            String referenceId,
            String targetUrl) {

        UserNotification notification = new UserNotification();

        notification.setUserEmail(email);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setTargetUrl(targetUrl);

        // Set notification creation timestamp
        notification.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        // Save notification into MySQL
        UserNotification saved = repository.saveAndFlush(notification);

        return NotificationResponse.from(saved);
    }

    public void publish(String email, NotificationResponse notification) {

    	 var emitters = connections.get(
    		        email.trim().toLowerCase(java.util.Locale.ROOT)
    		    );

    		    if (emitters == null) {
    		        return;
    		    }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("notification")
                        .id(String.valueOf(notification.id()))
                        .data(notification)
                );
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
                emitter.complete();
            }
        }
    }
    
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(
            java.util.Locale.ROOT
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String email) {

        return repository
            .findTop30ByUserEmailOrderByCreatedAtDesc(email)
            .stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {

        return repository.countByUserEmailAndReadAtIsNull(email);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, String email) {

        UserNotification notification = repository
            .findByIdAndUserEmail(id, email)
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Notification not found"
                )
            );

        if (notification.getReadAt() == null) {
            notification.setReadAt(
                LocalDateTime.now(ZoneOffset.UTC)
            );
        }

        return NotificationResponse.from(
            repository.save(notification)
        );
    }

    @Transactional
    public int markAllAsRead(String email) {
        return repository.markAllAsRead(email);
    }

    public SseEmitter subscribe(String email) {

        SseEmitter emitter = new SseEmitter(0L);

        String key = email.toLowerCase();

        connections.computeIfAbsent(
            key,
            ignored -> new CopyOnWriteArrayList<>()
        ).add(emitter);

        Runnable remove = () -> {
            var emitters = connections.get(key);

            if (emitters != null) {
                emitters.remove(emitter);

                if (emitters.isEmpty()) {
                    connections.remove(key, emitters);
                }
            }
        };

        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(error -> remove.run());

        try {
            emitter.send(
                SseEmitter.event()
                    .name("connected")
                    .data("Astra notification connection established")
            );
        } catch (IOException ex) {
            remove.run();
            emitter.completeWithError(ex);
        }

        return emitter;
    }
}
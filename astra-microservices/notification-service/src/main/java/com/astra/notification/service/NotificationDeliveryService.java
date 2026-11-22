
package com.astra.notification.service;

import com.astra.notification.entity.NotificationEvent;
import com.astra.notification.repository.NotificationRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationDeliveryService {

    private final NotificationRepository repository;

    public NotificationDeliveryService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Long> findDueNotifications() {
        return repository.findDueNotificationIds(
            LocalDateTime.now(),
            PageRequest.of(0, 50)
        );
    }

    @Transactional
    public Optional<NotificationEvent> claimNotification(Long id) {

        LocalDateTime now = LocalDateTime.now();

        int claimed = repository.claimForDelivery(
            id,
            now,
            now.plusMinutes(2)
        );

        if (claimed == 0) {
            return Optional.empty();
        }

        return repository.findById(id);
    }

    @Transactional
    public void markSent(Long id) {

        NotificationEvent notification = getNotification(id);

        notification.setDeliveryStatus("SENT");
        notification.setSentAt(LocalDateTime.now());
        notification.setNextAttemptAt(null);
        notification.setLockedUntil(null);
        notification.setFailureReason(null);

        repository.save(notification);
    }

    @Transactional
    public void markFailed(Long id, String reason) {

        NotificationEvent notification = getNotification(id);

        notification.setLockedUntil(null);
        notification.setFailureReason(limit(reason, 1000));

        if (notification.getAttemptCount()
                >= notification.getMaxAttempts()) {

            notification.setDeliveryStatus("FAILED");
            notification.setNextAttemptAt(null);

        } else {

            long delayMinutes = Math.min(
                16,
                1L << Math.max(0, notification.getAttemptCount() - 1)
            );

            notification.setDeliveryStatus("PENDING");
            notification.setNextAttemptAt(
                LocalDateTime.now().plusMinutes(delayMinutes)
            );
        }

        repository.save(notification);
    }

    @Transactional
    public void markPermanentFailure(Long id, String reason) {

        NotificationEvent notification = getNotification(id);

        notification.setDeliveryStatus("FAILED");
        notification.setFailureReason(limit(reason, 1000));
        notification.setNextAttemptAt(null);
        notification.setLockedUntil(null);

        repository.save(notification);
    }

    private NotificationEvent getNotification(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Notification not found: " + id
            ));
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "Unknown delivery error";
        }

        return value.length() <= maxLength
            ? value : value.substring(0, maxLength);
    }
}

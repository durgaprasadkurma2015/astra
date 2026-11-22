
package com.astra.notification.repository;

import com.astra.notification.entity.NotificationEvent;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository
        extends JpaRepository<NotificationEvent, Long> {

    boolean existsByEventId(String eventId);

    List<NotificationEvent> findAllByOrderByCreatedAtDesc();

    List<NotificationEvent> findByDeliveryStatusOrderByCreatedAtDesc(
        String deliveryStatus
    );

    @Query("""
        SELECT n.id
        FROM NotificationEvent n
        WHERE n.attemptCount < n.maxAttempts
          AND (
              (n.deliveryStatus = 'PENDING'
                  AND (n.nextAttemptAt IS NULL OR n.nextAttemptAt <= :now))
              OR
              (n.deliveryStatus = 'PROCESSING'
                  AND n.lockedUntil <= :now)
          )
        ORDER BY n.createdAt ASC
        """)
    List<Long> findDueNotificationIds(
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE NotificationEvent n
        SET n.deliveryStatus = 'PROCESSING',
            n.attemptCount = n.attemptCount + 1,
            n.lastAttemptAt = :now,
            n.lockedUntil = :lockedUntil
        WHERE n.id = :id
          AND n.attemptCount < n.maxAttempts
          AND (
              (n.deliveryStatus = 'PENDING'
                  AND (n.nextAttemptAt IS NULL OR n.nextAttemptAt <= :now))
              OR
              (n.deliveryStatus = 'PROCESSING'
                  AND n.lockedUntil <= :now)
          )
        """)
    int claimForDelivery(
        @Param("id") Long id,
        @Param("now") LocalDateTime now,
        @Param("lockedUntil") LocalDateTime lockedUntil
    );
}

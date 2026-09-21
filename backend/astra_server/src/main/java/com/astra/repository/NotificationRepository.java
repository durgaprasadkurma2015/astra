package com.astra.repository;

import com.astra.entity.Notification;
import com.astra.enums.NotificationStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            NotificationStatus status,
            Pageable pageable
    );

    long countByUserIdAndStatus(
            Long userId,
            NotificationStatus status
    );

    java.util.Optional<Notification>
    findByIdAndUserId(
            Long id,
            Long userId
    );
}

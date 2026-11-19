package com.astra.backend.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository
        extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findTop30ByUserEmailOrderByCreatedAtDesc(
        String userEmail
    );

    long countByUserEmailAndReadAtIsNull(String userEmail);

    Optional<UserNotification> findByIdAndUserEmail(
        Long id,
        String userEmail
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE UserNotification n
        SET n.readAt = CURRENT_TIMESTAMP
        WHERE n.userEmail = :email
          AND n.readAt IS NULL
    """)
    int markAllAsRead(@Param("email") String email);
}
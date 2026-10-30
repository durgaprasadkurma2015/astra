package com.astra.backend.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface PasswordResetRequestRepository
        extends JpaRepository<PasswordResetRequest, Long> {

    List<PasswordResetRequest>
        findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
            Long userId
        );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select r from PasswordResetRequest r
        where r.id = :id
        """)
    Optional<PasswordResetRequest> findByIdForUpdate(
        @Param("id") Long id
    );
}
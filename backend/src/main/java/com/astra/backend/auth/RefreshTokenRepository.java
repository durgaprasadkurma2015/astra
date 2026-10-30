package com.astra.backend.auth;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RefreshToken t WHERE t.tokenHash = :hash AND t.revokedAt IS NULL")
    Optional<RefreshToken> findActiveByHashForUpdate(@Param("hash") String hash);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revokedAt = :now WHERE LOWER(t.userEmail) = LOWER(:email) AND t.revokedAt IS NULL")
    int revokeAllForUser(@Param("email") String email,
            @Param("now") java.time.LocalDateTime now);
}

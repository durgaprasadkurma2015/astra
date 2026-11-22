
package com.astra.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "password_reset_otp_requests",
    indexes = {
        @Index(name = "idx_password_reset_user", columnList = "user_id"),
        @Index(name = "idx_password_reset_created", columnList = "created_at")
    }
)
public class PasswordResetOtpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "otp_hash", nullable = false, length = 64)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PasswordResetOtpRequest() {
    }

    public PasswordResetOtpRequest(
            AppUser user,
            String otpHash,
            LocalDateTime expiresAt,
            LocalDateTime now) {

        this.user = user;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
        this.lastSentAt = now;
        this.createdAt = now;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

    public void incrementAttempts() {
        attempts++;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}

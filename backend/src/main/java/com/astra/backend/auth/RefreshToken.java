package com.astra.backend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_owner_expiry", columnList = "user_email, expires_at"),
        @Index(name = "idx_refresh_tokens_revoked_expiry", columnList = "revoked_at, expires_at")
})
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private boolean persistent;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RefreshToken() { }

    public RefreshToken(String userEmail, String tokenHash, boolean persistent,
            LocalDateTime expiresAt) {
        this.userEmail = userEmail;
        this.tokenHash = tokenHash;
        this.persistent = persistent;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getTokenHash() { return tokenHash; }
    public boolean isPersistent() { return persistent; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime value) { revokedAt = value; }
}

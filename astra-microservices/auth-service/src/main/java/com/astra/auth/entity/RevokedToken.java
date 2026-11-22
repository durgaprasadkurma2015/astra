
package com.astra.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "revoked_tokens",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_revoked_tokens_token_id",
            columnNames = "token_id"
        )
    },
    indexes = {
        @Index(
            name = "idx_revoked_token_expiry",
            columnList = "expires_at"
        )
    }
)
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_id", nullable = false, length = 36)
    private String tokenId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    public RevokedToken() {
    }

    public RevokedToken(
            String tokenId,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt) {
        this.tokenId = tokenId;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}

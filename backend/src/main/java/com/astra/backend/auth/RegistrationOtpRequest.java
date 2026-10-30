
package com.astra.backend.auth;

import java.time.LocalDateTime;

import com.astra.backend.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "registration_otp_requests")
public class RegistrationOtpRequest {

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

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected RegistrationOtpRequest() {}

    public RegistrationOtpRequest(
        AppUser user,
        String otpHash,
        LocalDateTime expiresAt,
        LocalDateTime now
    ) {
        this.user = user;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
        this.lastSentAt = now;
        this.createdAt = now;
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public String getOtpHash() { return otpHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public int getAttempts() { return attempts; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public LocalDateTime getLastSentAt() { return lastSentAt; }

    public void incrementAttempts() { attempts++; }
    public void setVerifiedAt(LocalDateTime value) { verifiedAt = value; }
    public void setUsedAt(LocalDateTime value) { usedAt = value; }
    public void setLastSentAt(LocalDateTime value) { lastSentAt = value; }
}

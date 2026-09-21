package com.astra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "otp_verifications", indexes = {
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_phone", columnList = "phone")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 190)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 64)
    private String otpHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

package com.astra.service;

import com.astra.entity.OtpVerification;
import com.astra.exception.ApiException;
import com.astra.repository.OtpVerificationRepository;
import com.astra.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class OtpService {
    private final OtpVerificationRepository repository;
    private final SecureRandom random = new SecureRandom();
    private final long expiryMinutes;

    public OtpService(OtpVerificationRepository repository,
                      @Value("${astra.otp.expiry-minutes:10}") long expiryMinutes) {
        this.repository = repository;
        this.expiryMinutes = expiryMinutes;
    }

    public String createEmailOtp(String email) {
        repository.findTopByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(email)
                .ifPresent(existing -> { existing.setUsed(true); repository.save(existing); });
        String otp = generate();
        repository.save(OtpVerification.builder()
                .email(email.toLowerCase())
                .otpHash(HashUtil.sha256(otp))
                .expiresAt(Instant.now().plus(Duration.ofMinutes(expiryMinutes)))
                .build());
        System.out.println("[ASTRA EMAIL OTP] " + email + " -> " + otp);
        return otp;
    }

    public String createSmsOtp(String phone) {
        repository.findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(phone)
                .ifPresent(existing -> { existing.setUsed(true); repository.save(existing); });
        String otp = generate();
        repository.save(OtpVerification.builder()
                .phone(phone)
                .otpHash(HashUtil.sha256(otp))
                .expiresAt(Instant.now().plus(Duration.ofMinutes(expiryMinutes)))
                .build());
        System.out.println("[ASTRA SMS OTP - LOCAL DEV] " + phone + " -> " + otp);
        return otp;
    }

    public void verifyEmail(String email, String otp) {
        var record = repository.findTopByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "OTP not found or expired."));
        verify(record, otp);
    }

    public void verifyPhone(String phone, String otp) {
        var record = repository.findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "SMS OTP not found or expired."));
        verify(record, otp);
    }

    private void verify(OtpVerification record, String otp) {
        if (record.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP has expired.");
        }
        if (!HashUtil.sha256(otp).equals(record.getOtpHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid OTP.");
        }
        record.setUsed(true);
        repository.save(record);
    }

    private String generate() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}

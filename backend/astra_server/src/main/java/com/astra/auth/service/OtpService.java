package com.astra.auth.service;

import com.astra.entity.OtpVerification;
import com.astra.exception.ApiException;
import com.astra.repository.OtpVerificationRepository;
import com.astra.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class OtpService {

    private final OtpVerificationRepository repository;
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();
    private final long expiryMinutes;

    public OtpService(
            OtpVerificationRepository repository,
            EmailService emailService,
            @Value("${astra.otp.expiry-minutes:10}")
            long expiryMinutes) {

        this.repository = repository;
        this.emailService = emailService;
        this.expiryMinutes = expiryMinutes;
    }


    // =========================================================
    // EMAIL OTP
    // =========================================================

    @Transactional
    public String createEmailOtp(String email) {

        String normalizedEmail =
                email.trim().toLowerCase();

        repository
                .findTopByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(
                        normalizedEmail
                )
                .ifPresent(existing -> {
                    existing.setUsed(true);
                    repository.save(existing);
                });

        String otp = generate();

        OtpVerification record =
                OtpVerification.builder()
                        .email(normalizedEmail)
                        .otpHash(HashUtil.sha256(otp))
                        .expiresAt(
                                Instant.now()
                                        .plus(
                                                Duration.ofMinutes(
                                                        expiryMinutes
                                                )
                                        )
                        )
                        .used(false)
                        .build();

        repository.save(record);

        // Send actual email
        emailService.sendOtp(
                normalizedEmail,
                otp
        );

        return otp;
    }


    // =========================================================
    // SMS OTP
    // =========================================================

    @Transactional
    public String createSmsOtp(String phone) {

        if (phone == null || phone.isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number is required."
            );
        }

        repository
                .findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(phone)
                .ifPresent(existing -> {
                    existing.setUsed(true);
                    repository.save(existing);
                });

        String otp = generate();

        OtpVerification record =
                OtpVerification.builder()
                        .phone(phone)
                        .otpHash(HashUtil.sha256(otp))
                        .expiresAt(
                                Instant.now()
                                        .plus(
                                                Duration.ofMinutes(
                                                        expiryMinutes
                                                )
                                        )
                        )
                        .used(false)
                        .build();

        repository.save(record);

        // DEV ONLY
        System.out.println(
                "[ASTRA SMS OTP - LOCAL DEV] "
                        + phone
                        + " -> "
                        + otp
        );

        return otp;
    }

    // =========================================================
    // VERIFY EMAIL
    // =========================================================

    @Transactional
    public void verifyEmail(String email, String otp) {

        if (email == null || otp == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Email and OTP are required."
            );
        }

        OtpVerification record =
                repository
                        .findTopByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(
                                email.trim().toLowerCase()
                        )
                        .orElseThrow(() ->
                                new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "OTP not found or expired."
                                )
                        );

        verify(record, otp);
    }

    // =========================================================
    // VERIFY PHONE
    // =========================================================

    @Transactional
    public void verifyPhone(String phone, String otp) {

        if (phone == null || otp == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Phone and OTP are required."
            );
        }

        OtpVerification record =
                repository
                        .findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(
                                phone
                        )
                        .orElseThrow(() ->
                                new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "SMS OTP not found or expired."
                                )
                        );

        verify(record, otp);
    }

    // =========================================================
    // COMMON VERIFY
    // =========================================================

    private void verify(
            OtpVerification record,
            String otp) {

        if (record.getExpiresAt()
                .isBefore(Instant.now())) {

            record.setUsed(true);
            repository.save(record);

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "OTP has expired."
            );
        }

        String normalizedOtp = otp.trim();

        if (!normalizedOtp.matches("\\d{6}")) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "OTP must be 6 digits."
            );
        }

        String suppliedHash =
                HashUtil.sha256(normalizedOtp);

        if (!suppliedHash.equals(record.getOtpHash())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP."
            );
        }

        // OTP becomes unusable immediately.
        record.setUsed(true);

        repository.save(record);
    }

    // =========================================================
    // GENERATE OTP
    // =========================================================

    private String generate() {

        return String.format(
                "%06d",
                random.nextInt(1_000_000)
        );
    }
}

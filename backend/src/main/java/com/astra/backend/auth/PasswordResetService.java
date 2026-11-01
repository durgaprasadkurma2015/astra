package com.astra.backend.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.astra.backend.auth.dto.ForgotPasswordRequest;
import com.astra.backend.auth.dto.ResetPasswordRequest;
import com.astra.backend.auth.dto.VerifyOtpRequest;
import com.astra.backend.user.AppUser;
import com.astra.backend.user.AppUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.astra.backend.audit.AuditService;

@Service
public class PasswordResetService {

    private final AppUserRepository users;
    private final PasswordResetRequestRepository requests;
    private final PasswordResetEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final byte[] hmacSecret;

    private final int otpExpirySeconds;
    private final int tokenExpirySeconds;
    private final int cooldownSeconds;
    private final int maxAttempts;
    private final AuditService auditService;
    private final RefreshTokenRepository refreshTokens;

    private final SecureRandom random = new SecureRandom();
    

    public PasswordResetService(
        AppUserRepository users,
        PasswordResetRequestRepository requests,
        PasswordResetEmailService emailService,
        PasswordEncoder passwordEncoder,
        @Value("${app.password-reset.otp-hmac-secret}") String secret,
        @Value("${app.password-reset.otp-expiration-seconds:300}")
            int otpExpirySeconds,
        @Value("${app.password-reset.token-expiration-seconds:600}")
            int tokenExpirySeconds,
        @Value("${app.password-reset.resend-cooldown-seconds:60}")
            int cooldownSeconds,
            @Value("${app.password-reset.max-attempts:5}")
        int maxAttempts,
        AuditService auditService,
        RefreshTokenRepository refreshTokens
        ) {
        this.users = users;
        this.requests = requests;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.hmacSecret = Base64.getDecoder().decode(secret);
        this.otpExpirySeconds = otpExpirySeconds;
        this.tokenExpirySeconds = tokenExpirySeconds;
        this.cooldownSeconds = cooldownSeconds;
        this.maxAttempts = maxAttempts;
        this.auditService = auditService;
        this.refreshTokens = refreshTokens;

        if (hmacSecret.length < 32) {
            throw new IllegalArgumentException(
                "OTP HMAC secret must be at least 32 bytes"
            );
        }
    }

    @Transactional
    public void requestOtp(ForgotPasswordRequest input) {
        String email = normalizeEmail(input.email());

        AppUser user = users.findByEmail(email).orElse(null);

        // Do not reveal whether an account exists.
        if (user == null || !user.isActive()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);

        var previous = requests
            .findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                user.getId()
            );

        if (!previous.isEmpty()) {
            PasswordResetRequest latest = previous.get(0);

            if (latest.getLastSentAt().plusSeconds(cooldownSeconds)
                    .isAfter(now)) {
                return;
            }
        }

        String otp = String.format(
            Locale.ROOT, "%06d", random.nextInt(1_000_000)
        );

        PasswordResetRequest request = new PasswordResetRequest(
            user,
            hashOtp(email, otp),
            now.plusSeconds(otpExpirySeconds),
            now
        );

        requests.saveAndFlush(request);

        try {
            emailService.sendOtp(email, otp);
        } catch (RuntimeException ex) {
            // Do not leave an active OTP if delivery failed.
            requests.delete(request);
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Email delivery is temporarily unavailable"
            );
        }
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest input) {
        String email = normalizeEmail(input.email());
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);

        AppUser user = users.findByEmail(email).orElse(null);

        if (user == null) {
            return null;
        }

        var candidates = requests
            .findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                user.getId()
            );

        if (candidates.isEmpty()) {
            return null;
        }

        PasswordResetRequest request = requests
            .findByIdForUpdate(candidates.get(0).getId())
            .orElse(null);

        if (request == null
                || request.getUsedAt() != null
                || request.getVerifiedAt() != null
                || !request.getExpiresAt().isAfter(now)
                || request.getAttempts() >= maxAttempts) {
            return null;
        }

        request.setAttempts(request.getAttempts() + 1);

        String submittedHash = hashOtp(email, input.otp());

        boolean valid = MessageDigest.isEqual(
            submittedHash.getBytes(StandardCharsets.UTF_8),
            request.getOtpHash().getBytes(StandardCharsets.UTF_8)
        );

        if (!valid) {
            if (request.getAttempts() >= maxAttempts) {
                request.setUsedAt(now);
            }

            // Return normally so the failed attempt is committed.
            return null;
        }

        String resetToken = generateSecureToken();

        request.setVerifiedAt(now);
        request.setResetTokenHash(hashToken(resetToken));

        return resetToken;
    }
    @Transactional
    public void resetPassword(ResetPasswordRequest input) {
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        String tokenHash = hashToken(input.resetToken());

        PasswordResetRequest request = requests
        	    .findByResetTokenHashForUpdate(tokenHash)
        	    .orElseThrow(this::invalidResetToken);

        if (request.getUsedAt() != null
        	    || request.getVerifiedAt() == null
        	    || request.getResetTokenHash() == null
        	    || !request.getVerifiedAt()
        	        .plusSeconds(tokenExpirySeconds).isAfter(now)) {
        	    throw invalidResetToken();
        	}

        AppUser user = users.findById(request.getUser().getId())
            .orElseThrow(this::invalidResetToken);

        user.setPasswordHash(
            passwordEncoder.encode(input.newPassword())
        );

        users.save(user);
     // Revoke all existing refresh tokens after password reset.
        refreshTokens.revokeAllForUser(
            user.getEmail(),
            now
        );
        request.setUsedAt(now);
        request.setResetTokenHash(null);
        requests.save(request);
        auditService.log(
        	    user.getEmail(),
        	    "RESET_PASSWORD",
        	    "SUCCESS",
        	    "Password reset completed",
        	    null
        	);
    }

    private String hashOtp(String email, String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret, "HmacSHA256"));

            byte[] data = (email + ":" + otp)
                .getBytes(StandardCharsets.UTF_8);

            return HexFormat.of().formatHex(mac.doFinal(data));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash OTP", ex);
        }
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException(
                "Unable to hash reset token", ex
            );
        }
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException invalidOtp() {
        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid or expired OTP"
        );
    }

    private ResponseStatusException invalidResetToken() {
        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid or expired reset token"
        );
    }
}
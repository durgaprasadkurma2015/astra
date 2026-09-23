package com.astra.auth.service;

import com.astra.entity.PasswordResetToken;
import com.astra.entity.User;
import com.astra.exception.ApiException;
import com.astra.repository.PasswordResetTokenRepository;
import com.astra.repository.UserRepository;
import com.astra.util.HashUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class PasswordResetService {

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    private final long expiryMinutes;
    private final String frontendUrl;

    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(
            UserRepository users,
            PasswordResetTokenRepository tokens,
            PasswordEncoder encoder,
            EmailService emailService,

            @Value("${astra.password-reset.expiry-minutes:30}")
            long expiryMinutes,

            @Value("${astra.frontend.url:http://localhost:3000}")
            String frontendUrl) {

        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.emailService = emailService;
        this.expiryMinutes = expiryMinutes;
        this.frontendUrl = frontendUrl;
    }

    // =========================================================
    // REQUEST PASSWORD RESET
    // =========================================================

    @Transactional
    public void request(String email) {

        users.findByEmailIgnoreCase(email)
                .ifPresent(user -> {

                    // Invalidate previous reset tokens
                    tokens.deleteByUserId(user.getId());

                    // Generate secure random token
                    String raw = randomToken();

                    // Store only SHA-256 hash
                    tokens.save(
                            PasswordResetToken.builder()
                                    .user(user)
                                    .tokenHash(
                                            HashUtil.sha256(raw)
                                    )
                                    .expiresAt(
                                            Instant.now()
                                                    .plus(
                                                            Duration.ofMinutes(
                                                                    expiryMinutes
                                                            )
                                                    )
                                    )
                                    .build()
                    );

                    // Token goes to the user by email
                    String resetLink =
                            frontendUrl
                                    + "/reset-password?token="
                                    + raw;

                    emailService.sendPasswordReset(
                            user.getEmail(),
                            resetLink
                    );
                });
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Transactional
    public void reset(
            String rawToken,
            String newPassword) {

        if (rawToken == null || rawToken.isBlank()) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired reset token."
            );
        }

        PasswordResetToken token =
                tokens.findByTokenHashAndUsedFalse(
                        HashUtil.sha256(rawToken)
                )
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid or expired reset token."
                        )
                );

        // Check expiration
        if (token.getExpiresAt()
                .isBefore(Instant.now())) {

            token.setUsed(true);
            tokens.save(token);

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Reset token has expired."
            );
        }

        User user = token.getUser();

        // Encode the new password
        user.setPassword(
                encoder.encode(newPassword)
        );

        users.save(user);

        // Make token single-use
        token.setUsed(true);
        tokens.save(token);
    }

    // =========================================================
    // GENERATE RANDOM TOKEN
    // =========================================================

    private String randomToken() {

        byte[] bytes = new byte[32];

        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}

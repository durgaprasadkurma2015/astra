package com.astra.service;

import com.astra.entity.PasswordResetToken;
import com.astra.exception.ApiException;
import com.astra.repository.PasswordResetTokenRepository;
import com.astra.repository.UserRepository;
import com.astra.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class PasswordResetService {
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final long expiryMinutes;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
                                PasswordEncoder encoder,
                                @Value("${astra.password-reset.expiry-minutes:30}") long expiryMinutes) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.expiryMinutes = expiryMinutes;
    }

    public void request(String email) {
        users.findByEmailIgnoreCase(email).ifPresent(user -> {
            String raw = randomToken();
            tokens.save(PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(HashUtil.sha256(raw))
                    .expiresAt(Instant.now().plus(Duration.ofMinutes(expiryMinutes)))
                    .build());
            System.out.println("[ASTRA PASSWORD RESET TOKEN - LOCAL DEV] " + email + " -> " + raw);
        });
    }

    public void reset(String rawToken, String newPassword) {
        PasswordResetToken token = tokens.findByTokenHashAndUsedFalse(HashUtil.sha256(rawToken))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token."));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token has expired.");
        }
        var user = token.getUser();
        user.setPassword(encoder.encode(newPassword));
        users.save(user);
        token.setUsed(true);
        tokens.save(token);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

package com.astra.auth.service;

import com.astra.entity.RefreshToken;
import com.astra.entity.User;
import com.astra.exception.ApiException;
import com.astra.repository.RefreshTokenRepository;
import com.astra.security.JwtService;
import com.astra.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {

    private final RefreshTokenRepository repository;
    private final JwtService jwtService;
    private final long refreshDays;

    private final SecureRandom random = new SecureRandom();

    public TokenService(
            RefreshTokenRepository repository,
            JwtService jwtService,
            @Value("${astra.jwt.refresh-expiration-days:30}")
            long refreshDays) {

        this.repository = repository;
        this.jwtService = jwtService;
        this.refreshDays = refreshDays;
    }

    // =========================================================
    // ACCESS TOKEN
    // =========================================================

    public String issueAccessToken(User user) {
        return jwtService.generateAccessToken(user);
    }

    // =========================================================
    // CREATE REFRESH TOKEN
    // =========================================================

    @Transactional
    public String issueRefreshToken(User user) {

        String rawToken = randomToken();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(HashUtil.sha256(rawToken))
                .expiresAt(
                        Instant.now()
                                .plus(
                                        Duration.ofDays(refreshDays)
                                )
                )
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        repository.save(token);

        return rawToken;
    }

    // =========================================================
    // VALIDATE REFRESH TOKEN
    // =========================================================

    @Transactional(readOnly = true)
    public User validateRefreshToken(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token is required."
            );
        }

        String tokenHash = HashUtil.sha256(rawToken);

        RefreshToken token = repository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token."
                ));

        if (token.isRevoked()) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has been revoked."
            );
        }

        if (!token.getExpiresAt().isAfter(Instant.now())) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has expired."
            );
        }

        return token.getUser();
    }

    // =========================================================
    // REVOKE REFRESH TOKEN
    // =========================================================

    @Transactional
    public void revoke(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String tokenHash = HashUtil.sha256(rawToken);

        repository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    repository.save(token);
                });
    }

    // =========================================================
    // RANDOM TOKEN
    // =========================================================

    private String randomToken() {

        byte[] bytes = new byte[64];

        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}

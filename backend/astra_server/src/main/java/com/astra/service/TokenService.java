package com.astra.service;

import com.astra.entity.RefreshToken;
import com.astra.entity.User;
import com.astra.exception.ApiException;
import com.astra.repository.RefreshTokenRepository;
import com.astra.security.JwtService;
import com.astra.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    public TokenService(RefreshTokenRepository repository, JwtService jwtService,
                        @Value("${astra.jwt.refresh-expiration-days:7}") long refreshDays) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.refreshDays = refreshDays;
    }

    public String issueRefreshToken(User user) {
        String raw = randomToken();
        repository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(HashUtil.sha256(raw))
                .expiresAt(Instant.now().plus(Duration.ofDays(refreshDays)))
                .build());
        return raw;
    }

    public User validateRefreshToken(String raw) {
        var token = repository.findByTokenHash(HashUtil.sha256(raw))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token."));
        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked.");
        }
        return token.getUser();
    }

    public void revoke(String raw) {
        repository.findByTokenHash(HashUtil.sha256(raw)).ifPresent(token -> {
            token.setRevoked(true);
            repository.save(token);
        });
    }

    public String issueAccessToken(User user) {
        return jwtService.generateAccessToken(user);
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

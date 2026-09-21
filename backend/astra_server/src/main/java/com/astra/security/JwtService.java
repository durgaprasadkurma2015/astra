package com.astra.security;

import com.astra.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

private final SecretKey key;
private final long accessExpirationMs;

public JwtService(
        @Value("${astra.security.jwt.secret}") String secret,
        @Value("${astra.security.jwt.access-token-expiration:900000}") long accessExpirationMs) {

    if (secret == null || secret.isBlank()) {
        throw new IllegalArgumentException(
                "astra.security.jwt.secret must not be empty"
        );
    }

    if (secret.length() < 32) {
        throw new IllegalArgumentException(
                "astra.security.jwt.secret must contain at least 32 characters"
        );
    }

    this.key = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
    );

    this.accessExpirationMs = accessExpirationMs;
}

public String generateAccessToken(User user) {

    Instant now = Instant.now();

    return Jwts.builder()
            .subject(user.getEmail())
            .claim("userId", user.getId())
            .claim("role", user.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(
                    new Date(
                            System.currentTimeMillis()
                                    + accessExpirationMs
                    )
            )
            .signWith(key)
            .compact();
}

public String extractSubject(String token) {

    return parse(token)
            .getPayload()
            .getSubject();
}

public boolean isValid(String token) {

    try {
        parse(token);
        return true;

    } catch (JwtException | IllegalArgumentException ex) {
        return false;
    }
}

private Jws<Claims> parse(String token) {

    return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token);
}

public Duration accessTokenLifetime() {

    return Duration.ofMillis(accessExpirationMs);
}}
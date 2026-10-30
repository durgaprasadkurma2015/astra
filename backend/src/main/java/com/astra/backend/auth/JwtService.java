package com.astra.backend.auth;

import java.util.Date;
import java.util.Base64;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-ms:900000}") long expirationMs
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                "JWT secret must decode to at least 32 bytes"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();

        return Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expirationMs))
            .signWith(signingKey)
            .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(
        String token,
        UserDetails userDetails
    ) {
        Claims claims = parseClaims(token);

        return claims.getSubject().equals(userDetails.getUsername())
            && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
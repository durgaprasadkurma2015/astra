package com.astra.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:900000}")
    private long expirationMs;

    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret must contain at least 256 bits"
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(CustomUserDetails user) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
        	.id(UUID.randomUUID().toString())
            .subject(user.getUsername())
            .claim("userId", user.getId())
            .claim("role", user.getRole())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractTokenId(String token) {
        return extractAllClaims(token).getId();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        Claims claims = extractAllClaims(token);

        return claims.getSubject().equals(userDetails.getUsername())
            && claims.getExpiration().after(new Date());
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
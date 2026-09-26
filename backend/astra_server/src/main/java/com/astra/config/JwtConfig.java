package com.astra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "astra.security.jwt")
public record JwtConfig(
        String secret,
        long accessTokenExpiration,
        long refreshTokenExpiration
) {
}

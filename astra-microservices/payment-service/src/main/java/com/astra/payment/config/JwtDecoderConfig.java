
package com.astra.payment.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(
        @Value("${app.jwt.secret}") String secret
    ) {
        byte[] decoded = Base64.getDecoder().decode(secret);

        if (decoded.length < 32) {
            throw new IllegalArgumentException(
                "JWT secret must contain at least 32 bytes"
            );
        }

        SecretKey key = new SecretKeySpec(decoded, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}

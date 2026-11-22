
package com.astra.gateway.security;

import java.time.Duration;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class RevocationCheckingJwtDecoder implements ReactiveJwtDecoder {

    private final ReactiveJwtDecoder delegate;
    private final WebClient webClient;
    private final String internalApiKey;

    public RevocationCheckingJwtDecoder(
            ReactiveJwtDecoder delegate,
            WebClient webClient,
            String internalApiKey) {

        this.delegate = delegate;
        this.webClient = webClient;
        this.internalApiKey = internalApiKey;
    }

    @Override
    public Mono<Jwt> decode(String token) {

        return delegate.decode(token)
            .flatMap(jwt -> {

                String tokenId = jwt.getId();

                if (tokenId == null || tokenId.isBlank()) {
                    return Mono.error(
                        new JwtException("JWT token ID (jti) is missing")
                    );
                }

                return webClient.get()
                    .uri(
                        "/internal/auth/tokens/{tokenId}/revocation-status",
                        tokenId
                    )
                    .header("X-Internal-Api-Key", internalApiKey)
                    .retrieve()
                    .bodyToMono(TokenRevocationResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .flatMap(result -> {

                        if (result.revoked()) {
                            return Mono.error(
                                new JwtException("JWT token has been revoked")
                            );
                        }

                        return Mono.just(jwt);
                    })
                    .onErrorResume(ex -> {

                        if (ex instanceof JwtException) {
                            return Mono.error(ex);
                        }

                        // Fail closed if Auth Service is unavailable.
                        return Mono.error(
                            new JwtException(
                                "Unable to verify JWT revocation status"
                            )
                        );
                    });
            });
    }

    private record TokenRevocationResponse(boolean revoked) {
    }
}

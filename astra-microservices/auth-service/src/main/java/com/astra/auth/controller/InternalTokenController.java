
package com.astra.auth.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.astra.auth.repository.RevokedTokenRepository;

@RestController
@RequestMapping("/internal/auth/tokens")
public class InternalTokenController {

    private final RevokedTokenRepository revokedTokenRepository;
    private final String internalApiKey;

    public InternalTokenController(
            RevokedTokenRepository revokedTokenRepository,
            @Value("${app.internal-api-key}") String internalApiKey) {

        this.revokedTokenRepository = revokedTokenRepository;
        this.internalApiKey = internalApiKey;
    }

    @GetMapping("/{tokenId}/revocation-status")
    public RevocationResponse checkRevocation(
            @PathVariable String tokenId,
            @RequestHeader(
                value = "X-Internal-Api-Key",
                required = false
            ) String providedApiKey) {

        if (providedApiKey == null ||
                !MessageDigest.isEqual(
                    internalApiKey.getBytes(StandardCharsets.UTF_8),
                    providedApiKey.getBytes(StandardCharsets.UTF_8)
                )) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Invalid internal API key"
            );
        }

        boolean revoked =
            revokedTokenRepository.existsByTokenId(tokenId);

        return new RevocationResponse(revoked);
    }

    public record RevocationResponse(boolean revoked) {
    }
}

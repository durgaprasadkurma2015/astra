
package com.astra.auth.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astra.auth.repository.RevokedTokenRepository;

@Service
public class RevokedTokenCleanupService {

    private static final Logger log =
            LoggerFactory.getLogger(RevokedTokenCleanupService.class);

    private final RevokedTokenRepository revokedTokenRepository;

    public RevokedTokenCleanupService(
            RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Scheduled(cron = "${app.security.revoked-token-cleanup-cron:0 0 * * * *}")
    @Transactional
    public void cleanupExpiredTokens() {

        long deletedCount =
                revokedTokenRepository.deleteByExpiresAtBefore(
                        LocalDateTime.now()
                );

        if (deletedCount > 0) {
            log.info(
                "Revoked token cleanup completed. Deleted {} expired tokens.",
                deletedCount
            );
        }
    }
}

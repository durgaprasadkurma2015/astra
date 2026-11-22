
package com.astra.auth.repository;

import com.astra.auth.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, Long> {

    boolean existsByTokenId(String tokenId);

    long deleteByExpiresAtBefore(LocalDateTime now);
    
    
}


package com.astra.auth.repository;

import com.astra.auth.entity.PasswordResetOtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetOtpRequestRepository
        extends JpaRepository<PasswordResetOtpRequest, Long> {

    List<PasswordResetOtpRequest>
        findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(Long userId);

    Optional<PasswordResetOtpRequest>
        findTopByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(Long userId);

    Optional<PasswordResetOtpRequest>
        findTopByUser_IdOrderByCreatedAtDesc(Long userId);
}

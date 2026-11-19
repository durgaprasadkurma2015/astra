package com.astra.auth.repository;

import com.astra.auth.entity.RegistrationOtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationOtpRequestRepository
        extends JpaRepository<RegistrationOtpRequest, Long> {

    List<RegistrationOtpRequest>
        findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(Long userId);

    Optional<RegistrationOtpRequest>
        findTopByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(Long userId);

    Optional<RegistrationOtpRequest>
        findTopByUser_IdOrderByCreatedAtDesc(Long userId);
}
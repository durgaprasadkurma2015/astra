package com.astra.repository;

import com.astra.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(String email);
    Optional<OtpVerification> findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(String phone);
}

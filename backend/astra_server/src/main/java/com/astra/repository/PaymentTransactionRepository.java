package com.astra.repository;

import com.astra.entity.PaymentTransaction;
import com.astra.enums.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByIdAndUserId(
            Long id,
            Long userId
    );

    Optional<PaymentTransaction> findByOrderIdAndUserId(
            Long orderId,
            Long userId
    );

    Optional<PaymentTransaction> findByProviderPaymentId(
            String providerPaymentId
    );

    Optional<PaymentTransaction> findByTransactionNumber(
            String transactionNumber
    );

    boolean existsByTransactionNumber(
            String transactionNumber
    );

    boolean existsByOrderIdAndStatus(
            Long orderId,
            PaymentStatus status
    );
}

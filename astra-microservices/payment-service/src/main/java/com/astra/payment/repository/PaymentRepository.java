
package com.astra.payment.repository;

import com.astra.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByCustomerEmailAndIdempotencyKey(
        String customerEmail,
        String idempotencyKey
    );

    Optional<Payment> findByOrderNumber(String orderNumber);

    List<Payment> findByCustomerEmailOrderByCreatedAtDesc(
        String customerEmail
    );
    Optional<Payment> findByPaymentReferenceAndCustomerEmail(
    	    String paymentReference,
    	    String customerEmail
    	);
}

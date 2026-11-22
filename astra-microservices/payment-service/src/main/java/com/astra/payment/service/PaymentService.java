
package com.astra.payment.service;

import com.astra.payment.dto.PaymentRequest;
import com.astra.payment.dto.PaymentResponse;
import com.astra.payment.entity.Payment;
import com.astra.payment.entity.PaymentStatus;
import com.astra.payment.gateway.PaymentGateway;
import com.astra.payment.gateway.MockPaymentResult;
import com.astra.payment.repository.PaymentRepository;
import com.astra.payment.util.PaymentFingerprint;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentOutboxService paymentOutboxService;

    public PaymentService(
    	    PaymentRepository paymentRepository,
    	    PaymentGateway paymentGateway,
    	    PaymentOutboxService paymentOutboxService
    	) {
    	    this.paymentRepository = paymentRepository;
    	    this.paymentGateway = paymentGateway;
    	    this.paymentOutboxService = paymentOutboxService;
    	}

    @Transactional
    public PaymentResponse createPayment(
        String customerEmail,
        String idempotencyKey,
        PaymentRequest request
    ) {
        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A valid Idempotency-Key header is required"
            );
        }

        String normalizedEmail = customerEmail.trim().toLowerCase();
        String requestHash = PaymentFingerprint.create(request);

        // Return an existing payment for an identical retry.
        var existing = paymentRepository
            .findByCustomerEmailAndIdempotencyKey(
                normalizedEmail,
                idempotencyKey
            );

        if (existing.isPresent()) {
            Payment payment = existing.get();

            if (!requestHash.equals(payment.getRequestHash())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Idempotency key was already used with different payment details"
                );
            }

            return toResponse(payment);
        }

        Payment payment = new Payment();

        payment.setPaymentReference(
            "PAY-" + UUID.randomUUID()
        );

        payment.setOrderNumber(request.orderNumber().trim());
        payment.setCustomerEmail(normalizedEmail);
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency().toUpperCase());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setIdempotencyKey(idempotencyKey);
        payment.setRequestHash(requestHash);
        payment.setPaymentStatus(PaymentStatus.PROCESSING);

        paymentRepository.saveAndFlush(payment);

        // Invoke the mock payment provider.
        MockPaymentResult result =
            paymentGateway.processPayment(payment);

        payment.setPaymentStatus(result.status());
        payment.setGatewayTransactionId(result.transactionId());
        payment.setFailureReason(result.failureReason());

        Payment savedPayment = paymentRepository.save(payment);

     // Save the Kafka event in the same database transaction.
     paymentOutboxService.recordPaymentResult(savedPayment);

     return toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(
        String customerEmail,
        String paymentReference
    ) {
        Payment payment = paymentRepository
            .findByPaymentReferenceAndCustomerEmail(
                paymentReference,
                customerEmail.trim().toLowerCase()
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Payment not found"
            ));

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(String customerEmail) {

        return paymentRepository
            .findByCustomerEmailOrderByCreatedAtDesc(
                customerEmail.trim().toLowerCase()
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getPaymentReference(),
            payment.getOrderNumber(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getPaymentMethod().name(),
            payment.getPaymentStatus(),
            payment.getGatewayTransactionId(),
            payment.getFailureReason(),
            payment.getCreatedAt()
        );
    }
}

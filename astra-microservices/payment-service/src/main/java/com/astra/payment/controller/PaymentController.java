
package com.astra.payment.controller;

import com.astra.payment.dto.PaymentRequest;
import com.astra.payment.dto.PaymentResponse;
import com.astra.payment.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody PaymentRequest request,
        Authentication authentication
    ) {
        PaymentResponse response = paymentService.createPayment(
            authentication.getName(),
            idempotencyKey,
            request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/{paymentReference}")
    public ResponseEntity<PaymentResponse> getPayment(
        @PathVariable String paymentReference,
        Authentication authentication
    ) {
        return ResponseEntity.ok(
            paymentService.getPayment(
                authentication.getName(),
                paymentReference
            )
        );
    }

    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
        Authentication authentication
    ) {
        return ResponseEntity.ok(
            paymentService.getMyPayments(authentication.getName())
        );
    }
}

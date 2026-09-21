package com.astra.controller;

import com.astra.dto.PaymentInitiateRequest;
import com.astra.dto.PaymentResponse;
import com.astra.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService
    ) {

        this.paymentService =
                paymentService;
    }

    /**
     * Initiate payment.
     */
    @PostMapping("/orders/{orderId}/initiate")
    public ResponseEntity<PaymentResponse>
    initiatePayment(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody
            PaymentInitiateRequest request
    ) {

        return ResponseEntity.ok(
                paymentService.initiatePayment(
                        authentication.getName(),
                        orderId,
                        request
                )
        );
    }

    /**
     * Get payment.
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse>
    getPayment(
            Authentication authentication,
            @PathVariable Long paymentId
    ) {

        return ResponseEntity.ok(
                paymentService.getPayment(
                        authentication.getName(),
                        paymentId
                )
        );
    }

    /**
     * Mock success endpoint.
     *
     * This is ONLY for local development/testing.
     */
    @PostMapping("/{paymentId}/success")
    public ResponseEntity<PaymentResponse>
    markSuccess(
            Authentication authentication,
            @PathVariable Long paymentId
    ) {

        return ResponseEntity.ok(
                paymentService.markSuccess(
                        authentication.getName(),
                        paymentId
                )
        );
    }

    /**
     * Mock failure endpoint.
     */
    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse>
    markFailed(
            Authentication authentication,
            @PathVariable Long paymentId,
            @RequestParam(
                    required = false
            )
            String reason
    ) {

        return ResponseEntity.ok(
                paymentService.markFailed(
                        authentication.getName(),
                        paymentId,
                        reason
                )
        );
    }

    /**
     * Refund payment.
     *
     * Local/mock implementation for Phase 10.
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse>
    refundPayment(
            Authentication authentication,
            @PathVariable Long paymentId
    ) {

        return ResponseEntity.ok(
                paymentService.refundPayment(
                        authentication.getName(),
                        paymentId
                )
        );
    }
}

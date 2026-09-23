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
        this.paymentService = paymentService;
    }

    /**
     * Initiate payment for an order.
     */
    @PostMapping("/orders/{orderId}/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentInitiateRequest request
    ) {

        PaymentResponse response =
                paymentService.initiatePayment(
                        authentication.getName(),
                        orderId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by payment ID.
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            Authentication authentication,
            @PathVariable Long paymentId
    ) {

        PaymentResponse response =
                paymentService.getPayment(
                        authentication.getName(),
                        paymentId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Get payment for an order.
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> getOrderPayment(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        PaymentResponse response =
                paymentService.getOrderPayment(
                        authentication.getName(),
                        orderId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Mock/sandbox payment success.
     *
     * For local development/testing only.
     */
    @PostMapping("/{paymentId}/success")
    public ResponseEntity<PaymentResponse> markSuccess(
            Authentication authentication,
            @PathVariable Long paymentId
    ) {

        PaymentResponse response =
                paymentService.markSuccess(
                        authentication.getName(),
                        paymentId
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Mock payment failure.
     *
     * For local development/testing.
     */
    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> markFailed(
            Authentication authentication,
            @PathVariable Long paymentId,
            @RequestParam(required = false) String reason
    ) {

        PaymentResponse response =
                paymentService.markFailed(
                        authentication.getName(),
                        paymentId,
                        reason
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Refund a paid payment.
     *
     * Local/mock implementation for Phase 10.
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            Authentication authentication,
            @PathVariable Long paymentId
    ) {

        PaymentResponse response =
                paymentService.refundPayment(
                        authentication.getName(),
                        paymentId
                );

        return ResponseEntity.ok(response);
    }
}

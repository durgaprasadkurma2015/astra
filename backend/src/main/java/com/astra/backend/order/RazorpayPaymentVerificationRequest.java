package com.astra.backend.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RazorpayPaymentVerificationRequest(
        @NotBlank @Size(max = 80) @Pattern(regexp = "[A-Za-z0-9_-]+")
        String razorpayOrderId,
        @NotBlank @Size(max = 80) @Pattern(regexp = "[A-Za-z0-9_-]+")
        String razorpayPaymentId,
        @NotBlank @Pattern(regexp = "[a-fA-F0-9]{64}")
        String razorpaySignature) {}

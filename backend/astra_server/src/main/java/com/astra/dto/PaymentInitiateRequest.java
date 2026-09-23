package com.astra.dto;

import com.astra.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;

public record PaymentInitiateRequest(

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod

) {
}

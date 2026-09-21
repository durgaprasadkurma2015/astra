package com.astra.integration;

public record PaymentGatewayResult(

        boolean success,

        String providerPaymentId,

        String providerOrderId,

        String reference,

        String message

) {
}
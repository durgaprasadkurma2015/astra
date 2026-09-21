package com.astra.integration;

import com.astra.entity.PaymentTransaction;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway
        implements PaymentGateway {

    @Override
    public PaymentGatewayResult createPayment(
            PaymentTransaction transaction
    ) {

        String providerOrderId =
                "MOCK_ORDER_"
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();

        return new PaymentGatewayResult(
                true,
                null,
                providerOrderId,
                null,
                "Mock payment order created"
        );
    }

    @Override
    public PaymentGatewayResult verifyPayment(
            PaymentTransaction transaction
    ) {

        String providerPaymentId =
                "MOCK_PAYMENT_"
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();

        return new PaymentGatewayResult(
                true,
                providerPaymentId,
                transaction.getProviderOrderId(),
                "MOCK_SUCCESS",
                "Mock payment verified successfully"
        );
    }

    @Override
    public PaymentGatewayResult refundPayment(
            PaymentTransaction transaction
    ) {

        String refundReference =
                "MOCK_REFUND_"
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();

        return new PaymentGatewayResult(
                true,
                transaction.getProviderPaymentId(),
                transaction.getProviderOrderId(),
                refundReference,
                "Mock payment refunded successfully"
        );
    }
}


package com.astra.payment.gateway;

import com.astra.payment.entity.Payment;
import com.astra.payment.entity.PaymentStatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(
    name = "app.payment.gateway",
    havingValue = "mock",
    matchIfMissing = true
)
public class MockPaymentGateway implements PaymentGateway {

    private final PaymentStatus defaultStatus;

    public MockPaymentGateway(
        @Value("${app.payment.mock.default-result:SUCCESS}")
        String defaultResult
    ) {
        PaymentStatus configuredStatus;

        try {
            configuredStatus = PaymentStatus.valueOf(
                defaultResult.trim().toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                "Invalid mock payment result: " + defaultResult
            );
        }

        if (configuredStatus != PaymentStatus.SUCCESS
                && configuredStatus != PaymentStatus.FAILED
                && configuredStatus != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                "Mock result must be SUCCESS, FAILED or PENDING"
            );
        }

        this.defaultStatus = configuredStatus;
    }

    @Override
    public MockPaymentResult processPayment(Payment payment) {

        String transactionId =
            "MOCK-" + UUID.randomUUID();

        if (defaultStatus == PaymentStatus.SUCCESS) {
            return new MockPaymentResult(
                PaymentStatus.SUCCESS,
                transactionId,
                null
            );
        }

        if (defaultStatus == PaymentStatus.FAILED) {
            return new MockPaymentResult(
                PaymentStatus.FAILED,
                transactionId,
                "Simulated payment failure"
            );
        }

        return new MockPaymentResult(
            PaymentStatus.PENDING,
            transactionId,
            null
        );
    }
}

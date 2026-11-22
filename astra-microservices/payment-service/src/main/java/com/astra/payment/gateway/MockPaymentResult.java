
package com.astra.payment.gateway;

import com.astra.payment.entity.PaymentStatus;

public record MockPaymentResult(
    PaymentStatus status,
    String transactionId,
    String failureReason
) {
}


package com.astra.payment.dto;

import com.astra.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
    String paymentReference,
    String orderNumber,
    BigDecimal amount,
    String currency,
    String paymentMethod,
    PaymentStatus paymentStatus,
    String gatewayTransactionId,
    String failureReason,
    LocalDateTime createdAt
) {
}

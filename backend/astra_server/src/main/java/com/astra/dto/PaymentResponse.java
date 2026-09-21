package com.astra.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.astra.enums.PaymentMethod;
import com.astra.enums.PaymentStatus;

public record PaymentResponse(

        Long id,

        String transactionNumber,

        Long orderId,

        String orderNumber,

        Long userId,

        PaymentStatus status,

        PaymentMethod paymentMethod,

        BigDecimal amount,

        String currency,

        String providerPaymentId,

        String providerOrderId,

        String failureReason,

        String refundReference,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
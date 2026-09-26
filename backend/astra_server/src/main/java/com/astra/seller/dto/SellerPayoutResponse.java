package com.astra.seller.dto;

import com.astra.enums.PayoutStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SellerPayoutResponse(
        Long id, BigDecimal amount, PayoutStatus status,
        String payoutReference, String failureReason,
        LocalDateTime createdAt, LocalDateTime processedAt
) {}

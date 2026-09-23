package com.astra.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderSummaryResponse(

    Long id,

    String orderNumber,

    String customerName,

    String customerEmail,

    BigDecimal total,

    String status,

    String paymentStatus,

    LocalDateTime createdAt

) {
}
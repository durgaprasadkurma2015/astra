package com.astra.seller.dto;

import com.astra.enums.OrderStatus;
import com.astra.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SellerOrderResponse(
        Long orderId, String orderNumber, OrderStatus status,
        PaymentStatus paymentStatus, BigDecimal sellerSubtotal,
        LocalDateTime createdAt
) {}

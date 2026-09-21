package com.astra.order.dto;

import com.astra.dto.OrderItemResponse;
import com.astra.enums.OrderStatus;
import com.astra.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

        Long id,

        String orderNumber,

        Long userId,

        OrderStatus status,

        PaymentStatus paymentStatus,

        BigDecimal subtotal,

        BigDecimal discount,

        BigDecimal shippingCharge,

        BigDecimal tax,

        BigDecimal total,

        String shippingFullName,

        String shippingPhone,

        String shippingAddressLine1,

        String shippingAddressLine2,

        String shippingCity,

        String shippingState,

        String shippingPostalCode,

        String shippingCountry,

        List<OrderItemResponse> items,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}


package com.astra.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderNumber,
    String customerName,
    String phoneNumber,
    String shippingAddress,
    String orderStatus,
    String paymentMethod,
    String paymentStatus,
    BigDecimal subtotal,
    String couponCode,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    List<OrderItemResponse> items
) {}

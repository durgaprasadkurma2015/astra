package com.astra.backend.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
    @NotBlank
    @Size(max = 150)
    String customerName,

    @NotBlank
    @Size(max = 20)
    String phoneNumber,

    @NotBlank
    @Size(max = 2000)
    String shippingAddress,

    @Size(max = 50)
    String couponCode,

    @Size(max = 20)
    String paymentMethod
) {}

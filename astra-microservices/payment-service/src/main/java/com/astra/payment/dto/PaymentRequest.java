
package com.astra.payment.dto;

import com.astra.payment.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PaymentRequest(

    @NotBlank(message = "Order number is required")
    String orderNumber,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount,

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "INR", message = "Only INR is supported by the mock gateway")
    String currency,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod

) {
}

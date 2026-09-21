package com.astra.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(

        @NotBlank
        @Size(max = 100)
        String shippingFullName,

        @NotBlank
        @Size(max = 30)
        String shippingPhone,

        @NotBlank
        @Size(max = 255)
        String shippingAddressLine1,

        @Size(max = 255)
        String shippingAddressLine2,

        @NotBlank
        @Size(max = 100)
        String shippingCity,

        @NotBlank
        @Size(max = 100)
        String shippingState,

        @NotBlank
        @Size(max = 20)
        String shippingPostalCode,

        @NotBlank
        @Size(max = 100)
        String shippingCountry

) {
}
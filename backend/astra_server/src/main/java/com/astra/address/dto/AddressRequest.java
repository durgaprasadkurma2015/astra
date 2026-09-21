package com.astra.address.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(

        @NotBlank
        String fullName,

        @NotBlank
        String phone,

        @NotBlank
        String addressLine1,

        String addressLine2,

        @NotBlank
        String city,

        @NotBlank
        String state,

        @NotBlank
        String postalCode,

        @NotBlank
        String country,

        boolean defaultAddress
) {
}
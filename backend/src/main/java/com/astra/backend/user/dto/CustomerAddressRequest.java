package com.astra.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerAddressRequest(
        @NotBlank @Size(max = 40) String label,
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(max = 20)
        @Pattern(regexp = "[0-9+()\\-\\s]{7,20}") String phoneNumber,
        @NotBlank @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @Size(max = 150) String landmark,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String district,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Size(max = 100) String country,
        boolean defaultAddress) { }

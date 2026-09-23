package com.astra.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifySmsOtpRequest(

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+?[1-9]\\d{9,14}$",
                message = "Invalid phone number"
        )
        String phone,

        @NotBlank(message = "OTP is required")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "OTP must be 6 digits"
        )
        String otp

) {}

package com.astra.auth.dto;

import jakarta.validation.constraints.*;

public record VerifyOtpRequest(
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = "\\d{6}") String otp
) {}

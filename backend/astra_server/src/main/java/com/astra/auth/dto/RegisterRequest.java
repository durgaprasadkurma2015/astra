package com.astra.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Email @Size(max = 190) String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    @Size(max = 30) String phone
) {}

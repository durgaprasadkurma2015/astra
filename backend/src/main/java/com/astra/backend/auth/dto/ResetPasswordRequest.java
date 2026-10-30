package com.astra.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank
    String resetToken,

    @NotBlank
    @Size(min = 8, max = 72)
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$"
    )
    String newPassword
) {}
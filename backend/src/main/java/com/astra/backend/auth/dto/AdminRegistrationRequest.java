package com.astra.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminRegistrationRequest(

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(
        min = 8,
        max = 72,
        message = "Password must be between 8 and 72 characters"
    )
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "Password must contain uppercase, lowercase, number, and special character"
    )
    String password

) {}
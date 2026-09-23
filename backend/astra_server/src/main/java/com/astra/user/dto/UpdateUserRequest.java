package com.astra.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(
                max = 120,
                message = "Name must not exceed 120 characters"
        )
        String name,

        @Email(
                message = "Invalid email address"
        )
        @Size(
                max = 190,
                message = "Email must not exceed 190 characters"
        )
        String email,

        @Pattern(
                regexp = "^[0-9+()\\- ]{7,30}$",
                message = "Invalid phone number"
        )
        String phone

) {
}

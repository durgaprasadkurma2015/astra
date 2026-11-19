package com.astra.customer.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CustomerRequest(

    Long authUserId,

    @NotBlank
    @Size(max = 100)
    String fullName,

    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    @Past
    LocalDate dateOfBirth,

    @Pattern(regexp = "^(|Male|Female|Other|Prefer not to say)$")
    String gender,

    @Pattern(regexp = "^[+]?[0-9 ()-]{7,20}$")
    String phoneNumber,

    @Pattern(regexp = "^[+]?[0-9 ()-]{7,20}$")
    String alternatePhoneNumber,

    @Size(max = 255)
    String addressLine1,

    @Size(max = 255)
    String addressLine2,

    @Size(max = 150)
    String landmark,

    @Size(max = 100)
    String city,

    @Size(max = 100)
    String district,

    @Size(max = 100)
    String state,

    @Size(max = 100)
    String country,

    @Size(max = 20)
    @Pattern(regexp = "^[A-Za-z0-9 -]*$")
    String postalCode,

    @Size(max = 50)
    String preferredLanguage
) {}
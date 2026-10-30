
package com.astra.backend.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserProfileResponse(
    Long id,
    String fullName,
    String email,
    LocalDate dateOfBirth,
    String gender,
    String phoneNumber,
    String alternatePhoneNumber,
    String addressLine1,
    String addressLine2,
    String landmark,
    String city,
    String district,
    String state,
    String country,
    String postalCode,
    String preferredLanguage,
    String role,
    boolean emailVerified,
    LocalDateTime createdAt
) {}

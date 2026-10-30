package com.astra.backend.user.dto;

import com.astra.backend.user.AppUser;
import java.time.LocalDateTime;

public record AdminCustomerResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        boolean active,
        boolean emailVerified,
        LocalDateTime createdAt) {

    public static AdminCustomerResponse from(AppUser user) {
        return new AdminCustomerResponse(user.getId(), user.getFullName(),
                user.getEmail(), user.getPhoneNumber(), user.isActive(),
                user.isEmailVerified(), user.getCreatedAt());
    }
}

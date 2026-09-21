package com.astra.dto;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String phone,
        String role,
        boolean emailVerified,
        boolean phoneVerified
) {
}
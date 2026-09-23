package com.astra.user.dto;

import java.time.Instant;

public record UserResponse(

        Long id,

        String name,

        String email,

        String phone,

        String role,

        boolean enabled,

        boolean emailVerified,

        boolean phoneVerified,

        Instant createdAt,

        Instant updatedAt

) {
}

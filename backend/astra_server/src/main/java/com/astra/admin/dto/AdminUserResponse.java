package com.astra.admin.dto;

import com.astra.entity.Role;

import java.time.Instant;

public record AdminUserResponse(

    Long id,

    String name,

    String email,

    String phone,

    Role.RoleName role,

    boolean enabled,

    boolean emailVerified,

    boolean phoneVerified,

    Instant createdAt,

    Instant updatedAt

) {
}
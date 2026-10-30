package com.astra.backend.auth.dto;

import java.time.LocalDateTime;

public record AdminRegistrationResponse(
    Long id,
    String fullName,
    String email,
    String role,
    boolean active,
    boolean emailVerified,
    LocalDateTime createdAt
) {}
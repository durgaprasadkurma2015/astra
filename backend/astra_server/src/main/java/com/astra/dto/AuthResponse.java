package com.astra.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    Long id,
    String name,
    String email,
    String phone,
    String role
) {}

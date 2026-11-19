package com.astra.auth.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String email,
    String role
) {}
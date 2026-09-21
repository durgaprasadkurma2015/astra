package com.astra.dto;

import java.time.LocalDateTime;

public record AdminUserResponse(

        Long id,

        String name,

        String email,

        boolean enabled,

        LocalDateTime createdAt

) {
}
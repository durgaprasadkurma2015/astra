package com.astra.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(

        @NotBlank
        String name,

        String description,

        String imageUrl,

        boolean active
) {
}
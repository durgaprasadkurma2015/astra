package com.astra.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductImageRequest(

        @NotBlank
        String imageUrl,

        boolean primaryImage,

        Integer displayOrder

) {
}
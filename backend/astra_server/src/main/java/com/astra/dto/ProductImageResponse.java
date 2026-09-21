package com.astra.dto;

public record ProductImageResponse(

        Long id,

        Long productId,

        String imageUrl,

        String originalFileName,

        String storedFileName,

        String contentType,

        Long fileSize,

        boolean primaryImage,

        Integer displayOrder

) {
}
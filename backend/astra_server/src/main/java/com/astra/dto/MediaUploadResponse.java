package com.astra.dto;

public record MediaUploadResponse(

        String fileName,

        String originalFileName,

        String contentType,

        long fileSize,

        String url

) {
}
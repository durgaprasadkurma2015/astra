package com.astra.backend.storage;

import java.time.LocalDateTime;

public record StoredFileResponse(
        Long id,
        String originalFilename,
        String contentType,
        long fileSize,
        String purpose,
        String ownerUsername,
        Long entityId,
        LocalDateTime createdAt,
        String downloadUrl,
        String contentUrl
) {
    public static StoredFileResponse from(StoredFile file) {
        String base = "/api/v1/files/" + file.getId();

        return new StoredFileResponse(
                file.getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getFileSize(),
                file.getPurpose(),
                file.getOwnerUsername(),
                file.getEntityId(),
                file.getCreatedAt(),
                base + "/download",
//                "PRODUCT_IMAGE".equals(file.getPurpose())
//                        ? "/api/v1/files/public/"
//                            + file.getId() + "/content"
//                        : null
                
                "PRODUCT_IMAGE".equals(file.getPurpose())
                ? "/api/v1/files/public/" + file.getId() + "/download"
                : base + "/download"
        );
    }
}
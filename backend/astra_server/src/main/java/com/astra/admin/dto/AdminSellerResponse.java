package com.astra.admin.dto;

import com.astra.enums.SellerStatus;

import java.time.LocalDateTime;

public record AdminSellerResponse(

    Long id,

    Long userId,

    String userName,

    String userEmail,

    String storeName,

    String storeSlug,

    String description,

    String phone,

    SellerStatus status,

    LocalDateTime createdAt,

    LocalDateTime updatedAt

) {
}
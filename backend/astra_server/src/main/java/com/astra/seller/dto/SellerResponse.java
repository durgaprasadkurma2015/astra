package com.astra.seller.dto;

import com.astra.enums.SellerStatus;

import java.time.LocalDateTime;

public record SellerResponse(

        Long id,

        String storeName,

        String storeSlug,

        String description,

        String phone,

        SellerStatus status,

        LocalDateTime createdAt

) {
}
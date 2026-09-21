package com.astra.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerRegistrationRequest(

        @NotBlank
        @Size(max = 150)
        String storeName,

        @NotBlank
        @Size(max = 180)
        String storeSlug,

        @Size(max = 2000)
        String description,

        @Size(max = 30)
        String phone

) {
}

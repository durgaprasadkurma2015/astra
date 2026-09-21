package com.astra.dto;

import jakarta.validation.constraints.NotNull;

public record AddWishlistItemRequest(

        @NotNull
        Long productId

) {
}
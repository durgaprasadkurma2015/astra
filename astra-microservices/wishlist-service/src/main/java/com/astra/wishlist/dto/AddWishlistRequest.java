
package com.astra.wishlist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddWishlistRequest(
        @NotNull
        @Positive
        Long productId
) {}

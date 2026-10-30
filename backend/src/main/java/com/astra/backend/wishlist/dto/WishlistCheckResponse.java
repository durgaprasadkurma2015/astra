package com.astra.backend.wishlist.dto;

public record WishlistCheckResponse(
    Long productId,
    boolean inWishlist
) {
}
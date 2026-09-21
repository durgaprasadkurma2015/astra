package com.astra.wishlist.dto;

import java.util.List;

import com.astra.dto.WishlistItemResponse;

public record WishlistResponse(

        Long wishlistId,

        Long userId,

        List<WishlistItemResponse> items,

        Integer totalItems

) {
}